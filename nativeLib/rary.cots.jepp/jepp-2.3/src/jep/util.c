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

// shut up the compiler
#ifdef _POSIX_C_SOURCE
#  undef _POSIX_C_SOURCE
#endif
#include <jni.h>

// shut up the compiler
#ifdef _POSIX_C_SOURCE
#  undef _POSIX_C_SOURCE
#endif
#ifdef _FILE_OFFSET_BITS
# undef _FILE_OFFSET_BITS
#endif
#include "Python.h"

#include "util.h"
#include "pyjobject.h"
#include "pyjarray.h"
#include "pyjmethod.h"
#include "pyjclass.h"
#include "pyembed.h"

// added by njensen
#include "numpy/arrayobject.h"
#define TRACEBACK_FETCH_ERROR(what) {errMsg = what; goto done;}

// -------------------------------------------------- primitive class types
// these are shared for all threads, you shouldn't change them.

jclass JINT_TYPE     = NULL;
jclass JLONG_TYPE    = NULL;
jclass JOBJECT_TYPE  = NULL;
jclass JSTRING_TYPE  = NULL;
jclass JBOOLEAN_TYPE = NULL;
jclass JVOID_TYPE    = NULL;
jclass JDOUBLE_TYPE  = NULL;
jclass JSHORT_TYPE   = NULL;
jclass JFLOAT_TYPE   = NULL;
jclass JCHAR_TYPE    = NULL;
jclass JBYTE_TYPE    = NULL;
jclass JCLASS_TYPE   = NULL;

// added by njensen
jclass JINT_ARRAY_TYPE = NULL;
jclass JLONG_ARRAY_TYPE = NULL;
jclass JBOOLEAN_ARRAY_TYPE = NULL;
jclass JDOUBLE_ARRAY_TYPE = NULL;
jclass JFLOAT_ARRAY_TYPE = NULL;
jclass JBYTE_ARRAY_TYPE = NULL;
jclass JSHORT_ARRAY_TYPE = NULL;

// cached methodids
jmethodID objectToString     = 0;
jmethodID objectEquals       = 0;
jmethodID objectIsArray      = 0;

// added by njensen
jmethodID throwableAsString = 0;
jclass utilclass = 0;

// for convert_jobject
jmethodID getBooleanValue    = 0;
jmethodID getIntValue        = 0;
jmethodID getLongValue       = 0;
jmethodID getDoubleValue     = 0;
jmethodID getFloatValue      = 0;
jmethodID getCharValue       = 0;

// call toString() on jobject, make a python string and return
// sets error conditions as needed.
// returns new reference to PyObject
PyObject* jobject_topystring(JNIEnv *env, jobject obj, jclass clazz) {
    const char *result;
    PyObject   *pyres;
    jstring     jstr;

    jstr = jobject_tostring(env, obj, clazz);
    // it's possible, i guess. don't throw an error....
    if(process_java_exception(env) || jstr == NULL)
        return PyString_FromString("");

    result = (*env)->GetStringUTFChars(env, jstr, 0);
    pyres  = PyString_FromString(result);
    (*env)->ReleaseStringUTFChars(env, jstr, result);
    (*env)->DeleteLocalRef(env, jstr);

    // method returns new reference.
    return pyres;
}


PyObject* pystring_split_item(PyObject *str, char *split, int pos) {
    PyObject *splitList, *ret;
    int       len;

    if(pos < 0) {
        PyErr_SetString(PyExc_RuntimeError,
                        "Invalid position to return.");
        return NULL;
    }

    splitList = PyObject_CallMethod(str, "split", "s", split);
    if(PyErr_Occurred() || !splitList)
        return NULL;

    if(!PyList_Check(splitList)) {
        PyErr_SetString(PyExc_RuntimeError,
                        "Oops, split string return is not a list.");
        return NULL;
    }

    len = PyList_Size(splitList);
    if(pos > len - 1) {
        PyErr_SetString(PyExc_RuntimeError,
                        "Not enough items to return split position.");
        return NULL;
    }

    // get requested item
    ret = PyList_GetItem(splitList, pos);
    if(PyErr_Occurred())
        return NULL;
    if(!PyString_Check(ret)) {
        PyErr_SetString(PyExc_RuntimeError,
                        "Oops, item is not a string.");
        return NULL;
    }

    Py_INCREF(ret);
    Py_DECREF(splitList);
    return ret;
}


PyObject* pystring_split_last(PyObject *str, char *split) {
    PyObject *splitList, *ret;
    int       len;

    splitList = PyObject_CallMethod(str, "split", "s", split);
    if(PyErr_Occurred() || !splitList)
        return NULL;

    if(!PyList_Check(splitList)) {
        PyErr_SetString(PyExc_RuntimeError,
                        "Oops, split string return is not a list.");
        return NULL;
    }

    len = PyList_Size(splitList);

    // get the last one
    ret = PyList_GetItem(splitList, len - 1);
    if(PyErr_Occurred())
        return NULL;
    if(!PyString_Check(ret)) {
        PyErr_SetString(PyExc_RuntimeError,
                        "Oops, item is not a string.");
        return NULL;
    }

    Py_INCREF(ret);
    Py_DECREF(splitList);
    return ret;
}


// convert python exception to java.
int process_py_exception(JNIEnv *env, int printTrace) {
    JepThread  *jepThread;
    PyObject *f, *ptype, *pvalue, *ptrace, *message = NULL;
    char *m;
	char* tr;

    if(!PyErr_Occurred())
        return 0;

    // we only care about ptype and pvalue.
    // many people consider it a security vulnerability
    // to have the source code printed to the user's
    // screen. (i do.)
    //
    // so we pull relevant info and print strace to the
    // console.

    PyErr_Fetch(&ptype, &pvalue, &ptrace);

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        printf("Error while processing a Python exception, "
               "invalid JepThread.\n");
        if(jepThread->printStack) {
            PyErr_Print();
            if(!PyErr_Occurred())
                return 0;
        }
    }

    if(ptype) {
        message = PyObject_Str(ptype);

        if(pvalue) {
            PyObject *v;
            m = PyString_AsString(message);

            v = PyObject_Str(pvalue);
            if(PyString_Check(v)) {
                PyObject *t;
                t = PyString_FromFormat("%s: %s",
                                        m,
                                        PyString_AsString(v));

                Py_DECREF(v);
                Py_DECREF(message);
                message = t;

		// added by njensen
		if(ptrace)
		{
			m = PyString_AsString(message);
			tr = PyTraceback_AsString(ptrace);
			f = PyString_FromFormat("%s >>> %s", m, tr);
			PyMem_Free((void *) tr);
			Py_DECREF(message);
			message = f;
		}
            }
        }
    }

    if(ptype)
        Py_DECREF(ptype);
    if(pvalue)
        Py_DECREF(pvalue);
    if(ptrace)
        Py_DECREF(ptrace);

    if(message && PyString_Check(message)) {
        m = PyString_AsString(message);
        THROW_JEP(env, m);
        Py_DECREF(message);
    }

    return 1;
}


