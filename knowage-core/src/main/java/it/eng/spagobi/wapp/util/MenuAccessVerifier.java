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
package it.eng.spagobi.wapp.util;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.wapp.bo.Menu;

public class MenuAccessVerifier {

	public static transient Logger logger = Logger.getLogger(MenuAccessVerifier.class);

	// if menu contains a document that is not visible to user do not click menu
	// called for a single menu
	public static boolean checkClickable(Menu menu, IEngUserProfile profile) {
		logger.debug("IN");
		boolean isClickable = true;

		Integer objId = menu.getObjId();
		if (objId != null) {
			try {
				IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
				dao.setUserProfile(profile);
				BIObject obj = dao.loadBIObjectById(objId);
				if (obj != null)
					isClickable = ObjectsAccessVerifier.canSee(obj, profile);
			} catch (Exception e) {
				logger.error("error in evaluating menu visibility by Object contained", e);
				return false;
			}
		}
		logger.debug("OUT");
		return isClickable;
	}

	public static boolean canView(Menu menu, IEngUserProfile profile) {
		logger.debug("IN");
		Role[] menuRoles = menu.getRoles();
		Collection profileRoles = null;

		try {
			profileRoles = ((UserProfile) profile).getRolesForUse();
		} catch (EMFInternalError e) {
			return false;
		}

		boolean found = false;
		for (Iterator iterator = profileRoles.iterator(); iterator.hasNext() && !found;) {
			String profileRole = (String) iterator.next();
			for (int i = 0; i < menuRoles.length && !found; i++) {
				Role menuRole = menuRoles[i];
				String menuRoleName = menuRole.getName();

				if (menuRoleName.equals(profileRole)) {
					found = true;
				}
			}

		}
		logger.debug("OUT");

		return found;

	}

}
