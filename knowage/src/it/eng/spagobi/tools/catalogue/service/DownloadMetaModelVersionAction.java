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
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.mime.MimeUtils;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Zerbetto Davide
 */
public class DownloadMetaModelVersionAction extends AbstractSpagoBIAction {

	public static final String VERSION_ID = "id";

	// logger component
	private static Logger logger = Logger.getLogger(DownloadMetaModelVersionAction.class);

	@Override
	public void doService() {
		logger.debug("IN");

		try {
			IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
			dao.setUserProfile(this.getUserProfile());

			Integer id = this.getAttributeAsInteger(VERSION_ID);
			Content content = dao.loadMetaModelContentById(id);
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
