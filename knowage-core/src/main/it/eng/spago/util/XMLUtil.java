/**

    Copyright 2004, 2007 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spago.

    Spago is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    any later version.

    Spago is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Spago; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spago.util;

import it.eng.spago.base.Constants;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.XMLObject;
import it.eng.spago.base.XMLSerializer;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.IConfigurationCreator;
import it.eng.spago.tracing.TracerSingleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Vector;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.Document;

/**
 * DATE            CONTRIBUTOR/DEVELOPER    NOTE
 * 13-12-2004		  Butano           SourceBean ora restituisce ArrayList
 * 21-01-2005		  Butano           Corretto percorso XML_HEADER_DEFAULT_DOCTYPE 									    
 **/

/**
 * @author
 */
public abstract class XMLUtil {
	public static final String XML_MAPPINGS = "XML_MAPPINGS.MAPPING";
	public static final String REPLACING = "REPLACING";
	public static final String REPLACED = "REPLACED";
	public static final String XML_HEADER_PREFIX = "<?xml";
	public static final String XML_HEADER_DEFAULT_VERSION = "1.0";
	public static final String XML_HEADER_DEFAULT_ENCODING = "ISO-8859-1";
	public static final String XML_HEADER_DEFAULT_DOCTYPE = "/WEB-INF/conf/spago/xhtml-lat1.ent";
	private static final int _xmlIdentationSpaces = 2;
	private static boolean _mappingsRetrieved = false;
	private static int _mappingsNumber = 0;
	private static Vector _replacingMappings = null;
	private static Vector _replacedMappings = null;
	private static String _doctypeDefinition = null;

	public static String getIndentation(int level) {
		StringBuffer indentationLevel = new StringBuffer();
		for (int i = 0; i < _xmlIdentationSpaces; i++)
			indentationLevel.append(" ");
		StringBuffer indentation = new StringBuffer();
		for (int i = 0; i < level; i++)
			indentation.append(indentationLevel);
		return indentation.toString();
	} // public static String getIndentation(int level)

	public static String normalizeAttribute(String attribute) {
		if (attribute == null)
			return null;
		char[] attributeArray = attribute.toCharArray();
		StringBuffer normalizedAttribute = new StringBuffer();
		for (int i = 0; i < attributeArray.length; i++) {
			char c = attributeArray[i];
			switch (c) {
			case '<': {
				normalizedAttribute.append("&lt;");
				break;
			} // case '<'
			case '>': {
				normalizedAttribute.append("&gt;");
				break;
			}
			case '&': {
				normalizedAttribute.append("&amp;");
				break;
			}
			case '"': {
				normalizedAttribute.append("&quot;");
				break;
			}

			/*
			 * case '\r': case '\n': { normalizedAttribute.append("&#"); normalizedAttribute.append(Integer.toString(c)); normalizedAttribute.append(';');
			 * break; }
			 */

			default: {
				normalizedAttribute.append(c);
			}
			} // switch (c)
		} // for (int i = 0; i < attributeArray.length; i++)
		return normalizedAttribute.toString();
	} // public static String normalizeAttribute(String attribute)

	private static String parseAttribute(String toParse, String replacing, String replaced) {
		if (toParse == null)
			return null;
		if ((replacing == null) || (replaced == null))
			return toParse;
		StringBuffer parsed = new StringBuffer();
		int parameterIndex = toParse.indexOf(replacing);
		while (parameterIndex != -1) {
			parsed.append(toParse.substring(0, parameterIndex));
			parsed.append(replaced);
			toParse = toParse.substring(parameterIndex + replacing.length(), toParse.length());
			parameterIndex = toParse.indexOf(replacing);
		} // while (parameterIndex != -1)
		parsed.append(toParse);
		return parsed.toString();
	} // private static String parseAttribute(String toParse, String

	// replacing, String replaced)
	public static String parseAttribute(String attribute) {
		if (attribute == null)
			return null;
		String parsed = attribute;
		if (!_mappingsRetrieved) {
			synchronized (it.eng.spago.util.XMLUtil.class) {
				if (!_mappingsRetrieved) {
					_mappingsRetrieved = true;
					ConfigSingleton configure = ConfigSingleton.getInstance();
					List mappings = configure.getAttributeAsList(XML_MAPPINGS);
					if (mappings == null) {
						_mappingsNumber = 0;
						return parsed;
					} // if (mappings == null)
					_mappingsNumber = mappings.size();
					_replacingMappings = new Vector();
					_replacedMappings = new Vector();
					for (int i = 0; i < _mappingsNumber; i++) {
						SourceBean mapping = (SourceBean) mappings.get(i);
						_replacingMappings.addElement(mapping.getAttribute(REPLACING));
						_replacedMappings.addElement(mapping.getAttribute(REPLACED));
					} // for (int i = 0; i < _mappingsNumber; i++)
				} // if (!_mappingsRetrieved)
			} // synchronized(AbstractXMLObject.class)
		} // if (!_mappingsRetrieved)
		for (int i = 0; i < _mappingsNumber; i++)
			parsed = parseAttribute(parsed, (String) _replacingMappings.elementAt(i), (String) _replacedMappings.elementAt(i));
		return parsed;
	} // public static String parseAttribute(String attribute)

