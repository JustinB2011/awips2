%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')

%define _python_pkgs_dir "%{_baseline_workspace}/pythonPackages"

#
# AWIPS II Python pmw Spec File
#
Name: awips2-python-pmw
Summary: AWIPS II Python pmw Distribution - 64 Bit
Version: 1.3.2
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
provides: awips2-python-pmw

%description
AWIPS II Python pmw Site-Package - 64-bit.

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
PMW_SRC_DIR="%{_python_pkgs_dir}/pmw"
PMW_TAR="Pmw.1.3.2.tar.gz"
PMW_SRC="Pmw.1.3.2/src"

cp -rv ${PMW_SRC_DIR}/${PMW_TAR} \
   %{_build_root}/build-python
   
pushd . > /dev/null
cd %{_build_root}/build-python
tar -xvf ${PMW_TAR}
rm -f ${PMW_TAR}
cd ${PMW_SRC}
export LD_LIBRARY_PATH=/awips2/python/lib
/awips2/python/bin/python setup.py build
RC=$?
if [ ${RC} -ne 0 ]; then
   exit 1
fi
popd > /dev/null

%install
PMW_SRC="Pmw.1.3.2/src"

pushd . > /dev/null
cd %{_build_root}/build-python/${PMW_SRC}
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

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/python/lib/python2.7/site-packages
/awips2/python/lib/python2.7/site-packages/*