// added by njensen, taken from python.org
char *PyTraceback_AsString(PyObject *exc_tb)
{
	char *errMsg = NULL; /* holds a local error message */
	char *result = NULL; /* a valid, allocated result. */
	PyObject *modStringIO = NULL;
	PyObject *modTB = NULL;
	PyObject *obFuncStringIO = NULL;
	PyObject *obStringIO = NULL;
	PyObject *obFuncTB = NULL;
	PyObject *argsTB = NULL;
	PyObject *obResult = NULL;

	/* Import the modules we need - cStringIO and traceback */
	modStringIO = PyImport_ImportModule("cStringIO");
	if (modStringIO==NULL)
		TRACEBACK_FETCH_ERROR("cant import cStringIO\n");

	modTB = PyImport_ImportModule("traceback");
	if (modTB==NULL)
		TRACEBACK_FETCH_ERROR("cant import traceback\n");
	/* Construct a cStringIO object */
	obFuncStringIO = PyObject_GetAttrString(modStringIO, "StringIO");
	if (obFuncStringIO==NULL)
		TRACEBACK_FETCH_ERROR("cant find cStringIO.StringIO\n");
	obStringIO = PyObject_CallObject(obFuncStringIO, NULL);
	if (obStringIO==NULL)
		TRACEBACK_FETCH_ERROR("cStringIO.StringIO() failed\n");
	/* Get the traceback.print_exception function, and call it. */
	obFuncTB = PyObject_GetAttrString(modTB, "print_tb");
	if (obFuncTB==NULL)
		TRACEBACK_FETCH_ERROR("cant find traceback.print_tb\n");

	argsTB = Py_BuildValue("OOO",
			exc_tb  ? exc_tb  : Py_None,
			Py_None,
			obStringIO);
	if (argsTB==NULL)
		TRACEBACK_FETCH_ERROR("cant make print_tb arguments\n");

	obResult = PyObject_CallObject(obFuncTB, argsTB);
	if (obResult==NULL)
		TRACEBACK_FETCH_ERROR("traceback.print_tb() failed\n");
	/* Now call the getvalue() method in the StringIO instance */
	Py_DECREF(obFuncStringIO);
	obFuncStringIO = PyObject_GetAttrString(obStringIO, "getvalue");
	if (obFuncStringIO==NULL)
		TRACEBACK_FETCH_ERROR("cant find getvalue function\n");
	Py_DECREF(obResult);
	obResult = PyObject_CallObject(obFuncStringIO, NULL);
	if (obResult==NULL)
		TRACEBACK_FETCH_ERROR("getvalue() failed.\n");

	/* And it should be a string all ready to go - duplicate it. */
	if (!PyString_Check(obResult))
			TRACEBACK_FETCH_ERROR("getvalue() did not return a string\n");

	{ // a temp scope so I can use temp locals.
	char *tempResult = PyString_AsString(obResult);
	result = (char *)PyMem_Malloc(strlen(tempResult)+1);
	if (result==NULL)
		TRACEBACK_FETCH_ERROR("memory error duplicating the traceback string\n");

	strcpy(result, tempResult);
	} // end of temp scope.
done:
	/* All finished - first see if we encountered an error */
	if (result==NULL && errMsg != NULL) {
		result = (char *)PyMem_Malloc(strlen(errMsg)+1);
		if (result != NULL)
			/* if it does, not much we can do! */
			strcpy(result, errMsg);
	}
	Py_XDECREF(modStringIO);
	Py_XDECREF(modTB);
	Py_XDECREF(obFuncStringIO);
	Py_XDECREF(obStringIO);
	Py_XDECREF(obFuncTB);
	Py_XDECREF(argsTB);
	Py_XDECREF(obResult);
	return result;
}

// convert java exception to ImportError.
// true (1) if an exception was processed.
int process_import_exception(JNIEnv *env) {
    jstring     estr;
    jthrowable  exception    = NULL;
    jclass      clazz;
    PyObject   *pyException  = PyExc_ImportError;
    PyObject   *str, *tmp, *texc, *className;
    char       *message;
    JepThread  *jepThread;

    if(!(*env)->ExceptionCheck(env))
        return 0;

    if((exception = (*env)->ExceptionOccurred(env)) == NULL)
        return 0;

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        printf("Error while processing a Java exception, "
               "invalid JepThread.\n");
        return 1;
    }

    if(jepThread->printStack)
        (*env)->ExceptionDescribe(env);

    // we're already processing this one, clear the old
    (*env)->ExceptionClear(env);

    clazz = (*env)->GetObjectClass(env, exception);
    if((*env)->ExceptionCheck(env) || !clazz) {
        (*env)->ExceptionDescribe(env);
        return 1;
    }

    estr = jobject_tostring(env, exception, clazz);
    if((*env)->ExceptionCheck(env) || !estr) {
        PyErr_Format(PyExc_RuntimeError, "toString() on exception failed.");
        return 1;
    }

    message = (char *) jstring2char(env, estr);
    PyErr_Format(pyException, message);
    release_utf_char(env, estr, message);

    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, exception);
    return 1;
}


