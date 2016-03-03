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
package it.eng.spagobi.engines.qbe;

import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class SmartFilterAnalysisState extends EngineAnalysisState {

	public static final String FORMVALUES = "FORM_VALUES";
		
	/** Logger component. */

    private static transient Logger logger = Logger.getLogger(SmartFilterAnalysisState.class);
	
	public SmartFilterAnalysisState( JSONObject formValues ) {
		super( );
		setFormValues( formValues );
	}

	public SmartFilterAnalysisState() {
		super( );
	}
	
	public void setFormValues(JSONObject formValues ){
		setProperty( FORMVALUES, formValues );
	}
	
	public JSONObject getFormValues(){
		return (JSONObject)getProperty( FORMVALUES);
	}
	
	public byte[] store() throws SpagoBIEngineException {
		JSONObject formJSON = null;
		JSONObject rowDataJSON = null;
		String rowData = null;	
		
		formJSON = (JSONObject)getProperty( FORMVALUES );
		
		try {
			rowDataJSON = new JSONObject();
			rowDataJSON.put(FORMVALUES, formJSON);		
			rowData = rowDataJSON.toString();
		} catch (Throwable e) {
			throw new SpagoBIEngineException("Impossible to store analysis state from catalogue object", e);
		}
		
		return rowData.getBytes();
	}
	
	public void load(byte[] rowData) throws SpagoBIEngineException {
		String str = null;
		JSONObject rowDataJSON = null;
		String encodingFormatVersion;
		
		logger.debug("IN");

		try {
			if(rowData!=null){
				str = new String( rowData );
				logger.debug("loading analysis state from row data [" + str + "] ...");
				
				rowDataJSON = new JSONObject(str);
				try {
					encodingFormatVersion = rowDataJSON.getString("version");
				} catch (JSONException e) {
					encodingFormatVersion = "0";
				}
				
				logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");
							
				JSONObject formJSON = rowDataJSON.getJSONObject(FORMVALUES);
	
				setProperty( FORMVALUES,  formJSON);
	
				logger.debug("analysis state loaded succsfully from row data");
			}
		} catch (JSONException e) {
			throw new SpagoBIEngineException("Impossible to load analysis state from raw data", e);
		} finally {
			logger.debug("OUT");
		}
	}
}
		