	public static String getDoctypeFilename() {
		// String rootPath = ConfigSingleton.getRootPath();
		// if (rootPath == null) {
		// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "XMLUtil::getDoctype: rootPath non valido");
		// rootPath = "";
		// } // if (rootPath == null)
		String doctypeFilename = (String) ConfigSingleton.getInstance().getAttribute("COMMON.XHTML_LAT1_ENT");
		if (doctypeFilename == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "XMLUtil::getDoctype: doctypeFilename non valido");
			doctypeFilename = XML_HEADER_DEFAULT_DOCTYPE;
		} // if (doctypeFilename == null)
		// return rootPath + doctypeFilename;
		return doctypeFilename;
	} // public static String getDoctypeFilename()

	public static String getDoctypeDefinition() {
		if (_doctypeDefinition == null)
			synchronized (XMLUtil.class) {
				if (_doctypeDefinition == null) {
					IConfigurationCreator configCreator = ConfigSingleton.getConfigurationCreator();
					StringBuffer doctypeDefinitionBuffer = new StringBuffer();
					try {
						InputStream inDoctypeFile = configCreator.getInputStream(getDoctypeFilename());
						int ch = 0;
						while ((ch = inDoctypeFile.read()) != -1) {
							doctypeDefinitionBuffer.append((char) ch);
						}
						inDoctypeFile.close();
					} // try
					catch (Exception ex) {
						TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "XMLUtil::getDoctypeDefinition: ", ex);
					} // catch (Exception ex)
					_doctypeDefinition = doctypeDefinitionBuffer.toString();
				} // if (_doctypeDefinition == null)
			} // synchronized(XMLUtil.class)
		return _doctypeDefinition;
	} // public static String getDoctypeDefinition()

	public static String toXML(XMLObject xmlObject, boolean inlineEntity, boolean indent, XMLSerializer serializer) {
		Document document = xmlObject.toDocument(serializer);

		if (document == null)
			return "";

		OutputFormat format = new OutputFormat(document);

		/**
		 * PROBLEM WITH THE WRONG ENCODING SET TO THE XML STRING REPRESENTATION OF THE CHART TEMPLATE INSIDE THE xmlObject INPUT PARAMETER. FOR THIS
		 * APPLICATION, IT IS ALWAYS SET TO UTF-8, EVEN IF WE APPLY THE XML TEMPLATE WITH SOME DIFFERENT ENCODING (e.g. ISO-8859-1).
		 * 
		 * This line (encoding of the format) is commented due to the problem stated in the KNOWAGE-775 JIRA issue (title
		 * "COCKPIT-Chart: letters with accent are not correctly decoded").
		 * 
		 * It is found that the XML template of a chart has the UTF-8 encoding set in its header, no matter what is the encoding set on saving of the chart
		 * document (or uploading of the template with some different encoding, e.g. ISO-8859-1). It is not clear why this happens.
		 * 
		 * THE WORKAROUND: the encoding setting of the format will be skipped in order to avoid misbehavior and wrong encoding of the XML string representation
		 * (inside the xmlObject input parameter). This way we are keeping all the details provided in the template, such as specific Italian letters (such as
		 * à, è, ò etc.). Otherwise, this line would encode that XML string with the default encoding type (XML_HEADER_DEFAULT_ENCODING), that is ISO-8859-1. If
		 * we let this happen, we would lose mentioned specific letters and we will start getting weird ones on the chart (such as Ã¨).
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		// format.setEncoding(XML_HEADER_DEFAULT_ENCODING);

		format.setIndenting(indent);
		format.setIndent(indent ? 2 : 0);
		format.setLineWidth(0);
		format.setLineSeparator(ConfigSingleton.LINE_SEPARATOR);
		format.setPreserveEmptyAttributes(true);
		StringWriter stringOut = new StringWriter();
		org.apache.xml.serialize.XMLSerializer serial = new org.apache.xml.serialize.XMLSerializer(stringOut, format);
		try {
			serial.asDOMSerializer();
			if (inlineEntity)
				serial.serialize(document);
			else
				serial.serialize(document.getDocumentElement());
		} // try
		catch (IOException ex) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "XMLUtil::toXML: ", ex);
		} // catch (IOException ex)
		return stringOut.toString();
	} // public static String toXML(XMLObject xmlObject, boolean inlineEntity)

	public static Document toDocument(XMLObject xmlObject, XMLSerializer serializer) {
		DocumentImpl document = new DocumentImpl();
		document.appendChild(xmlObject.toElement(document, serializer));
		DocumentTypeImpl dtd = new DocumentTypeImpl(document, "dtd");
		dtd.setInternalSubset(getDoctypeDefinition());
		document.appendChild(dtd);
		return document;
	} // public static Document toDocument(XMLObject xmlObject)
} // public class AbstractXMLObject
