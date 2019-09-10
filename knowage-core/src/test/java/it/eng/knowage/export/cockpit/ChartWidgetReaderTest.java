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
import it.eng.knowage.document.cockpit.template.widget.ChartWidgetReader;

/**
 * @author Dragan Pirkovic
 *
 */
public class ChartWidgetReaderTest {

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.ChartWidgetReader#getName()}.
	 */
	ChartWidgetReader chartWiget;

	/**
	 *
	 */
	public ChartWidgetReaderTest() {
		try {
			JSONObject jsonWidget = new CockpitTemplateReader(new FileCockpitTemplateRetriver().getTemplate()).getWidgets().getJSONObject(0);
			chartWiget = new ChartWidgetReader(jsonWidget);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		;
	}

	@Test
	public void testGetName() {
		String name = chartWiget.getName();
		assertNotNull(name);
		assertEquals(name, "widget_chart_1567696154084");
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.ChartWidgetReader#getDsId()}.
	 */
	@Test
	public void testGetDsId() {

		assertEquals(chartWiget.getDsId(), Integer.valueOf(322));
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.ChartWidgetReader#getColumnSelectedOfDataSet()}.
	 *
	 * @throws JSONException
	 */
	@Test
	public void testGetColumnSelectedOfDataSet() throws JSONException {
		JSONArray columnSelected = chartWiget.getColumnSelectedOfDataSet();
		assertNotNull(columnSelected);
		assertEquals(columnSelected.getJSONObject(0).getString("name"), "total_children");
		assertEquals(columnSelected.getJSONObject(2).getString("aliasToShow"), "num_cars_owned_SUM");
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.ChartWidgetReader#getFilters()}.
	 */
	@Test
	public void testGetFilters() {
		JSONArray filters = chartWiget.getFilters();
		assertNotNull(filters);
	}

}
