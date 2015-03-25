/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package it.eng.spagobi.tools.forecastedit.service.rest;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

/**
 * Forecast editing REST service
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

@Path("/forecast")
public class ForecastEditService {
	static private Logger logger = Logger.getLogger(ForecastEditService.class);

	@POST
	@Produces(MediaType.TEXT_HTML)
	// TODO: change produced output
	public String insertValues(@Context HttpServletRequest req) {
		try {
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			if (profile == null) {
				return "User must be logged in";
			}
			String userId = (String) profile.getUserUniqueIdentifier();
			IBIObjectDAO sbiObjectsDAO = DAOFactory.getBIObjectDAO();
			BIObject document = sbiObjectsDAO.loadBIObjectByLabel("EII_RPT_0013_Edit"); // TODO: to change
			Integer documentId = document.getId();
			List enabledRoles = sbiObjectsDAO.getCorrectRolesForExecution(documentId, profile);
			// check if current user can see this document
			if ((enabledRoles != null) && (enabledRoles.size() > 0)) {
				// Get fixed parameters values
				String year = req.getParameter("par_year");
				String closureCode = req.getParameter("par_closure_cd");
				String hierCode = req.getParameter("par_hier_cd");
				String accountCode = req.getParameter("par_account_cd");
				String accountName = req.getParameter("par_account_nm");

				Enumeration paramNames = req.getParameterNames();
				Map<String, ForecastValue> values = new HashMap<String, ForecastValue>();
				while (paramNames.hasMoreElements()) {
					String paramName = (String) paramNames.nextElement();
					// skip parameters values
					if (!paramName.startsWith("par_")) {
						String[] paramValues = req.getParameterValues(paramName);
						String paramValue = paramValues[0]; // single value

						String prefixName;
						boolean isAbsoluteValue = false;
						// 1 - Process values into a Map
						// ---------------------
						if (paramName.endsWith("result")) {
							// Absolute value
							prefixName = paramName.substring(0, paramName.lastIndexOf("_result"));
							isAbsoluteValue = true;
						} else {
							// Percent value
							prefixName = paramName;
						}

						if (values.containsKey(prefixName)) {
							// already present a value (abs or perc) for this prefixName
							ForecastValue forecastValue = values.get(prefixName);
							if (isAbsoluteValue) {
								forecastValue.setVarAbs(Double.valueOf(paramValue));
							} else {
								forecastValue.setVarPerc(Double.valueOf(paramValue));
							}
							values.put(prefixName, forecastValue);
						} else {
							// new ForecastValue object
							ForecastValue newForecastValue = new ForecastValue();
							String[] prefixParts = prefixName.split("_");
							String source = prefixParts[1];
							String target = prefixParts[0];
							newForecastValue.setSource(source);
							newForecastValue.setTarget(target);
							if (isAbsoluteValue) {
								newForecastValue.setVarAbs(Double.valueOf(paramValue));
							} else {
								newForecastValue.setVarPerc(Double.valueOf(paramValue));
							}
							values.put(prefixName, newForecastValue);
						}
					}

				}

				// ----------------------

				// 2- Scan map and insert values on database
				for (Map.Entry<String, ForecastValue> entry : values.entrySet()) {
					persistValues(entry, accountName, accountCode, hierCode, year, closureCode, userId);
				}

				return "<b>Values Updated</b>";

				// Uncomment next line for test
				// return printSubmittedValues(req);

			} else {
				// User cannot execute operation
				return "Operation forbidden";
			}

		} catch (Exception e) {
			logger.error("An unexpected error occured while executing Forecast Edit");
			throw new SpagoBIServiceException("An unexpected error occured while executing Forecast Edit", e);
		}

	}

	private void persistValues(Map.Entry<String, ForecastValue> entry, String accountName, String accountCode, String hierCode, String year,
			String closureCode, String userId) {

		try {
			ForecastValue forecastValue = entry.getValue();
			String source = forecastValue.getSource();
			String target = forecastValue.getTarget();
			double valAbs = forecastValue.getVarAbs();
			double valPerc = forecastValue.getVarPerc();

			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel("DWH BIENG");
			Connection connection = dataSource.getConnection();
			String insertQuery = "INSERT INTO SUPP_REP0_TRIS (ACCOUNT_NM, ACCOUNT_CD, HIER_CD, YEAR, CLOSURE_CD, INSERT_USER, CONS_SEGMENT_SOURCE, CONS_SEGMENT_TARGET, VAR_ABS, VAR_PERC ) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

			preparedStatement.setString(1, accountName);
			preparedStatement.setString(2, accountCode);
			preparedStatement.setString(3, hierCode);
			preparedStatement.setInt(4, Integer.valueOf(year));
			preparedStatement.setInt(5, Integer.valueOf(closureCode));
			preparedStatement.setString(6, userId);
			preparedStatement.setString(7, source);
			preparedStatement.setString(8, target);
			preparedStatement.setDouble(9, valAbs);
			preparedStatement.setDouble(10, valPerc);

			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (Exception e) {
			logger.error("An unexpected error occured while updating Forecast Edit");
			throw new SpagoBIServiceException("An unexpected error occured while updating Forecast Edit", e);
		}

	}

	/*
	 * Test method for print on output the submitted values
	 */
	private String printSubmittedValues(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		String title = "Submitted Values";
		String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";
		sb.append(docType + "<html>\n" + "<head><title>" + title + "</title></head>\n" + "<body bgcolor=\"#f0f0f0\">\n" + "<h1 align=\"center\">" + title
				+ "</h1>\n" + "<table width=\"100%\" border=\"1\" align=\"center\">\n" + "<tr bgcolor=\"#949494\">\n"
				+ "<th>Param Name</th><th>Param Value(s)</th>\n" + "</tr>\n");

		Enumeration paramNames = req.getParameterNames();

		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			sb.append("<tr><td>" + paramName + "</td>\n<td>");
			String[] paramValues = req.getParameterValues(paramName);
			// Read single valued data
			if (paramValues.length == 1) {
				String paramValue = paramValues[0];
				if (paramValue.length() == 0)
					sb.append("<i>No Value</i>");
				else
					sb.append(paramValue);
			} else {
				// Read multiple valued data
				sb.append("<ul>");
				for (int i = 0; i < paramValues.length; i++) {
					sb.append("<li>" + paramValues[i]);
				}
				sb.append("</ul>");
			}
		}
		return sb.toString();
	}

}
