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

#include <stdlib.h> /* added by bkowal */

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

#include "pyembed.h"
#include "pyjobject.h"
#include "pyjmethod.h"
#include "pyjfield.h"
#include "pyjclass.h"
#include "util.h"

// added by njensen
#include "numpy/arrayobject.h"
#include "pyjmethodwrapper.h"

staticforward PyTypeObject PyJobject_Type;

static int pyjobject_init(JNIEnv *env, PyJobject_Object*);
static int pyjobject_setattr(PyJobject_Object*, char*, PyObject*);
static void pyjobject_addmethod(PyJobject_Object*, PyObject*);
static void pyjobject_addfield(PyJobject_Object*, PyObject*);
static void pyjobject_dealloc(PyJobject_Object*);

static jmethodID objectGetClass  = 0;
static jmethodID classGetMethods = 0;
static jmethodID classGetFields  = 0;

// all following static variables added by njensen
static jmethodID xMethod = 0;
static jmethodID yMethod = 0;
static jmethodID getNumpyMethod = 0;

static jmethodID classGetName  = 0;
static PyObject* classnamePyJMethodsDict = NULL;

// called internally to make new PyJobject_Object instances
PyObject* pyjobject_new(JNIEnv *env, jobject obj) {
    PyJobject_Object *pyjob;

    if(PyType_Ready(&PyJobject_Type) < 0)
        return NULL;
    if(!obj) {
        PyErr_Format(PyExc_RuntimeError, "Invalid object.");
        return NULL;
    }

    pyjob              = PyObject_NEW(PyJobject_Object, &PyJobject_Type);
    pyjob->object      = (*env)->NewGlobalRef(env, obj);
    pyjob->clazz       = (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, obj));
    pyjob->pyjclass    = NULL;
    pyjob->attr        = PyList_New(0);
    pyjob->methods     = PyList_New(0);
    pyjob->fields      = PyList_New(0);
    pyjob->finishAttr  = 0;

    if(pyjobject_init(env, pyjob))
        return (PyObject *) pyjob;
    return NULL;
}


PyObject* pyjobject_new_class(JNIEnv *env, jclass clazz) {
    PyJobject_Object *pyjob;

    if(!clazz) {
        PyErr_Format(PyExc_RuntimeError, "Invalid class object.");
        return NULL;
    }

    pyjob              = PyObject_NEW(PyJobject_Object, &PyJobject_Type);
    pyjob->object      = NULL;
    pyjob->clazz       = (*env)->NewGlobalRef(env, clazz);
    pyjob->attr        = PyList_New(0);
    pyjob->methods     = PyList_New(0);
    pyjob->fields      = PyList_New(0);
    pyjob->finishAttr  = 0;

    pyjob->pyjclass    = pyjclass_new(env, (PyObject *) pyjob);

    if(pyjobject_init(env, pyjob))
        return (PyObject *) pyjob;
    return NULL;
}


