/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.testsuite.finder;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to select a column or row in a finder of the specified page.
 *
 * @author Harald Pehl
 */
public class FinderNavigation {

    private static class AddressTuple {

        final String column;
        final String row;

        AddressTuple(final String column, final String row) {
            this.column = column;
            this.row = row;
        }
    }


    private static final String WILDCARD = "*";

    private final WebDriver browser;
    private final Class<? extends BasePage> page;
    private final List<AddressTuple> address;

    public FinderNavigation(final WebDriver browser, final Class<? extends BasePage> page) {
        this.browser = browser;
        this.page = page;
        this.address = new ArrayList<>();
    }

    /**
     * Adds an address to select a column.
     */
    public FinderNavigation addAddress(String column) {
        address.add(new AddressTuple(column, WILDCARD));
        return this;
    }

    /**
     * Adds an address to select a row inside a column.
     */
    public FinderNavigation addAddress(String column, String row) {
        address.add(new AddressTuple(column, row));
        return this;
    }

    public Column selectColumn() {
        WebElement column = navigate()[0];
        if (column == null) {
            throw new IllegalStateException("No address for selecting a column given");
        }
        return Graphene.createPageFragment(Column.class, column);
    }

    public Row selectRow() {
        WebElement row = navigate()[1];
        if (row == null) {
            throw new IllegalStateException("No address for selecting a row given.");
        }
        return Graphene.createPageFragment(Row.class, row);
    }

    private WebElement[] navigate() {
        WebElement[] columnRow = new WebElement[2];
        Console.withBrowser(browser).refreshAndNavigate(page);

        for (int i = 0; i < address.size(); i++) {
            AddressTuple tuple = address.get(i);

            columnRow[0] = browser.findElement(columnSelector(tuple.column));
            if (!WILDCARD.equals(tuple.row)) {
                By rowSelector = rowSelector(tuple.row);
                columnRow[1] = columnRow[0].findElement(rowSelector);
                columnRow[1].click();
                Graphene.waitModel().until().element(rowSelector).attribute("class")
                        .contains("cellTableSelectedRowCell");

                // wait for next column to be visible
                if (i < address.size() - 2) {
                    AddressTuple nextTuple = address.get(i + 1);
                    Graphene.waitModel().until().element(columnSelector(nextTuple.column)).is().visible();
                }
            }
        }
        return columnRow;
    }

    private By columnSelector(String name) {
        return By.cssSelector("[data-column=\"" + name + "\"]");
    }

    private By rowSelector(String label) {
        String cellClass = PropUtils.get("table.cell.class");
        return By.ByXPath.xpath("//td[contains(@class,'" + cellClass + "') and " +
                "descendant::div[@class='navigation-column-item'" + " and contains(.,'" + label + "')]]");
    }
}