// convert java exception to pyerr.
// true (1) if an exception was processed.
int process_java_exception(JNIEnv *env) {
    jstring     estr;
    jthrowable  exception    = NULL;
    jclass      clazz;
    PyObject   *pyException  = PyExc_RuntimeError;
    PyObject   *str, *tmp, *texc, *className;
    char       *message;
    JepThread  *jepThread;

    if(!(*env)->ExceptionCheck(env))
        return 0;

    if((exception = (*env)->ExceptionOccurred(env)) == NULL)
        return 0;

    jepThread = pyembed_get_jepthread();
    if(!jepThread) {
        printf("Error while processing a Java exception, "
               "invalid JepThread.\n");
        return 1;
    }

    if(jepThread->printStack)
        (*env)->ExceptionDescribe(env);

    // we're already processing this one, clear the old
    (*env)->ExceptionClear(env);

    clazz = (*env)->GetObjectClass(env, exception);
    if((*env)->ExceptionCheck(env) || !clazz) {
        (*env)->ExceptionDescribe(env);
        return 1;
    }

    // changed by njensen to get whole stacktrace back
    //estr = jobject_tostring(env, exception, clazz);
    estr = javaStacktrace_tostring(env, exception);
    if((*env)->ExceptionCheck(env) || !estr) {
        PyErr_Format(PyExc_RuntimeError, "toString() on exception failed.");
        return 1;
    }

    message = (char *) jstring2char(env, estr);

#if USE_MAPPED_EXCEPTIONS

    // need to find the class name of the exception.
    // we already did toString(), so we'll just hackishly
    // find the name off that.
    //
    // format is:
    // java.lang.NumberFormatException: For input string: "asdf"
    //
    // if we don't find the name, just throw a RuntimeError

    str = PyString_FromString(message);

    if((tmp = pystring_split_last(str, ".")) == NULL || PyErr_Occurred()) {
        Py_DECREF(str);
        return 1;
    }

    // may not have a message
    if((className = pystring_split_item(tmp, ":", 0)) == NULL ||
       PyErr_Occurred()) {

        Py_DECREF(tmp);
        Py_DECREF(str);
        return 1;
    }

    if((texc = PyObject_GetAttr(jepThread->modjep, className)) != NULL)
        pyException = texc;

    Py_DECREF(str);
    Py_DECREF(tmp);
    Py_DECREF(className);

#endif // #if USE_MAPPED_EXCEPTIONS


    //PyErr_Format(pyException, message);
    // njensen changed this from PyErr_Format to PyErr_SetString
    PyErr_SetString(pyException, message);
    release_utf_char(env, estr, message);

    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, exception);
    return 1;
}

// added by njensen
jstring javaStacktrace_tostring(JNIEnv *env, jthrowable exception)
{
	jstring jstr = NULL;

	if(utilclass == 0)
	{
		utilclass = (*env)->FindClass(env, "jep/Util");
	}
	if(throwableAsString == 0)
	{
		throwableAsString = (*env)->GetStaticMethodID(env,
                                             utilclass,
                                             "throwableAsString",
                                             "(Ljava/lang/Throwable;)Ljava/lang/String;");
    }

    jstr = (jstring) (*env)->CallStaticObjectMethod(env, utilclass, throwableAsString, exception);
	return jstr;
}


// call toString() on jobject and return result.
// NULL on error
jstring jobject_tostring(JNIEnv *env, jobject obj, jclass clazz) {
    jstring     jstr;

    if(!env || !obj || !clazz)
        return NULL;

    if(objectToString == 0) {
        objectToString = (*env)->GetMethodID(env,
                                             clazz,
                                             "toString",
                                             "()Ljava/lang/String;");
        if(process_java_exception(env))
            return NULL;
    }

    if(!objectToString) {
        PyErr_Format(PyExc_RuntimeError, "Couldn't get methodId.");
        return NULL;
    }

    jstr = (jstring) (*env)->CallObjectMethod(env, obj, objectToString);
    if(process_java_exception(env))
        return NULL;

    return jstr;
}


// get a const char* string from java string.
// you *must* call release when you're finished with it.
// returns local reference.
const char* jstring2char(JNIEnv *env, jstring str) {
    if(str == NULL)
        return NULL;
    return (*env)->GetStringUTFChars(env, str, 0);
}


// release memory allocated by jstring2char
void release_utf_char(JNIEnv *env, jstring str, const char *v) {
    if(v != NULL && str != NULL) {
        (*env)->ReleaseStringUTFChars(env, str, v);
        (*env)->DeleteLocalRef(env, str);
    }
}


// in order to call methods that return primitives,
// we have to know they're return type. that's easy,
// i'm simply using the reflection api to call getReturnType().
//
// however, jni requires us to use Call<type>Method.
// so, here we fetch the primitive Class objects
// (i.e. Integer.TYPE)
//
// returns 1 if successful, 0 if failed.
// doesn't process java exceptions.
int cache_primitive_classes(JNIEnv *env) {
    jclass   clazz, tmpclazz = NULL;
    jfieldID fieldId;
    jobject  tmpobj          = NULL;

    // ------------------------------ get Integer.TYPE

    if(JINT_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Integer");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JINT_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JSHORT_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Short");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JSHORT_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JDOUBLE_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Double");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JDOUBLE_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JFLOAT_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Float");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JFLOAT_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JLONG_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Long");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JLONG_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JBOOLEAN_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Boolean");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JBOOLEAN_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JVOID_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Void");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JVOID_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JBYTE_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Byte");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JBYTE_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JCHAR_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Character");
        if((*env)->ExceptionOccurred(env))
            return 0;

        fieldId = (*env)->GetStaticFieldID(env,
                                           clazz,
                                           "TYPE",
                                           "Ljava/lang/Class;");
        if((*env)->ExceptionOccurred(env))
            return 0;

        tmpclazz = (jclass) (*env)->GetStaticObjectField(env,
                                                         clazz,
                                                         fieldId);
        if((*env)->ExceptionOccurred(env))
            return 0;

        JCHAR_TYPE = (*env)->NewGlobalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpclazz);
        (*env)->DeleteLocalRef(env, tmpobj);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JOBJECT_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Object");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JOBJECT_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JSTRING_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/String");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JSTRING_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JCLASS_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "java/lang/Class");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JCLASS_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    // added by njensen
    if(JINT_ARRAY_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "[I");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JINT_ARRAY_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JLONG_ARRAY_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "[J");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JLONG_ARRAY_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JBYTE_ARRAY_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "[B");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JBYTE_ARRAY_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JBOOLEAN_ARRAY_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "[Z");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JBOOLEAN_ARRAY_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JFLOAT_ARRAY_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "[F");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JFLOAT_ARRAY_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JDOUBLE_ARRAY_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "[D");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JDOUBLE_ARRAY_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    if(JSHORT_ARRAY_TYPE == NULL) {
        clazz = (*env)->FindClass(env, "[S");
        if((*env)->ExceptionOccurred(env))
            return 0;

        JSHORT_ARRAY_TYPE = (*env)->NewGlobalRef(env, clazz);
        (*env)->DeleteLocalRef(env, clazz);
    }

    return 1;
}


