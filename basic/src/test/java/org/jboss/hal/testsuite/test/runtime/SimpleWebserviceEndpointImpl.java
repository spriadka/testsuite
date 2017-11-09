package org.jboss.hal.testsuite.test.runtime;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

/**
 * Created by pcyprian on 13.11.15.
 */
@WebService(
        endpointInterface = "org.jboss.hal.testsuite.test.runtime.SimpleWebserviceEndpointIface",
        targetNamespace = "org.jboss.hal.testsuite.test.runtime",
        serviceName = "SimpleService"
)
public class SimpleWebserviceEndpointImpl {

    @Resource
    WebServiceContext ctx;

    // method driven injection, with default name to be computed
    private String string1;
    @Resource(name = "string2")
    private String string2;
    // XML driven injection
    private String string3;

    @Resource
    private void setString1(final String s) {
        string1 = s;
    }

    private boolean postConstructCalled;

    @PostConstruct
    private void init() {
        postConstructCalled = true;
    }

    public String echo(final String s) {
        if (!postConstructCalled) throw new RuntimeException("@PostConstruct not called");
        if (!"Ahoj 1".equals(string1)) throw new RuntimeException("@Resource String with default name not injected");
        if (!"Ahoj 2".equals(string2)) throw new RuntimeException("@Resource String with explicit name not injected");
        if (!"Ahoj 2".equals(string3)) throw new RuntimeException("@Resource String with DD driven injection not injected");
        if (ctx == null) throw new RuntimeException("@Resource WebServiceContext not injected");
        return s;
    }

}
