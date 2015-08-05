#!/bin/bash

export WF="$1"
export HAL="$2"

if [ -z "$1" ] ; then
   echo "test-versions.sh WF_VERSION HAL_VERSION <standalone|domain>"
   exit 1
fi

if [ -z "$2" ] ; then
   echo "test-versions.sh WF_VERSION HAL_VERSION <standalone|domain>"
   exit 1
fi

export WF_VERSION=$1
export HAL_VERSION=$2
export SERVER_MODE=$3

sh run-suite.sh




