%define CORE_DELTA_SETUP ${WORKSPACE_DIR}/Installer.rpm/delta/setup/updateSetup.sh
%define _component_name           awips2-notification
%define _component_project_dir    awips2.core/Installer.notification
%define _component_default_prefix /awips2/notification
#
# AWIPS II Notification Spec File
#
%define __prelink_undo_cmd %{nil}

Name: %{_component_name}
Summary: AWIPS II Notification
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
requires: boost >= 1.33.1
provides: awips2-notification

%description
AWIPS II Notification Distribution - the AWIPS II Notification application.

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "${RPM_BUILD_ROOT}" = "/tmp" ]
then
   echo "An Actual BuildRoot Must Be Specified. Use The --buildroot Parameter."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

mkdir -p ${RPM_BUILD_ROOT}/awips2/notification
mkdir -p ${RPM_BUILD_ROOT}/etc/profile.d

PROFILE_D_DIR="Installer.rpm/awips2.core/Installer.notification/scripts/profile.d"
cp ${WORKSPACE_DIR}/${PROFILE_D_DIR}/* ${RPM_BUILD_ROOT}/etc/profile.d

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
# Copies the standard Raytheon licenses into a license directory for the
# current component.
function copyLegal()
{
   # $1 == Component Build Root
   
   COMPONENT_BUILD_DIR=${1}
   
   mkdir -p ${RPM_BUILD_ROOT}/${COMPONENT_BUILD_DIR}/licenses
   
   # Create a Tar file with our FOSS licenses.
   tar -cjf ${WORKSPACE_DIR}/Installer.rpm/legal/FOSS_licenses.tar \
      ${WORKSPACE_DIR}/Installer.rpm/legal/FOSS_licenses/
   
   cp ${WORKSPACE_DIR}/Installer.rpm/legal/license.txt \
      ${RPM_BUILD_ROOT}/${COMPONENT_BUILD_DIR}/licenses
   cp "${WORKSPACE_DIR}/Installer.rpm/legal/Master Rights File.pdf" \
      ${RPM_BUILD_ROOT}/${COMPONENT_BUILD_DIR}/licenses
   cp ${WORKSPACE_DIR}/Installer.rpm/legal/FOSS_licenses.tar \
      ${RPM_BUILD_ROOT}/${COMPONENT_BUILD_DIR}/licenses
      
   rm -f ${WORKSPACE_DIR}/Installer.rpm/legal/FOSS_licenses.tar    
}
NOTIFICATION_TAR_FILE_DIR="packages/notification"
NOTIFICATION_TAR_FILE="${NOTIFICATION_TAR_FILE_DIR}/edex_com.tar.bz2"

cd ${RPM_BUILD_ROOT}/awips2
/bin/gtar -xpf ${AWIPSCM_SHARE}/${NOTIFICATION_TAR_FILE}

cp -r ${RPM_BUILD_ROOT}/awips2/edex_com/* ${RPM_BUILD_ROOT}/awips2/notification/
# Remove the boost rpms directory
rm -rf ${RPM_BUILD_ROOT}/awips2/notification/rpms
rm -rf ${RPM_BUILD_ROOT}/awips2/edex_com

copyLegal "awips2/notification"

%pre
if [ "${1}" = "2" ]; then
   exit 0
fi

echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;34m\| Installing AWIPS II Notification...\e[m"
echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;34m   Installation Root = ${RPM_INSTALL_PREFIX}\e[m"

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
echo "--------------------------------------------------------------------------------"
echo "\| Setting up the AWIPS II Notification Runtime and Environment..."
echo "--------------------------------------------------------------------------------"

echo "--------------------------------------------------------------------------------"
echo "\| Adding Environment Variables for AWIPS II Notification"
echo "--------------------------------------------------------------------------------"

echo -e "\e[1;32m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;32m\| AWIPS II Notification Installation - COMPLETE\e[m"
echo -e "\e[1;32m--------------------------------------------------------------------------------\e[m"

%postun
if [ "${1}" = "1" ]; then
   exit 0
fi

echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"
echo -e "\e[1;34m\| AWIPS II Notification Has Been Successfully Removed\e[m"
echo -e "\e[1;34m--------------------------------------------------------------------------------\e[m"

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
%attr(755,root,root) /etc/profile.d/awips2Notification.csh
%attr(755,root,root) /etc/profile.d/awips2Notification.sh
%dir /awips2/notification
#---------------------------------------------------------------------------#
# Delta-Enabled RPM
#---------------------------------------------------------------------------#
%dir %{_component_default_prefix}/delta
%attr(700,root,root) %{_component_default_prefix}/delta/updateManager.sh
%attr(700,root,root) %{_component_default_prefix}/delta/createUpdateRegistry.sh
%{_component_default_prefix}/delta/%{_component_name}
#---------------------------------------------------------------------------#
%dir /awips2/notification/include
/awips2/notification/include/*
%dir /awips2/notification/lib
/awips2/notification/lib/*
%docdir /awips2/notification/licenses
%dir /awips2/notification/licenses
/awips2/notification/licenses/*
%doc /awips2/notification/README
%dir /awips2/notification/src
/awips2/notification/src/*

%defattr(755,awips,fxalpha,755)
%dir /awips2/notification/bin
/awips2/notification/bin/*