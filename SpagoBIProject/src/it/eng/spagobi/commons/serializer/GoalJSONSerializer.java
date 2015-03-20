/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.kpi.goal.metadata.bo.Goal;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONObject;

public class GoalJSONSerializer  implements Serializer {
	
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String START_DATE = "startdate";
	public static final String END_DATE = "enddate";
	public static final String GRANT_ID = "grantid";
	public static final String GRANT_NAME = "grantname";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Goal) ) {
			throw new SerializationException("GoalJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Goal goal = (Goal) o;
			result = new JSONObject();
			result.put(ID, goal.getId() );
			result.put(NAME, goal.getName() );
			result.put(DESCRIPTION, goal.getDescription() );
			result.put(LABEL, goal.getLabel() );
			String df = GeneralUtilities.getServerDateFormat();
			SimpleDateFormat dateFormat = new SimpleDateFormat();
			dateFormat.applyPattern(df);
			dateFormat.setLenient(false);
			result.put(START_DATE, dateFormat.format(goal.getStartDate()) );
			result.put(END_DATE, dateFormat.format(goal.getEndDate()) );
			
			if(goal.getGrant()!=null){
				
				OrganizationalUnitGrant grant = DAOFactory.getOrganizationalUnitDAO().getGrant(goal.getGrant());
				result.put(GRANT_ID, goal.getGrant());
				result.put(GRANT_NAME, grant.getName());
			}
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		}
		
		return result;
	}
	
	
}
