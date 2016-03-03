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
package it.eng.spagobi.services.metamodel;

import javax.activation.DataHandler;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface MetamodelService {
	/**
	 * Returns the content of specified metamodel
	 * 
	 * @param token. The token.
	 * @param user. The user.
	 * @param id. The metamodel's name.
	 * @return the content of specified metamodel
	 */
	DataHandler getMetamodelContentByName(String token, String user, String name);
	
	/**
	 * Returns the last modification date of the metamodel specified
	 * 
	 * @param token The token.
	 * @param user The user.
	 * @param name  The metamodel's name.
	 * 
	 * @return the last modification date of the metamodel specified
	 */
	long getMetamodelContentLastModified(String token, String user, String name);
}