// remove global references setup in above function.
void unref_cache_primitive_classes(JNIEnv *env) {
    if(JINT_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JINT_TYPE);
        JINT_TYPE = NULL;
    }
    if(JSHORT_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JSHORT_TYPE);
        JSHORT_TYPE = NULL;
    }
    if(JDOUBLE_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JDOUBLE_TYPE);
        JDOUBLE_TYPE = NULL;
    }
    if(JFLOAT_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JFLOAT_TYPE);
        JFLOAT_TYPE = NULL;
    }
    if(JLONG_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JLONG_TYPE);
        JLONG_TYPE = NULL;
    }
    if(JBOOLEAN_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JBOOLEAN_TYPE);
        JBOOLEAN_TYPE = NULL;
    }
    if(JOBJECT_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JOBJECT_TYPE);
        JOBJECT_TYPE = NULL;
    }
    if(JSTRING_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JSTRING_TYPE);
        JSTRING_TYPE = NULL;
    }
    if(JVOID_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JVOID_TYPE);
        JVOID_TYPE = NULL;
    }
    if(JCHAR_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JCHAR_TYPE);
        JCHAR_TYPE = NULL;
    }
    if(JBYTE_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JBYTE_TYPE);
        JBYTE_TYPE = NULL;
    }
    if(JCLASS_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JCLASS_TYPE);
        JCLASS_TYPE = NULL;
    }

    // added by njensen
    if(JBOOLEAN_ARRAY_TYPE != NULL) {
        (*env)->DeleteGlobalRef(env, JBOOLEAN_ARRAY_TYPE);
        JBOOLEAN_ARRAY_TYPE = NULL;
    }
    if(JBYTE_ARRAY_TYPE != NULL) {
            (*env)->DeleteGlobalRef(env, JBYTE_ARRAY_TYPE);
            JBYTE_ARRAY_TYPE = NULL;
        }
    if(JINT_ARRAY_TYPE != NULL) {
            (*env)->DeleteGlobalRef(env, JINT_ARRAY_TYPE);
            JINT_ARRAY_TYPE = NULL;
        }
    if(JLONG_ARRAY_TYPE != NULL) {
            (*env)->DeleteGlobalRef(env, JLONG_ARRAY_TYPE);
            JLONG_ARRAY_TYPE = NULL;
        }
    if(JFLOAT_ARRAY_TYPE != NULL) {
            (*env)->DeleteGlobalRef(env, JFLOAT_ARRAY_TYPE);
            JFLOAT_ARRAY_TYPE = NULL;
        }
    if(JDOUBLE_ARRAY_TYPE != NULL) {
            (*env)->DeleteGlobalRef(env, JDOUBLE_ARRAY_TYPE);
            JDOUBLE_ARRAY_TYPE = NULL;
        }
    if(JSHORT_ARRAY_TYPE != NULL) {
            (*env)->DeleteGlobalRef(env, JSHORT_ARRAY_TYPE);
            JSHORT_ARRAY_TYPE = NULL;
        }

}


// given the Class object, return the const ID.
// -1 on error or NULL.
// doesn't process errors!
int get_jtype(JNIEnv *env, jobject obj, jclass clazz) {
    jboolean equals = JNI_FALSE;
    jboolean array  = JNI_FALSE;

    getjtypeCount = getjtypeCount + 1;

    // have to find the equals() method.
    if(objectEquals == 0 || objectIsArray == 0) {
        jobject super = NULL;

        super = (*env)->GetSuperclass(env, clazz);
        if((*env)->ExceptionCheck(env) || !super) {
            (*env)->DeleteLocalRef(env, super);
            return -1;
        }

        objectEquals = (*env)->GetMethodID(env,
                                           super,
                                           "equals",
                                           "(Ljava/lang/Object;)Z");
        (*env)->DeleteLocalRef(env, super);
        if((*env)->ExceptionCheck(env) || !objectEquals)
            return -1;

        objectIsArray = (*env)->GetMethodID(env,
                                            clazz,
                                            "isArray",
                                            "()Z");
        if((*env)->ExceptionCheck(env) || !objectIsArray)
            return -1;
    }

    // int
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JINT_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JINT_ID;

    // short
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JSHORT_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JSHORT_ID;

    // double
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JDOUBLE_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JDOUBLE_ID;

    // float
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JFLOAT_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JFLOAT_ID;

    // boolean
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JBOOLEAN_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JBOOLEAN_ID;

    // long
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JLONG_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JLONG_ID;

    // string
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JSTRING_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JSTRING_ID;

    // void
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JVOID_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JVOID_ID;

    // char
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JCHAR_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JCHAR_ID;

    // byte
    equals = (*env)->CallBooleanMethod(env, obj, objectEquals, JBYTE_TYPE);
    if((*env)->ExceptionCheck(env))
        return -1;
    if(equals)
        return JBYTE_ID;

    // object checks

    // check if it's an array first
    array = (*env)->CallBooleanMethod(env, obj, objectIsArray);
    if((*env)->ExceptionCheck(env))
        return -1;

    if(array)
        return JARRAY_ID;

    if((*env)->IsAssignableFrom(env, obj, JCLASS_TYPE))
        return JCLASS_ID;

    if((*env)->IsAssignableFrom(env, clazz, JOBJECT_TYPE))
        return JOBJECT_ID;

    return -1;
}


// returns if the type of python object matches jclass
int pyarg_matches_jtype(JNIEnv *env,
                        PyObject *param,
                        jclass paramType,
                        int paramTypeId) {

    switch(paramTypeId) {

    case JCHAR_ID:
        // must not be null...
        if(PyString_Check(param) && PyString_GET_SIZE(param) == 1)
            return 1;
        return 0;

    case JSTRING_ID:

        if(param == Py_None)
            return 1;

        if(PyString_Check(param))
            return 1;

        if(pyjobject_check(param)) {
            // check if the object itself can cast to parameter type.
            if((*env)->IsAssignableFrom(env,
                                        ((PyJobject_Object *) param)->clazz,
                                        paramType))
                return 1;
        }

        break;

    case JARRAY_ID:
        if(param == Py_None)
            return 1;

        if(pyjarray_check(param)) {
            // check if the object itself can cast to parameter type.
            if((*env)->IsAssignableFrom(env,
                                        ((PyJarray_Object *) param)->clazz,
                                        paramType))
                return 1;
        }

        break;

    case JCLASS_ID:
        if(param == Py_None)
            return 1;

        if(pyjclass_check(param))
            return 1;

        break;

    case JOBJECT_ID:
        if(param == Py_None)
            return 1;

        if(pyjobject_check(param)) {
            // check if the object itself can cast to parameter type.
            if((*env)->IsAssignableFrom(env,
                                        ((PyJobject_Object *) param)->clazz,
                                        paramType))
                return 1;
        }

        if(PyString_Check(param)) {
            if((*env)->IsAssignableFrom(env,
                                        JSTRING_TYPE,
                                        paramType))
                return 1;
        }

        break;

    case JBYTE_ID:
    case JSHORT_ID:
    case JINT_ID:
        if(PyInt_Check(param))
            return 1;
        break;

    case JFLOAT_ID:
    case JDOUBLE_ID:
        if(PyFloat_Check(param))
            return 1;
        break;

    case JLONG_ID:
        if(PyLong_Check(param))
            return 1;
        if(PyInt_Check(param))
            return 1;
        break;

    case JBOOLEAN_ID:
        if(PyInt_Check(param))
            return 1;
        break;
    }

    // no match
    return 0;
}


