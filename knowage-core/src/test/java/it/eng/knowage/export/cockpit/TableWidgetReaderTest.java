/**
 *
 */
package it.eng.knowage.export.cockpit;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import it.eng.knowage.document.cockpit.template.CockpitTemplateReader;
import it.eng.knowage.document.cockpit.template.FileCockpitTemplateRetriver;
import it.eng.knowage.document.cockpit.template.widget.AbstactWidgetReader;
import it.eng.knowage.document.cockpit.template.widget.TableWidgetReader;

/**
 * @author Dragan Pirkovic
 *
 */
public class TableWidgetReaderTest {

	AbstactWidgetReader tablewidget;

	/**
	 * @throws JSONException
	 *
	 */
	public TableWidgetReaderTest() throws JSONException {
		JSONObject jsonWidget = new CockpitTemplateReader(new FileCockpitTemplateRetriver().getTemplate()).getWidgets().getJSONObject(6);
		tablewidget = new TableWidgetReader(jsonWidget);
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.TableWidgetReader#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals(tablewidget.getName(), "widget_table_1567766821558");
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.TableWidgetReader#getDsId()}.
	 */
	@Test
	public void testGetDsId() {

		assertEquals(tablewidget.getDsId(), Integer.valueOf(322));
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.TableWidgetReader#getColumnSelectedOfDataSet()}.
	 */
	@Test
	public void testGetColumnSelectedOfDataSet() {
		assertEquals(tablewidget.getColumnSelectedOfDataSet().optJSONObject(1).optString("type"), "java.lang.String");
	}

	/**
	 * Test method for {@link it.eng.knowage.document.cockpit.template.widget.TableWidgetReader#getFilters()}.
	 */
	@Test
	public void testGetFilters() {
		assertEquals(tablewidget.getFilters().length(), 0);
	}

}
