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
import it.eng.knowage.document.cockpit.template.widget.ChartWidgetReader;
import it.eng.knowage.document.export.cockpit.converter.SelectionsConverter;

/**
 * @author Dragan Pirkovic
 *
 */
public class SelectionsConverterTest {

	private final SelectionsConverter selectionsConverter;
	private final ChartWidgetReader widget;

	/**
	 * @throws JSONException
	 *
	 */
	public SelectionsConverterTest() throws JSONException {

		ICockpitTemplateReader cockpitTemplate = new CockpitTemplateReader(new FileCockpitTemplateRetriver().getTemplate());
		this.widget = new ChartWidgetReader(cockpitTemplate.getWidgets().getJSONObject(0));
		this.selectionsConverter = new SelectionsConverter("cockpit_export_csv_1", cockpitTemplate.getFilters());
		;
	}

	/**
	 * Test method for {@link it.eng.knowage.document.export.cockpit.converter.SelectionsConverter#convert(org.json.JSONArray)}.
	 *
	 * @throws JSONException
	 */
	@Test
	public void testConvert() throws JSONException {

		JSONObject selections = selectionsConverter.convert(widget.getFilters());
		assertNotNull(selections);
		assertEquals("=", selections.getJSONObject("cockpit_export_csv_1").getJSONObject("gender").getString("filterOperator"));
		assertEquals("('M')", selections.getJSONObject("cockpit_export_csv_1").getJSONObject("gender").getJSONArray("filterVals").getString(0));
	}

}
