/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
