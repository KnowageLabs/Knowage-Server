/**
 *
 */
package it.eng.knowage.export.cockpit;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.junit.Test;

import it.eng.knowage.document.cockpit.CockpitDocument;
import it.eng.knowage.document.cockpit.template.widget.WidgetReaderFactory;
import it.eng.knowage.document.export.cockpit.converter.CockpitWidgetJsonConfConverter;
import it.eng.knowage.document.export.cockpit.converter.IJsonConfiguration;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitWidgetJsonConfConverterTest {

	private final CockpitWidgetJsonConfConverter converter;
	private final CockpitDocument cockpitDocument;

	/**
	 *
	 */
	public CockpitWidgetJsonConfConverterTest() {
		Map<String, String> documentParams = new HashMap<String, String>();

		documentParams.put("country", "USA");
		documentParams.put("city", "Belgrade");
		documentParams.put("sadsd", "Belgradde");
		this.cockpitDocument = new CockpitDocument(documentParams, 123, "document123");
		this.converter = new CockpitWidgetJsonConfConverter(cockpitDocument);
	}

	/**
	 * Test method for
	 * {@link it.eng.knowage.document.export.cockpit.converter.CockpitWidgetJsonConfConverter#convert(it.eng.knowage.document.cockpit.template.widget.ICockpitWidget)}.
	 *
	 * @throws JSONException
	 */
	@Test
	public void testConvert() throws JSONException {
		IJsonConfiguration conf = converter.convert(WidgetReaderFactory.getWidget(cockpitDocument.getWidgets().getJSONObject(0)));
		assertNotNull(conf);
	}

}
