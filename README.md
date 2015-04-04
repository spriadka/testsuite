# Testsuite
Selenium testsuite for the HAL management console. It uses [Drone](https://docs.jboss.org/author/display/ARQ/Drone) 
and [Graphene](https://docs.jboss.org/author/display/ARQGRA2/Home) Arquillian extensions.

## Prerequisites

* JDK 8 or higher
* Maven 3.0.4 or higher
* Firefox browser (tested on 31.2.0 esr version, will probably not run with far older or younger versions)

In order to work on the console you may want running WildFly
instance on your local host. You can download it here:

<http://www.wildfly.org/download/>

## How to run tests

`mvn test -P{profile} -Djboss.dist=${path_to_server_home} -Darq.extension.webdriver.firefox_binary=${path_to_firefox_binary}`

### Required profile parameter

Can be one of those:
* `-Pwildfly` ... run testsuite against standalone mode of Wildfly (standalone-full-ha)
* `-Pwildfly-domain`` ... run testsuite against domain mode of Wildfly
* `-Peap` ... run testsuite against standalone mode of provided EAP (standalone-full-ha)
* `-Peap-domain` ... run testsuite against domain mode of provided EAP

### jboss.dist parameter

Path to server home folder.
* Optional for Wildfly. If not provided Wildfly 9.0.0.Beta2 will be downloaded by maven.
* Required for EAP.
E.g. `-Djboss.dist=/home/user/workspace/wildfly/build/target/wildfly-9.0.0.Alpha2-SNAPSHOT/`

### Optional arq.extension.webdriver.firefox_binary parameter

Path to Firefox binary file. If not provided system default firefox will be used.
E.g. `-Darq.extension.webdriver.firefox_binary=/home/user/apps/firefox-31.2.0esr/firefox`

## Tips

* If you want tests to be run on background use vncserver. E.g. 

`vncserver :10 -geometry 1920x1080`

`export DISPLAY=:10`


## Known issues

* For `-Pwildfly-domain` profile it's currently necessary to start the domain manually.
* No sreenshots on test failure neither test error currently (It seems Arquillian unlike Surefire thinks they passed).

## Problems?

Ping us on IRC freenode.net#wildfly-management

Have fun.
