/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.List;

/**
 */
public class JavaClassUtils {

	/**
	 * checks if the result is formatted in the right xml structure
	 * @param result the result of the lov
	 * @return true if the result is formatted correctly false otherwise
	 * @throws EMFUserError 
	 */
	public static boolean checkSintax(String result) throws EMFUserError {
		boolean toconvert = false;
		try{
			SourceBean source = SourceBean.fromXMLString(result);
			if(!source.getName().equalsIgnoreCase("ROWS")) {
				toconvert = true;
			} else {
				List rowsList = source.getAttributeAsList(DataRow.ROW_TAG);
				if( (rowsList==null) || (rowsList.size()==0) ) {
					toconvert = true;
				}
			}
			
		} catch (Exception e) {
			SpagoBITracer.warning(SpagoBIConstants.NAME_MODULE, JavaClassUtils.class.getName(), 
					              "checkSintax", "the result of the java class lov is not formatted " +
					              "with the right structure so it will be wrapped inside an xml envelope");
			EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 9219);
			throw userError;
		}
		return toconvert;
	}
	
	
	/**
	 * Wraps the result of the query execution into the right xml structure
	 * @param result the result of the query (which is not formatted with the right xml structure)
	 * @return the xml structure of the result 
	 */
	public static String convertResult(String result) {
		StringBuffer sb = new StringBuffer();
		sb.append("<ROWS>");
		sb.append("<ROW VALUE=\"" + result +"\"/>");
		sb.append("</ROWS>");
		return sb.toString();
	}
	
}
