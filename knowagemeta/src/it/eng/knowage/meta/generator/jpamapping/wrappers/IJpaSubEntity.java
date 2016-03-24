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
package it.eng.knowage.meta.generator.jpamapping.wrappers;

import it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaSubEntity;

import java.util.List;

/**
 * A unique reference to a table or view given its position in a 
 * relationship path
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IJpaSubEntity {
	
	/**
	 * @return the unique name of the specified entity
	 */
	String getUniqueName();
	
	/**
	 * @return the value of the specified entity attribute
	 */
	public String getAttribute(String name);
	
	/**
	 * @return the columns objects of the sub entity
	 */
	public List<IJpaColumn> getColumns();
	
	/**
	 * @return the columns names of the sub entity
	 */
	public List<String> getColumnNames();
	
	/**
	 * @return the unique columns names of the sub entity
	 */
	public List<String> getColumnUniqueNames();
	
	public JpaSubEntity getParent();
	
}
