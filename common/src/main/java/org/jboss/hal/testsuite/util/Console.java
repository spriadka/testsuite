package org.jboss.hal.testsuite.util;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.fragment.PopUpFragment;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.formeditor.PropertyEditor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableFragment;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jcechace on 18/02/14.
 */
public class Console {

    private static final Logger log = LoggerFactory.getLogger(Console.class);

    private WebDriver browser;

    public static Console withBrowser(WebDriver browser) {
        Console console = new Console();
        console.browser = browser;

        return console;
    }

    // Prevent constructor instantiation
    private Console() {
    }

    /**
     * Wait until the application is loaded.
     */
    public Console waitUntilLoaded() {
        // TODO: this should rather wait until the loading box is not present
        Graphene.waitModel().withTimeout(30, TimeUnit.SECONDS).until().element(By.className("hal-ProgressElement")).is().not().visible();
        return this;
    }

    public void refresh() {
        browser.navigate().refresh();
        waitUntilFinished();
    }

    /**
     * Wait until content is loaded.
     */
    public void waitForContent() {
        // TODO: might be unreliable
        Graphene.waitAjax().until().element(By.className("content-header-label")).is().present();
    }

    /**
     * Waits until operation is finished (progress bar is hidden)
     */
    public void waitUntilFinished() {
        By selector = By.className("hal-ProgressElement");
        Graphene.waitModel().until().element(selector).is().not().visible();
    }

    /**
     * Retrieves currently opened popup menu
     *
     * @param clazz
     * @param selector
     * @param <T>
     * @return currently opened menu
     */
    public <T extends PopUpFragment> T openedPopup(Class<T> clazz, By selector) {
        Graphene.waitGui().until().element(PopUpFragment.ROOT_SELECTOR).is().present();

        WebElement popupRoot = browser.findElement(selector);
        T popup = Graphene.createPageFragment(clazz, popupRoot);

        return popup;
    }

    public <T extends PopUpFragment> T openedPopup(Class<T> clazz) {
        return openedPopup(clazz, PopUpFragment.ROOT_SELECTOR);
    }

    public PopUpFragment openedPopup() {
        return openedPopup(PopUpFragment.class, PopUpFragment.ROOT_SELECTOR);
    }


    /**
     * Retrieves currently opened window using specified selector.
     *
     * @param clazz
     * @param selector
     * @param <T>
     * @return currently opened window
     */
    public <T extends WindowFragment> T openedWindow(Class<T> clazz, By selector) {
        Graphene.waitGui().until().element(selector).is().present();

        WebElement windowRoot = browser.findElement(selector);
        T window = Graphene.createPageFragment(clazz, windowRoot);

        return window;
    }

    /**
     * Retrieves currently opened window using default selector.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends WindowFragment> T openedWindow(Class<T> clazz) {
        return openedWindow(clazz, WindowFragment.ROOT_SELECTOR);

    }

    /**
     * Retrieves currently opened window with specified head title using default selector.
     *
     * @param headTitle
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends WindowFragment> T openedWindow(String headTitle, Class<T> clazz) {
        By selector = WindowFragment.ROOT_SELECTOR;
        Graphene.waitGui().until().element(selector).is().present();

        List<WebElement> windowEelements = browser.findElements(selector);
        T window = null;

        for (WebElement el : windowEelements) {
            window = Graphene.createPageFragment(clazz, el);
            if (window.getHeadTitle().equals(headTitle)) {
                return window;
            }
        }
        // Window with given title not found
        throw new NoSuchElementException("Unable to find window with title '" + headTitle
                + "' using " + selector);
    }

    public WindowFragment openedWindow() {
        return openedWindow(WindowFragment.class, WindowFragment.ROOT_SELECTOR);
    }


    /**
     * Finds out the number of opened windows
     *
     * @param selector the selector used to find the root of each window
     * @return number of windows currently opened
     */
    public int getWindowCount(By selector) {
        List<WebElement> elements = browser.findElements(selector);
        return elements.size();
    }

