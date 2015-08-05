#!/bin/bash

#
# Env args:
# - FIREFOX_BINARY: path to the FF binary
# - WF_VERSION: the wildfly version to install
# - TMPDIR: the tmp directory to download and install into
# - SERVER_MODE: which tests to execute (standalone || domain)
# - DOWNLOAD_URL (optional): the location for required binaries
# - HAL_VERSION: the HAL version to install
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


if [ "${M2_HOME}x" == "x" ] ; then
   echo "M2_HOME has to be specified!"
   exit 1
fi

if [ "${HAL_VERSION}x" == "x" ] ; then
   echo "HAL_VERSION has to be specified!"
   exit 1
fi

#
# function definitions
#

function prepareServer {

  if [ "${DOWNLOAD_URL}x" == "x" ]; then
    export URL="http://download.jboss.org/wildfly/${WF_VERSION}"
  else
    export URL=$DOWNLOAD_URL
  fi

  if [ "${WF_VERSION}x" != "x" ]; then
    wget -nc -O $TMPDIR/wildfly-${WF_VERSION}.zip "$URL/wildfly-${WF_VERSION}.zip"
    rm -rf $TMPDIR/wildfly-${WF_VERSION}
    unzip -q $TMPDIR/wildfly-${WF_VERSION}.zip -d $TMPDIR
  fi

  export SERVER_DIR_PATH=$TMPDIR/wildfly-${WF_VERSION}

  echo "test suite server at: $SERVER_DIR_PATH"

  #add user
  $SERVER_DIR_PATH/bin/add-user.sh -s admin asd1asd!

  # unsecure
  if [ "$(uname -s)" == "Darwin" ]; then
    sed -i '.bak' 's/http-interface security-realm=\"ManagementRealm\"/http-interface /' $SERVER_DIR_PATH/standalone/configuration/standalone-full-ha.xml
    sed -i '.bak' 's/http-interface security-realm=\"ManagementRealm\"/http-interface /' $SERVER_DIR_PATH/domain/configuration/host.xml
  else
    sed 's/http-interface security-realm=\"ManagementRealm\"/http-interface /' -i $SERVER_DIR_PATH/standalone/configuration/standalone-full-ha.xml
    sed 's/http-interface security-realm=\"ManagementRealm\"/http-interface /' -i $SERVER_DIR_PATH/domain/configuration/host.xml
  fi

}


function shutdownServer {
    $SERVER_DIR_PATH/bin/jboss-cli.sh -c --command=:shutdown || true
}

function runServer {
  shutdownServer
  if [ "$SERVER_MODE" == "domain" ]; then
    eval "($SERVER_DIR_PATH/bin/domain.sh) &"
    echo $!>$TMPDIR/HAL_TS_WF.pid
  else
    eval "($SERVER_DIR_PATH/bin/standalone.sh -c standalone-full-ha.xml) &"
    echo $!>$TMPDIR/HAL_TS_WF.pid
  fi

  echo "WF PID: $(cat $TMPDIR/HAL_TS_WF.pid)"
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
  sh kill.sh
}

function runSuite {

  if [ -n "$DTEST" ]; then
    export DTEST="-Dtest=$DTEST"
  fi

  if [ "$SERVER_MODE" == "domain" ]; then
    sh $M2_HOME/bin/mvn test -Pdomain -Djboss.dist=$SERVER_DIR_PATH $DTEST -Darq.extension.webdriver.firefox_binary="${FIREFOX_BINARY}"  || true
  else
    sh $M2_HOME/bin/mvn test -Pstandalone -Djboss.dist=$SERVER_DIR_PATH $DTEST -Darq.extension.webdriver.firefox_binary="${FIREFOX_BINARY}" || true
  fi
}

function prepareCore {
  sh $M2_HOME/bin/mvn dependency:copy -DoutputDirectory=$TMPDIR -Dartifact=org.jboss.as:jboss-as-console:$HAL_VERSION:jar:resources
  new_artifact="$TMPDIR/jboss-as-console-$HAL_VERSION-resources.jar"

  original_artifact=$(ls $SERVER_DIR_PATH/modules/system/layers/base/org/jboss/as/console/**/*.jar)
  rm -f $original_artifact
  cp -v $new_artifact $original_artifact
}

#
# Execution
#

prepareServer

prepareCore

killServer

runServer

runSuite

shutdownServer
