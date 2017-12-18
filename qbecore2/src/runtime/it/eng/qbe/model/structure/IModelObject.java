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

import java.util.Map;

/**
 * All objects in the model structure, even the <code>IModelStructure</code>, 
 * extend this interface
 * 
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IModelObject {
	
	
	long getId();
	String getName();
	void setName(String name);
	
	void setProperties(Map<String,Object> properties);
	
	Map<String,Object> getProperties();
	Object getProperty(String name);
	String getPropertyAsString(String name);
	boolean getPropertyAsBoolean(String name);
	int getPropertyAsInt(String name);

}
