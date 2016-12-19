package org.jboss.hal.testsuite.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Collects all assertion errors which occurred during test execution
 * to report them back at the end of test lifecycle. This approach
 * allows to run through entire test execution and executed all
 * required phases (like cleaning up database at the end of each
 * persistence test) - so called soft assertion.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
public class AssertionErrorCollector {
    private final List<Throwable> assertionErrors = new ArrayList<>();

    public void collect(Throwable error) {
        assertionErrors.add(error);
    }

    public void report() {
        if (assertionErrors.isEmpty()) {
            return;
        }

        throw new AssertionError(createErrorMessage());
    }

    public String showAllErrors() {
        return Arrays.toString(assertionErrors.toArray());
    }

    public boolean contains(Class<? extends Throwable> throwable) {
        for (Throwable error : assertionErrors) {
            if (error.getClass().equals(throwable)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        assertionErrors.clear();
    }

    public int amountOfErrors() {
        return assertionErrors.size();
    }

    private String createErrorMessage() {
        final StringBuilder builder = new StringBuilder();

        builder.append("Test failed in ").append(amountOfErrors()).append(" case").append(amountOfErrors() > 1 ? "s" : "").append(". \n");
        for (Throwable error : assertionErrors) {
            builder.append(error.getMessage()).append('\n');
        }
        return builder.toString();
    }

}