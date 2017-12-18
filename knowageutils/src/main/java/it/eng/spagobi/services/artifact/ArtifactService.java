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
package it.eng.spagobi.services.artifact;

import it.eng.spagobi.services.artifact.bo.SpagoBIArtifact;

import javax.activation.DataHandler;



/**
 * This is the ArtifactService interfaces
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ArtifactService {

	/**
	 * return the artifact by name and type
	 * @param token. The token.
	 * @param user. The user.
	 * @param name. The artifact's name.
	 * @param type. The artifact's type.
	 * @return the content of the artifact.
	 */
	DataHandler getArtifactContentByNameAndType(String token,String user, String name, String type);  

	/**
	 * return the artifact by the id
	 * @param token. The token.
	 * @param user. The user.
	 * @param id. The artifact's id.
	 * @return the content of the artifact.
	 */
	DataHandler getArtifactContentById(String token, String user, Integer id);
	
	/**
	 * return the artifacts list of the given type
	 * @param token. The token.
	 * @param user. The user.
	 * @param type. The artifact's type.
	 * @return the list of the artifacts of the given type.
	 */
	SpagoBIArtifact[] getArtifactsByType(String token, String user, String type);

}
