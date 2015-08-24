# Copyright (C) 2003-2005 Peter J. Verveer
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above
#    copyright notice, this list of conditions and the following
#    disclaimer in the documentation and/or other materials provided
#    with the distribution.
#
# 3. The name of the author may not be used to endorse or promote
#    products derived from this software without specific prior
#    written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
# OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

<<<<<<< HEAD
import math
import numpy
import numpy as np
from numpy import fft
from numpy.testing import *
import scipy.ndimage as ndimage

eps = 1e-12

def diff(a, b):
    if not isinstance(a, numpy.ndarray):
        a = numpy.asarray(a)
    if not isinstance(b, numpy.ndarray):
        b = numpy.asarray(b)
    if (0 in a.shape) and (0 in b.shape):
        return 0.0
    if (a.dtype in [numpy.complex64, numpy.complex128] or
        b.dtype in [numpy.complex64, numpy.complex128]):
        a = numpy.asarray(a, numpy.complex128)
        b = numpy.asarray(b, numpy.complex128)
        t = ((a.real - b.real)**2).sum() + ((a.imag - b.imag)**2).sum()
    if (a.dtype == numpy.object or b.dtype == numpy.object):
        t = sum([diff(c,d)**2 for c,d in zip(a,b)])
    else:
        a = numpy.asarray(a)
        a = a.astype(numpy.float64)
        b = numpy.asarray(b)
        b = b.astype(numpy.float64)
        t = ((a - b)**2).sum()
    return math.sqrt(t)


class TestNdimage(TestCase):

    def setUp(self):
        # list of numarray data types
        self.types = [numpy.int8, numpy.uint8, numpy.int16,
                      numpy.uint16, numpy.int32, numpy.uint32,
                      numpy.int64, numpy.uint64,
                      numpy.float32, numpy.float64]
=======
from __future__ import division, print_function, absolute_import

import warnings
import math
import sys

import numpy
from numpy import fft
from numpy.testing import (assert_, assert_equal, assert_array_equal,
        run_module_suite, assert_array_almost_equal, assert_almost_equal, dec)
import scipy.ndimage as ndimage
from nose import SkipTest


eps = 1e-12


def sumsq(a, b):
    return math.sqrt(((a - b)**2).sum())


class TestNdimage:
    def setUp(self):
        # list of numarray data types
        self.integer_types = [numpy.int8, numpy.uint8, numpy.int16,
                numpy.uint16, numpy.int32, numpy.uint32,
                numpy.int64, numpy.uint64]

        self.float_types = [numpy.float32, numpy.float64]

        self.types = self.integer_types + self.float_types
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b

        # list of boundary modes:
        self.modes = ['nearest', 'wrap', 'reflect', 'mirror', 'constant']

    def test_correlate01(self):
