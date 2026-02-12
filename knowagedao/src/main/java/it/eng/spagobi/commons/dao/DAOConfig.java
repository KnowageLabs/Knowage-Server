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
package it.eng.spagobi.commons.dao;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.common.EnginConf;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DAOConfig {

	private static Map<String, String> mappings;
	private static String hibernateConfigurationFile;
	/**
	 * For testing purpose
	 */
	private static File hibernateConfigurationFileFile;

	private static Logger logger = Logger.getLogger(DAOConfig.class);

	public static String getResourcePath() {
		String resourcePath;
		try {
			resourcePath = SpagoBIUtilities.getResourcePath();
		} catch (Throwable t) {
			logger.debug(t);
			resourcePath = EnginConf.getInstance().getResourcePath();
		}
		return resourcePath;
	}

	public static void setMappings(Map<String, String> mappings) {
		DAOConfig.mappings = mappings;
	}


	public static Map<String, String> getMappings() {
		Map<String, String> local = mappings;
		if (local == null) {
			synchronized (DAOConfig.class) {
				local = mappings;
				if (local == null) {
					local = loadMappings();
					local = java.util.Collections.unmodifiableMap(local);
					mappings = local;
				}
			}
		}
		return local;
	}

	private static Map<String, String> loadMappings() {
		Map<String, String> map = new HashMap<>();
		try (InputStream is = DAOConfig.class.getResourceAsStream("/conf/dao_config.xml")) {
			if (is == null) {
				throw new IllegalStateException("dao_config.xml not found in the classpath");
			}

			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			f.setNamespaceAware(false);
			f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			DocumentBuilder b = f.newDocumentBuilder();
			Document doc = b.parse(is);

			XPath x = javax.xml.xpath.XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) x.evaluate("/DAO-CONF/DAO", doc, javax.xml.xpath.XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++) {
				Element el = (Element) nodes.item(i);
				String name = el.getAttribute("name");
				String impl = el.getAttribute("implementation");
				map.put(name, impl);
			}
			return map;

		} catch (Exception ex) {
			throw new IllegalStateException("Errore nel parsing di dao_config.xml", ex);
		}
	}


	public static String getHibernateConfigurationFile() {
		if (DAOConfig.hibernateConfigurationFile == null) {
			DAOConfig.hibernateConfigurationFile = "hibernate.cfg.xml";
		}
		return DAOConfig.hibernateConfigurationFile;
	}

	public static void setHibernateConfigurationFile(String hibernateConfigurationFile) {
		DAOConfig.hibernateConfigurationFile = hibernateConfigurationFile;
	}

	public static File getHibernateConfigurationFileFile() {
		return hibernateConfigurationFileFile;
	}

	public static void setHibernateConfigurationFileFile(File hibernateConfigurationFileFile) {
		DAOConfig.hibernateConfigurationFileFile = hibernateConfigurationFileFile;
	}

	private DAOConfig() {

	}
}
