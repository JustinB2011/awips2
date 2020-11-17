%define _component_name           awips2-database
%define _component_project_dir    awips2.core/Installer.database
%define _component_default_prefix /awips2/database
#
# AWIPS II Database Spec File
#

Name: %{_component_name}
Summary: AWIPS II Database Installation
Version: %{_component_version}
Release: %{_component_release}%{?dist}
Group: AWIPSII
BuildRoot: /tmp
BuildArch: noarch
Prefix: %{_component_default_prefix}
URL: N/A
License: N/A
Distribution: N/A
Vendor: %{_build_vendor}
Packager: %{_build_site}

AutoReq: no
Provides: awips2-database
Provides: awips2-static-user
Requires: awips2, libpng, perl
Requires: awips2-postgresql
Requires: awips2-psql
Requires: netcdf >= 4.0.0
Requires: netcdf-devel >= 4.0.0


%description
AWIPS II Database Installation - Sets Up The Basic AWIPS II Database, Creating The
Required Tables And Schemas And Populating Static Tables As Needed.

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "${RPM_BUILD_ROOT}" = "/tmp" ]
then
   echo "An Actual BuildRoot Must Be Specified. Use The --buildroot Parameter."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

%build

%install
# Create data directory and other required directories
mkdir -p ${RPM_BUILD_ROOT}/awips2/database/{data,tablespaces,pg_log}
if [ $? -ne 0 ]; then
   exit 1
fi

mkdir -p ${RPM_BUILD_ROOT}/awips2/database/ssl
if [ $? -ne 0 ]; then
   exit 1
fi

CONFIGURATION_DIR="rpms/awips2.core/Installer.database/configuration"
CONF_FILE="postgresql.conf"