<<<<<<< HEAD
        "correlation 1"
        array = numpy.array([1, 2])
        weights = numpy.array([2])
        true = [2, 4]
        output = ndimage.correlate(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.convolve(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.correlate1d(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.convolve1d(array, weights)
        self.failUnless(diff(output, true) < eps)

    def test_correlate02(self):
        "correlation 2"
        array = numpy.array([1, 2, 3])
        kernel = numpy.array([1])
        output = ndimage.correlate(array, kernel)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.convolve(array, kernel)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.correlate1d(array, kernel)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.convolve1d(array, kernel)
        self.failUnless(diff(array, output) < eps)

    def test_correlate03(self):
        "correlation 3"
        array = numpy.array([1])
        weights = numpy.array([1, 1])
        true = [2]
        output = ndimage.correlate(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.convolve(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.correlate1d(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.convolve1d(array, weights)
        self.failUnless(diff(output, true) < eps)

    def test_correlate04(self):
        "correlation 4"
=======
        array = numpy.array([1, 2])
        weights = numpy.array([2])
        expected = [2, 4]

        output = ndimage.correlate(array, weights)
        assert_array_almost_equal(output, expected)

        output = ndimage.convolve(array, weights)
        assert_array_almost_equal(output, expected)

        output = ndimage.correlate1d(array, weights)
        assert_array_almost_equal(output, expected)

        output = ndimage.convolve1d(array, weights)
        assert_array_almost_equal(output, expected)

    def test_correlate02(self):
        array = numpy.array([1, 2, 3])
        kernel = numpy.array([1])

        output = ndimage.correlate(array, kernel)
        assert_array_almost_equal(array, output)

        output = ndimage.convolve(array, kernel)
        assert_array_almost_equal(array, output)

        output = ndimage.correlate1d(array, kernel)
        assert_array_almost_equal(array, output)

        output = ndimage.convolve1d(array, kernel)
        assert_array_almost_equal(array, output)

    def test_correlate03(self):
        array = numpy.array([1])
        weights = numpy.array([1, 1])
        expected = [2]

        output = ndimage.correlate(array, weights)
        assert_array_almost_equal(output, expected)

        output = ndimage.convolve(array, weights)
        assert_array_almost_equal(output, expected)

        output = ndimage.correlate1d(array, weights)
        assert_array_almost_equal(output, expected)

        output = ndimage.convolve1d(array, weights)
        assert_array_almost_equal(output, expected)

    def test_correlate04(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([1, 2])
        tcor = [2, 3]
        tcov = [3, 4]
        weights = numpy.array([1, 1])
        output = ndimage.correlate(array, weights)
<<<<<<< HEAD
        self.failUnless(diff(output, tcor) < eps)
        output = ndimage.convolve(array, weights)
        self.failUnless(diff(output, tcov) < eps)
        output = ndimage.correlate1d(array, weights)
        self.failUnless(diff(output, tcor) < eps)
        output = ndimage.convolve1d(array, weights)
        self.failUnless(diff(output, tcov) < eps)

    def test_correlate05(self):
        "correlation 5"
=======
        assert_array_almost_equal(output, tcor)
        output = ndimage.convolve(array, weights)
        assert_array_almost_equal(output, tcov)
        output = ndimage.correlate1d(array, weights)
        assert_array_almost_equal(output, tcor)
        output = ndimage.convolve1d(array, weights)
        assert_array_almost_equal(output, tcov)

    def test_correlate05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([1, 2, 3])
        tcor = [2, 3, 5]
        tcov = [3, 5, 6]
        kernel = numpy.array([1, 1])
        output = ndimage.correlate(array, kernel)
<<<<<<< HEAD
        self.failUnless(diff(tcor, output) < eps)
        output = ndimage.convolve(array, kernel)
        self.failUnless(diff(tcov, output) < eps)
        output = ndimage.correlate1d(array, kernel)
        self.failUnless(diff(tcor, output) < eps)
        output = ndimage.convolve1d(array, kernel)
        self.failUnless(diff(tcov, output) < eps)

    def test_correlate06(self):
        "correlation 6"
=======
        assert_array_almost_equal(tcor, output)
        output = ndimage.convolve(array, kernel)
        assert_array_almost_equal(tcov, output)
        output = ndimage.correlate1d(array, kernel)
        assert_array_almost_equal(tcor, output)
        output = ndimage.convolve1d(array, kernel)
        assert_array_almost_equal(tcov, output)

    def test_correlate06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([1, 2, 3])
        tcor = [9, 14, 17]
        tcov = [7, 10, 15]
        weights = numpy.array([1, 2, 3])
        output = ndimage.correlate(array, weights)
<<<<<<< HEAD
        self.failUnless(diff(output, tcor) < eps)
        output = ndimage.convolve(array, weights)
        self.failUnless(diff(output, tcov) < eps)
        output = ndimage.correlate1d(array, weights)
        self.failUnless(diff(output, tcor) < eps)
        output = ndimage.convolve1d(array, weights)
        self.failUnless(diff(output, tcov) < eps)

    def test_correlate07(self):
        "correlation 7"
        array = numpy.array([1, 2, 3])
        true = [5, 8, 11]
        weights = numpy.array([1, 2, 1])
        output = ndimage.correlate(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.convolve(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.correlate1d(array, weights)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.convolve1d(array, weights)
        self.failUnless(diff(output, true) < eps)

    def test_correlate08(self):
        "correlation 8"
=======
        assert_array_almost_equal(output, tcor)
        output = ndimage.convolve(array, weights)
        assert_array_almost_equal(output, tcov)
        output = ndimage.correlate1d(array, weights)
        assert_array_almost_equal(output, tcor)
        output = ndimage.convolve1d(array, weights)
        assert_array_almost_equal(output, tcov)

    def test_correlate07(self):
        array = numpy.array([1, 2, 3])
        expected = [5, 8, 11]
        weights = numpy.array([1, 2, 1])
        output = ndimage.correlate(array, weights)
        assert_array_almost_equal(output, expected)
        output = ndimage.convolve(array, weights)
        assert_array_almost_equal(output, expected)
        output = ndimage.correlate1d(array, weights)
        assert_array_almost_equal(output, expected)
        output = ndimage.convolve1d(array, weights)
        assert_array_almost_equal(output, expected)

    def test_correlate08(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([1, 2, 3])
        tcor = [1, 2, 5]
        tcov = [3, 6, 7]
        weights = numpy.array([1, 2, -1])
        output = ndimage.correlate(array, weights)
<<<<<<< HEAD
        self.failUnless(diff(output, tcor) < eps)
        output = ndimage.convolve(array, weights)
        self.failUnless(diff(output, tcov) < eps)
        output = ndimage.correlate1d(array, weights)
        self.failUnless(diff(output, tcor) < eps)
        output = ndimage.convolve1d(array, weights)
        self.failUnless(diff(output, tcov) < eps)

    def test_correlate09(self):
        "correlation 9"
        array = []
        kernel = numpy.array([1, 1])
        output = ndimage.correlate(array, kernel)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.convolve(array, kernel)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.correlate1d(array, kernel)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.convolve1d(array, kernel)
        self.failUnless(diff(array, output) < eps)

    def test_correlate10(self):
        "correlation 10"
        array = [[]]
        kernel = numpy.array([[1, 1]])
        output = ndimage.correlate(array, kernel)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.convolve(array, kernel)
        self.failUnless(diff(array, output) < eps)

    def test_correlate11(self):
        "correlation 11"
=======
        assert_array_almost_equal(output, tcor)
        output = ndimage.convolve(array, weights)
        assert_array_almost_equal(output, tcov)
        output = ndimage.correlate1d(array, weights)
        assert_array_almost_equal(output, tcor)
        output = ndimage.convolve1d(array, weights)
        assert_array_almost_equal(output, tcov)

    def test_correlate09(self):
        array = []
        kernel = numpy.array([1, 1])
        output = ndimage.correlate(array, kernel)
        assert_array_almost_equal(array, output)
        output = ndimage.convolve(array, kernel)
        assert_array_almost_equal(array, output)
        output = ndimage.correlate1d(array, kernel)
        assert_array_almost_equal(array, output)
        output = ndimage.convolve1d(array, kernel)
        assert_array_almost_equal(array, output)

    def test_correlate10(self):
        array = [[]]
        kernel = numpy.array([[1, 1]])
        output = ndimage.correlate(array, kernel)
        assert_array_almost_equal(array, output)
        output = ndimage.convolve(array, kernel)
        assert_array_almost_equal(array, output)

    def test_correlate11(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[1, 2, 3],
                                [4, 5, 6]])
        kernel = numpy.array([[1, 1],
                                 [1, 1]])
        output = ndimage.correlate(array, kernel)
<<<<<<< HEAD
        self.failUnless(diff([[4, 6, 10], [10, 12, 16]], output) < eps)
        output = ndimage.convolve(array, kernel)
        self.failUnless(diff([[12, 16, 18], [18, 22, 24]], output) < eps)

    def test_correlate12(self):
        "correlation 12"
=======
        assert_array_almost_equal([[4, 6, 10], [10, 12, 16]], output)
        output = ndimage.convolve(array, kernel)
        assert_array_almost_equal([[12, 16, 18], [18, 22, 24]], output)

    def test_correlate12(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[1, 2, 3],
                                [4, 5, 6]])
        kernel = numpy.array([[1, 0],
                                 [0, 1]])
        output = ndimage.correlate(array, kernel)
<<<<<<< HEAD
        self.failUnless(diff([[2, 3, 5], [5, 6, 8]], output) < eps)
        output = ndimage.convolve(array, kernel)
        self.failUnless(diff([[6, 8, 9], [9, 11, 12]], output) < eps)

    def test_correlate13(self):
        "correlation 13"
        kernel = numpy.array([[1, 0],
                                 [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [4, 5, 6]], type1)
            for type2 in self.types:
                output = ndimage.correlate(array, kernel,
                                                    output = type2)
                error = diff([[2, 3, 5], [5, 6, 8]], output)
                self.failUnless(error < eps and output.dtype.type == type2)
                output = ndimage.convolve(array, kernel,
                                                   output = type2)
                error = diff([[6, 8, 9], [9, 11, 12]], output)
                self.failUnless(error < eps and output.dtype.type == type2)

    def test_correlate14(self):
        "correlation 14"
        kernel = numpy.array([[1, 0],
                                 [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [4, 5, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros(array.shape, type2)
                ndimage.correlate(array, kernel,
                                                        output = output)
                error = diff([[2, 3, 5], [5, 6, 8]], output)
                self.failUnless(error < eps and output.dtype.type == type2)
                ndimage.convolve(array, kernel, output = output)
                error = diff([[6, 8, 9], [9, 11, 12]], output)
                self.failUnless(error < eps and output.dtype.type == type2)

    def test_correlate15(self):
        "correlation 15"
        kernel = numpy.array([[1, 0],
                                 [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [4, 5, 6]], type1)
            output = ndimage.correlate(array, kernel,
                                                output = numpy.float32)
            error = diff([[2, 3, 5], [5, 6, 8]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)
            output = ndimage.convolve(array, kernel,
                                               output = numpy.float32)
            error = diff([[6, 8, 9], [9, 11, 12]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)

    def test_correlate16(self):
        "correlation 16"
        kernel = numpy.array([[0.5, 0  ],
                                 [0,   0.5]])
=======
        assert_array_almost_equal([[2, 3, 5], [5, 6, 8]], output)
        output = ndimage.convolve(array, kernel)
        assert_array_almost_equal([[6, 8, 9], [9, 11, 12]], output)

    def test_correlate13(self):
        kernel = numpy.array([[1, 0],
                              [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                 [4, 5, 6]], type1)
            for type2 in self.types:
                output = ndimage.correlate(array, kernel,
                                                    output=type2)
                assert_array_almost_equal([[2, 3, 5], [5, 6, 8]], output)
                assert_equal(output.dtype.type, type2)

                output = ndimage.convolve(array, kernel,
                                          output=type2)
                assert_array_almost_equal([[6, 8, 9], [9, 11, 12]], output)
                assert_equal(output.dtype.type, type2)

    def test_correlate14(self):
        kernel = numpy.array([[1, 0],
                              [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                 [4, 5, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros(array.shape, type2)
                ndimage.correlate(array, kernel,
                                  output=output)
                assert_array_almost_equal([[2, 3, 5], [5, 6, 8]], output)
                assert_equal(output.dtype.type, type2)

                ndimage.convolve(array, kernel, output=output)
                assert_array_almost_equal([[6, 8, 9], [9, 11, 12]], output)
                assert_equal(output.dtype.type, type2)

    def test_correlate15(self):
        kernel = numpy.array([[1, 0],
                              [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                 [4, 5, 6]], type1)
            output = ndimage.correlate(array, kernel,
                                       output=numpy.float32)
            assert_array_almost_equal([[2, 3, 5], [5, 6, 8]], output)
            assert_equal(output.dtype.type, numpy.float32)

            output = ndimage.convolve(array, kernel,
                                      output=numpy.float32)
            assert_array_almost_equal([[6, 8, 9], [9, 11, 12]], output)
            assert_equal(output.dtype.type, numpy.float32)

    def test_correlate16(self):
        kernel = numpy.array([[0.5, 0],
                                 [0, 0.5]])
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [4, 5, 6]], type1)
            output = ndimage.correlate(array, kernel,
<<<<<<< HEAD
                                                output = numpy.float32)
            error = diff([[1, 1.5, 2.5], [2.5, 3, 4]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)
            output = ndimage.convolve(array, kernel,
                                               output = numpy.float32)
            error = diff([[3, 4, 4.5], [4.5, 5.5, 6]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)

    def test_correlate17(self):
        "correlation 17"
=======
                                                output=numpy.float32)
            assert_array_almost_equal([[1, 1.5, 2.5], [2.5, 3, 4]], output)
            assert_equal(output.dtype.type, numpy.float32)

            output = ndimage.convolve(array, kernel,
                                      output=numpy.float32)
            assert_array_almost_equal([[3, 4, 4.5], [4.5, 5.5, 6]], output)
            assert_equal(output.dtype.type, numpy.float32)

    def test_correlate17(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([1, 2, 3])
        tcor = [3, 5, 6]
        tcov = [2, 3, 5]
        kernel = numpy.array([1, 1])
<<<<<<< HEAD
        output = ndimage.correlate(array, kernel, origin = -1)
        self.failUnless(diff(tcor, output) < eps)
        output = ndimage.convolve(array, kernel, origin = -1)
        self.failUnless(diff(tcov, output) < eps)
        output = ndimage.correlate1d(array, kernel, origin = -1)
        self.failUnless(diff(tcor, output) < eps)
        output = ndimage.convolve1d(array, kernel, origin = -1)
        self.failUnless(diff(tcov, output) < eps)

    def test_correlate18(self):
        "correlation 18"
=======
        output = ndimage.correlate(array, kernel, origin=-1)
        assert_array_almost_equal(tcor, output)
        output = ndimage.convolve(array, kernel, origin=-1)
        assert_array_almost_equal(tcov, output)
        output = ndimage.correlate1d(array, kernel, origin=-1)
        assert_array_almost_equal(tcor, output)
        output = ndimage.convolve1d(array, kernel, origin=-1)
        assert_array_almost_equal(tcov, output)

    def test_correlate18(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        kernel = numpy.array([[1, 0],
                                 [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [4, 5, 6]], type1)
            output = ndimage.correlate(array, kernel,
<<<<<<< HEAD
                                        output = numpy.float32,
                                        mode = 'nearest', origin = -1)
            error = diff([[6, 8, 9], [9, 11, 12]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)
            output = ndimage.convolve(array, kernel,
                output = numpy.float32, mode = 'nearest', origin = -1)
            error = diff([[2, 3, 5], [5, 6, 8]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)

    def test_correlate19(self):
        "correlation 19"
        kernel = numpy.array([[1, 0],
                                 [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [4, 5, 6]], type1)
            output = ndimage.correlate(array, kernel,
                                    output = numpy.float32,
                                    mode = 'nearest', origin = [-1, 0])
            error = diff([[5, 6, 8], [8, 9, 11]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)
            output = ndimage.convolve(array, kernel,
                                    output = numpy.float32,
                                    mode = 'nearest', origin = [-1, 0])
            error = diff([[3, 5, 6], [6, 8, 9]], output)
            self.failUnless(error < eps and
                            output.dtype.type == numpy.float32)

    def test_correlate20(self):
        "correlation 20"
        weights = numpy.array([1, 2, 1])
        true = [[5, 10, 15], [7, 14, 21]]
=======
                                        output=numpy.float32,
                                        mode='nearest', origin=-1)
            assert_array_almost_equal([[6, 8, 9], [9, 11, 12]], output)
            assert_equal(output.dtype.type, numpy.float32)

            output = ndimage.convolve(array, kernel,
                                      output=numpy.float32,
                                      mode='nearest', origin=-1)
            assert_array_almost_equal([[2, 3, 5], [5, 6, 8]], output)
            assert_equal(output.dtype.type, numpy.float32)

    def test_correlate19(self):
        kernel = numpy.array([[1, 0],
                              [0, 1]])
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                 [4, 5, 6]], type1)
            output = ndimage.correlate(array, kernel,
                                       output=numpy.float32,
                                       mode='nearest', origin=[-1, 0])
            assert_array_almost_equal([[5, 6, 8], [8, 9, 11]], output)
            assert_equal(output.dtype.type, numpy.float32)

            output = ndimage.convolve(array, kernel,
                                      output=numpy.float32,
                                      mode='nearest', origin=[-1, 0])
            assert_array_almost_equal([[3, 5, 6], [6, 8, 9]], output)
            assert_equal(output.dtype.type, numpy.float32)

    def test_correlate20(self):
        weights = numpy.array([1, 2, 1])
        expected = [[5, 10, 15], [7, 14, 21]]
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [2, 4, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros((2, 3), type2)
<<<<<<< HEAD
                ndimage.correlate1d(array, weights, axis = 0,
                                              output = output)
                self.failUnless(diff(output, true) < eps)
                ndimage.convolve1d(array, weights, axis = 0,
                                              output = output)
                self.failUnless(diff(output, true) < eps)

    def test_correlate21(self):
        "correlation 21"
        array = numpy.array([[1, 2, 3],
                                [2, 4, 6]])
        true = [[5, 10, 15], [7, 14, 21]]
        weights = numpy.array([1, 2, 1])
        output = ndimage.correlate1d(array, weights, axis = 0)
        self.failUnless(diff(output, true) < eps)
        output = ndimage.convolve1d(array, weights, axis = 0)
        self.failUnless(diff(output, true) < eps)

    def test_correlate22(self):
        "correlation 22"
        weights = numpy.array([1, 2, 1])
        true = [[6, 12, 18], [6, 12, 18]]
=======
                ndimage.correlate1d(array, weights, axis=0,
                                    output=output)
                assert_array_almost_equal(output, expected)
                ndimage.convolve1d(array, weights, axis=0,
                                   output=output)
                assert_array_almost_equal(output, expected)

    def test_correlate21(self):
        array = numpy.array([[1, 2, 3],
                                [2, 4, 6]])
        expected = [[5, 10, 15], [7, 14, 21]]
        weights = numpy.array([1, 2, 1])
        output = ndimage.correlate1d(array, weights, axis=0)
        assert_array_almost_equal(output, expected)
        output = ndimage.convolve1d(array, weights, axis=0)
        assert_array_almost_equal(output, expected)

    def test_correlate22(self):
        weights = numpy.array([1, 2, 1])
        expected = [[6, 12, 18], [6, 12, 18]]
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [2, 4, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros((2, 3), type2)
<<<<<<< HEAD
                ndimage.correlate1d(array, weights, axis = 0,
                                            mode = 'wrap', output = output)
                self.failUnless(diff(output, true) < eps)
                ndimage.convolve1d(array, weights, axis = 0,
                                            mode = 'wrap', output = output)
                self.failUnless(diff(output, true) < eps)

    def test_correlate23(self):
        "correlation 23"
        weights = numpy.array([1, 2, 1])
        true = [[5, 10, 15], [7, 14, 21]]
=======
                ndimage.correlate1d(array, weights, axis=0,
                                            mode='wrap', output=output)
                assert_array_almost_equal(output, expected)
                ndimage.convolve1d(array, weights, axis=0,
                                            mode='wrap', output=output)
                assert_array_almost_equal(output, expected)

    def test_correlate23(self):
        weights = numpy.array([1, 2, 1])
        expected = [[5, 10, 15], [7, 14, 21]]
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [2, 4, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros((2, 3), type2)
<<<<<<< HEAD
                ndimage.correlate1d(array, weights, axis = 0,
                                         mode = 'nearest', output = output)
                self.failUnless(diff(output, true) < eps)
                ndimage.convolve1d(array, weights, axis = 0,
                                         mode = 'nearest', output = output)
                self.failUnless(diff(output, true) < eps)

    def test_correlate24(self):
        "correlation 24"
=======
                ndimage.correlate1d(array, weights, axis=0,
                                         mode='nearest', output=output)
                assert_array_almost_equal(output, expected)
                ndimage.convolve1d(array, weights, axis=0,
                                         mode='nearest', output=output)
                assert_array_almost_equal(output, expected)

    def test_correlate24(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        weights = numpy.array([1, 2, 1])
        tcor = [[7, 14, 21], [8, 16, 24]]
        tcov = [[4, 8, 12], [5, 10, 15]]
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
                                    [2, 4, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros((2, 3), type2)
<<<<<<< HEAD
                ndimage.correlate1d(array, weights, axis = 0,
                           mode = 'nearest', output = output, origin = -1)
                self.failUnless(diff(output, tcor) < eps)
                ndimage.convolve1d(array, weights, axis = 0,
                           mode = 'nearest', output = output, origin = -1)
                self.failUnless(diff(output, tcov) < eps)

    def test_correlate25(self):
        "correlation 25"
=======
                ndimage.correlate1d(array, weights, axis=0,
                           mode='nearest', output=output, origin=-1)
                assert_array_almost_equal(output, tcor)
                ndimage.convolve1d(array, weights, axis=0,
                           mode='nearest', output=output, origin=-1)
                assert_array_almost_equal(output, tcov)

    def test_correlate25(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        weights = numpy.array([1, 2, 1])
        tcor = [[4, 8, 12], [5, 10, 15]]
        tcov = [[7, 14, 21], [8, 16, 24]]
        for type1 in self.types:
            array = numpy.array([[1, 2, 3],
<<<<<<< HEAD
                                    [2, 4, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros((2, 3), type2)
                ndimage.correlate1d(array, weights, axis = 0,
                             mode = 'nearest', output = output, origin = 1)
                self.failUnless(diff(output, tcor) < eps)
                ndimage.convolve1d(array, weights, axis = 0,
                             mode = 'nearest', output = output, origin = 1)
                self.failUnless(diff(output, tcov) < eps)

    def test_gauss01(self):
        "gaussian filter 1"
        input = numpy.array([[1, 2, 3],
                                [2, 4, 6]], numpy.float32)
        output = ndimage.gaussian_filter(input, 0)
        self.failUnless(diff(output, input) < eps)

    def test_gauss02(self):
        "gaussian filter 2"
        input = numpy.array([[1, 2, 3],
                                [2, 4, 6]], numpy.float32)
        output = ndimage.gaussian_filter(input, 1.0)
        self.failUnless(input.dtype == output.dtype and
                        input.shape == output.shape)

    def test_gauss03(self):
        "gaussian filter 3"
=======
                                 [2, 4, 6]], type1)
            for type2 in self.types:
                output = numpy.zeros((2, 3), type2)
                ndimage.correlate1d(array, weights, axis=0,
                             mode='nearest', output=output, origin=1)
                assert_array_almost_equal(output, tcor)
                ndimage.convolve1d(array, weights, axis=0,
                             mode='nearest', output=output, origin=1)
                assert_array_almost_equal(output, tcov)

    def test_gauss01(self):
        input = numpy.array([[1, 2, 3],
                             [2, 4, 6]], numpy.float32)
        output = ndimage.gaussian_filter(input, 0)
        assert_array_almost_equal(output, input)

    def test_gauss02(self):
        input = numpy.array([[1, 2, 3],
                             [2, 4, 6]], numpy.float32)
        output = ndimage.gaussian_filter(input, 1.0)
        assert_equal(input.dtype, output.dtype)
        assert_equal(input.shape, output.shape)

    def test_gauss03(self):
        # single precision data"
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        input = numpy.arange(100 * 100).astype(numpy.float32)
        input.shape = (100, 100)
        output = ndimage.gaussian_filter(input, [1.0, 1.0])

<<<<<<< HEAD
        self.failUnless(input.dtype == output.dtype and
                        input.shape == output.shape and
                        output.sum(dtype='d') - input.sum(dtype='d') < eps and
                        diff(input, output) > 1.0)

    def test_gauss04(self):
        "gaussian filter 4"
=======
        assert_equal(input.dtype, output.dtype)
        assert_equal(input.shape, output.shape)

        # input.sum() is 49995000.0.  With single precision floats, we can't
        # expect more than 8 digits of accuracy, so use decimal=0 in this test.
        assert_almost_equal(output.sum(dtype='d'), input.sum(dtype='d'), decimal=0)
        assert_(sumsq(input, output) > 1.0)

    def test_gauss04(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        input = numpy.arange(100 * 100).astype(numpy.float32)
        input.shape = (100, 100)
        otype = numpy.float64
        output = ndimage.gaussian_filter(input, [1.0, 1.0],
<<<<<<< HEAD
                                                            output = otype)
        self.failUnless(output.dtype.type == numpy.float64 and
                        input.shape == output.shape and
                        diff(input, output) > 1.0)

    def test_gauss05(self):
        "gaussian filter 5"
=======
                                                            output=otype)
        assert_equal(output.dtype.type, numpy.float64)
        assert_equal(input.shape, output.shape)
        assert_(sumsq(input, output) > 1.0)

    def test_gauss05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        input = numpy.arange(100 * 100).astype(numpy.float32)
        input.shape = (100, 100)
        otype = numpy.float64
        output = ndimage.gaussian_filter(input, [1.0, 1.0],
<<<<<<< HEAD
                                                 order = 1, output = otype)
        self.failUnless(output.dtype.type == numpy.float64 and
                        input.shape == output.shape and
                        diff(input, output) > 1.0)

    def test_gauss06(self):
        "gaussian filter 6"
=======
                                                 order=1, output=otype)
        assert_equal(output.dtype.type, numpy.float64)
        assert_equal(input.shape, output.shape)
        assert_(sumsq(input, output) > 1.0)

    def test_gauss06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        input = numpy.arange(100 * 100).astype(numpy.float32)
        input.shape = (100, 100)
        otype = numpy.float64
        output1 = ndimage.gaussian_filter(input, [1.0, 1.0],
<<<<<<< HEAD
                                                            output = otype)
        output2 = ndimage.gaussian_filter(input, 1.0,
                                                            output = otype)
        self.failUnless(diff(output1, output2) < eps)

    def test_prewitt01(self):
        "prewitt filter 1"
=======
                                                            output=otype)
        output2 = ndimage.gaussian_filter(input, 1.0,
                                                            output=otype)
        assert_array_almost_equal(output1, output2)

    def test_prewitt01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.correlate1d(array, [-1.0, 0.0, 1.0], 0)
            t = ndimage.correlate1d(t, [1.0, 1.0, 1.0], 1)
            output = ndimage.prewitt(array, 0)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)


    def test_prewitt02(self):
        "prewitt filter 2"
=======
            assert_array_almost_equal(t, output)

    def test_prewitt02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.correlate1d(array, [-1.0, 0.0, 1.0], 0)
            t = ndimage.correlate1d(t, [1.0, 1.0, 1.0], 1)
            output = numpy.zeros(array.shape, type)
            ndimage.prewitt(array, 0, output)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)

    def test_prewitt03(self):
        "prewitt filter 3"
=======
            assert_array_almost_equal(t, output)

    def test_prewitt03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.correlate1d(array, [-1.0, 0.0, 1.0], 1)
            t = ndimage.correlate1d(t, [1.0, 1.0, 1.0], 0)
            output = ndimage.prewitt(array, 1)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)

    def test_prewitt04(self):
        "prewitt filter 4"
=======
            assert_array_almost_equal(t, output)

    def test_prewitt04(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.prewitt(array, -1)
            output = ndimage.prewitt(array, 1)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)

    def test_sobel01(self):
        "sobel filter 1"
=======
            assert_array_almost_equal(t, output)

    def test_sobel01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.correlate1d(array, [-1.0, 0.0, 1.0], 0)
            t = ndimage.correlate1d(t, [1.0, 2.0, 1.0], 1)
            output = ndimage.sobel(array, 0)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)

    def test_sobel02(self):
        "sobel filter 2"
=======
            assert_array_almost_equal(t, output)

    def test_sobel02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.correlate1d(array, [-1.0, 0.0, 1.0], 0)
            t = ndimage.correlate1d(t, [1.0, 2.0, 1.0], 1)
            output = numpy.zeros(array.shape, type)
            ndimage.sobel(array, 0, output)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)

    def test_sobel03(self):
        "sobel filter 3"
=======
            assert_array_almost_equal(t, output)

    def test_sobel03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.correlate1d(array, [-1.0, 0.0, 1.0], 1)
            t = ndimage.correlate1d(t, [1.0, 2.0, 1.0], 0)
            output = numpy.zeros(array.shape, type)
            output = ndimage.sobel(array, 1)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)

    def test_sobel04(self):
        "sobel filter 4"
=======
            assert_array_almost_equal(t, output)

    def test_sobel04(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            t = ndimage.sobel(array, -1)
            output = ndimage.sobel(array, 1)
<<<<<<< HEAD
            self.failUnless(diff(t, output) < eps)

    def test_laplace01(self):
        "laplace filter 1"
=======
            assert_array_almost_equal(t, output)

    def test_laplace01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in [numpy.int32, numpy.float32, numpy.float64]:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type) * 100
            tmp1 = ndimage.correlate1d(array, [1, -2, 1], 0)
            tmp2 = ndimage.correlate1d(array, [1, -2, 1], 1)
            output = ndimage.laplace(array)
<<<<<<< HEAD
            self.failUnless(diff(tmp1 + tmp2, output) < eps)

    def test_laplace02(self):
        "laplace filter 2"
=======
            assert_array_almost_equal(tmp1 + tmp2, output)

    def test_laplace02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in [numpy.int32, numpy.float32, numpy.float64]:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type) * 100
            tmp1 = ndimage.correlate1d(array, [1, -2, 1], 0)
            tmp2 = ndimage.correlate1d(array, [1, -2, 1], 1)
            output = numpy.zeros(array.shape, type)
<<<<<<< HEAD
            ndimage.laplace(array, output = output)
            self.failUnless(diff(tmp1 + tmp2, output) < eps)

    def test_gaussian_laplace01(self):
        "gaussian laplace filter 1"
=======
            ndimage.laplace(array, output=output)
            assert_array_almost_equal(tmp1 + tmp2, output)

    def test_gaussian_laplace01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in [numpy.int32, numpy.float32, numpy.float64]:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type) * 100
            tmp1 = ndimage.gaussian_filter(array, 1.0, [2, 0])
            tmp2 = ndimage.gaussian_filter(array, 1.0, [0, 2])
            output = ndimage.gaussian_laplace(array, 1.0)
<<<<<<< HEAD
            self.failUnless(diff(tmp1 + tmp2, output) < eps)

    def test_gaussian_laplace02(self):
        "gaussian laplace filter 2"
=======
            assert_array_almost_equal(tmp1 + tmp2, output)

    def test_gaussian_laplace02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in [numpy.int32, numpy.float32, numpy.float64]:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type) * 100
            tmp1 = ndimage.gaussian_filter(array, 1.0, [2, 0])
            tmp2 = ndimage.gaussian_filter(array, 1.0, [0, 2])
            output = numpy.zeros(array.shape, type)
            ndimage.gaussian_laplace(array, 1.0, output)
<<<<<<< HEAD
            self.failUnless(diff(tmp1 + tmp2, output) < eps)

    def test_generic_laplace01(self):
        "generic laplace filter 1"
=======
            assert_array_almost_equal(tmp1 + tmp2, output)

    def test_generic_laplace01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def derivative2(input, axis, output, mode, cval, a, b):
            sigma = [a, b / 2.0]
            input = numpy.asarray(input)
            order = [0] * input.ndim
            order[axis] = 2
            return ndimage.gaussian_filter(input, sigma, order,
                                           output, mode, cval)
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            output = numpy.zeros(array.shape, type)
            tmp = ndimage.generic_laplace(array, derivative2,
<<<<<<< HEAD
                    extra_arguments = (1.0,), extra_keywords = {'b': 2.0})
            ndimage.gaussian_laplace(array, 1.0, output)
            self.failUnless(diff(tmp, output) < eps)

    def test_gaussian_gradient_magnitude01(self):
        "gaussian gradient magnitude filter 1"
=======
                    extra_arguments=(1.0,), extra_keywords={'b': 2.0})
            ndimage.gaussian_laplace(array, 1.0, output)
            assert_array_almost_equal(tmp, output)

    def test_gaussian_gradient_magnitude01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in [numpy.int32, numpy.float32, numpy.float64]:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type) * 100
            tmp1 = ndimage.gaussian_filter(array, 1.0, [1, 0])
            tmp2 = ndimage.gaussian_filter(array, 1.0, [0, 1])
            output = ndimage.gaussian_gradient_magnitude(array,
                                                                       1.0)
<<<<<<< HEAD
            true = tmp1 * tmp1 + tmp2 * tmp2
            numpy.sqrt(true, true)
            self.failUnless(diff(true, output) < eps)

    def test_gaussian_gradient_magnitude02(self):
        "gaussian gradient magnitude filter 2"
=======
            expected = tmp1 * tmp1 + tmp2 * tmp2
            expected = numpy.sqrt(expected).astype(type)
            assert_array_almost_equal(expected, output)

    def test_gaussian_gradient_magnitude02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in [numpy.int32, numpy.float32, numpy.float64]:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type) * 100
            tmp1 = ndimage.gaussian_filter(array, 1.0, [1, 0])
            tmp2 = ndimage.gaussian_filter(array, 1.0, [0, 1])
            output = numpy.zeros(array.shape, type)
            ndimage.gaussian_gradient_magnitude(array, 1.0,
                                                           output)
<<<<<<< HEAD
            true = tmp1 * tmp1 + tmp2 * tmp2
            numpy.sqrt(true, true)
            self.failUnless(diff(true, output) < eps)

    def test_generic_gradient_magnitude01(self):
        "generic gradient magnitude 1"
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]], numpy.float64)
=======
            expected = tmp1 * tmp1 + tmp2 * tmp2
            expected = numpy.sqrt(expected).astype(type)
            assert_array_almost_equal(expected, output)

    def test_generic_gradient_magnitude01(self):
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]], numpy.float64)

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def derivative(input, axis, output, mode, cval, a, b):
            sigma = [a, b / 2.0]
            input = numpy.asarray(input)
            order = [0] * input.ndim
            order[axis] = 1
            return ndimage.gaussian_filter(input, sigma, order,
                                        output, mode, cval)
        tmp1 = ndimage.gaussian_gradient_magnitude(array, 1.0)
        tmp2 = ndimage.generic_gradient_magnitude(array,
<<<<<<< HEAD
                derivative, extra_arguments = (1.0,),
                extra_keywords = {'b': 2.0})
        self.failUnless(diff(tmp1, tmp2) < eps)

    def test_uniform01(self):
        "uniform filter 1"
        array = numpy.array([2, 4, 6])
        size = 2
        output = ndimage.uniform_filter1d(array, size,
                                                   origin = -1)
        self.failUnless(diff([3, 5, 6], output) < eps)

    def test_uniform02(self):
        "uniform filter 2"
        array = numpy.array([1, 2, 3])
        filter_shape = [0]
        output = ndimage.uniform_filter(array, filter_shape)
        self.failUnless(diff(array, output) < eps)

    def test_uniform03(self):
        "uniform filter 3"
        array = numpy.array([1, 2, 3])
        filter_shape = [1]
        output = ndimage.uniform_filter(array, filter_shape)
        self.failUnless(diff(array, output) < eps)

    def test_uniform04(self):
        "uniform filter 4"
        array = numpy.array([2, 4, 6])
        filter_shape = [2]
        output = ndimage.uniform_filter(array, filter_shape)
        self.failUnless(diff([2, 3, 5], output) < eps)

    def test_uniform05(self):
        "uniform filter 5"
        array = []
        filter_shape = [1]
        output = ndimage.uniform_filter(array, filter_shape)
        self.failUnless(diff([], output) < eps)

    def test_uniform06(self):
        "uniform filter 6"
=======
                derivative, extra_arguments=(1.0,),
                extra_keywords={'b': 2.0})
        assert_array_almost_equal(tmp1, tmp2)

    def test_uniform01(self):
        array = numpy.array([2, 4, 6])
        size = 2
        output = ndimage.uniform_filter1d(array, size,
                                                   origin=-1)
        assert_array_almost_equal([3, 5, 6], output)

    def test_uniform02(self):
        array = numpy.array([1, 2, 3])
        filter_shape = [0]
        output = ndimage.uniform_filter(array, filter_shape)
        assert_array_almost_equal(array, output)

    def test_uniform03(self):
        array = numpy.array([1, 2, 3])
        filter_shape = [1]
        output = ndimage.uniform_filter(array, filter_shape)
        assert_array_almost_equal(array, output)

    def test_uniform04(self):
        array = numpy.array([2, 4, 6])
        filter_shape = [2]
        output = ndimage.uniform_filter(array, filter_shape)
        assert_array_almost_equal([2, 3, 5], output)

    def test_uniform05(self):
        array = []
        filter_shape = [1]
        output = ndimage.uniform_filter(array, filter_shape)
        assert_array_almost_equal([], output)

    def test_uniform06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        filter_shape = [2, 2]
        for type1 in self.types:
            array = numpy.array([[4, 8, 12],
                                    [16, 20, 24]], type1)
            for type2 in self.types:
                output = ndimage.uniform_filter(array,
<<<<<<< HEAD
                                        filter_shape, output = type2)
                error = diff([[4, 6, 10], [10, 12, 16]], output)
                self.failUnless(error < eps and output.dtype.type == type2)

    def test_minimum_filter01(self):
        "minimum filter 1"
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([2])
        output = ndimage.minimum_filter(array, filter_shape)
        self.failUnless(diff([1, 1, 2, 3, 4], output) < eps)

    def test_minimum_filter02(self):
        "minimum filter 2"
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([3])
        output = ndimage.minimum_filter(array, filter_shape)
        self.failUnless(diff([1, 1, 2, 3, 4], output) < eps)

    def test_minimum_filter03(self):
        "minimum filter 3"
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([2])
        output = ndimage.minimum_filter(array, filter_shape)
        self.failUnless(diff([3, 2, 2, 1, 1], output) < eps)

    def test_minimum_filter04(self):
        "minimum filter 4"
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([3])
        output = ndimage.minimum_filter(array, filter_shape)
        self.failUnless(diff([2, 2, 1, 1, 1], output) < eps)

    def test_minimum_filter05(self):
        "minimum filter 5"
=======
                                        filter_shape, output=type2)
                assert_array_almost_equal([[4, 6, 10], [10, 12, 16]], output)
                assert_equal(output.dtype.type, type2)

    def test_minimum_filter01(self):
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([2])
        output = ndimage.minimum_filter(array, filter_shape)
        assert_array_almost_equal([1, 1, 2, 3, 4], output)

    def test_minimum_filter02(self):
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([3])
        output = ndimage.minimum_filter(array, filter_shape)
        assert_array_almost_equal([1, 1, 2, 3, 4], output)

    def test_minimum_filter03(self):
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([2])
        output = ndimage.minimum_filter(array, filter_shape)
        assert_array_almost_equal([3, 2, 2, 1, 1], output)

    def test_minimum_filter04(self):
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([3])
        output = ndimage.minimum_filter(array, filter_shape)
        assert_array_almost_equal([2, 2, 1, 1, 1], output)

    def test_minimum_filter05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        filter_shape = numpy.array([2, 3])
        output = ndimage.minimum_filter(array, filter_shape)
<<<<<<< HEAD
        self.failUnless(diff([[2, 2, 1, 1, 1],
                              [2, 2, 1, 1, 1],
                              [5, 3, 3, 1, 1]], output) < eps)

    def test_minimum_filter06(self):
        "minimum filter 6"
=======
        assert_array_almost_equal([[2, 2, 1, 1, 1],
                              [2, 2, 1, 1, 1],
                              [5, 3, 3, 1, 1]], output)

    def test_minimum_filter06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 1, 1], [1, 1, 1]]
        output = ndimage.minimum_filter(array,
<<<<<<< HEAD
                                                 footprint = footprint)
        self.failUnless(diff([[2, 2, 1, 1, 1],
                              [2, 2, 1, 1, 1],
                              [5, 3, 3, 1, 1]], output) < eps)

    def test_minimum_filter07(self):
        "minimum filter 7"
=======
                                                 footprint=footprint)
        assert_array_almost_equal([[2, 2, 1, 1, 1],
                              [2, 2, 1, 1, 1],
                              [5, 3, 3, 1, 1]], output)

    def test_minimum_filter07(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.minimum_filter(array,
<<<<<<< HEAD
                                                 footprint = footprint)
        self.failUnless(diff([[2, 2, 1, 1, 1],
                              [2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1]], output) < eps)

    def test_minimum_filter08(self):
        "minimum filter 8"
=======
                                                 footprint=footprint)
        assert_array_almost_equal([[2, 2, 1, 1, 1],
                              [2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1]], output)

    def test_minimum_filter08(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.minimum_filter(array,
<<<<<<< HEAD
                                       footprint = footprint, origin = -1)
        self.failUnless(diff([[3, 1, 3, 1, 1],
                              [5, 3, 3, 1, 1],
                              [3, 3, 1, 1, 1]], output) < eps)

    def test_minimum_filter09(self):
        "minimum filter 9"
=======
                                       footprint=footprint, origin=-1)
        assert_array_almost_equal([[3, 1, 3, 1, 1],
                              [5, 3, 3, 1, 1],
                              [3, 3, 1, 1, 1]], output)

    def test_minimum_filter09(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.minimum_filter(array,
<<<<<<< HEAD
                                  footprint = footprint, origin = [-1, 0])
        self.failUnless(diff([[2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1],
                              [5, 3, 3, 1, 1]], output) < eps)

    def test_maximum_filter01(self):
        "maximum filter 1"
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([2])
        output = ndimage.maximum_filter(array, filter_shape)
        self.failUnless(diff([1, 2, 3, 4, 5], output) < eps)

    def test_maximum_filter02(self):
        "maximum filter 2"
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([3])
        output = ndimage.maximum_filter(array, filter_shape)
        self.failUnless(diff([2, 3, 4, 5, 5], output) < eps)

    def test_maximum_filter03(self):
        "maximum filter 3"
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([2])
        output = ndimage.maximum_filter(array, filter_shape)
        self.failUnless(diff([3, 3, 5, 5, 4], output) < eps)

    def test_maximum_filter04(self):
        "maximum filter 4"
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([3])
        output = ndimage.maximum_filter(array, filter_shape)
        self.failUnless(diff([3, 5, 5, 5, 4], output) < eps)

    def test_maximum_filter05(self):
        "maximum filter 5"
=======
                                  footprint=footprint, origin=[-1, 0])
        assert_array_almost_equal([[2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1],
                              [5, 3, 3, 1, 1]], output)

    def test_maximum_filter01(self):
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([2])
        output = ndimage.maximum_filter(array, filter_shape)
        assert_array_almost_equal([1, 2, 3, 4, 5], output)

    def test_maximum_filter02(self):
        array = numpy.array([1, 2, 3, 4, 5])
        filter_shape = numpy.array([3])
        output = ndimage.maximum_filter(array, filter_shape)
        assert_array_almost_equal([2, 3, 4, 5, 5], output)

    def test_maximum_filter03(self):
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([2])
        output = ndimage.maximum_filter(array, filter_shape)
        assert_array_almost_equal([3, 3, 5, 5, 4], output)

    def test_maximum_filter04(self):
        array = numpy.array([3, 2, 5, 1, 4])
        filter_shape = numpy.array([3])
        output = ndimage.maximum_filter(array, filter_shape)
        assert_array_almost_equal([3, 5, 5, 5, 4], output)

    def test_maximum_filter05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        filter_shape = numpy.array([2, 3])
        output = ndimage.maximum_filter(array, filter_shape)
<<<<<<< HEAD
        self.failUnless(diff([[3, 5, 5, 5, 4],
                              [7, 9, 9, 9, 5],
                              [8, 9, 9, 9, 7]], output) < eps)

    def test_maximum_filter06(self):
        "maximum filter 6"
=======
        assert_array_almost_equal([[3, 5, 5, 5, 4],
                              [7, 9, 9, 9, 5],
                              [8, 9, 9, 9, 7]], output)

    def test_maximum_filter06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 1, 1], [1, 1, 1]]
        output = ndimage.maximum_filter(array,
<<<<<<< HEAD
                                                 footprint = footprint)
        self.failUnless(diff([[3, 5, 5, 5, 4],
                              [7, 9, 9, 9, 5],
                              [8, 9, 9, 9, 7]], output) < eps)

    def test_maximum_filter07(self):
        "maximum filter 7"
=======
                                                 footprint=footprint)
        assert_array_almost_equal([[3, 5, 5, 5, 4],
                              [7, 9, 9, 9, 5],
                              [8, 9, 9, 9, 7]], output)

    def test_maximum_filter07(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.maximum_filter(array,
<<<<<<< HEAD
                                                 footprint = footprint)
        self.failUnless(diff([[3, 5, 5, 5, 4],
                              [7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7]], output) < eps)

    def test_maximum_filter08(self):
        "maximum filter 8"
=======
                                                 footprint=footprint)
        assert_array_almost_equal([[3, 5, 5, 5, 4],
                              [7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7]], output)

    def test_maximum_filter08(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.maximum_filter(array,
<<<<<<< HEAD
                                      footprint = footprint, origin = -1)
        self.failUnless(diff([[7, 9, 9, 5, 5],
                              [9, 8, 9, 7, 5],
                              [8, 8, 7, 7, 7]], output) < eps)

    def test_maximum_filter09(self):
        "maximum filter 9"
=======
                                      footprint=footprint, origin=-1)
        assert_array_almost_equal([[7, 9, 9, 5, 5],
                              [9, 8, 9, 7, 5],
                              [8, 8, 7, 7, 7]], output)

    def test_maximum_filter09(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.maximum_filter(array,
<<<<<<< HEAD
                                 footprint = footprint, origin = [-1, 0])
        self.failUnless(diff([[7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7],
                              [8, 8, 8, 7, 7]], output) < eps)

    def test_rank01(self):
        "rank filter 1"
        array = numpy.array([1, 2, 3, 4, 5])
        output = ndimage.rank_filter(array, 1, size = 2)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.percentile_filter(array, 100, size = 2)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.median_filter(array, 2)
        self.failUnless(diff(array, output) < eps)

    def test_rank02(self):
        "rank filter 2"
        array = numpy.array([1, 2, 3, 4, 5])
        output = ndimage.rank_filter(array, 1, size = [3])
        self.failUnless(diff(array, output) < eps)
        output = ndimage.percentile_filter(array, 50, size = 3)
        self.failUnless(diff(array, output) < eps)
        output = ndimage.median_filter(array, (3,))
        self.failUnless(diff(array, output) < eps)

    def test_rank03(self):
        "rank filter 3"
        array = numpy.array([3, 2, 5, 1, 4])
        output = ndimage.rank_filter(array, 1, size = [2])
        self.failUnless(diff([3, 3, 5, 5, 4], output) < eps)
        output = ndimage.percentile_filter(array, 100, size = 2)
        self.failUnless(diff([3, 3, 5, 5, 4], output) < eps)

    def test_rank04(self):
        "rank filter 4"
        array = numpy.array([3, 2, 5, 1, 4])
        true = [3, 3, 2, 4, 4]
        output = ndimage.rank_filter(array, 1, size = 3)
        self.failUnless(diff(true, output) < eps)
        output = ndimage.percentile_filter(array, 50, size = 3)
        self.failUnless(diff(true, output) < eps)
        output = ndimage.median_filter(array, size = 3)
        self.failUnless(diff(true, output) < eps)

    def test_rank05(self):
        "rank filter 5"
        array = numpy.array([3, 2, 5, 1, 4])
        true = [3, 3, 2, 4, 4]
        output = ndimage.rank_filter(array, -2, size = 3)
        self.failUnless(diff(true, output) < eps)

    def test_rank06(self):
        "rank filter 6"
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]])
        true = [[2, 2, 1, 1, 1],
                [3, 3, 2, 1, 1],
                [5, 5, 3, 3, 1]]
        output = ndimage.rank_filter(array, 1, size = [2, 3])
        self.failUnless(diff(true, output) < eps)
        output = ndimage.percentile_filter(array, 17,
                                                    size = (2, 3))
        self.failUnless(diff(true, output) < eps)

    def test_rank07(self):
        "rank filter 7"
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]])
        true = [[3, 5, 5, 5, 4],
                [5, 5, 7, 5, 4],
                [6, 8, 8, 7, 5]]
        output = ndimage.rank_filter(array, -2, size = [2, 3])
        self.failUnless(diff(true, output) < eps)

    def test_rank08(self):
        "median filter 8"
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]])
        true = [[3, 3, 2, 4, 4],
                [5, 5, 5, 4, 4],
                [5, 6, 7, 5, 5]]
        kernel = numpy.array([2, 3])
        output = ndimage.percentile_filter(array, 50.0,
                                                    size = (2, 3))
        self.failUnless(diff(true, output) < eps)
        output = ndimage.rank_filter(array, 3, size = (2, 3))
        self.failUnless(diff(true, output) < eps)
        output = ndimage.median_filter(array, size = (2, 3))
        self.failUnless(diff(true, output) < eps)

    def test_rank09(self):
        "rank filter 9"
        true = [[3, 3, 2, 4, 4],
=======
                                 footprint=footprint, origin=[-1, 0])
        assert_array_almost_equal([[7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7],
                              [8, 8, 8, 7, 7]], output)

    def test_rank01(self):
        array = numpy.array([1, 2, 3, 4, 5])
        output = ndimage.rank_filter(array, 1, size=2)
        assert_array_almost_equal(array, output)
        output = ndimage.percentile_filter(array, 100, size=2)
        assert_array_almost_equal(array, output)
        output = ndimage.median_filter(array, 2)
        assert_array_almost_equal(array, output)

    def test_rank02(self):
        array = numpy.array([1, 2, 3, 4, 5])
        output = ndimage.rank_filter(array, 1, size=[3])
        assert_array_almost_equal(array, output)
        output = ndimage.percentile_filter(array, 50, size=3)
        assert_array_almost_equal(array, output)
        output = ndimage.median_filter(array, (3,))
        assert_array_almost_equal(array, output)

    def test_rank03(self):
        array = numpy.array([3, 2, 5, 1, 4])
        output = ndimage.rank_filter(array, 1, size=[2])
        assert_array_almost_equal([3, 3, 5, 5, 4], output)
        output = ndimage.percentile_filter(array, 100, size=2)
        assert_array_almost_equal([3, 3, 5, 5, 4], output)

    def test_rank04(self):
        array = numpy.array([3, 2, 5, 1, 4])
        expected = [3, 3, 2, 4, 4]
        output = ndimage.rank_filter(array, 1, size=3)
        assert_array_almost_equal(expected, output)
        output = ndimage.percentile_filter(array, 50, size=3)
        assert_array_almost_equal(expected, output)
        output = ndimage.median_filter(array, size=3)
        assert_array_almost_equal(expected, output)

    def test_rank05(self):
        array = numpy.array([3, 2, 5, 1, 4])
        expected = [3, 3, 2, 4, 4]
        output = ndimage.rank_filter(array, -2, size=3)
        assert_array_almost_equal(expected, output)

    def test_rank06(self):
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]])
        expected = [[2, 2, 1, 1, 1],
                [3, 3, 2, 1, 1],
                [5, 5, 3, 3, 1]]
        output = ndimage.rank_filter(array, 1, size=[2, 3])
        assert_array_almost_equal(expected, output)
        output = ndimage.percentile_filter(array, 17,
                                                    size=(2, 3))
        assert_array_almost_equal(expected, output)

    def test_rank07(self):
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]])
        expected = [[3, 5, 5, 5, 4],
                [5, 5, 7, 5, 4],
                [6, 8, 8, 7, 5]]
        output = ndimage.rank_filter(array, -2, size=[2, 3])
        assert_array_almost_equal(expected, output)

    def test_rank08(self):
        array = numpy.array([[3, 2, 5, 1, 4],
                                [5, 8, 3, 7, 1],
                                [5, 6, 9, 3, 5]])
        expected = [[3, 3, 2, 4, 4],
                [5, 5, 5, 4, 4],
                [5, 6, 7, 5, 5]]
        output = ndimage.percentile_filter(array, 50.0,
                                                    size=(2, 3))
        assert_array_almost_equal(expected, output)
        output = ndimage.rank_filter(array, 3, size=(2, 3))
        assert_array_almost_equal(expected, output)
        output = ndimage.median_filter(array, size=(2, 3))
        assert_array_almost_equal(expected, output)

    def test_rank09(self):
        expected = [[3, 3, 2, 4, 4],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [3, 5, 2, 5, 1],
                [5, 5, 8, 3, 5]]
        footprint = [[1, 0, 1], [0, 1, 0]]
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            output = ndimage.rank_filter(array, 1,
<<<<<<< HEAD
                                                  footprint = footprint)
            self.failUnless(diff(true, output) < eps)
            output = ndimage.percentile_filter(array, 35,
                                                    footprint = footprint)
            self.failUnless(diff(true, output) < eps)

    def test_rank10(self):
        "rank filter 10"
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        true = [[2, 2, 1, 1, 1],
=======
                                                  footprint=footprint)
            assert_array_almost_equal(expected, output)
            output = ndimage.percentile_filter(array, 35,
                                                    footprint=footprint)
            assert_array_almost_equal(expected, output)

    def test_rank10(self):
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        expected = [[2, 2, 1, 1, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [2, 3, 1, 3, 1],
                [5, 5, 3, 3, 1]]
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.rank_filter(array, 0,
<<<<<<< HEAD
                                              footprint = footprint)
        self.failUnless(diff(true, output) < eps)
        output = ndimage.percentile_filter(array, 0.0,
                                                    footprint = footprint)
        self.failUnless(diff(true, output) < eps)

    def test_rank11(self):
        "rank filter 11"
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        true = [[3, 5, 5, 5, 4],
=======
                                              footprint=footprint)
        assert_array_almost_equal(expected, output)
        output = ndimage.percentile_filter(array, 0.0,
                                                    footprint=footprint)
        assert_array_almost_equal(expected, output)

    def test_rank11(self):
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        expected = [[3, 5, 5, 5, 4],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [7, 7, 9, 9, 5],
                [7, 9, 8, 9, 7]]
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.rank_filter(array, -1,
<<<<<<< HEAD
                                              footprint = footprint)
        self.failUnless(diff(true, output) < eps)
        output = ndimage.percentile_filter(array, 100.0,
                                                    footprint = footprint)
        self.failUnless(diff(true, output) < eps)


    def test_rank12(self):
        "rank filter 12"
        true = [[3, 3, 2, 4, 4],
=======
                                              footprint=footprint)
        assert_array_almost_equal(expected, output)
        output = ndimage.percentile_filter(array, 100.0,
                                                    footprint=footprint)
        assert_array_almost_equal(expected, output)

    def test_rank12(self):
        expected = [[3, 3, 2, 4, 4],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [3, 5, 2, 5, 1],
                [5, 5, 8, 3, 5]]
        footprint = [[1, 0, 1], [0, 1, 0]]
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            output = ndimage.rank_filter(array, 1,
<<<<<<< HEAD
                                                  footprint = footprint)
            self.failUnless(diff(true, output) < eps)
            output = ndimage.percentile_filter(array, 50.0,
                                                     footprint = footprint)
            self.failUnless(diff(true, output) < eps)
            output = ndimage.median_filter(array,
                                                    footprint = footprint)
            self.failUnless(diff(true, output) < eps)

    def test_rank13(self):
        "rank filter 13"
        true = [[5, 2, 5, 1, 1],
=======
                                                  footprint=footprint)
            assert_array_almost_equal(expected, output)
            output = ndimage.percentile_filter(array, 50.0,
                                                     footprint=footprint)
            assert_array_almost_equal(expected, output)
            output = ndimage.median_filter(array,
                                                    footprint=footprint)
            assert_array_almost_equal(expected, output)

    def test_rank13(self):
        expected = [[5, 2, 5, 1, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [5, 8, 3, 5, 5],
                [6, 6, 5, 5, 5]]
        footprint = [[1, 0, 1], [0, 1, 0]]
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            output = ndimage.rank_filter(array, 1,
<<<<<<< HEAD
                                       footprint = footprint, origin = -1)
            self.failUnless(diff(true, output) < eps)

    def test_rank14(self):
        "rank filter 14"
        true = [[3, 5, 2, 5, 1],
=======
                                       footprint=footprint, origin=-1)
            assert_array_almost_equal(expected, output)

    def test_rank14(self):
        expected = [[3, 5, 2, 5, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [5, 5, 8, 3, 5],
                [5, 6, 6, 5, 5]]
        footprint = [[1, 0, 1], [0, 1, 0]]
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            output = ndimage.rank_filter(array, 1,
<<<<<<< HEAD
                                  footprint = footprint, origin = [-1, 0])
            self.failUnless(diff(true, output) < eps)

    def test_generic_filter1d01(self):
        "generic 1d filter 1"
        weights = numpy.array([1.1, 2.2, 3.3])
=======
                                  footprint=footprint, origin=[-1, 0])
            assert_array_almost_equal(expected, output)

    def test_rank15(self):
        "rank filter 15"
        expected = [[2, 3, 1, 4, 1],
                [5, 3, 7, 1, 1],
                [5, 5, 3, 3, 3]]
        footprint = [[1, 0, 1], [0, 1, 0]]
        for type in self.types:
            array = numpy.array([[3, 2, 5, 1, 4],
                                    [5, 8, 3, 7, 1],
                                    [5, 6, 9, 3, 5]], type)
            output = ndimage.rank_filter(array, 0,
                                  footprint=footprint, origin=[-1, 0])
            assert_array_almost_equal(expected, output)

    def test_generic_filter1d01(self):
        weights = numpy.array([1.1, 2.2, 3.3])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def _filter_func(input, output, fltr, total):
            fltr = fltr / total
            for ii in range(input.shape[0] - 2):
                output[ii] = input[ii] * fltr[0]
                output[ii] += input[ii + 1] * fltr[1]
                output[ii] += input[ii + 2] * fltr[2]
        for type in self.types:
<<<<<<< HEAD
            a = numpy.arange(12, dtype = type)
            a.shape = (3,4)
            r1 = ndimage.correlate1d(a, weights / weights.sum(), 0,
                                              origin = -1)
            r2 = ndimage.generic_filter1d(a, _filter_func, 3,
                      axis = 0, origin = -1, extra_arguments = (weights,),
                      extra_keywords = {'total': weights.sum()})
            self.failUnless(diff(r1, r2) < eps)

    def test_generic_filter01(self):
        "generic filter 1"
        filter = numpy.array([[1.0, 2.0], [3.0, 4.0]])
        footprint = numpy.array([[1, 0], [0, 1]])
        cf = numpy.array([1., 4.])
        def _filter_func(buffer, weights, total = 1.0):
            weights = cf / total
            return (buffer * weights).sum()
        for type in self.types:
            a = numpy.arange(12, dtype = type)
            a.shape = (3,4)
            r1 = ndimage.correlate(a, filter * footprint) / 5
            r2 = ndimage.generic_filter(a, _filter_func,
                            footprint = footprint, extra_arguments = (cf,),
                            extra_keywords = {'total': cf.sum()})
            self.failUnless(diff(r1, r2) < eps)

    def test_extend01(self):
        "line extension 1"
        array = numpy.array([1, 2, 3])
        weights = numpy.array([1, 0])
        true_values = [[1, 1, 2],
=======
            a = numpy.arange(12, dtype=type)
            a.shape = (3,4)
            r1 = ndimage.correlate1d(a, weights / weights.sum(), 0,
                                              origin=-1)
            r2 = ndimage.generic_filter1d(a, _filter_func, 3,
                      axis=0, origin=-1, extra_arguments=(weights,),
                      extra_keywords={'total': weights.sum()})
            assert_array_almost_equal(r1, r2)

    def test_generic_filter01(self):
        filter_ = numpy.array([[1.0, 2.0], [3.0, 4.0]])
        footprint = numpy.array([[1, 0], [0, 1]])
        cf = numpy.array([1., 4.])

        def _filter_func(buffer, weights, total=1.0):
            weights = cf / total
            return (buffer * weights).sum()
        for type in self.types:
            a = numpy.arange(12, dtype=type)
            a.shape = (3,4)
            r1 = ndimage.correlate(a, filter_ * footprint)
            if type in self.float_types:
                r1 /= 5
            else:
                r1 //= 5
            r2 = ndimage.generic_filter(a, _filter_func,
                            footprint=footprint, extra_arguments=(cf,),
                            extra_keywords={'total': cf.sum()})
            assert_array_almost_equal(r1, r2)

    def test_extend01(self):
        array = numpy.array([1, 2, 3])
        weights = numpy.array([1, 0])
        expected_values = [[1, 1, 2],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [3, 1, 2],
                       [1, 1, 2],
                       [2, 1, 2],
                       [0, 1, 2]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode = mode, cval = 0)
            assert_array_equal(output,true_value)

    def test_extend02(self):
        "line extension 2"
        array = numpy.array([1, 2, 3])
        weights = numpy.array([1, 0, 0, 0, 0, 0, 0, 0])
        true_values = [[1, 1, 1],
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode=mode, cval=0)
            assert_array_equal(output,expected_value)

    def test_extend02(self):
        array = numpy.array([1, 2, 3])
        weights = numpy.array([1, 0, 0, 0, 0, 0, 0, 0])
        expected_values = [[1, 1, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [3, 1, 2],
                       [3, 3, 2],
                       [1, 2, 3],
                       [0, 0, 0]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode = mode, cval = 0)
            assert_array_equal(output, true_value)

    def test_extend03(self):
        "line extension 3"
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 1])
        true_values = [[2, 3, 3],
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend03(self):
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 1])
        expected_values = [[2, 3, 3],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [2, 3, 1],
                       [2, 3, 3],
                       [2, 3, 2],
                       [2, 3, 0]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode = mode, cval = 0)
            assert_array_equal(output, true_value)

    def test_extend04(self):
        "line extension 4"
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 0, 0, 0, 0, 0, 0, 1])
        true_values = [[3, 3, 3],
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend04(self):
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 0, 0, 0, 0, 0, 0, 1])
        expected_values = [[3, 3, 3],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [2, 3, 1],
                       [2, 1, 1],
                       [1, 2, 3],
                       [0, 0, 0]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode = mode, cval = 0)
            assert_array_equal(output, true_value)


    def test_extend05(self):
        "line extension 5"
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate1d(array, weights, 0,
                                         mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[1, 2, 3],
                             [4, 5, 6],
                             [7, 8, 9]])
        weights = numpy.array([[1, 0], [0, 0]])
