package org.jboss.hal.testsuite.util;

import org.jboss.hal.testsuite.page.home.HomePage;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class Authentication {

    private static Map<WebDriver, Boolean> loginMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(Console.class);
    private boolean authenticated = false;
    private WebDriver browser;

    public static Authentication with(WebDriver browser) {
        return new Authentication(browser);
    }

    private Authentication(WebDriver browser) {
        this.browser = browser;
        if(loginMap.containsKey(browser)){
            authenticated = loginMap.get(browser);
        }else{
            loginMap.put(browser, authenticated);
        }
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
        loginMap.replace(browser, true);
        Console.withBrowser(browser).refreshAndNavigate(HomePage.class);
    }

    public void logout() {
        Console.withBrowser(browser).logout();
        authenticated = false;
        loginMap.replace(browser, false);
    }

    public void authenticate(RbacRole role) {
        authenticate(role.username, role.password);
    }

}
