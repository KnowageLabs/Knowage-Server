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