<<<<<<< HEAD
        true_values = [[[1, 1, 2], [1, 1, 2], [4, 4, 5]],
=======
        expected_values = [[[1, 1, 2], [1, 1, 2], [4, 4, 5]],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [[9, 7, 8], [3, 1, 2], [6, 4, 5]],
                       [[1, 1, 2], [1, 1, 2], [4, 4, 5]],
                       [[5, 4, 5], [2, 1, 2], [5, 4, 5]],
                       [[0, 0, 0], [0, 1, 2], [0, 4, 5]]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate(array, weights,
                                       mode = mode, cval = 0)
            assert_array_equal(output, true_value)


    def test_extend06(self):
        "line extension 6"
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate(array, weights,
                                       mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[1, 2, 3],
                                [4, 5, 6],
                                [7, 8, 9]])
        weights = numpy.array([[0, 0, 0], [0, 0, 0], [0, 0, 1]])
<<<<<<< HEAD
        true_values = [[[5, 6, 6], [8, 9, 9], [8, 9, 9]],
=======
        expected_values = [[[5, 6, 6], [8, 9, 9], [8, 9, 9]],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [[5, 6, 4], [8, 9, 7], [2, 3, 1]],
                       [[5, 6, 6], [8, 9, 9], [8, 9, 9]],
                       [[5, 6, 5], [8, 9, 8], [5, 6, 5]],
                       [[5, 6, 0], [8, 9, 0], [0, 0, 0]]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate(array, weights,
                                       mode = mode, cval = 0)
            assert_array_equal(output, true_value)


    def test_extend07(self):
        "line extension 7"
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 0, 0, 0, 0, 0, 0, 1])
        true_values = [[3, 3, 3],
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate(array, weights,
                                       mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend07(self):
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 0, 0, 0, 0, 0, 0, 1])
        expected_values = [[3, 3, 3],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [2, 3, 1],
                       [2, 1, 1],
                       [1, 2, 3],
                       [0, 0, 0]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate(array, weights,
                                                 mode = mode, cval = 0)
            assert_array_equal(output, true_value)

    def test_extend08(self):
        "line extension 8"
        array = numpy.array([[1], [2], [3]])
        weights = numpy.array([[0], [0], [0], [0], [0], [0], [0],
                                  [0], [1]])
        true_values = [[[3], [3], [3]],
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate(array, weights,
                                                 mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend08(self):
        array = numpy.array([[1], [2], [3]])
        weights = numpy.array([[0], [0], [0], [0], [0], [0], [0],
                                  [0], [1]])
        expected_values = [[[3], [3], [3]],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [[2], [3], [1]],
                       [[2], [1], [1]],
                       [[1], [2], [3]],
                       [[0], [0], [0]]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate(array, weights,
                                                 mode = mode, cval = 0)
            assert_array_equal(output, true_value)

    def test_extend09(self):
        "line extension 9"
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 0, 0, 0, 0, 0, 0, 1])
        true_values = [[3, 3, 3],
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate(array, weights,
                                                 mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend09(self):
        array = numpy.array([1, 2, 3])
        weights = numpy.array([0, 0, 0, 0, 0, 0, 0, 0, 1])
        expected_values = [[3, 3, 3],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [2, 3, 1],
                       [2, 1, 1],
                       [1, 2, 3],
                       [0, 0, 0]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate(array, weights,
                                       mode = mode, cval = 0)
            assert_array_equal(output, true_value)

    def test_extend10(self):
        "line extension 10"
        array = numpy.array([[1], [2], [3]])
        weights = numpy.array([[0], [0], [0], [0], [0], [0], [0],
                                  [0], [1]])
        true_values = [[[3], [3], [3]],
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate(array, weights,
                                       mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_extend10(self):
        array = numpy.array([[1], [2], [3]])
        weights = numpy.array([[0], [0], [0], [0], [0], [0], [0],
                                  [0], [1]])
        expected_values = [[[3], [3], [3]],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                       [[2], [3], [1]],
                       [[2], [1], [1]],
                       [[1], [2], [3]],
                       [[0], [0], [0]]]
<<<<<<< HEAD
        for mode, true_value in zip(self.modes, true_values):
            output = ndimage.correlate(array, weights,
                                       mode = mode, cval = 0)
            assert_array_equal(output, true_value)

    def test_boundaries(self):
        "boundary modes"
=======
        for mode, expected_value in zip(self.modes, expected_values):
            output = ndimage.correlate(array, weights,
                                       mode=mode, cval=0)
            assert_array_equal(output, expected_value)

    def test_boundaries(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def shift(x):
            return (x[0] + 0.5,)

        data = numpy.array([1,2,3,4.])
        expected = {'constant': [1.5,2.5,3.5,-1,-1,-1,-1],
                    'wrap': [1.5,2.5,3.5,1.5,2.5,3.5,1.5],
<<<<<<< HEAD
                    'mirror' : [1.5,2.5,3.5,3.5,2.5,1.5,1.5],
                    'nearest' : [1.5,2.5,3.5,4,4,4,4]}

        for mode in expected.keys():
=======
                    'mirror': [1.5,2.5,3.5,3.5,2.5,1.5,1.5],
                    'nearest': [1.5,2.5,3.5,4,4,4,4]}

        for mode in expected:
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
            assert_array_equal(expected[mode],
                               ndimage.geometric_transform(data,shift,
                                                           cval=-1,mode=mode,
                                                           output_shape=(7,),
                                                           order=1))

    def test_boundaries2(self):
<<<<<<< HEAD
        "boundary modes 2"
=======
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def shift(x):
            return (x[0] - 0.9,)

        data = numpy.array([1,2,3,4])
        expected = {'constant': [-1,1,2,3],
                    'wrap': [3,1,2,3],
<<<<<<< HEAD
                    'mirror' : [2,1,2,3],
                    'nearest' : [1,1,2,3]}

        for mode in expected.keys():
=======
                    'mirror': [2,1,2,3],
                    'nearest': [1,1,2,3]}

        for mode in expected:
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
            assert_array_equal(expected[mode],
                               ndimage.geometric_transform(data,shift,
                                                           cval=-1,mode=mode,
                                                           output_shape=(4,)))

    def test_fourier_gaussian_real01(self):
<<<<<<< HEAD
        "gaussian fourier filter for real transforms 1"
=======
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.float32, numpy.float64]:
                a = numpy.zeros(shape, type)
                a[0, 0] = 1.0
                a = fft.rfft(a, shape[0], 0)
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_gaussian(a, [5.0, 2.5],
                                                       shape[0], 0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.irfft(a, shape[0], 0)
<<<<<<< HEAD
                self.failUnless(diff(ndimage.sum(a), 1.0) < eps)

    def test_fourier_gaussian_complex01(self):
        "gaussian fourier filter for complex transforms 1"
=======
                assert_almost_equal(ndimage.sum(a), 1)

    def test_fourier_gaussian_complex01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.complex64, numpy.complex128]:
                a = numpy.zeros(shape, type)
                a[0, 0] = 1.0
                a = fft.fft(a, shape[0], 0)
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_gaussian(a, [5.0, 2.5], -1,
                                                       0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.ifft(a, shape[0], 0)
<<<<<<< HEAD
                error = diff(ndimage.sum(a.real), 1.0)
                self.failUnless(error < eps)

    def test_fourier_uniform_real01(self):
        "uniform fourier filter for real transforms 1"
=======
                assert_almost_equal(ndimage.sum(a.real), 1.0)

    def test_fourier_uniform_real01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.float32, numpy.float64]:
                a = numpy.zeros(shape, type)
                a[0, 0] = 1.0
                a = fft.rfft(a, shape[0], 0)
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_uniform(a, [5.0, 2.5],
                                                      shape[0], 0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.irfft(a, shape[0], 0)
<<<<<<< HEAD
                self.failUnless(diff(ndimage.sum(a), 1.0) < eps)

    def test_fourier_uniform_complex01(self):
        "uniform fourier filter for complex transforms 1"
=======
                assert_almost_equal(ndimage.sum(a), 1.0)

    def test_fourier_uniform_complex01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.complex64, numpy.complex128]:
                a = numpy.zeros(shape, type)
                a[0, 0] = 1.0
                a = fft.fft(a, shape[0], 0)
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_uniform(a, [5.0, 2.5], -1, 0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.ifft(a, shape[0], 0)
<<<<<<< HEAD
                error = diff(ndimage.sum(a.real), 1.0)
                self.failUnless(error < eps)

    def test_fourier_shift_real01(self):
        "shift filter for real transforms 1"
        for shape in [(32, 16), (31, 15)]:
            for dtype in [numpy.float32, numpy.float64]:
                true = numpy.arange(shape[0] * shape[1], dtype = dtype)
                true.shape = shape
                a = fft.rfft(true, shape[0], 0)
=======
                assert_almost_equal(ndimage.sum(a.real), 1.0)

    def test_fourier_shift_real01(self):
        for shape in [(32, 16), (31, 15)]:
            for dtype in [numpy.float32, numpy.float64]:
                expected = numpy.arange(shape[0] * shape[1], dtype=dtype)
                expected.shape = shape
                a = fft.rfft(expected, shape[0], 0)
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_shift(a, [1, 1], shape[0], 0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.irfft(a, shape[0], 0)
<<<<<<< HEAD
                error1 = diff(a[1:, 1:], true[:-1, :-1])
                error2 = diff(a.imag, numpy.zeros(shape))
                self.failUnless(error1 < 1e-10 and error2 < 1e-10)

    def test_fourier_shift_complex01(self):
        "shift filter for complex transforms 1"
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.complex64, numpy.complex128]:
                true = numpy.arange(shape[0] * shape[1],
                                       dtype = type)
                true.shape = shape
                a = fft.fft(true, shape[0], 0)
=======
                assert_array_almost_equal(a[1:, 1:], expected[:-1, :-1])
                assert_array_almost_equal(a.imag, numpy.zeros(shape))

    def test_fourier_shift_complex01(self):
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.complex64, numpy.complex128]:
                expected = numpy.arange(shape[0] * shape[1],
                                       dtype=type)
                expected.shape = shape
                a = fft.fft(expected, shape[0], 0)
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_shift(a, [1, 1], -1, 0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.ifft(a, shape[0], 0)
<<<<<<< HEAD
                error1 = diff(a.real[1:, 1:], true[:-1, :-1])
                error2 = diff(a.imag, numpy.zeros(shape))
                self.failUnless(error1 < 1e-10 and error2 < 1e-10)

    def test_fourier_ellipsoid_real01(self):
        "ellipsoid fourier filter for real transforms 1"
=======
                assert_array_almost_equal(a.real[1:, 1:], expected[:-1, :-1])
                assert_array_almost_equal(a.imag, numpy.zeros(shape))

    def test_fourier_ellipsoid_real01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.float32, numpy.float64]:
                a = numpy.zeros(shape, type)
                a[0, 0] = 1.0
                a = fft.rfft(a, shape[0], 0)
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_ellipsoid(a, [5.0, 2.5],
<<<<<<< HEAD
                                                        shape[0], 0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.irfft(a, shape[0], 0)
                self.failUnless(diff(ndimage.sum(a), 1.0) < eps)

    def test_fourier_ellipsoid_complex01(self):
        "ellipsoid fourier filter for complex transforms 1"
=======
                                              shape[0], 0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.irfft(a, shape[0], 0)
                assert_almost_equal(ndimage.sum(a), 1.0)

    def test_fourier_ellipsoid_complex01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for shape in [(32, 16), (31, 15)]:
            for type in [numpy.complex64, numpy.complex128]:
                a = numpy.zeros(shape, type)
                a[0, 0] = 1.0
                a = fft.fft(a, shape[0], 0)
                a = fft.fft(a, shape[1], 1)
                a = ndimage.fourier_ellipsoid(a, [5.0, 2.5], -1,
                                                        0)
                a = fft.ifft(a, shape[1], 1)
                a = fft.ifft(a, shape[0], 0)
<<<<<<< HEAD
                error = diff(ndimage.sum(a.real), 1.0)
                self.failUnless(error < eps)

    def test_spline01(self):
        "spline filter 1"
        for type in self.types:
            data = numpy.ones([], type)
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order = order)
                self.failUnless(diff(out, 1)< eps and
                                out.dtype.type == numpy.float64)

    def test_spline02(self):
        "spline filter 2"
        for type in self.types:
            data = numpy.array([1])
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order = order)
                self.failUnless(diff(out, [1]) < eps and
                                out.dtype.type == numpy.float64)

    def test_spline03(self):
        "spline filter 3"
=======
                assert_almost_equal(ndimage.sum(a.real), 1.0)

    def test_spline01(self):
        for type in self.types:
            data = numpy.ones([], type)
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order=order)
                assert_array_almost_equal(out, 1)

    def test_spline02(self):
        for type in self.types:
            data = numpy.array([1])
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order=order)
                assert_array_almost_equal(out, [1])

    def test_spline03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([], type)
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order,
<<<<<<< HEAD
                                                      output = type)
                self.failUnless(diff(out, 1) < eps and
                                out.dtype.type == type)

    def test_spline04(self):
        "spline filter 4"
=======
                                            output=type)
                assert_array_almost_equal(out, 1)

    def test_spline04(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([4], type)
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order)
<<<<<<< HEAD
                self.failUnless(diff(out, [1, 1, 1, 1]) < eps)

    def test_spline05(self):
        "spline filter 5"
        for type in self.types:
            data = numpy.ones([4, 4], type)
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order = order)
                self.failUnless(diff(out, [[1, 1, 1, 1],
                                           [1, 1, 1, 1],
                                           [1, 1, 1, 1],
                                           [1, 1, 1, 1]]) < eps)

    def test_geometric_transform01(self):
        "geometric transform 1"
        data = numpy.array([1])
=======
                assert_array_almost_equal(out, [1, 1, 1, 1])

    def test_spline05(self):
        for type in self.types:
            data = numpy.ones([4, 4], type)
            for order in range(2, 6):
                out = ndimage.spline_filter(data, order=order)
                assert_array_almost_equal(out, [[1, 1, 1, 1],
                                           [1, 1, 1, 1],
                                           [1, 1, 1, 1],
                                           [1, 1, 1, 1]])

    def test_geometric_transform01(self):
        data = numpy.array([1])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return x
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                        data.shape,
                                                        order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1]) < eps)

    def test_geometric_transform02(self):
        "geometric transform 2"
        data = numpy.ones([4])
=======
            assert_array_almost_equal(out, [1])

    def test_geometric_transform01_with_output_parameter(self):
        data = numpy.array([1])

        def mapping(x):
            return x
        for order in range(0, 6):
            out = numpy.empty_like(data)
            ndimage.geometric_transform(data, mapping,
                                        data.shape,
                                        output=out)
            assert_array_almost_equal(out, [1])

            out = numpy.empty_like(data).astype(data.dtype.newbyteorder())
            ndimage.geometric_transform(data, mapping,
                                        data.shape,
                                        output=out)
            assert_array_almost_equal(out, [1])

    def test_geometric_transform02(self):
        data = numpy.ones([4])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return x
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                  data.shape, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 1, 1, 1]) < eps)

    def test_geometric_transform03(self):
        "geometric transform 3"
        data = numpy.ones([4])
=======
            assert_array_almost_equal(out, [1, 1, 1, 1])

    def test_geometric_transform03(self):
        data = numpy.ones([4])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] - 1,)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                   data.shape, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [0, 1, 1, 1]) < eps)

    def test_geometric_transform04(self):
        "geometric transform 4"
        data = numpy.array([4, 1, 3, 2])
=======
            assert_array_almost_equal(out, [0, 1, 1, 1])

    def test_geometric_transform04(self):
        data = numpy.array([4, 1, 3, 2])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] - 1,)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                   data.shape, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [0, 4, 1, 3]) < eps)

    def test_geometric_transform05(self):
        "geometric transform 5"
        data = numpy.array([[1, 1, 1, 1],
                               [1, 1, 1, 1],
                               [1, 1, 1, 1]])
=======
            assert_array_almost_equal(out, [0, 4, 1, 3])

    def test_geometric_transform05(self):
        data = numpy.array([[1, 1, 1, 1],
                               [1, 1, 1, 1],
                               [1, 1, 1, 1]])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0], x[1] - 1)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                   data.shape, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 1, 1, 1],
                                       [0, 1, 1, 1],
                                       [0, 1, 1, 1]]) < eps)

    def test_geometric_transform06(self):
        "geometric transform 6"
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
=======
            assert_array_almost_equal(out, [[0, 1, 1, 1],
                                       [0, 1, 1, 1],
                                       [0, 1, 1, 1]])

    def test_geometric_transform06(self):
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0], x[1] - 1)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                   data.shape, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 4, 1, 3],
                                       [0, 7, 6, 8],
                                       [0, 3, 5, 3]]) < eps)

    def test_geometric_transform07(self):
        "geometric transform 7"
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
=======
            assert_array_almost_equal(out, [[0, 4, 1, 3],
                                       [0, 7, 6, 8],
                                       [0, 3, 5, 3]])

    def test_geometric_transform07(self):
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] - 1, x[1])
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                   data.shape, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [4, 1, 3, 2],
                                       [7, 6, 8, 5]]) < eps)

    def test_geometric_transform08(self):
        "geometric transform 8"
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
=======
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [4, 1, 3, 2],
                                       [7, 6, 8, 5]])

    def test_geometric_transform08(self):
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] - 1, x[1] - 1)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                   data.shape, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]]) < eps)

    def test_geometric_transform10(self):
        "geometric transform 10"
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
=======
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]])

    def test_geometric_transform10(self):
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] - 1, x[1] - 1)
        for order in range(0, 6):
            if (order > 1):
                filtered = ndimage.spline_filter(data,
                                                           order=order)
            else:
                filtered = data
            out = ndimage.geometric_transform(filtered, mapping,
<<<<<<< HEAD
                               data.shape, order=order, prefilter = False)
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]]) < eps)

    def test_geometric_transform13(self):
        "geometric transform 13"
        data = numpy.ones([2], numpy.float64)
        def mapping(x):
            return (x[0] / 2,)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                        [4], order=order)
            self.failUnless(diff(out, [1, 1, 1, 1]) < eps)

    def test_geometric_transform14(self):
        "geometric transform 14"
        data = [1, 5, 2, 6, 3, 7, 4, 4]
=======
                               data.shape, order=order, prefilter=False)
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]])

    def test_geometric_transform13(self):
        data = numpy.ones([2], numpy.float64)

        def mapping(x):
            return (x[0] // 2,)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                        [4], order=order)
            assert_array_almost_equal(out, [1, 1, 1, 1])

    def test_geometric_transform14(self):
        data = [1, 5, 2, 6, 3, 7, 4, 4]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (2 * x[0],)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                        [4], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 2, 3, 4]) < eps)

    def test_geometric_transform15(self):
        "geometric transform 15"
        data = [1, 2, 3, 4]
=======
            assert_array_almost_equal(out, [1, 2, 3, 4])

    def test_geometric_transform15(self):
        data = [1, 2, 3, 4]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] / 2,)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                        [8], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[::2], [1, 2, 3, 4]) < eps)

    def test_geometric_transform16(self):
        "geometric transform 16"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9.0, 10, 11, 12]]
=======
            assert_array_almost_equal(out[::2], [1, 2, 3, 4])

    def test_geometric_transform16(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9.0, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0], x[1] * 2)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                       (3, 2), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[1, 3], [5, 7], [9, 11]]) < eps)

    def test_geometric_transform17(self):
        "geometric transform 17"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
=======
            assert_array_almost_equal(out, [[1, 3], [5, 7], [9, 11]])

    def test_geometric_transform17(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] * 2, x[1])
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                       (1, 4), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[1, 2, 3, 4]]) < eps)

    def test_geometric_transform18(self):
        "geometric transform 18"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
=======
            assert_array_almost_equal(out, [[1, 2, 3, 4]])

    def test_geometric_transform18(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] * 2, x[1] * 2)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                       (1, 2), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[1, 3]]) < eps)

    def test_geometric_transform19(self):
        "geometric transform 19"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
=======
            assert_array_almost_equal(out, [[1, 3]])

    def test_geometric_transform19(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0], x[1] / 2)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                       (3, 8), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[..., ::2], data) < eps)

    def test_geometric_transform20(self):
        "geometric transform 20"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
=======
            assert_array_almost_equal(out[..., ::2], data)

    def test_geometric_transform20(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] / 2, x[1])
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                       (6, 4), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[::2, ...], data) < eps)

    def test_geometric_transform21(self):
        "geometric transform 21"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
=======
            assert_array_almost_equal(out[::2, ...], data)

    def test_geometric_transform21(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (x[0] / 2, x[1] / 2)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                      (6, 8), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[::2, ::2], data) < eps)


    def test_geometric_transform22(self):
        "geometric transform 22"
        data = numpy.array([[1, 2, 3, 4],
                               [5, 6, 7, 8],
                               [9, 10, 11, 12]], numpy.float64)
        def mapping1(x):
            return (x[0] / 2, x[1] / 2)
=======
            assert_array_almost_equal(out[::2, ::2], data)

    def test_geometric_transform22(self):
        data = numpy.array([[1, 2, 3, 4],
                               [5, 6, 7, 8],
                               [9, 10, 11, 12]], numpy.float64)

        def mapping1(x):
            return (x[0] / 2, x[1] / 2)

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping2(x):
            return (x[0] * 2, x[1] * 2)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping1,
<<<<<<< HEAD
                                                      (6, 8),  order=order)
            out = ndimage.geometric_transform(out, mapping2,
                                                       (3, 4), order=order)
            error = diff(out, data)
            self.failUnless(diff(out, data) < eps)

    def test_geometric_transform23(self):
        "geometric transform 23"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
=======
                                              (6, 8), order=order)
            out = ndimage.geometric_transform(out, mapping2,
                                              (3, 4), order=order)
            assert_array_almost_equal(out, data)

    def test_geometric_transform23(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x):
            return (1, x[0] * 2)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
                                                        (2,), order=order)
            out = out.astype(numpy.int32)
<<<<<<< HEAD
            self.failUnless(diff(out, [5, 7]) < eps)

    def test_geometric_transform24(self):
        "geometric transform 24"
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
=======
            assert_array_almost_equal(out, [5, 7])

    def test_geometric_transform24(self):
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        def mapping(x, a, b):
            return (a, x[0] * b)
        for order in range(0, 6):
            out = ndimage.geometric_transform(data, mapping,
<<<<<<< HEAD
                                (2,), order=order, extra_arguments = (1,),
                                extra_keywords = {'b': 2})
            self.failUnless(diff(out, [5, 7]) < eps)

    def test_map_coordinates01(self):
        "map coordinates 1"