static int pyjobject_init(JNIEnv *env, PyJobject_Object *pyjob) {
    jobjectArray      methodArray = NULL;
    jobjectArray      fieldArray  = NULL;
    int               i, len = 0;
    jobject           langClass   = NULL;

    // added by njensen
    jstring			jClassName = NULL;
    const char *          charJClassName;
    PyObject *      pyJClassName = NULL;
    PyObject *      pyAttrName = NULL;
    PyObject *      cachedMethodList = NULL;
    jclass lock = NULL;


    (*env)->PushLocalFrame(env, 20);
    // ------------------------------ call Class.getMethods()


    // well, first call getClass()
    if(objectGetClass == 0) {
        objectGetClass = (*env)->GetMethodID(env,
                                             pyjob->clazz,
                                             "getClass",
                                             "()Ljava/lang/Class;");
        if(process_java_exception(env) || !objectGetClass)
            goto EXIT_ERROR;
    }

    langClass = (*env)->CallObjectMethod(env, pyjob->clazz, objectGetClass);
    if(process_java_exception(env) || !langClass)
        goto EXIT_ERROR;

    // then, get methodid for getMethods()
    if(classGetMethods == 0) {
        classGetMethods = (*env)->GetMethodID(env,
                                              langClass,
                                              "getMethods",
                                              "()[Ljava/lang/reflect/Method;");
        if(process_java_exception(env) || !classGetMethods)
            goto EXIT_ERROR;
    }

    // added by njensen
	if(classGetName == 0)
	{
		classGetName = (*env)->GetMethodID(env,
															langClass,
															"getName",
															"()Ljava/lang/String;");
	}
	jClassName = (*env)->CallObjectMethod(env, pyjob->clazz, classGetName);
	charJClassName = jstring2char(env, jClassName);
	pyJClassName = PyString_FromString(charJClassName);
	release_utf_char(env, jClassName, charJClassName);
	pyAttrName = PyString_FromString("jclassname");
	if(PyObject_SetAttr((PyObject *) pyjob,
									pyAttrName,
									pyJClassName) != 0)
	{
		PyErr_Format(PyExc_RuntimeError, "Couldn't add jclassname as attribute.");
	}
	PyList_Append(pyjob->fields, pyAttrName);
	Py_DECREF(pyAttrName);
	(*env)->DeleteLocalRef(env, jClassName);
	pyjob->jclassname = pyJClassName;
	// end of jclassname code

	// begin synchronized
	lock = (*env)->FindClass(env, "java/lang/String");
	if((*env)->MonitorEnter(env, lock) != JNI_OK)
	{
		PyErr_Format(PyExc_RuntimeError, "Couldn't get synchronization lock on class method creation.");
	}
    if(classnamePyJMethodsDict == NULL)
    {
    	classnamePyJMethodsDict = PyDict_New();
    }

    cachedMethodList = PyDict_GetItem(classnamePyJMethodsDict, pyJClassName);
    if(cachedMethodList == NULL)
    {
    	PyObject* pyjMethodList = NULL;
    	pyjMethodList = PyList_New(0);

    	// njensen: I changed the architecture to speed it up so it caches
    	// pyjmethods for each Java Class object, then applies that method
    	// to the object in question.  The cache is built here, then
    	// pyjobject_getattr associates the object with the method, and
    	// pyjmethod_call ensures it is passed along.


		// - GetMethodID fails when you pass the clazz object, it expects
		//   a java.lang.Class jobject.
		// - if you CallObjectMethod with the langClass jclass object,
		//   it'll return an array of methods, but they're methods of the
		//   java.lang.reflect.Method class -- not ->object.
		//
		// so what i did here was find the methodid using langClass,
		// but then i call the method using clazz. methodIds for java
		// classes are shared....

		methodArray = (jobjectArray) (*env)->CallObjectMethod(env,
															  pyjob->clazz,
															  classGetMethods);
		if(process_java_exception(env) || !methodArray)
			goto EXIT_ERROR;

		// for each method, create a new pyjmethod object
		// and add to the internal methods list.
		len = (*env)->GetArrayLength(env, methodArray);
		for(i = 0; i < len; i++) {
			PyJmethod_Object *pymethod = NULL;
			jobject           rmethod  = NULL;

			rmethod = (*env)->GetObjectArrayElement(env,
													methodArray,
													i);

			// make new PyJmethod_Object, linked to pyjob
			if(pyjob->object)
				pymethod = pyjmethod_new(env, rmethod, pyjob);
			else
				pymethod = pyjmethod_new_static(env, rmethod, pyjob);

			if(!pymethod)
				continue;

			if(pymethod->pyMethodName && PyString_Check(pymethod->pyMethodName)) {
				if(PyList_Append(pyjMethodList, (PyObject*) pymethod) != 0)
						printf("WARNING: couldn't add method");
			}

			Py_DECREF(pymethod);
			(*env)->DeleteLocalRef(env, rmethod);
		}
		PyDict_SetItem(classnamePyJMethodsDict, pyJClassName, pyjMethodList);
		cachedMethodList = pyjMethodList;
		(*env)->DeleteLocalRef(env, methodArray);
    } // end of looping over available methods
    if((*env)->MonitorExit(env, lock) != JNI_OK)
    {
    	PyErr_Format(PyExc_RuntimeError, "Couldn't release synchronization lock on class method creation.");
    }
    // end of synchronization

	len = PyList_Size(cachedMethodList);
	for(i = 0; i < len; i++)
	{
		PyJmethod_Object* pymethod = (PyJmethod_Object*) PyList_GetItem(cachedMethodList, i);
		if(PyObject_SetAttr((PyObject *) pyjob, pymethod->pyMethodName, (PyObject*) pymethod) != 0)
		{
			PyErr_Format(PyExc_RuntimeError, "Couldn't add method as attribute.");
		}
		else
		{
			pyjobject_addmethod(pyjob, pymethod->pyMethodName);
		}
	}
	// end of cached method optimizations


    // ------------------------------ process fields

    if(classGetFields == 0) {
        classGetFields = (*env)->GetMethodID(env,
                                             langClass,
                                             "getFields",
                                             "()[Ljava/lang/reflect/Field;");
        if(process_java_exception(env) || !classGetFields)
            goto EXIT_ERROR;
    }

    fieldArray = (jobjectArray) (*env)->CallObjectMethod(env,
                                                         pyjob->clazz,
                                                         classGetFields);
    if(process_java_exception(env) || !fieldArray)
        goto EXIT_ERROR;


    // for each field, create a pyjfield object and
    // add to the internal members list.
    len = (*env)->GetArrayLength(env, fieldArray);
    for(i = 0; i < len; i++) {
        jobject          rfield   = NULL;
        PyJfield_Object *pyjfield = NULL;

        rfield = (*env)->GetObjectArrayElement(env,
                                               fieldArray,
                                               i);

        pyjfield = pyjfield_new(env, rfield, pyjob);

        if(!pyjfield)
            continue;

        if(pyjfield->pyFieldName && PyString_Check(pyjfield->pyFieldName)) {
            if(PyObject_SetAttr((PyObject *) pyjob,
                                pyjfield->pyFieldName,
                                (PyObject *) pyjfield) != 0) {
                printf("WARNING: couldn't add field.\n");
            }
            else
                pyjobject_addfield(pyjob, pyjfield->pyFieldName);
        }

        Py_DECREF(pyjfield);
        (*env)->DeleteLocalRef(env, rfield);
    }
    (*env)->DeleteLocalRef(env, fieldArray);

    // we've finished the object.
    pyjob->finishAttr = 1;
    (*env)->PopLocalFrame(env, NULL);
    return 1;


EXIT_ERROR:
    (*env)->PopLocalFrame(env, NULL);

    if(PyErr_Occurred()) { // java exceptions translated by this time
        if(pyjob)
            pyjobject_dealloc(pyjob);
    }

    return 0;
}


