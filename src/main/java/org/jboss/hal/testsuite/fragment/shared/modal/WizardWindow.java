package org.jboss.hal.testsuite.fragment.shared.modal;

import org.junit.Assert;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author jcechace
 */
public class WizardWindow extends WindowFragment {

    private static final Logger log = LoggerFactory.getLogger(WizardWindow.class);

    public void next() {
        String label = PropUtils.get("modals.wizard.next.label");
        clickButton(label);

        // TODO: possible refactoring to dynamic wait
        Graphene.waitModel().withTimeout(2, TimeUnit.SECONDS);
    }

    /**
     * Clicks either Finish or Save button and wait's unit the wizard window is closed
     *
     * @return true if window is not present after the button was clicked
     */
    public boolean finish() {
        String label = PropUtils.get("modals.wizard.finish.label");
        try {
            clickButton(label);
        } catch (WebDriverException e) {
            log.debug("Button with label \"" + label + "\" not found");
            label = PropUtils.get("modals.window.save.label");
            clickButton(label);
        }

        try {
            Graphene.waitModel().until().element(root).is().not().present();
            closed = true;
            log.debug("Wizard window finished (should be closed)");
            return true;
        } catch (TimeoutException e) {
            log.debug("Wizard window remains open");
            return false;
        }

    }

    /**
     * Calls  {@link #finish() finish} method and asserts the output
     *
     * @param expected <code>true</code>if wizard is expected to finish, <code>false</code> otherwise
     */
    public void assertFinish(boolean expected) {
        boolean finished = finish();

        if(expected) {
            Assert.assertTrue("Wizard was supposed to finish, the window should be closed.", finished);
        } else {
            Assert.assertFalse("Wizard was supposed to fail, the window should be open.", finished);
        }
    }

    /**
     *  Waits until operation is finished (spinner circle is hidden)
     */
    public void waitUntilFinished() {
        By selector = By.className(PropUtils.get("modals.window.spinner.class"));
        Graphene.waitGui().withTimeout(1200, TimeUnit.MILLISECONDS);
        Graphene.waitModel().until().element(root, selector).is().not().visible();
    }
}
