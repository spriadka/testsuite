package org.jboss.hal.testsuite.page;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public abstract class MetricsPage extends BasePage{

    public MetricsAreaFragment getMetricsArea(String title){
        //////add property
        By selector = By.xpath(".//table[contains(@class, '" + PropUtils.get("metrics.container.class") + "')][.//h3[text()='" + title + "']]");
        WebElement element = null;
        try {
            element = getContentRoot().findElement(selector);
        }catch(NoSuchElementException exc){
            return null;
        }
        MetricsAreaFragment area = Graphene.createPageFragment(MetricsAreaFragment.class, element);

        Map<String, String> metricGrid = element.findElement(By.className(PropUtils.get("metrics.grid.class"))).
                findElements(By.tagName("tr")).stream().
                collect(Collectors.toMap(
                        e -> e.findElement(By.className(PropUtils.get("metrics.grid.nominal.class"))).getText(),
                        e -> e.findElement(By.className(PropUtils.get("metrics.grid.numerical.class"))).getText()));

        area.setMetricGrid(metricGrid);

        return area;
    }
}
