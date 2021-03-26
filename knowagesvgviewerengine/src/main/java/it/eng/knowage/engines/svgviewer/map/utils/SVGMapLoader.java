/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.engines.svgviewer.map.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.content.bo.Content;

/**
 * The Class SVGMapLoader.
 *
 */
public class SVGMapLoader {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SVGMapLoader.class);

	private static final Base64.Decoder DECODER = Base64.getDecoder();

	/** The document factory. */
	private SAXSVGDocumentFactory documentFactory;

	private String parser = XMLResourceDescriptor.getXMLParserClassName();
	/** The xml input factory. */
	private static XMLInputFactory xmlInputFactory;

	static {
		xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
	}

	/**
	 * Load map as document.
	 *
	 * @param file
	 *            the file
	 *
	 * @return the sVG document
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public SVGDocument loadMapAsDocument(File file) throws IOException {
		String url;
		url = file.toURI().toURL().toString();
		return loadMapAsDocument(url);
	}

	public SVGDocument loadMapAsDocument(Content map) throws IOException {
		String mapContent;

		mapContent = new String(DECODER.decode(map.getContent()));
		documentFactory = new SAXSVGDocumentFactory(parser);
		return (SVGDocument) documentFactory.createDocument(null, new StringReader(mapContent));
	}

	/**
	 * Load map as document.
	 *
	 * @param url
	 *            the url
	 *
	 * @return the sVG document
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public SVGDocument loadMapAsDocument(String url) throws IOException {
		logger.debug(url);
		SVGDocument svgDoc = null;
		try {
			documentFactory = new SAXSVGDocumentFactory(parser);
			svgDoc = (SVGDocument) documentFactory.createDocument(url);
		} catch (Throwable e) {
			logger.error(e);
		}
		return svgDoc;
	}

	/**
	 * Gets the map as stream.
	 *
	 * @param file
	 *            the file
	 *
	 * @return the map as stream
	 *
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	public static XMLStreamReader getMapAsStream(File file) throws FileNotFoundException, XMLStreamException {
		return xmlInputFactory.createXMLStreamReader(new FileInputStream(file));
	}

	/**
	 * Gets the map as stream.
	 *
	 * @param url
	 *            the url
	 *
	 * @return the map as stream
	 *
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	public static XMLStreamReader getMapAsStream(String url) throws FileNotFoundException, XMLStreamException {
		return xmlInputFactory.createXMLStreamReader(new FileInputStream(url));
	}

	public static String getDefaultMemberName(List confSBList) {
		String toReturn = null;
		int idx = 0;
		for (int i = 0; i < confSBList.size(); i++) {
			SourceBean memberSB = (SourceBean) confSBList.get(i);
			if (i == 0) {
				toReturn = (String) memberSB.getAttribute("name");
			}
			if (memberSB.getAttribute("name") == "1") {
				toReturn = (String) memberSB.getAttribute("name");
				break;
			}
			idx++;
			logger.error("Member with level [1]  not found into the template. Returned the first member found [" + (String) memberSB.getAttribute("name")
					+ "]! Check the template.");
		}

		return toReturn;

	}
}