=======
                                (2,), order=order, extra_arguments=(1,),
                                extra_keywords={'b': 2})
            assert_array_almost_equal(out, [5, 7])

    def test_map_coordinates01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        idx = numpy.indices(data.shape)
        idx -= 1
        for order in range(0, 6):
            out = ndimage.map_coordinates(data, idx, order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]]) < eps)

    def test_map_coordinates02(self):
        "map coordinates 2"
=======
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]])

    def test_map_coordinates01_with_output_parameter(self):
        data = numpy.array([[4, 1, 3, 2],
                            [7, 6, 8, 5],
                            [3, 5, 3, 6]])
        idx = numpy.indices(data.shape)
        idx -= 1
        expected = numpy.array([[0, 0, 0, 0],
                                [0, 4, 1, 3],
                                [0, 7, 6, 8]])
        for order in range(0, 6):
            out = numpy.empty_like(expected)
            ndimage.map_coordinates(data, idx, order=order, output=out)
            assert_array_almost_equal(out, expected)

            out = numpy.empty_like(expected).astype(
                expected.dtype.newbyteorder())
            ndimage.map_coordinates(data, idx, order=order, output=out)
            assert_array_almost_equal(out, expected)

    def test_map_coordinates02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        idx = numpy.indices(data.shape, numpy.float64)
        idx -= 0.5
        for order in range(0, 6):
            out1 = ndimage.shift(data, 0.5, order=order)
            out2 = ndimage.map_coordinates(data, idx,
                                                     order=order)
<<<<<<< HEAD
            self.failUnless(diff(out1, out2) < eps)

    def test_affine_transform01(self):
        "affine_transform 1"
=======
            assert_array_almost_equal(out1, out2)

    def test_map_coordinates03(self):
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]], order='F')
        idx = numpy.indices(data.shape) - 1
        out = ndimage.map_coordinates(data, idx)
        assert_array_almost_equal(out, [[0, 0, 0, 0],
                                   [0, 4, 1, 3],
                                   [0, 7, 6, 8]])
        assert_array_almost_equal(out, ndimage.shift(data, (1, 1)))
        idx = numpy.indices(data[::2].shape) - 1
        out = ndimage.map_coordinates(data[::2], idx)
        assert_array_almost_equal(out, [[0, 0, 0, 0],
                                   [0, 4, 1, 3]])
        assert_array_almost_equal(out, ndimage.shift(data[::2], (1, 1)))
        idx = numpy.indices(data[:,::2].shape) - 1
        out = ndimage.map_coordinates(data[:,::2], idx)
        assert_array_almost_equal(out, [[0, 0], [0, 4], [0, 7]])
        assert_array_almost_equal(out, ndimage.shift(data[:,::2], (1, 1)))

    # do not run on 32 bit or windows (no sparse memory)
    @dec.skipif('win32' in sys.platform or numpy.intp(0).itemsize < 8)
    def test_map_coordinates_large_data(self):
        # check crash on large data
        try:
            n = 30000
            a = numpy.empty(n**2, dtype=numpy.float32).reshape(n, n)
            # fill the part we might read
            a[n - 3:,n - 3:] = 0
            ndimage.map_coordinates(a, [[n - 1.5], [n - 1.5]], order=1)
        except MemoryError:
            raise SkipTest("Not enough memory available")

    def test_affine_transform01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([1])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1]],
                                                     order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1]) < eps)

    def test_affine_transform02(self):
        "affine transform 2"
=======
            assert_array_almost_equal(out, [1])

    def test_affine_transform01_with_output_parameter(self):
        data = numpy.array([1])
        for order in range(0, 6):
            out = numpy.empty_like(data)
            ndimage.affine_transform(data, [[1]],
                                     order=order,
                                     output=out)
            assert_array_almost_equal(out, [1])

            out = numpy.empty_like(data).astype(data.dtype.newbyteorder())
            ndimage.affine_transform(data, [[1]],
                                     order=order,
                                     output=out)
            assert_array_almost_equal(out, [1])

    def test_affine_transform02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.ones([4])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1]],
                                                     order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 1, 1, 1]) < eps)

    def test_affine_transform03(self):
        "affine transform 3"
=======
            assert_array_almost_equal(out, [1, 1, 1, 1])

    def test_affine_transform03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.ones([4])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1]], -1,
                                                     order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [0, 1, 1, 1]) < eps)

    def test_affine_transform04(self):
        "affine transform 4"
=======
            assert_array_almost_equal(out, [0, 1, 1, 1])

    def test_affine_transform04(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([4, 1, 3, 2])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1]], -1,
                                                     order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [0, 4, 1, 3]) < eps)

    def test_affine_transform05(self):
        "affine transform 5"
=======
            assert_array_almost_equal(out, [0, 4, 1, 3])

    def test_affine_transform05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[1, 1, 1, 1],
                               [1, 1, 1, 1],
                               [1, 1, 1, 1]])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1, 0],
                                                            [0, 1]],
                                                     [0, -1], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 1, 1, 1],
                                       [0, 1, 1, 1],
                                       [0, 1, 1, 1]]) < eps)

    def test_affine_transform06(self):
        "affine transform 6"
=======
            assert_array_almost_equal(out, [[0, 1, 1, 1],
                                       [0, 1, 1, 1],
                                       [0, 1, 1, 1]])

    def test_affine_transform06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1, 0],
                                                            [0, 1]],
                                                     [0, -1], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 4, 1, 3],
                                       [0, 7, 6, 8],
                                       [0, 3, 5, 3]]) < eps)

    def test_affine_transform07(self):
        "affine transform 7"
=======
            assert_array_almost_equal(out, [[0, 4, 1, 3],
                                       [0, 7, 6, 8],
                                       [0, 3, 5, 3]])

    def test_affine_transform07(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1, 0],
                                                            [0, 1]],
                                                     [-1, 0], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [4, 1, 3, 2],
                                       [7, 6, 8, 5]]) < eps)

    def test_affine_transform08(self):
        "affine transform 8"
=======
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [4, 1, 3, 2],
                                       [7, 6, 8, 5]])

    def test_affine_transform08(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1, 0],
                                                            [0, 1]],
                                                     [-1, -1], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]]) < eps)

    def test_affine_transform09(self):
        "affine transform 9"
=======
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]])

    def test_affine_transform09(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            if (order > 1):
                filtered = ndimage.spline_filter(data,
                                                           order=order)
            else:
                filtered = data
            out = ndimage.affine_transform(filtered,[[1, 0],
                                                               [0, 1]],
<<<<<<< HEAD
                                  [-1, -1], order=order, prefilter = False)
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]]) < eps)

    def test_affine_transform10(self):
        "affine transform 10"
        data = numpy.ones([2], numpy.float64)
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[0.5]],
                                          output_shape = (4,), order=order)
            self.failUnless(diff(out, [1, 1, 1, 0]) < eps)

    def test_affine_transform11(self):
        "affine transform 11"
=======
                                  [-1, -1], order=order, prefilter=False)
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]])

    def test_affine_transform10(self):
        data = numpy.ones([2], numpy.float64)
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[0.5]],
                                          output_shape=(4,), order=order)
            assert_array_almost_equal(out, [1, 1, 1, 0])

    def test_affine_transform11(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [1, 5, 2, 6, 3, 7, 4, 4]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[2]], 0, (4,),
                                                     order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 2, 3, 4]) < eps)

    def test_affine_transform12(self):
        "affine transform 12"
=======
            assert_array_almost_equal(out, [1, 2, 3, 4])

    def test_affine_transform12(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [1, 2, 3, 4]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[0.5]], 0,
                                                     (8,), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[::2], [1, 2, 3, 4]) < eps)

    def test_affine_transform13(self):
        "affine transform 13"
=======
            assert_array_almost_equal(out[::2], [1, 2, 3, 4])

    def test_affine_transform13(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9.0, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1, 0],
                                                            [0, 2]], 0,
                                                     (3, 2), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[1, 3], [5, 7], [9, 11]]) < eps)

    def test_affine_transform14(self):
        "affine transform 14"
=======
            assert_array_almost_equal(out, [[1, 3], [5, 7], [9, 11]])

    def test_affine_transform14(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[2, 0],
                                                            [0, 1]], 0,
                                                     (1, 4), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[1, 2, 3, 4]]) < eps)

    def test_affine_transform15(self):
        "affine transform 15"
=======
            assert_array_almost_equal(out, [[1, 2, 3, 4]])

    def test_affine_transform15(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[2, 0],
                                                            [0, 2]], 0,
                                                     (1, 2), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[1, 3]]) < eps)

    def test_affine_transform16(self):
        "affine transform 16"
=======
            assert_array_almost_equal(out, [[1, 3]])

    def test_affine_transform16(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[1, 0.0],
                                                            [0, 0.5]], 0,
                                                     (3, 8), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[..., ::2], data) < eps)

    def test_affine_transform17(self):
        "affine transform 17"
=======
            assert_array_almost_equal(out[..., ::2], data)

    def test_affine_transform17(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[0.5, 0],
<<<<<<< HEAD
                                                            [0,   1]], 0,
                                                     (6, 4), order=order)
            self.failUnless(diff(out[::2, ...], data) < eps)

    def test_affine_transform18(self):
        "affine transform 18"
=======
                                                            [0, 1]], 0,
                                                     (6, 4), order=order)
            assert_array_almost_equal(out[::2, ...], data)

    def test_affine_transform18(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data,
                                                     [[0.5, 0],
                                                      [0, 0.5]], 0,
                                                     (6, 8), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[::2, ::2], data) < eps)

    def test_affine_transform19(self):
        "affine transform 19"
=======
            assert_array_almost_equal(out[::2, ::2], data)

    def test_affine_transform19(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[1, 2, 3, 4],
                               [5, 6, 7, 8],
                               [9, 10, 11, 12]], numpy.float64)
        for order in range(0, 6):
            out = ndimage.affine_transform(data,
                                                     [[0.5, 0],
                                                      [0, 0.5]], 0,
                                                     (6, 8), order=order)
            out = ndimage.affine_transform(out,
                                                     [[2.0, 0],
                                                      [0, 2.0]], 0,
                                                     (3, 4), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, data) < eps)

    def test_affine_transform20(self):
        "affine transform 20"
=======
            assert_array_almost_equal(out, data)

    def test_affine_transform20(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[0], [2]], 0,
                                                     (2,), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 3]) < eps)

    def test_affine_transform21(self):
        "affine transform 21"
=======
            assert_array_almost_equal(out, [1, 3])

    def test_affine_transform21(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [[2], [0]], 0,
                                                     (2,), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 9]) < eps)

    def test_shift01(self):
        "shift 1"
        data = numpy.array([1])
        for order in range(0, 6):
            out = ndimage.shift(data, [1], order=order)
            self.failUnless(diff(out, [0]) < eps)

    def test_shift02(self):
        "shift 2"
        data = numpy.ones([4])
        for order in range(0, 6):
            out = ndimage.shift(data, [1], order=order)
            self.failUnless(diff(out, [0, 1, 1, 1]) < eps)

    def test_shift03(self):
        "shift 3"
        data = numpy.ones([4])
        for order in range(0, 6):
            out = ndimage.shift(data, -1, order=order)
            self.failUnless(diff(out, [1, 1, 1, 0]) < eps)

    def test_shift04(self):
        "shift 4"
        data = numpy.array([4, 1, 3, 2])
        for order in range(0, 6):
            out = ndimage.shift(data, 1, order=order)
            self.failUnless(diff(out, [0, 4, 1, 3]) < eps)

    def test_shift05(self):
        "shift 5"
=======
            assert_array_almost_equal(out, [1, 9])

    def test_shift01(self):
        data = numpy.array([1])
        for order in range(0, 6):
            out = ndimage.shift(data, [1], order=order)
            assert_array_almost_equal(out, [0])

    def test_shift02(self):
        data = numpy.ones([4])
        for order in range(0, 6):
            out = ndimage.shift(data, [1], order=order)
            assert_array_almost_equal(out, [0, 1, 1, 1])

    def test_shift03(self):
        data = numpy.ones([4])
        for order in range(0, 6):
            out = ndimage.shift(data, -1, order=order)
            assert_array_almost_equal(out, [1, 1, 1, 0])

    def test_shift04(self):
        data = numpy.array([4, 1, 3, 2])
        for order in range(0, 6):
            out = ndimage.shift(data, 1, order=order)
            assert_array_almost_equal(out, [0, 4, 1, 3])

    def test_shift05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[1, 1, 1, 1],
                               [1, 1, 1, 1],
                               [1, 1, 1, 1]])
        for order in range(0, 6):
            out = ndimage.shift(data, [0, 1], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 1, 1, 1],
                                       [0, 1, 1, 1],
                                       [0, 1, 1, 1]]) < eps)

    def test_shift06(self):
        "shift 6"
=======
            assert_array_almost_equal(out, [[0, 1, 1, 1],
                                       [0, 1, 1, 1],
                                       [0, 1, 1, 1]])

    def test_shift06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            out = ndimage.shift(data, [0, 1], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 4, 1, 3],
                                       [0, 7, 6, 8],
                                       [0, 3, 5, 3]]) < eps)

    def test_shift07(self):
        "shift 7"
=======
            assert_array_almost_equal(out, [[0, 4, 1, 3],
                                       [0, 7, 6, 8],
                                       [0, 3, 5, 3]])

    def test_shift07(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            out = ndimage.shift(data, [1, 0], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [4, 1, 3, 2],
                                       [7, 6, 8, 5]]) < eps)


    def test_shift08(self):
        "shift 8"
=======
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [4, 1, 3, 2],
                                       [7, 6, 8, 5]])

    def test_shift08(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            out = ndimage.shift(data, [1, 1], order=order)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]]) < eps)

    def test_shift09(self):
        "shift 9"
=======
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]])

    def test_shift09(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[4, 1, 3, 2],
                               [7, 6, 8, 5],
                               [3, 5, 3, 6]])
        for order in range(0, 6):
            if (order > 1):
                filtered = ndimage.spline_filter(data,
                                                           order=order)
            else:
                filtered = data
            out = ndimage.shift(filtered, [1, 1], order=order,
<<<<<<< HEAD
                                          prefilter = False)
            self.failUnless(diff(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]]) < eps)

    def test_zoom1(self):
        "zoom 1"
        for order in range(0,6):
            for z in [2,[2,2]]:
                arr = numpy.array(range(25)).reshape((5,5)).astype(float)
                arr = ndimage.zoom(arr, z, order=order)
                assert_equal(arr.shape,(10,10))
                assert numpy.all(arr[-1,:] != 0)
                assert numpy.all(arr[-1,:] >= (20 - eps))
                assert numpy.all(arr[0,:] <= (5 + eps))
                assert numpy.all(arr >= (0 - eps))
                assert numpy.all(arr <= (24 + eps))

    def test_zoom2(self):
        "zoom 2"
=======
                                          prefilter=False)
            assert_array_almost_equal(out, [[0, 0, 0, 0],
                                       [0, 4, 1, 3],
                                       [0, 7, 6, 8]])

    def test_zoom1(self):
        for order in range(0,6):
            for z in [2,[2,2]]:
                arr = numpy.array(list(range(25))).reshape((5,5)).astype(float)
                arr = ndimage.zoom(arr, z, order=order)
                assert_equal(arr.shape,(10,10))
                assert_(numpy.all(arr[-1,:] != 0))
                assert_(numpy.all(arr[-1,:] >= (20 - eps)))
                assert_(numpy.all(arr[0,:] <= (5 + eps)))
                assert_(numpy.all(arr >= (0 - eps)))
                assert_(numpy.all(arr <= (24 + eps)))

    def test_zoom2(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        arr = numpy.arange(12).reshape((3,4))
        out = ndimage.zoom(ndimage.zoom(arr,2),0.5)
        assert_array_equal(out,arr)

<<<<<<< HEAD
    def test_zoom_affine01(self):
        "zoom by affine transformation 1"
=======
    def test_zoom3(self):
        err = numpy.seterr(invalid='ignore')
        arr = numpy.array([[1, 2]])
        try:
            out1 = ndimage.zoom(arr, (2, 1))
            out2 = ndimage.zoom(arr, (1,2))
        finally:
            numpy.seterr(**err)

        assert_array_almost_equal(out1, numpy.array([[1, 2], [1, 2]]))
        assert_array_almost_equal(out2, numpy.array([[1, 1, 2, 2]]))

    def test_zoom_affine01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = [[1, 2, 3, 4],
                [5, 6, 7, 8],
                [9, 10, 11, 12]]
        for order in range(0, 6):
            out = ndimage.affine_transform(data, [0.5, 0.5], 0,
                                                     (6, 8), order=order)
<<<<<<< HEAD
            self.failUnless(diff(out[::2, ::2], data) < eps)

    def test_rotate01(self):
        "rotate 1"
        data = numpy.array([[0, 0, 0, 0],
                               [0, 1, 1, 0],
                               [0, 0, 0, 0]], dtype = numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 0)
            self.failUnless(diff(out, data) < eps)

    def test_rotate02(self):
        "rotate 2"
        data = numpy.array([[0, 0, 0, 0],
                               [0, 1, 0, 0],
                               [0, 0, 0, 0]], dtype = numpy.float64)
        true = numpy.array([[0, 0, 0],
                               [0, 0, 0],
                               [0, 1, 0],
                               [0, 0, 0]], dtype = numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 90)
            self.failUnless(diff(out, true) < eps)

    def test_rotate03(self):
        "rotate 3"
        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0]], dtype = numpy.float64)
        true = numpy.array([[0, 0, 0],
                               [0, 0, 0],
                               [0, 1, 0],
                               [0, 1, 0],
                               [0, 0, 0]], dtype = numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 90)
            self.failUnless(diff(out, true) < eps)

    def test_rotate04(self):
        "rotate 4"
        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0]], dtype = numpy.float64)
        true = numpy.array([[0, 0, 0, 0, 0],
                               [0, 0, 1, 0, 0],
                               [0, 0, 1, 0, 0]], dtype = numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 90, reshape = False)
            self.failUnless(diff(out, true) < eps)

    def test_rotate05(self):
        "rotate 5"
=======
            assert_array_almost_equal(out[::2, ::2], data)

    def test_zoom_infinity(self):
        # Ticket #1419 regression test
        err = numpy.seterr(divide='ignore')

        try:
            dim = 8
            ndimage.zoom(numpy.zeros((dim, dim)), 1./dim, mode='nearest')
        finally:
            numpy.seterr(**err)

    def test_zoom_zoomfactor_one(self):
        # Ticket #1122 regression test
        arr = numpy.zeros((1, 5, 5))
        zoom = (1.0, 2.0, 2.0)

        err = numpy.seterr(invalid='ignore')
        try:
            out = ndimage.zoom(arr, zoom, cval=7)
        finally:
            numpy.seterr(**err)
        ref = numpy.zeros((1, 10, 10))
        assert_array_almost_equal(out, ref)

    def test_zoom_output_shape_roundoff(self):
        arr = numpy.zeros((3, 11, 25))
        zoom = (4.0 / 3, 15.0 / 11, 29.0 / 25)
        with warnings.catch_warnings():
            warnings.simplefilter("ignore", UserWarning)
            out = ndimage.zoom(arr, zoom)
            assert_array_equal(out.shape, (4, 15, 29))

    def test_rotate01(self):
        data = numpy.array([[0, 0, 0, 0],
                               [0, 1, 1, 0],
                               [0, 0, 0, 0]], dtype=numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 0)
            assert_array_almost_equal(out, data)

    def test_rotate02(self):
        data = numpy.array([[0, 0, 0, 0],
                               [0, 1, 0, 0],
                               [0, 0, 0, 0]], dtype=numpy.float64)
        expected = numpy.array([[0, 0, 0],
                               [0, 0, 0],
                               [0, 1, 0],
                               [0, 0, 0]], dtype=numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 90)
            assert_array_almost_equal(out, expected)

    def test_rotate03(self):
        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0]], dtype=numpy.float64)
        expected = numpy.array([[0, 0, 0],
                               [0, 0, 0],
                               [0, 1, 0],
                               [0, 1, 0],
                               [0, 0, 0]], dtype=numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 90)
            assert_array_almost_equal(out, expected)

    def test_rotate04(self):
        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0]], dtype=numpy.float64)
        expected = numpy.array([[0, 0, 0, 0, 0],
                               [0, 0, 1, 0, 0],
                               [0, 0, 1, 0, 0]], dtype=numpy.float64)
        for order in range(0, 6):
            out = ndimage.rotate(data, 90, reshape=False)
            assert_array_almost_equal(out, expected)

    def test_rotate05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.empty((4,3,3))
        for i in range(3):
            data[:,:,i] = numpy.array([[0,0,0],
                                       [0,1,0],
                                       [0,1,0],
<<<<<<< HEAD
                                       [0,0,0]], dtype = numpy.float64)

        true = numpy.array([[0,0,0,0],
                            [0,1,1,0],
                            [0,0,0,0]], dtype = numpy.float64)
=======
                                       [0,0,0]], dtype=numpy.float64)

        expected = numpy.array([[0,0,0,0],
                            [0,1,1,0],
                            [0,0,0,0]], dtype=numpy.float64)
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b

        for order in range(0, 6):
            out = ndimage.rotate(data, 90)
            for i in range(3):
<<<<<<< HEAD
                self.failUnless(diff(out[:,:,i], true) < eps)

    def test_rotate06(self):
        "rotate 6"
=======
                assert_array_almost_equal(out[:,:,i], expected)

    def test_rotate06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.empty((3,4,3))
        for i in range(3):
            data[:,:,i] = numpy.array([[0,0,0,0],
                                       [0,1,1,0],
<<<<<<< HEAD
                                       [0,0,0,0]], dtype = numpy.float64)

        true = numpy.array([[0,0,0],
                            [0,1,0],
                            [0,1,0],
                            [0,0,0]], dtype = numpy.float64)
=======
                                       [0,0,0,0]], dtype=numpy.float64)

        expected = numpy.array([[0,0,0],
                            [0,1,0],
                            [0,1,0],
                            [0,0,0]], dtype=numpy.float64)
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b

        for order in range(0, 6):
            out = ndimage.rotate(data, 90)
            for i in range(3):
<<<<<<< HEAD
                self.failUnless(diff(out[:,:,i], true) < eps)

    def test_rotate07(self):
        "rotate 7"
        data = numpy.array([[[0, 0, 0, 0, 0],
                             [0, 1, 1, 0, 0],
                             [0, 0, 0, 0, 0]]] * 2,
                           dtype = numpy.float64)
        data = data.transpose()
        true = numpy.array([[[0, 0, 0],
                                [0, 1, 0],
                                [0, 1, 0],
                                [0, 0, 0],
                                [0, 0, 0]]] * 2, dtype = numpy.float64)
        true = true.transpose([2,1,0])

        for order in range(0, 6):
            out = ndimage.rotate(data, 90, axes = (0, 1))
            self.failUnless(diff(out, true) < eps)

    def test_rotate08(self):
        "rotate 8"
        data = numpy.array([[[0, 0, 0, 0, 0],
                                [0, 1, 1, 0, 0],
                                [0, 0, 0, 0, 0]]] * 2,
                              dtype = numpy.float64)
        data = data.transpose()
        true = numpy.array([[[0, 0, 1, 0, 0],
                                [0, 0, 1, 0, 0],
                                [0, 0, 0, 0, 0]]] * 2,
                              dtype = numpy.float64)
        true = true.transpose()
        for order in range(0, 6):
            out = ndimage.rotate(data, 90, axes = (0, 1),
                                           reshape = False)
            self.failUnless(diff(out, true) < eps)

    def test_watershed_ift01(self):
        "watershed_ift 1"
=======
                assert_array_almost_equal(out[:,:,i], expected)

    def test_rotate07(self):
        data = numpy.array([[[0, 0, 0, 0, 0],
                             [0, 1, 1, 0, 0],
                             [0, 0, 0, 0, 0]]] * 2,
                           dtype=numpy.float64)
        data = data.transpose()
        expected = numpy.array([[[0, 0, 0],
                                [0, 1, 0],
                                [0, 1, 0],
                                [0, 0, 0],
                                [0, 0, 0]]] * 2, dtype=numpy.float64)
        expected = expected.transpose([2,1,0])

        for order in range(0, 6):
            out = ndimage.rotate(data, 90, axes=(0, 1))
            assert_array_almost_equal(out, expected)

    def test_rotate08(self):
        data = numpy.array([[[0, 0, 0, 0, 0],
                                [0, 1, 1, 0, 0],
                                [0, 0, 0, 0, 0]]] * 2,
                              dtype=numpy.float64)
        data = data.transpose()
        expected = numpy.array([[[0, 0, 1, 0, 0],
                                [0, 0, 1, 0, 0],
                                [0, 0, 0, 0, 0]]] * 2,
                              dtype=numpy.float64)
        expected = expected.transpose()
        for order in range(0, 6):
            out = ndimage.rotate(data, 90, axes=(0, 1),
                                           reshape=False)
            assert_array_almost_equal(out, expected)

    def test_watershed_ift01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
