package org.jboss.hal.testsuite.page.runtime;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

/**
 * @author jcechace
 *
 * This class represents a meta page entry point to the Runtime part of the consle in domain.
 * As such it is meant for navigation purposes only and thus can't be instantiated. Also note
 * that the actual landing page is determined by console and may change in the future.
 *
 */
@Location("#hosts")
public class DomainRuntimeEntryPoint extends BasePage {

    private static final String NBSP = "\u00a0";
    private static final String HOSTS = NBSP + "Hosts";
    private static final String SERVER_GROUPS = NBSP + "Server Groups";

    /**
     * timeout is waitModelInterval
     * @param label
     * @return
     */
    public boolean leftMenuItemIsVisible(String label){
        By selector = getMenuEqualsSelector(label);
        try {
            Graphene.waitModel().until().element(selector).is().visible();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     *
     * @param label
     * @param timeout in seconds
     * @return
     */
    public boolean leftMenuItemIsVisible(String label, Long timeout){
        By selector = getMenuContainsSelector(label);
        try {
            Graphene.waitModel().withTimeout(timeout, TimeUnit.SECONDS).until().element(selector).is().visible();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public DomainRuntimeEntryPoint selectHosts(){
        return (DomainRuntimeEntryPoint) super.selectMenu(HOSTS);
    }

    public DomainRuntimeEntryPoint selectServerGroups(){
        return (DomainRuntimeEntryPoint) super.selectMenu(SERVER_GROUPS);
    }
}
