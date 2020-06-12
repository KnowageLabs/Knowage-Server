package it.eng.knowage.menu.api;

import java.util.Collection;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.dao.IMenuDAO;

/**
 *
 * @author albnale
 * @since 2020/06/12
 */

public class MenuManagementAPI {

	private IRoleDAO roleDao = null;
	private IMenuDAO menuDao = null;
	private IEngUserProfile userProfile = null;

	public MenuManagementAPI(IEngUserProfile userProfile) {
		this.userProfile = userProfile;

		try {
			roleDao = DAOFactory.getRoleDAO();
			roleDao.setUserProfile(userProfile);

			menuDao = DAOFactory.getMenuDAO();
			menuDao.setUserProfile(userProfile);

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to instatiate DAO", t);
		}
	}

	/**
	 * Method used to check if the menu is allowed for at least one of the user's roles
	 *
	 * @param childElem
	 * @return
	 * @throws EMFUserError
	 * @throws EMFInternalError
	 */
	public boolean isAccessibleMenu(Menu childElem) throws EMFUserError, EMFInternalError {
		boolean allowed = false;
		try {
			Collection roles = userProfile.getRoles();
			for (Object object : roles) {
				String roleName = (String) object;

				roleDao = DAOFactory.getRoleDAO();
				Role role = roleDao.loadByName(roleName);
				if (role != null) {
					menuDao = DAOFactory.getMenuDAO();
					Menu childrenMenu = menuDao.loadMenuByID(childElem.getMenuId(), role.getId());

					if (childrenMenu != null) {
						allowed = true;
						break;
					}
				}
			}
		} catch (EMFUserError e) {
			throw new EMFUserError(e);
		} catch (EMFInternalError e) {
			throw new EMFInternalError(e);
		}
		return allowed;
	}

}
