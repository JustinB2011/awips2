#
# AWIPS II EDEX Spec File
#
Name: awips2-edex
Summary: AWIPS II Edex
Version: %{_component_version}
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: Bryan Kowal

provides: awips2-edex
provides: awips2-base-component
provides: awips2-base
requires: awips2-python
requires: awips2-java
requires: awips2-psql

%description
AWIPS II Edex Installation - Installs and configures AWIPS II Edex.

# Turn off the brp-python-bytecompile script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-java-repack-jars[[:space:]].*$!!g')

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "%{_build_root}" = "" ]
then
   echo "ERROR: The RPM Build Root has not been specified."
   exit 1
fi

if [ -d %{_build_root} ]; then
   rm -rf %{_build_root}
fi

%build

%install
mkdir -p %{_build_root}/awips2/edex
if [ $? -ne 0 ]; then
   exit 1
fi
mkdir -p %{_build_root}/awips2/edex/logs
if [ $? -ne 0 ]; then
   exit 1
fi
mkdir -p %{_build_root}/etc/init.d
if [ $? -ne 0 ]; then
   exit 1
fi

DEPLOY_SCRIPT="build.edex/deploy-common/deploy-esb.xml"

BUILD_ARCH="%{_build_arch}"
if [ "${BUILD_ARCH}" = "i386" ]; then
   BUILD_ARCH="x86"
fi

# use deploy-install to deploy edex.
pushd . > /dev/null
cd %{_baseline_workspace}
/awips2/ant/bin/ant -f ${DEPLOY_SCRIPT} \
   -Ddeploy.data=true -Ddeploy.web=true \
   -Desb.overwrite=true \
   -Desb.directory=%{_baseline_workspace}/build.edex/esb \
   -Dedex.root.directory=${RPM_BUILD_ROOT}/awips2/edex \
   -Dbasedir=%{_baseline_workspace}/build.edex \
   -Dbasedirectories=%{_baseline_workspace} \
   -Darchitecture=${BUILD_ARCH}
if [ $? -ne 0 ]; then
   exit 1
fi
popd > /dev/null

# remove any .gitignore files
# currently, the ebxml webapp includes a .gitignore file
/usr/bin/find ${RPM_BUILD_ROOT}/awips2/edex -name .gitignore -exec rm -f {} \;
if [ $? -ne 0 ]; then
   exit 1
fi

INSTALLER_RPM="%{_baseline_workspace}/rpms"
# copy the service script.
EDEX_BASE="${INSTALLER_RPM}/awips2.edex/Installer.edex"
cp -v ${EDEX_BASE}/scripts/init.d/* \
   %{_build_root}/etc/init.d
if [ $? -ne 0 ]; then
   exit 1
fi

# copy versions.sh.
UTILITY="${INSTALLER_RPM}/utility"
cp -v ${UTILITY}/scripts/versions.sh \
   %{_build_root}/awips2/edex/bin
if [ $? -ne 0 ]; then
   exit 1
fi

%pre

%post
MACHINE_BIT=`uname -i`
if [ "${MACHINE_BIT}" = "i386" ]
then
    rm -Rf /awips2/edex/lib/lib64
fi

# We need to create a link to the python shared library if it does not exist.
pushd . > /dev/null 2>&1
if [ -d /awips2/python/lib ]; then
   cd /awips2/python/lib
   if [ -L libpython.so ]; then
      # Ensure that we are pointing to the correct shared library.
      rm -f libpython.so
   fi
      
   if [ -f libpython2.7.so.1.0 ]; then
      ln -s libpython2.7.so.1.0 libpython.so
   fi
fi
popd > /dev/null 2>&1

if [ -f /etc/init.d/edex_camel ]; then
   /sbin/chkconfig --add edex_camel
fi

# determine if an installation of awips2-common-base is already present
# (CAVE has been installed before edex on an ADAM machine)
if [ -d /awips2/.edex ]; then
   # copy the common-base contributions to the EDEX installation
   cp -r /awips2/.edex/* /awips2/edex
   
   # cleanup
   rm -rf /awips2/.edex
fi

%preun
if [ "${1}" = "1" ]; then
   exit 0
fi
if [ -f /etc/init.d/edex_camel ]; then
   /sbin/chkconfig --del edex_camel
fi

%postun

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2
%dir /awips2/edex
%dir /awips2/edex/conf
/awips2/edex/conf/*
%dir /awips2/edex/data
/awips2/edex/data/*
%dir /awips2/edex/etc
/awips2/edex/etc/*
%dir /awips2/edex/lib
/awips2/edex/lib/*
%dir /awips2/edex/logs
%dir /awips2/edex/webapps
/awips2/edex/webapps/*
%dir /awips2/edex/bin/yajsw
/awips2/edex/bin/yajsw/*

%defattr(755,awips,fxalpha,755)
%dir /awips2/edex/bin
/awips2/edex/bin/*.sh

%attr(744,root,root) /etc/init.d/*