static void pyjobject_dealloc(PyJobject_Object *self) {
#if USE_DEALLOC
    JNIEnv *env = pyembed_get_env();
    if(env) {
        if(self->object)
            (*env)->DeleteGlobalRef(env, self->object);
        if(self->clazz)
            (*env)->DeleteGlobalRef(env, self->clazz);

        Py_DECREF(self->attr);
        Py_DECREF(self->methods);
        Py_DECREF(self->fields);
        Py_DECREF(self->jclassname);  // added by njensen
        if(self->pyjclass)
            Py_DECREF(self->pyjclass);
    }

    PyObject_Del(self);
#endif
}


static PyObject* pyjobject_call(PyJobject_Object *self,
                                PyObject *args,
                                PyObject *keywords) {

    if(!self->pyjclass) {
        PyErr_Format(PyExc_RuntimeError, "Not a class.");
        return NULL;
    }

    return pyjclass_call(self->pyjclass, args, keywords);
}


int pyjobject_check(PyObject *obj) {
    if(PyObject_TypeCheck(obj, &PyJobject_Type))
        return 1;
    return 0;
}


// add a method name to obj->methods list
static void pyjobject_addmethod(PyJobject_Object *obj, PyObject *name) {
    if(!PyString_Check(name))
        return;
    if(!PyList_Check(obj->methods))
        return;

    PyList_Append(obj->methods, name);
}


