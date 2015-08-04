#!/bin/bash

#
# Env args:
# - FIREFOX_BINARY: path to the FF binary
# - WF_VERSION: the wildfly version to install
# - TMPDIR: the tmp directory to download and install into
# - SERVER_MODE: which tests to execute (standalone || domain)
#

if [ "${WF_VERSION}x" == "x" ] ; then
   echo "WF_VERSION has to be specified!"
   exit 1
fi

if [ "${FIREFOX_BINARY}x" == "x" ] ; then
   echo "FIREFOX_BINARY has to be specified!"
   exit 1
fi

if [ "${SERVER_MODE}x" == "x" ] ; then
   echo "SERVER_MODE has to be specified!"
   exit 1
fi


if [ "${MVN_HOME}x" == "x" ] ; then
   MVN = $(which mvn)
   export MVN_BIN=$MVN
else
   MVN = "$MVN_HOME/bin/mvn"
   export MVN_BIN=$MVN
fi

if [ "${MVN_BIN}x" == "x" ] ; then
   echo "MVN_HOME has to be specified!"
   exit 1
fi

#
# function definitions
#

function prepareServer {
  if [ "${WF_VERSION}x" != "x" ]; then
    wget -nc -O $TMPDIR/wildfly-${WF_VERSION}.zip http://download.jboss.org/wildfly/${WF_VERSION}/wildfly-${WF_VERSION}.zip
    rm -rf $TMPDIR/wildfly-${WF_VERSION}
    unzip -q $TMPDIR/wildfly-${WF_VERSION}.zip -d $TMPDIR
  fi

  export SERVER_DIR_PATH=$TMPDIR/wildfly-${WF_VERSION}

  echo "test suite server at: $SERVER_DIR_PATH"

  #add user
  $SERVER_DIR_PATH/bin/add-user.sh -s admin asd1asd!

  # unsecure
  sed -i '' 's/http-interface security-realm=\"ManagementRealm\"/http-interface /' $SERVER_DIR_PATH/standalone/configuration/standalone-full-ha.xml
  sed -i '' 's/http-interface security-realm=\"ManagementRealm\"/http-interface /' $SERVER_DIR_PATH/domain/configuration/host.xml
}


function shutdownServer {
    $SERVER_DIR_PATH/bin/jboss-cli.sh -c --command=:shutdown || true
}

function runServer {
  shutdownServer
  if [ "$SERVER_MODE" == "domain" ]; then
    $SERVER_DIR_PATH/bin/domain.sh &
  else
    $SERVER_DIR_PATH/bin/standalone.sh -c standalone-full-ha.xml &
  fi

  echo $$>$TMPDIR/HAL_TS_WF.pid

  sleep 20
}

function prepareSuite {
  cd $WORKSPACE
  git clone https://github.com/hal/testsuite.git
  pushd testsuite
  git log --no-merges -1 --pretty=format:"Testsuite commit %h %an %s"
  popd
}

function killServer() {
  PID="$TMPDIR/HAL_TS_WF.pid"
  if [ -f $PID ]; then
    kill -9 $(cat $PID)
  else
    echo "The File '$PID' Does Not Exist"
  fi

}

function runSuite {

  if [ -n "$DTEST" ]; then
    export DTEST="-Dtest=$DTEST"
  fi

  if [ "$SERVER_MODE" == "domain" ]; then
    $MVN_BIN test -Pdomain -Djboss.dist=$SERVER_DIR_PATH $DTEST -Darq.extension.webdriver.firefox_binary="${FIREFOX_BINARY}"  || true
  else
    $MVN_BIN test -Pstandalone -Djboss.dist=$SERVER_DIR_PATH $DTEST -Darq.extension.webdriver.firefox_binary="${FIREFOX_BINARY}" || true
  fi
}

function prepareCore {
  pushd core
  git log --no-merges -1 --pretty=format:"Core commit %h %an %s"
  mvn clean install -Dmaven.repo.local=$WORKSPACE/maven_repo
  popd
  new_artifact=$(ls core/build/app/target/*-console-*-resources.jar | xargs)
  cp $new_artifact .

  # amending eap bits
  original_artifact=$(ls $SERVER_DIR_PATH/modules/system/layers/base/org/jboss/as/console/**/*.jar)
  cp $new_artifact $original_artifact
}

#
# Execution
#

prepareServer

#prepareCore

killServer

runServer

runSuite

shutdownServer
