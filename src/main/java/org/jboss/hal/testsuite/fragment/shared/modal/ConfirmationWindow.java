package org.jboss.hal.testsuite.fragment.shared.modal;

import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.util.PropUtils;

/**
 * Created by jcechace on 21/02/14.
 */
public class ConfirmationWindow extends WindowFragment {
    public void confirm() {
        String label = PropUtils.get("modals.confirmation.confirm.label");
        clickButton(label);

        waitUntilClosed();
        closed = true;
    }
}