<<<<<<< HEAD
        markers = numpy.array([[ -1, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 1, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                     structure = [[1,1,1],
                                                  [1,1,1],
                                                  [1,1,1]])
        error = diff([[-1, -1, -1, -1, -1, -1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1, -1, -1, -1, -1, -1, -1],
                      [-1, -1, -1, -1, -1, -1, -1]], out)
        self.failUnless(error < eps)

    def test_watershed_ift02(self):
        "watershed_ift 2"
=======
        markers = numpy.array([[-1, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 1, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                     structure=[[1,1,1],
                                                [1,1,1],
                                                [1,1,1]])
        expected = [[-1, -1, -1, -1, -1, -1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, -1, -1, -1, -1, -1, -1],
                    [-1, -1, -1, -1, -1, -1, -1]]
        assert_array_almost_equal(out, expected)

    def test_watershed_ift02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
<<<<<<< HEAD
        markers = numpy.array([[ -1, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 1, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers)
        error = diff([[-1, -1, -1, -1, -1, -1, -1],
                      [-1, -1,  1,  1,  1, -1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1, -1,  1,  1,  1, -1, -1],
                      [-1, -1, -1, -1, -1, -1, -1],
                      [-1, -1, -1, -1, -1, -1, -1]], out)
        self.failUnless(error < eps)

    def test_watershed_ift03(self):
        "watershed_ift 3"
=======
        markers = numpy.array([[-1, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 1, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers)
        expected = [[-1, -1, -1, -1, -1, -1, -1],
                    [-1, -1, 1, 1, 1, -1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, -1, 1, 1, 1, -1, -1],
                    [-1, -1, -1, -1, -1, -1, -1],
                    [-1, -1, -1, -1, -1, -1, -1]]
        assert_array_almost_equal(out, expected)

    def test_watershed_ift03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
<<<<<<< HEAD
        markers = numpy.array([[ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 2, 0, 3, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, -1]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers)
        error = diff([[-1, -1, -1, -1, -1, -1, -1],
                      [-1, -1,  2, -1,  3, -1, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1, -1,  2, -1,  3, -1, -1],
                      [-1, -1, -1, -1, -1, -1, -1]], out)
        self.failUnless(error < eps)

    def test_watershed_ift04(self):
        "watershed_ift 4"
=======
        markers = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 2, 0, 3, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, -1]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers)
        expected = [[-1, -1, -1, -1, -1, -1, -1],
                    [-1, -1, 2, -1, 3, -1, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, -1, 2, -1, 3, -1, -1],
                    [-1, -1, -1, -1, -1, -1, -1]]
        assert_array_almost_equal(out, expected)

    def test_watershed_ift04(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
<<<<<<< HEAD
        markers = numpy.array([[ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 2, 0, 3, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, -1]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                              structure = [[1,1,1],
                                                           [1,1,1],
                                                           [1,1,1]])
        error = diff([[-1, -1, -1, -1, -1, -1, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1,  2,  2,  3,  3,  3, -1],
                      [-1, -1, -1, -1, -1, -1, -1]], out)
        self.failUnless(error < eps)

    def test_watershed_ift05(self):
        "watershed_ift 5"
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 0, 1, 0, 1, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
        markers = numpy.array([[ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 3, 0, 2, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, -1]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                              structure = [[1,1,1],
                                                           [1,1,1],
                                                           [1,1,1]])
        error = diff([[-1, -1, -1, -1, -1, -1, -1],
                      [-1,  3,  3,  2,  2,  2, -1],
                      [-1,  3,  3,  2,  2,  2, -1],
                      [-1,  3,  3,  2,  2,  2, -1],
                      [-1,  3,  3,  2,  2,  2, -1],
                      [-1,  3,  3,  2,  2,  2, -1],
                      [-1, -1, -1, -1, -1, -1, -1]], out)
        self.failUnless(error < eps)

    def test_watershed_ift06(self):
        "watershed_ift 6"
=======
        markers = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 2, 0, 3, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, -1]],
                              numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                    structure=[[1,1,1],
                                               [1,1,1],
                                               [1,1,1]])
        expected = [[-1, -1, -1, -1, -1, -1, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, 2, 2, 3, 3, 3, -1],
                    [-1, -1, -1, -1, -1, -1, -1]]
        assert_array_almost_equal(out, expected)

    def test_watershed_ift05(self):
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                            [0, 1, 1, 1, 1, 1, 0],
                            [0, 1, 0, 1, 0, 1, 0],
                            [0, 1, 0, 1, 0, 1, 0],
                            [0, 1, 0, 1, 0, 1, 0],
                            [0, 1, 1, 1, 1, 1, 0],
                            [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
        markers = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 3, 0, 2, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, -1]],
                              numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                    structure=[[1,1,1],
                                               [1,1,1],
                                               [1,1,1]])
        expected = [[-1, -1, -1, -1, -1, -1, -1],
                    [-1, 3, 3, 2, 2, 2, -1],
                    [-1, 3, 3, 2, 2, 2, -1],
                    [-1, 3, 3, 2, 2, 2, -1],
                    [-1, 3, 3, 2, 2, 2, -1],
                    [-1, 3, 3, 2, 2, 2, -1],
                    [-1, -1, -1, -1, -1, -1, -1]]
        assert_array_almost_equal(out, expected)

    def test_watershed_ift06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = numpy.array([[0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 0, 0, 0, 1, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
<<<<<<< HEAD
        markers = numpy.array([[ -1, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 1, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0],
                                  [  0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                              structure = [[1,1,1],
                                                           [1,1,1],
                                                           [1,1,1]])
        error = diff([[-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1, -1, -1, -1, -1, -1, -1],
                      [-1, -1, -1, -1, -1, -1, -1]], out)
        self.failUnless(error < eps)

    def test_watershed_ift07(self):
        "watershed_ift 7"
        shape = (7, 6)
        data = numpy.zeros(shape, dtype = numpy.uint8)
=======
        markers = numpy.array([[-1, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 1, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = ndimage.watershed_ift(data, markers,
                                              structure=[[1,1,1],
                                                         [1,1,1],
                                                         [1,1,1]])
        expected = [[-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, -1, -1, -1, -1, -1, -1],
                    [-1, -1, -1, -1, -1, -1, -1]]
        assert_array_almost_equal(out, expected)

    def test_watershed_ift07(self):
        shape = (7, 6)
        data = numpy.zeros(shape, dtype=numpy.uint8)
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        data = data.transpose()
        data[...] = numpy.array([[0, 1, 0, 0, 0, 1, 0],
                                    [0, 1, 0, 0, 0, 1, 0],
                                    [0, 1, 0, 0, 0, 1, 0],
                                    [0, 1, 1, 1, 1, 1, 0],
                                    [0, 0, 0, 0, 0, 0, 0],
                                    [0, 0, 0, 0, 0, 0, 0]], numpy.uint8)
        markers = numpy.array([[-1, 0, 0, 0, 0, 0, 0],
<<<<<<< HEAD
                                  [ 0, 0, 0, 1, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0],
                                  [ 0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = numpy.zeros(shape, dtype = numpy.int16)
        out = out.transpose()
        ndimage.watershed_ift(data, markers,
                               structure = [[1,1,1],
                                            [1,1,1],
                                            [1,1,1]],
                               output = out)
        error = diff([[-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1,  1,  1,  1,  1,  1, -1],
                      [-1, -1, -1, -1, -1, -1, -1],
                      [-1, -1, -1, -1, -1, -1, -1]], out)
        self.failUnless(error < eps)

    def test_label01(self):
        "label 1"
        data = numpy.ones([])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, 1) < eps and n == 1)

    def test_label02(self):
        "label 2"
        data = numpy.zeros([])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, 0) < eps and n == 0)

    def test_label03(self):
        "label 3"
        data = numpy.ones([1])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, [1]) < eps and n == 1)

    def test_label04(self):
        "label 4"
        data = numpy.zeros([1])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, [0]) < eps and n == 0)

    def test_label05(self):
        "label 5"
        data = numpy.ones([5])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, [1, 1, 1, 1, 1]) < eps and n == 1)

    def test_label06(self):
        "label 6"
        data = numpy.array([1, 0, 1, 1, 0, 1])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, [1, 0, 2, 2, 0, 3]) < eps and n == 3)

    def test_label07(self):
        "label 7"
        data = numpy.array([[0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0]])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, [[0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0]]) < eps and n == 0)

    def test_label08(self):
        "label 8"
        data = numpy.array([[1, 0, 0, 0, 0, 0],
                               [0, 0, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0],
                               [1, 1, 0, 0, 0, 0],
                               [1, 1, 0, 0, 0, 0],
                               [0, 0, 0, 1, 1, 0]])
        out, n = ndimage.label(data)
        self.failUnless(diff(out, [[1, 0, 0, 0, 0, 0],
                                   [0, 0, 2, 2, 0, 0],
                                   [0, 0, 2, 2, 2, 0],
                                   [3, 3, 0, 0, 0, 0],
                                   [3, 3, 0, 0, 0, 0],
                                   [0, 0, 0, 4, 4, 0]]) < eps and n == 4)

    def test_label09(self):
        "label 9"
        data = numpy.array([[1, 0, 0, 0, 0, 0],
                               [0, 0, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0],
                               [1, 1, 0, 0, 0, 0],
                               [1, 1, 0, 0, 0, 0],
                               [0, 0, 0, 1, 1, 0]])
        struct = ndimage.generate_binary_structure(2, 2)
        out, n = ndimage.label(data, struct)
        self.failUnless(diff(out, [[1, 0, 0, 0, 0, 0],
                                   [0, 0, 2, 2, 0, 0],
                                   [0, 0, 2, 2, 2, 0],
                                   [2, 2, 0, 0, 0, 0],
                                   [2, 2, 0, 0, 0, 0],
                                   [0, 0, 0, 3, 3, 0]]) < eps and n == 3)

    def test_label10(self):
        "label 10"
        data = numpy.array([[0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 0, 1, 0],
                               [0, 1, 1, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0]])
        struct = ndimage.generate_binary_structure(2, 2)
        out, n = ndimage.label(data, struct)
        self.failUnless(diff(out, [[0, 0, 0, 0, 0, 0],
                                   [0, 1, 1, 0, 1, 0],
                                   [0, 1, 1, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0]]) < eps and n == 1)

    def test_label11(self):
        "label 11"
        for type in self.types:
            data = numpy.array([[1, 0, 0, 0, 0, 0],
                                   [0, 0, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 0],
                                   [1, 1, 0, 0, 0, 0],
                                   [1, 1, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 0]], type)
            out, n = ndimage.label(data)
            error = diff(out, [[1, 0, 0, 0, 0, 0],
                               [0, 0, 2, 2, 0, 0],
                               [0, 0, 2, 2, 2, 0],
                               [3, 3, 0, 0, 0, 0],
                               [3, 3, 0, 0, 0, 0],
                               [0, 0, 0, 4, 4, 0]])
            self.failUnless(error < eps and n == 4)

    def test_label12(self):
        "label 12"
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 1, 1],
                                   [0, 0, 0, 0, 0, 1],
                                   [0, 0, 1, 0, 1, 1],
                                   [0, 0, 1, 1, 1, 1],
                                   [0, 0, 0, 1, 1, 0]], type)
            out, n = ndimage.label(data)
            error = diff(out, [[0, 0, 0, 0, 1, 1],
                               [0, 0, 0, 0, 0, 1],
                               [0, 0, 1, 0, 1, 1],
                               [0, 0, 1, 1, 1, 1],
                               [0, 0, 0, 1, 1, 0]])
            self.failUnless(error < eps and n == 1)

    def test_label13(self):
        "label 13"
        for type in self.types:
            data = numpy.array([[1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1],
                                   [1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1],
                                   [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
                                   [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]],
                                  type)
            out, n = ndimage.label(data)
            error = diff(out, [[1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1],
                               [1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1],
                               [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
                               [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]])
            self.failUnless(error < eps and n == 1)

    def test_find_objects01(self):
        "find_objects 1"
        data = numpy.ones([], dtype=int)
        out = ndimage.find_objects(data)
        self.failUnless(out == [()])

    def test_find_objects02(self):
        "find_objects 2"
        data = numpy.zeros([], dtype=int)
        out = ndimage.find_objects(data)
        self.failUnless(out == [])

    def test_find_objects03(self):
        "find_objects 3"
        data = numpy.ones([1], dtype=int)
        out = ndimage.find_objects(data)
        self.failUnless(out == [(slice(0, 1, None),)])

    def test_find_objects04(self):
        "find_objects 4"
        data = numpy.zeros([1], dtype=int)
        out = ndimage.find_objects(data)
        self.failUnless(out == [])

    def test_find_objects05(self):
        "find_objects 5"
        data = numpy.ones([5], dtype=int)
        out = ndimage.find_objects(data)
        self.failUnless(out == [(slice(0, 5, None),)])

    def test_find_objects06(self):
        "find_objects 6"
        data = numpy.array([1, 0, 2, 2, 0, 3])
        out = ndimage.find_objects(data)
        self.failUnless(out == [(slice(0, 1, None),),
                                (slice(2, 4, None),),
                                (slice(5, 6, None),)])

    def test_find_objects07(self):
        "find_objects 7"
        data = numpy.array([[0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0]])
        out = ndimage.find_objects(data)
        self.failUnless(out == []),

    def test_find_objects08(self):
        "find_objects 8"
        data = numpy.array([[1, 0, 0, 0, 0, 0],
                               [0, 0, 2, 2, 0, 0],
                               [0, 0, 2, 2, 2, 0],
                               [3, 3, 0, 0, 0, 0],
                               [3, 3, 0, 0, 0, 0],
                               [0, 0, 0, 4, 4, 0]])
        out = ndimage.find_objects(data)
        self.failUnless(out == [(slice(0, 1, None), slice(0, 1, None)),
                                (slice(1, 3, None), slice(2, 5, None)),
                                (slice(3, 5, None), slice(0, 2, None)),
                                (slice(5, 6, None), slice(3, 5, None))])

    def test_find_objects09(self):
        "find_objects 9"
        data = numpy.array([[1, 0, 0, 0, 0, 0],
                               [0, 0, 2, 2, 0, 0],
                               [0, 0, 2, 2, 2, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 4, 4, 0]])
        out = ndimage.find_objects(data)
        self.failUnless(out == [(slice(0, 1, None), slice(0, 1, None)),
                                (slice(1, 3, None), slice(2, 5, None)),
                                None,
                                (slice(5, 6, None), slice(3, 5, None))])

    def test_sum01(self):
        "sum 1"
        for type in self.types:
            input = numpy.array([], type)
            output = ndimage.sum(input)
            self.failUnless(output == 0.0)

    def test_sum02(self):
        "sum 2"
        for type in self.types:
            input = numpy.zeros([0, 4], type)
            output = ndimage.sum(input)
            self.failUnless(output == 0.0)

    def test_sum03(self):
        "sum 3"
        for type in self.types:
            input = numpy.ones([], type)
            output = ndimage.sum(input)
            self.failUnless(output == 1.0)

    def test_sum04(self):
        "sum 4"
        for type in self.types:
            input = numpy.array([1, 2], type)
            output = ndimage.sum(input)
            self.failUnless(output == 3.0)

    def test_sum05(self):
        "sum 5"
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.sum(input)
            self.failUnless(output == 10.0)

    def test_sum06(self):
        "sum 6"
        labels = numpy.array([], bool)
        for type in self.types:
            input = numpy.array([], type)
            output = ndimage.sum(input, labels = labels)
            self.failUnless(output == 0.0)

    def test_sum07(self):
        "sum 7"
        labels = numpy.ones([0, 4], bool)
        for type in self.types:
            input = numpy.zeros([0, 4], type)
            output = ndimage.sum(input, labels = labels)
            self.failUnless(output == 0.0)

    def test_sum08(self):
        "sum 8"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([1, 2], type)
            output = ndimage.sum(input, labels = labels)
            self.failUnless(output == 1.0)

    def test_sum09(self):
        "sum 9"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.sum(input, labels = labels)
            self.failUnless(output == 4.0)

    def test_sum10(self):
        "sum 10"
        labels = numpy.array([1, 0], bool)
        input = numpy.array([[1, 2], [3, 4]], bool)
        output = ndimage.sum(input, labels = labels)
        self.failUnless(output == 2.0)

    def test_sum11(self):
        "sum 11"
        labels = numpy.array([1, 2], numpy.int8)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.sum(input, labels = labels,
                                           index = 2)
            self.failUnless(output == 6.0)

    def test_sum12(self):
        "sum 12"
        labels = numpy.array([[1, 2], [2, 4]], numpy.int8)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.sum(input, labels = labels,
                                            index = [4, 8, 2])
            self.failUnless(numpy.all(output == [4.0, 0.0, 5.0]))

    def test_mean01(self):
        "mean 1"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.mean(input, labels = labels)
            self.failUnless(output == 2.0)

    def test_mean02(self):
        "mean 2"
        labels = numpy.array([1, 0], bool)
        input = numpy.array([[1, 2], [3, 4]], bool)
        output = ndimage.mean(input, labels = labels)
        self.failUnless(output == 1.0)

    def test_mean03(self):
        "mean 3"
        labels = numpy.array([1, 2])
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.mean(input, labels = labels,
                                            index = 2)
            self.failUnless(output == 3.0)

    def test_mean04(self):
        "mean 4"
        labels = numpy.array([[1, 2], [2, 4]], numpy.int8)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.mean(input, labels = labels,
                                            index = [4, 8, 2])
            self.failUnless(numpy.all(output[[0,2]] == [4.0, 2.5]) and
                            numpy.isnan(output[1]))

    def test_minimum01(self):
        "minimum 1"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.minimum(input, labels = labels)
            self.failUnless(output == 1.0)

    def test_minimum02(self):
        "minimum 2"
        labels = numpy.array([1, 0], bool)
        input = numpy.array([[2, 2], [2, 4]], bool)
        output = ndimage.minimum(input, labels = labels)
        self.failUnless(output == 1.0)

    def test_minimum03(self):
        "minimum 3"
        labels = numpy.array([1, 2])
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.minimum(input, labels = labels,
                                               index = 2)
            self.failUnless(output == 2.0)

    def test_minimum04(self):
        "minimum 4"
        labels = numpy.array([[1, 2], [2, 3]])
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.minimum(input, labels = labels,
                                               index = [2, 3, 8])
            self.failUnless(numpy.all(output == [2.0, 4.0, 0.0]))

    def test_maximum01(self):
        "maximum 1"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.maximum(input, labels = labels)
            self.failUnless(output == 3.0)

    def test_maximum02(self):
        "maximum 2"
        labels = numpy.array([1, 0], bool)
        input = numpy.array([[2, 2], [2, 4]], bool)
        output = ndimage.maximum(input, labels = labels)
        self.failUnless(output == 1.0)

    def test_maximum03(self):
        "maximum 3"
        labels = numpy.array([1, 2])
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.maximum(input, labels = labels,
                                               index = 2)
            self.failUnless(output == 4.0)

    def test_maximum04(self):
        "maximum 4"
        labels = numpy.array([[1, 2], [2, 3]])
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.maximum(input, labels = labels,
                                               index = [2, 3, 8])
            self.failUnless(numpy.all(output == [3.0, 4.0, 0.0]))

    def test_maximum05(self):
        "Ticket #501"
        x = numpy.array([-3,-2,-1])
        assert_equal(ndimage.maximum(x),-1)

    def test_variance01(self):
        "variance 1"
        for type in self.types:
            input = numpy.array([], type)
            output = ndimage.variance(input)
            self.failUnless(numpy.isnan(output))

    def test_variance02(self):
        "variance 2"
        for type in self.types:
            input = numpy.array([1], type)
            output = ndimage.variance(input)
            self.failUnless(float(output) == 0.0)

    def test_variance03(self):
        "variance 3"
        for type in self.types:
            input = numpy.array([1, 3], type)
            output = ndimage.variance(input)
            self.failUnless(output == 1.0)

    def test_variance04(self):
        "variance 4"
        input = numpy.array([1, 0], bool)
        output = ndimage.variance(input)
        self.failUnless(output == 0.25)

    def test_variance05(self):
        "variance 5"
        labels = [2, 2, 3]
        for type in self.types:
            input = numpy.array([1, 3, 8], type)
            output = ndimage.variance(input, labels, 2)
            self.failUnless(output == 1.0)

    def test_variance06(self):
        "variance 6"
        labels = [2, 2, 3, 3, 4]
        for type in self.types:
            input = numpy.array([1, 3, 8, 10, 8], type)
            output = ndimage.variance(input, labels, [2, 3, 4])
            self.failUnless(numpy.all(output == [1.0, 1.0, 0.0]))

    def test_standard_deviation01(self):
        "standard deviation 1"
        for type in self.types:
            input = numpy.array([], type)
            output = ndimage.standard_deviation(input)
            self.failUnless(numpy.isnan(output))

    def test_standard_deviation02(self):
        "standard deviation 2"
        for type in self.types:
            input = numpy.array([1], type)
            output = ndimage.standard_deviation(input)
            self.failUnless(float(output) == 0.0)

    def test_standard_deviation03(self):
        "standard deviation 3"
        for type in self.types:
            input = numpy.array([1, 3], type)
            output = ndimage.standard_deviation(input)
            self.failUnless(output == math.sqrt(1.0))

    def test_standard_deviation04(self):
        "standard deviation 4"
        input = numpy.array([1, 0], bool)
        output = ndimage.standard_deviation(input)
        self.failUnless(output == 0.5)

    def test_standard_deviation05(self):
        "standard deviation 5"
        labels = [2, 2, 3]
        for type in self.types:
            input = numpy.array([1, 3, 8], type)
            output = ndimage.standard_deviation(input, labels, 2)
            self.failUnless(output == 1.0)

    def test_standard_deviation06(self):
        "standard deviation 6"
        labels = [2, 2, 3, 3, 4]
        for type in self.types:
            input = numpy.array([1, 3, 8, 10, 8], type)
            output = ndimage.standard_deviation(input, labels,
                                                          [2, 3, 4])
            self.failUnless(np.all(output == [1.0, 1.0, 0.0]))

    def test_minimum_position01(self):
        "minimum position 1"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.minimum_position(input,
                                                        labels = labels)
            self.failUnless(output == (0, 0))

    def test_minimum_position02(self):
        "minimum position 2"
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 0, 2],
                                    [1, 5, 1, 1]], type)
            output = ndimage.minimum_position(input)
            self.failUnless(output == (1, 2))

    def test_minimum_position03(self):
        "minimum position 3"
        input = numpy.array([[5, 4, 2, 5],
                                [3, 7, 0, 2],
                                [1, 5, 1, 1]], bool)
        output = ndimage.minimum_position(input)
        self.failUnless(output == (1, 2))

    def test_minimum_position04(self):
        "minimum position 4"
        input = numpy.array([[5, 4, 2, 5],
                                [3, 7, 1, 2],
                                [1, 5, 1, 1]], bool)
        output = ndimage.minimum_position(input)
        self.failUnless(output == (0, 0))

    def test_minimum_position05(self):
        "minimum position 5"
        labels = [1, 2, 0, 4]
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 0, 2],
                                    [1, 5, 2, 3]], type)
            output = ndimage.minimum_position(input, labels)
            self.failUnless(output == (2, 0))

    def test_minimum_position06(self):
        "minimum position 6"
        labels = [1, 2, 3, 4]
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 0, 2],
                                    [1, 5, 1, 1]], type)
            output = ndimage.minimum_position(input, labels, 2)
            self.failUnless(output == (0, 1))

    def test_minimum_position07(self):
        "minimum position 7"
        labels = [1, 2, 3, 4]
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 0, 2],
                                    [1, 5, 1, 1]], type)
            output = ndimage.minimum_position(input, labels,
                                                        [2, 3])
            self.failUnless(output[0] == (0, 1) and output[1] == (1, 2))

    def test_maximum_position01(self):
        "maximum position 1"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output = ndimage.maximum_position(input,
                                                        labels = labels)
            self.failUnless(output == (1, 0))

    def test_maximum_position02(self):
        "maximum position 2"
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 8, 2],
                                    [1, 5, 1, 1]], type)
            output = ndimage.maximum_position(input)
            self.failUnless(output == (1, 2))

    def test_maximum_position03(self):
        "maximum position 3"
        input = numpy.array([[5, 4, 2, 5],
                                [3, 7, 8, 2],
                                [1, 5, 1, 1]], bool)
        output = ndimage.maximum_position(input)
        self.failUnless(output == (0, 0))

    def test_maximum_position04(self):
        "maximum position 4"
        labels = [1, 2, 0, 4]
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 8, 2],
                                    [1, 5, 1, 1]], type)
            output = ndimage.maximum_position(input, labels)
            self.failUnless(output == (1, 1))

    def test_maximum_position05(self):
        "maximum position 5"
        labels = [1, 2, 0, 4]
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 8, 2],
                                    [1, 5, 1, 1]], type)
            output = ndimage.maximum_position(input, labels, 1)
            self.failUnless(output == (0, 0))

    def test_maximum_position06(self):
        "maximum position 6"
        labels = [1, 2, 0, 4]
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 8, 2],
                                    [1, 5, 1, 1]], type)
            output = ndimage.maximum_position(input, labels,
                                                        [1, 2])
            self.failUnless(output[0] == (0, 0) and output[1] == (1, 1))

    def test_extrema01(self):
        "extrema 1"
        labels = numpy.array([1, 0], bool)
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output1 = ndimage.extrema(input, labels = labels)
            output2 = ndimage.minimum(input, labels = labels)
            output3 = ndimage.maximum(input, labels = labels)
            output4 = ndimage.minimum_position(input,
                                                         labels = labels)
            output5 = ndimage.maximum_position(input,
                                                         labels = labels)
            self.failUnless(output1 == (output2, output3, output4,
                                        output5))

    def test_extrema02(self):
        "extrema 2"
        labels = numpy.array([1, 2])
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output1 = ndimage.extrema(input, labels = labels,
                                                index = 2)
            output2 = ndimage.minimum(input, labels = labels,
                                                index = 2)
            output3 = ndimage.maximum(input, labels = labels,
                                                index = 2)
            output4 = ndimage.minimum_position(input,
                                                labels = labels, index = 2)
            output5 = ndimage.maximum_position(input,
                                                labels = labels, index = 2)
            self.failUnless(output1 == (output2, output3, output4,
                                        output5))

    def test_extrema03(self):
        "extrema 3"
        labels = numpy.array([[1, 2], [2, 3]])
        for type in self.types:
            input = numpy.array([[1, 2], [3, 4]], type)
            output1 = ndimage.extrema(input, labels = labels,
                                                index = [2, 3, 8])
            output2 = ndimage.minimum(input, labels = labels,
                                                index = [2, 3, 8])
            output3 = ndimage.maximum(input, labels = labels,
                                                index = [2, 3, 8])
            output4 = ndimage.minimum_position(input,
                                        labels = labels, index = [2, 3, 8])
            output5 = ndimage.maximum_position(input,
                                        labels = labels, index = [2, 3, 8])
            self.failUnless(numpy.all(output1[0] == output2))
            self.failUnless(numpy.all(output1[1] == output3))
            self.failUnless(numpy.all(output1[2]  == output4))
            self.failUnless(numpy.all(output1[3]  == output5))

    def test_extrema04(self):
        "extrema 4"
        labels = [1, 2, 0, 4]
        for type in self.types:
            input = numpy.array([[5, 4, 2, 5],
                                    [3, 7, 8, 2],
                                    [1, 5, 1, 1]], type)
            output1 = ndimage.extrema(input, labels, [1, 2])
            output2 = ndimage.minimum(input, labels, [1, 2])
            output3 = ndimage.maximum(input, labels, [1, 2])
            output4 = ndimage.minimum_position(input, labels,
                                                         [1, 2])
            output5 = ndimage.maximum_position(input, labels,
                                                         [1, 2])
            self.failUnless(numpy.all(output1[0] == output2))
            self.failUnless(numpy.all(output1[1] == output3))
            self.failUnless(numpy.all(output1[2] == output4))
            self.failUnless(numpy.all(output1[3] == output5))

    def test_center_of_mass01(self):
        "center of mass 1"
        true = [0.0, 0.0]
        for type in self.types:
            input = numpy.array([[1, 0], [0, 0]], type)
            output = ndimage.center_of_mass(input)
            e = diff(true, output)
            self.failUnless(e < eps)

    def test_center_of_mass02(self):
        "center of mass 2"
        true = [1, 0]
        for type in self.types:
            input = numpy.array([[0, 0], [1, 0]], type)
            output = ndimage.center_of_mass(input)
            e = diff(true, output)
            self.failUnless(e < eps)

    def test_center_of_mass03(self):
        "center of mass 3"
        true = [0, 1]
        for type in self.types:
            input = numpy.array([[0, 1], [0, 0]], type)
            output = ndimage.center_of_mass(input)
            e = diff(true, output)
            self.failUnless(e < eps)

    def test_center_of_mass04(self):
        "center of mass 4"
        true = [1, 1]
        for type in self.types:
            input = numpy.array([[0, 0], [0, 1]], type)
            output = ndimage.center_of_mass(input)
            e = diff(true, output)
            self.failUnless(e < eps)

    def test_center_of_mass05(self):
        "center of mass 5"
        true = [0.5, 0.5]
        for type in self.types:
            input = numpy.array([[1, 1], [1, 1]], type)
            output = ndimage.center_of_mass(input)
            e = diff(true, output)
            self.failUnless(e < eps)

    def test_center_of_mass06(self):
        "center of mass 6"
        true = [0.5, 0.5]
        input = numpy.array([[1, 2], [3, 1]], bool)
        output = ndimage.center_of_mass(input)
        e = diff(true, output)
        self.failUnless(e < eps)

    def test_center_of_mass07(self):
        "center of mass 7"
        labels = [1, 0]
        true = [0.5, 0.0]
        input = numpy.array([[1, 2], [3, 1]], bool)
        output = ndimage.center_of_mass(input, labels)
        e = diff(true, output)
        self.failUnless(e < eps)

    def test_center_of_mass08(self):
        "center of mass 8"
        labels = [1, 2]
        true = [0.5, 1.0]
        input = numpy.array([[5, 2], [3, 1]], bool)
        output = ndimage.center_of_mass(input, labels, 2)
        e = diff(true, output)
        self.failUnless(e < eps)


    def test_center_of_mass09(self):
        "center of mass 9"
        labels = [1, 2]
        true = [(0.5, 0.0), (0.5, 1.0)]
        input = numpy.array([[1, 2], [1, 1]], bool)
        output = ndimage.center_of_mass(input, labels, [1, 2])
        e = diff(true, output)
        self.failUnless(e < eps)

    def test_histogram01(self):
        "histogram 1"
        true = numpy.ones(10)
        input = numpy.arange(10)
        output = ndimage.histogram(input, 0, 10, 10)
        e = diff(true, output)
        self.failUnless(e < eps)

    def test_histogram02(self):
        "histogram 2"
        labels = [1, 1, 1, 1, 2, 2, 2, 2]
        true = [0, 2, 0, 1, 1]
        input = numpy.array([1, 1, 3, 4, 3, 3, 3, 3])
        output = ndimage.histogram(input, 0, 4, 5, labels, 1)
        e = diff(true, output)
        self.failUnless(e < eps)

    def test_histogram03(self):
        "histogram 3"
        labels = [1, 0, 1, 1, 2, 2, 2, 2]
        true1 = [0, 1, 0, 1, 1]
        true2 = [0, 0, 0, 3, 0]
        input = numpy.array([1, 1, 3, 4, 3, 5, 3, 3])
        output = ndimage.histogram(input, 0, 4, 5, labels, (1,2))
        e1 = diff(true1, output[0])
        e2 = diff(true2, output[1])
        self.failUnless(e1 < eps and e2 < eps)

    def test_distance_transform_bf01(self):
        "brute force distance transform 1"
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_bf(data,
                                        'euclidean', return_indices = True)
        error1 = diff([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 1, 1, 1, 0, 0, 0],
                       [0, 0, 1, 2, 4, 2, 1, 0, 0],
                       [0, 0, 1, 4, 8, 4, 1, 0, 0],
                       [0, 0, 1, 2, 4, 2, 1, 0, 0],
                       [0, 0, 0, 1, 1, 1, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0]],
                                    out * out)
        error2 = diff([[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [1, 1, 1, 1, 1, 1, 1, 1, 1],
                        [2, 2, 2, 2, 1, 2, 2, 2, 2],
                        [3, 3, 3, 2, 1, 2, 3, 3, 3],
                        [4, 4, 4, 4, 6, 4, 4, 4, 4],
                        [5, 5, 6, 6, 7, 6, 6, 5, 5],
                        [6, 6, 6, 7, 7, 7, 6, 6, 6],
                        [7, 7, 7, 7, 7, 7, 7, 7, 7],
                        [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                       [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 2, 4, 6, 6, 7, 8],
                        [0, 1, 1, 2, 4, 6, 7, 7, 8],
                        [0, 1, 1, 1, 6, 7, 7, 7, 8],
                        [0, 1, 2, 2, 4, 6, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8]]], ft)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_bf02(self):
        "brute force distance transform 2"
=======
                                  [0, 0, 0, 1, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0],
                                  [0, 0, 0, 0, 0, 0, 0]],
                                 numpy.int8)
        out = numpy.zeros(shape, dtype=numpy.int16)
        out = out.transpose()
        ndimage.watershed_ift(data, markers,
                               structure=[[1,1,1],
                                          [1,1,1],
                                          [1,1,1]],
                               output=out)
        expected = [[-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, 1, 1, 1, 1, 1, -1],
                    [-1, -1, -1, -1, -1, -1, -1],
                    [-1, -1, -1, -1, -1, -1, -1]]
        assert_array_almost_equal(out, expected)

    def test_distance_transform_bf01(self):
        # brute force (bf) distance transform
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_bf(data, 'euclidean',
                                                return_indices=True)
        expected = [[0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 1, 1, 1, 0, 0, 0],
                    [0, 0, 1, 2, 4, 2, 1, 0, 0],
                    [0, 0, 1, 4, 8, 4, 1, 0, 0],
                    [0, 0, 1, 2, 4, 2, 1, 0, 0],
                    [0, 0, 0, 1, 1, 1, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0]]
        assert_array_almost_equal(out * out, expected)

        expected = [[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                     [1, 1, 1, 1, 1, 1, 1, 1, 1],
                     [2, 2, 2, 2, 1, 2, 2, 2, 2],
                     [3, 3, 3, 2, 1, 2, 3, 3, 3],
                     [4, 4, 4, 4, 6, 4, 4, 4, 4],
                     [5, 5, 6, 6, 7, 6, 6, 5, 5],
                     [6, 6, 6, 7, 7, 7, 6, 6, 6],
                     [7, 7, 7, 7, 7, 7, 7, 7, 7],
                     [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                    [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 2, 4, 6, 6, 7, 8],
                     [0, 1, 1, 2, 4, 6, 7, 7, 8],
                     [0, 1, 1, 1, 6, 7, 7, 7, 8],
                     [0, 1, 2, 2, 4, 6, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8]]]
        assert_array_almost_equal(ft, expected)

    def test_distance_transform_bf02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
<<<<<<< HEAD
        out, ft = ndimage.distance_transform_bf(data,
                                        'cityblock', return_indices = True)
        error1 = diff([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 1, 1, 1, 0, 0, 0],
                       [0, 0, 1, 2, 2, 2, 1, 0, 0],
                       [0, 0, 1, 2, 3, 2, 1, 0, 0],
                       [0, 0, 1, 2, 2, 2, 1, 0, 0],
                       [0, 0, 0, 1, 1, 1, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0]], out)
        error2 = diff([[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [1, 1, 1, 1, 1, 1, 1, 1, 1],
                        [2, 2, 2, 2, 1, 2, 2, 2, 2],
                        [3, 3, 3, 3, 1, 3, 3, 3, 3],
                        [4, 4, 4, 4, 7, 4, 4, 4, 4],
                        [5, 5, 6, 7, 7, 7, 6, 5, 5],
                        [6, 6, 6, 7, 7, 7, 6, 6, 6],
                        [7, 7, 7, 7, 7, 7, 7, 7, 7],
                        [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                       [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 2, 4, 6, 6, 7, 8],
                        [0, 1, 1, 1, 4, 7, 7, 7, 8],
                        [0, 1, 1, 1, 4, 7, 7, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8]]], ft)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_bf03(self):
        "brute force distance transform 3"
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_bf(data,
                                    'chessboard', return_indices = True)
        error1 = diff([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 1, 1, 1, 0, 0, 0],
                       [0, 0, 1, 1, 2, 1, 1, 0, 0],
                       [0, 0, 1, 2, 2, 2, 1, 0, 0],
                       [0, 0, 1, 1, 2, 1, 1, 0, 0],
                       [0, 0, 0, 1, 1, 1, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0]], out)
        error2 = diff([[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [1, 1, 1, 1, 1, 1, 1, 1, 1],
                        [2, 2, 2, 2, 1, 2, 2, 2, 2],
                        [3, 3, 4, 2, 2, 2, 4, 3, 3],
                        [4, 4, 5, 6, 6, 6, 5, 4, 4],
                        [5, 5, 6, 6, 7, 6, 6, 5, 5],
                        [6, 6, 6, 7, 7, 7, 6, 6, 6],
                        [7, 7, 7, 7, 7, 7, 7, 7, 7],
                        [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                       [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 2, 5, 6, 6, 7, 8],
                        [0, 1, 1, 2, 6, 6, 7, 7, 8],
                        [0, 1, 1, 2, 6, 7, 7, 7, 8],
                        [0, 1, 2, 2, 6, 6, 7, 7, 8],
                        [0, 1, 2, 4, 5, 6, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8]]], ft)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_bf04(self):
        "brute force distance transform 4"
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        tdt, tft = ndimage.distance_transform_bf(data,
                                                    return_indices = 1)
        dts = []
        fts = []
        dt = numpy.zeros(data.shape, dtype = numpy.float64)
        ndimage.distance_transform_bf(data, distances = dt)
        dts.append(dt)
        ft = ndimage.distance_transform_bf(data,
                            return_distances = False, return_indices = 1)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        ndimage.distance_transform_bf(data,
             return_distances = False, return_indices = True, indices = ft)
        fts.append(ft)
        dt, ft = ndimage.distance_transform_bf(data,
                                                       return_indices = 1)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype = numpy.float64)
        ft = ndimage.distance_transform_bf(data, distances = dt,
                                                     return_indices = True)
        dts.append(dt)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        dt = ndimage.distance_transform_bf(data,
                                       return_indices = True, indices = ft)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype = numpy.float64)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        ndimage.distance_transform_bf(data, distances = dt,
                                       return_indices = True, indices = ft)
        dts.append(dt)
        fts.append(ft)
        for dt in dts:
            self.failUnless(diff(tdt, dt) < eps)
        for ft in fts:
            self.failUnless(diff(tft, ft) < eps)

    def test_distance_transform_bf05(self):
        "brute force distance transform 5"
