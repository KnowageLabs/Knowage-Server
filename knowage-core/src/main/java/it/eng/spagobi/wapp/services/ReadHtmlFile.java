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
package it.eng.spagobi.wapp.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.wapp.bo.Menu;

import java.io.FileInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class ReadHtmlFile extends AbstractHttpAction {

	static private Logger logger = Logger.getLogger(ReadHtmlFile.class);

	@Override
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception {
		logger.debug("IN");
		freezeHttpResponse();

		// Start writing log in the DB
		Session aSession = null;
		try {
			aSession = HibernateSessionManager.getCurrentSession();

			AuditLogUtilities.updateAudit(getHttpRequest(), UserUtilities.getUserProfile(), "HTML_MENU.OPEN_HTML_FILE", null, "OK");
		} catch (HibernateException he) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		// End writing log in the DB

		HttpServletResponse httpResp = getHttpResponse();
		httpResp.setContentType("text/html");
		ServletOutputStream out = httpResp.getOutputStream();

		String menuId = (String) serviceRequest.getAttribute("MENU_ID");
		logger.debug("menuId=" + menuId);
		if (menuId != null) {
			Menu menu = DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(menuId));
			String fileName = menu.getStaticPage();

			if (fileName == null) {
				logger.error("Menu with id = " + menu.getMenuId() + " has no file name specified");
				throw new Exception("Menu has no file name specified");
			}

			// check the validity of the fileName (it must not be a path)
			// TODO remove this control and write better this action, or remove the action at all
			if (fileName.contains("\\") || fileName.contains("/") || fileName.contains("..")) {
				logger.error("Menu with id = " + menu.getMenuId() + " has file name [" + fileName + "] containing file separator character!!!");
				throw new Exception("Menu file name cannot contain file separator character");
			}

			logger.debug("fileName=" + fileName);

			String filePath = SpagoBIUtilities.getResourcePath();
			filePath += "/static_menu/" + fileName;

			logger.debug("filePath=" + filePath);

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(filePath);
			} catch (Exception e) {
				logger.error("Could not open file " + filePath);
				throw new Exception("Could not open file");
			}

			int avalaible = fis.available(); // Mi informo sul num. bytes.

			for (int i = 0; i < avalaible; i++) {
				out.write(fis.read());
			}

			fis.close();
			out.flush();
			out.close();
			logger.debug("OUT");
		} else {
			logger.error("missing id");
			throw new Exception("missing id");
		}
	}

}
