/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class FoldersJSONSerializer implements Serializer {
	
	private static Logger logger = Logger.getLogger(FoldersJSONSerializer.class);

	public static final String ID = "id";
	public static final String CODE = "code";
	public static final String CODTYPE = "codType";
	public static final String PATH = "path";
	public static final String PROG = "prog";
	public static final String PARENTID = "parentId";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	
	public static final String DEVROLES = "devRoles";
	public static final String TESTROLES = "testRoles";
	public static final String EXECROLES = "execRoles";
	public static final String BIOBJECTS = "biObjects";
	public static final String ACTIONS = "actions";
	
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		logger.debug("IN");
		if( !(o instanceof LowFunctionality) ) {
			logger.error("FoldersJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
			throw new SerializationException("FoldersJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			LowFunctionality lowFunct = (LowFunctionality)o;
			result = new JSONObject();
			
			result.put(ID, lowFunct.getId() );
			result.put(CODE, lowFunct.getCode() );
			result.put(CODTYPE, lowFunct.getCodType() );
			result.put(PATH, lowFunct.getPath() );			
			result.put(PROG, lowFunct.getProg() );
			result.put(PARENTID, lowFunct.getParentId() );
			MessageBuilder msgBuild=new MessageBuilder();
			//String lowFunctName=msgBuild.getUserMessage(lowFunct.getName(),null, locale);
			String lowFunctName=msgBuild.getI18nMessage(locale, lowFunct.getName());

			result.put(NAME, lowFunctName );
			//String description = lowFunct.getDescription() != null ? msgBuild.getUserMessage(lowFunct.getDescription(), null,locale) : "";
			String description = msgBuild.getI18nMessage(locale, lowFunct.getDescription());
			result.put(DESCRIPTION, description);	
			result.put(DEVROLES, lowFunct.getDevRoles() );
			result.put(TESTROLES, lowFunct.getTestRoles() );		
			result.put(EXECROLES, lowFunct.getExecRoles() );
			result.put(BIOBJECTS, lowFunct.getBiObjects() );		
			result.put(ACTIONS, new JSONArray());
		} catch (Throwable t) {
			logger.error("An error occurred while serializing object: " + o);
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		logger.debug("OUT");
		return result;
	}
	
	
}
