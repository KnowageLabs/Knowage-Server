/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.modules;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;
import it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia
 *
 */
public class DossierRefreshModule extends AbstractModule {
	
	public static final String MODULE_PAGE = "DossierRefreshPage";	
	static private Logger logger = Logger.getLogger(DossierRefreshModule.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {}
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		try {
			String dossierIdStr = (String) request.getAttribute(DossierConstants.DOSSIER_ID);
			logger.debug("Using dossier id = " + dossierIdStr);
			Integer dossierId = new Integer(dossierIdStr);
			IDossierPresentationsDAO pdDAO = DAOFactory.getDossierPresentationDAO();
			List presVersions = pdDAO.getPresentationVersions(dossierId);
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierPresentationVersion");
			response.setAttribute(DossierConstants.DOSSIER_PRESENTATION_VERSIONS, presVersions);
			response.setAttribute(DossierConstants.DOSSIER_ID, dossierIdStr);
		} catch (Exception e) {
			logger.error("Error while setting response attribute " + e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
		}
	}
}
	
	
	