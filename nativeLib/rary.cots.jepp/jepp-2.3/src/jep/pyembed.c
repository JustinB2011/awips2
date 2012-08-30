/* -*- Mode: C; indent-tabs-mode: nil; c-basic-offset: 4 c-style: "K&R" -*- */
/*
   jep - Java Embedded Python

   Copyright (c) 2004 - 2008 Mike Johnson.

   This file is licenced under the the zlib/libpng License.

   This software is provided 'as-is', without any express or implied
   warranty. In no event will the authors be held liable for any
   damages arising from the use of this software.

   Permission is granted to anyone to use this software for any
   purpose, including commercial applications, and to alter it and
   redistribute it freely, subject to the following restrictions:

   1. The origin of this software must not be misrepresented; you
   must not claim that you wrote the original software. If you use
   this software in a product, an acknowledgment in the product
   documentation would be appreciated but is not required.

   2. Altered source versions must be plainly marked as such, and
   must not be misrepresented as being the original software.

   3. This notice may not be removed or altered from any source
   distribution.


   *****************************************************************************
   This file handles two main things:
   - startup, shutdown of interpreters.
      (those are the pyembed_* functions)
   - setting of parameters
      (the pyembed_set*)

   The really interesting stuff is not here. :-) This file simply makes calls
   to the type definitions for pyjobject, etc.
   *****************************************************************************
*/

/*
  August 2, 2012
  Modified by Raytheon (c) 2012 Raytheon Company. All Rights Reserved.
   Modifications marked and described by 'njensen'
*/


#ifdef WIN32
# include "winconfig.h"
#endif

#if HAVE_CONFIG_H
# include <config.h>
#endif

#if HAVE_UNISTD_H
# include <sys/types.h>
# include <unistd.h>
#endif

#include <stdlib.h> /* added by bkowal */

#if STDC_HEADERS
# include <stdio.h>
#endif

#include <ctype.h>

#include "pyembed.h"
#include "pyjobject.h"
#include "pyjarray.h"
#include "util.h"
// added by njensen
#include "numpy/arrayobject.h"
//#include "Numeric/arrayobject.h"


static PyThreadState *mainThreadState = NULL;

static PyObject* pyembed_findclass(PyObject*, PyObject*);
static PyObject* pyembed_forname(PyObject*, PyObject*);
static PyObject* pyembed_jimport(PyObject*, PyObject*);
static PyObject* pyembed_set_print_stack(PyObject*, PyObject*);
static PyObject* pyembed_jproxy(PyObject*, PyObject*);

static int maybe_pyc_file(FILE*, const char*, const char*, int);
static void pyembed_run_pyc(JepThread *jepThread, FILE *);


// ClassLoader.loadClass
static jmethodID loadClassMethod = 0;

// jep.ClassList.get()
static jmethodID getClassListMethod = 0;

// jep.Proxy.newProxyInstance
static jmethodID newProxyMethod = 0;

// Integer(int)
static jmethodID integerIConstructor = 0;

// Long(long)
static jmethodID longJConstructor = 0;

// Float(float)
static jmethodID floatFConstructor = 0;

// Boolean(boolean)
static jmethodID booleanBConstructor = 0;

static struct PyMethodDef jep_methods[] = {
    { "findClass",
      pyembed_findclass,
      METH_VARARGS,
      "Find and instantiate a system class, somewhat faster than forName." },

    { "forName",
      pyembed_forname,
      METH_VARARGS,
      "Find and return a jclass object using the supplied ClassLoader." },

    { "jarray",
      pyjarray_new_v,
      METH_VARARGS,
      "Create a new primitive array in Java.\n"
      "Accepts:\n"
      "(size, type _ID, [0]) || "
      "(size, JCHAR_ID, [string value] || "
      "(size, jobject) || "
      "(size, str) || "
      "(size, jarray)" },

    { "jimport",
      pyembed_jimport,
      METH_VARARGS,
      "Same definition as the standard __import__." },

    { "printStack",
      pyembed_set_print_stack,
      METH_VARARGS,
      "Turn on printing of stack traces (True|False)" },

    { "jproxy",
      pyembed_jproxy,
      METH_VARARGS,
      "Create a Proxy class for a Python object.\n"
      "Accepts two arguments: ([a class object], [list of java interfaces "
      "to implement, string names])" },

    { NULL, NULL }
};


static struct PyMethodDef noop_methods[] = {
    { NULL, NULL }
};


static PyObject* initjep(void) {
    PyObject *modjep;

    PyImport_AddModule("jep");
    Py_InitModule((char *) "jep", jep_methods);
    modjep = PyImport_ImportModule("jep");
    if(modjep == NULL)
        printf("WARNING: couldn't import module jep.\n");
    else {
#ifdef VERSION
        PyModule_AddStringConstant(modjep, "VERSION", VERSION);
#endif

        // stuff for making new pyjarray objects
        PyModule_AddIntConstant(modjep, "JBOOLEAN_ID", JBOOLEAN_ID);
        PyModule_AddIntConstant(modjep, "JINT_ID", JINT_ID);
        PyModule_AddIntConstant(modjep, "JLONG_ID", JLONG_ID);
        PyModule_AddIntConstant(modjep, "JSTRING_ID", JSTRING_ID);
        PyModule_AddIntConstant(modjep, "JDOUBLE_ID", JDOUBLE_ID);
        PyModule_AddIntConstant(modjep, "JSHORT_ID", JSHORT_ID);
        PyModule_AddIntConstant(modjep, "JFLOAT_ID", JFLOAT_ID);
        PyModule_AddIntConstant(modjep, "JCHAR_ID", JCHAR_ID);
        PyModule_AddIntConstant(modjep, "JBYTE_ID", JBYTE_ID);
    }

    return modjep;
}


void pyembed_startup(void) {
    if(mainThreadState != NULL)
        return;

    Py_OptimizeFlag = 1;

    Py_Initialize();
    PyEval_InitThreads();

    // save a pointer to the main PyThreadState object
    mainThreadState = PyThreadState_Get();
    PyEval_ReleaseLock();
}


void pyembed_shutdown(void) {
    printf("Shutting down Python...\n");
    PyEval_AcquireLock();
    PyThreadState_Swap(mainThreadState);
    Py_Finalize();
}


