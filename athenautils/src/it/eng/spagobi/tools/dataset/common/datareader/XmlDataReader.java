/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datareader;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @authors 
 * 		Angelo Bernabei (angelo.bernabei@eng.it)
 * 		Andrea Gioia (andrea.gioia@eng.it)
 */
public class XmlDataReader extends AbstractDataReader {

	DocumentBuilderFactory domFactory;
	
	private static transient Logger logger = Logger.getLogger(XmlDataReader.class);

	public XmlDataReader() {
		super();
		domFactory = DocumentBuilderFactory.newInstance();
        
	}

	public IDataStore read( Object data ) {
		String dataString;
		InputStream dataStream;
		
		DataStore dataStore;
		MetaData dataStoreMeta;

		logger.debug("IN");

		dataStream = null;
		dataStore = new DataStore();
		dataStoreMeta = new MetaData();
		dataStore.setMetaData(dataStoreMeta);


		try {
			if(data == null) throw new IllegalArgumentException("Input parameter [data] cannot be null");
			
			
			dataStream = openStream(data);
			DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
			Document document = null;
			try {
				document = documentBuilder.parse(dataStream);
			} catch(Throwable t) {
				if(data instanceof String) {
					document = adaptSyntax((String)data);
				} else {
					throw t;
				}
			}
			
			NodeList nodes = readXMLNodes(document, "/ROWS/ROW");
			if(nodes == null) {
				throw new RuntimeException("Malformed data. Impossible to find tag rows.row");
			}

			int rowNumber = nodes.getLength();
			boolean firstRow = true;
			for (int i = 0; i < rowNumber; i++, firstRow = false) {
				IRecord record = new Record(dataStore);
				
			    NamedNodeMap nodeAttributes = nodes.item(i).getAttributes();
			    for(int j = 0; j < nodeAttributes.getLength(); j++) {
				    Node attribute = nodeAttributes.item(j);
				    String columnName = attribute.getNodeName();
				    String columnValue = attribute.getNodeValue();
				    Class columnType = attribute.getNodeValue().getClass();
				    
				    if(firstRow==true) {
						FieldMetadata fieldMeta = new FieldMetadata();
						fieldMeta.setName( columnName );
						fieldMeta.setType( columnType );
						dataStoreMeta.addFiedMeta(fieldMeta);
					}
				    
				    IField field = new Field(columnValue);
					record.appendField(field);
			    }
			    
			    dataStore.appendRecord(record);
			}
			 
			 
		} catch (Throwable t) {
			logger.error("Exception reading data", t);
		} finally{
			if(dataStream!=null)
				try {
					dataStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("IOException during File Closure");
				}
		}

		return dataStore;
	}
	
	public boolean isSyntaxCorrect(String data) {
		
		logger.debug("IN");
		
		SourceBean dataSourceBean = null;
		try {
			dataSourceBean = SourceBean.fromXMLString( data );
		} catch (Throwable t) {
			return false;
		}
		
		if(dataSourceBean == null || !dataSourceBean.getName().equalsIgnoreCase("ROWS")) {
			return false;
		} 
		
		List rowsList = dataSourceBean.getAttributeAsList(DataRow.ROW_TAG);
		if( (rowsList == null) || (rowsList.size()==0) ) {
			return false;
		}			
						
		logger.debug("OUT");
		
		return true;
	}
	
	private Document adaptSyntax(String data) {

		Document document;
		InputStream dataStream;
		
		logger.debug("IN");
		
		document = null;
		dataStream = null;
		try {
			StringBuffer dataBuffer = new StringBuffer();
			dataBuffer.append("<ROWS>");
			dataBuffer.append("<ROW value=\"" + data +"\"/>");
			dataBuffer.append("</ROWS>");
			
			dataStream =  openStream(dataBuffer.toString());
			DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
			document = documentBuilder.parse( dataStream );
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while adapting syntax of script result [" + data + "]", t);
		} finally {
			if(dataStream != null)
				try {
					dataStream.close();
				} catch (IOException t) {
					throw new SpagoBIRuntimeException("Impossible to close stream associated to data string [" + data + "]", t);
				}
			logger.debug("OUT");
		}
		
		
		return document;
	}
	
	private NodeList readXMLNodes(Document doc, String xpathExpression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(xpathExpression);
 
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
 
        return nodes;
    }
	
	private InputStream openStream( Object data ) {
		InputStream inputDataStream;
		if (!(data instanceof InputStream)) {
			inputDataStream = new StringBufferInputStream((String)data);
		} else{
			inputDataStream = (InputStream)data;
		}
		return inputDataStream;
	}

}
