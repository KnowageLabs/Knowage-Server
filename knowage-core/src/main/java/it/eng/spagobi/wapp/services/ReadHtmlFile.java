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

import java.io.FileInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import it.eng.knowage.menu.api.MenuManagementAPI;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.wapp.bo.Menu;

public class ReadHtmlFile extends AbstractHttpAction {

	private static final Logger LOGGER = Logger.getLogger(ReadHtmlFile.class);

	@Override
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception {
		LOGGER.debug("IN");
		freezeHttpResponse();

		IEngUserProfile profile = (IEngUserProfile) this.getHttpRequest().getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		// Start writing log in the DB
		Session aSession = null;
		try {
			aSession = HibernateSessionManager.getCurrentSession();
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "HTML_MENU.OPEN_HTML_FILE", null, "OK");
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
		LOGGER.debug("menuId=" + menuId);
		if (menuId != null) {
			Menu menu = DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(menuId));
			boolean accessible = new MenuManagementAPI(profile).isAccessibleMenu(menu);
			if (!accessible) {
				LOGGER.error("No role found for menu with id = " + menu.getMenuId() + ". Not allowed menu.");
				throw new Exception("No role found for menu with id = " + menu.getMenuId() + ". Not allowed menu.");
			}

			String fileName = menu.getStaticPage();

			if (fileName == null) {
				LOGGER.error("Menu with id = " + menu.getMenuId() + " has no file name specified");
				throw new Exception("Menu has no file name specified");
			}

			// check the validity of the fileName (it must not be a path)
			// TODO remove this control and write better this action, or remove the action at all
			if (fileName.contains("\\") || fileName.contains("/") || fileName.contains("..")) {
				LOGGER.error("Menu with id = " + menu.getMenuId() + " has file name [" + fileName + "] containing file separator character!!!");
				throw new Exception("Menu file name cannot contain file separator character");
			}

			LOGGER.debug("fileName=" + fileName);

			String filePath = SpagoBIUtilities.getResourcePath();
			filePath += "/static_menu/" + fileName;

			LOGGER.debug("filePath=" + filePath);

			try (FileInputStream fis = new FileInputStream(filePath)) {
				int avalaible = fis.available(); // Mi informo sul num. bytes.

				for (int i = 0; i < avalaible; i++) {
					out.write(fis.read());
				}

				out.flush();
			} catch (Exception e) {
				LOGGER.error("Could not open file " + filePath);
				throw new Exception("Could not open file");
			}

			LOGGER.debug("OUT");
		} else {
			LOGGER.error("missing id");
			throw new Exception("missing id");
		}
	}

}