cp -p %{_baseline_workspace}/${CONFIGURATION_DIR}/*.{key,crt} \
   ${RPM_BUILD_ROOT}/awips2/database/ssl

cp %{_baseline_workspace}/${CONFIGURATION_DIR}/${CONF_FILE} \
   ${RPM_BUILD_ROOT}/awips2/database/data

PATH_TO_DDL="build.edex/opt/db/ddl"
PATH_TO_REPLICATION="build.edex/opt/db/replication"

# Create A Temporary Directory For The SQL Scripts That The Database
# RPM Will Need.
mkdir -p ${RPM_BUILD_ROOT}/awips2/database/sqlScripts
mkdir -p ${RPM_BUILD_ROOT}/awips2/database/sqlScripts/share
mkdir -p ${RPM_BUILD_ROOT}/awips2/database/sqlScripts/share/sql

CONFIG_FILE_TO_INCLUDE="pg_hba.conf"
EXPECTED_PATH_TO_CONFIG="${PATH_TO_DDL}/setup"
KNOWN_CONFIG_DESTINATION="awips2/database/sqlScripts/share/sql"
# Ensure That We Have Access To The Configuration Files Before Continuing.
if [ ! -f %{_baseline_workspace}/${EXPECTED_PATH_TO_CONFIG}/${CONFIG_FILE_TO_INCLUDE} ]; then
   echo "The ${CONFIG_FILE_TO_INCLUDE} PostgreSQL Configuration File Can Not Be Found."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

# Copy The Configuration File
cp -r %{_baseline_workspace}/${EXPECTED_PATH_TO_CONFIG}/${CONFIG_FILE_TO_INCLUDE} \
   ${RPM_BUILD_ROOT}/${KNOWN_CONFIG_DESTINATION} 

# Copy The SQL Scripts That The Database RPM Will Need To The
# Temporary Directory.
DIRS_TO_COPY=('hmdb' 'migrated' 'setup' 'events' 'ebxml' 'climate')
for dir in ${DIRS_TO_COPY[*]};
do
   cp -r %{_baseline_workspace}/${PATH_TO_DDL}/${dir}/* \
      ${RPM_BUILD_ROOT}/awips2/database/sqlScripts/share/sql
done

# Install replication scripts
mkdir -p ${RPM_BUILD_ROOT}/awips2/database/replication
cp -r %{_baseline_workspace}/${PATH_TO_REPLICATION}/* \
    ${RPM_BUILD_ROOT}/awips2/database/replication

   
# Create our installation log file.
touch ${RPM_BUILD_ROOT}/awips2/database/sqlScripts/share/sql/sql_install.log   

%pre
# Remove any existing postgresql.conf files
if [ -f /awips2/database/data/postgresql.conf ]; then
   rm -f /awips2/database/data/postgresql.conf
fi

%post
if [[ ! -e /awips2/data ]]; then
    ln -s /awips2/database/data /awips2/data
fi
# Get Important Directories Again. Even Exporting Them The First Time Still
# Will Not Make Them Available.
POSTGRESQL_INSTALL="/awips2/postgresql"
PSQL_INSTALL="/awips2/psql"
AWIPS2_DATA_DIRECTORY="/awips2/database/data"
TABLESPACE_DIR="/awips2/database/tablespaces"

SHARE_DIR=${RPM_INSTALL_PREFIX}/sqlScripts/share
SQL_SHARE_DIR=${SHARE_DIR}/sql
if [ "${1}" = "2" ]; then   
   exit 0
fi

function printFailureMessage() {
   echo -e "\e[1;31m\| AWIPS II Database Installation - FAILED\e[m"
   echo -e "\e[1;31m Check the installation log: /awips2/database/sqlScripts/share/sql/sql_install.log\e[m"
   if [ -f /awips2/database/sqlScripts/share/sql/sql_install.log ]; then
      tail -n 6 /awips2/database/sqlScripts/share/sql/sql_install.log
   fi
   
   exit 1
}

AWIPS_DEFAULT_OWNER="awips"
AWIPS_DEFAULT_USER="awips"
AWIPS_DEFAULT_DB_ADMIN="awipsadmin"
AWIPS_DEFAULT_GROUP="fxalpha"
AWIPS_DEFAULT_PORT="5432"

# This Is The Log File That We Will Use To Log All SQL Interactions.
SQL_LOG=${SQL_SHARE_DIR}/sql_install.log

# SQL Data Directories
METADATA=${TABLESPACE_DIR}/metadata
IFHS=${TABLESPACE_DIR}/pgdata_ihfs
MAPS=${TABLESPACE_DIR}/maps
DAMCAT=${TABLESPACE_DIR}/damcat
HMDB=${TABLESPACE_DIR}/hmdb
CLIMATE=${TABLESPACE_DIR}/climate

# Add The PostgreSQL Libraries And The PSQL Libraries To LD_LIBRARY_PATH.
export LD_LIBRARY_PATH=${POSTGRESQL_INSTALL}/lib:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH=${PSQL_INSTALL}/lib:$LD_LIBRARY_PATH

function is_postgresql_running() {
   NUM_PGSQL_COUNT=`/bin/netstat -l | /bin/grep -c "PGSQL.${AWIPS_DEFAULT_PORT}"`
   if [ ${NUM_PGSQL_COUNT} -gt 0 ]; then
      echo "PostgreSQL is already running or PostgreSQL lock files exist!" > \
         /awips2/database/sqlScripts/share/sql/sql_install.log
      printFailureMessage   
   fi
}

function create_sql_element() {
   # $1 == element
   
   mkdir -p ${1}
   update_owner ${1}
}

function update_owner() {
   # $1 == element
   chown ${AWIPS_DEFAULT_OWNER} ${1}
   chgrp ${AWIPS_DEFAULT_GROUP} ${1}
}

function init_db() {
   # move postgresql.conf to a temporary location.
   if [ -f /awips2/database/data/postgresql.conf ]; then
      mv /awips2/database/data/postgresql.conf /awips2/
      rm -rf /awips2/database/data/*
   fi

   su - ${AWIPS_DEFAULT_USER} -c \
      "${POSTGRESQL_INSTALL}/bin/initdb --auth=trust --locale=en_US.UTF-8 --pgdata=${AWIPS2_DATA_DIRECTORY} --lc-collate=en_US.UTF-8 --lc-ctype=en_US.UTF-8" > /dev/null 2>&1
   RC=$?   
      
   if [ -f /awips2/postgresql.conf ]; then
      mv /awips2/postgresql.conf /awips2/database/data
   fi

   return ${RC}
}

function control_pg_ctl() {
   # $1 == pg_ctl command
   su - ${AWIPS_DEFAULT_USER} -c \
      "${POSTGRESQL_INSTALL}/bin/pg_ctl ${1} -D ${AWIPS2_DATA_DIRECTORY} -o \"-p ${AWIPS_DEFAULT_PORT}\" -w" > /dev/null 2>&1
   
}

function execute_initial_sql_script() {
   # Make The Necessary Replacements In The Script.
   perl -p -i -e "s/%{databaseUsername}/${AWIPS_DEFAULT_OWNER}/g" \
      ${1}
   echo ${TABLESPACE_DIR} | sed 's/\//\\\//g' > .awips2_escape.tmp
   TABLESPACE_DIR_ESCAPED=`cat .awips2_escape.tmp`
   rm -f .awips2_escape.tmp
   perl -p -i -e "s/%{tablespace_dir}/${TABLESPACE_DIR_ESCAPED}/g" \
      ${1}

   # $1 == script to execute
   su - ${AWIPS_DEFAULT_USER} -c \
      "${PSQL_INSTALL}/bin/psql -d postgres -U ${AWIPS_DEFAULT_USER} -q -p ${AWIPS_DEFAULT_PORT} -f ${1}" \
      > ${SQL_LOG} 2>&1
}

function update_createHMDB() {
   echo ${TABLESPACE_DIR} | sed 's/\//\\\//g' > .awips2_escape.tmp
   TABLESPACE_DIR_ESCAPED=`cat .awips2_escape.tmp`
   rm -f .awips2_escape.tmp
   perl -p -i -e "s/%{tablespace_dir}%/${TABLESPACE_DIR_ESCAPED}/g" \
      ${SQL_SHARE_DIR}/createHMDB.sql
}

function update_createClimateDb() {
   echo ${TABLESPACE_DIR} | sed 's/\//\\\//g' > .awips2_escape.tmp
   TABLESPACE_DIR_ESCAPED=`cat .awips2_escape.tmp`
   rm -f .awips2_escape.tmp
   perl -p -i -e "s/%{tablespace_dir}%/${TABLESPACE_DIR_ESCAPED}/g" \
      ${SQL_SHARE_DIR}/createClimateDb.sql
}

function execute_psql_sql_script() {
   # $1 == script to execute
   # $2 == database
   
   su - ${AWIPS_DEFAULT_USER} -c \
      "${PSQL_INSTALL}/bin/psql --db ${2} --user ${AWIPS_DEFAULT_DB_ADMIN} -q -p ${AWIPS_DEFAULT_PORT} -f ${1}" \
      >> ${SQL_LOG} 2>&1
}

function copy_addl_config() {
   rm -f ${AWIPS2_DATA_DIRECTORY}/pg_hba.conf
   cp ${SQL_SHARE_DIR}/pg_hba.conf ${AWIPS2_DATA_DIRECTORY}/pg_hba.conf
   update_owner ${AWIPS2_DATA_DIRECTORY}/pg_hba.conf
}

is_postgresql_running

init_db
RC="$?"
if [ ! "${RC}" = "0" ]; then
   printFailureMessage 
fi

create_sql_element ${METADATA}
create_sql_element ${IFHS}
create_sql_element ${DAMCAT}
create_sql_element ${HMDB}
create_sql_element ${CLIMATE}
control_pg_ctl "start"
# Ensure that the database started.
if [ $? -ne 0 ]; then
   printFailureMessage   
fi

execute_initial_sql_script ${SQL_SHARE_DIR}/initial_setup_server.sql > /dev/null 2>&1

/awips2/psql/bin/psql -U awipsadmin -d metadata -c "CREATE EXTENSION postgis;" > /dev/null 2>&1
/awips2/psql/bin/psql -U awipsadmin -d metadata -c "CREATE EXTENSION postgis_topology;" > /dev/null 2>&1
execute_psql_sql_script /awips2/postgresql/share/contrib/postgis-2.4/legacy.sql metadata > /dev/null 2>&1
execute_psql_sql_script ${SQL_SHARE_DIR}/permissions.sql metadata > /dev/null 2>&1
execute_psql_sql_script ${SQL_SHARE_DIR}/fxatext.sql metadata > /dev/null 2>&1

# create the events schema
execute_psql_sql_script ${SQL_SHARE_DIR}/createEventsSchema.sql metadata > /dev/null 2>&1

update_createHMDB > /dev/null 2>&1
su - ${AWIPS_DEFAULT_USER} -c \
   "${SQL_SHARE_DIR}/createHMDB.sh ${PSQL_INSTALL} ${AWIPS_DEFAULT_PORT} ${AWIPS_DEFAULT_DB_ADMIN} ${SQL_SHARE_DIR} ${SQL_LOG}" > /dev/null 2>&1
   
execute_psql_sql_script ${SQL_SHARE_DIR}/createEbxml.sql metadata > /dev/null 2>&1

update_createClimateDb > /dev/null 2>&1
su - ${AWIPS_DEFAULT_USER} -c \
   "${SQL_SHARE_DIR}/createClimateDB.sh ${PSQL_INSTALL} ${POSTGRESQL_INSTALL} ${AWIPS_DEFAULT_PORT} ${AWIPS_DEFAULT_DB_ADMIN} ${SQL_SHARE_DIR} ${SQL_LOG}" > /dev/null 2>&1

# install replication role if this is a central registry server
#case $SITE_IDENTIFIER in
#    ${centralCaseArray} ) 
#        execute_psql_sql_script ${SQL_SHARE_DIR}/createReplicationRole.sql metadata
#        ;;
#    *)  ;;
#esac

${SQL_SHARE_DIR}/alter_database_roles_and_permissions.sh > /dev/null 2>&1

control_pg_ctl "stop" > /dev/null 2>&1

copy_addl_config > /dev/null 2>&1

%preun

%postun
# Remove /awips2/data symlink if it exists
if [[ -h /awips2/data ]]; then
    rm -f /awips2/data
fi

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(600,awips,fxalpha,700)
/awips2/database/ssl
%config(noreplace) /awips2/database/ssl/server.crt
%config(noreplace) /awips2/database/ssl/root.crt
%config(noreplace) /awips2/database/ssl/server.key
%defattr(644,awips,fxalpha,700)
%dir /awips2/database/data
%dir /awips2/database/tablespaces
%defattr(644,awips,fxalpha,755)
%dir /awips2/database/sqlScripts
%dir /awips2/database/replication
%dir /awips2/database/sqlScripts/share
/awips2/database/data/postgresql.conf
/awips2/database/sqlScripts/share/sql/sql_install.log
/awips2/database/sqlScripts/share/sql/pg_hba.conf
/awips2/database/replication/README

%defattr(755,awips,fxalpha,755)
%dir /awips2/database/sqlScripts/share/sql
/awips2/database/sqlScripts/share/sql/*.sql
/awips2/database/sqlScripts/share/sql/*.sh
/awips2/database/replication/setup-standby.sh
/awips2/database/replication/replication-config.sh
