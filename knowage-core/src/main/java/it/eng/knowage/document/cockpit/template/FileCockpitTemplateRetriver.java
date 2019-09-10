/**
 *
 */
package it.eng.knowage.document.cockpit.template;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import it.eng.knowage.export.cockpit.CockpitTemplateReaderTest;

/**
 * @author Dragan Pirkovic
 *
 */
public class FileCockpitTemplateRetriver implements ICockpitTemplateRetriver {

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.ICockpitTemplateRetriver#getTemplate()
	 */
	@Override

	public JSONObject getTemplate() {
		JSONObject jsonObject = null;
		InputStream is = CockpitTemplateReaderTest.class.getResourceAsStream("template.sbicockpit");
		String text = null;
		try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
			text = scanner.useDelimiter("\\A").next();
		}

		try {
			jsonObject = new JSONObject(text);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject;
	}

	@Test
	public void getTemplateTest() {
		assertNotNull(this.getTemplate());

	}

}
