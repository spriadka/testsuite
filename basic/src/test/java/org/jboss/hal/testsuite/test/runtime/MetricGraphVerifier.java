package org.jboss.hal.testsuite.test.runtime;

import static org.junit.Assert.assertEquals;

import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;

/**
 * Helper class for metrics graphs ratio verification
 */
public class MetricGraphVerifier {

    private MetricsAreaFragment area;
    private String divisorLabel;
    public static final int DELTA = 3;

    /**
     * @param area - the {@link MetricsAreaFragment} with graphs to be verified
     * @param divisorLabel the label of summary metric which should be used as the divisor in the ratio
     */
    public MetricGraphVerifier(final MetricsAreaFragment area, final String divisorLabel) {
        this.area = area;
        this.divisorLabel = divisorLabel;
    }

    /**
     * Asserts that graph corresponds to ratio between particular metric and the summary metric.
     * @param dividendLabel - the label of the metric to be compared to the summary metric to make the ratio
     */
    public MetricGraphVerifier verifyRatio(final String dividendLabel) {
        double expectedPercentage = area.getPercentage(dividendLabel, divisorLabel);
        double actualPercentage = area.getMetricsFragment(dividendLabel).getPercentage();
        assertEquals(expectedPercentage, actualPercentage, DELTA);
        return this;
    }

}
