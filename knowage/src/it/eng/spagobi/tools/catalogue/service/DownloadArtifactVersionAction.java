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
package it.eng.spagobi.tools.catalogue.service;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.mime.MimeUtils;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Zerbetto Davide
 */
public class DownloadArtifactVersionAction extends AbstractSpagoBIAction {
	
	public static final String VERSION_ID = "id";
	
	// logger component
	private static Logger logger = Logger.getLogger(DownloadArtifactVersionAction.class);
	
	public void doService() {
		logger.debug("IN");
		
		try {
			IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
			dao.setUserProfile(this.getUserProfile());
			
			Integer id = this.getAttributeAsInteger(VERSION_ID);
			Content content = dao.loadArtifactContentById(id);
			try {
				writeBackToClient(content.getContent(), null, false, content.getFileName(), MimeUtils.getMimeType(content.getFileName()));
			} catch (IOException e) {
				throw new SpagoBIServiceException(this.getActionName(), "Impossible to write back the responce to the client", e);
			}
			
		} finally {
			logger.debug("OUT");
		}
	}

}