=======
        out, ft = ndimage.distance_transform_bf(data, 'cityblock',
                                                return_indices=True)

        expected = [[0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 1, 1, 1, 0, 0, 0],
                    [0, 0, 1, 2, 2, 2, 1, 0, 0],
                    [0, 0, 1, 2, 3, 2, 1, 0, 0],
                    [0, 0, 1, 2, 2, 2, 1, 0, 0],
                    [0, 0, 0, 1, 1, 1, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0]]
        assert_array_almost_equal(out, expected)

        expected = [[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                     [1, 1, 1, 1, 1, 1, 1, 1, 1],
                     [2, 2, 2, 2, 1, 2, 2, 2, 2],
                     [3, 3, 3, 3, 1, 3, 3, 3, 3],
                     [4, 4, 4, 4, 7, 4, 4, 4, 4],
                     [5, 5, 6, 7, 7, 7, 6, 5, 5],
                     [6, 6, 6, 7, 7, 7, 6, 6, 6],
                     [7, 7, 7, 7, 7, 7, 7, 7, 7],
                     [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                    [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 2, 4, 6, 6, 7, 8],
                     [0, 1, 1, 1, 4, 7, 7, 7, 8],
                     [0, 1, 1, 1, 4, 7, 7, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8]]]
        assert_array_almost_equal(expected, ft)

    def test_distance_transform_bf03(self):
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_bf(data, 'chessboard',
                                                return_indices=True)

        expected = [[0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 1, 1, 1, 0, 0, 0],
                    [0, 0, 1, 1, 2, 1, 1, 0, 0],
                    [0, 0, 1, 2, 2, 2, 1, 0, 0],
                    [0, 0, 1, 1, 2, 1, 1, 0, 0],
                    [0, 0, 0, 1, 1, 1, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0]]
        assert_array_almost_equal(out, expected)

        expected = [[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                     [1, 1, 1, 1, 1, 1, 1, 1, 1],
                     [2, 2, 2, 2, 1, 2, 2, 2, 2],
                     [3, 3, 4, 2, 2, 2, 4, 3, 3],
                     [4, 4, 5, 6, 6, 6, 5, 4, 4],
                     [5, 5, 6, 6, 7, 6, 6, 5, 5],
                     [6, 6, 6, 7, 7, 7, 6, 6, 6],
                     [7, 7, 7, 7, 7, 7, 7, 7, 7],
                     [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                    [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 2, 5, 6, 6, 7, 8],
                     [0, 1, 1, 2, 6, 6, 7, 7, 8],
                     [0, 1, 1, 2, 6, 7, 7, 7, 8],
                     [0, 1, 2, 2, 6, 6, 7, 7, 8],
                     [0, 1, 2, 4, 5, 6, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8]]]
        assert_array_almost_equal(ft, expected)

    def test_distance_transform_bf04(self):
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        tdt, tft = ndimage.distance_transform_bf(data,
                                                 return_indices=1)
        dts = []
        fts = []
        dt = numpy.zeros(data.shape, dtype=numpy.float64)
        ndimage.distance_transform_bf(data, distances=dt)
        dts.append(dt)
        ft = ndimage.distance_transform_bf(data,
                            return_distances=False, return_indices=1)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        ndimage.distance_transform_bf(data,
             return_distances=False, return_indices=True, indices=ft)
        fts.append(ft)
        dt, ft = ndimage.distance_transform_bf(data,
                                                       return_indices=1)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype=numpy.float64)
        ft = ndimage.distance_transform_bf(data, distances=dt,
                                                     return_indices=True)
        dts.append(dt)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        dt = ndimage.distance_transform_bf(data,
                                       return_indices=True, indices=ft)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype=numpy.float64)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        ndimage.distance_transform_bf(data, distances=dt,
                                       return_indices=True, indices=ft)
        dts.append(dt)
        fts.append(ft)
        for dt in dts:
            assert_array_almost_equal(tdt, dt)
        for ft in fts:
            assert_array_almost_equal(tft, ft)

    def test_distance_transform_bf05(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_bf(data,
<<<<<<< HEAD
                     'euclidean', return_indices = True, sampling = [2, 2])
        error1 = diff([[0, 0, 0,  0,  0,  0, 0, 0, 0],
                       [0, 0, 0,  0,  0,  0, 0, 0, 0],
                       [0, 0, 0,  4,  4,  4, 0, 0, 0],
                       [0, 0, 4,  8, 16,  8, 4, 0, 0],
                       [0, 0, 4, 16, 32, 16, 4, 0, 0],
                       [0, 0, 4,  8, 16,  8, 4, 0, 0],
                       [0, 0, 0,  4,  4,  4, 0, 0, 0],
                       [0, 0, 0,  0,  0,  0, 0, 0, 0],
                       [0, 0, 0,  0,  0,  0, 0, 0, 0]], out * out)
        error2 = diff([[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [1, 1, 1, 1, 1, 1, 1, 1, 1],
                        [2, 2, 2, 2, 1, 2, 2, 2, 2],
                        [3, 3, 3, 2, 1, 2, 3, 3, 3],
                        [4, 4, 4, 4, 6, 4, 4, 4, 4],
                        [5, 5, 6, 6, 7, 6, 6, 5, 5],
                        [6, 6, 6, 7, 7, 7, 6, 6, 6],
                        [7, 7, 7, 7, 7, 7, 7, 7, 7],
                        [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                       [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 2, 4, 6, 6, 7, 8],
                        [0, 1, 1, 2, 4, 6, 7, 7, 8],
                        [0, 1, 1, 1, 6, 7, 7, 7, 8],
                        [0, 1, 2, 2, 4, 6, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8]]], ft)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_bf06(self):
        "brute force distance transform 6"
=======
                     'euclidean', return_indices=True, sampling=[2, 2])
        expected = [[0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 4, 4, 4, 0, 0, 0],
                    [0, 0, 4, 8, 16, 8, 4, 0, 0],
                    [0, 0, 4, 16, 32, 16, 4, 0, 0],
                    [0, 0, 4, 8, 16, 8, 4, 0, 0],
                    [0, 0, 0, 4, 4, 4, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0]]
        assert_array_almost_equal(out * out, expected)

        expected = [[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                     [1, 1, 1, 1, 1, 1, 1, 1, 1],
                     [2, 2, 2, 2, 1, 2, 2, 2, 2],
                     [3, 3, 3, 2, 1, 2, 3, 3, 3],
                     [4, 4, 4, 4, 6, 4, 4, 4, 4],
                     [5, 5, 6, 6, 7, 6, 6, 5, 5],
                     [6, 6, 6, 7, 7, 7, 6, 6, 6],
                     [7, 7, 7, 7, 7, 7, 7, 7, 7],
                     [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                    [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 2, 4, 6, 6, 7, 8],
                     [0, 1, 1, 2, 4, 6, 7, 7, 8],
                     [0, 1, 1, 1, 6, 7, 7, 7, 8],
                     [0, 1, 2, 2, 4, 6, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8]]]
        assert_array_almost_equal(ft, expected)

    def test_distance_transform_bf06(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_bf(data,
<<<<<<< HEAD
                     'euclidean', return_indices = True, sampling = [2, 1])
        error1 = diff([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 1, 4, 1, 0, 0, 0],
                       [0, 0, 1, 4, 8, 4, 1, 0, 0],
                       [0, 0, 1, 4, 9, 4, 1, 0, 0],
                       [0, 0, 1, 4, 8, 4, 1, 0, 0],
                       [0, 0, 0, 1, 4, 1, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0],
                       [0, 0, 0, 0, 0, 0, 0, 0, 0]], out * out)
        error2 = diff([[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [1, 1, 1, 1, 1, 1, 1, 1, 1],
                        [2, 2, 2, 2, 2, 2, 2, 2, 2],
                        [3, 3, 3, 3, 2, 3, 3, 3, 3],
                        [4, 4, 4, 4, 4, 4, 4, 4, 4],
                        [5, 5, 5, 5, 6, 5, 5, 5, 5],
                        [6, 6, 6, 6, 7, 6, 6, 6, 6],
                        [7, 7, 7, 7, 7, 7, 7, 7, 7],
                        [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                       [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 2, 6, 6, 6, 7, 8],
                        [0, 1, 1, 1, 6, 7, 7, 7, 8],
                        [0, 1, 1, 1, 7, 7, 7, 7, 8],
                        [0, 1, 1, 1, 6, 7, 7, 7, 8],
                        [0, 1, 2, 2, 4, 6, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8]]], ft)
        self.failUnless(error1 < eps and error2 < eps)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_cdt01(self):
        "chamfer type distance transform 1"
=======
                     'euclidean', return_indices=True, sampling=[2, 1])
        expected = [[0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 1, 4, 1, 0, 0, 0],
                    [0, 0, 1, 4, 8, 4, 1, 0, 0],
                    [0, 0, 1, 4, 9, 4, 1, 0, 0],
                    [0, 0, 1, 4, 8, 4, 1, 0, 0],
                    [0, 0, 0, 1, 4, 1, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0],
                    [0, 0, 0, 0, 0, 0, 0, 0, 0]]
        assert_array_almost_equal(out * out, expected)

        expected = [[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                     [1, 1, 1, 1, 1, 1, 1, 1, 1],
                     [2, 2, 2, 2, 2, 2, 2, 2, 2],
                     [3, 3, 3, 3, 2, 3, 3, 3, 3],
                     [4, 4, 4, 4, 4, 4, 4, 4, 4],
                     [5, 5, 5, 5, 6, 5, 5, 5, 5],
                     [6, 6, 6, 6, 7, 6, 6, 6, 6],
                     [7, 7, 7, 7, 7, 7, 7, 7, 7],
                     [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                    [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 2, 6, 6, 6, 7, 8],
                     [0, 1, 1, 1, 6, 7, 7, 7, 8],
                     [0, 1, 1, 1, 7, 7, 7, 7, 8],
                     [0, 1, 1, 1, 6, 7, 7, 7, 8],
                     [0, 1, 2, 2, 4, 6, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8]]]
        assert_array_almost_equal(ft, expected)

    def test_distance_transform_cdt01(self):
        #chamfer type distance (cdt) transform
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_cdt(data,
<<<<<<< HEAD
                                        'cityblock', return_indices = True)
        bf = ndimage.distance_transform_bf(data, 'cityblock')
        error1 = diff(bf, out)
        error2 = diff([[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [1, 1, 1, 1, 1, 1, 1, 1, 1],
                        [2, 2, 2, 1, 1, 1, 2, 2, 2],
                        [3, 3, 2, 1, 1, 1, 2, 3, 3],
                        [4, 4, 4, 4, 1, 4, 4, 4, 4],
                        [5, 5, 5, 5, 7, 7, 6, 5, 5],
                        [6, 6, 6, 6, 7, 7, 6, 6, 6],
                        [7, 7, 7, 7, 7, 7, 7, 7, 7],
                        [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                       [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 1, 1, 4, 7, 7, 7, 8],
                        [0, 1, 1, 1, 4, 5, 6, 7, 8],
                        [0, 1, 2, 2, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],]], ft)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_cdt02(self):
        "chamfer type distance transform 2"
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_cdt(data,
                                       'chessboard', return_indices = True)
        bf = ndimage.distance_transform_bf(data, 'chessboard')
        error1 = diff(bf, out)
        error2 = diff([[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [1, 1, 1, 1, 1, 1, 1, 1, 1],
                        [2, 2, 2, 1, 1, 1, 2, 2, 2],
                        [3, 3, 2, 2, 1, 2, 2, 3, 3],
                        [4, 4, 3, 2, 2, 2, 3, 4, 4],
                        [5, 5, 4, 6, 7, 6, 4, 5, 5],
                        [6, 6, 6, 6, 7, 7, 6, 6, 6],
                        [7, 7, 7, 7, 7, 7, 7, 7, 7],
                        [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                       [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 2, 3, 4, 6, 7, 8],
                        [0, 1, 1, 2, 2, 6, 6, 7, 8],
                        [0, 1, 1, 1, 2, 6, 7, 7, 8],
                        [0, 1, 1, 2, 6, 6, 7, 7, 8],
                        [0, 1, 2, 2, 5, 6, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        [0, 1, 2, 3, 4, 5, 6, 7, 8],]], ft)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_cdt03(self):
        "chamfer type distance transform 3"
=======
                                        'cityblock', return_indices=True)
        bf = ndimage.distance_transform_bf(data, 'cityblock')
        assert_array_almost_equal(bf, out)

        expected = [[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                     [1, 1, 1, 1, 1, 1, 1, 1, 1],
                     [2, 2, 2, 1, 1, 1, 2, 2, 2],
                     [3, 3, 2, 1, 1, 1, 2, 3, 3],
                     [4, 4, 4, 4, 1, 4, 4, 4, 4],
                     [5, 5, 5, 5, 7, 7, 6, 5, 5],
                     [6, 6, 6, 6, 7, 7, 6, 6, 6],
                     [7, 7, 7, 7, 7, 7, 7, 7, 7],
                     [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                    [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 1, 1, 4, 7, 7, 7, 8],
                     [0, 1, 1, 1, 4, 5, 6, 7, 8],
                     [0, 1, 2, 2, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],]]
        assert_array_almost_equal(ft, expected)

    def test_distance_transform_cdt02(self):
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_cdt(data, 'chessboard',
                                                 return_indices=True)
        bf = ndimage.distance_transform_bf(data, 'chessboard')
        assert_array_almost_equal(bf, out)

        expected = [[[0, 0, 0, 0, 0, 0, 0, 0, 0],
                     [1, 1, 1, 1, 1, 1, 1, 1, 1],
                     [2, 2, 2, 1, 1, 1, 2, 2, 2],
                     [3, 3, 2, 2, 1, 2, 2, 3, 3],
                     [4, 4, 3, 2, 2, 2, 3, 4, 4],
                     [5, 5, 4, 6, 7, 6, 4, 5, 5],
                     [6, 6, 6, 6, 7, 7, 6, 6, 6],
                     [7, 7, 7, 7, 7, 7, 7, 7, 7],
                     [8, 8, 8, 8, 8, 8, 8, 8, 8]],
                    [[0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 2, 3, 4, 6, 7, 8],
                     [0, 1, 1, 2, 2, 6, 6, 7, 8],
                     [0, 1, 1, 1, 2, 6, 7, 7, 8],
                     [0, 1, 1, 2, 6, 6, 7, 7, 8],
                     [0, 1, 2, 2, 5, 6, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],
                     [0, 1, 2, 3, 4, 5, 6, 7, 8],]]
        assert_array_almost_equal(ft, expected)

    def test_distance_transform_cdt03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        tdt, tft = ndimage.distance_transform_cdt(data,
<<<<<<< HEAD
                                                     return_indices = True)
        dts = []
        fts = []
        dt = numpy.zeros(data.shape, dtype = numpy.int32)
        ndimage.distance_transform_cdt(data, distances = dt)
        dts.append(dt)
        ft = ndimage.distance_transform_cdt(data,
                           return_distances = False, return_indices = True)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        ndimage.distance_transform_cdt(data,
             return_distances = False, return_indices = True, indices = ft)
        fts.append(ft)
        dt, ft = ndimage.distance_transform_cdt(data,
                                                     return_indices = True)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype = numpy.int32)
        ft = ndimage.distance_transform_cdt(data, distances = dt,
                                                     return_indices = True)
        dts.append(dt)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        dt = ndimage.distance_transform_cdt(data,
                                       return_indices = True, indices = ft)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype = numpy.int32)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        ndimage.distance_transform_cdt(data, distances = dt,
                                       return_indices = True, indices = ft)
        dts.append(dt)
        fts.append(ft)
        for dt in dts:
            self.failUnless(diff(tdt, dt) < eps)
        for ft in fts:
            self.failUnless(diff(tft, ft) < eps)

    def test_distance_transform_edt01(self):
        "euclidean distance transform 1"
=======
                                                     return_indices=True)
        dts = []
        fts = []
        dt = numpy.zeros(data.shape, dtype=numpy.int32)
        ndimage.distance_transform_cdt(data, distances=dt)
        dts.append(dt)
        ft = ndimage.distance_transform_cdt(data,
                           return_distances=False, return_indices=True)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        ndimage.distance_transform_cdt(data,
             return_distances=False, return_indices=True, indices=ft)
        fts.append(ft)
        dt, ft = ndimage.distance_transform_cdt(data,
                                                     return_indices=True)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype=numpy.int32)
        ft = ndimage.distance_transform_cdt(data, distances=dt,
                                                     return_indices=True)
        dts.append(dt)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        dt = ndimage.distance_transform_cdt(data,
                                       return_indices=True, indices=ft)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype=numpy.int32)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        ndimage.distance_transform_cdt(data, distances=dt,
                                       return_indices=True, indices=ft)
        dts.append(dt)
        fts.append(ft)
        for dt in dts:
            assert_array_almost_equal(tdt, dt)
        for ft in fts:
            assert_array_almost_equal(tft, ft)

    def test_distance_transform_edt01(self):
        #euclidean distance transform (edt)
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        out, ft = ndimage.distance_transform_edt(data,
<<<<<<< HEAD
                                                     return_indices = True)
        bf = ndimage.distance_transform_bf(data, 'euclidean')

        error1 = diff(bf, out)
        dt = ft - numpy.indices(ft.shape[1:], dtype = ft.dtype)
        dt = dt.astype(numpy.float64)
        numpy.multiply(dt, dt, dt)
        dt = numpy.add.reduce(dt, axis = 0)
        numpy.sqrt(dt, dt)
        error2 = diff(bf, dt)
        self.failUnless(error1 < eps and error2 < eps)

    def test_distance_transform_edt02(self):
        "euclidean distance transform 2"
=======
                                                     return_indices=True)
        bf = ndimage.distance_transform_bf(data, 'euclidean')
        assert_array_almost_equal(bf, out)

        dt = ft - numpy.indices(ft.shape[1:], dtype=ft.dtype)
        dt = dt.astype(numpy.float64)
        numpy.multiply(dt, dt, dt)
        dt = numpy.add.reduce(dt, axis=0)
        numpy.sqrt(dt, dt)

        assert_array_almost_equal(bf, dt)

    def test_distance_transform_edt02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        tdt, tft = ndimage.distance_transform_edt(data,
<<<<<<< HEAD
                                                     return_indices = True)
        dts = []
        fts = []
        dt = numpy.zeros(data.shape, dtype = numpy.float64)
        ndimage.distance_transform_edt(data, distances = dt)
        dts.append(dt)
        ft = ndimage.distance_transform_edt(data,
                               return_distances = 0, return_indices = True)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        ndimage.distance_transform_edt(data,
              return_distances = False,return_indices = True, indices = ft)
        fts.append(ft)
        dt, ft = ndimage.distance_transform_edt(data,
                                                     return_indices = True)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype = numpy.float64)
        ft = ndimage.distance_transform_edt(data, distances = dt,
                                                     return_indices = True)
        dts.append(dt)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        dt = ndimage.distance_transform_edt(data,
                                       return_indices = True, indices = ft)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype = numpy.float64)
        ft = numpy.indices(data.shape, dtype = numpy.int32)
        ndimage.distance_transform_edt(data, distances = dt,
                                       return_indices = True, indices = ft)
        dts.append(dt)
        fts.append(ft)
        for dt in dts:
            self.failUnless(diff(tdt, dt) < eps)
        for ft in fts:
            self.failUnless(diff(tft, ft) < eps)

    def test_distance_transform_edt03(self):
        "euclidean distance transform 3"
=======
                                                     return_indices=True)
        dts = []
        fts = []
        dt = numpy.zeros(data.shape, dtype=numpy.float64)
        ndimage.distance_transform_edt(data, distances=dt)
        dts.append(dt)
        ft = ndimage.distance_transform_edt(data,
                               return_distances=0, return_indices=True)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        ndimage.distance_transform_edt(data,
              return_distances=False,return_indices=True, indices=ft)
        fts.append(ft)
        dt, ft = ndimage.distance_transform_edt(data,
                                                     return_indices=True)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype=numpy.float64)
        ft = ndimage.distance_transform_edt(data, distances=dt,
                                                     return_indices=True)
        dts.append(dt)
        fts.append(ft)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        dt = ndimage.distance_transform_edt(data,
                                       return_indices=True, indices=ft)
        dts.append(dt)
        fts.append(ft)
        dt = numpy.zeros(data.shape, dtype=numpy.float64)
        ft = numpy.indices(data.shape, dtype=numpy.int32)
        ndimage.distance_transform_edt(data, distances=dt,
                                       return_indices=True, indices=ft)
        dts.append(dt)
        fts.append(ft)
        for dt in dts:
            assert_array_almost_equal(tdt, dt)
        for ft in fts:
            assert_array_almost_equal(tft, ft)

    def test_distance_transform_edt03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        ref = ndimage.distance_transform_bf(data, 'euclidean',
<<<<<<< HEAD
                                                      sampling = [2, 2])
        out = ndimage.distance_transform_edt(data,
                                                       sampling = [2, 2])
        self.failUnless(diff(ref, out) < eps)


    def test_distance_transform_edt4(self):
        "euclidean distance transform 4"
=======
                                                      sampling=[2, 2])
        out = ndimage.distance_transform_edt(data,
                                                       sampling=[2, 2])
        assert_array_almost_equal(ref, out)

    def test_distance_transform_edt4(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0, 0]], type)
        ref = ndimage.distance_transform_bf(data, 'euclidean',
<<<<<<< HEAD
                                                      sampling = [2, 1])
        out = ndimage.distance_transform_edt(data,
                                                       sampling = [2, 1])
        self.failUnless(diff(ref, out) < eps)

    def test_generate_structure01(self):
        "generation of a binary structure 1"
        struct = ndimage.generate_binary_structure(0, 1)
        self.failUnless(diff(struct, 1) < eps)

    def test_generate_structure02(self):
        "generation of a binary structure 2"
        struct = ndimage.generate_binary_structure(1, 1)
        self.failUnless(diff(struct, [1, 1, 1]) < eps)

    def test_generate_structure03(self):
        "generation of a binary structure 3"
        struct = ndimage.generate_binary_structure(2, 1)
        self.failUnless(diff(struct, [[0, 1, 0],
                                      [1, 1, 1],
                                      [0, 1, 0]]) < eps)

    def test_generate_structure04(self):
        "generation of a binary structure 4"
        struct = ndimage.generate_binary_structure(2, 2)
        self.failUnless(diff(struct, [[1, 1, 1],
                                      [1, 1, 1],
                                      [1, 1, 1]]) < eps)

    def test_iterate_structure01(self):
        "iterating a structure 1"
=======
                                                      sampling=[2, 1])
        out = ndimage.distance_transform_edt(data,
                                                       sampling=[2, 1])
        assert_array_almost_equal(ref, out)

    def test_distance_transform_edt5(self):
        #Ticket #954 regression test
        out = ndimage.distance_transform_edt(False)
        assert_array_almost_equal(out, [0.])

    def test_generate_structure01(self):
        struct = ndimage.generate_binary_structure(0, 1)
        assert_array_almost_equal(struct, 1)

    def test_generate_structure02(self):
        struct = ndimage.generate_binary_structure(1, 1)
        assert_array_almost_equal(struct, [1, 1, 1])

    def test_generate_structure03(self):
        struct = ndimage.generate_binary_structure(2, 1)
        assert_array_almost_equal(struct, [[0, 1, 0],
                                      [1, 1, 1],
                                      [0, 1, 0]])

    def test_generate_structure04(self):
        struct = ndimage.generate_binary_structure(2, 2)
        assert_array_almost_equal(struct, [[1, 1, 1],
                                      [1, 1, 1],
                                      [1, 1, 1]])

    def test_iterate_structure01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        out = ndimage.iterate_structure(struct, 2)
<<<<<<< HEAD
        self.failUnless(diff(out, [[0, 0, 1, 0, 0],
                                   [0, 1, 1, 1, 0],
                                   [1, 1, 1, 1, 1],
                                   [0, 1, 1, 1, 0],
                                   [0, 0, 1, 0, 0]]) < eps)

    def test_iterate_structure02(self):
        "iterating a structure 2"
=======
        assert_array_almost_equal(out, [[0, 0, 1, 0, 0],
                                   [0, 1, 1, 1, 0],
                                   [1, 1, 1, 1, 1],
                                   [0, 1, 1, 1, 0],
                                   [0, 0, 1, 0, 0]])

    def test_iterate_structure02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        struct = [[0, 1],
                  [1, 1],
                  [0, 1]]
        out = ndimage.iterate_structure(struct, 2)
<<<<<<< HEAD
        self.failUnless(diff(out, [[0, 0, 1],
                                   [0, 1, 1],
                                   [1, 1, 1],
                                   [0, 1, 1],
                                   [0, 0, 1]]) < eps)

    def test_iterate_structure03(self):
        "iterating a structure 3"
=======
        assert_array_almost_equal(out, [[0, 0, 1],
                                   [0, 1, 1],
                                   [1, 1, 1],
                                   [0, 1, 1],
                                   [0, 0, 1]])

    def test_iterate_structure03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        out = ndimage.iterate_structure(struct, 2, 1)
