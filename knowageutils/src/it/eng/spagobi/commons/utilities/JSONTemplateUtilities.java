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
package it.eng.spagobi.commons.utilities;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JSONTemplateUtilities {

	public static String convertJsonToXML(JSONObject json) throws ParserConfigurationException, JSONException, IOException {
		String xml;
		DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
		Document dom = docBuilder.newDocument();

		Element xmlDom = extractXmlFromJson(json, dom, null);
		StringWriter outputStream = new StringWriter();
		OutputFormat outputFormat = new OutputFormat();
		outputFormat.setEncoding("UTF-8");
		outputFormat.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(outputStream, outputFormat);
		serializer.serialize(xmlDom);

		xml = outputStream.toString();
		return xml;
	}

	public static Element extractXmlFromJson(JSONObject json, Document dom, Element parent) throws JSONException {

		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {

			String key = keys.next();
			Object value = json.get(key);

			if (value instanceof JSONObject) {
				Element currElement = dom.createElement(key);
				JSONObject jsoChild = (JSONObject) value;

				extractXmlFromJson(jsoChild, dom, currElement);

				if (parent == null) {
					parent = currElement;
				} else {
					parent.appendChild(currElement);
				}
			}

			else if (value instanceof JSONArray) {
				JSONArray jsoArray = (JSONArray) value;
				for (int i = 0; i < jsoArray.length(); i++) {
					JSONObject jsoChild = (JSONObject) jsoArray.get(i);
					Element currElement = dom.createElement(key);
					extractXmlFromJson(jsoChild, dom, currElement);
					parent.appendChild(currElement);
				}
			}

			else {
				parent.setAttribute(key, value.toString());
			}
		}

		return parent;
	}
}
