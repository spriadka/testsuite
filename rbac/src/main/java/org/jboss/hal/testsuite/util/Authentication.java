package org.jboss.hal.testsuite.util;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class Authentication {

    private static final Logger log = LoggerFactory.getLogger(Console.class);
    private static boolean authenticated = false;
    private WebDriver browser;

    public static Authentication with(WebDriver browser){
        return new Authentication(browser);
    }

    private Authentication(WebDriver browser) {
        this.browser = browser;
    }

    public void authenticate(String username, String password) {
        if (authenticated) {
            log.debug("# Already Logged in. Trying to Logout");
            logout();
        }
        log.debug("# Trying to authenticate using following credentials");
        log.debug("# username: " + username);
        log.debug("# password: " + password);

        String authUrl = "localhost:9990/management/";
        String protocol = "http";
        browser.get(protocol + "://" + username + ":" + password + "@" + authUrl);
        authenticated = true;
    }

    public void logout(){
        Console.withBrowser(browser).logout();
        authenticated = false;
    }

    public void authenticate(RbacRole role) {
        authenticate(role.username, role.password);
    }

}
