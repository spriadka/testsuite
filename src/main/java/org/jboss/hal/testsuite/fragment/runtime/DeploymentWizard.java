package org.jboss.hal.testsuite.fragment.runtime;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class DeploymentWizard extends WizardWindow {

    private static final Logger log = LoggerFactory.getLogger(DeploymentWizard.class);

    private static final String NAME = "name";
    private static final String ENABLED = "enabled";
    private static final String RUNTIME_NAME = "runtimeName";
    private static final String IS_ARCHIVE = "archive";
    private static final String PATH = "path";
    private static final String UPLOAD_FORM_ELEMENT = "uploadFormElement";
    private static final String MANAGED = "Managed";
    private static final String UNMANAGED = "Unmanaged";


    public DeploymentWizard switchToManaged() {
        root.findElement(By.xpath(".//div[text()='"+MANAGED+"']")).click();
        return this;
    }

    public DeploymentWizard switchToUnmanaged() {
        root.findElement(By.xpath(".//div[text()='"+ UNMANAGED +"']")).click();
        return this;
    }

    public DeploymentWizard uploadDeployment(File file) {
        getEditor().uploadFile(file, UPLOAD_FORM_ELEMENT);
        return this;
    }

    public DeploymentWizard name(String name) {
        getEditor().text(NAME, name);
        return this;
    }

    public DeploymentWizard runtimeName(String runtimeName) {
        getEditor().text(RUNTIME_NAME, runtimeName);
        return this;
    }

    public DeploymentWizard enable(boolean enable) {
        getEditor().checkbox(ENABLED, enable);
        return this;
    }

    public DeploymentWizard path(String path) {
        getEditor().text(PATH, path);
        return this;
    }

    public DeploymentWizard isArchive(boolean val) {
        getEditor().checkbox(IS_ARCHIVE, val);
        return this;
    }

    public DeploymentWizard nextFluent() {
        next();
        return this;
    }
}
