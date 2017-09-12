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
package it.eng.spagobi.metamodel;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class SiblingsFileLoader {
	
	public static transient Logger logger = Logger.getLogger(SiblingsFileLoader.class);


	/**
	 * Loads the Siblings File from the file system
	 * and return a Document object for reading the xml properties
	 * @param SiblingsFile the *.siblings file
	 * @return Document xml informations
	 */
	public static Document load(File siblingsFile){
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(siblingsFile);
			//optional, but recommended
			doc.getDocumentElement().normalize();

		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException for "+siblingsFile.getName());
		} catch (SAXException e) {
			logger.error("SAXException for "+siblingsFile.getName());
		} catch (IOException e) {
			logger.error("IOException for "+siblingsFile.getName());
		}
		return doc;

	}
}
