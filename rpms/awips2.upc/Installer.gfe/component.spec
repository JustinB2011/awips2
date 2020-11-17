%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')
%define _build_arch %(uname -i)
%define _python_build_loc %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

#
# GFE Python gfe Spec File
#
Name: awips2-python-gfe
Summary: AWIPS II Python GFE Files
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
requires: awips2-python
provides: awips2-python-gfe

%description
AWIPS II Python GFE Site-Package

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "%{_build_root}" = "" ]
then
   echo "A Build Root has not been specified."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

rm -rf %{_build_root}
if [ $? -ne 0 ]; then
   exit 1
fi

%build

%install

mkdir -p %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe
if [ $? -ne 0 ]; then
   exit 1
fi

# Copy source files to site-packages/gfe/
pysrc=(%{_baseline_workspace}/deploy.edex.awips2/esb/data/utility/common_static/base/python/
%{_baseline_workspace}/com.raytheon.viz.textworkstation/localization/textws/python/
%{_baseline_workspace}/com.raytheon.edex.plugin.gfe/utility/common_static/base/gfe/python/
%{_baseline_workspace}/com.raytheon.uf.common.localization.python/utility/common_static/base/python/
%{_baseline_workspace}/rpms/awips2.core/Installer.localization/utility/common_static/configured/OAX/gfe/python/
%{_baseline_workspace}/com.raytheon.uf.common.python/utility/common_static/base/python/
%{_baseline_workspace}/com.raytheon.uf.common.status/utility/common_static/base/python/
%{_baseline_workspace}/com.raytheon.uf.edex.activetable/utility/common_static/base/vtec/
%{_baseline_workspace}/com.raytheon.uf.common.time/utility/common_static/base/python/time/
%{_baseline_workspace}/com.raytheon.uf.edex.dataaccess/utility/common_static/base/python/dataaccess/
%{_baseline_workspace}/com.raytheon.edex.plugin.gfe/utility/common_static/base/gfe/python/)

for dir in "${pysrc[@]}"; do
  echo ${dir}
  cp ${dir}*.py %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/
done

# Copy entire directories to site-packages/gfe/ (each requires __init__.py)
pydirs=(%{_baseline_workspace}/com.raytheon.viz.gfe/localization/gfe/userPython/utilities/
%{_baseline_workspace}/com.raytheon.edex.plugin.gfe/utility/common_static/base/gfe/textproducts/
%{_baseline_workspace}/com.raytheon.uf.tools.gfesuite/cli/src/activeTable/)

for dir in "${pydirs[@]}"; do
  cp -R ${dir} %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/
done

mv %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/textproducts/templates/product/*.py %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/
mv %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/textproducts/templates/utility/*.py %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/

cp %{_baseline_workspace}/rpms/awips2.upc/Installer.gfe/__init__.py %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/activeTable/
cp %{_baseline_workspace}/rpms/awips2.upc/Installer.gfe/__init__.py %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe/utilities/

find %{_build_root}/awips2/python/lib/python2.7/site-packages/gfe -name "*.pyc" | xargs rm -rf 

%pre
if [ -d /awips2/python/lib/python2.7/site-packages/gfe ]; then
   rm -rf /awips2/python/lib/python2.7/site-packages/gfe
fi

%post

%preun

%postun
if [ -d /awips2/python/lib/python2.7/site-packages/gfe ]; then
   rm -rf /awips2/python/lib/python2.7/site-packages/gfe
fi

%clean
rm -rf %{_build_root}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/python/lib/python2.7/site-packages/gfe
/awips2/python/lib/python2.7/site-packages/gfe*
