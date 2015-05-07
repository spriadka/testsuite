package org.jboss.hal.testsuite.fragment.config.resourceadapters;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class ResourceAdaptersFragment extends ConfigFragment{

    private static final By CONTENT = ByJQuery.selector("." + PropUtils.get("page.content.rhs.class") + ":visible");
    private static final By ADMIN_OBJECTS_ANCHOR = ByJQuery.selector("a:contains('Admin Objects')");
    private static final By CONNECTION_DEFINITIONS_ANCHOR = ByJQuery.selector("a:contains('Connection Definitions')");
    private static final By BACK_ANCHOR = ByJQuery.selector("a:contains('Back')");

    public ResourceAdapterWizard addResourceAdapter(){
        return getResourceManager().addResource(ResourceAdapterWizard.class);
    }

    public ConnectionDefinitionsFragment viewConnectionDefinitions(String name){
        viewTab(name, CONNECTION_DEFINITIONS_ANCHOR);
        WebElement contentRoot = root.findElement(CONTENT);
        return Graphene.createPageFragment(ConnectionDefinitionsFragment.class, contentRoot);
    }

    public AdminObjectsFragment viewAdminObjects(String name){
        viewTab(name, ADMIN_OBJECTS_ANCHOR);
        WebElement contentRoot = root.findElement(CONTENT);
        return Graphene.createPageFragment(AdminObjectsFragment.class, contentRoot);
    }

    private void viewTab(String resourceAdapterName, By tabAnchor){
        backIfAvailable();
        getResourceManager().viewByName(resourceAdapterName);
        WebElement anchor = root.findElement(tabAnchor);
        anchor.click();
    }

    private void backIfAvailable(){
        WebElement back = root.findElement(BACK_ANCHOR);
        if(back.isDisplayed()){
            back.click();
        }
    }

    public void removeResourceAdapter(String resourceAdapterName) {
        backIfAvailable();
        getResourceManager().removeResourceAndConfirm(resourceAdapterName);
    }

    public void selectResourceAdapter(String nameNoTransaction) {
        getResourceManager().selectByName(nameNoTransaction);
    }
}
