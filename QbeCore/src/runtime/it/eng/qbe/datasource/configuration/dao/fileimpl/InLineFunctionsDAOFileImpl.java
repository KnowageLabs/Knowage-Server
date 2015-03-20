/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration.dao.fileimpl;

import it.eng.qbe.datasource.configuration.dao.DAOException;
import it.eng.qbe.datasource.configuration.dao.IInLineFunctionsDAO;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.spagobi.utilities.assertion.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Implementation of IInLineFunctionsDAO that read functions code (ie. data functions)
 * 
 * @author Antonella Giachino
 */
public class InLineFunctionsDAOFileImpl implements IInLineFunctionsDAO {
	HashMap<String, InLineFunction> mapInLineFunctions = new HashMap<String, InLineFunction>();
	
	public static transient Logger logger = Logger.getLogger(ViewsDAOFileImpl.class);

	public static final String FUNCTIONS_FILE_NAME = "functions.xml";
	public final static String ROOT_TAG = "INLINE_FUNCTIONS";
	public final static String FIELD_TAG = "FUNCTION";
	public final static String FIELD_TAG_GROUP_ATTR = "group";
	public final static String FIELD_TAG_NAME_ATTR = "name";
	public final static String FIELD_TAG_DESC_ATTR = "desc";
	public final static String FIELD_TAG_NPARAMS_ATTR = "nParams";
	public final static String FIELD_TAG_MYSQL_DIALECT = "MySQLInnoDBDialect";
	public final static String FIELD_TAG_ORACLE_DIALECT = "OracleDialect";
	public final static String FIELD_TAG_INGRES_DIALECT = "IngresDialect";
	public final static String FIELD_TAG_POSTGRES_DIALECT = "PostgreSQLDialect";
	public final static String FIELD_TAG_HQL_DIALECT = "HSQLDialect";
	public final static String FIELD_TAG_SQLSERVER_DIALECT = "SQLServerDialect";
	public final static String FIELD_TAG_CODE_ATTR = "code";

	// =============================================================================
	// LOAD
	// =============================================================================
	