static void pyjobject_addfield(PyJobject_Object *obj, PyObject *name) {
    if(!PyString_Check(name))
        return;
    if(!PyList_Check(obj->fields))
        return;

    PyList_Append(obj->fields, name);
}


// find and call a method on this object that matches the python args.
// typically called by way of pyjmethod when python invokes __call__.
//
// steals reference to self, methodname and args.
PyObject* find_method(JNIEnv *env,
		              PyJobject_Object *self,
                      PyObject *methodName,
                      int methodCount,
                      PyObject *attr,
                      PyObject *args) {
    // all possible method candidates
    PyJmethod_Object **cand = NULL;
    int                pos, i, listSize, argsSize;

    pos = i = listSize = argsSize = 0;

    // not really likely if we were called from pyjmethod, but hey...
    if(methodCount < 1) {
        PyErr_Format(PyExc_RuntimeError, "I have no methods.");
        return NULL;
    }

    if(!attr || !PyList_CheckExact(attr)) {
        PyErr_Format(PyExc_RuntimeError, "Invalid attr list.");
        return NULL;
    }

    cand = (PyJmethod_Object **)
        PyMem_Malloc(sizeof(PyJmethod_Object*) * methodCount);

    // just for safety
    for(i = 0; i < methodCount; i++)
        cand[i] = NULL;

    listSize = PyList_GET_SIZE(attr);
    for(i = 0; i < listSize; i++) {
        PyObject *tuple = PyList_GetItem(attr, i);               /* borrowed */

        if(PyErr_Occurred())
            break;

        if(!tuple || tuple == Py_None || !PyTuple_CheckExact(tuple))
            continue;

        if(PyTuple_Size(tuple) == 2) {
            PyObject *key = PyTuple_GetItem(tuple, 0);           /* borrowed */

            if(PyErr_Occurred())
                break;

            if(!key || !PyString_Check(key))
                continue;

            if(PyObject_Compare(key, methodName) == 0) {
                PyObject *method = PyTuple_GetItem(tuple, 1);    /* borrowed */
                if(pyjmethod_check(method))
                    cand[pos++] = (PyJmethod_Object *) method;
            }
        }
    }

    if(PyErr_Occurred())
        goto EXIT_ERROR;

    // makes more sense to work with...
    pos--;

    if(pos < 0) {
        // didn't find a method by that name....
        // that shouldn't happen unless the search above is broken.
        PyErr_Format(PyExc_NameError, "No such method.");
        goto EXIT_ERROR;
    }
    if(pos == 0) {
        // we're done, call that one
        PyObject *ret = pyjmethod_call_internal(cand[0], self, args);
        PyMem_Free(cand);
        return ret;
    }

    // first, find out if there's only one method that
    // has the correct number of args
    argsSize = PyTuple_Size(args);
    {
        PyJmethod_Object *matching = NULL;
        int               count    = 0;

        for(i = 0; i <= pos && cand[i]; i++) {
            // make sure method is fully initialized
            if(!cand[i]->parameters) {
                if(!pyjmethod_init(env, cand[i])) {
                    // init failed, that's not good.
                    cand[i] = NULL;
                    PyErr_Warn(PyExc_Warning, "pyjmethod init failed.");
                    continue;
                }
            }

            if(cand[i]->lenParameters == argsSize) {
                matching = cand[i];
                count++;
            }
            else
                cand[i] = NULL; // eliminate non-matching
        }

        if(matching && count == 1) {
            PyMem_Free(cand);
            return pyjmethod_call_internal(matching, self, args);
        }
    } // local scope

    for(i = 0; i <= pos; i++) {
        int parmpos = 0;

        // already eliminated?
        if(!cand[i])
            continue;

        // check if argument types match
        (*env)->PushLocalFrame(env, 20);
        for(parmpos = 0; parmpos < cand[i]->lenParameters; parmpos++) {
            PyObject *param       = PyTuple_GetItem(args, parmpos);
            int       paramTypeId = -1;
            jclass    pclazz;
            jclass    paramType =
                (jclass) (*env)->GetObjectArrayElement(env,
                                                       cand[i]->parameters,
                                                       parmpos);

            if(process_java_exception(env) || !paramType)
                break;

            pclazz = (*env)->GetObjectClass(env, paramType);
            if(process_java_exception(env) || !pclazz)
                break;

            paramTypeId = get_jtype(env, paramType, pclazz);

            if(pyarg_matches_jtype(env, param, paramType, paramTypeId)) {
                if(PyErr_Occurred())
                    break;
                continue;
            }

            // args don't match
            break;
        }
        (*env)->PopLocalFrame(env, NULL);

        // this method matches?
        if(parmpos == cand[i]->lenParameters) {
            PyObject *ret = pyjmethod_call_internal(cand[i], self, args);
            PyMem_Free(cand);
            return ret;
        }
    }


EXIT_ERROR:
    PyMem_Free(cand);
    if(!PyErr_Occurred())
        PyErr_Format(PyExc_NameError,
                     "Matching overloaded method not found.");
    return NULL;
}


