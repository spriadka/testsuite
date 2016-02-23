package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.PropUtils;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;

/**
 * Created by pcyprian on 16.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class DomainElementaryAccessControlTestCase {

    private static final AddressTemplate ADDRESS_TEMPLATE = AddressTemplate.of("/profile=default/subsystem=datasources/data-source=*");

    private String addressName = "ds_" + RandomStringUtils.randomAlphanumeric(5);
    private DefaultContext statementContext = new DefaultContext();
    private FinderNavigation navigation;
    private ResourceAddress address = ADDRESS_TEMPLATE.resolve(statementContext, addressName);
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;

    @BeforeClass
    public static void beforeClass() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void afterClass() {
        dispatcher.close();
    }

    @Drone
    public WebDriver browser;

    @Before
    public void before() {
        dispatcher.execute(new Operation.Builder(ModelDescriptionConstants.ADD, address).param("connection-url", "url")
                .param("jndi-name", "java:/" + addressName).param("driver-name", "h2")
                .param("user-name", "un").param("password", "pw").param("enabled", "false").build());
        navigation = new FinderNavigation(browser, DomainConfigurationPage.class);
    }

    @After
    public void after() {
        ResourceAddress address = ADDRESS_TEMPLATE.resolve(statementContext, addressName);
        dispatcher.execute(new Operation.Builder(ModelDescriptionConstants.REMOVE, address).build());
    }


    @Test
    public void administrator() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        goToDatasource();
        checkAttribute(true, true);
        checkSensitiveAttribute(true, true);
        accessRestrictedPage(true);
    }

    @Test
    public void monitor() {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);
        goToDatasource();
        checkAttribute(true, false);
        checkSensitiveAttribute(false, false);
        accessRestrictedPage(false);
    }

    @Test
    public void auditor() {
        Authentication.with(browser).authenticate(RbacRole.AUDITOR);
        goToDatasource();
        checkAttribute(true, false);
        checkSensitiveAttribute(true, false);
        accessRestrictedPage(true);
    }

    @Test
    public void mainAdministrator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAIN_ADMINISTRATOR);
        goToDatasource();
        checkAttribute(true, true);
        checkSensitiveAttribute(true, true);
        accessRestrictedPage(true);
    }

    @Test
    public void mainMonitor() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAIN_MONITOR);
        goToDatasource();
        checkAttribute(true, false);
        checkSensitiveAttribute(false, false);
        accessRestrictedPage(false);
    }

    @Test
    public void hostMasterAdministrator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_ADMINISTRATOR);
        goToDatasource();
        checkAttribute(true, false);
        checkSensitiveAttribute(false, false);
        accessRestrictedPage(false);
    }



    private void goToDatasource() {
        navigation.step(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                .step(FinderNames.PROFILE, "default")
                .step(FinderNames.SUBSYSTEM, "Datasources")
                .step("Type", "Non-XA")
                .step("Datasource", addressName)
                .selectRow().invoke("View");
    }

    private ConfigFragment getConfig(String tab) {
        String labelClass = PropUtils.get("configarea.label.class");
        String contentClass = PropUtils.get("page.content.rhs.class");
        Library.letsSleep(2000);
        By selector = ByJQuery.selector("." + labelClass + ":contains('" + tab + "')");
        browser.findElement(selector).click();
        return Graphene.createPageFragment(ConfigFragment.class, browser.findElement(ByJQuery.selector("." + contentClass + ":visible")));
    }

    private void checkAttribute(boolean read, boolean write) {
        ConfigFragment attributes = getConfig("Attributes");
        if (read) {
            WebElement jndi = getElementByLabel("JNDI");
            boolean enabled = jndi.isEnabled();
            assertEquals("Input element is enabled: " + enabled + ", but expected is " + read, read, enabled);
            if (enabled) {
                String jndiValue = getAttributeValueForElemenentWithLabel(jndi);
                assertEquals("java:/" + addressName, jndiValue);
            }
        }
        if (write) {
            Editor editor = attributes.edit();
            editor.text("jndiName", "java:jboss/" + addressName);
            attributes.save();
            Library.letsSleep(1500);
            verifier.verifyAttribute(address, "jndi-name", "java:jboss/" + addressName);
        }
        else {
            boolean visibleEditBtn = true;
            try {
                attributes.edit();
            } catch (NoSuchElementException ex) {
                visibleEditBtn = false;
            }
            assertEquals("Should not be editable.", visibleEditBtn, write);
        }
    }

    private void checkSensitiveAttribute(boolean read, boolean write) {
        ConfigFragment security = getConfig("Security");
        if (read) {
            WebElement username = getElementByLabel("Username");
            boolean enabled = username.isEnabled();
            assertEquals("Input element is enabled: " + enabled + ", but expected is " + read, read, enabled);
            if (enabled) {
                String usernameValue = getAttributeValueForElemenentWithLabel(username);
                assertEquals("un", usernameValue);
            }
        }
        if (write) {
            Editor editor = security.edit();
            editor.text("username", "sa");
            security.save();
            Library.letsSleep(1000);
            verifier.verifyAttribute(address, "user-name", "sa");
        }
        else {
            boolean visibleEditBtn = true;
            try {
                security.edit();
            } catch (NoSuchElementException ex) {
                visibleEditBtn = false;
            }
            assertEquals("Should not be editable.", visibleEditBtn, write);
        }
    }

    private WebElement getElementByLabel(String name) {

        WebElement nameCell = browser.findElement
                (By.xpath("//div[@class='form-item-title' and contains(text(), '" + name + "')]"));

        if (nameCell == null) {
            return null;
        }
        return nameCell;
    }

    private String getAttributeValueForElemenentWithLabel(WebElement element) {
        WebElement valueCell = element.findElement(By.xpath("./ancestor::td[1]/following-sibling::td//span"));
        if (valueCell == null) {
            return "";
        }
        return valueCell.getText();
    }


    private void accessRestrictedPage(boolean shouldSucceed) {
        navigation.step(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                .step(FinderNames.PROFILE, "default")
                .step(FinderNames.SUBSYSTEM, "Security")
                .step("Security Domain", "other");
        boolean permissions = true;
        try {
            navigation.selectRow();
        } catch (TimeoutException ex ) {
            permissions = false;
            browser.findElement(By.className("icon-remove")).click();
        }
        assertEquals("Problem with access to page.",  shouldSucceed, permissions);
    }
}
