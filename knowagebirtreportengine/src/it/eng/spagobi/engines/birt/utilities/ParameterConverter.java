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
