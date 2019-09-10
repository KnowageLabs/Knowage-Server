/**
 *
 */
package it.eng.knowage.export.cockpit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import it.eng.knowage.document.cockpit.template.CockpitTemplateReader;
import it.eng.knowage.document.cockpit.template.FileCockpitTemplateRetriver;
import it.eng.knowage.document.cockpit.template.ICockpitTemplateReader;
import it.eng.knowage.document.export.cockpit.converter.AggregationConverter;

/**
 * @author Dragan Pirkovic
 *
 */
public class AggregationConverterTest {

	private final AggregationConverter aggregationConverter;
	private final JSONObject widget;

	/**
	 * @throws JSONException
	 *
	 */
	public AggregationConverterTest() throws JSONException {

		this.aggregationConverter = new AggregationConverter("cockpit_export_csv_1");
		ICockpitTemplateReader cockpitTemplate = new CockpitTemplateReader(new FileCockpitTemplateRetriver().getTemplate());
		this.widget = cockpitTemplate.getWidgets().getJSONObject(0);
	}

	/**
	 * Test method for {@link it.eng.knowage.document.export.cockpit.converter.AggregationConverter#convert(org.json.JSONObject)}.
	 *
	 * @throws JSONException
	 */
	@Test
	public void testConvert() throws JSONException {
		JSONObject aggregation = aggregationConverter.convert(widget);

		assertNotNull(aggregation);
		assertEquals(aggregation.getJSONArray("measures").length(), 3);
		assertEquals(aggregation.getJSONArray("categories").length(), 1);
		assertEquals(aggregation.getString("dataset"), "cockpit_export_csv_1");
	}

}
