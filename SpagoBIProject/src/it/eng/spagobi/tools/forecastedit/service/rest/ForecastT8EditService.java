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
import java.util.List;

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

@Path("/forecastT8")
public class ForecastT8EditService {
	static private Logger logger = Logger.getLogger(ForecastT8EditService.class);

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
			BIObject document = sbiObjectsDAO.loadBIObjectByLabel("EII_RPT_0015_frc"); // TODO: to change
			Integer documentId = document.getId();
			List enabledRoles = sbiObjectsDAO.getCorrectRolesForExecution(documentId, profile);
			// check if current user can see this document
			if ((enabledRoles != null) && (enabledRoles.size() > 0)) {
				// Get fixed parameters values
				String year = req.getParameter("par_year");
				String closureCode = req.getParameter("par_closure_cd");
				String businessAreaCode = req.getParameter("par_ba");
				String businessUnitCode = req.getParameter("par_bu");
				String cdc = req.getParameter("par_cdc");

				String ricavi = req.getParameter("Ricavi_resValue");
				String ricaviVarPer = req.getParameter("Ricavi_variationPerc");
				if (ricaviVarPer == null) {
					ricaviVarPer = "0";
				}
				String ricaviVarAbs = req.getParameter("Ricavi_variationAbsolute");
				if (ricaviVarAbs == null) {
					ricaviVarAbs = "0";
				}

				String primoMargine = req.getParameter("PM_resValue");
				String primoMargineVarPer = req.getParameter("PM_variationPerc");
				if (primoMargineVarPer == null) {
					primoMargineVarPer = "0";
				}
				String primoMargineVarAbs = req.getParameter("PM_variationAbsolute");
				if (primoMargineVarAbs == null) {
					primoMargineVarAbs = "0";
				}

				String margineContribuzione = req.getParameter("MC_resValue");
				String margineContribuzioneVarPer = req.getParameter("MC_variationPerc");
				if (margineContribuzioneVarPer == null) {
					margineContribuzioneVarPer = "0";
				}
				String margineContribuzioneVarAbs = req.getParameter("MC_variationAbsolute");
				if (margineContribuzioneVarAbs == null) {
					margineContribuzioneVarAbs = "0";
				}

				// ----------------------

				// 2- Insert values on database
				persistValues(businessUnitCode, businessAreaCode, cdc, year, closureCode, userId, ricavi, primoMargine, margineContribuzione, ricaviVarPer,
						ricaviVarAbs, primoMargineVarPer, primoMargineVarAbs, margineContribuzioneVarPer, margineContribuzioneVarAbs);

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

	private void persistValues(String businessUnitCode, String businessAreaCode, String cdc, String year, String closureCode, String userId, String ricavi,
			String primoMargine, String margineContribuzione, String ricaviVarPer, String ricaviVarAbs, String primoMargineVarPer, String primoMargineVarAbs,
			String margineContribuzioneVarPer, String margineContribuzioneVarAbs) {

		try {

			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel("DWH BIENG");
			Connection connection = dataSource.getConnection();
			String insertQuery = "INSERT INTO SUPP_REP02_T8 (BUSINESS_UNIT, BUSINESS_AREA, CDC, YEAR, CLOSURE, INSERT_USER, FC_RICAVI, FC_PM, FC_MDC, "
					+ "VAR_PERC_RICAVI,VAR_ABS_RICAVI,VAR_PERC_PM,VAR_ABS_PM,VAR_PERC_MDC,VAR_ABS_MDC ) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

			preparedStatement.setString(1, businessUnitCode);
			preparedStatement.setString(2, businessAreaCode);
			preparedStatement.setString(3, cdc);
			preparedStatement.setInt(4, Integer.valueOf(year));
			preparedStatement.setInt(5, Integer.valueOf(closureCode));
			preparedStatement.setString(6, userId);
			preparedStatement.setDouble(7, Double.valueOf(ricavi));
			preparedStatement.setDouble(8, Double.valueOf(primoMargine));
			preparedStatement.setDouble(9, Double.valueOf(margineContribuzione));

			preparedStatement.setDouble(10, Double.valueOf(ricaviVarPer));
			preparedStatement.setDouble(11, Double.valueOf(ricaviVarAbs));
			preparedStatement.setDouble(12, Double.valueOf(primoMargineVarPer));
			preparedStatement.setDouble(13, Double.valueOf(primoMargineVarAbs));
			preparedStatement.setDouble(14, Double.valueOf(margineContribuzioneVarPer));
			preparedStatement.setDouble(15, Double.valueOf(margineContribuzioneVarAbs));

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
