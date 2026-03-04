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
package it.eng.spagobi.tools.scheduler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FormulaSingleton {

	private static final Logger logger = Logger.getLogger(FormulaSingleton.class);
	private static final String FORMULAS_XML_PATH = "conf/formulas.xml";

	private final List<Formula> formulas;

	private FormulaSingleton() {
		this.formulas = loadFormulas();
	}

	public static FormulaSingleton getInstance() {
		return Holder.INSTANCE;
	}

	public List<Formula> getFormulas() {
		return formulas;
	}

	private List<Formula> loadFormulas() {
		List<Formula> loaded = new ArrayList<>();
		try (InputStream is = FormulaSingleton.class.getClassLoader().getResourceAsStream(FORMULAS_XML_PATH)) {
			if (is == null) {
				logger.debug("No formulas configured: resource not found at " + FORMULAS_XML_PATH);
				return Collections.unmodifiableList(loaded);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);
			NodeList formulas = document.getElementsByTagName("FORMULA");
			if (formulas == null || formulas.getLength() == 0) {
				logger.debug("No formulas configured.");
				return Collections.unmodifiableList(loaded);
			}

			for (int i = 0; i < formulas.getLength(); i++) {
				Node node = formulas.item(i);
				if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String name = element.getAttribute("name");
					String description = element.getAttribute("description");
					String groovyCode = element.getTextContent();
					try {
						Formula f = new Formula(name, description, groovyCode);
						loaded.add(f);
					} catch (Exception e) {
						logger.error("Invalid formula configuration for name [" + name + "]", e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while loading formulas from " + FORMULAS_XML_PATH, e);
		}
		return Collections.unmodifiableList(loaded);
	}

	private static class Holder {
		private static final FormulaSingleton INSTANCE = new FormulaSingleton();
	}
}