// convert java object to python. use this to unbox jobject
// throws java exception on error
PyObject* convert_jobject(JNIEnv *env, jobject val, int typeid) {
    PyThreadState *_save;

    if(getIntValue == 0) {
        jclass clazz;

        // get all the methodIDs here. Faster this way for Number
        // subclasses, then we'll just call the right methods below
        Py_UNBLOCK_THREADS;
        clazz = (*env)->FindClass(env, "java/lang/Number");

        getIntValue = (*env)->GetMethodID(env,
                                          clazz,
                                          "intValue",
                                          "()I");
        getLongValue = (*env)->GetMethodID(env,
                                           clazz,
                                           "longValue",
                                           "()J");
        getDoubleValue = (*env)->GetMethodID(env,
                                             clazz,
                                             "doubleValue",
                                             "()D");
        getFloatValue = (*env)->GetMethodID(env,
                                            clazz,
                                            "floatValue",
                                            "()F");

        (*env)->DeleteLocalRef(env, clazz);
        Py_BLOCK_THREADS;

        if((*env)->ExceptionOccurred(env))
            return NULL;
    }

    switch(typeid) {
    case -1:
        // null
        Py_INCREF(Py_None);
        return Py_None;

    case JARRAY_ID:
        return (PyObject *) pyjarray_new(env, val);

    case JSTRING_ID: {
        const char *str;
        PyObject *ret;

        str = jstring2char(env, val);
        ret = PyString_FromString(str);
        release_utf_char(env, val, str);

        return ret;
    }

    case JCLASS_ID:
        return (PyObject *) pyjobject_new_class(env, val);

    case JVOID_ID:
        // pass through
        // wrap as a object... try to be diligent.

    case JOBJECT_ID:
        return (PyObject *) pyjobject_new(env, val);

    case JBOOLEAN_ID: {
        jboolean b;

        if(getBooleanValue == 0) {
            jclass clazz;

            Py_UNBLOCK_THREADS;
            clazz = (*env)->FindClass(env, "java/lang/Boolean");

            getBooleanValue = (*env)->GetMethodID(env,
                                                  clazz,
                                                  "booleanValue",
                                                  "()Z");

            Py_BLOCK_THREADS;
            if((*env)->ExceptionOccurred(env))
                return NULL;
        }

        b = (*env)->CallBooleanMethod(env, val, getBooleanValue);
        if((*env)->ExceptionOccurred(env))
            return NULL;

        if(b)
            return Py_BuildValue("i", 1);
        return Py_BuildValue("i", 0);
    }

    case JBYTE_ID:              /* pass through */
    case JSHORT_ID:             /* pass through */
    case JINT_ID: {
        jint b = (*env)->CallIntMethod(env, val, getIntValue);
        if((*env)->ExceptionOccurred(env))
            return NULL;

        return Py_BuildValue("i", b);
    }

    case JLONG_ID: {
        jlong b = (*env)->CallLongMethod(env, val, getLongValue);
        if((*env)->ExceptionOccurred(env))
            return NULL;

        return Py_BuildValue("i", b);
    }

    case JDOUBLE_ID: {
        jdouble b = (*env)->CallDoubleMethod(env, val, getDoubleValue);
        if((*env)->ExceptionOccurred(env))
            return NULL;

        return PyFloat_FromDouble(b);
    }

    case JFLOAT_ID: {
        jfloat b = (*env)->CallFloatMethod(env, val, getFloatValue);
        if((*env)->ExceptionOccurred(env))
            return NULL;

        return PyFloat_FromDouble(b);
    }

    case JCHAR_ID: {
        jchar c;

        if(getCharValue == 0) {
            jclass clazz;

            Py_UNBLOCK_THREADS;
            clazz = (*env)->FindClass(env, "java/lang/Character");

            getCharValue = (*env)->GetMethodID(env,
                                               clazz,
                                               "charValue",
                                               "()C");
            (*env)->DeleteLocalRef(env, clazz);
            Py_BLOCK_THREADS;

            if((*env)->ExceptionOccurred(env))
                return NULL;
        }

        c = (*env)->CallCharMethod(env, val, getCharValue);
        if((*env)->ExceptionOccurred(env))
            return NULL;

        return PyString_FromFormat("%c", (char) c);
    }

    default:
        break;
    }

    THROW_JEP(env, "util.c:convert_jobject invalid typeid.");
    return NULL;
}


// added by njensen, this converts a numpy array to a java primitive array
// Returns    null if the python object is a pyjarray
//            null and error indicator if the object can't be transformed to a java primitive array
//            a java primitive array corresponding to the numpy array
jvalue convert_pynumpyarg_jvalue(JNIEnv *env,
                            PyObject *param,
                            jclass paramType,
                            int paramTypeId,
                            int pos) {
    jvalue ret;
    jarray arr;
    jclass arrclazz;

    ret.l = NULL;
    arr = NULL;

	if(param != Py_None && !pyjarray_check(param)) {
		initNumpy();

		if(PyArray_Check(param))
		{
			arr = numpyToJavaArray(env, param, paramType);
			if(arr == NULL)
			{
				PyErr_Format(PyExc_TypeError,
								   "No JEP numpy support for type at parameter %i.",
									   pos + 1);
				return ret;
			}

			arrclazz = (*env)->GetObjectClass(env, arr);

			if(!(*env)->IsAssignableFrom(env,
								 arrclazz,
								 paramType)) {
					PyErr_Format(PyExc_TypeError,
						 "Incompatible numpy array type at parameter %i.",
						pos + 1);
					return ret;
			}

			ret.l = arr;
			return ret;
		}
		else
		{
			PyErr_Format(PyExc_TypeError,
			                             "Expected jarray parameter at %i.",
			                             pos + 1);
			return ret;
		}
	}

	return ret;
}


