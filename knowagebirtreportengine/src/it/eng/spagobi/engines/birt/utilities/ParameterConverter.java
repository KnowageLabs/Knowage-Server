/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.birt.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

public class ParameterConverter {

    protected static Logger logger = Logger.getLogger(ParameterConverter.class);

    /**
     * Convert parameter.
     * 
     * @param paramType the param type
     * @param paramValueString the param value string
     * @param dateformat the dateformat
     * 
     * @return the object
     */
    public static Object convertParameter(int paramType, String paramValueString, String dateformat) {
	logger.debug("IN.paramValueString=" + paramValueString + " /dateformat=" + dateformat + " /paramType="
		+ paramType);
	Object paramValue = null;

	switch (paramType) {

	case IScalarParameterDefn.TYPE_DATE:
	    try {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		Date date = dateFormat.parse(paramValueString);
		paramValue = DataTypeUtil.toSqlDate(date);
	    } catch (Exception e) {
		logger.error(ParameterConverter.class.getName() + "findReportParams() " + "Error parsing the string ["
			+ paramValueString + "] " + "as a date using the format [" + dateformat + "].", e);
	    }
	    break;

	case IScalarParameterDefn.TYPE_DATE_TIME:
	    try {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		Date date = dateFormat.parse(paramValueString);
		paramValue = DataTypeUtil.toDate(date);
	    } catch (Exception e) {
		logger.error(ParameterConverter.class.getName() + "findReportParams() " + "Error parsing the string ["
			+ paramValueString + "] " + "as a date using the format [" + dateformat + "].", e);
	    }
	    break;

	case IScalarParameterDefn.TYPE_TIME:
	    try {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		Date date = dateFormat.parse(paramValueString);
		paramValue = DataTypeUtil.toSqlTime(date);
	    } catch (Exception e) {
		logger.error(ParameterConverter.class.getName() + "findReportParams() " + "Error parsing the string ["
			+ paramValueString + "] " + "as a date using the format [" + dateformat + "].", e);
	    }
	    break;

	case IScalarParameterDefn.TYPE_BOOLEAN:
	    try {
		paramValue = new Boolean(paramValueString);
	    } catch (Exception e) {
		logger.error(ParameterConverter.class.getName() + "findReportParams() " + "Error parsing the string ["
			+ paramValueString + "] as a Boolean.", e);
	    }
	    break;

	case IScalarParameterDefn.TYPE_INTEGER:
	    try {
		paramValue = new Integer(paramValueString);
	    } catch (Exception e) {
		logger.error(ParameterConverter.class.getName() + "findReportParams() " + "Error parsing the string ["
			+ paramValueString + "] as an integer.", e);
	    }
	    break;

	case IScalarParameterDefn.TYPE_DECIMAL:
	    try {
		// Spago uses Double (and Float) number format
		paramValue = new Double(paramValueString);
	    } catch (Exception e) {
		logger.error(ParameterConverter.class.getName() + "findReportParams() " + "Error parsing the string ["
			+ paramValueString + "] as a double.", e);
	    }
	    break;

	case IScalarParameterDefn.TYPE_FLOAT:
	    try {
		// Spago uses Double (and Float) number format
		paramValue = new Double(paramValueString);
	    } catch (Exception e) {
		logger.debug(ParameterConverter.class.getName() + "findReportParams() " + "Error parsing the string ["
			+ paramValueString + "] as a double.", e);
	    }
	    break;

	case IScalarParameterDefn.TYPE_STRING:
	    paramValue = paramValueString;
	    break;

	default:
	    paramValue = paramValueString;
	}
	logger.debug("OUT");
	return paramValue;
    }

}
