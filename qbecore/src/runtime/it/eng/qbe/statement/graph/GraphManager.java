/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.cover.IDefaultCoverGraph;
import it.eng.qbe.statement.graph.cover.ShortestPathsCoverGraph;
import it.eng.qbe.statement.graph.filter.IPathsFilter;
import it.eng.qbe.statement.graph.validator.ConnectionValidator;
import it.eng.qbe.statement.graph.validator.IGraphValidator;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * Central manager of the graphs
 *
 */
public class GraphManager {
	
	private static IDefaultCoverGraph defaultCoverGraph;
	private static IGraphValidator validator;
	private static List<IPathsFilter> pathFilters;
	private static transient Logger logger = Logger.getLogger(GraphManager.class);
	
	public synchronized static IDefaultCoverGraph getDefaultCoverGraphInstance(String className) {
		
		try{
			if (defaultCoverGraph == null)
				defaultCoverGraph = (IDefaultCoverGraph)Class.forName(className).newInstance();
		}catch(Exception e) {
			logger.debug("Impossible to load cover graph instance. The IDefaultCoverGraph implementation should be defined in the qbe.xml. The property is QBE.GRAPH-PATH.defaultCoverImpl. Using the default one.",e);
			defaultCoverGraph = new ShortestPathsCoverGraph();
		}
		return defaultCoverGraph;
	}

	public synchronized static IGraphValidator getGraphValidatorInstance(String className) {
		
		try{
			if (validator == null)
				validator = (IGraphValidator)Class.forName(className).newInstance();
		}catch(Exception e) {
			logger.debug("Impossible to load validator instance. The IGraphValidator implementation should be defined in the qbe.xml. The property is QBE.GRAPH-PATH.graphValidatorImpl. Using the default one.",e);
			validator = new ConnectionValidator();
		}
		return validator;
	}
	
	public synchronized static List<IPathsFilter> getPathFilters(String classNames) {
		
		try{
			if (pathFilters == null){
				pathFilters = new ArrayList<IPathsFilter>();
				String[] classes = classNames.split(",");
				if(classes!=null){
					for(int i=0; i<classes.length; i++){
						pathFilters.add((IPathsFilter)Class.forName(classes[i]).newInstance());
					}
				}
			}
		}catch(Exception e) {
			logger.debug("Impossible to load filters. The IPathsFilter implementations should be defined in the qbe.xml. The property is QBE.GRAPH-PATH.pathsFiltersImpl.",e);
			throw new SpagoBIEngineRuntimeException("Impossible to load filters. The IPathsFilter implementations should be defined in the qbe.xml. The property is QBE.GRAPH-PATH.pathsFiltersImpl.", e);
		}
		return pathFilters;
	}
	
	public static void filterPaths(Set<ModelFieldPaths> paths, Map<String, Object> properties, String filterNames){
		if(filterNames!=null && filterNames.length()>0){
			List<IPathsFilter> filters = getPathFilters(filterNames); 
			if(filters!=null){
				for(int i=0; i<filters.size(); i++){
					filters.get(i).filterPaths(paths, properties);
				}
			}
		}
	}
	
	/**
	 * If the query graph contains only the selected entities return true
	 * @param modelEntities the collection of the entities involved in the query 
	 * @param queryGraph the query graph
	 * @return
	 */
	public static boolean isDirectlyExecutable(Collection<IModelEntity> modelEntities, QueryGraph queryGraph){
		if(modelEntities==null || queryGraph==null){
			return false;
		}
		if(queryGraph.vertexSet()==null || queryGraph.vertexSet().size()==0 ){
			return true;//the case when the user select only an entity
		}
		return queryGraph.vertexSet().size() == modelEntities.size();
	}
	
	
}
