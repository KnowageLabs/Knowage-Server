/**
 *
 */
package it.eng.knowage.export.cockpit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Dragan Pirkovic
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ ChartWidgetReaderTest.class, CockpitTemplateReaderTest.class, TableWidgetReaderTest.class, SelectionsConverterTest.class,
		AggregationConverterTest.class, ParametersConverterTest.class })
public class AllTests {

}
