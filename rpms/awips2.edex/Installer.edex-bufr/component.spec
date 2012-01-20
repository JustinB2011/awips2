%define CORE_DELTA_SETUP ${WORKSPACE_DIR}/Installer.rpm/delta/setup/updateSetup.sh
%define _component_name           awips2-edex-bufr
%define _component_project_dir    awips2.edex/Installer.edex-bufr
%define _component_default_prefix /awips2
#
# AWIPS II Edex Bufr Spec File
#
Name: %{_component_name}
Summary: AWIPS II Edex Bufr
Version: %{_component_version}
Release: %{_component_release}
Group: AWIPSII
BuildRoot: /tmp
Prefix: %{_component_default_prefix}
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: Bryan Kowal

AutoReq: no
provides: awips2-edex-bufr
requires: awips2
requires: awips2-edex-base
requires: awips2-python
requires: awips2-java
requires: awips2-psql

%description
AWIPS II Edex Installation - Installs The AWIPS II Edex Bufr Plugins.

# Turn off the brp-python-bytecompile script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-java-repack-jars[[:space:]].*$!!g')

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "${RPM_BUILD_ROOT}" = "/tmp" ]
then
   echo "An Actual BuildRoot Must Be Specified. Use The --buildroot Parameter."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

mkdir -p ${RPM_BUILD_ROOT}/awips2/edex

%build
#---------------------------------------------------------------------------#
# Delta-Enabled RPM
#---------------------------------------------------------------------------#
source %{CORE_DELTA_SETUP}
copySetupCore ${RPM_BUILD_ROOT} %{_component_default_prefix}
copyApplicableDeltas ${RPM_BUILD_ROOT} %{_component_name} \
   %{_component_project_dir} %{_component_default_prefix}
#---------------------------------------------------------------------------#

%install
DEPLOY_SCRIPT="build.edex/deploy-install.xml"

# Deploy Edex To Our Temporary Build Directory.

# Determine which ant executable to use.
COMMAND=`rpm -q awips2-ant`
RC="$?"
if [ ! "${RC}" = "0" ]; then
   echo "ERROR: awips2-ant Must Be Installed."
   echo "Unable To Continue ... Terminating."
   exit 1
fi

ANT_EXE=`rpm -q --queryformat '%{INSTALLPREFIX}\n' awips2-ant`
ANT_EXE="${ANT_EXE}/bin/ant"

${ANT_EXE} -file ${WORKSPACE_DIR}/${DEPLOY_SCRIPT} \
   -Dinstall.dir=${RPM_BUILD_ROOT}/awips2/edex \
   -Dinstaller=true -Dlocal.build=false \
   -Dcomponent.to.deploy=edex-bufr

%pre
if [ "${1}" = "1" ]; then
   # This Is An Installation - Not An Upgrade.
   # Ensure That We Are Being Installed To The Correct Location.
   EDEX_INSTALL=`rpm -q --queryformat '%{INSTALLPREFIX}\n' awips2-edex-base`
   if [ ! "${RPM_INSTALL_PREFIX}" = "${EDEX_INSTALL}" ]; then
      echo -e "\e[1;31m--------------------------------------------------------------------------------\e[m"
      echo -e "\e[1;31m\| ERROR: These Plugins MUST Be Installed At The Same Location As EDEX!!!" 
      echo -e "\e[1;34m\|  INFO: Use '--prefix=${EDEX_INSTALL}'.\e[m"
      echo -e "\e[1;31m--------------------------------------------------------------------------------\e[m"

      exit 1
   fi
fi

if [ "${1}" = "2" ]; then
   exit 0
fi
echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;34m\| Installing AWIPS II Edex Bufr Plugins...\e[m"
echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;34m   Installation Root = ${RPM_INSTALL_PREFIX}/edex\e[m"

%post
#---------------------------------------------------------------------------#
# Delta-Enabled RPM
#---------------------------------------------------------------------------#
if [ "${1}" = "2" ]; then
   echo "INFO: Performing %{_component_name} Upgrade."
   echo "Preparing ..."
   
   # Check the delta directory to see if there are updates that
   # may need to be applied.
   cd ${RPM_INSTALL_PREFIX}/delta/%{_component_name}
   COUNT=`ls -1 | wc -l`
   
   if [ "${COUNT}" = "0" ]; then
      echo "INFO: No Updates To Perform."
      exit 0
   fi
   
   echo "INFO: Potentially Applying ${COUNT} Updates."
   
   # The Update Manager Is In: ${RPM_INSTALL_PREFIX}/delta
   UPDATE_MANAGER="${RPM_INSTALL_PREFIX}/delta/updateManager.sh"
   cd ${RPM_INSTALL_PREFIX}/delta
   export COMPONENT_INSTALL="${RPM_INSTALL_PREFIX}"
   ${UPDATE_MANAGER} %{_component_name}
   
   exit 0
fi
#---------------------------------------------------------------------------#
echo -e "\e[1;32m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;32m\| AWIPS II Edex Bufr Plugin Installation - COMPLETE\e[m"
echo -e "\e[1;32m--------------------------------------------------------------------------------\e[m"

%postun
if [ "${1}" = "1" ]; then
   exit 0
fi
echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;34m\| AWIPS II Edex Bufr Plugins Have Been Successfully Removed\e[m"
echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
#---------------------------------------------------------------------------#
# Delta-Enabled RPM
#---------------------------------------------------------------------------#
%dir %{_component_default_prefix}/delta
%attr(700,root,root) %{_component_default_prefix}/delta/updateManager.sh
%attr(700,root,root) %{_component_default_prefix}/delta/createUpdateRegistry.sh
%{_component_default_prefix}/delta/%{_component_name}
#---------------------------------------------------------------------------#
%dir /awips2
%dir /awips2/edex
/awips2/edex/*