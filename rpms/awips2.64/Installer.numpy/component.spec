%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')

%define _python_pkgs_dir "%{_baseline_workspace}/pythonPackages"

#
# AWIPS II Python numpy Spec File
#
Name: awips2-python-numpy
Summary: AWIPS II Python numpy Distribution - 64 Bit
Version: 1.5.0
Release: 1
Group: AWIPSII
BuildRoot: %{_build_root}
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: Bryan Kowal

AutoReq: no
requires: awips2-python
requires: awips2-python-nose
provides: awips2-python-numpy

%description
AWIPS II Python numpy Site-Package - 64-bit.

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "%{_build_root}" = "/tmp" ]
then
   echo "An Actual BuildRoot Must Be Specified. Use The --buildroot Parameter."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

rm -rf %{_build_root}
mkdir -p %{_build_root}
mkdir -p %{_build_root}/build-python

%build
NUMPY_SRC_DIR="%{_python_pkgs_dir}/numpy"
NUMPY_TAR="numpy-1.5.0.tar.gz"
NUMPY_PATCH="numpy.patch1"
cp -v ${NUMPY_SRC_DIR}/${NUMPY_TAR} \
   %{_build_root}/build-python
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
cp -v %{_baseline_workspace}/Installer.rpm/awips2.64/Installer.numpy/src/${NUMPY_PATCH} \
   %{_build_root}/build-python
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi

pushd . > /dev/null
cd %{_build_root}/build-python
tar -xvf ${NUMPY_TAR}
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
rm -fv ${NUMPY_TAR}
if [ ! -d numpy-1.5.0 ]; then
   file numpy-1.5.0
   exit 1
fi
source /etc/profile.d/awips2Python64.sh
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
cd numpy-1.5.0
# Apply the patch
patch -p1 -i ../${NUMPY_PATCH}
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
/awips2/python/bin/python setup.py clean
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi 
/awips2/python/bin/python setup.py build
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
popd > /dev/null

%install
NUMPY_SRC_DIR="%{_python_pkgs_dir}/numpy"

pushd . > /dev/null
cd %{_build_root}/build-python/numpy-1.5.0
export LD_LIBRARY_PATH=/awips2/python/lib
/awips2/python/bin/python setup.py install \
   --root=%{_build_root} \
   --prefix=/awips2/python
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
popd > /dev/null

rm -rf %{_build_root}/build-python

%pre

%post

%preun

%postun

%clean
rm -rf %{_build_root}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/python/lib/python2.7/site-packages
/awips2/python/lib/python2.7/site-packages/*
%defattr(755,awips,fxalpha,755)
%dir /awips2/python/bin
/awips2/python/bin/*