// for parsing args.
// takes a python object and sets the right jvalue member for the given java type.
// returns uninitialized on error and raises a python exception.
jvalue convert_pyarg_jvalue(JNIEnv *env,
                            PyObject *param,
                            jclass paramType,
                            int paramTypeId,
                            int pos) {
    jvalue ret;
    ret.l = NULL;

    switch(paramTypeId) {

    case JCHAR_ID: {
        char *val;

        if(param == Py_None ||
           !PyString_Check(param) ||
           PyString_GET_SIZE(param) != 1) {

            PyErr_Format(PyExc_TypeError,
                         "Expected char parameter at %i",
                         pos + 1);
            return ret;
        }

        val = PyString_AsString(param);
        ret.c = (jchar) val[0];
        return ret;
    }

    case JSTRING_ID: {
        jstring   jstr;
        char     *val;

        // none is okay, we'll set a null
        if(param == Py_None)
            ret.l = NULL;
        else {
            // we could just convert it to a string...
            if(!PyString_Check(param)) {
                PyErr_Format(PyExc_TypeError,
                             "Expected string parameter at %i.",
                             pos + 1);
                return ret;
            }

            val   = PyString_AsString(param);
            jstr  = (*env)->NewStringUTF(env, (const char *) val);

            ret.l = jstr;
        }

        return ret;
    }

    case JARRAY_ID: {
        jobjectArray obj = NULL;
		jarray arr = NULL;
		jclass arrclazz;

        if(param == Py_None)
            ;
        else {
            PyJarray_Object *ar;
            // njensen changed this code to try and convert it from a numpy array,
            // it will return null if the parameter is already a pyjarray
            ret = convert_pynumpyarg_jvalue(env, param, paramType, paramTypeId, pos);
            if(ret.l != NULL)
            {
            	return ret;
            }

            ar = (PyJarray_Object *) param;

            if(!(*env)->IsAssignableFrom(env,
                                         ar->clazz,
                                         paramType)) {
                PyErr_Format(PyExc_TypeError,
                             "Incompatible array type at parameter %i.",
                             pos + 1);
                return ret;
            }

            // since this method is called before the value is used,
            // release the pinned array from here.
            pyjarray_release_pinned((PyJarray_Object *) param, 0);
            obj = ((PyJarray_Object *) param)->object;
        }

        ret.l = obj;
        return ret;
    }

    case JCLASS_ID: {
        jobject obj = NULL;
        // none is okay, we'll translate to null
        if(param == Py_None)
            ;
        else {
            if(!pyjclass_check(param)) {
                PyErr_Format(PyExc_TypeError,
                             "Expected class parameter at %i.",
                             pos + 1);
                return ret;
            }

            obj = ((PyJobject_Object *) param)->clazz;
        }

        ret.l = obj;
        return ret;
    }

    case JOBJECT_ID: {
        jobject obj = NULL;
        // none is okay, we'll translate to null
        if(param == Py_None)
            ;
        else if(PyString_Check(param)) {
            char *val;

            // strings count as objects here
            if(!(*env)->IsAssignableFrom(env,
                                         JSTRING_TYPE,
                                         paramType)) {
                PyErr_Format(
                    PyExc_TypeError,
                    "Tried to set a string on an incomparable parameter %i.",
                    pos + 1);
                return ret;
            }

            val = PyString_AsString(param);
            obj = (*env)->NewStringUTF(env, (const char *) val);
        }
        else {
        	//added by njensen
        	initNumpy();
        	if(PyArray_Check(param))
        	{
        		ret.l = numpyToJavaArray(env, param, NULL);
        		return ret;
        	}

            if(!pyjobject_check(param)) {
            	                PyErr_Format(PyExc_TypeError,
                             "Expected object parameter at %i.",
                             pos + 1);
                return ret;
            }

            // check object itself is assignable to that type.
            if(!(*env)->IsAssignableFrom(env,
                                         ((PyJobject_Object *) param)->clazz,
                                         paramType)) {
                PyErr_Format(PyExc_TypeError,
                             "Incorrect object type at %i.",
                             pos + 1);
                return ret;
            }

            obj = ((PyJobject_Object *) param)->object;
        }

        ret.l = obj;
        return ret;
    }

    case JSHORT_ID: {
        if(param == Py_None || !PyInt_Check(param)) {
            PyErr_Format(PyExc_TypeError,
                         "Expected short parameter at %i.",
                         pos + 1);
            return ret;
        }

        // precision loss...
        ret.s = (jshort) PyInt_AsLong(param);
        return ret;
    }

    case JINT_ID: {
        if(param == Py_None || !PyInt_Check(param)) {
            PyErr_Format(PyExc_TypeError,
                         "Expected int parameter at %i.",
                         pos + 1);
            return ret;
        }

        ret.i = (jint) PyInt_AS_LONG(param);
        return ret;
    }

    case JBYTE_ID: {
        if(param == Py_None || !PyInt_Check(param)) {
            PyErr_Format(PyExc_TypeError,
                         "Expected byte parameter at %i.",
                         pos + 1);
            return ret;
        }

        ret.b = (jbyte) PyInt_AS_LONG(param);
        return ret;
    }

    case JDOUBLE_ID: {
        if(param == Py_None || !PyFloat_Check(param)) {
            PyErr_Format(PyExc_TypeError,
                         "Expected double parameter at %i.",
                         pos + 1);
            return ret;
        }

        ret.d = (jdouble) PyFloat_AsDouble(param);
        return ret;
    }

    case JFLOAT_ID: {
        if(param == Py_None || !PyFloat_Check(param)) {
            PyErr_Format(PyExc_TypeError,
                         "Expected float parameter at %i.",
                         pos + 1);
            return ret;
        }

        // precision loss
        ret.f = (jfloat) PyFloat_AsDouble(param);
        return ret;
    }

    case JLONG_ID: {
        if(PyInt_Check(param))
            ret.j = (jlong) PyInt_AS_LONG(param);
        else if(PyLong_Check(param))
            ret.j = (jlong) PyLong_AsLongLong(param);
        else {
            PyErr_Format(PyExc_TypeError,
                         "Expected long parameter at %i.",
                         pos + 1);
            return ret;
        }

        return ret;
    }

    case JBOOLEAN_ID: {
        long bvalue;

        if(param == Py_None || !PyInt_Check(param)) {
            PyErr_Format(PyExc_TypeError,
                         "Expected boolean parameter at %i.",
                         pos + 1);
            return ret;
        }

        bvalue = PyInt_AsLong(param);
        if(bvalue > 0)
            ret.z = JNI_TRUE;
        else
            ret.z = JNI_FALSE;
        return ret;
    }

    } // switch

    PyErr_Format(PyExc_TypeError, "Unknown java type at %i.",
                 pos + 1);
    return ret;
}

