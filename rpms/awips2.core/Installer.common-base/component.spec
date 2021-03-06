%define _build_arch %(uname -i)
%define _zip_file common-base.zip

%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-java-repack-jars[[:space:]].*$!!g')

#
# AWIPS II CAVE/EDEX common base Spec File
#
Name: awips2-common-base
Summary: AWIPS II Edex
Version: %{_component_version}
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
BuildArch: %{_build_arch}
URL: N/A
License: N/A
Distribution: N/A
Vendor: %{_build_vendor}
Packager: %{_build_site}

AutoReq: no
Provides: awips2-common-base
Requires: awips2-base
Requires: netcdf = 4.1.2
Requires: netcdf-devel = 4.1.2

BuildRequires: awips2-ant
BuildRequires: awips2-java

%description
AWIPS II Common Base - Contains common plugins utilized by EDEX.

%prep
# Ensure that a "buildroot" has been specified.
if [ "%{_build_root}" = "" ]; then
   echo "ERROR: A BuildRoot has not been specified."
   echo "FATAL: Unable to Continue ... Terminating."
   exit 1
fi

if [ -d %{_build_root} ]; then
   rm -rf %{_build_root}
fi
/bin/mkdir -p %{_build_root}
if [ $? -ne 0 ]; then
   exit 1
fi

%build
_build_xml=build.xml
BUILD_EDEX=%{_baseline_workspace}/build.edex
EDEX_DIST=${BUILD_EDEX}/edex/dist

cd ${BUILD_EDEX}
/awips2/ant/bin/ant -f ${_build_xml} \
   -Dbuild.arch=x86_64 \
   -Dfeature=com.raytheon.uf.common.base.feature \
   -Duframe.eclipse=%{_uframe_eclipse} \
   clean \
   build \
   clean
if [ $? -ne 0 ]; then
   exit 1
fi

%install
BUILD_EDEX=%{_baseline_workspace}/build.edex
EDEX_DIST=${BUILD_EDEX}/edex/dist

/usr/bin/unzip ${EDEX_DIST}/common-base.zip -d %{_build_root}
if [ $? -ne 0 ]; then
   exit 1
fi

#create a list of all files packaged for /awips2/edex/data/utility
UTILITY=/awips2/edex/data/utility
if [ -d %{_build_root}/$UTILITY ]; then
   cd %{_build_root}/$UTILITY
   find . -type f > %{_build_root}/awips2/edex/util_filelist.%{name}.txt
fi

%pre
%post
# EDEX installed?

# when the plugins are for EDEX, we just leave
# them on the filesystem; no action required.
rpm -q awips2-edex > /dev/null 2>&1
retVal=$?
if [ $retVal -ne 0 ]; then
   # hide the edex plugins
   pushd . > /dev/null 2>&1
   cd /awips2
   rm -rf .edex
   mv edex .edex
   popd > /dev/null 2>&1
else if [ $retVal -eq 0 ]; then
   #change date stamp of utility files
   UTILITY=/awips2/edex/data/utility
   UTIL_FILENAME=/awips2/edex/util_filelist.%{name}.txt
   if [ -d $UTILITY ] && [ -f $UTIL_FILENAME ]; then
     while read fileName
     do
      touch "$UTILITY/$fileName"
     done < $UTIL_FILENAME
     rm -f $UTIL_FILENAME
   fi
 fi
fi

%preun
if [ -d /awips2/.edex ]; then
   rm -rf /awips2/.edex
fi

%postun

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2
%dir /awips2/edex
/awips2/edex/*