intptr_t pyembed_thread_init(JNIEnv *env, jobject cl, jobject caller) {
    JepThread *jepThread;
    PyObject  *tdict, *main, *globals;

    if(cl == NULL) {
        THROW_JEP(env, "Invalid Classloader.");
        return 0;
    }

    PyEval_AcquireLock();
    Py_NewInterpreter();

    jepThread = PyMem_Malloc(sizeof(JepThread));
    if(!jepThread) {
        THROW_JEP(env, "Out of memory.");
        PyEval_ReleaseLock();
        return 0;
    }

    jepThread->tstate = PyEval_SaveThread();
    PyEval_RestoreThread(jepThread->tstate);

    // store primitive java.lang.Class objects for later use.
    // it's a noop if already done, but to synchronize, have the lock first
    if(!cache_primitive_classes(env))
        printf("WARNING: failed to get primitive class types.\n");

    main = PyImport_AddModule("__main__");                      /* borrowed */
    if(main == NULL) {
        THROW_JEP(env, "Couldn't add module __main__.");
        PyEval_ReleaseLock();
        return 0;
    }

    globals = PyModule_GetDict(main);
    Py_INCREF(globals);

    // init static module
    jepThread->modjep      = initjep();
    jepThread->globals     = globals;
    jepThread->env         = env;
    jepThread->classloader = (*env)->NewGlobalRef(env, cl);
    jepThread->caller      = (*env)->NewGlobalRef(env, caller);
    jepThread->printStack  = 0;

    // now, add custom import function to builtin module

    // i did have a whole crap load of code to do this from C but it
    // didn't work. i found a PEP that said it wasn't possible, then
    // Guido said they were wrong. *shrug*. this is my work-around.

// commented out by njensen in favor of JavaImporter.py
    PyRun_SimpleString("import jep\n");
    //PyRun_SimpleString("__builtins__.__import__ = jep.jimport\n");

    if((tdict = PyThreadState_GetDict()) != NULL) {
        PyObject *key, *t;

        t   = (PyObject *) PyCObject_FromVoidPtr((void *) jepThread, NULL);
        key = PyString_FromString(DICT_KEY);

        PyDict_SetItem(tdict, key, t);   /* takes ownership */

        Py_DECREF(key);
        Py_DECREF(t);
    }

    PyEval_SaveThread();
    return (intptr_t) jepThread;
}


void pyembed_thread_close(intptr_t _jepThread) {
    PyThreadState *prevThread, *thread;
    JepThread     *jepThread;
    PyObject      *tdict, *key;
    JNIEnv        *env;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        printf("WARNING: thread_close, invalid JepThread pointer.\n");
        return;
    }

    env = jepThread->env;
    if(!env) {
        printf("WARNING: thread_close, invalid env pointer.\n");
        return;
    }

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    key = PyString_FromString(DICT_KEY);
    if((tdict = PyThreadState_GetDict()) != NULL && key != NULL)
        PyDict_DelItem(tdict, key);
    Py_DECREF(key);

    if(jepThread->globals)
        Py_DECREF(jepThread->globals);
    if(jepThread->modjep)
        Py_DECREF(jepThread->modjep);
    if(jepThread->classloader)
        (*env)->DeleteGlobalRef(env, jepThread->classloader);
    if(jepThread->caller)
        (*env)->DeleteGlobalRef(env, jepThread->caller);

    Py_EndInterpreter(jepThread->tstate);

    PyMem_Free(jepThread);
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
}


JNIEnv* pyembed_get_env(void) {
    JavaVM *jvm;
    JNIEnv *env;

    JNI_GetCreatedJavaVMs(&jvm, 1, NULL);
    (*jvm)->AttachCurrentThread(jvm, (void**) &env, NULL);

    return env;
}


// get thread struct when called from internals.
// NULL if not found.
// hold the lock before calling.
JepThread* pyembed_get_jepthread(void) {
    PyObject  *tdict, *t, *key;
    JepThread *ret = NULL;

    key = PyString_FromString(DICT_KEY);
    if((tdict = PyThreadState_GetDict()) != NULL && key != NULL) {
        t = PyDict_GetItem(tdict, key); /* borrowed */
        if(t != NULL && !PyErr_Occurred())
            ret = (JepThread*) PyCObject_AsVoidPtr(t);
    }

    Py_DECREF(key);
    return ret;
}


// used by _jimport and _forname
#define LOAD_CLASS_METHOD(env, cl)                                          \
{                                                                           \
    if(loadClassMethod == 0) {                                              \
        jobject clazz;                                                      \
                                                                            \
        clazz = (*env)->GetObjectClass(env, cl);                            \
        if(process_java_exception(env) || !clazz)                           \
            return NULL;                                                    \
                                                                            \
        loadClassMethod =                                                   \
            (*env)->GetMethodID(env,                                        \
                                clazz,                                      \
                                "loadClass",                                \
                                "(Ljava/lang/String;)Ljava/lang/Class;");   \
                                                                            \
        if(process_java_exception(env) || !loadClassMethod) {               \
            (*env)->DeleteLocalRef(env, clazz);                             \
            return NULL;                                                    \
        }                                                                   \
                                                                            \
        (*env)->DeleteLocalRef(env, clazz);                                 \
    }                                                                       \
}


