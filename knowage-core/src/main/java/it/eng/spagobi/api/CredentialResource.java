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
package it.eng.spagobi.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.message.MessageBundle;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.validation.PasswordChecker;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.PublicService;

/**
 * Bean of the data needed to change the password of an user.
 *
 * @author Marco Libanori
 */
class ChangePasswordData {
	@NotNull
	@Size(max = 150)
	private String newPassword;
	@NotNull
	@Size(max = 150)
	private String newPasswordConfirm;
	@NotNull
	@Size(max = 150)
	private String oldPassword;
	@NotNull
	@Size(max = 100)
	private String userId;

	public String getNewPassword() {
		return newPassword;
	}

	public String getNewPasswordConfirm() {
		return newPasswordConfirm;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public String getUserId() {
		return userId;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public void setNewPasswordConfirm(String newPasswordConfirm) {
		this.newPasswordConfirm = newPasswordConfirm;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}

/**
 * Credential resource controller.
 *
 * Manage login and password change procedure to an unauthenticated user.
 *
 * @author Marco Libanori
 */
@Path("/credential")
public class CredentialResource {

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final Logger logger = Logger.getLogger(CredentialResource.class);

	/**
	 * Change password of an user.
	 *
	 * @param data Data needed to change the password
	 * @return HTTP response
	 */
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@PublicService
	public Response change(final ChangePasswordData data) {

		Response response = Response.ok().entity(MessageBundle.getMessage("changePwd.pwdChanged")).build();

		final String userId = data.getUserId();
		final String oldPassword = data.getOldPassword();
		final String newPassword = data.getNewPassword();
		final String newPasswordConfirm = data.getNewPasswordConfirm();

		if (StringUtils.isEmpty(userId)) {
			logger.error("Trying to change password with userId");
			response = Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser tmpUser = userDao.loadSbiUserByUserId(userId);

			try {
				if (PasswordChecker.getInstance().isValid(tmpUser, oldPassword, newPassword, newPasswordConfirm)) {
					// getting days number for calculate new expiration date
					IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
					List lstConfigChecks = configDao.loadConfigParametersByProperties(SpagoBIConstants.CHANGEPWD_EXPIRED_TIME);
					Date beginDate = new Date();
					if (lstConfigChecks.size() > 0) {
						Config check = (Config) lstConfigChecks.get(0);
						if (check.isActive()) {
							// define the new expired date
							SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
							Calendar cal = Calendar.getInstance();
							cal.set(beginDate.getYear() + 1900, beginDate.getMonth(), beginDate.getDate());
							// adds n days (getted from db)
							cal.add(Calendar.DATE, Integer.parseInt(check.getValueCheck()));
							try {
								Date endDate = StringUtilities.stringToDate(sdf.format(cal.getTime()), DATE_FORMAT);
								logger.debug("End Date for expiration calculeted: " + endDate);
								tmpUser.setDtPwdBegin(beginDate);
								tmpUser.setDtPwdEnd(endDate);
							} catch (Exception e) {
								logger.error("The control pwd goes on error: " + e);
								throw new EMFUserError(EMFErrorSeverity.ERROR, 14008, new Vector(), new HashMap());
							}
						}
					}
					tmpUser.setDtLastAccess(beginDate); // reset last access date
					tmpUser.setPassword(Password.encriptPassword(newPassword));// SHA encrypt
					tmpUser.setFlgPwdBlocked(false); // reset blocking flag
					userDao.updateSbiUser(tmpUser, tmpUser.getId());
					logger.debug("Updated properties for user with id " + tmpUser.getId() + " - DtLastAccess: " + tmpUser.getDtLastAccess().toString());
				}
			} catch (EMFUserError e) {
				logger.error("Error during retrieving of user " + userId, e);
				response = Response.status(Response.Status.NOT_FOUND).entity(e.getDescription()).build();
			} catch (Exception e) {
				logger.error("Error during password change", e);
				response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

		}

		return response;

	}

}
