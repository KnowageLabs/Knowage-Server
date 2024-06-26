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
package it.eng.spagobi.engines.whatif.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.whatif.WhatIfEngine;
import it.eng.spagobi.engines.whatif.WhatIfEngineAnalysisState;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmFactory;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.DefaultWeightedAllocationAlgorithm;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;
import it.eng.spagobi.engines.whatif.parser.Lexer;
import it.eng.spagobi.engines.whatif.parser.parser;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplateParseException;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/olap/startwhatif")
public class EditExcelStartAction extends WhatIfEngineAbstractStartAction {

	// INPUT PARAMETERS
	public static final String LANGUAGE = "SBI_LANGUAGE";
	public static final String COUNTRY = "SBI_COUNTRY";
	private String userId = null;
	private String tenant = null;
	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	// OUTPUT PARAMETERS

	// SESSION PARAMETRES
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	public static final String STARTUP_ERROR = EngineConstants.STARTUP_ERROR;

	// Defaults
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(WhatIfEngineStartAction.class);

	@POST
	@Path("/editxls")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String test(@javax.ws.rs.core.Context HttpServletRequest req, @QueryParam("user_id") String userId, @QueryParam("tenant") String tenant) {
		WhatIfEngineInstance whatIfEngineInstance = null;
		String body = null;
		JSONObject jo = null;
		String mdx = null;
		String algorithm = null;
		String editCubeName = null;
		Integer ordinal = null;
		String expression = null;
		JSONArray newValues = null;
		this.userId = userId;
		this.tenant = tenant;

		try {
			body = RestUtilities.readBodyXSSUnsafe(req);
			jo = new JSONObject(body);
			newValues = jo.getJSONArray("newValues");
			mdx = jo.getString("mdx");

			editCubeName = jo.getString("editCubeName");
		} catch (JSONException e2) {
			logger.error("Cant read JSON", e2);
		} catch (IOException e) {
			logger.error("Cant get JSON", e);
		}

		logger.debug("IN");

		logger.debug("Creating engine instance ...");

		try {

			whatIfEngineInstance = WhatIfEngine.createInstance(mdx, getEnv(), editCubeName);

		} catch (WhatIfTemplateParseException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Template not valid", e);
			engineException.setDescription(e.getCause().getMessage());
			engineException.addHint("Check the document's template");
			throw engineException;
		} catch (Exception e) {
			logger.error("Error starting the What-If engine: error while generating the engine instance.", e);
			throw new SpagoBIEngineRuntimeException("Error starting the What-If engine: error while generating the engine instance.", e);
		}
		logger.debug("Engine instance succesfully created");

		// loads subobjects
		whatIfEngineInstance.setAnalysisMetadata(getAnalysisMetadata());
		if (getAnalysisStateRowData() != null) {
			logger.debug("Loading subobject [" + whatIfEngineInstance.getAnalysisMetadata().getName() + "] ...");
			try {
				WhatIfEngineAnalysisState analysisState = new WhatIfEngineAnalysisState();
				analysisState.load(getAnalysisStateRowData());
				whatIfEngineInstance.setAnalysisState(analysisState);
			} catch (Throwable t) {
				logger.error("Error loading the subobject", t);
				throw new SpagoBIRestServiceException("sbi.olap.start.load.subobject.error", getLocale(), "Error loading the subobject", t);
			}
			logger.debug("Subobject [" + whatIfEngineInstance.getAnalysisMetadata().getName() + "] succesfully loaded");
		}

		SpagoBIPivotModel model = (SpagoBIPivotModel) whatIfEngineInstance.getPivotModel();

		model.setMdx(mdx);
		model.initialize();
		for (int i = 0; i < newValues.length(); i++) {
			try {
				JSONObject newValue = newValues.getJSONObject(i);
				ordinal = newValue.getInt("ordinal");
				expression = newValue.getString("expression");
				algorithm = newValue.getString("algorithm");
				whatIfEngineInstance.setAlgorithmInUse(algorithm);
				setValue(ordinal, expression, model, whatIfEngineInstance);
			} catch (JSONException e) {
				logger.error("Cant read JSON", e);
			}
		}

		model.refresh();

		int a = model.getCellSet().getAxes().get(0).getPositionCount();
		int b = model.getCellSet().getAxes().get(1).getPositionCount();

		int c = a * b;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < c; i++) {

			sb.append(String.valueOf(model.getCellSet().getCell(i).getValue()));
			sb.append(';');

		}

		return sb.toString();

	}

	private void setValue(Integer ordinal, String expression, PivotModel model, WhatIfEngineInstance ei) {
		logger.debug("IN : ordinal = [" + ordinal + "]");

		// check if a version has been selected in the cube
		// ((SpagoBIPivotModel)
		// ei.getPivotModel()).getActualVersionSlicer(ei.getModelConfig());

		logger.debug("expression = [" + expression + "]");
		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) model.getCellSet();
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(ordinal);
		OlapDataSource olapDataSource = ei.getOlapDataSource();

		Double value = null;
		try {
			Lexer lex = new Lexer(new java.io.StringReader(expression));
			parser par = new parser(lex);
			par.setWhatIfInfo(cellWrapper, model, olapDataSource, ei);
			value = (Double) par.parse().value;
		} catch (Exception e) {
			logger.debug("Error parsing What-if metalanguage expression", e);
			String errorMessage = e.getMessage().replace(": Couldn't repair and continue parse", "");
			throw new SpagoBIEngineRestServiceRuntimeException(errorMessage, this.getLocale(), e);
		}

		String algorithm = ei.getAlgorithmInUse();
		logger.debug("Resolving the allocation algorithm");
		logger.debug("The class of the algorithm is [" + algorithm + "]");
		IAllocationAlgorithm allocationAlgorithm;

		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(DefaultWeightedAllocationAlgorithm.ENGINEINSTANCE_PROPERTY, ei);
			allocationAlgorithm = AllocationAlgorithmFactory.getAllocationAlgorithm(algorithm, ei, properties);
		} catch (SpagoBIEngineException e) {
			logger.error(e);
			throw new SpagoBIEngineRestServiceRuntimeException("sbi.olap.writeback.algorithm.definition.error", getLocale(), e);
		}

		CellTransformation transformation = new CellTransformation(value, cellWrapper.getValue(), cellWrapper, allocationAlgorithm);
		cellSetWrapper.applyTranformation(transformation);

		logger.debug("OUT");

	}

	@Override
	public Locale getLocale() {
		logger.debug("IN");
		Locale toReturn = null;
		try {
			String language = this.getServletRequest().getParameter(LANGUAGE);
			String country = this.getServletRequest().getParameter(COUNTRY);

			if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(country)) {
				Builder builder = new Builder().setLanguage(language).setRegion(country);
				String script = (String) getEnv().get("SBI_SCRIPT");
				if (org.apache.commons.lang.StringUtils.isNotBlank(script)) {
					builder.setScript(script);
				}
				toReturn = builder.build();
			} else {
				logger.warn("Language and country not specified in request. Considering default locale that is " + DEFAULT_LOCALE.toString());
				toReturn = DEFAULT_LOCALE;
			}
		} catch (Exception e) {
			logger.error("An error occurred while retrieving locale from request, using default locale that is " + DEFAULT_LOCALE.toString(), e);
			toReturn = DEFAULT_LOCALE;
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public String getEngineName() {
		return WhatIfConstants.ENGINE_NAME;
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return request;
	}

	@Override
	public UserProfile getUserProfile() {
		if (userId != null) {
			return new UserProfile(userId, tenant);
		}
		return super.getUserProfile();

	}

}