static PyObject* pyembed_jproxy(PyObject *self, PyObject *args) {
    PyThreadState *_save;
    JepThread     *jepThread;
    JNIEnv        *env = NULL;
    PyObject      *pytarget;
    PyObject      *interfaces;
    jclass         clazz;
    jobject        cl;
    jobject        classes;
    int            inum, i;
    jobject        proxy;

	if(!PyArg_ParseTuple(args, "OO!:jproxy",
                         &pytarget,
                         &PyList_Type,
                         &interfaces))
        return NULL;

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        if(!PyErr_Occurred())
            PyErr_SetString(PyExc_RuntimeError, "Invalid JepThread pointer.");
        return NULL;
    }

    env = jepThread->env;
    cl  = jepThread->classloader;

    Py_UNBLOCK_THREADS;
    clazz = (*env)->FindClass(env, "jep/Proxy");
    Py_BLOCK_THREADS;
    if(process_java_exception(env) || !clazz)
        return NULL;

    if(newProxyMethod == 0) {
        newProxyMethod =
            (*env)->GetStaticMethodID(
                env,
                clazz,
                "newProxyInstance",
                "(JJLjep/Jep;Ljava/lang/ClassLoader;[Ljava/lang/String;)Ljava/lang/Object;");

        if(process_java_exception(env) || !newProxyMethod)
            return NULL;
    }

    inum = PyList_GET_SIZE(interfaces);
    if(inum < 1)
        return PyErr_Format(PyExc_ValueError, "Empty interface list.");

    // now convert string list to java array

    classes = (*env)->NewObjectArray(env, inum, JSTRING_TYPE, NULL);
    if(process_java_exception(env) || !classes)
        return NULL;

    for(i = 0; i < inum; i++) {
        char     *str;
        jstring   jstr;
        PyObject *item;

        item = PyList_GET_ITEM(interfaces, i);
        if(!PyString_Check(item))
            return PyErr_Format(PyExc_ValueError, "Item %i not a string.", i);

        str  = PyString_AsString(item);
        jstr = (*env)->NewStringUTF(env, (const char *) str);

        (*env)->SetObjectArrayElement(env, classes, i, jstr);
        (*env)->DeleteLocalRef(env, jstr);
    }

    // do the deed
    proxy = (*env)->CallStaticObjectMethod(env,
                                           clazz,
                                           newProxyMethod,
                                           (jlong) (intptr_t) jepThread,
                                           (jlong) (intptr_t) pytarget,
                                           jepThread->caller,
                                           cl,
                                           classes);
    if(process_java_exception(env) || !proxy)
        return NULL;

    // make sure target doesn't get garbage collected
    Py_INCREF(pytarget);

    return pyjobject_new(env, proxy);
}


static PyObject* pyembed_set_print_stack(PyObject *self, PyObject *args) {
    JepThread *jepThread;
    JNIEnv    *env   = NULL;
    char      *print = 0;

	if(!PyArg_ParseTuple(args, "b:setPrintStack", &print))
        return NULL;

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        if(!PyErr_Occurred())
            PyErr_SetString(PyExc_RuntimeError, "Invalid JepThread pointer.");
        return NULL;
    }

    if(print == 0)
        jepThread->printStack = 0;
    else
        jepThread->printStack = 1;

    Py_INCREF(Py_None);
    return Py_None;
}


/*
 * njensen: I rewrote this for the umpteenth time to get it working nicely with
 * the OSGI classloader and python 2.6.  Most of the work is now performed
 * in JavaImporter.py.  This method retrieves the Java class and wraps it in
 * a pyjclass and returns it.  Module management is in JavaImporter.py now.
 */
static PyObject* pyembed_jimport(PyObject* self, PyObject *arg) {
    PyThreadState *_save;
    JNIEnv        *env = NULL;
    jclass         clazz;
    jobject        cl;
    JepThread     *jepThread;

    PyObject     *ptype = NULL;
    PyObject     *pvalue = NULL;
    PyObject     *ptrace = NULL;
    char *cname;
	jstring member;

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        if(!PyErr_Occurred() || ptype != NULL)
        if(ptype != NULL)
    	{
    		Py_DECREF(ptype);
    	}
    	if(pvalue != NULL)
    	{
    		Py_DECREF(pvalue);
    	}
    	if(ptrace != NULL)
    	{
    		Py_DECREF(ptrace);
    	}
        PyErr_SetString(PyExc_RuntimeError, "Invalid JepThread pointer.");
        return NULL;
    }

    env = jepThread->env;
    cl  = jepThread->classloader;

    LOAD_CLASS_METHOD(env, cl);
    {
    	int i;
    	jclass objclazz;
    	PyObject *pclass;
    	if(!PyArg_ParseTuple(arg, "s:__import__",  &cname))
    	{
			return NULL;
    	}

		// check for _ to indicate enums, replace _ with $ for inner class
		i = 0;
		while(1) {
			if(cname[i] == '_')
			{
				cname[i] = '$';
			}
			i += 1;
			if(cname[i] == '\0')
			{
				break;
			}
		}

		member = (*env)->NewStringUTF(env, cname);

		Py_UNBLOCK_THREADS;
		objclazz = (jclass) (*env)->CallObjectMethod(env,
									 cl,
									 loadClassMethod,
									 member);
		Py_BLOCK_THREADS;

		if((*env)->ExceptionOccurred(env) != NULL || !objclazz) {
			// this error we ignore
			// (*env)->ExceptionDescribe(env);
			(*env)->DeleteLocalRef(env, member);
			(*env)->ExceptionClear(env);
			if(ptype != NULL)
			{
				Py_DECREF(ptype);
			}
			if(pvalue != NULL)
			{
				Py_DECREF(pvalue);
			}
			if(ptrace != NULL)
			{
				Py_DECREF(ptrace);
			}
			return PyErr_Format(
				PyExc_ImportError,
				"LoadClass(%s) failed.",
				cname);
			}

		 // make a new class object
		pclass = (PyObject *) pyjobject_new_class(env, objclazz);
		(*env)->DeleteLocalRef(env, member);
		return pclass;
    }
}


static PyObject* pyembed_forname(PyObject *self, PyObject *args) {
    JNIEnv    *env       = NULL;
    char      *name;
    jobject    cl;
    jclass     objclazz;
    jstring    jstr;
    JepThread *jepThread;

    if(!PyArg_ParseTuple(args, "s", &name))
        return NULL;

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        if(!PyErr_Occurred())
            PyErr_SetString(PyExc_RuntimeError, "Invalid JepThread pointer.");
        return NULL;
    }

    env = jepThread->env;
    cl  = jepThread->classloader;

    LOAD_CLASS_METHOD(env, cl);

    jstr = (*env)->NewStringUTF(env, (const char *) name);
    if(process_java_exception(env) || !jstr)
        return NULL;

    objclazz = (jclass) (*env)->CallObjectMethod(env,
                                                 cl,
                                                 loadClassMethod,
                                                 jstr);
    if(process_java_exception(env) || !objclazz)
        return NULL;

    return (PyObject *) pyjobject_new_class(env, objclazz);
}


