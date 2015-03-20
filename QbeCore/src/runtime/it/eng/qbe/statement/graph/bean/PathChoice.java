/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph.bean;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.statement.graph.GraphUtilities;

import java.util.List;

import org.jgrapht.GraphPath;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * A choice is a Path with some utilities method
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class PathChoice {
	GraphPath<IModelEntity, Relationship> path;
	private boolean active;
	private List<Relationship> relations;
	
	public PathChoice( List<Relationship> relations, boolean active) {
		super();
		this.relations = relations;
		this.active = active;
	}

	public PathChoice(GraphPath<IModelEntity, Relationship> path) {
		super();
		this.path = path;
		this.active =  false;
	}
	
	public String getStart(){
		return path.getStartVertex().getUniqueName();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEnd(){
		return path.getEndVertex().getUniqueName();
	}
	
	public List<Relationship> getNodes() {
		if(path!=null){
			return (path.getEdgeList());
		}
		return null;
	}

	@JsonIgnore
	public  List<Relationship> getRelations() {
		if(this.relations==null){
			this.relations = this.path.getEdgeList();
		}
		return relations;
	}

	@JsonIgnore
	public boolean isTheSamePath(GraphPath<IModelEntity, Relationship> path2){
		if(path == null){
			return false;
		}
		return GraphUtilities.arePathsEquals(path, path2);
	}	
	
}
