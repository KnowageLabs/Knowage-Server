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