static PyObject* pyembed_findclass(PyObject *self, PyObject *args) {
    JNIEnv    *env       = NULL;
    char      *name, *p;
    jclass     clazz;
    JepThread *jepThread;

    if(!PyArg_ParseTuple(args, "s", &name))
        return NULL;

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        if(!PyErr_Occurred())
            PyErr_SetString(PyExc_RuntimeError, "Invalid JepThread pointer.");
        return NULL;
    }

    env = jepThread->env;

    // replace '.' with '/'
    // i'm told this is okay to do with unicode.
    for(p = name; *p != '\0'; p++) {
        if(*p == '.')
            *p = '/';
    }

    clazz = (*env)->FindClass(env, name);
    if(process_java_exception(env))
        return NULL;

    return (PyObject *) pyjobject_new_class(env, clazz);
}


jobject pyembed_invoke_method(JNIEnv *env,
                              intptr_t _jepThread,
                              const char *cname,
                              jobjectArray args,
                              jintArray types) {
    PyThreadState    *prevThread, *thread;
    PyObject         *callable;
    JepThread        *jepThread;
    jobject           ret;

    ret = NULL;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return ret;
    }

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    callable = PyDict_GetItemString(jepThread->globals, (char *) cname);
    if(!callable) {
        THROW_JEP(env, "Object was not found in the global dictionary.");
        goto EXIT;
    }
    if(process_py_exception(env, 0))
        goto EXIT;

    ret = pyembed_invoke(env, callable, args, types);

EXIT:
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();

    return ret;
}


// invoke object callable
// **** hold lock before calling ****
jobject pyembed_invoke(JNIEnv *env,
                       PyObject *callable,
                       jobjectArray args,
                       jintArray _types) {

    PyThreadState *_save;
    jobject        ret;
    int            iarg, arglen;
    jint          *types;       /* pinned primitive array */
    jboolean       isCopy;
    PyObject      *pyargs;      /* a tuple */
    PyObject      *pyret;

    types    = NULL;
    ret      = NULL;
    pyret    = NULL;

    if(!PyCallable_Check(callable)) {
        THROW_JEP(env, "pyembed:invoke Invalid callable.");
        return NULL;
    }

    // pin primitive array so we can get to it
    types = (*env)->GetIntArrayElements(env, _types, &isCopy);

    // first thing to do, convert java arguments to a python tuple
    arglen = (*env)->GetArrayLength(env, args);
    pyargs = PyTuple_New(arglen);
    for(iarg = 0; iarg < arglen; iarg++) {
        jobject   val;
        int       typeid;
        PyObject *pyval;

        val = (*env)->GetObjectArrayElement(env, args, iarg);
        if((*env)->ExceptionCheck(env)) /* careful, NULL is okay */
            goto EXIT;

        typeid = (int) types[iarg];

        // now we know the type, convert and add to pyargs.  we know
        pyval = convert_jobject(env, val, typeid);
        if((*env)->ExceptionOccurred(env))
            goto EXIT;

        PyTuple_SET_ITEM(pyargs, iarg, pyval); /* steals */
        if(val)
            (*env)->DeleteLocalRef(env, val);
    } // for(iarg = 0; iarg < arglen; iarg++)

    pyret = PyObject_CallObject(callable, pyargs);
    if(process_py_exception(env, 0) || !pyret)
        goto EXIT;

    // handles errors
    ret = pyembed_box_py(env, pyret);

EXIT:
    if(pyargs)
        Py_DECREF(pyargs);
    if(pyret)
        Py_DECREF(pyret);

    if(types) {
        (*env)->ReleaseIntArrayElements(env,
                                        _types,
                                        types,
                                        JNI_ABORT);

        (*env)->DeleteLocalRef(env, _types);
    }

    return ret;
}

long classCallCount;
long getjtypeCount;

void pyembed_eval(JNIEnv *env,
                  intptr_t _jepThread,
                  char *str) {
    PyThreadState    *prevThread, *thread;
    PyObject         *modjep, *result;
    JepThread        *jepThread;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return;
    }

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    if(str == NULL)
        goto EXIT;

    if(process_py_exception(env, 1))
        goto EXIT;

    classCallCount = 0;
    getjtypeCount = 0;
    result = PyRun_String(str,  /* new ref */
                          Py_single_input,
                          jepThread->globals,
                          jepThread->globals);
    //printf("pyjclass.pyjclass_call count: %i\n", classCallCount);
    //printf("getjtype count: %i\n", getjtypeCount);

    // c programs inside some java environments may get buffered output
    fflush(stdout);
    fflush(stderr);

    process_py_exception(env, 1);

    if(result != NULL)
        Py_DECREF(result);

EXIT:
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
}


// returns 1 if finished, 0 if not, throws exception otherwise
int pyembed_compile_string(JNIEnv *env,
                           intptr_t _jepThread,
                           char *str) {
    PyThreadState  *prevThread;
    PyObject       *code;
    int             ret = -1;
    JepThread      *jepThread;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return 0;
    }

    if(str == NULL)
        return 0;

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    code = Py_CompileString(str, "<stdin>", Py_single_input);

    if(code != NULL) {
        Py_DECREF(code);
        ret = 1;
    }
    else if(PyErr_ExceptionMatches(PyExc_SyntaxError)) {
        PyErr_Clear();
        ret = 0;
    }
    else
        process_py_exception(env, 0);

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return ret;
}


intptr_t pyembed_create_module(JNIEnv *env,
                               intptr_t _jepThread,
                               char *str) {
    PyThreadState  *prevThread;
    PyObject       *module;
    JepThread      *jepThread;
    intptr_t        ret;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return 0;
    }

    if(str == NULL)
        return 0;

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    if(PyImport_AddModule(str) == NULL || process_py_exception(env, 1))
        goto EXIT;

    Py_InitModule(str, noop_methods);
    module = PyImport_ImportModuleEx(str,
                                     jepThread->globals,
                                     jepThread->globals,
                                     NULL);

    PyDict_SetItem(jepThread->globals,
                   PyString_FromString(str),
                   module);     /* steals */
    Py_DECREF(module); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!

    if(process_py_exception(env, 0) || module == NULL)
        ret = 0;
    else
        ret = (intptr_t) module;