// find and call a method on this object that matches the python args.
// typically called from pyjmethod when python invokes __call__.
//
// steals reference to self, methodname and args.
PyObject* pyjobject_find_method(PyJobject_Object *self,
                                PyObject *methodName,
                                PyObject *args) {
    // util method does this for us
    return find_method(pyembed_get_env(),
    				   self,
                       methodName,
                       PyList_Size(self->methods),
                       self->attr,
                       args);
}


// call toString() on jobject. returns null on error.
// excpected to return new reference.
static PyObject* pyjobject_str(PyJobject_Object *self) {
    PyObject   *pyres     = NULL;
    JNIEnv     *env;

    env   = pyembed_get_env();
    pyres = jobject_topystring(env, self->object, self->clazz);

    if(process_java_exception(env))
        return NULL;

    // python doesn't like Py_None here...
    if(pyres == NULL)
        return Py_BuildValue("s", "");

    return pyres;
}


// get attribute 'name' for object.
// uses obj->attr list of tuples for storage.
// returns new reference.
static PyObject* pyjobject_getattr(PyJobject_Object *obj,
                                   char *name) {
    PyObject *ret, *pyname, *methods, *members, *numpy;
    int       listSize, i, found;

    ret = pyname = methods = members = NULL;
    listSize = i = found = 0;

    if(!name) {
        Py_INCREF(Py_None);
        return Py_None;
    }
    pyname  = PyString_FromString(name);
    methods = PyString_FromString("__methods__");
    members = PyString_FromString("__members__");

    // numpy added by njensen
    numpy = PyString_FromString("__numpy__");
    if(PyObject_Compare(pyname, numpy) == 0) {
        Py_DECREF(pyname);
        Py_DECREF(methods);
        Py_DECREF(members);
        Py_DECREF(numpy);

        return pyjobject_numpy(obj);
    }
    Py_DECREF(numpy);

    if(PyObject_Compare(pyname, methods) == 0) {
        Py_DECREF(pyname);
        Py_DECREF(methods);
        Py_DECREF(members);

        Py_INCREF(obj->methods);
        return obj->methods;
    }
    Py_DECREF(methods);

    if(PyObject_Compare(pyname, members) == 0) {
        Py_DECREF(pyname);
        Py_DECREF(members);

        Py_INCREF(obj->fields);
        return obj->fields;
    }
    Py_DECREF(members);

    if(!PyList_Check(obj->attr)) {
        Py_DECREF(pyname);
        PyErr_Format(PyExc_RuntimeError, "Invalid attr list.");
        return NULL;
    }

    // util function fetches from attr list for us.
    if(obj->attr == NULL)
    {
    	printf("attribute list is null!\n");
    	printf("name is %s\n", name);
    }
    ret = tuplelist_getitem(obj->attr, pyname);      /* new reference */

    Py_DECREF(pyname);

    // method optimizations by njensen
    if(pyjmethod_check(ret))
    {
    	PyJmethodWrapper_Object* wrapper = pyjmethodwrapper_new(obj, (PyJmethod_Object*) ret);
    	Py_DECREF(ret);
    	Py_INCREF(wrapper);
    	ret = (PyObject *) wrapper;
    }

    if(PyErr_Occurred() || ret == Py_None) {
        PyErr_Format(PyExc_NameError, "Method not found %s", name);
        return NULL;
    }

    if(pyjfield_check(ret)) {
        PyObject *t = pyjfield_get((PyJfield_Object *) ret);
        Py_DECREF(ret);
        return t;
    }

    return ret;
}


