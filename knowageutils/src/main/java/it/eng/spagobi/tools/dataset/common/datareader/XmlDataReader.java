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
package it.eng.spagobi.tools.dataset.common.datareader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import javax.xml.XMLConstants;
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

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Angelo Bernabei (angelo.bernabei@eng.it) Andrea Gioia (andrea.gioia@eng.it)
 */
public class XmlDataReader extends AbstractDataReader {

	DocumentBuilderFactory domFactory;

	private static transient Logger logger = Logger.getLogger(XmlDataReader.class);

	public XmlDataReader() {
		domFactory = DocumentBuilderFactory.newInstance();

	}

	@Override
	public IDataStore read(Object data) {
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
			if (data == null)
				throw new IllegalArgumentException("Input parameter [data] cannot be null");

			dataStream = openStream(data);
			DocumentBuilder documentBuilder = getFactory().newDocumentBuilder();
			Document document = null;
			try {
				document = documentBuilder.parse(dataStream);
			} catch (Throwable t) {
				if (data instanceof String) {
					document = adaptSyntax((String) data);
				} else {
					throw t;
				}
			}

			NodeList nodes = readXMLNodes(document, "/ROWS/ROW");
			if (nodes == null) {
				throw new RuntimeException("Malformed data. Impossible to find tag rows.row");
			}

			boolean checkMaxResults = false;
			if ((maxResults > 0)) {
				checkMaxResults = true;
			}

			boolean paginated = false;
			logger.debug("Reading data ...");
			if (isPaginationSupported() && getOffset() >= 0 && getFetchSize() >= 0) {
				logger.debug("Offset is equal to [" + getOffset() + "] and fetchSize is equal to [" + getFetchSize() + "]");
				paginated = true;
			} else {
				logger.debug("Offset and fetch size not set");
			}

			int rowFetched = 0;
			int rowNumber = nodes.getLength();
			boolean firstRow = true;
			for (int i = 0; i < rowNumber; i++, firstRow = false) {
				if ((!paginated && (!checkMaxResults || (rowFetched < maxResults)))
						|| ((paginated && (rowFetched >= offset) && (rowFetched - offset < fetchSize))
								&& (!checkMaxResults || (rowFetched - offset < maxResults)))) {
					IRecord record = new Record(dataStore);

					NamedNodeMap nodeAttributes = nodes.item(i).getAttributes();
					for (int j = 0; j < nodeAttributes.getLength(); j++) {
						Node attribute = nodeAttributes.item(j);
						String columnName = attribute.getNodeName();
						String columnValue = attribute.getNodeValue();
						Class columnType = attribute.getNodeValue().getClass();

						if (firstRow) {
							FieldMetadata fieldMeta = new FieldMetadata();
							fieldMeta.setName(columnName);
							fieldMeta.setType(columnType);
							dataStoreMeta.addFiedMeta(fieldMeta);
						}

						IField field = new Field(columnValue);
						record.appendField(field);
					}

					dataStore.appendRecord(record);
				}
				rowFetched++;
			}
			logger.debug("Read [" + rowFetched + "] records");
			logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");

			if (this.isCalculateResultNumberEnabled()) {
				logger.debug("Calculation of result set number is enabled");
				dataStore.getMetaData().setProperty("resultNumber", new Integer(rowFetched));
			} else {
				logger.debug("Calculation of result set number is NOT enabled");
			}

		} catch (Throwable t) {
			logger.error("Exception reading data", t);
		} finally {
			if (dataStream != null)
				try {
					dataStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("IOException during File Closure");
				}
		}

		return dataStore;
	}

	private static DocumentBuilderFactory getFactory() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-parameterentities", false);
			dbf.setFeature("http://xml.org/sax/features/external-generalentities", false);
		} catch (ParserConfigurationException e) {
			logger.error("Error configuring DocumentBuilderFactory: " + e.getMessage(), e);
			throw new SecurityException("Error configuring DocumentBuilderFactory: " + e.getMessage(), e);
		}

		dbf.setXIncludeAware(false);
		dbf.setExpandEntityReferences(false);
		return dbf;
	}

	public boolean isSyntaxCorrect(String data) {

		logger.debug("IN");

		SourceBean dataSourceBean = null;
		try {
			dataSourceBean = SourceBean.fromXMLString(data);
		} catch (Throwable t) {
			return false;
		}

		if (dataSourceBean == null || !dataSourceBean.getName().equalsIgnoreCase("ROWS")) {
			return false;
		}

		List rowsList = dataSourceBean.getAttributeAsList(DataRow.ROW_TAG);
		if ((rowsList == null) || (rowsList.size() == 0)) {
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
			StringBuilder dataBuffer = new StringBuilder();
			dataBuffer.append("<ROWS>");
			dataBuffer.append("<ROW value=\"" + data + "\"/>");
			dataBuffer.append("</ROWS>");

			dataStream = openStream(dataBuffer.toString());
			DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
			document = documentBuilder.parse(dataStream);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while adapting syntax of script result [" + data + "]", t);
		} finally {
			if (dataStream != null)
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

	private InputStream openStream(Object data) {
		InputStream inputDataStream;
		if (!(data instanceof InputStream)) {
			inputDataStream = new StringBufferInputStream((String) data);
		} else {
			inputDataStream = (InputStream) data;
		}
		return inputDataStream;
	}

	@Override
	public boolean isOffsetSupported() {
		return true;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}

	@Override
	public boolean isMaxResultsSupported() {
		return true;
	}

}
