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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.sort.SortCriteria;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.member.SbiMember;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.PivotObjectForRendering;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.serializer.SerializationException;
import it.eng.spagobi.engines.whatif.serializer.SerializationManager;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineRestService;
import it.eng.spagobi.utilities.engines.rest.ExecutionSession;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

public class AbstractWhatIfEngineService extends AbstractEngineRestService {

	private static final String OUTPUTFORMAT = "OUTPUTFORMAT";
	private static final String OUTPUTFORMAT_JSONHTML = "application/json";

	public static transient Logger logger = Logger.getLogger(AbstractWhatIfEngineService.class);

	@Context
	protected HttpServletRequest servletRequest;

	/**
	 * Renders the model and return the HTML table
	 *
	 * @param request
	 * @return the String that contains the HTML table
	 */
	public String renderModel(PivotModel model) {
		logger.debug("IN");

		String serializedModel = null;

		try {
			Monitor totalTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService.renderModel.total");
			SpagoBIPivotModel sbiModel = (SpagoBIPivotModel) model;
			Monitor applycalcTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService.renderModel.applyCalc");
			//ModelConfig modelConfig = getWhatIfEngineInstance().getModelConfig();
			// adds the calculated fields before rendering the model

			sbiModel.applyCal();
			applycalcTime.stop();

			Monitor serializeTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService.renderModel.serialize");
			serializedModel = serialize(new PivotObjectForRendering(getWhatIfEngineInstance().getOlapConnection(), sbiModel, getWhatIfEngineInstance()
					.getModelConfig()));
			serializeTime.stop();

			Monitor postTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService.renderModel.postActivities");
			SortCriteria sortCriteria = model.getSortCriteria();

			// restore the query without calculated fields
			sbiModel.restoreQuery();
			model.setSortCriteria(sortCriteria);
			postTime.stop();
			totalTime.stop();
		} catch (SerializationException e) {
			logger.error("Error serializing the pivot", e);
			throw new SpagoBIEngineRuntimeException("Error serializing the pivot", e);
		}

		logger.debug("OUT: table correctly serialized");
		return serializedModel;

	}

	@Override
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	/**
	 * Gets the what if engine instance.
	 *
	 * @return the console engine instance
	 */
	public WhatIfEngineInstance getWhatIfEngineInstance() {
		ExecutionSession es = getExecutionSession();

		return (WhatIfEngineInstance) es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);

	}

	public PivotModel getPivotModel() {
		return getWhatIfEngineInstance().getPivotModel();
	}

	public ModelConfig getModelConfig() {
		return getWhatIfEngineInstance().getModelConfig();
	}

	public String getOutputFormat() {
		String outputFormat = servletRequest.getParameter(OUTPUTFORMAT);

		if (outputFormat == null || outputFormat.equals("")) {
			logger.debug("the output format is null.. use the default one" + OUTPUTFORMAT_JSONHTML);
			outputFormat = OUTPUTFORMAT_JSONHTML;
		}

		return outputFormat;
	}

	public String serialize(Object obj) throws SerializationException {
		String outputFormat = getOutputFormat();
		return (String) SerializationManager.serialize(outputFormat, obj);
	}

	public Object deserialize(String obj, Class clazz) throws SerializationException {
		String outputFormat = getOutputFormat();
		return SerializationManager.deserialize(outputFormat, obj, clazz);
	}

	public Object deserialize(String obj, TypeReference object) throws SerializationException {
		String outputFormat = getOutputFormat();
		return SerializationManager.deserialize(outputFormat, obj, object);
	}

	public List<Member> getMembersFromBody() {
		logger.debug("Getting the members from the request");
		List<SbiMember> sbiMembers = null;
		List<Member> members = new ArrayList<Member>();
		String membersString = null;

		try {
			membersString = RestUtilities.readBodyXSSUnsafe(getServletRequest());
			TypeReference<List<SbiMember>> type = new TypeReference<List<SbiMember>>() {
			};
			sbiMembers = (List<SbiMember>) deserialize(membersString, type);
			for (int i = 0; i < sbiMembers.size(); i++) {
				members.add(sbiMembers.get(i).getMember(getPivotModel().getCube()));
			}
		} catch (Exception e) {
			logger.error("Error loading the members from the request ", e);
			throw new SpagoBIEngineRestServiceRuntimeException("generic.error.request.members.getting", getLocale(), e);
		}

		return members;
	}

	@Override
	public String getEngineName() {
		return WhatIfConstants.ENGINE_NAME;
	}

	protected void applyConfiguration(ModelConfig modelConfig, SpagoBIPivotModel model) {
		applySupperssEmptyConfiguration(modelConfig, model);
		applySortConfiguration(modelConfig, model);
	
	}

	private void applySortConfiguration(ModelConfig modelConfig, SpagoBIPivotModel model) {
		Boolean sortingEnabled = modelConfig.getSortingEnabled();
		String sortingPositionUniqeName = modelConfig.getSortingPositionUniqueName();
		int axisToSort = modelConfig.getAxisToSort();
		int axis = modelConfig.getAxis();
		String sortMode = modelConfig.getSortMode();
		if (shouldSort(sortingEnabled, sortingPositionUniqeName)) {
	
			model.sortModel(axisToSort, axis, sortingPositionUniqeName, sortMode);
	
		}
	
	}

	private void applySupperssEmptyConfiguration(ModelConfig modelConfig, SpagoBIPivotModel model) {
		Boolean suppressEmpty = modelConfig.getSuppressEmpty();
		model.setNonEmpty(suppressEmpty);
	}

	private boolean shouldSort(boolean sortingEnabled, String sortingPositionUniqeName) {
		return sortingEnabled && sortingPositionUniqeName != null;
	}

}
