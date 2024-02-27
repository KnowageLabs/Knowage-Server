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
package it.eng.spagobi.engines.qbe.services.core;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.PathInspector;
import it.eng.qbe.statement.graph.bean.ModelObjectI18n;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.serializer.ModelObjectInternationalizedSerializer;
import it.eng.qbe.statement.graph.serializer.RelationJSONSerializer;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

public class GetAmbiguousFieldsAction extends AbstractQbeEngineAction {

	private static final long serialVersionUID = -3367673458706691427L;

	// INPUT PARAMETERS
	public static final String QUERY_ID = "id";
	public static final String CATALOGUE = "catalogue";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(GetAmbiguousFieldsAction.class);

	@Override
	public void service(SourceBean request, SourceBean response) {
		Query query = null;

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		try {

			super.service(request, response);

			totalTimeMonitor = MonitorFactory.start("QbeEngine.getAmbiguousFieldsAction.totalTime");

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName()
					+ " service before having properly created an instance of EngineInstance class");

			query = getQuery();

			String modelName = getDataSource().getConfiguration().getModelName();

			Map<IModelField, Set<IQueryField>> modelFieldsMap = query.getQueryFields(getDataSource());
			Set<IModelField> modelFields = modelFieldsMap.keySet();

			Assert.assertNotNull(modelFields, "No field specified in teh query");
			Set<ModelFieldPaths> ambiguousModelField = new HashSet<>();
			if (modelFields != null) {
				Set<IModelEntity> modelEntities = getQueryEntities(modelFields);
				Graph<IModelEntity, Relationship> graph = getDataSource().getModelStructure()
						.getRootEntitiesGraph(modelName, false).getRootEntitiesGraph();

				PathInspector pathInspector = new PathInspector(graph, modelEntities);
				Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> ambiguousMap = pathInspector
						.getAmbiguousEntitiesAllPathsMap();

				Iterator<IModelField> modelFieldsIter = modelFields.iterator();

				while (modelFieldsIter.hasNext()) {
					IModelField iModelField = modelFieldsIter.next();
					IModelEntity me = iModelField.getParent();
					Set<GraphPath<IModelEntity, Relationship>> paths = ambiguousMap.get(me);
					if (paths != null) {
						Set<IQueryField> queryFields = modelFieldsMap.get(iModelField);
						if (queryFields != null) {
							Iterator<IQueryField> queryFieldsIter = queryFields.iterator();
							while (queryFieldsIter.hasNext()) {
								ambiguousModelField
										.add(new ModelFieldPaths(queryFieldsIter.next(), iModelField, paths));
							}
						}
					}
				}

				GraphManager.getDefaultCoverGraphInstance(QbeEngineConfig.getInstance().getDefaultCoverImpl())
						.applyDefault(ambiguousModelField, graph, modelEntities);
			}

			ObjectMapper mapper = new ObjectMapper();
			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
			simpleModule.addSerializer(Relationship.class, new RelationJSONSerializer(getDataSource(), getLocale()));
			simpleModule.addSerializer(ModelObjectI18n.class,
					new ModelObjectInternationalizedSerializer(getDataSource(), getLocale()));

			mapper.registerModule(simpleModule);
			String serialized = mapper.writeValueAsString(ambiguousModelField);

			Assert.assertNotNull(query, "Query object not specified");
			if (getEngineInstance().getActiveQuery() == null
					|| !getEngineInstance().getActiveQuery().getId().equals(query.getId())) {
				logger.debug("Query with id [" + query.getId()
						+ "] is not the current active query. A new statment will be generated");
				getEngineInstance().setActiveQuery(query);
			}

			try {
				writeBackToClient(serialized);
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Throwable t) {
			errorHitsMonitor = MonitorFactory.start("QbeEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(),
					getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null)
				totalTimeMonitor.stop();
			logger.debug("OUT");
		}
	}

	/**
	 * Get the query to be evaluated
	 *
	 * @return
	 */
	@Override
	public Query getQuery() {
		String jsonEncodedCatalogue = getAttributeAsString(CATALOGUE);
		logger.debug(CATALOGUE + " = [" + jsonEncodedCatalogue + "]");
		if (StringUtils.isNotEmpty(jsonEncodedCatalogue)) {
			try {
				JSONArray queries = new JSONArray(jsonEncodedCatalogue);
				for (int i = 0; i < queries.length(); i++) {
					JSONObject queryJSON = queries.getJSONObject(i);
					Query query = deserializeQuery(queryJSON);
					getEngineInstance().getQueryCatalogue().addQuery(query);
					getEngineInstance().resetActiveQuery();
				}
			} catch (Exception e) {
				throw new SpagoBIEngineServiceException(getActionName(), "Cannot deserialize catalogue", e);
			}
		}
		String queryId = getAttributeAsString(QUERY_ID);
		logger.debug("Parameter [" + QUERY_ID + "] is equals to [" + queryId + "]");
		Query query = getEngineInstance().getQueryCatalogue().getQuery(queryId);
		return query;
	}

	private Query deserializeQuery(JSONObject queryJSON) throws SerializationException, JSONException {
		return SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON.toString(),
				getEngineInstance().getDataSource());
	}

	private Set<IModelEntity> getQueryEntities(Set<IModelField> mf) {
		Set<IModelEntity> me = new HashSet<>();
		Iterator<IModelField> mfi = mf.iterator();
		while (mfi.hasNext()) {
			IModelField iModelField = mfi.next();
			me.add(iModelField.getParent());

		}
		return me;
	}

}