// added by njensen
jarray numpyToJavaArray(JNIEnv* env, PyObject *param, jclass desiredType)
{
	int sz;
	jarray arr = NULL;
	PyObject *nobj = NULL;
	PyObject *nvalue = NULL;
	PyArrayObject *pa = NULL;
	int minDim = 0;
	int maxDim = 0;
	initNumpy();
	sz  = PyArray_Size(param);

	if(desiredType == NULL)
	{
		if(((PyArrayObject *) param)->descr->type_num == NPY_BOOL)
			desiredType = JBOOLEAN_ARRAY_TYPE;
		else if(((PyArrayObject *) param)->descr->type_num == NPY_BYTE)
			desiredType = JBYTE_ARRAY_TYPE;
                else if(((PyArrayObject *) param)->descr->type_num == NPY_INT16)
                        desiredType = JSHORT_ARRAY_TYPE;
		else if(((PyArrayObject *) param)->descr->type_num == NPY_INT32)
			desiredType = JINT_ARRAY_TYPE;
		else if(((PyArrayObject *) param)->descr->type_num == NPY_INT64)
			desiredType = JLONG_ARRAY_TYPE;
		else if(((PyArrayObject *) param)->descr->type_num == NPY_FLOAT32)
			desiredType = JFLOAT_ARRAY_TYPE;
		else if(((PyArrayObject *) param)->descr->type_num == NPY_FLOAT64)
			desiredType = JDOUBLE_ARRAY_TYPE;
	}

	if(desiredType != NULL)
	{
		if((*env)->IsSameObject(env, desiredType, JBOOLEAN_ARRAY_TYPE)
				&& (((PyArrayObject *) param)->descr->type_num == NPY_BOOL))
		{
			arr = (*env)->NewBooleanArray(env, sz);
			nobj = PyArray_ContiguousFromObject(param, NPY_BOOL, minDim, maxDim);
			nvalue = (PyObject *)PyArray_Cast((PyArrayObject *)nobj, NPY_BOOL);
			pa = (PyArrayObject *) nvalue;
			(*env)->SetBooleanArrayRegion(env, arr, 0, sz, (const jboolean *)pa->data);
		}
		else if((*env)->IsSameObject(env, desiredType, JBYTE_ARRAY_TYPE)
				 && (((PyArrayObject *) param)->descr->type_num == NPY_BYTE))
		{
			arr = (*env)->NewByteArray(env, sz);
			nobj = PyArray_ContiguousFromObject(param, NPY_BYTE, minDim, maxDim);
			nvalue = (PyObject *)PyArray_Cast((PyArrayObject *)nobj, NPY_BYTE);
			pa = (PyArrayObject *) nvalue;
			(*env)->SetByteArrayRegion(env, arr, 0, sz, (const jbyte *)pa->data);
		}
                else if((*env)->IsSameObject(env, desiredType, JSHORT_ARRAY_TYPE)
                                 && (((PyArrayObject *) param)->descr->type_num == NPY_INT16))
                {
                        arr = (*env)->NewShortArray(env, sz);
                        nobj = PyArray_ContiguousFromObject(param, NPY_INT16, minDim, maxDim);
                        nvalue = (PyObject *)PyArray_Cast((PyArrayObject *)nobj, NPY_INT16);
                        pa = (PyArrayObject *) nvalue;
                        (*env)->SetShortArrayRegion(env, arr, 0, sz, (const jshort *)pa->data);
                }
		else if((*env)->IsSameObject(env, desiredType, JINT_ARRAY_TYPE)
				&& (((PyArrayObject *) param)->descr->type_num == NPY_INT32))
		{
			arr = (*env)->NewIntArray(env, sz);
			nobj = PyArray_ContiguousFromObject(param, NPY_INT32, minDim, maxDim);
			nvalue = (PyObject *)PyArray_Cast((PyArrayObject *)nobj, NPY_INT32);
			pa = (PyArrayObject *) nvalue;
			(*env)->SetIntArrayRegion(env, arr, 0, sz, (const jint *)pa->data);
		}
		else if((*env)->IsSameObject(env, desiredType, JLONG_ARRAY_TYPE)
				&& (((PyArrayObject *) param)->descr->type_num == NPY_INT64))
		{
			arr = (*env)->NewLongArray(env, sz);
			nobj = PyArray_ContiguousFromObject(param, NPY_INT64, minDim, maxDim);
			nvalue = (PyObject *)PyArray_Cast((PyArrayObject *)nobj, NPY_INT64);
			pa = (PyArrayObject *) nvalue;
			(*env)->SetLongArrayRegion(env, arr, 0, sz, (const jlong *)pa->data);
		}
		else if ((*env)->IsSameObject(env, desiredType, JFLOAT_ARRAY_TYPE)
				&& (((PyArrayObject *) param)->descr->type_num == NPY_FLOAT32))
		{
			arr = (*env)->NewFloatArray(env, sz);
			nobj = PyArray_ContiguousFromObject(param, NPY_FLOAT32, minDim, maxDim);
			nvalue = (PyObject *)PyArray_Cast((PyArrayObject *)nobj, NPY_FLOAT32);
			pa = (PyArrayObject *) nvalue;
			(*env)->SetFloatArrayRegion(env, arr, 0, sz, (const jfloat *)pa->data);
		}
		else if((*env)->IsSameObject(env, desiredType, JDOUBLE_ARRAY_TYPE)
				&& (((PyArrayObject *) param)->descr->type_num == NPY_FLOAT64))
		{
			arr = (*env)->NewDoubleArray(env, sz);
			nobj = PyArray_ContiguousFromObject(param, NPY_FLOAT64, minDim, maxDim);
			nvalue = (PyObject *)PyArray_Cast((PyArrayObject *)nobj, NPY_FLOAT64);
			pa = (PyArrayObject *) nvalue;
			(*env)->SetDoubleArrayRegion(env, arr, 0, sz, (const jdouble *)pa->data);
		}
	}

	if(nobj != NULL)
		Py_DECREF(nobj);
	if(pa != NULL)
		Py_DECREF(pa);

	return arr;
}

