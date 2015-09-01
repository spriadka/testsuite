package org.jboss.hal.testsuite.fragment.config.web.servlet;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

import static org.jboss.arquillian.graphene.Graphene.waitGui;

/**
 * Created by mkrajcov on 3/26/15.
 */
public class ConnectorCreateWindow extends WizardWindow {

    private static final String NAME = "name";
    private static final String SOCKET_BINDING = "socketBinding";
    private static final String PROTOCOL = "protocol";
    private static final String SCHEME = "scheme";
    private static final String ENABLED = "enabled";

    public void save() {
        clickButton(PropUtils.get("modals.window.save.label"));
        if (isClosed()) {
            waitGui().until().element(By.className(PropUtils.get("resourcetable.cell.class"))).is().visible();
        }
    }

    public ConnectorCreateWindow name(String value) {
        getEditor().text(NAME, value);
        return this;
    }

    public ConnectorCreateWindow socketBinding(String value) {
        getEditor().text(SOCKET_BINDING, value);
        return this;
    }

    public ConnectorCreateWindow protocol(String value) {
        getEditor().select(PROTOCOL, value);
        return this;
    }

    public ConnectorCreateWindow scheme(String value) {
        getEditor().select(SCHEME, value);
        return this;
    }

    public ConnectorCreateWindow enabled(boolean value) {
        getEditor().checkbox(ENABLED, value);
        return this;
    }

}