	public HashMap<String, InLineFunction> loadInLineFunctions(String dialect){
		
		FileInputStream in;
		InputStream is;
		Document document;
		String group;
		String name;
		String desc;
		String nParams;
		String code;
		List functionsNodes;
		Iterator it;
		Node functionNode;
		Node dialectNode;

		logger.debug("IN");
		
		in = null;		
		try {	
			if (getInLineFunctions() != null && getInLineFunctions().size() > 0) {
				logger.info("Functions for dialect " + dialect + " yet loaded." );
				return getInLineFunctions();
			}
			
			is = getClass().getClassLoader().getResourceAsStream(FUNCTIONS_FILE_NAME);
			Assert.assertNotNull(is, "Input stream cannot be null");
			
			logger.debug("Functions will be loaded from file [" + FUNCTIONS_FILE_NAME + "]");
							
			document = readFile(is);
			Assert.assertNotNull(document, "Document cannot be null");
			
			functionsNodes = document.selectNodes("//" + ROOT_TAG + "/" + FIELD_TAG + "");
			logger.debug("Found [" + functionsNodes.size() + "] functions");
			
			it = functionsNodes.iterator();				
			while (it.hasNext()) {
				functionNode = (Node) it.next();
				group = functionNode.valueOf("@" + FIELD_TAG_GROUP_ATTR);
				name = functionNode.valueOf("@" + FIELD_TAG_NAME_ATTR);
				desc = functionNode.valueOf("@" + FIELD_TAG_DESC_ATTR);
				nParams = functionNode.valueOf("@" + FIELD_TAG_NPARAMS_ATTR);
				dialectNode = null;
				//get the code function only for the dialect managed
				if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_MYSQL)){					
					dialectNode = functionNode.selectSingleNode(functionNode.getUniquePath()+ "/" + FIELD_TAG_MYSQL_DIALECT + "");
				}else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_HSQL)){		
					dialectNode = functionNode.selectSingleNode(functionNode.getUniquePath()+ "/" + FIELD_TAG_HQL_DIALECT + "");
				}else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE) ||
						  dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_ORACLE9i10g)){		
					dialectNode = functionNode.selectSingleNode(functionNode.getUniquePath()+ "/" + FIELD_TAG_ORACLE_DIALECT + "");
				}else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_INGRES)){	
					dialectNode = functionNode.selectSingleNode(functionNode.getUniquePath()+ "/" + FIELD_TAG_INGRES_DIALECT + "");
				}else if (dialect.equalsIgnoreCase(QuerySerializationConstants.DIALECT_POSTGRES)){		
					dialectNode = functionNode.selectSingleNode(functionNode.getUniquePath()+ "/" + FIELD_TAG_POSTGRES_DIALECT + "");
				}else{
					dialectNode = functionNode.selectSingleNode(functionNode.getUniquePath()+ "/" + FIELD_TAG_SQLSERVER_DIALECT + "");
				}
				code = "";
				if (dialectNode != null){
					code = dialectNode.valueOf( "@" + FIELD_TAG_CODE_ATTR );
				}
				InLineFunction func = new InLineFunction();
				func.setDialect(dialect);
				func.setName(name);
				func.setGroup(group);
				func.setDesc(desc);
				func.setnParams(Integer.valueOf(nParams));
				func.setCode(code);
				addInLineFunction(func);
				logger.debug("Function [" + mapInLineFunctions.get(func.name) + "] loaded succesfully");
			}	
			
		} catch(Throwable t){
			if(t instanceof DAOException) throw (DAOException)t;
			throw new DAOException("An unpredicted error occurred while loading functions on file [" + FUNCTIONS_FILE_NAME + "]", t);
		}finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					throw new DAOException("Impossible to properly close stream to file file [" + FUNCTIONS_FILE_NAME + "]", e);
				}
			}
			logger.debug("OUT");
		}
		
		return getInLineFunctions();
	}
	
	private Document readFile(InputStream in) {
		SAXReader reader;
		Document document;
		
		logger.debug("IN");

		reader = null;
		
		try {
			reader = new SAXReader();
			try {
				document = reader.read(in);
			} catch (DocumentException de) {
				DAOException e = new DAOException("Impossible to parse file ", de);
				e.addHint("Check if is a well formed XML file");
				throw e;
			}
			Assert.assertNotNull(document, "Document cannot be null");
		} catch(Throwable t) {
			if(t instanceof DAOException) throw (DAOException)t;
			throw new DAOException("An unpredicetd error occurred while reading from inputStream: " , t);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					throw new DAOException("Impossible to properly close stream", e);
				}
			}
			
			logger.debug("OUT");
		}	
		
		return document;
	}
	
	
	//PUBLIC FUNCTIONS
	
	public void addInLineFunction(InLineFunction func) {
		mapInLineFunctions.put(func.name, func);
	}
	
	public HashMap<String, InLineFunction> getInLineFunctions() {
		return mapInLineFunctions;
	}
	
	public InLineFunction getInLineFunctionByName(String name) {
		return (InLineFunction)mapInLineFunctions.get(name);
	}
	
	public List<InLineFunction> getInLineFunctionsByDialect(String dialect) {
		List toReturn = new ArrayList();
		for (int i=0; i<mapInLineFunctions.size(); i++){
			InLineFunction func =(InLineFunction) mapInLineFunctions.get(i);
			if (func.dialect.contains(dialect))
				toReturn.add(func);
		}
		return toReturn;
	}
	
	public static class InLineFunction {
		String group;
		String name;
		String desc;
		String code;
		String dialect;
		Integer nParams;
		
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the code
		 */
		public String getCode() {
			return code;
		}
		/**
		 * @param code the code to set
		 */
		public void setCode(String code) {
			this.code = code;
		}
		/**
		 * @return the group
		 */
		public String getGroup() {
			return group;
		}
		/**
		 * @param group the group to set
		 */
		public void setGroup(String group) {
			this.group = group;
		}
		/**
		 * @return the desc
		 */
		public String getDesc() {
			return desc;
		}
		/**
		 * @param desc the desc to set
		 */
		public void setDesc(String desc) {
			this.desc = desc;
		}
		/**
		 * @return the nParams
		 */
		public Integer getnParams() {
			return nParams;
		}
		/**
		 * @param nParams the nParams to set
		 */
		public void setnParams(Integer nParams) {
			this.nParams = nParams;
		}
		/**
		 * @return the dialect
		 */
		public String getDialect() {
			return dialect;
		}
		/**
		 * @param dialect the dialect to set
		 */
		public void setDialect(String dialect) {
			this.dialect = dialect;
		}

	}
}