EXIT:
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();

    return ret;
}


intptr_t pyembed_create_module_on(JNIEnv *env,
                                  intptr_t _jepThread,
                                  intptr_t _onModule,
                                  char *str) {
    PyThreadState  *prevThread;
    PyObject       *module, *onModule;
    JepThread      *jepThread;
    intptr_t        ret;
    PyObject       *globals;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return 0;
    }

    if(str == NULL)
        return 0;

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    onModule = (PyObject *) _onModule;
    if(!PyModule_Check(onModule)) {
        THROW_JEP(env, "Invalid onModule.");
        goto EXIT;
    }

    globals = PyModule_GetDict(onModule);
    Py_INCREF(globals);

    if(PyImport_AddModule(str) == NULL || process_py_exception(env, 1))
        goto EXIT;

    Py_InitModule(str, noop_methods);
    module = PyImport_ImportModuleEx(str, globals, globals, NULL);

    PyDict_SetItem(globals,
                   PyString_FromString(str),
                   module);     /* steals */
    Py_DECREF(module); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!

    if(process_py_exception(env, 0) || module == NULL)
        ret = 0;
    else
        ret = (intptr_t) module;

EXIT:
    if(globals)
        Py_DECREF(globals);

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();

    return ret;
}


void pyembed_setloader(JNIEnv *env, intptr_t _jepThread, jobject cl) {
    jobject    oldLoader = NULL;
    JepThread *jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return;
    }

    if(!cl)
        return;

    oldLoader = jepThread->classloader;
    if(oldLoader)
        (*env)->DeleteGlobalRef(env, oldLoader);

    jepThread->classloader = (*env)->NewGlobalRef(env, cl);
}


// convert pyobject to boxed java value
jobject pyembed_box_py(JNIEnv *env, PyObject *result) {

	// added (via proxy by brockwoo) by njensen
    if(result == Py_None)
        return NULL;

    // class and object need to return a new local ref so the object
    // isn't garbage collected.

    if(pyjclass_check(result))
        return (*env)->NewLocalRef(env, ((PyJobject_Object *) result)->clazz);

    if(pyjobject_check(result))
        return (*env)->NewLocalRef(env, ((PyJobject_Object *) result)->object);

    if(PyString_Check(result)) {
        char *s = PyString_AS_STRING(result);
        return (*env)->NewStringUTF(env, (const char *) s);
    }

    if(PyBool_Check(result)) {
        jclass clazz;
        jboolean b = JNI_FALSE;
        if(result == Py_True)
            b = JNI_TRUE;

        clazz = (*env)->FindClass(env, "java/lang/Boolean");

        if(booleanBConstructor == 0) {
            booleanBConstructor = (*env)->GetMethodID(env,
                                                      clazz,
                                                      "<init>",
                                                      "(Z)V");
        }

        if(!process_java_exception(env) && booleanBConstructor)
            return (*env)->NewObject(env, clazz, booleanBConstructor, b);
        else
            return NULL;
    }

    if(PyInt_Check(result)) {
        jclass clazz;
        jint i = PyInt_AS_LONG(result);

        clazz = (*env)->FindClass(env, "java/lang/Integer");

        if(integerIConstructor == 0) {
            integerIConstructor = (*env)->GetMethodID(env,
                                                      clazz,
                                                      "<init>",
                                                      "(I)V");
        }

        if(!process_java_exception(env) && integerIConstructor)
            return (*env)->NewObject(env, clazz, integerIConstructor, i);
        else
            return NULL;
    }

    if(PyLong_Check(result)) {
        jclass clazz;
        jeplong i = PyLong_AsLongLong(result);

        clazz = (*env)->FindClass(env, "java/lang/Long");

        if(longJConstructor == 0) {
            longJConstructor = (*env)->GetMethodID(env,
                                                   clazz,
                                                   "<init>",
                                                   "(J)V");
        }

        if(!process_java_exception(env) && longJConstructor)
            return (*env)->NewObject(env, clazz, longJConstructor, i);
        else
            return NULL;
    }

    if(PyFloat_Check(result)) {
        jclass clazz;

        // causes precision loss. python's float type sucks. *shrugs*
        jfloat f = PyFloat_AS_DOUBLE(result);

        clazz = (*env)->FindClass(env, "java/lang/Float");

        if(floatFConstructor == 0) {
            floatFConstructor = (*env)->GetMethodID(env,
                                                    clazz,
                                                    "<init>",
                                                    "(F)V");
        }

        if(!process_java_exception(env) && floatFConstructor)
            return (*env)->NewObject(env, clazz, floatFConstructor, f);
        else
            return NULL;
    }

    if(pyjarray_check(result)) {
        PyJarray_Object *t = (PyJarray_Object *) result;
        pyjarray_release_pinned(t, JNI_COMMIT);

        return t->object;
    }

    // added by brockwoo
    if(PyList_Check(result)) {
        return pylistToJStringList(env, result);
    }

    // added by njensen
    if(PyTuple_Check(result)) {
	// convert to a string for now
	jobject ret;
        char *tt;
        PyObject *t = PyObject_Str(result);
        tt = PyString_AsString(t);
        ret = (jobject) (*env)->NewStringUTF(env, (const char *) tt);
        Py_DECREF(t);

        return ret;
    }

    // added by njensen
    init();
    if(PyArray_Check(result)) {
        jarray arr = NULL;

        arr = numpyToJavaArray(env, result, NULL);

         if(arr != NULL)
        	 return arr;
    }

    // convert everything else to string
    {
        jobject ret;
        char *tt;
        PyObject *t = PyObject_Str(result);
        tt = PyString_AsString(t);
        ret = (jobject) (*env)->NewStringUTF(env, (const char *) tt);
        Py_DECREF(t);

        return ret;
    }
}