// added by njensen
static PyObject* pyjobject_numpy(PyJobject_Object *obj) {
    int i=0;
    /* updated by bkowal */
    npy_intp *dims = NULL;
    jobjectArray objarray = NULL;
    PyObject *resultList = NULL;
    jint xsize = 0;
    jint ysize = 0;
    jsize listSize =0;

    JNIEnv *env = pyembed_get_env();
    // methods are forever but classes are fleeting
    jclass numpyable = (*env)->FindClass(env, "jep/INumpyable");
    if((*env)->IsInstanceOf(env, obj->object, numpyable))
    {
        if(xMethod == NULL)
            xMethod = (*env)->GetMethodID(env, numpyable, "getNumpyX", "()I");
        xsize = (jint) (*env)->CallIntMethod(env, obj->object, xMethod);

        if(yMethod == NULL)
            yMethod = (*env)->GetMethodID(env, numpyable, "getNumpyY", "()I");
        ysize = (jint) (*env)->CallIntMethod(env, obj->object, yMethod);

        dims = malloc(2 * sizeof(npy_intp));
        dims[0] = ysize;
        dims[1] = xsize;

        if(getNumpyMethod == NULL)
            getNumpyMethod = (*env)->GetMethodID(env, numpyable, "getNumpy", "()[Ljava/lang/Object;");
        objarray = (jobjectArray) (*env)->CallObjectMethod(env, obj->object, getNumpyMethod);
        if(process_java_exception(env) || !objarray)
        {
                Py_INCREF(Py_None);
                return Py_None;
        }

        listSize = (*env)->GetArrayLength(env, objarray);
        resultList = PyList_New(listSize);               
        for(i=0; i < listSize; i=i+1)
        {
               PyObject      *pyjob = NULL; 
               jobject jo = (*env)->GetObjectArrayElement(env, objarray, i);
               pyjob = javaToNumpyArray(env, jo, dims);
               if(pyjob == NULL)
               {
                   PyErr_Format(PyExc_TypeError, "Cannot transform INumpyable.getNumpy()[%i] java object to numpy array", i);
                   free(dims);
                   Py_DECREF(resultList);
                   return NULL;                       
               }               
               PyList_SetItem(resultList, i, pyjob);
               (*env)->DeleteLocalRef(env, jo);
        }
        free(dims);
        (*env)->DeleteLocalRef(env, objarray);
        return resultList;
    }
    else
    {
        PyErr_Format(PyExc_TypeError, "Object does not implement INumpyable and therefore cannot be transformed to numpy array");
        return NULL;
    }
}


