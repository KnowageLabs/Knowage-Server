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