jobject pyembed_getvalue_on(JNIEnv *env,
                            intptr_t _jepThread,
                            intptr_t _onModule,
                            char *str) {
    PyThreadState  *prevThread;
    PyObject       *dict, *result, *onModule;
    jobject         ret = NULL;
    JepThread      *jepThread;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return NULL;
    }

    if(str == NULL)
        return NULL;

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    if(process_py_exception(env, 1))
        goto EXIT;

    onModule = (PyObject *) _onModule;
    if(!PyModule_Check(onModule)) {
        THROW_JEP(env, "pyembed_getvalue_on: Invalid onModule.");
        goto EXIT;
    }

    dict = PyModule_GetDict(onModule);
    Py_INCREF(dict);

    result = PyRun_String(str, Py_eval_input, dict, dict);      /* new ref */

    process_py_exception(env, 1);
    Py_DECREF(dict);

    if(result == NULL)
        goto EXIT;              /* don't return, need to release GIL */
    if(result == Py_None)
        goto EXIT;

    // convert results to jobject
    ret = pyembed_box_py(env, result);

EXIT:
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();

    if(result != NULL)
        Py_DECREF(result);
    return ret;
}


jobject pyembed_getvalue(JNIEnv *env, intptr_t _jepThread, char *str) {
    PyThreadState  *prevThread;
    PyObject       *main, *dict, *result;
    jobject         ret = NULL;
    JepThread      *jepThread;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return NULL;
    }

    if(str == NULL)
        return NULL;

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    if(process_py_exception(env, 1))
        goto EXIT;

    result = PyRun_String(str,  /* new ref */
                          Py_eval_input,
                          jepThread->globals,
                          jepThread->globals);

    process_py_exception(env, 1);

    if(result == NULL || result == Py_None)
        goto EXIT;              /* don't return, need to release GIL */

    // convert results to jobject
    ret = pyembed_box_py(env, result);

EXIT:
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();

    if(result != NULL)
        Py_DECREF(result);
    return ret;
}



jobject pyembed_getvalue_array(JNIEnv *env, intptr_t _jepThread, char *str, int typeId) {
    PyThreadState  *prevThread;
    PyObject       *main, *dict, *result;
    jobject         ret = NULL;
    JepThread      *jepThread;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return NULL;
    }

    if(str == NULL)
        return NULL;

    PyEval_AcquireLock();
    prevThread = PyThreadState_Swap(jepThread->tstate);

    if(process_py_exception(env, 1))
        goto EXIT;

    result = PyRun_String(str,  /* new ref */
                          Py_eval_input,
                          jepThread->globals,
                          jepThread->globals);

    process_py_exception(env, 1);

    if(result == NULL || result == Py_None)
        goto EXIT;              /* don't return, need to release GIL */

    if(PyString_Check(result)) {
        void *s = (void*) PyString_AS_STRING(result);
        int n = PyString_Size(result);

        switch (typeId) {
        case JFLOAT_ID:
            if(n % SIZEOF_FLOAT != 0) {
                THROW_JEP(env, "The Python string is the wrong length.\n");
                goto EXIT;
            }

            ret = (*env)->NewFloatArray(env, (jsize) n / SIZEOF_FLOAT);
            (*env)->SetFloatArrayRegion(env, ret, 0, (n / SIZEOF_FLOAT), (jfloat *) s);
            break;

        case JBYTE_ID:
            ret = (*env)->NewByteArray(env, (jsize) n);
            (*env)->SetByteArrayRegion(env, ret, 0, n, (jbyte *) s);
            break;

        default:
            THROW_JEP(env, "Internal error: array type not handled.");
            ret = NULL;
            goto EXIT;

        } // switch

    }
    else{
        THROW_JEP(env, "Value is not a string.");
        goto EXIT;
    }


EXIT:
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();

    if(result != NULL)
        Py_DECREF(result);
    return ret;
}






void pyembed_run(JNIEnv *env,
                 intptr_t _jepThread,
                 char *file) {
    PyThreadState	*prevThread;
    JepThread		*jepThread;
    const char		*ext;
	FILE			*script  = NULL;

    jepThread = (JepThread *) _jepThread;
    if(!jepThread) {
        THROW_JEP(env, "Couldn't get thread objects.");
        return;
    }

    PyEval_AcquireLock();

    prevThread = PyThreadState_Swap(jepThread->tstate);

    if(file != NULL) {
        script = fopen(file, "r");
        if(!script) {
            THROW_JEP(env, "Couldn't open script file.");
            goto EXIT;
        }

        // check if it's a pyc/pyo file
        ext = file + strlen(file) - 4;
        if (maybe_pyc_file(script, file, ext, 0)) {
            /* Try to run a pyc file. First, re-open in binary */
			fclose(script);
            if((script = fopen(file, "rb")) == NULL) {
                THROW_JEP(env, "pyembed_run: Can't reopen .pyc file");
                goto EXIT;
            }

            /* Turn on optimization if a .pyo file is given */
            if(strcmp(ext, ".pyo") == 0)
                Py_OptimizeFlag = 1;
            else
                Py_OptimizeFlag = 0;

            pyembed_run_pyc(jepThread, script);
        }
        else {
            // Added to fix Windows compile
            FILE * f = fopen(file, "r");
            if ( f==NULL ) {
                THROW_JEP(env, "pyembed_run: Can't PyFile_FromString");
                goto EXIT;
            }
            PyRun_AnyFileEx(f, file, 1);
        }

        // c programs inside some java environments may get buffered output
        fflush(stdout);
        fflush(stderr);

        fclose(script);
        process_py_exception(env, 1);
    }

EXIT:
    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
}


// gratuitously copyied from pythonrun.c::run_pyc_file
static void pyembed_run_pyc(JepThread *jepThread,
                            FILE *fp) {
	PyCodeObject    *co;
	PyObject        *v;
    PyObject        *globals;
	long             magic;

	long PyImport_GetMagicNumber(void);

	magic = PyMarshal_ReadLongFromFile(fp);
	if(magic != PyImport_GetMagicNumber()) {
		PyErr_SetString(PyExc_RuntimeError,
                        "Bad magic number in .pyc file");
		return;
	}
	(void) PyMarshal_ReadLongFromFile(fp);
	v = (PyObject *) (intptr_t) PyMarshal_ReadLastObjectFromFile(fp);
	if(v == NULL || !PyCode_Check(v)) {
		Py_XDECREF(v);
		PyErr_SetString(PyExc_RuntimeError,
                        "Bad code object in .pyc file");
		return;
	}
	co = (PyCodeObject *) v;
	v = PyEval_EvalCode(co, jepThread->globals, jepThread->globals);
	Py_DECREF(co);
    Py_XDECREF(v);
}


