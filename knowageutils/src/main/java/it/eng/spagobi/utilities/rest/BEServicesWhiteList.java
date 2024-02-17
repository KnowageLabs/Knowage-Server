package it.eng.spagobi.utilities.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class BEServicesWhiteList {
	private static final Logger LOGGER = LogManager.getLogger(BEServicesWhiteList.class);

	private static final BEServicesWhiteList INSTANCE = new BEServicesWhiteList();
	private static final String PROPERTY_PATH = "be-services-whitelist.xml";
	private static final String PROPERTY_ELEMENT_NAME = "backendservice";
	private static final String PROPERTY_ATTRIBUTE_NAME = "baseurl";
	
	
	private final List<String> whitelist = new ArrayList<>();
	
	private DocumentBuilderFactory dbf = null;
	
    private BEServicesWhiteList() {}
    
    public static BEServicesWhiteList getInstance() {
		return INSTANCE;
	}
    
	public List<String> getWhitelist() {
		loadWhitelist();
		return whitelist;
	}
	
	private void loadWhitelist() {
		if (whitelist.isEmpty()) {
			loadEntriesFromClasspath();
		}
	}
	
	private void loadEntriesFromClasspath() {
		ClassLoader classLoader = this.getClass().getClassLoader();

		try {
			// prepare the doc builder factory
			dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			Enumeration<URL> resources = classLoader.getResources(PROPERTY_PATH);
			List<URL> resourcesAslist = Collections.list(resources);

			for (URL url : resourcesAslist) {
				manageClasspathEntry(url);
			}
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Cannot list " + PROPERTY_PATH + " from classpat", e);
		} catch (ParserConfigurationException e) {
			throw new SpagoBIRuntimeException("Cannot configure DocumentBuilderFactory to read whitelist ", e);
		}
	}
	
	
	private void manageClasspathEntry(URL url) {
		try (InputStream inputStream = url.openStream()) {
			loadServicesBaseURLOntoWhitelist(inputStream);
		} catch (IOException e) {
			LOGGER.warn("Non-fatal error loading {} from classpath. Skipping!", url);
		}
	}
	
	private void loadServicesBaseURLOntoWhitelist(InputStream inputStream) {
		try {
			
			DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.parse(inputStream);
	        doc.getDocumentElement().normalize();
	        NodeList list = doc.getElementsByTagName(PROPERTY_ELEMENT_NAME);
	        for(int i = 0; i < list.getLength(); i++) {
	        	Node node = list.item(i);
	        	String baseUrl = node.getAttributes().getNamedItem(PROPERTY_ATTRIBUTE_NAME).getTextContent();
	        	whitelist.add(baseUrl);
	        }

		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Errors closing input stream for be-services-whitelist.xml file.");
		} catch (SAXException e) {
			throw new SpagoBIRuntimeException("Errors creating DocumentBuilder to read be-services-whitelist.xml file.");
		} catch (ParserConfigurationException e) {
			throw new SpagoBIRuntimeException("Errors while parsing be-services-whitelist.xml file.");
		}
	}

}