// added by njensen
PyObject* javaToNumpyArray(JNIEnv* env, jobject jo, npy_intp *dims)
{
    PyObject *pyjob = NULL;
    int ysize, xsize;
    ysize = dims[0];
    xsize = dims[1];
    initNumpy(); 
    
    if((*env)->IsInstanceOf(env, jo, JBOOLEAN_ARRAY_TYPE))
    {
        jboolean *dataBool = NULL;
        pyjob = PyArray_SimpleNew(2, dims, NPY_BOOL);
        dataBool = (*env)->GetBooleanArrayElements(env, jo, 0);
        memcpy(((PyArrayObject *)pyjob)->data, dataBool, ysize * xsize * 1);
        (*env)->ReleaseBooleanArrayElements(env, jo, dataBool, 0);
    }
    else if((*env)->IsInstanceOf(env, jo, JBYTE_ARRAY_TYPE))
    {
        jbyte *dataByte = NULL;
        pyjob = PyArray_SimpleNew(2, dims, NPY_BYTE);
        dataByte = (*env)->GetByteArrayElements(env, jo, 0);
        memcpy(((PyArrayObject *)pyjob)->data, dataByte, ysize * xsize * 1);
        (*env)->ReleaseByteArrayElements(env, jo, dataByte, 0);
    }
    else if((*env)->IsInstanceOf(env, jo, JSHORT_ARRAY_TYPE))
    {
        jshort *dataShort = NULL;
        pyjob = PyArray_SimpleNew(2, dims, NPY_INT16);
        dataShort = (*env)->GetShortArrayElements(env, jo, 0);
        memcpy(((PyArrayObject *)pyjob)->data, dataShort, ysize * xsize * 2);
        (*env)->ReleaseShortArrayElements(env, jo, dataShort, 0);
    }
    else if((*env)->IsInstanceOf(env, jo, JINT_ARRAY_TYPE))
    {
        jint *dataInt = NULL;
        pyjob = PyArray_SimpleNew(2, dims, NPY_INT32);
        dataInt = (*env)->GetIntArrayElements(env, jo, 0);
        memcpy(((PyArrayObject *)pyjob)->data, dataInt, ysize * xsize * 4);
        (*env)->ReleaseIntArrayElements(env, jo, dataInt, 0);
    }
    else if((*env)->IsInstanceOf(env, jo, JLONG_ARRAY_TYPE))
    {
        jlong *dataLong = NULL;
        pyjob = PyArray_SimpleNew(2, dims, NPY_INT64);
        dataLong = (*env)->GetLongArrayElements(env, jo, 0);
        memcpy(((PyArrayObject *)pyjob)->data, dataLong, ysize * xsize * 8);
        (*env)->ReleaseLongArrayElements(env, jo, dataLong, 0);
    }
    else if((*env)->IsInstanceOf(env, jo, JFLOAT_ARRAY_TYPE))
    {
        jfloat *dataFloat = NULL;
        pyjob = PyArray_SimpleNew(2, dims, NPY_FLOAT32);
        dataFloat = (*env)->GetFloatArrayElements(env, jo, 0);
        memcpy(((PyArrayObject *)pyjob)->data, dataFloat, ysize * xsize * 4);
        (*env)->ReleaseFloatArrayElements(env, jo, dataFloat, 0);
    }
    else if((*env)->IsInstanceOf(env, jo, JDOUBLE_ARRAY_TYPE))
    {
        jdouble *dataDouble = NULL;
        pyjob = PyArray_SimpleNew(2, dims, NPY_FLOAT64);
        dataDouble = (*env)->GetDoubleArrayElements(env, jo, 0);
        memcpy(((PyArrayObject *)pyjob)->data, dataDouble, ysize * xsize * 8);
        (*env)->ReleaseDoubleArrayElements(env, jo, dataDouble, 0);
    }

    return pyjob;
}

// added by njensen, code from brockwoo
jarray pylistToJStringList(JNIEnv* env, PyObject* plist)
{
	int sz = PyList_Size(plist);
	jobjectArray ret= (jobjectArray)(*env)->NewObjectArray(env, sz,
						(*env)->FindClass(env, "java/lang/String"),
						(*env)->NewStringUTF(env, ""));
	int i = 0;
	char *tt;
	for(i = 0; i < sz; i++){
		PyObject *t = PyObject_Str(PyList_GetItem(plist, i));
		tt = PyString_AsString(t);
		(*env)->SetObjectArrayElement(env, ret, i, (*env)->NewStringUTF(env, tt));
		Py_DECREF(t);
	}
	return (jobject)ret;
}

// added by njensen
static void initNumpy(void)
{
    if (!numpyInited)
    {
        import_array();
        numpyInited = 1;
    }
}


// convenience function to pull a value from a list of tuples.
// expects tuples to be key, value format.
// parameters cannot be invalid!
// steals all references.
// returns new reference, new reference to Py_None if not found
PyObject* tuplelist_getitem(PyObject *list, PyObject *pyname) {
    int       i;
    int       listSize = 0;
    PyObject *ret      = NULL;

    listSize = PyList_GET_SIZE(list);
    for(i = 0; i < listSize; i++) {
        PyObject *tuple = PyList_GetItem(list, i);        /* borrowed */

        if(!tuple || !PyTuple_Check(tuple))
            continue;

        if(PyTuple_Size(tuple) == 2) {
            PyObject *key = PyTuple_GetItem(tuple, 0);    /* borrowed */
            if(!key || !PyString_Check(key))
                continue;

            if(PyObject_Compare(key, pyname) == 0) {
                ret   = PyTuple_GetItem(tuple, 1);        /* borrowed */
                break;
            }
        }
    }

    if(!ret)
        ret = Py_None;

    Py_INCREF(ret);
    return ret;
}


// reflect and retrieve Class array of exception types.
// register what we find as python exceptions in modjep.
int register_exceptions(JNIEnv *env,
                        jobject langClass,
                        jobject reflectObj,
                        jobjectArray exceptions) {
#if USE_MAPPED_EXCEPTIONS
    int        len, i;
    JepThread *jepThread;

    jepThread = pyembed_get_jepthread();

    len = (*env)->GetArrayLength(env, exceptions);
    for(i = 0; i < len; i++) {
        jobject   exceptionClass;
        jclass    exceptionClazz;
        PyObject *str, *pyexc, *_jep, *tmp;
        char     *className, *jep;

        exceptionClass = (*env)->GetObjectArrayElement(env,
                                                       exceptions,
                                                       i);
        if(process_java_exception(env) || !exceptionClass)
            return 0;

        exceptionClazz = (*env)->GetObjectClass(env,
                                                exceptionClass);
        if(process_java_exception(env) || !exceptionClazz)
            return 0;

        str = jobject_topystring(env, exceptionClass, exceptionClazz);
        if((*env)->ExceptionCheck(env) || !str)
            return 0;

        // let string handle parsing the class name, i'm lazy.
        // (okay, i don't want to add more includes.)

        if((tmp = pystring_split_last(str, ".")) == NULL) {
            Py_DECREF(str);
            (*env)->DeleteLocalRef(env, exceptionClazz);
            (*env)->DeleteLocalRef(env, exceptionClass);
            continue;
        }
        Py_DECREF(str);

        // don't add more
        if(PyObject_HasAttr(jepThread->modjep, tmp)) {
            Py_DECREF(tmp);
            (*env)->DeleteLocalRef(env, exceptionClazz);
            (*env)->DeleteLocalRef(env, exceptionClass);
            continue;
        }

        className = PyString_AsString(tmp);
        _jep = PyString_FromFormat("jep.%s", className);
        jep = PyString_AsString(_jep);

        pyexc = PyErr_NewException(jep, NULL, NULL);
        PyModule_AddObject(jepThread->modjep, className, pyexc); /* steals */
        if(PyErr_Occurred()) {
            printf("WARNING: Failed to add exception %s.\n", className);
            PyErr_Print();
        }

        Py_DECREF(tmp);
        Py_DECREF(_jep);
        (*env)->DeleteLocalRef(env, exceptionClazz);
        (*env)->DeleteLocalRef(env, exceptionClass);
    }

#endif // #if USE_MAPPED_EXCEPTIONS
    return 1;
}
