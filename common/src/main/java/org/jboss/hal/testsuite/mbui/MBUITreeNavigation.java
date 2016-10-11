package org.jboss.hal.testsuite.mbui;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Class representing model based user navigation
 */
public class MBUITreeNavigation {

    private static final Logger logger = LoggerFactory.getLogger(MBUITreeNavigation.class);

    private static final String TREE_CLASS_NAME = "gwt-Tree";

    private MBUITreeNavigation() {
    }

    @Drone
    private WebDriver browser;

    @FindByJQuery("." + TREE_CLASS_NAME)
    @Root
    private WebElement root;

    private List<String> steps;

    /**
     * Add step of navigation - i.e. level in tree structure
     * @param label name of label of item through which the navigation will be performed.
     * @return this
     */
    public MBUITreeNavigation step(String label) {
        if (steps == null) {
            steps = new LinkedList<>();
        }
        steps.add(label);
        return this;
    }

    /**
     * Navigates to target item step by step
     * @return target item
     */
    public MBUITreeGroup navigateToTreeItem() {
        MBUITreeGroup current = new MBUITreeGroup(root.findElement(ByJQuery.selector("> div:nth-child(2)")));
        logger.info(current.getRoot().getAttribute("innerHTML"));
        for (String step : steps) {
            current = current.openSubTreeIfNotOpen().getDirectChildByLabel(step);
        }
        return current;
    }

}
