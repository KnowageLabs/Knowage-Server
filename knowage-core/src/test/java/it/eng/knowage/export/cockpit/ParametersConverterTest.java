/**
 *
 */
package it.eng.knowage.export.cockpit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import it.eng.knowage.document.cockpit.template.CockpitTemplateReader;
import it.eng.knowage.document.cockpit.template.FileCockpitTemplateRetriver;
import it.eng.knowage.document.cockpit.template.ICockpitTemplateReader;
import it.eng.knowage.document.cockpit.template.widget.ICockpitWidget;
import it.eng.knowage.document.cockpit.template.widget.WidgetReaderFactory;
import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.knowage.document.export.cockpit.converter.ConverterFactory;
import it.eng.knowage.document.export.cockpit.converter.ParametersConverter;

/**
 * @author Dragan Pirkovic
 *
 */
public class ParametersConverterTest {

	private final JSONObject dataSetParameters;
	private final IConverter<JSONObject, JSONObject> parametersConverter;

	/**
	 * @throws JSONException
	 *
	 */
	public ParametersConverterTest() throws JSONException {
		ICockpitTemplateReader cockpitTemplate = new CockpitTemplateReader(new FileCockpitTemplateRetriver().getTemplate());
		ICockpitWidget widget = WidgetReaderFactory.getWidget(cockpitTemplate.getWidgets().getJSONObject(0));
		this.dataSetParameters = cockpitTemplate.getParamsByDataSetId(widget.getDsId());

		Map<String, String> documentParams = new HashMap<String, String>();

		documentParams.put("country", "USA");
		documentParams.put("city", "Belgrade");
		documentParams.put("sadsd", "Belgradde");

		this.parametersConverter = ConverterFactory.getParametersConverter(documentParams);

	}

	/**
	 * Test method for {@link it.eng.knowage.document.export.cockpit.converter.ParametersConverter#convert(org.json.JSONObject)}.
	 *
	 * @throws JSONException
	 */
	@Test
	public void testConvert() throws JSONException {
		Map<String, String> documentParams = new HashMap<String, String>();

		documentParams.put("country", "USA");
		documentParams.put("city", "Belgrade");
		JSONObject parameters = parametersConverter.convert(dataSetParameters);
		assertNotNull(parameters);
		assertEquals(parameters.getString("city"), documentParams.get("city"));
		assertEquals(parameters.getString("country"), documentParams.get("country"));
	}

}
