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
package it.eng.spagobi.engines.jasperreport;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class ParameterConverter {

    protected static Logger logger = Logger.getLogger(ParameterConverter.class);

    /**
     * Convert parameter into the nature specified by paramType class.
     * 
     * @param paramType the param type
     * @param paramValueString the param value string
     * @param dateformat the dateformat
     * 
     * @return the object
     */
    public static Object convertParameter(Class paramType, String paramValueString, String dateformat) {
    	logger.debug("IN.paramValueString=" + paramValueString + " /dateformat=" + dateformat + " /paramType="+ paramType);
    	Object paramValue = null;
    	if (paramType == null) {
    		logger.error("paramType input parameter is null!!! cannot convert parameter without knowing its nature");
    		logger.debug("Returning null");
    		return paramValue;
    	}
    	
    	String paramTypeName = paramType.getName();
    	logger.debug("Parameter type is [" + paramTypeName + "]");
    	
    	if (paramTypeName.equals(new String().getClass().getName())) {
	    	paramValue = paramValueString;
    	} else if (paramTypeName.equals(new Date().getClass().getName())) {
    		try {
	    		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
	    		paramValue = dateFormat.parse(paramValueString);
    	    } catch (Exception e) {
    			logger.error("Error parsing the string [" + paramValueString + "] " + "as a java.util.Date using the format [" + dateformat + "].", e);
    	    }
    	} else if (paramTypeName.equals(new java.sql.Date(0).getClass().getName())) {
    		try {
	    		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
	    		Date theDate = dateFormat.parse(paramValueString);
	    		paramValue = new java.sql.Date(theDate.getTime());
    	    } catch (Exception e) {
    			logger.error("Error parsing the string [" + paramValueString + "] " + "as a java.sql.Date using the format [" + dateformat + "].", e);
    	    }
    	} else if (paramTypeName.equals(new java.sql.Timestamp(0).getClass().getName())) {
    		try {
	    		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
	    		Date theDate = dateFormat.parse(paramValueString);
	    		paramValue = new java.sql.Timestamp(theDate.getTime());
    	    } catch (Exception e) {
    			logger.error("Error parsing the string [" + paramValueString + "] " + "as a java.sql.Date using the format [" + dateformat + "].", e);
    	    }
    	} else {
    		// try using the constructor with a String as input (valid for Boolean, Integer, Double, Float, Short, Long and may be others....)
        	try {
        		paramValue = paramType.getConstructor(String.class).newInstance(paramValueString);
    		} catch (Exception e) {
    			logger.error("Error parsing the string [" + paramValueString + "] as a [" + paramTypeName + "].", e);
    		}
    	}
    	
    	if (paramValue == null) {
    		logger.debug("Returning null");
    	}
		
    	logger.debug("OUT");
    	return paramValue;
    }

}