// set attribute v for object.
// uses obj->attr dictionary for storage.
static int pyjobject_setattr(PyJobject_Object *obj,
                             char *name,
                             PyObject *v) {
    PyObject *pyname, *tuple;

    if(!name) {
        PyErr_Format(PyExc_RuntimeError, "Invalid name: NULL.");
        return -1;
    }

    if(!PyList_Check(obj->attr)) {
        PyErr_Format(PyExc_RuntimeError, "Invalid attr list.");
        return -1;
    }

    Py_INCREF(v);

    if(obj->finishAttr) {
        PyObject *cur, *pyname;
        int       ret;

        // finished setting internal objects.
        // don't allow python to add new, but do
        // allow python script to change values on pyjfields

        pyname = PyString_FromString(name);
        cur    = tuplelist_getitem(obj->attr, pyname);      /* new reference */
        Py_DECREF(pyname);

        if(PyErr_Occurred())
            return -1;

        if(cur == Py_None) {
            PyErr_SetString(PyExc_RuntimeError, "No such field.");
            return -1;
        }

        if(!pyjfield_check(cur)) {
            PyErr_SetString(PyExc_TypeError, "Not a pyjfield object.");
            return -1;
        }

        if(!PyList_Check(obj->attr)) {
            Py_DECREF(pyname);
            PyErr_SetString(PyExc_RuntimeError, "Invalid attr list.");
            return -1;
        }

        // now, just ask pyjfield to handle.
        ret = pyjfield_set((PyJfield_Object *) cur, v); /* borrows ref */

        Py_DECREF(cur);
        Py_DECREF(v);
        return ret;
    }

    pyname = PyString_FromString((const char *) name);
    tuple  = PyTuple_New(2);

    Py_INCREF(pyname);
    PyTuple_SetItem(tuple, 0, pyname);   /* steals ref */
    PyTuple_SetItem(tuple, 1, v);        /* steals ref */

    // the docs don't mention this, but the source INCREFs tuple
    // ...
    // after much printf'ing. uhm. must decref it somewhere.
    // ...
    // doh. the docs suck.

    // Py_INCREF(tuple);

    PyList_Append(obj->attr, tuple);

    Py_DECREF(tuple);
    Py_DECREF(pyname);
    return 0;  // success
}


static PyMethodDef pyjobject_methods[] = {
    {NULL, NULL, 0, NULL}
};


static PyTypeObject PyJobject_Type = {
    PyObject_HEAD_INIT(0)
    0,                                        /* ob_size */
    "PyJobject",                              /* tp_name */
    sizeof(PyJobject_Object),                 /* tp_basicsize */
    0,                                        /* tp_itemsize */
    (destructor) pyjobject_dealloc,           /* tp_dealloc */
    0,                                        /* tp_print */
    (getattrfunc) pyjobject_getattr,          /* tp_getattr */
    (setattrfunc) pyjobject_setattr,          /* tp_setattr */
    0,                                        /* tp_compare */
    0,                                        /* tp_repr */
    0,                                        /* tp_as_number */
    0,                                        /* tp_as_sequence */
    0,                                        /* tp_as_mapping */
    0,                                        /* tp_hash  */
    (ternaryfunc) pyjobject_call,             /* tp_call */
    (reprfunc) pyjobject_str,                 /* tp_str */
    0,                                        /* tp_getattro */
    0,                                        /* tp_setattro */
    0,                                        /* tp_as_buffer */
    Py_TPFLAGS_DEFAULT,                       /* tp_flags */
    "jobject",                                /* tp_doc */
    0,                                        /* tp_traverse */
    0,                                        /* tp_clear */
    0,                                        /* tp_richcompare */
    0,                                        /* tp_weaklistoffset */
    0,                                        /* tp_iter */
    0,                                        /* tp_iternext */
    pyjobject_methods,                        /* tp_methods */
    0,                                        /* tp_members */
    0,                                        /* tp_getset */
    0,                                        /* tp_base */
    0,                                        /* tp_dict */
    0,                                        /* tp_descr_get */
    0,                                        /* tp_descr_set */
    0,                                        /* tp_dictoffset */
    0,                                        /* tp_init */
    0,                                        /* tp_alloc */
    NULL,                                     /* tp_new */
};
