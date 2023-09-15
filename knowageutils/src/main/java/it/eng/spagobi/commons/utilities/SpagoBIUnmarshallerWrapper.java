package it.eng.spagobi.commons.utilities;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 * This class is used to wrap unmarshalling operation ( conversion from xml file to appropriate java object)
 *
 * @author dpirkovic
 *
 * @param <T>
 */

public class SpagoBIUnmarshallerWrapper<T> {

	private static final Logger LOGGER = Logger.getLogger(SpagoBIUnmarshallerWrapper.class);

	/**
	 *
	 * @param xmlFile
	 * @param clazz
	 * @return object type T
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	public T unmarshall(File xmlFile, Class<T> clazz) throws JAXBException {

		JAXBContext jc = JAXBContext.newInstance(clazz);
		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(xmlFile);
			InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(reader));

			return (T) jc.createUnmarshaller().unmarshal(xsr);
		} catch (FileNotFoundException | XMLStreamException e) {
			LOGGER.error("Error loading XML document: " + e.getMessage(), e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				LOGGER.error("Error loading XML document: " + e.getMessage(), e);
			}
		}

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return (T) unmarshaller.unmarshal(xmlFile);

	}
}
