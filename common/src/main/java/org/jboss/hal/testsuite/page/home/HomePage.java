package org.jboss.hal.testsuite.page.home;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.fragment.homepage.HomepageInfoFragment;
import org.jboss.hal.testsuite.fragment.homepage.HomepageSideBarFragment;
import org.jboss.hal.testsuite.fragment.homepage.HomepageTaskFragment;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jcechace
 */
@Location("#home")
public class HomePage extends BasePage {

    public static enum BoxType {

        TASK("homepage.task.class", "homepage.task.header.class"),
        INFO("homepage.info.class", "homepage.info.header.class");

        private final String headerClass;
        private final String boxClass;

        private BoxType(String boxClass, String headerClass) {
            this.boxClass = PropUtils.get(boxClass);
            this.headerClass = PropUtils.get(headerClass);
        }

        public String getHeaderClass() {
            return headerClass;
        }

        public String getBoxClass() {
            return boxClass;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase() + " box";
        }
    }

    public HomepageTaskFragment getTaskBox(String identifier) {
        return getBox(identifier, BoxType.TASK, HomepageTaskFragment.class);
    }

    public HomepageInfoFragment getInfoBox(String identifier) {
        return getBox(identifier, BoxType.INFO, HomepageInfoFragment.class);
    }

    public <T extends BaseFragment> T getBox(String identifier, BoxType box, Class<T> clazz) {
        Map<String, T> boxes = getAllBoxes(box, clazz);

        T fragment = boxes.get(identifier.toLowerCase());

        if (fragment == null){
            throw new NoSuchElementException("Unable to found " + box + " with identifier: "
                + identifier);
        }

        return fragment;
    }

    public <T extends BaseFragment> Map<String,T> getAllBoxes(BoxType box, Class<T> clazz) {
        By selector = By.className(box.getBoxClass());

        List<WebElement> elements = browser.findElements(selector);
        Map<String, T> tasks = new HashMap<String, T>();

        for (WebElement element : elements) {
            By headerSelector = By.className(box.getHeaderClass());
            WebElement header = element.findElement(headerSelector);

            String id = header.getAttribute("id");
            String[] classAttr = id.split("_"); // only prefix up to first "_"
            String key = classAttr[box == BoxType.TASK ? 0 : classAttr.length -1].toLowerCase();
            T task = Graphene.createPageFragment(clazz, element);

            tasks.put(key, task);
        }

        return tasks;
    }


    public HomepageSideBarFragment getSideBar() {
        By selector = By.className(PropUtils.get("homepage.sidebar.class"));
        WebElement sidebarRoot = browser.findElement(selector);

        HomepageSideBarFragment sidebar = Graphene.createPageFragment(HomepageSideBarFragment.class,
                sidebarRoot);

        return sidebar;
    }

}
