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
package it.eng.spagobi.services.artifact.service;

import java.util.List;

import javax.activation.DataHandler;

import org.apache.axis.attachments.ManagedMemoryDataSource;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.artifact.bo.SpagoBIArtifact;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;

public class ArtifactServiceImplSupplier {
    static private Logger logger = Logger.getLogger(ArtifactServiceImplSupplier.class);

    /**
	 * return the artifact by name and type
	 * @param token. The token.
	 * @param user. The user.
	 * @param name. The artifact's name.
	 * @param type. The artifact's type.
	 * @return the content of the artifact.
	 */
    public DataHandler getArtifactContentByNameAndType(String name, String type) {
		logger.debug("IN.name:" + name);
		logger.debug("IN.type:" + type);
		DataHandler toReturn;

		if (name == null || type == null)
		    return null;

		// gets artifact content from database
		try {
			IArtifactsDAO artdao = DAOFactory.getArtifactsDAO();
			Artifact artifact = artdao.loadArtifactByNameAndType(name, type);
			Content content = artdao.loadArtifactContentById(Integer.valueOf(artifact.getId()));
			byte[] cont = content.getContent();
			ManagedMemoryDataSource mods =  new ManagedMemoryDataSource(new java.io.ByteArrayInputStream(cont), Integer.MAX_VALUE - 2,
					null, true);
			toReturn = new DataHandler(mods);
			return toReturn;
		} catch (Exception e) {
		    logger.error("The artifact is not correctly returned", e);
		    return null;
	    } finally {
			logger.debug("OUT");
		}
    }

    /**
	 * return the artifact by the id
	 * @param token. The token.
	 * @param user. The user.
	 * @param id. The artifact's id.
	 * @return the content of the artifact.
	 */
    public DataHandler getArtifactContentById(Integer id){
    	logger.debug("IN.id:" + id);
    	DataHandler toReturn;

    	if (id == null)
    	    return null;

    	// gets artifact content from database
		try {
			IArtifactsDAO artdao = DAOFactory.getArtifactsDAO();
			Content content = artdao.loadArtifactContentById(id);
			byte[] cont = content.getContent();
			ManagedMemoryDataSource mods =  new ManagedMemoryDataSource(new java.io.ByteArrayInputStream(cont), Integer.MAX_VALUE - 2,
			null, true);
			toReturn = new DataHandler(mods);
			return toReturn;
		} catch (Exception e) {
			logger.error("The artifact is not correctly returned", e);
			return null;
		} finally {
			logger.debug("OUT");
		}
    }


	/**
	 * return the artifacts list of the given type
	 * @param token. The token.
	 * @param user. The user.
	 * @param type. The artifact's type.
	 * @return the list of the artifacts of the given type.
	 */
	public SpagoBIArtifact[] getArtifactsByType(String type) {
    	logger.debug("IN.type:" + type);
    	SpagoBIArtifact[] toReturn = null;
    	// gets artifacts from database
		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			List<Artifact> list = artifactDAO.loadAllArtifacts(type);

		    if (list == null || list.isEmpty()) {
		    	logger.warn("There are no artifacts of type [" + type + "] defined on the database.");
		    	return new SpagoBIArtifact[0];
		    }
		    toReturn = new SpagoBIArtifact[list.size()];
		    for (int i = 0; i < list.size(); i++) {
		    	Artifact artifact = list.get(i);
				toReturn[i] = new SpagoBIArtifact(artifact.getId(),
						artifact.getName(), artifact.getType(),
						artifact.getDescription(),
						artifact.getCurrentContentId());
		    }
			return toReturn;
		} catch (Exception e) {
			logger.error("Error while loading artifacts list of type [" + type + "]", e);
			return null;
		} finally {
			logger.debug("OUT");
		}
	}
}
