/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.accessibility.xslt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Transformation {

	private static transient Logger logger = Logger.getLogger(Transformation.class);

	/**
	 * Excecutes xslt transformation, to produce a byte array representing html page.
	 * 
	 * @param xml
	 *            query result exported in xml format
	 * @param xsl
	 *            stylesheet , loaded on SpagoBI server as document's template
	 * @return result of xslt transformation
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static byte[] tarnsformXSLT(String xml, byte[] xsl) throws TransformerException, ParserConfigurationException, IOException, SAXException {
		logger.debug("IN");
		// create an instance of TransformerFactory
		final ByteArrayOutputStream byteArray;
		final Result result;
		final TransformerFactory transFact;
		final Transformer trans;

		byteArray = new ByteArrayOutputStream();
		result = new StreamResult(byteArray);
		transFact = TransformerFactory.newInstance();

		// TODO for security reasons it should be set to true
		transFact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
		logger.debug("Set FEATURE_SECURE_PROCESSING to false");

		final Source xmlSource;
		final Document doc = loadXMLFrom(new ByteArrayInputStream(xml.getBytes()));

		xmlSource = new DOMSource(doc);

		Source xsltSource = null;

		if (xsl == null) { // grab the XSL defined by the XML's xml-stylesheet instruction
			xsltSource = transFact.getAssociatedStylesheet(xmlSource, null, null, null);
		}

		if (xsl == null) {
			logger.error("Invalid xsl");
			throw new IllegalArgumentException("Invalid XSL file");
		}
		InputStream is = new ByteArrayInputStream(xsl);

		xsltSource = new StreamSource(is);

		trans = transFact.newTransformer(xsltSource);

		trans.transform(xmlSource, result);
		logger.debug("OUT");
		is.close();
		return byteArray.toByteArray();
	}

	public static Document loadXMLFrom(final InputStream is) throws SAXException, IOException

	{
		logger.debug("IN");
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException t) {
			logger.error("Unable to get Document Builder", t);
		}
		assert builder != null;
		final Document doc = builder.parse(is);
		is.close();
		logger.debug("OUT");
		return doc;
	}

}
