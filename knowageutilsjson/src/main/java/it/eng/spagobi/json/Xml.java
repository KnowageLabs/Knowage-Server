/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONException;
import org.json.JSONObject;

public class Xml {

	public static String xml2json(String xml) throws TransformerFactoryConfigurationError, TransformerException {
		String json = "{}";

		if (xml != null && !"".equals(xml)) {
			// Fastest way to check if a big string is a JSONObject
			// don't do this at home...
			try {
				new JSONObject(xml);
				return xml;
			} catch (JSONException e) {
			}

			byte[] bytes = xml.getBytes();
			InputStream inputStream = new ByteArrayInputStream(bytes);

			TransformerFactory factory = TransformerFactory.newInstance();

			InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("it/eng/spagobi/json/xml2Json.xslt");

			StreamSource source = new StreamSource(resourceAsStream);
			Templates template = factory.newTemplates(source);
			Transformer transformer = template.newTransformer();

			OutputStream os = new ByteArrayOutputStream();

			transformer.transform(new StreamSource(inputStream), new StreamResult(os));

			json = os.toString().replaceAll("\\p{Cntrl}", "");
		}
		return json;
	}

}
