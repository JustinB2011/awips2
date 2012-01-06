%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')

%define _python_pkgs_dir "%{_baseline_workspace}/pythonPackages"

#
# AWIPS II Python tables Spec File
#
Name: awips2-python-tables
Summary: AWIPS II Python tables Distribution - 64 Bit
Version: 2.1.2
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
provides: awips2-python-numpy
provides: awips2-python-tables

%description
AWIPS II Python tables Site-Package - 64-bit.

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

PRE_REQS_HDF5_TAR="hdf5-1.8.4-patch1-linux-x86_64-shared.tar.gz"
PRE_REQS_DIR="%{_baseline_workspace}/Installer.rpm/awips2.64/deploy.builder/pre-reqs"
cp -v ${PRE_REQS_DIR}/${PRE_REQS_HDF5_TAR} \
   %{_build_root}
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi

pushd . > /dev/null
cd %{_build_root}
/bin/tar -xvf ${PRE_REQS_HDF5_TAR}
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
rm -f ${PRE_REQS_HDF5_TAR}
popd > /dev/null

%build
TABLES_SRC_DIR="%{_python_pkgs_dir}/tables"
TABLES_TAR="tables-2.1.2.tar.gz"
cp -v ${TABLES_SRC_DIR}/${TABLES_TAR} \
   %{_build_root}/build-python
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi

pushd . > /dev/null
cd %{_build_root}/build-python
tar -xvf ${TABLES_TAR}
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
rm -fv ${TABLES_TAR}
if [ ! -d tables-2.1.2 ]; then
   file tables-2.1.2
   exit 1
fi
cd tables-2.1.2
export HDF5_DIR="%{_build_root}/hdf5-1.8.4-patch1-linux-x86_64-shared"
/awips2/python/bin/python setup.py build_ext --inplace
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
popd > /dev/null

%install
export HDF5_DIR="%{_build_root}/hdf5-1.8.4-patch1-linux-x86_64-shared"
pushd . > /dev/null
cd %{_build_root}/build-python/tables-2.1.2
/awips2/python/bin/python setup.py install \
   --root=%{_build_root} \
   --prefix=/awips2/python
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
popd > /dev/null

rm -rf ${HDF5_DIR}
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