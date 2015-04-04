package org.jboss.hal.testsuite.fragment.homepage;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jcechace
 */
public class HomepageSideBarFragment extends BaseFragment {

    public List<WebElement> getAllSectionElements() {
        By selector = By.className(PropUtils.get("homepage.sidebar.section.class"));
        List<WebElement> elements = root.findElements(selector);

        return elements;
    }

    public List<HomepageSideSectionFragment> getAllSections() {
        List<WebElement> elements = getAllSectionElements();
        List<HomepageSideSectionFragment> sections = new ArrayList<HomepageSideSectionFragment>();

        for (WebElement elem : elements) {
            sections.add(Graphene.createPageFragment(HomepageSideSectionFragment.class, elem));
        }

        return sections;
    }

    public HomepageSideSectionFragment getSection(String headerLabel) {
        for (HomepageSideSectionFragment section : getAllSections()) {
            if (section.getHeaderLabel().equalsIgnoreCase(headerLabel)) {
                return section;
            }
        }
        throw new NoSuchElementException("Unable to found sidebar section labeled: " + headerLabel);
    }

}
