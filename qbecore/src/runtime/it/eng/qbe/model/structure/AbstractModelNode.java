/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractModelNode extends AbstractModelObject implements IModelNode {
	
	protected IModelStructure structure;
	protected IModelEntity parent;	
	protected String modelName;

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public IModelStructure getStructure() {
		return structure;
	}

	protected void setStructure(IModelStructure structure) {
		this.structure = structure;
	}
	
	public IModelEntity getParent() {
		return parent;
	}
	
	public void setParent(IModelEntity parent) {
		this.parent = parent;
	}
	
	/**
	 * Gets the parent of the node from the structure.
	 * The difference with getParent() is that if the parent 
	 * entity is a ModelView: getPathParent() returns the view. 
	 * getParent() returns the entity of the view that contains the node
	 */
	public IModelEntity getLogicalParent(){
		if(parent==null){
			return null;
		}
		String parentViewName = parent.getPropertyAsString("parentView");
		if(parentViewName != null) {
			return structure.getEntity(parentViewName);
		}else{
			return parent;
		}
	}
	
		
	protected List<ModelViewEntity> getParentViews(IModelEntity entity){
		List<ModelViewEntity> parentViews = new ArrayList<ModelViewEntity>();
		IModelEntity nextEntity;
		if(entity instanceof ModelViewEntity){
			parentViews.add((ModelViewEntity)entity);
			nextEntity = entity.getParent();
		}else{
			String parentViewName = entity.getPropertyAsString("parentView");
			if(parentViewName != null) {
				ModelViewEntity viewEntity = (ModelViewEntity)structure.getEntity(parentViewName);
				parentViews.add(viewEntity);
				nextEntity = viewEntity.getParent();
			}else{
				nextEntity = entity.getParent();
			}
		}
		if(nextEntity!=null){
			parentViews.addAll(getParentViews(nextEntity));
		}
		return parentViews;
	}
	
	
}
