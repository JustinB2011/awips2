#!/bin/bash
dir="$( cd "$(dirname "$0")" ; pwd -P )"
pushd $dir 
. ../buildEnvironment.sh


if [ -z "$1" ]; then
  echo "supply type (el7)"
  exit
fi
os_version=$1

existing=$(sudo docker images |grep awips-ade | grep $1 | awk '{ print $3 }')
if [ ! -z "$existing" ]; then
   sudo docker rmi $existing
fi
pushd /awips2/repo/awips2-builds/build/awips-ade
sudo docker build -t unidata/awips-ade -f Dockerfile.awips-ade.${os_version} .
dockerID=$(sudo docker images | grep awips-ade | grep latest | awk '{print $3}' | head -1 )
sudo docker tag $dockerID unidata/awips-ade:${AWIPSII_VERSION}-${os_version} 
sudo docker rmi unidata/awips-ade:latest
sudo docker push unidata/awips-ade