<<<<<<< HEAD
        error = diff(out[0], [[0, 0, 1, 0, 0],
                              [0, 1, 1, 1, 0],
                              [1, 1, 1, 1, 1],
                              [0, 1, 1, 1, 0],
                              [0, 0, 1, 0, 0]])
        self.failUnless(error < eps and out[1] == [2, 2])

    def test_binary_erosion01(self):
        "binary erosion 1"
        for type in self.types:
            data = numpy.ones([], type)
            out = ndimage.binary_erosion(data)
            self.failUnless(diff(out, 1) < eps)

    def test_binary_erosion02(self):
        "binary erosion 2"
        for type in self.types:
            data = numpy.ones([], type)
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, 1) < eps)

    def test_binary_erosion03(self):
        "binary erosion 3"
        for type in self.types:
            data = numpy.ones([1], type)
            out = ndimage.binary_erosion(data)
            self.failUnless(diff(out, [0]) < eps)

    def test_binary_erosion04(self):
        "binary erosion 4"
        for type in self.types:
            data = numpy.ones([1], type)
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, [1]) < eps)

    def test_binary_erosion05(self):
        "binary erosion 5"
        for type in self.types:
            data = numpy.ones([3], type)
            out = ndimage.binary_erosion(data)
            self.failUnless(diff(out, [0, 1, 0]) < eps)

    def test_binary_erosion06(self):
        "binary erosion 6"
        for type in self.types:
            data = numpy.ones([3], type)
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, [1, 1, 1]) < eps)

    def test_binary_erosion07(self):
        "binary erosion 7"
        for type in self.types:
            data = numpy.ones([5], type)
            out = ndimage.binary_erosion(data)
            self.failUnless(diff(out, [0, 1, 1, 1, 0]) < eps)

    def test_binary_erosion08(self):
        "binary erosion 8"
        for type in self.types:
            data = numpy.ones([5], type)
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, [1, 1, 1, 1, 1]) < eps)

    def test_binary_erosion09(self):
        "binary erosion 9"
=======
        expected = [[0, 0, 1, 0, 0],
                    [0, 1, 1, 1, 0],
                    [1, 1, 1, 1, 1],
                    [0, 1, 1, 1, 0],
                    [0, 0, 1, 0, 0]]
        assert_array_almost_equal(out[0], expected)
        assert_equal(out[1], [2, 2])

    def test_binary_erosion01(self):
        for type in self.types:
            data = numpy.ones([], type)
            out = ndimage.binary_erosion(data)
            assert_array_almost_equal(out, 1)

    def test_binary_erosion02(self):
        for type in self.types:
            data = numpy.ones([], type)
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, 1)

    def test_binary_erosion03(self):
        for type in self.types:
            data = numpy.ones([1], type)
            out = ndimage.binary_erosion(data)
            assert_array_almost_equal(out, [0])

    def test_binary_erosion04(self):
        for type in self.types:
            data = numpy.ones([1], type)
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, [1])

    def test_binary_erosion05(self):
        for type in self.types:
            data = numpy.ones([3], type)
            out = ndimage.binary_erosion(data)
            assert_array_almost_equal(out, [0, 1, 0])

    def test_binary_erosion06(self):
        for type in self.types:
            data = numpy.ones([3], type)
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, [1, 1, 1])

    def test_binary_erosion07(self):
        for type in self.types:
            data = numpy.ones([5], type)
            out = ndimage.binary_erosion(data)
            assert_array_almost_equal(out, [0, 1, 1, 1, 0])

    def test_binary_erosion08(self):
        for type in self.types:
            data = numpy.ones([5], type)
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, [1, 1, 1, 1, 1])

    def test_binary_erosion09(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            out = ndimage.binary_erosion(data)
<<<<<<< HEAD
            self.failUnless(diff(out, [0, 0, 0, 0, 0]) < eps)

    def test_binary_erosion10(self):
        "binary erosion 10"
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, [1, 0, 0, 0, 1]) < eps)

    def test_binary_erosion11(self):
        "binary erosion 11"
=======
            assert_array_almost_equal(out, [0, 0, 0, 0, 0])

    def test_binary_erosion10(self):
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, [1, 0, 0, 0, 1])

    def test_binary_erosion11(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            struct = [1, 0, 1]
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1)
            self.failUnless(diff(out, [1, 0, 1, 0, 1]) < eps)

    def test_binary_erosion12(self):
        "binary erosion 12"
=======
                                                   border_value=1)
            assert_array_almost_equal(out, [1, 0, 1, 0, 1])

    def test_binary_erosion12(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            struct = [1, 0, 1]
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1,
                                                   origin = -1)
            self.failUnless(diff(out, [0, 1, 0, 1, 1]) < eps)

    def test_binary_erosion13(self):
        "binary erosion 13"
=======
                                                   border_value=1,
                                                   origin=-1)
            assert_array_almost_equal(out, [0, 1, 0, 1, 1])

    def test_binary_erosion13(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            struct = [1, 0, 1]
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1,
                                                   origin = 1)
            self.failUnless(diff(out, [1, 1, 0, 1, 0]) < eps)

    def test_binary_erosion14(self):
        "binary erosion 14"
=======
                                                   border_value=1,
                                                   origin=1)
            assert_array_almost_equal(out, [1, 1, 0, 1, 0])

    def test_binary_erosion14(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            struct = [1, 1]
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1)
            self.failUnless(diff(out, [1, 1, 0, 0, 1]) < eps)

    def test_binary_erosion15(self):
        "binary erosion 15"
=======
                                                   border_value=1)
            assert_array_almost_equal(out, [1, 1, 0, 0, 1])

    def test_binary_erosion15(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.ones([5], type)
            data[2] = 0
            struct = [1, 1]
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1,
                                                   origin = -1)
            self.failUnless(diff(out, [1, 0, 0, 1, 1]) < eps)

    def test_binary_erosion16(self):
        "binary erosion 16"
        for type in self.types:
            data = numpy.ones([1, 1], type)
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, [[1]]) < eps)

    def test_binary_erosion17(self):
        "binary erosion 17"
        for type in self.types:
            data = numpy.ones([1, 1], type)
            out = ndimage.binary_erosion(data)
            self.failUnless(diff(out, [[0]]) < eps)

    def test_binary_erosion18(self):
        "binary erosion 18"
        for type in self.types:
            data = numpy.ones([1, 3], type)
            out = ndimage.binary_erosion(data)
            self.failUnless(diff(out, [[0, 0, 0]]) < eps)

    def test_binary_erosion19(self):
        "binary erosion 19"
        for type in self.types:
            data = numpy.ones([1, 3], type)
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, [[1, 1, 1]]) < eps)

    def test_binary_erosion20(self):
        "binary erosion 20"
        for type in self.types:
            data = numpy.ones([3, 3], type)
            out = ndimage.binary_erosion(data)
            self.failUnless(diff(out, [[0, 0, 0],
                                       [0, 1, 0],
                                       [0, 0, 0]]) < eps)

    def test_binary_erosion21(self):
        "binary erosion 21"
        for type in self.types:
            data = numpy.ones([3, 3], type)
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, [[1, 1, 1],
                                       [1, 1, 1],
                                       [1, 1, 1]]) < eps)

    def test_binary_erosion22(self):
        "binary erosion 22"
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
                                                   border_value=1,
                                                   origin=-1)
            assert_array_almost_equal(out, [1, 0, 0, 1, 1])

    def test_binary_erosion16(self):
        for type in self.types:
            data = numpy.ones([1, 1], type)
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, [[1]])

    def test_binary_erosion17(self):
        for type in self.types:
            data = numpy.ones([1, 1], type)
            out = ndimage.binary_erosion(data)
            assert_array_almost_equal(out, [[0]])

    def test_binary_erosion18(self):
        for type in self.types:
            data = numpy.ones([1, 3], type)
            out = ndimage.binary_erosion(data)
            assert_array_almost_equal(out, [[0, 0, 0]])

    def test_binary_erosion19(self):
        for type in self.types:
            data = numpy.ones([1, 3], type)
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, [[1, 1, 1]])

    def test_binary_erosion20(self):
        for type in self.types:
            data = numpy.ones([3, 3], type)
            out = ndimage.binary_erosion(data)
            assert_array_almost_equal(out, [[0, 0, 0],
                                       [0, 1, 0],
                                       [0, 0, 0]])

    def test_binary_erosion21(self):
        for type in self.types:
            data = numpy.ones([3, 3], type)
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, [[1, 1, 1],
                                       [1, 1, 1],
                                       [1, 1, 1]])

    def test_binary_erosion22(self):
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 1, 0, 0],
                [0, 0, 0, 1, 1, 0, 0, 0],
                [0, 0, 1, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 1, 0, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 0, 0, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
<<<<<<< HEAD
            out = ndimage.binary_erosion(data, border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_erosion23(self):
        "binary erosion 23"
        struct = ndimage.generate_binary_structure(2, 2)
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
            out = ndimage.binary_erosion(data, border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_erosion23(self):
        struct = ndimage.generate_binary_structure(2, 2)
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 1, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 1, 0, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 0, 0, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_erosion24(self):
        "binary erosion 24"
        struct = [[0, 1],
                  [1, 1]]
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
                                                   border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_erosion24(self):
        struct = [[0, 1],
                  [1, 1]]
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 1, 1, 1],
                [0, 0, 0, 1, 1, 1, 0, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 0, 1, 0, 0, 0, 1, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 1, 0, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 0, 0, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_erosion25(self):
        "binary erosion 25"
        struct = [[0, 1, 0],
                  [1, 0, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
                                                   border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_erosion25(self):
        struct = [[0, 1, 0],
                  [1, 0, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 1, 0, 0],
                [0, 0, 0, 1, 0, 0, 0, 0],
                [0, 0, 1, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 0, 1, 1],
                                   [0, 0, 1, 0, 1, 1, 0, 0],
                                   [0, 1, 0, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 0, 0, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                                   border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_erosion26(self):
        "binary erosion 26"
        struct = [[0, 1, 0],
                  [1, 0, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
                                                   border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_erosion26(self):
        struct = [[0, 1, 0],
                  [1, 0, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0, 1],
                [0, 0, 0, 0, 1, 0, 0, 1],
                [0, 0, 1, 0, 0, 0, 0, 0],
                [0, 1, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 1]]
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 1, 1],
                                   [0, 0, 1, 1, 1, 0, 1, 1],
                                   [0, 0, 1, 0, 1, 1, 0, 0],
                                   [0, 1, 0, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 0, 0, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                      border_value = 1, origin = (-1, -1))
            self.failUnless(diff(out, true) < eps)

    def test_binary_erosion27(self):
        "binary erosion 27"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0],
=======
                                      border_value=1, origin=(-1, -1))
            assert_array_almost_equal(out, expected)

    def test_binary_erosion27(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                         border_value = 1, iterations = 2)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion28(self):
        "binary erosion 28"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0],
=======
                                         border_value=1, iterations=2)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion28(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], bool)
        out = numpy.zeros(data.shape, bool)
<<<<<<< HEAD
        ndimage.binary_erosion(data, struct, border_value = 1,
                                         iterations = 2, output = out)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion29(self):
        "binary erosion 29"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0],
=======
        ndimage.binary_erosion(data, struct, border_value=1,
                                         iterations=2, output=out)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion29(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        data = numpy.array([[0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [1, 1, 1, 1, 1, 1, 1],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0]], bool)
        out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                         border_value = 1, iterations = 3)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion30(self):
        "binary erosion 30"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0],
=======
                                         border_value=1, iterations=3)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion30(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        data = numpy.array([[0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [1, 1, 1, 1, 1, 1, 1],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0]], bool)
        out = numpy.zeros(data.shape, bool)
<<<<<<< HEAD
        ndimage.binary_erosion(data, struct, border_value = 1,
                                         iterations = 3, output = out)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion31(self):
        "binary erosion 31"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 1, 0, 0, 0, 0],
=======
        ndimage.binary_erosion(data, struct, border_value=1,
                                         iterations=3, output=out)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion31(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 1, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 1, 1, 1, 0, 0, 0],
                [1, 1, 1, 1, 1, 0, 1],
                [0, 1, 1, 1, 0, 0, 0],
                [0, 0, 1, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 1, 0, 0, 0, 1]]
        data = numpy.array([[0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [1, 1, 1, 1, 1, 1, 1],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0]], bool)
        out = numpy.zeros(data.shape, bool)
<<<<<<< HEAD
        ndimage.binary_erosion(data, struct, border_value = 1,
                          iterations = 1, output = out, origin = (-1, -1))
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion32(self):
        "binary erosion 32"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0],
=======
        ndimage.binary_erosion(data, struct, border_value=1,
                          iterations=1, output=out, origin=(-1, -1))
        assert_array_almost_equal(out, expected)

    def test_binary_erosion32(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                         border_value = 1, iterations = 2)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion33(self):
        "binary erosion 33"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 1, 1],
=======
                                         border_value=1, iterations=2)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion33(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 1, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 1],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        mask = [[1, 1, 1, 1, 1, 0, 0],
                [1, 1, 1, 1, 1, 1, 0],
                [1, 1, 1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1, 1, 1]]
        data = numpy.array([[0, 0, 0, 0, 0, 1, 1],
                               [0, 0, 0, 1, 0, 0, 1],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                            border_value = 1, mask = mask, iterations = -1)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion34(self):
        "binary erosion 34"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0],
=======
                            border_value=1, mask=mask, iterations=-1)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion34(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 1, 1, 1, 1, 1, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        mask = [[0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 1, 1, 1, 0, 0],
                [0, 0, 1, 0, 1, 0, 0],
                [0, 0, 1, 1, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_erosion(data, struct,
<<<<<<< HEAD
                                            border_value = 1, mask = mask)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion35(self):
        "binary erosion 35"
=======
                                            border_value=1, mask=mask)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion35(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        mask = [[0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 1, 1, 1, 0, 0],
                [0, 0, 1, 0, 1, 0, 0],
                [0, 0, 1, 1, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0]]
        data = numpy.array([[0, 0, 0, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 1, 1, 1, 1, 1, 0],
                               [1, 1, 1, 1, 1, 1, 1],
                               [0, 1, 1, 1, 1, 1, 0],
                               [0, 0, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 0, 0, 0]], bool)
        tmp = [[0, 0, 1, 0, 0, 0, 0],
               [0, 1, 1, 1, 0, 0, 0],
               [1, 1, 1, 1, 1, 0, 1],
               [0, 1, 1, 1, 0, 0, 0],
               [0, 0, 1, 0, 0, 0, 0],
               [0, 0, 0, 0, 0, 0, 0],
               [0, 0, 1, 0, 0, 0, 1]]
<<<<<<< HEAD
        true = numpy.logical_and(tmp, mask)
        tmp = numpy.logical_and(data, numpy.logical_not(mask))
        true = numpy.logical_or(true, tmp)
        out = numpy.zeros(data.shape, bool)
        ndimage.binary_erosion(data, struct, border_value = 1,
                                         iterations = 1, output = out,
                                         origin = (-1, -1), mask = mask)
        self.failUnless(diff(out, true) < eps)

    def test_binary_erosion36(self):
        "binary erosion 36"
=======
        expected = numpy.logical_and(tmp, mask)
        tmp = numpy.logical_and(data, numpy.logical_not(mask))
        expected = numpy.logical_or(expected, tmp)
        out = numpy.zeros(data.shape, bool)
        ndimage.binary_erosion(data, struct, border_value=1,
                                         iterations=1, output=out,
                                         origin=(-1, -1), mask=mask)
        assert_array_almost_equal(out, expected)

    def test_binary_erosion36(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        struct = [[0, 1, 0],
                  [1, 0, 1],
                  [0, 1, 0]]
        mask = [[0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 1, 1, 1, 0, 0, 0],
                [0, 0, 1, 0, 1, 0, 0, 0],
                [0, 0, 1, 1, 1, 0, 0, 0],
                [0, 0, 1, 1, 1, 0, 0, 0],
                [0, 0, 1, 1, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        tmp = [[0, 0, 0, 0, 0, 0, 0, 0],
               [0, 0, 0, 0, 0, 0, 0, 1],
               [0, 0, 0, 0, 1, 0, 0, 1],
               [0, 0, 1, 0, 0, 0, 0, 0],
               [0, 1, 0, 0, 1, 0, 0, 0],
               [0, 0, 0, 0, 0, 0, 0, 0],
               [0, 0, 0, 0, 0, 0, 0, 0],
               [0, 0, 0, 0, 0, 0, 0, 1]]
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 1, 1, 1],
                               [0, 0, 1, 1, 1, 0, 1, 1],
                               [0, 0, 1, 0, 1, 1, 0, 0],
                               [0, 1, 0, 1, 1, 1, 1, 0],
                               [0, 1, 1, 0, 0, 1, 1, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]])
<<<<<<< HEAD
        true = numpy.logical_and(tmp, mask)
        tmp = numpy.logical_and(data, numpy.logical_not(mask))
        true = numpy.logical_or(true, tmp)
        out = ndimage.binary_erosion(data, struct, mask = mask,
                                       border_value = 1, origin = (-1, -1))
        self.failUnless(diff(out, true) < eps)

    def test_binary_dilation01(self):
        "binary dilation 1"
        for type in self.types:
            data = numpy.ones([], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, 1) < eps)

    def test_binary_dilation02(self):
        "binary dilation 2"
        for type in self.types:
            data = numpy.zeros([], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, 0) < eps)

    def test_binary_dilation03(self):
        "binary dilation 3"
        for type in self.types:
            data = numpy.ones([1], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [1]) < eps)

    def test_binary_dilation04(self):
        "binary dilation 4"
        for type in self.types:
            data = numpy.zeros([1], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [0]) < eps)

    def test_binary_dilation05(self):
        "binary dilation 5"
        for type in self.types:
            data = numpy.ones([3], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [1, 1, 1]) < eps)

    def test_binary_dilation06(self):
        "binary dilation 6"
        for type in self.types:
            data = numpy.zeros([3], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [0, 0, 0]) < eps)

    def test_binary_dilation07(self):
        "binary dilation 7"
        struct = ndimage.generate_binary_structure(1, 1)
=======
        expected = numpy.logical_and(tmp, mask)
        tmp = numpy.logical_and(data, numpy.logical_not(mask))
        expected = numpy.logical_or(expected, tmp)
        out = ndimage.binary_erosion(data, struct, mask=mask,
                                       border_value=1, origin=(-1, -1))
        assert_array_almost_equal(out, expected)

    def test_binary_dilation01(self):
        for type in self.types:
            data = numpy.ones([], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, 1)

    def test_binary_dilation02(self):
        for type in self.types:
            data = numpy.zeros([], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, 0)

    def test_binary_dilation03(self):
        for type in self.types:
            data = numpy.ones([1], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [1])

    def test_binary_dilation04(self):
        for type in self.types:
            data = numpy.zeros([1], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [0])

    def test_binary_dilation05(self):
        for type in self.types:
            data = numpy.ones([3], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [1, 1, 1])

    def test_binary_dilation06(self):
        for type in self.types:
            data = numpy.zeros([3], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [0, 0, 0])

    def test_binary_dilation07(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([3], type)
            data[1] = 1
            out = ndimage.binary_dilation(data)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 1, 1]) < eps)

    def test_binary_dilation08(self):
        "binary dilation 8"
=======
            assert_array_almost_equal(out, [1, 1, 1])

    def test_binary_dilation08(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            data[3] = 1
            out = ndimage.binary_dilation(data)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 1, 1, 1, 1]) < eps)

    def test_binary_dilation09(self):
        "binary dilation 9"
=======
            assert_array_almost_equal(out, [1, 1, 1, 1, 1])

    def test_binary_dilation09(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            out = ndimage.binary_dilation(data)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 1, 1, 0, 0]) < eps)

    def test_binary_dilation10(self):
        "binary dilation 10"
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            out = ndimage.binary_dilation(data, origin = -1)
            self.failUnless(diff(out, [0, 1, 1, 1, 0]) < eps)

    def test_binary_dilation11(self):
        "binary dilation 11"
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            out = ndimage.binary_dilation(data, origin = 1)
            self.failUnless(diff(out, [1, 1, 0, 0, 0]) < eps)

    def test_binary_dilation12(self):
        "binary dilation 12"
=======
            assert_array_almost_equal(out, [1, 1, 1, 0, 0])

    def test_binary_dilation10(self):
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            out = ndimage.binary_dilation(data, origin=-1)
            assert_array_almost_equal(out, [0, 1, 1, 1, 0])

    def test_binary_dilation11(self):
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            out = ndimage.binary_dilation(data, origin=1)
            assert_array_almost_equal(out, [1, 1, 0, 0, 0])

    def test_binary_dilation12(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            struct = [1, 0, 1]
            out = ndimage.binary_dilation(data, struct)
<<<<<<< HEAD
            self.failUnless(diff(out, [1, 0, 1, 0, 0]) < eps)

    def test_binary_dilation13(self):
        "binary dilation 13"
=======
            assert_array_almost_equal(out, [1, 0, 1, 0, 0])

    def test_binary_dilation13(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            struct = [1, 0, 1]
            out = ndimage.binary_dilation(data, struct,
<<<<<<< HEAD
                                                    border_value = 1)
            self.failUnless(diff(out, [1, 0, 1, 0, 1]) < eps)

    def test_binary_dilation14(self):
        "binary dilation 14"
=======
                                                    border_value=1)
            assert_array_almost_equal(out, [1, 0, 1, 0, 1])

    def test_binary_dilation14(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            struct = [1, 0, 1]
            out = ndimage.binary_dilation(data, struct,
<<<<<<< HEAD
                                                    origin = -1)
            self.failUnless(diff(out, [0, 1, 0, 1, 0]) < eps)

    def test_binary_dilation15(self):
        "binary dilation 15"
=======
                                                    origin=-1)
            assert_array_almost_equal(out, [0, 1, 0, 1, 0])

    def test_binary_dilation15(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([5], type)
            data[1] = 1
            struct = [1, 0, 1]
            out = ndimage.binary_dilation(data, struct,
<<<<<<< HEAD
                                            origin = -1, border_value = 1)
            self.failUnless(diff(out, [1, 1, 0, 1, 0]) < eps)

    def test_binary_dilation16(self):
        "binary dilation 16"
        for type in self.types:
            data = numpy.ones([1, 1], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [[1]]) < eps)

    def test_binary_dilation17(self):
        "binary dilation 17"
        for type in self.types:
            data = numpy.zeros([1, 1], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [[0]]) < eps)

    def test_binary_dilation18(self):
        "binary dilation 18"
        for type in self.types:
            data = numpy.ones([1, 3], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [[1, 1, 1]]) < eps)

    def test_binary_dilation19(self):
        "binary dilation 19"
        for type in self.types:
            data = numpy.ones([3, 3], type)
            out = ndimage.binary_dilation(data)
            self.failUnless(diff(out, [[1, 1, 1],
                               [1, 1, 1],
                               [1, 1, 1]]) < eps)

    def test_binary_dilation20(self):
        "binary dilation 20"
