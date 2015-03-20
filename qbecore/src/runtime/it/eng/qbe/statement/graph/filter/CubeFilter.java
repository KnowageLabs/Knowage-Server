/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph.filter;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * A path filter that remove all the paths containing cubes not involved in the query (for example a path that pass for a cube where no field is selected in the query) 
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class CubeFilter implements IPathsFilter{
	
	public static transient Logger logger = Logger.getLogger(CubeFilter.class);
	public static String PROPERTY_ENTITIES = "PROPERTY_ENTITIES";
	public static String PROPERTY_MODEL_STRUCTURE = "PROPERTY_ROOT_GRAPH";

	public void filterPaths(Set<ModelFieldPaths> paths,	Map<String, Object> properties) {
		
		Set<IModelEntity> modelEntities = (Set<IModelEntity>)properties.get(PROPERTY_ENTITIES);
		Assert.assertNotNull(modelEntities, "For the cube validator need a list of entities");
		
		IModelStructure modelStructure = (IModelStructure)properties.get(PROPERTY_MODEL_STRUCTURE);
		Assert.assertNotNull(modelStructure, "For the cube validator need the model structure");
		
		//Get all the cubes of the model
		List<String> modelEntityCubesList = new ArrayList<String>();
		Iterator<IModelEntity> entitiesIter = modelEntities.iterator();
		while (entitiesIter.hasNext()) {
			IModelEntity iModelEntity = (IModelEntity) entitiesIter.next();
			String type = (String) iModelEntity.getProperty("type");
			if ("cube".equalsIgnoreCase( type )) {
				modelEntityCubesList.add(iModelEntity.getUniqueName());
			}
		}
		
		logger.debug("Filtering the paths");
		Iterator<ModelFieldPaths> pathIter = paths.iterator();
		while (pathIter.hasNext()) {
			ModelFieldPaths modelFieldPaths = (ModelFieldPaths) pathIter.next();
			Set<PathChoice> choices = modelFieldPaths.getChoices();
			Set<PathChoice> filteredChoices = new HashSet<PathChoice>();
			if(choices!=null){
				Iterator<PathChoice> choicesIter = choices.iterator();
				while (choicesIter.hasNext()) {
					PathChoice pathChoice = (PathChoice) choicesIter.next();
					List<Relationship> relations = pathChoice.getRelations();
					boolean choiceToFilter=false;
					for(int i=0; i<relations.size(); i++){
						Relationship r = relations.get(i);
						IModelEntity source = r.getSourceEntity();
						IModelEntity target = r.getTargetEntity();
						if(isAnotherCube(modelStructure, source.getUniqueName(), modelEntityCubesList) || isAnotherCube(modelStructure, target.getUniqueName(), modelEntityCubesList)){
							choiceToFilter = true;
							logger.debug("Filtered a path where a node is a cube ["+source.getName()+" or "+target.getName()+"]");
						}
					}
					if(!choiceToFilter){
						filteredChoices.add(pathChoice);
					}
				}
			}
			modelFieldPaths.setChoices(filteredChoices);
		}
		logger.debug("Finish to filter paths");
	}

		
	private boolean isAnotherCube(IModelStructure modelStructure, String entityUniqueName, List<String> modelEntityCubesList){
		IModelEntity me = modelStructure.getEntity(entityUniqueName);
		String type = (String) me.getProperty("type");
		if ("cube".equalsIgnoreCase( type )) {
			return !modelEntityCubesList.contains(entityUniqueName);
		}
		
		return false;
	}
	
	
	
}
