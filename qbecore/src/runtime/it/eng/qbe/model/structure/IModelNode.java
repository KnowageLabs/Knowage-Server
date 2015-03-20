/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import java.util.List;


/**
 * All nodes of a IModelStructure (i.e. entities & fields) implement this interface 
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface IModelNode extends IModelObject{
	
	String getModelName();
	void setModelName(String modelName);
	IModelStructure getStructure();
	IModelEntity getParent();
	/**
	 * Get the list of view in the path
	 * from the root to the node
	 * @return
	 */
	List<ModelViewEntity> getParentViews();
	/**
	 * Gets the parent of the node from the structure.
	 * The difference with getParent() is that if the parent 
	 * entity is a ModelView getLogicalParent() returns the view while 
	 * getParent() returns the entity of the view that contains the node
	 */
	IModelEntity getLogicalParent();
	void setParent(IModelEntity parent);
	String getUniqueName();

}
