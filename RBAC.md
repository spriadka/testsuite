# Setup WildFly/EAP for HAL RBAC tests

## Prerequisites 
See Prerequisites in [README.md](README.md)

## Common paths for further configuration
(modify used paths according to your directory structure/distribution used)

* `export SERVER_DIR=${PATH_TO_DISTRIBUTION}/jboss-eap-7.0`
* `export SCRIPT_DIR=${PATH_TO_HAL_TESTUITE}/rbac/src/main/resources`

## Domain configuration
HAL RBAC domain features are tested against two node domain. 

* Enable RBAC provider: `sed -i 's/<access-control provider="simple"/<access-control provider="rbac"/g' ${SERVER_DIR}/domain/configuration/domain.xml`
* Setup users: `cp ${SCRIPT_DIR}/mgmt-users.properties ${SERVER_DIR}/domain/configuration`
* Create basedir for slave node in domain: `cp -r ${SERVER_DIR}/domain ${SERVER_DIR}/domain2`
* Start master: `${SERVER_DIR}/bin/domain.sh`
* Start slave: `${SERVER_DIR}/bin/domain.sh --host-config=host-slave.xml -Djboss.domain.base.dir=${SERVER_DIR}/domain2 -Djboss.host.name=slave -Djboss.domain.master.address=127.0.0.1 -Djboss.bind.address=127.0.0.2 -Djboss.bind.address.management=127.0.0.2 -Djboss.bind.address.unsecure=127.0.0.2`
* Configure domain using CLI script: `${SERVER_DIR}/bin/jboss-cli.sh -c --file="${SCRIPT_DIR}/rbac-setup-domain.batch"`

## Standalone configuration

* Enable RBAC provider: `sed -i 's/<access-control provider="simple"/<access-control provider="rbac"/g' ${SERVER_DIR}/standalone/configuration/standalone-full-ha.xml`
* Setup users: `cp ${SCRIPT_DIR}/mgmt-users.properties ${SERVER_DIR}/standalone/configuration`
* Start server: `${SERVER_DIR}/bin/standalone.sh -c "standalone-full-ha.xml"`
* Configure server using CLI script: `${SERVER_DIR}/bin/jboss-cli.sh -c --file="${SCRIPT_DIR}/rbac-setup-standalone.batch"`
