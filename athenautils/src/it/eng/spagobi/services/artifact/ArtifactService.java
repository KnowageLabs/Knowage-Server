/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