/* Check whether a file maybe a pyc file: Look at the extension,
   the file type, and, if we may close it, at the first few bytes. */
// gratuitously copyied from pythonrun.c::run_pyc_file
static int maybe_pyc_file(FILE *fp,
                          const char* filename,
                          const char* ext,
                          int closeit) {
	if(strcmp(ext, ".pyc") == 0 || strcmp(ext, ".pyo") == 0)
		return 1;

	/* Only look into the file if we are allowed to close it, since
	   it then should also be seekable. */
	if(closeit) {
		/* Read only two bytes of the magic. If the file was opened in
		   text mode, the bytes 3 and 4 of the magic (\r\n) might not
		   be read as they are on disk. */
		unsigned int halfmagic = PyImport_GetMagicNumber() & 0xFFFF;
		unsigned char buf[2];
		/* Mess:  In case of -x, the stream is NOT at its start now,
		   and ungetc() was used to push back the first newline,
		   which makes the current stream position formally undefined,
		   and a x-platform nightmare.
		   Unfortunately, we have no direct way to know whether -x
		   was specified.  So we use a terrible hack:  if the current
		   stream position is not 0, we assume -x was specified, and
		   give up.  Bug 132850 on SourceForge spells out the
		   hopelessness of trying anything else (fseek and ftell
		   don't work predictably x-platform for text-mode files).
		*/
		int ispyc = 0;
		if(ftell(fp) == 0) {
			if(fread(buf, 1, 2, fp) == 2 &&
               ((unsigned int)buf[1]<<8 | buf[0]) == halfmagic)
				ispyc = 1;
			rewind(fp);
		}

		return ispyc;
	}
	return 0;
}


// -------------------------------------------------- set() things

#define GET_COMMON                                                  \
    JepThread *jepThread;                                           \
                                                                    \
    jepThread = (JepThread *) _jepThread;                           \
    if(!jepThread) {                                                \
        THROW_JEP(env, "Couldn't get thread objects.");             \
        return;                                                     \
    }                                                               \
                                                                    \
    if(name == NULL) {                                              \
        THROW_JEP(env, "name is invalid.");                         \
        return;                                                     \
    }                                                               \
                                                                    \
    PyEval_AcquireLock();                                           \
    prevThread = PyThreadState_Swap(jepThread->tstate);             \
                                                                    \
    pymodule = NULL;                                                \
    if(module != 0)                                                 \
        pymodule = (PyObject *) module;



