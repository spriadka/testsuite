package org.jboss.hal.testsuite.fragment;


public class AlertFragment extends BaseFragment {
    public static final String ROOT_SELECTOR = "div[role='alert'] div.notification-display";

    public String getMessage() {
        return root.getText().trim();
    }
}