    /**
     * Finds out the number of default popup windows
     *
     * @return number of windows currently opened
     */
    public int getWindowCount() {
        return getWindowCount(WindowFragment.ROOT_SELECTOR);
    }

    /**
     * Convenience method to determine whether there is a window currently opened.
     *
     * @param selector the selector used to find the root of each window
     * @param time     time to wait
     * @param unit     time unit
     * @return <code>true</code> if there is a window after waiting timeout, false otehrwise
     */
    public boolean isWindowOpen(By selector, long time, TimeUnit unit) {
        browser.manage().timeouts().implicitlyWait(time, unit);
        try {
            Graphene.waitModel().withTimeout(time, unit).until().element(selector).is().not().visible();
            return false;
        } catch (TimeoutException e) {
            return true;
        }
    }

    /**
     * Convenience method to determine whether there is a window currently opened.
     *
     * @return <code>true</code> if there is a window after 5 seconds, false otehrwise
     */
    public boolean isWindowOpen() {
        return isWindowOpen(WindowFragment.ROOT_SELECTOR, 5, TimeUnit.SECONDS);
    }

    public <T extends WizardWindow> T openedWizard(Class<T> clazz) {
        return openedWindow(clazz);
    }

    public WizardWindow openedWizard() {
        return openedWindow(WizardWindow.class);
    }


    public <T extends ResourceTableFragment> T getTableByHeader(String label, Class<T> clazz,
                                                                WebElement root) {
        String cssClass = PropUtils.get("resourcetable.class");

        String tableSelector = "table[contains(@class, '" + cssClass + "')]";
        String headerSelector = "//th/descendant-or-self::*[contains(text(), '" + label + "')]";
        By selector = By.xpath(".//" + tableSelector + headerSelector +
                "/ancestor::" + tableSelector);

        WebElement tableRoot = findElement(selector, root);
        T table = Graphene.createPageFragment(clazz, tableRoot);

        return table;
    }


    public ResourceTableFragment getTableByHeader(String label) {
        return getTableByHeader(label, ResourceTableFragment.class, null);
    }

    public PropertyEditor getPropertyEditor(WebElement root) {
        PropertyEditor properties = Graphene.createPageFragment(PropertyEditor.class, root);

        return properties;
    }

    public WebElement findElement(By selector, WebElement root) {
        WebElement element = null;
        if (root == null) {
            element = browser.findElement(selector);
        } else {
            if (!root.isDisplayed()) {
                log.warn("Looking for element in hidden root!");
            }
            element = root.findElement(selector);
        }
        return element;
    }

    public WebElement findElement(By selector, BaseFragment fragment) {
        WebElement root = fragment.getRoot();
        return findElement(selector, root);
    }

    /**
     * Workaround for org.openqa.selenium.WebDriver.Window.maximize() not working properly in some environments.
     */
    public Console maximizeWindow() {
        int maxWidth = Integer.parseInt(ConfigUtils.get("window.max.width", "1920"));
        int maxHeight = Integer.parseInt(ConfigUtils.get("window.max.height", "1080"));
        Dimension maxDimension = new Dimension(maxWidth, maxHeight);
        browser.manage().window().setSize(maxDimension);
        return this;
    }

    public void pageDown() {
        List<WebElement> elements = browser.findElements(By.id(PropUtils.get("page.scrollpanel.id")));
        elements.stream().filter(WebElement::isDisplayed).forEach(e -> e.sendKeys(Keys.PAGE_DOWN));
        Library.letsSleep(100);
    }

    public <T extends BasePage> Console refreshAndNavigate(Class<T> clazz) {
        browser.navigate().refresh();
        Graphene.goTo(HomePage.class);
        waitUntilLoaded();
        Graphene.goTo(clazz);
        waitUntilLoaded().maximizeWindow();
        return this;
    }
}