void pyembed_setparameter_object(JNIEnv *env,
                                 intptr_t _jepThread,
                                 intptr_t module,
                                 const char *name,
                                 jobject value) {
    PyObject      *pyjob;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if(value == NULL) {
        Py_INCREF(Py_None);
        pyjob = Py_None;
    }
    else
        pyjob = pyjobject_new(env, value);

    if(pyjob) {
        if(pymodule == NULL) {
        	PyObject      *pyname;  // added by njensen
        	pyname = PyString_FromString(name);
            PyDict_SetItem(jepThread->globals,
                           pyname,
                           pyjob); // steals reference
            Py_DECREF(pyjob); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
            Py_DECREF(pyname);
        }
        else {
            PyModule_AddObject(pymodule,
                               (char *) name,
                               pyjob); // steals reference
        }
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}

// added by njensen
void pyembed_setnumeric_array(JNIEnv *env,
                              intptr_t _jepThread,
                              intptr_t module,
                              const char *name,
                              jobjectArray obj,
                              int nx,
                              int ny) {

    PyObject      *pyjob;
    PyThreadState *prevThread;
    PyObject      *pymodule;

	jclass floatArrayClass;
	jclass intArrayClass;
	jclass byteArrayClass;

    GET_COMMON;

    floatArrayClass = (*env)->FindClass(env, "[F");
    intArrayClass = (*env)->FindClass(env, "[I");
    byteArrayClass = (*env)->FindClass(env, "[B");

    if(obj == NULL) {
        Py_INCREF(Py_None);
        pyjob = Py_None;
    }
    else {
      /* altered by bkowal - int* to npy_intp* */
	  npy_intp *dims = malloc(2 * sizeof(npy_intp));
	  dims[0] = ny;
	  dims[1] = nx;

	  init();
          if((*env)->IsInstanceOf(env, obj, floatArrayClass)) {
        	  pyjob = PyArray_SimpleNew(2, dims, NPY_FLOAT32);
	  }
          else if((*env)->IsInstanceOf(env, obj, intArrayClass)) {
        	    pyjob = PyArray_SimpleNew(2, dims, NPY_INT32);
          }
          else if((*env)->IsInstanceOf(env, obj, byteArrayClass)) {
        	    pyjob = PyArray_SimpleNew(2, dims, NPY_BYTE);
          }
	  free(dims);
    }

    if(pyjob) {
        size_t memorySize = 0;
        if((*env)->IsInstanceOf(env, obj, floatArrayClass)) {
                jfloat *data = (*env)->GetFloatArrayElements(env, obj, 0);
                memcpy(((PyArrayObject *)pyjob)->data,
                  data,
                  ny * nx * sizeof(float));
                (*env)->ReleaseFloatArrayElements(env, obj, data, 0);
        } else if((*env)->IsInstanceOf(env, obj, intArrayClass)) {
                jint *data = (*env)->GetIntArrayElements(env, obj, 0);
                memcpy(((PyArrayObject *)pyjob)->data,
                  data,
                  ny * nx * sizeof(int));
                (*env)->ReleaseIntArrayElements(env, obj, data, 0);
        } else if((*env)->IsInstanceOf(env, obj, byteArrayClass)) {
                jbyte *data = (*env)->GetByteArrayElements(env, obj, 0);
                memcpy(((PyArrayObject *)pyjob)->data,
                  data,
                  ny * nx);
                (*env)->ReleaseByteArrayElements(env, obj, data, 0);
        }
        if(pymodule == NULL) {
        	PyObject      *pyname;  // added by njensen
        	pyname = PyString_FromString(name);
            PyDict_SetItem(jepThread->globals,
                           pyname,
                           pyjob); // steals reference
            Py_DECREF(pyjob); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
            Py_DECREF(pyname);
        }
        else {
            PyModule_AddObject(pymodule,
                               (char *) name,
                               pyjob); // steals reference
        }
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}

// added by njensen
static void init(void)
{
    if (!ran)
    {
        import_array();
        ran = 1;
    }
}


void pyembed_setparameter_array(JNIEnv *env,
                                intptr_t _jepThread,
                                intptr_t module,
                                const char *name,
                                jobjectArray obj) {
    PyObject      *pyjob;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if(obj == NULL) {
        Py_INCREF(Py_None);
        pyjob = Py_None;
    }
    else
        pyjob = pyjarray_new(env, obj);

    if(pyjob) {
        if(pymodule == NULL) {
        	PyObject      *pyname;  // added by njensen
        	pyname = PyString_FromString(name);
            PyDict_SetItem(jepThread->globals,
                           pyname,
                           pyjob); // steals reference
            Py_DECREF(pyjob); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
            Py_DECREF(pyname);
        }
        else {
            PyModule_AddObject(pymodule,
                               (char *) name,
                               pyjob); // steals reference
        }
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}


void pyembed_setparameter_class(JNIEnv *env,
                                intptr_t _jepThread,
                                intptr_t module,
                                const char *name,
                                jclass value) {
    PyObject      *pyjob;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if(value == NULL) {
        Py_INCREF(Py_None);
        pyjob = Py_None;
    }
    else
        pyjob = pyjobject_new_class(env, value);

    if(pyjob) {
        if(pymodule == NULL) {
        	PyObject      *pyname;  // added by njensen
        	pyname = PyString_FromString(name);
            PyDict_SetItem(jepThread->globals,
                           pyname,
                           pyjob); // steals reference
            Py_DECREF(pyjob); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
            Py_DECREF(pyname);
        }
        else {
            PyModule_AddObject(pymodule,
                               (char *) name,
                               pyjob); // steals reference
        }
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}


void pyembed_setparameter_string(JNIEnv *env,
                                 intptr_t _jepThread,
                                 intptr_t module,
                                 const char *name,
                                 const char *value) {
    PyObject      *pyvalue;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if(value == NULL) {
        Py_INCREF(Py_None);
        pyvalue = Py_None;
    }
    else
        pyvalue = PyString_FromString(value);

    if(pymodule == NULL) {
    	PyObject      *pyname;  // added by njensen
    	pyname = PyString_FromString(name);
        PyDict_SetItem(jepThread->globals,
                       pyname,
                       pyvalue);  // steals reference
        Py_DECREF(pyvalue); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
        Py_DECREF(pyname);
    }
    else {
        PyModule_AddObject(pymodule,
                           (char *) name,
                           pyvalue); // steals reference
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}


void pyembed_setparameter_int(JNIEnv *env,
                              intptr_t _jepThread,
                              intptr_t module,
                              const char *name,
                              int value) {
    PyObject      *pyvalue;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if((pyvalue = Py_BuildValue("i", value)) == NULL) {
        PyErr_SetString(PyExc_MemoryError, "Out of memory.");
        return;
    }

    if(pymodule == NULL) {
    	PyObject      *pyname;  // added by njensen
    	pyname = PyString_FromString(name);
        PyDict_SetItem(jepThread->globals,
                       pyname,
                       pyvalue); // steals reference
        Py_DECREF(pyvalue); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
        Py_DECREF(pyname);
    }
    else {
        PyModule_AddObject(pymodule,
                           (char *) name,
                           pyvalue); // steals reference
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}


void pyembed_setparameter_long(JNIEnv *env,
                               intptr_t _jepThread,
                               intptr_t module,
                               const char *name,
                               jeplong value) {
    PyObject      *pyvalue;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if((pyvalue = PyLong_FromLongLong(value)) == NULL) {
        PyErr_SetString(PyExc_MemoryError, "Out of memory.");
        return;
    }

    if(pymodule == NULL) {
    	PyObject      *pyname;  // added by njensen
    	pyname = PyString_FromString(name);
        PyDict_SetItem(jepThread->globals,
                       pyname,
                       pyvalue); // steals reference
        Py_DECREF(pyvalue); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
        Py_DECREF(pyname);
    }
    else {
        PyModule_AddObject(pymodule,
                           (char *) name,
                           pyvalue); // steals reference
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}


void pyembed_setparameter_double(JNIEnv *env,
                                 intptr_t _jepThread,
                                 intptr_t module,
                                 const char *name,
                                 double value) {
    PyObject      *pyvalue;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if((pyvalue = PyFloat_FromDouble(value)) == NULL) {
        PyErr_SetString(PyExc_MemoryError, "Out of memory.");
        return;
    }

    if(pymodule == NULL) {
    	PyObject      *pyname;  // added by njensen
    	pyname = PyString_FromString(name);
        PyDict_SetItem(jepThread->globals,
                       pyname,
                       pyvalue); // steals reference
        Py_DECREF(pyvalue); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
        Py_DECREF(pyname);
    }
    else {
        PyModule_AddObject(pymodule,
                           (char *) name,
                           pyvalue); // steals reference
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}


void pyembed_setparameter_float(JNIEnv *env,
                                intptr_t _jepThread,
                                intptr_t module,
                                const char *name,
                                float value) {
    PyObject      *pyvalue;
    PyThreadState *prevThread;
    PyObject      *pymodule;

    // does common things
    GET_COMMON;

    if((pyvalue = PyFloat_FromDouble((double) value)) == NULL) {
        PyErr_SetString(PyExc_MemoryError, "Out of memory.");
        return;
    }

    if(pymodule == NULL) {
    	PyObject      *pyname;  // added by njensen
    	pyname = PyString_FromString(name);
        PyDict_SetItem(jepThread->globals,
                       pyname,
                       pyvalue); // steals reference
        Py_DECREF(pyvalue); // njensen: ABOVE LINE DOES NOT STEAL REFERENCE!
        Py_DECREF(pyname);
    }
    else {
        PyModule_AddObject(pymodule,
                           (char *) name,
                           pyvalue); // steals reference
    }

    PyThreadState_Swap(prevThread);
    PyEval_ReleaseLock();
    return;
}