=======
                                            origin=-1, border_value=1)
            assert_array_almost_equal(out, [1, 1, 0, 1, 0])

    def test_binary_dilation16(self):
        for type in self.types:
            data = numpy.ones([1, 1], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [[1]])

    def test_binary_dilation17(self):
        for type in self.types:
            data = numpy.zeros([1, 1], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [[0]])

    def test_binary_dilation18(self):
        for type in self.types:
            data = numpy.ones([1, 3], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [[1, 1, 1]])

    def test_binary_dilation19(self):
        for type in self.types:
            data = numpy.ones([3, 3], type)
            out = ndimage.binary_dilation(data)
            assert_array_almost_equal(out, [[1, 1, 1],
                               [1, 1, 1],
                               [1, 1, 1]])

    def test_binary_dilation20(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.zeros([3, 3], type)
            data[1, 1] = 1
            out = ndimage.binary_dilation(data)
<<<<<<< HEAD
            self.failUnless(diff(out, [[0, 1, 0],
                                       [1, 1, 1],
                                       [0, 1, 0]]) < eps)

    def test_binary_dilation21(self):
        "binary dilation 21"
=======
            assert_array_almost_equal(out, [[0, 1, 0],
                                       [1, 1, 1],
                                       [0, 1, 0]])

    def test_binary_dilation21(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        struct = ndimage.generate_binary_structure(2, 2)
        for type in self.types:
            data = numpy.zeros([3, 3], type)
            data[1, 1] = 1
            out = ndimage.binary_dilation(data, struct)
<<<<<<< HEAD
            self.failUnless(diff(out, [[1, 1, 1],
                                       [1, 1, 1],
                                       [1, 1, 1]]) < eps)

    def test_binary_dilation22(self):
        "binary dilation 22"
        true = [[0, 1, 0, 0, 0, 0, 0, 0],
=======
            assert_array_almost_equal(out, [[1, 1, 1],
                                       [1, 1, 1],
                                       [1, 1, 1]])

    def test_binary_dilation22(self):
        expected = [[0, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 1, 1, 0, 0, 0, 0, 0],
                [0, 1, 0, 0, 0, 1, 0, 0],
                [0, 0, 0, 1, 1, 1, 1, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 0, 1, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]

        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                           [0, 1, 0, 0, 0, 0, 0, 0],
                                           [0, 0, 0, 0, 0, 0, 0, 0],
                                           [0, 0, 0, 0, 0, 1, 0, 0],
                                           [0, 0, 0, 1, 1, 0, 0, 0],
                                           [0, 0, 1, 0, 0, 1, 0, 0],
                                           [0, 0, 0, 0, 0, 0, 0, 0],
                                           [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_dilation(data)
<<<<<<< HEAD
            self.failUnless(diff(out, true) < eps)

    def test_binary_dilation23(self):
        "binary dilation 23"
        true = [[1, 1, 1, 1, 1, 1, 1, 1],
=======
            assert_array_almost_equal(out, expected)

    def test_binary_dilation23(self):
        expected = [[1, 1, 1, 1, 1, 1, 1, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 1, 1, 0, 0, 0, 0, 1],
                [1, 1, 0, 0, 0, 1, 0, 1],
                [1, 0, 0, 1, 1, 1, 1, 1],
                [1, 0, 1, 1, 1, 1, 0, 1],
                [1, 1, 1, 1, 1, 1, 1, 1],
                [1, 0, 1, 0, 0, 1, 0, 1],
                [1, 1, 1, 1, 1, 1, 1, 1]]

        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
<<<<<<< HEAD
            out = ndimage.binary_dilation(data, border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_dilation24(self):
        "binary dilation 24"
        true = [[1, 1, 0, 0, 0, 0, 0, 0],
=======
            out = ndimage.binary_dilation(data, border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_dilation24(self):
        expected = [[1, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 0, 0, 0, 1, 0, 0, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 1, 1, 1, 1, 0, 0, 0],
                [1, 1, 1, 1, 1, 1, 0, 0],
                [0, 1, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]

        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
<<<<<<< HEAD
            out = ndimage.binary_dilation(data, origin = (1, 1))
            self.failUnless(diff(out, true) < eps)

    def test_binary_dilation25(self):
        "binary dilation 25"
        true = [[1, 1, 0, 0, 0, 0, 1, 1],
=======
            out = ndimage.binary_dilation(data, origin=(1, 1))
            assert_array_almost_equal(out, expected)

    def test_binary_dilation25(self):
        expected = [[1, 1, 0, 0, 0, 0, 1, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 0, 0, 0, 1, 0, 1, 1],
                [0, 0, 1, 1, 1, 1, 1, 1],
                [0, 1, 1, 1, 1, 0, 1, 1],
                [1, 1, 1, 1, 1, 1, 1, 1],
                [0, 1, 0, 0, 1, 0, 1, 1],
                [1, 1, 1, 1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1, 1, 1, 1]]

        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
<<<<<<< HEAD
            out = ndimage.binary_dilation(data, origin = (1, 1),
                                                         border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_dilation26(self):
        "binary dilation 26"
        struct = ndimage.generate_binary_structure(2, 2)
        true = [[1, 1, 1, 0, 0, 0, 0, 0],
=======
            out = ndimage.binary_dilation(data, origin=(1, 1),
                                                         border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_dilation26(self):
        struct = ndimage.generate_binary_structure(2, 2)
        expected = [[1, 1, 1, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 1, 1, 0, 0, 0, 0, 0],
                [1, 1, 1, 0, 1, 1, 1, 0],
                [0, 0, 1, 1, 1, 1, 1, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]

        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_dilation(data, struct)
<<<<<<< HEAD
            self.failUnless(diff(out, true) < eps)

    def test_binary_dilation27(self):
        "binary dilation 27"
        struct = [[0, 1],
                  [1, 1]]
        true = [[0, 1, 0, 0, 0, 0, 0, 0],
=======
            assert_array_almost_equal(out, expected)

    def test_binary_dilation27(self):
        struct = [[0, 1],
                  [1, 1]]
        expected = [[0, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 1, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 1, 0, 0],
                [0, 0, 0, 1, 1, 1, 0, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 1, 1, 0, 1, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]

        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_dilation(data, struct)
<<<<<<< HEAD
            self.failUnless(diff(out, true) < eps)

    def test_binary_dilation28(self):
        "binary dilation 28"
        true = [[1, 1, 1, 1],
=======
            assert_array_almost_equal(out, expected)

    def test_binary_dilation28(self):
        expected = [[1, 1, 1, 1],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 0, 0, 1],
                [1, 0, 0, 1],
                [1, 1, 1, 1]]

        for type in self.types:
            data = numpy.array([[0, 0, 0, 0],
                                   [0, 0, 0, 0],
                                   [0, 0, 0, 0],
                                   [0, 0, 0, 0]], type)
<<<<<<< HEAD
            out = ndimage.binary_dilation(data, border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_dilation29(self):
        "binary dilation 29"
        struct = [[0, 1],
                  [1, 1]]
        true = [[0, 0, 0, 0, 0],
=======
            out = ndimage.binary_dilation(data, border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_dilation29(self):
        struct = [[0, 1],
                  [1, 1]]
        expected = [[0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 1, 0],
                [0, 0, 1, 1, 0],
                [0, 1, 1, 1, 0],
                [0, 0, 0, 0, 0]]

        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0],
                               [0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_dilation(data, struct,
<<<<<<< HEAD
                                                iterations = 2)
        self.failUnless(diff(out, true) < eps)

    def test_binary_dilation30(self):
        "binary dilation 30"
        struct = [[0, 1],
                  [1, 1]]
        true = [[0, 0, 0, 0, 0],
=======
                                                iterations=2)
        assert_array_almost_equal(out, expected)

    def test_binary_dilation30(self):
        struct = [[0, 1],
                  [1, 1]]
        expected = [[0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 1, 0],
                [0, 0, 1, 1, 0],
                [0, 1, 1, 1, 0],
                [0, 0, 0, 0, 0]]

        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0],
                               [0, 0, 0, 0, 0]], bool)
        out = numpy.zeros(data.shape, bool)
<<<<<<< HEAD
        ndimage.binary_dilation(data, struct, iterations = 2,
                                          output = out)
        self.failUnless(diff(out, true) < eps)

    def test_binary_dilation31(self):
        "binary dilation 31"
        struct = [[0, 1],
                  [1, 1]]
        true = [[0, 0, 0, 1, 0],
=======
        ndimage.binary_dilation(data, struct, iterations=2,
                                          output=out)
        assert_array_almost_equal(out, expected)

    def test_binary_dilation31(self):
        struct = [[0, 1],
                  [1, 1]]
        expected = [[0, 0, 0, 1, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 1, 1, 0],
                [0, 1, 1, 1, 0],
                [1, 1, 1, 1, 0],
                [0, 0, 0, 0, 0]]

        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0],
                               [0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_dilation(data, struct,
<<<<<<< HEAD
                                                iterations = 3)
        self.failUnless(diff(out, true) < eps)

    def test_binary_dilation32(self):
        "binary dilation 32"
        struct = [[0, 1],
                  [1, 1]]
        true = [[0, 0, 0, 1, 0],
=======
                                                iterations=3)
        assert_array_almost_equal(out, expected)

    def test_binary_dilation32(self):
        struct = [[0, 1],
                  [1, 1]]
        expected = [[0, 0, 0, 1, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 1, 1, 0],
                [0, 1, 1, 1, 0],
                [1, 1, 1, 1, 0],
                [0, 0, 0, 0, 0]]

        data = numpy.array([[0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 0],
                               [0, 0, 0, 0, 0]], bool)
        out = numpy.zeros(data.shape, bool)
<<<<<<< HEAD
        ndimage.binary_dilation(data, struct, iterations = 3,
                                          output = out)
        self.failUnless(diff(out, true) < eps)

    def test_binary_dilation33(self):
        "binary dilation 33"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
=======
        ndimage.binary_dilation(data, struct, iterations=3,
                                          output=out)
        assert_array_almost_equal(out, expected)

    def test_binary_dilation33(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0, 0],
                               [0, 1, 1, 0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        mask = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 1, 0],
                               [0, 0, 0, 0, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0, 0],
                               [0, 1, 1, 0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        data = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)

        out = ndimage.binary_dilation(data, struct,
<<<<<<< HEAD
                           iterations = -1, mask = mask, border_value = 0)
        self.failUnless(diff(out, true) < eps)

    def test_binary_dilation34(self):
        "binary dilation 34"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 1, 0, 0, 0, 0, 0, 0],
=======
                           iterations=-1, mask=mask, border_value=0)
        assert_array_almost_equal(out, expected)

    def test_binary_dilation34(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 1, 1, 0, 0, 0, 0, 0],
                [0, 0, 1, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        mask = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 0, 0, 0, 0, 0],
                               [0, 0, 1, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 1, 0, 0],
                               [0, 0, 0, 1, 1, 0, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        data = numpy.zeros(mask.shape, bool)
        out = ndimage.binary_dilation(data, struct,
<<<<<<< HEAD
                          iterations = -1, mask = mask, border_value = 1)
        self.failUnless(diff(out, true) < eps)

    def test_binary_dilation35(self):
        "binary dilation 35"
=======
                          iterations=-1, mask=mask, border_value=1)
        assert_array_almost_equal(out, expected)

    def test_binary_dilation35(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        tmp = [[1, 1, 0, 0, 0, 0, 1, 1],
               [1, 0, 0, 0, 1, 0, 1, 1],
               [0, 0, 1, 1, 1, 1, 1, 1],
               [0, 1, 1, 1, 1, 0, 1, 1],
               [1, 1, 1, 1, 1, 1, 1, 1],
               [0, 1, 0, 0, 1, 0, 1, 1],
               [1, 1, 1, 1, 1, 1, 1, 1],
               [1, 1, 1, 1, 1, 1, 1, 1]]
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 1, 0, 0],
                               [0, 0, 0, 1, 1, 0, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]])
        mask = [[0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
<<<<<<< HEAD
        true = numpy.logical_and(tmp, mask)
        tmp = numpy.logical_and(data, numpy.logical_not(mask))
        true = numpy.logical_or(true, tmp)
=======
        expected = numpy.logical_and(tmp, mask)
        tmp = numpy.logical_and(data, numpy.logical_not(mask))
        expected = numpy.logical_or(expected, tmp)
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        for type in self.types:
            data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 0, 0, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
<<<<<<< HEAD
            out = ndimage.binary_dilation(data, mask = mask,
                                        origin = (1, 1), border_value = 1)
            self.failUnless(diff(out, true) < eps)

    def test_binary_propagation01(self):
        "binary propagation 1"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
=======
            out = ndimage.binary_dilation(data, mask=mask,
                                        origin=(1, 1), border_value=1)
            assert_array_almost_equal(out, expected)

    def test_binary_propagation01(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0, 0],
                               [0, 1, 1, 0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        mask = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 1, 0],
                               [0, 0, 0, 0, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 0, 0, 0],
                               [0, 1, 1, 0, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        data = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)

        out = ndimage.binary_propagation(data, struct,
<<<<<<< HEAD
                                            mask = mask, border_value = 0)
        self.failUnless(diff(out, true) < eps)

    def test_binary_propagation02(self):
        "binary propagation 2"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 1, 0, 0, 0, 0, 0, 0],
=======
                                            mask=mask, border_value=0)
        assert_array_almost_equal(out, expected)

    def test_binary_propagation02(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 1, 1, 0, 0, 0, 0, 0],
                [0, 0, 1, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        mask = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                               [0, 1, 1, 0, 0, 0, 0, 0],
                               [0, 0, 1, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 1, 0, 0],
                               [0, 0, 0, 1, 1, 0, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        data = numpy.zeros(mask.shape, bool)
        out = ndimage.binary_propagation(data, struct,
<<<<<<< HEAD
                                             mask = mask, border_value = 1)
        self.failUnless(diff(out, true) < eps)

    def test_binary_opening01(self):
        "binary opening 1"
        true = [[0, 1, 0, 0, 0, 0, 0, 0],
=======
                                             mask=mask, border_value=1)
        assert_array_almost_equal(out, expected)

    def test_binary_opening01(self):
        expected = [[0, 1, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 1, 1, 0, 0, 0, 0, 0],
                [0, 1, 0, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 1, 1, 1, 0],
                [0, 0, 1, 0, 0, 1, 0, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 0, 1, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                                   [1, 1, 1, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 1, 0],
                                   [0, 0, 1, 1, 0, 1, 0, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_opening(data)
<<<<<<< HEAD
            self.failUnless(diff(out, true) < eps)

    def test_binary_opening02(self):
        "binary opening 2"
        struct = ndimage.generate_binary_structure(2, 2)
        true = [[1, 1, 1, 0, 0, 0, 0, 0],
=======
            assert_array_almost_equal(out, expected)

    def test_binary_opening02(self):
        struct = ndimage.generate_binary_structure(2, 2)
        expected = [[1, 1, 1, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [1, 1, 1, 0, 0, 0, 0, 0],
                [1, 1, 1, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 1, 1, 1, 0, 0, 0, 0],
                [0, 1, 1, 1, 0, 0, 0, 0],
                [0, 1, 1, 1, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[1, 1, 1, 0, 0, 0, 0, 0],
                                   [1, 1, 1, 0, 0, 0, 0, 0],
                                   [1, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 1, 0, 1, 1, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_opening(data, struct)
<<<<<<< HEAD
            self.failUnless(diff(out, true) < eps)

    def test_binary_closing01(self):
        "binary closing 1"
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
            assert_array_almost_equal(out, expected)

    def test_binary_closing01(self):
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 1, 1, 0, 0, 0, 0, 0],
                [0, 1, 1, 1, 0, 1, 0, 0],
                [0, 0, 1, 1, 1, 1, 1, 0],
                [0, 0, 1, 1, 1, 1, 0, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 0, 1, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 1, 0, 0, 0, 0, 0, 0],
                                   [1, 1, 1, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 1, 1, 1, 1, 0],
                                   [0, 0, 1, 1, 0, 1, 0, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 0, 1, 0, 0, 1, 0, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_closing(data)
<<<<<<< HEAD
            self.failUnless(diff(out, true) < eps)

    def test_binary_closing02(self):
        "binary closing 2"
        struct = ndimage.generate_binary_structure(2, 2)
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
            assert_array_almost_equal(out, expected)

    def test_binary_closing02(self):
        struct = ndimage.generate_binary_structure(2, 2)
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 1, 1, 0, 0, 0, 0, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 1, 1, 1, 1, 1, 1, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[1, 1, 1, 0, 0, 0, 0, 0],
                                   [1, 1, 1, 0, 0, 0, 0, 0],
                                   [1, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 1, 0, 1, 1, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_closing(data, struct)
<<<<<<< HEAD
            self.failUnless(diff(out, true) < eps)

    def test_binary_fill_holes01(self):
        "binary fill holes 1"
        true = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
=======
            assert_array_almost_equal(out, expected)

    def test_binary_fill_holes01(self):
        expected = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_fill_holes(data)
<<<<<<< HEAD
        self.failUnless(diff(out, true) < eps)

    def test_binary_fill_holes02(self):
        "binary fill holes 2"
        true = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
=======
        assert_array_almost_equal(out, expected)

    def test_binary_fill_holes02(self):
        expected = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                               [0, 0, 0, 1, 1, 0, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 1, 1, 1, 1, 0, 0],
                               [0, 0, 0, 1, 1, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 0, 1, 1, 0, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 1, 0, 0, 1, 0, 0],
                               [0, 0, 0, 1, 1, 0, 0, 0],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_fill_holes(data)
<<<<<<< HEAD
        self.failUnless(diff(out, true) < eps)

    def test_binary_fill_holes03(self):
        "binary fill holes 3"
        true = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
=======
        assert_array_almost_equal(out, expected)

    def test_binary_fill_holes03(self):
        expected = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                               [0, 0, 1, 0, 0, 0, 0, 0],
                               [0, 1, 1, 1, 0, 1, 1, 1],
                               [0, 1, 1, 1, 0, 1, 1, 1],
                               [0, 1, 1, 1, 0, 1, 1, 1],
                               [0, 0, 1, 0, 0, 1, 1, 1],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        data = numpy.array([[0, 0, 0, 0, 0, 0, 0, 0],
                               [0, 0, 1, 0, 0, 0, 0, 0],
                               [0, 1, 0, 1, 0, 1, 1, 1],
                               [0, 1, 0, 1, 0, 1, 0, 1],
                               [0, 1, 0, 1, 0, 1, 0, 1],
                               [0, 0, 1, 0, 0, 1, 1, 1],
                               [0, 0, 0, 0, 0, 0, 0, 0]], bool)
        out = ndimage.binary_fill_holes(data)
<<<<<<< HEAD
        self.failUnless(diff(out, true) < eps)

    def test_grey_erosion01(self):
        "grey erosion 1"
=======
        assert_array_almost_equal(out, expected)

    def test_grey_erosion01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        output = ndimage.grey_erosion(array,
<<<<<<< HEAD
                                                footprint = footprint)
        self.failUnless(diff([[2, 2, 1, 1, 1],
                              [2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1]], output) < eps)

    def test_grey_erosion02(self):
        "grey erosion 2"
=======
                                                footprint=footprint)
        assert_array_almost_equal([[2, 2, 1, 1, 1],
                              [2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1]], output)

    def test_grey_erosion02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
        output = ndimage.grey_erosion(array,
<<<<<<< HEAD
                              footprint = footprint, structure = structure)
        self.failUnless(diff([[2, 2, 1, 1, 1],
                              [2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1]], output) < eps)

    def test_grey_erosion03(self):
        "grey erosion 3"
=======
                              footprint=footprint, structure=structure)
        assert_array_almost_equal([[2, 2, 1, 1, 1],
                              [2, 3, 1, 3, 1],
                              [5, 5, 3, 3, 1]], output)

    def test_grey_erosion03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[1, 1, 1], [1, 1, 1]]
        output = ndimage.grey_erosion(array,
<<<<<<< HEAD
                              footprint = footprint, structure = structure)
        self.failUnless(diff([[1, 1, 0, 0, 0],
                              [1, 2, 0, 2, 0],
                              [4, 4, 2, 2, 0]], output) < eps)

    def test_grey_dilation01(self):
        "grey dilation 1"
=======
                              footprint=footprint, structure=structure)
        assert_array_almost_equal([[1, 1, 0, 0, 0],
                              [1, 2, 0, 2, 0],
                              [4, 4, 2, 2, 0]], output)

    def test_grey_dilation01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[0, 1, 1], [1, 0, 1]]
        output = ndimage.grey_dilation(array,
<<<<<<< HEAD
                                                 footprint = footprint)
        self.failUnless(diff([[7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7],
                              [8, 8, 8, 7, 7]], output) < eps)

    def test_grey_dilation02(self):
        "grey dilation 2"
=======
                                                 footprint=footprint)
        assert_array_almost_equal([[7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7],
                              [8, 8, 8, 7, 7]], output)

    def test_grey_dilation02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[0, 1, 1], [1, 0, 1]]
        structure = [[0, 0, 0], [0, 0, 0]]
        output = ndimage.grey_dilation(array,
<<<<<<< HEAD
                             footprint = footprint, structure = structure)
        self.failUnless(diff([[7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7],
                              [8, 8, 8, 7, 7]], output) < eps)

    def test_grey_dilation03(self):
        "grey dilation 3"
=======
                             footprint=footprint, structure=structure)
        assert_array_almost_equal([[7, 7, 9, 9, 5],
                              [7, 9, 8, 9, 7],
                              [8, 8, 8, 7, 7]], output)

    def test_grey_dilation03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[0, 1, 1], [1, 0, 1]]
        structure = [[1, 1, 1], [1, 1, 1]]
        output = ndimage.grey_dilation(array,
<<<<<<< HEAD
                             footprint = footprint, structure = structure)
        self.failUnless(diff([[8,  8, 10, 10, 6],
                              [8, 10,  9, 10, 8],
                              [9,  9,  9,  8, 8]], output) < eps)

    def test_grey_opening01(self):
        "grey opening 1"
=======
                             footprint=footprint, structure=structure)
        assert_array_almost_equal([[8, 8, 10, 10, 6],
                              [8, 10, 9, 10, 8],
                              [9, 9, 9, 8, 8]], output)

    def test_grey_opening01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_erosion(array, footprint = footprint)
        true = ndimage.grey_dilation(tmp, footprint = footprint)
        output = ndimage.grey_opening(array,
                                                footprint = footprint)
        self.failUnless(diff(true, output) < eps)


    def test_grey_opening02(self):
        "grey opening 2"
=======
        tmp = ndimage.grey_erosion(array, footprint=footprint)
        expected = ndimage.grey_dilation(tmp, footprint=footprint)
        output = ndimage.grey_opening(array,
                                                footprint=footprint)
        assert_array_almost_equal(expected, output)

    def test_grey_opening02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_erosion(array, footprint = footprint,
                                             structure = structure)
        true = ndimage.grey_dilation(tmp, footprint = footprint,
                                               structure = structure)
        output = ndimage.grey_opening(array,
                             footprint = footprint, structure = structure)
        self.failUnless(diff(true, output) < eps)

    def test_grey_closing01(self):
        "grey closing 1"
=======
        tmp = ndimage.grey_erosion(array, footprint=footprint,
                                             structure=structure)
        expected = ndimage.grey_dilation(tmp, footprint=footprint,
                                               structure=structure)
        output = ndimage.grey_opening(array,
                             footprint=footprint, structure=structure)
        assert_array_almost_equal(expected, output)

    def test_grey_closing01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_dilation(array, footprint = footprint)
        true = ndimage.grey_erosion(tmp, footprint = footprint)
        output = ndimage.grey_closing(array,
                                                footprint = footprint)
        self.failUnless(diff(true, output) < eps)

    def test_grey_closing02(self):
        "grey closing 2"
=======
        tmp = ndimage.grey_dilation(array, footprint=footprint)
        expected = ndimage.grey_erosion(tmp, footprint=footprint)
        output = ndimage.grey_closing(array,
                                                footprint=footprint)
        assert_array_almost_equal(expected, output)

    def test_grey_closing02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_dilation(array, footprint = footprint,
                                              structure = structure)
        true = ndimage.grey_erosion(tmp, footprint = footprint,
                                              structure = structure)
        output = ndimage.grey_closing(array,
                              footprint = footprint, structure = structure)
        self.failUnless(diff(true, output) < eps)

    def test_morphological_gradient01(self):
        "morphological gradient 1"
=======
        tmp = ndimage.grey_dilation(array, footprint=footprint,
                                              structure=structure)
        expected = ndimage.grey_erosion(tmp, footprint=footprint,
                                              structure=structure)
        output = ndimage.grey_closing(array,
                              footprint=footprint, structure=structure)
        assert_array_almost_equal(expected, output)

    def test_morphological_gradient01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
        tmp1 = ndimage.grey_dilation(array,
<<<<<<< HEAD
                             footprint = footprint, structure = structure)
        tmp2 = ndimage.grey_erosion(array, footprint = footprint,
                                              structure = structure)
        true = tmp1 - tmp2
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.morphological_gradient(array,
                footprint=footprint, structure=structure, output = output)
        self.failUnless(diff(true, output) < eps)

    def test_morphological_gradient02(self):
        "morphological gradient 2"
=======
                             footprint=footprint, structure=structure)
        tmp2 = ndimage.grey_erosion(array, footprint=footprint,
                                              structure=structure)
        expected = tmp1 - tmp2
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.morphological_gradient(array,
                footprint=footprint, structure=structure, output=output)
        assert_array_almost_equal(expected, output)

    def test_morphological_gradient02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
        tmp1 = ndimage.grey_dilation(array,
<<<<<<< HEAD
                             footprint = footprint, structure = structure)
        tmp2 = ndimage.grey_erosion(array, footprint = footprint,
                                              structure = structure)
        true = tmp1 - tmp2
        output =ndimage.morphological_gradient(array,
                                footprint=footprint, structure=structure)
        self.failUnless(diff(true, output) < eps)

    def test_morphological_laplace01(self):
        "morphological laplace 1"
=======
                             footprint=footprint, structure=structure)
        tmp2 = ndimage.grey_erosion(array, footprint=footprint,
                                              structure=structure)
        expected = tmp1 - tmp2
        output = ndimage.morphological_gradient(array,
                                footprint=footprint, structure=structure)
        assert_array_almost_equal(expected, output)

    def test_morphological_laplace01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
        tmp1 = ndimage.grey_dilation(array,
<<<<<<< HEAD
                              footprint = footprint, structure = structure)
        tmp2 = ndimage.grey_erosion(array, footprint = footprint,
                                              structure = structure)
        true = tmp1 + tmp2 - 2 * array
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.morphological_laplace(array, footprint=footprint,
                                     structure=structure, output = output)
        self.failUnless(diff(true, output) < eps)

    def test_morphological_laplace02(self):
        "morphological laplace 2"
=======
                              footprint=footprint, structure=structure)
        tmp2 = ndimage.grey_erosion(array, footprint=footprint,
                                              structure=structure)
        expected = tmp1 + tmp2 - 2 * array
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.morphological_laplace(array, footprint=footprint,
                                     structure=structure, output=output)
        assert_array_almost_equal(expected, output)

    def test_morphological_laplace02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
        tmp1 = ndimage.grey_dilation(array,
<<<<<<< HEAD
                             footprint = footprint, structure = structure)
        tmp2 = ndimage.grey_erosion(array, footprint = footprint,
                                              structure = structure)
        true = tmp1 + tmp2 - 2 * array
        output = ndimage.morphological_laplace(array,
                                footprint=footprint, structure=structure)
        self.failUnless(diff(true, output) < eps)

    def test_white_tophat01(self):
        "white tophat 1"
=======
                             footprint=footprint, structure=structure)
        tmp2 = ndimage.grey_erosion(array, footprint=footprint,
                                              structure=structure)
        expected = tmp1 + tmp2 - 2 * array
        output = ndimage.morphological_laplace(array,
                                footprint=footprint, structure=structure)
        assert_array_almost_equal(expected, output)

    def test_white_tophat01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_opening(array, footprint = footprint,
                                             structure = structure)
        true = array - tmp
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.white_tophat(array, footprint=footprint,
                                      structure=structure, output = output)
        self.failUnless(diff(true, output) < eps)

    def test_white_tophat02(self):
        "white tophat 2"
=======
        tmp = ndimage.grey_opening(array, footprint=footprint,
                                             structure=structure)
        expected = array - tmp
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.white_tophat(array, footprint=footprint,
                                      structure=structure, output=output)
        assert_array_almost_equal(expected, output)

    def test_white_tophat02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_opening(array, footprint = footprint,
                                             structure = structure)
        true = array - tmp
        output = ndimage.white_tophat(array, footprint=footprint,
                                                structure=structure)
        self.failUnless(diff(true, output) < eps)

    def test_black_tophat01(self):
        "black tophat 1"
=======
        tmp = ndimage.grey_opening(array, footprint=footprint,
                                             structure=structure)
        expected = array - tmp
        output = ndimage.white_tophat(array, footprint=footprint,
                                                structure=structure)
        assert_array_almost_equal(expected, output)

    def test_black_tophat01(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_closing(array, footprint = footprint,
                                             structure = structure)
        true = tmp - array
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.black_tophat(array, footprint=footprint,
                                      structure=structure, output = output)
        self.failUnless(diff(true, output) < eps)

    def test_black_tophat02(self):
        "black tophat 2"
=======
        tmp = ndimage.grey_closing(array, footprint=footprint,
                                             structure=structure)
        expected = tmp - array
        output = numpy.zeros(array.shape, array.dtype)
        ndimage.black_tophat(array, footprint=footprint,
                                      structure=structure, output=output)
        assert_array_almost_equal(expected, output)

    def test_black_tophat02(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        array = numpy.array([[3, 2, 5, 1, 4],
                                [7, 6, 9, 3, 5],
                                [5, 8, 3, 7, 1]])
        footprint = [[1, 0, 1], [1, 1, 0]]
        structure = [[0, 0, 0], [0, 0, 0]]
<<<<<<< HEAD
        tmp = ndimage.grey_closing(array, footprint = footprint,
                                             structure = structure)
        true = tmp - array
        output = ndimage.black_tophat(array, footprint=footprint,
                                                structure=structure)
        self.failUnless(diff(true, output) < eps)

    def test_hit_or_miss01(self):
        "binary hit-or-miss transform 1"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0],
=======
        tmp = ndimage.grey_closing(array, footprint=footprint,
                                             structure=structure)
        expected = tmp - array
        output = ndimage.black_tophat(array, footprint=footprint,
                                                structure=structure)
        assert_array_almost_equal(expected, output)

    def test_hit_or_miss01(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 1, 0, 0, 0],
                [0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 1, 0, 0, 0],
                                   [1, 1, 1, 0, 0],
                                   [0, 1, 0, 1, 1],
                                   [0, 0, 1, 1, 1],
                                   [0, 1, 1, 1, 0],
                                   [0, 1, 1, 1, 1],
                                   [0, 1, 1, 1, 1],
                                   [0, 0, 0, 0, 0]], type)
            out = numpy.zeros(data.shape, bool)
            ndimage.binary_hit_or_miss(data, struct,
<<<<<<< HEAD
                                                 output = out)
            self.failUnless(diff(true, out) < eps)

    def test_hit_or_miss02(self):
        "binary hit-or-miss transform 2"
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        true = [[0, 0, 0, 0, 0, 0, 0, 0],
=======
                                                 output=out)
            assert_array_almost_equal(expected, out)

    def test_hit_or_miss02(self):
        struct = [[0, 1, 0],
                  [1, 1, 1],
                  [0, 1, 0]]
        expected = [[0, 0, 0, 0, 0, 0, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 1, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 1, 0, 0, 1, 1, 1, 0],
                                   [1, 1, 1, 0, 0, 1, 0, 0],
                                   [0, 1, 0, 1, 1, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_hit_or_miss(data, struct)
<<<<<<< HEAD
            self.failUnless(diff(true, out) < eps)

    def test_hit_or_miss03(self):
        "binary hit-or-miss transform 3"
=======
            assert_array_almost_equal(expected, out)

    def test_hit_or_miss03(self):
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
        struct1 = [[0, 0, 0],
                   [1, 1, 1],
                   [0, 0, 0]]
        struct2 = [[1, 1, 1],
                   [0, 0, 0],
                   [1, 1, 1]]
<<<<<<< HEAD
        true = [[0, 0, 0, 0, 0, 1, 0, 0],
=======
        expected = [[0, 0, 0, 0, 0, 1, 0, 0],
>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 1, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0]]
        for type in self.types:
            data = numpy.array([[0, 1, 0, 0, 1, 1, 1, 0],
                                   [1, 1, 1, 0, 0, 0, 0, 0],
                                   [0, 1, 0, 1, 1, 1, 1, 0],
                                   [0, 0, 1, 1, 1, 1, 1, 0],
                                   [0, 1, 1, 1, 0, 1, 1, 0],
                                   [0, 0, 0, 0, 1, 1, 1, 0],
                                   [0, 1, 1, 1, 1, 1, 1, 0],
                                   [0, 0, 0, 0, 0, 0, 0, 0]], type)
            out = ndimage.binary_hit_or_miss(data, struct1,
                                              struct2)
<<<<<<< HEAD
            self.failUnless(diff(true, out) < eps)


#class NDImageTestResult(unittest.TestResult):
#    separator1 = '=' * 70 + '\n'
#    separator2 = '-' * 70 + '\n'
#
#    def __init__(self, stream, verbose):
#        unittest.TestResult.__init__(self)
#        self.stream = stream
#        self.verbose = verbose
#
#    def getDescription(self, test):
#        return test.shortDescription() or str(test)
#
#    def startTest(self, test):
#        unittest.TestResult.startTest(self, test)
#        if self.verbose:
#            self.stream.write(self.getDescription(test))
#            self.stream.write(" ... ")
#
#    def addSuccess(self, test):
#        unittest.TestResult.addSuccess(self, test)
#        if self.verbose:
#            self.stream.write("ok\n")
#
#    def addError(self, test, err):
#        unittest.TestResult.addError(self, test, err)
#        if self.verbose:
#            self.stream.write("ERROR\n")
#
#    def addFailure(self, test, err):
#        unittest.TestResult.addFailure(self, test, err)
#        if self.verbose:
#            self.stream.write("FAIL\n")
#
#    def printErrors(self):
#        self.printErrorList('ERROR', self.errors)
#        self.printErrorList('FAIL', self.failures)
#
#    def printErrorList(self, flavour, errors):
#        for test, err in errors:
#            self.stream.write(self.separator1)
#            description = self.getDescription(test)
#            self.stream.write("%s: %s\n" % (flavour, description))
#            self.stream.write(self.separator2)
#            self.stream.write(err)
#
#def test():
#    if '-v' in sys.argv[1:]:
#        verbose = 1
#    else:
#        verbose = 0
#    suite = unittest.TestSuite()
#    suite.addTest(unittest.makeSuite(NDImageTest))
#    result = NDImageTestResult(sys.stdout, verbose)
#    suite(result)
#    result.printErrors()
#    return len(result.failures), result.testsRun
=======
            assert_array_almost_equal(expected, out)


class TestDilateFix:

    def setUp(self):
        # dilation related setup
        self.array = numpy.array([[0, 0, 0, 0, 0,],
                                  [0, 0, 0, 0, 0,],
                                  [0, 0, 0, 1, 0,],
                                  [0, 0, 1, 1, 0,],
                                  [0, 0, 0, 0, 0,]], dtype=numpy.uint8)

        self.sq3x3 = numpy.ones((3, 3))
        dilated3x3 = ndimage.binary_dilation(self.array, structure=self.sq3x3)
        self.dilated3x3 = dilated3x3.view(numpy.uint8)

    def test_dilation_square_structure(self):
        result = ndimage.grey_dilation(self.array, structure=self.sq3x3)
        # +1 accounts for difference between grey and binary dilation
        assert_array_almost_equal(result, self.dilated3x3 + 1)

    def test_dilation_scalar_size(self):
        result = ndimage.grey_dilation(self.array, size=3)
        assert_array_almost_equal(result, self.dilated3x3)

>>>>>>> 85b42d3bbdcef5cbe0fe2390bba8b3ff1608040b

if __name__ == "__main__":
    run_module_suite()
