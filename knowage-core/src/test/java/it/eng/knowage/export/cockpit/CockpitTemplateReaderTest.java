/**
 *
 */
package it.eng.knowage.export.cockpit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import it.eng.knowage.document.cockpit.template.CockpitTemplateReader;
import it.eng.knowage.document.cockpit.template.FileCockpitTemplateRetriver;
import it.eng.knowage.document.cockpit.template.ICockpitTemplateReader;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitTemplateReaderTest {

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.CockpitTemplateReader#getWidgets()}.
	 *
	 * @throws JSONException
	 */
	/**
	 *
	 */
	ICockpitTemplateReader cockpitTemplateReader = null;

	public CockpitTemplateReaderTest() {
		cockpitTemplateReader = new CockpitTemplateReader(new FileCockpitTemplateRetriver().getTemplate());
	}

	@Test
	public void testGetWidgets() throws JSONException {
		JSONArray widgets = cockpitTemplateReader.getWidgets();
		assertNotNull(widgets);
		assertEquals(widgets.length(), 7);
		assertEquals(widgets.getJSONObject(0).get("type"), "chart");
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.CockpitTemplateReader#getFilters()}.
	 *
	 * @throws JSONException
	 */
	@Test
	public void testGetFilters() throws JSONException {
		JSONObject filters = cockpitTemplateReader.getFilters();
		assertNotNull(filters);
		assertEquals(filters.length(), 1);
		assertEquals(filters.getJSONObject("cockpit_export_csv_2").getString("city"), "Arcadia");

	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.CockpitTemplateReader#getParamsByDataSetId(java.lang.Integer)}.
	 *
	 * @throws JSONException
	 */
	@Test
	public void testGetParamsByDataSetId() throws JSONException {
		JSONObject dsParams = cockpitTemplateReader.getParamsByDataSetId(322);
		assertNotNull(dsParams);
		assertEquals(dsParams.length(), 2);
		assertEquals(dsParams.getString("country"), "$P{country}");
		assertEquals(dsParams.getString("city"), "$P{city}");
	}

	@Test
	public void testGetParamsByDataSetId2() throws JSONException {
		JSONObject dsParams = cockpitTemplateReader.getParamsByDataSetId(323);
		assertNotNull(dsParams);
		assertEquals(dsParams.length(), 1);
		assertEquals(dsParams.getString("gender"), "$P{gender}");

	}

}
