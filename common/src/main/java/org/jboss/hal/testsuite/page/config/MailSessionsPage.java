package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.mail.MailServerFragment;
import org.jboss.hal.testsuite.fragment.config.mail.MailSessionsFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#profile")
public class MailSessionsPage extends ConfigurationPage {
    private static final Logger log = LoggerFactory.getLogger(MailSessionsPage.class);

    private static final By BACK_ANCHOR = ByJQuery.selector("a:contains('Back')");
    private static final By CONTENT = ByJQuery.selector("." + PropUtils.get("page.content.gwt-layoutpanel") + ":visible");
    private static final By SESSIONCONTENT = ByJQuery.selector("." + PropUtils.get("page.content.rhs.class") + ":visible");

    public MailSessionsFragment getMailSessions(){
        backIfAvailable();
        WebElement root = getContentRoot().findElement(CONTENT);
        return Graphene.createPageFragment(MailSessionsFragment.class, root);
    }

    public MailServerFragment getMailServers(String jndiName) {
        backIfAvailable();
        getResourceManager().viewByName(jndiName);
        WebElement fragmentRoot = getContentRoot().findElement(CONTENT);
        return Graphene.createPageFragment(MailServerFragment.class, fragmentRoot);
    }

    public MailServerFragment getSesionsServers() {
        WebElement fragmentRoot = getContentRoot().findElement(SESSIONCONTENT);
        return Graphene.createPageFragment(MailServerFragment.class, fragmentRoot);
    }

    private void backIfAvailable(){
        try{
        WebElement back = getContentRoot().findElement(BACK_ANCHOR);
        if(back.isDisplayed()){
            back.click();
        }
        }catch(NoSuchElementException e){
            log.debug("No back anchor found");
        }
    }
}
