/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.AbstractSelectField;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.statement.graph.bean.ModelObjectI18n;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.GraphPath;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModelFieldPaths {

	private IModelField field;
	private IQueryField queryField;
	private Set<PathChoice> choices;



	public ModelFieldPaths(IQueryField queryField, IModelField field, Set<GraphPath<IModelEntity, Relationship>> paths ){
		this(field, queryField);

		choices = new HashSet<PathChoice>();
		if(paths!=null){
			Iterator<GraphPath<IModelEntity, Relationship>> pathsiter = paths.iterator();
			while (pathsiter.hasNext()) {
				choices.add(new PathChoice(pathsiter.next()));
			}
		}
	}

	public ModelFieldPaths(IQueryField queryField, IModelField field, Set<PathChoice> choices, boolean choicesBoolean){
		this(field, queryField);
		this.choices = choices;
	}
	
	private ModelFieldPaths(IModelField field, IQueryField queryField) {
		super();
		this.field = field;
		this.queryField = queryField;
	}
	
	@JsonIgnore
	public IModelEntity getModelEntity(){
		return field.getParent();
	}
	
	public ModelObjectI18n getEntity(){
		return new ModelObjectI18n (field.getParent());
	}

	public ModelObjectI18n getName(){
		return new ModelObjectI18n (field);
	}

	public String getId(){
		return field.getUniqueName();
	}

	public Set<PathChoice> getChoices() {
		return choices;
	}

	public void setChoices(Set<PathChoice> choices) {
		this.choices = choices;
	}

	public String getQueryFieldName() {
		return queryField.getName();
	}


	public String getQueryFieldAlias() {
		return queryField.getAlias();
	}

	public String getQueryFieldType() {
		if(queryField instanceof AbstractSelectField){
			return "select";
		}
		return "filter";
	}

}
