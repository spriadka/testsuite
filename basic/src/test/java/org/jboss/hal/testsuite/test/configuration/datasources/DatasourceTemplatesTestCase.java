package org.jboss.hal.testsuite.test.configuration.datasources;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.fragment.config.datasource.DatasourceWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.DatasourcesPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains smoke tests which verifies that wizard in datasources subsystem contains correctly predefined values for
 * each template.
 */
@Category(Shared.class)
@RunWith(Arquillian.class)
public class DatasourceTemplatesTestCase {

    private static final String
            MODULE_NAME = "driverModuleName",
            NAME = "name",
            DRIVER_CLASS = "driverClass",
            ERROR_MESSAGE_SUFFIX = " are not equal. Either the test was not updated or there are bad data in GUI.",
            BAD_NAME_ERROR_MESSAGE = "Names" + ERROR_MESSAGE_SUFFIX,
            BAD_DRIVER_CLASS_ERROR_MESSAGE = "Driver classes" + ERROR_MESSAGE_SUFFIX,
            BAD_MODULE_NAME_MESSAGE = "Module names" + ERROR_MESSAGE_SUFFIX;

    @Drone
    protected WebDriver browser;

    @Page
    protected DatasourcesPage page;

    private DatasourceWizard datasourceWizard;
    private Editor datasourceWizardEditor;

    private enum DatasourceTemplate {
        H2("H2 Datasource", "h2", "com.h2database.h2", "org.h2.Driver"),
        POSTGRESQL("PostgreSQL Datasource", "postgresql", "org.postgresql", "org.postgresql.Driver"),
        MYSQL("MySQL Datasource", "mysql", "com.mysql", "com.mysql.jdbc.Driver"),
        MARIADB("MariaDB Datasource", "mariadb", "org.mariadb", "org.mariadb.jdbc.Driver"),
        ORACLE("Oracle Datasource", "oracle", "com.oracle", "oracle.jdbc.driver.OracleDriver"),
        MICROSOFT("Microsoft SQLServer Datasource", "sqlserver", "com.microsoft", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
        IBM_DB2("IBM DB2 Datasource", "ibmdb2", "com.ibm", "COM.ibm.db2.jdbc.app.DB2Driver"),
        SYBASE("Sybase Datasource", "sybase", "com.sybase", "com.sybase.jdbc.SybDriver");

        private String datasourceTemplateName;
        private String name;
        private String moduleName;
        private String driverClass;

        DatasourceTemplate(String datasourceTemplateName, String name, String moduleName, String driverClass) {
            this.datasourceTemplateName = datasourceTemplateName;
            this.name = name;
            this.moduleName = moduleName;
            this.driverClass = driverClass;
        }

        public String getDatasourceTemplateName() {
            return datasourceTemplateName;
        }

        public String getName() {
            return name;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getDriverClass() {
            return driverClass;
        }
    }

    /**
     * Verifies that datasource wizard contains correctly predefined values for given template
     * @param template template to verify
     */
    private void verifyDatasourceDriverFormForTemplate(DatasourceTemplate template) {
        datasourceWizard.selectTemplate(template.getDatasourceTemplateName());
        datasourceWizard.next();
        datasourceWizard.next();

        List<String> messages = new LinkedList<>();
        boolean passed = true;
        if (!datasourceWizardEditor.text(NAME).equals(template.getName())) {
            passed = false;
            messages.add(BAD_NAME_ERROR_MESSAGE);
        }
        if (!datasourceWizardEditor.text(MODULE_NAME).equals(template.getModuleName())) {
            passed = false;
            messages.add(BAD_MODULE_NAME_MESSAGE);
        }
        if (!datasourceWizardEditor.text(DRIVER_CLASS).equals(template.getDriverClass())) {
            passed = false;
            messages.add(BAD_DRIVER_CLASS_ERROR_MESSAGE);
        }
        Assert.assertTrue(messages.stream().collect(Collectors.joining(", ")), passed);
    }

    @Before
    public void before() {
        page.invokeAddDatasource();
        datasourceWizard = page.getDatasourceWizard();
        datasourceWizardEditor = datasourceWizard.getEditor();
    }

    @Test
    public void testH2Template() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.H2);
    }

    @Test
    public void testPostgreSQLTemplate() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.POSTGRESQL);
    }

    @Test
    public void testMySQLTemplate() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.MYSQL);
    }

    @Test
    public void testMariaDBTemplate() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.MARIADB);
    }

    @Test
    public void testOracleTemplate() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.ORACLE);
    }

    @Test
    public void testMicrosoftTemplate() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.MICROSOFT);
    }

    @Test
    public void testIBMDB2Template() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.IBM_DB2);
    }

    @Test
    public void testSybaseTemplate() {
        verifyDatasourceDriverFormForTemplate(DatasourceTemplate.SYBASE);
    }

}
