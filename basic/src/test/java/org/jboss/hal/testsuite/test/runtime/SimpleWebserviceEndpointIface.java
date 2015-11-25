package org.jboss.hal.testsuite.test.runtime;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Created by pcyprian on 13.11.15.
 */
@WebService
@SOAPBinding
public interface SimpleWebserviceEndpointIface {

    String echo(String s);

}
