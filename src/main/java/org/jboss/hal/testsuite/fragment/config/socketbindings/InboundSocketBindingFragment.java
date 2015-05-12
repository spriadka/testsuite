package org.jboss.hal.testsuite.fragment.config.socketbindings;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class InboundSocketBindingFragment extends ConfigFragment {

    private static final By MULTICAST_ANCHOR = ByJQuery.selector("a.header:has(td:contains('Multicast'):visible)");

    public void editPortAndSave(String socketBindingName, String value){
        getResourceManager().selectByName(socketBindingName);
        root.sendKeys(Keys.PAGE_DOWN);
        Editor editor = edit();
        editor.text("port", value);
        WebElement saveButton = getButton(PropUtils.get("configarea.save.button.label"));
        Graphene.waitGui().until().element(saveButton).is().visible();
        save();
    }

    public void editMulticastAndSave(String socketBindingName, String multicastPort, String multicastAddress){
        getResourceManager().selectByName(socketBindingName);
        Editor editor = edit();
        root.sendKeys(Keys.PAGE_DOWN);
        WebElement multicast = editor.getRoot().findElement(MULTICAST_ANCHOR);
        Graphene.waitGui().until().element(multicast).is().visible();
        multicast.click();

        root.sendKeys(Keys.PAGE_DOWN);
        editor.text("multiCastPort", multicastPort);
        editor.text("multiCastAddress", multicastAddress);
        WebElement saveButton = getButton(PropUtils.get("configarea.save.button.label"));
        Graphene.waitGui().until().element(saveButton).is().visible();
        save();
    }

    public InboundSocketBindingWizard addSocketBinding(){
        return getResourceManager().addResource(InboundSocketBindingWizard.class);
    }
}
