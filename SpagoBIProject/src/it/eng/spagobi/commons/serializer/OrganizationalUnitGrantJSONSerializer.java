/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class OrganizationalUnitGrantJSONSerializer implements Serializer {
	
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String IS_AVAILABLE = "isAvailable";
	public static final String START_DATE = "startdate";
	public static final String END_DATE = "enddate";
	public static final String HIERARCHY = "hierarchy";
	public static final String MODEL_INSTANCE = "modelinstance";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof OrganizationalUnitGrant) ) {
			throw new SerializationException("OrganizationalUnitGrantJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			OrganizationalUnitGrant grant = (OrganizationalUnitGrant) o;
			result = new JSONObject();
			result.put(ID, grant.getId() );
			result.put(LABEL, grant.getLabel() );
			result.put(NAME, grant.getName() );
			result.put(DESCRIPTION, grant.getDescription() );
			if(grant.getIsAvailable() != null){
				result.put(IS_AVAILABLE, grant.getIsAvailable());
			}
			String df = GeneralUtilities.getServerDateFormat();
			SimpleDateFormat dateFormat = new SimpleDateFormat();
			dateFormat.applyPattern(df);
			dateFormat.setLenient(false);
			result.put(START_DATE, dateFormat.format(grant.getStartDate()) );
			result.put(END_DATE, dateFormat.format(grant.getEndDate()) );
			
			OrganizationalUnitHierarchyJSONSerializer hierarchySer = new OrganizationalUnitHierarchyJSONSerializer();
			JSONObject hierarchyJSON = (JSONObject) hierarchySer.serialize(grant.getHierarchy(), locale);
			result.put(HIERARCHY, hierarchyJSON);
			
			ModelInstanceNodeJSONSerializer modelSer = new ModelInstanceNodeJSONSerializer();
			JSONObject modelInstanceJSON = (JSONObject) modelSer.serialize(grant.getModelInstance(), locale);
			result.put(MODEL_INSTANCE, modelInstanceJSON);
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		}
		
		return result;
	}
	
	
}
