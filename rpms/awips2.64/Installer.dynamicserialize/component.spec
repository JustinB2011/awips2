%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')

%define _python_pkgs_dir "%{_baseline_workspace}/pythonPackages"

#
# AWIPS II Python dynamicserialize Spec File
#
Name: awips2-python-dynamicserialize
Summary: AWIPS II Python dynamicserialize Distribution - 64 Bit
Version: %{_component_version}
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: Bryan Kowal

AutoReq: no
requires: awips2-python
requires: awips2-python-thrift
provides: awips2-python-dynamicserialize

%description
AWIPS II Python dynamicserialize Site-Package - 64-bit.

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "%{_build_root}" = "/tmp" ]
then
   echo "An Actual BuildRoot Must Be Specified. Use The --buildroot Parameter."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

rm -rf %{_build_root}
mkdir -p %{_build_root}/awips2/python/lib/python2.7/site-packages/dynamicserialize

%build

%install
DYNAMICSERIALIZE_SRC_DIR="%{_python_pkgs_dir}/dynamicserialize"

cp -rv ${DYNAMICSERIALIZE_SRC_DIR}/* \
   %{_build_root}/awips2/python/lib/python2.7/site-packages/dynamicserialize
   
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