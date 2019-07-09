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
package it.eng.spagobi.services.security.service;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

/**
 * Factory class for the security supplier
 * 
 * @author Bernabei Angelo
 *
 */
public class SecurityServiceSupplierFactory {
	
	/**
	 * Decorate instance of {@link SecurityServiceSupplierFactory} to add functionalites to authentication.
	 * 
	 * @author Marco Libanori
	 */
	private static class _SecurityServiceSupplierDecorator implements ISecurityServiceSupplier {
		
		final private ISecurityServiceSupplier instance;

		public _SecurityServiceSupplierDecorator(ISecurityServiceSupplier instance) {
			super();
			this.instance = instance;
		}

		@Override
		public SpagoBIUserProfile createUserProfile(String userId) {
			return instance.createUserProfile(userId);
		}

		@Override
		public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
			return instance.checkAuthentication(userId, psw);
		}

		@Override
		public SpagoBIUserProfile checkAuthenticationToken(String token) {
			return instance.checkAuthenticationToken(token);
		}

		@Override
		public SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token) {
			return instance.checkAuthenticationWithToken(userId, token);
		}

		@Override
		public boolean checkAuthorization(String userId, String function) {
			return instance.checkAuthorization(userId, function);
		}
		
	}
	
	/**
	 * Check user failed login counter before authentication.
	 * 
	 * @author Marco Libanori
	 */
	private static class TooMuchFailedLoginAttemtpsDecorator extends _SecurityServiceSupplierDecorator {
		
		public TooMuchFailedLoginAttemtpsDecorator(ISecurityServiceSupplier instance) {
			super(instance);
		}
		
		private boolean isLoginAttemtpsCounterBelowLimit(String userId) {
			String configValue =
					SingletonConfig.getInstance().getConfigValue("internal.security.login.maxFailedLoginAttempts");
			final int maxFailedLogin = Integer.parseInt(configValue);
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			int failedLoginAttempts = userDao.getFailedLoginAttempts(userId);
			return failedLoginAttempts < maxFailedLogin;
		}

		@Override
		public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
			SpagoBIUserProfile userProfile = null;
			if (isLoginAttemtpsCounterBelowLimit(userId)) {
				ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
				userProfile = super.checkAuthentication(userId, psw);
				if (userProfile != null) {
					userDao.resetFailedLoginAttempts(userId);
				} else {
					userDao.incrementFailedLoginAttempts(userId);
				}
			}
			return userProfile;
		}

		@Override
		public SpagoBIUserProfile checkAuthenticationToken(String token) {
			// TODO
			SpagoBIUserProfile checkAuthenticationToken = super.checkAuthenticationToken(token);
			return checkAuthenticationToken;
		}

		@Override
		public SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token) {
			SpagoBIUserProfile userProfile = null;
			if (isLoginAttemtpsCounterBelowLimit(userId)) {
				ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
				userProfile = super.checkAuthenticationWithToken(userId, token);
				if (userProfile != null) {
					userDao.resetFailedLoginAttempts(userId);
				} else {
					userDao.incrementFailedLoginAttempts(userId);
				}
			}
			return userProfile;
		}

		@Override
		public boolean checkAuthorization(String userId, String function) {
			boolean authorized = false;
			if (isLoginAttemtpsCounterBelowLimit(userId)) {
				authorized = super.checkAuthorization(userId, function);
			}
			return authorized;
		}
		
	}

	static Logger logger = Logger.getLogger(SecurityServiceSupplierFactory.class);

	/**
	 * Creates a new SecurityServiceSupplier object.
	 * 
	 * @return the i security service supplier
	 */
	public static ISecurityServiceSupplier createISecurityServiceSupplier() {
		logger.debug("IN");
		SingletonConfig configSingleton = SingletonConfig.getInstance();
		String engUserProfileFactorySB = configSingleton.getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
		if (engUserProfileFactorySB == null) {
			logger.warn("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS ... NOT FOUND");
		}
		String engUserProfileFactoryClass = engUserProfileFactorySB;
		engUserProfileFactoryClass = engUserProfileFactoryClass.trim();
		try {
			String configValue =
					SingletonConfig.getInstance().getConfigValue("internal.security.login.checkForMaxFailedLoginAttempts");
			Boolean enableFailedLoginAttemptsFilter = Boolean.parseBoolean(configValue);
			
			Class<?> clazz = Class.forName(engUserProfileFactoryClass);
			ISecurityServiceSupplier newInstance = (ISecurityServiceSupplier) clazz.newInstance();
			if (enableFailedLoginAttemptsFilter) {
				newInstance = new TooMuchFailedLoginAttemtpsDecorator(newInstance);
			}
			return newInstance;
		} catch (InstantiationException e) {
			logger.warn("InstantiationException", e);
		} catch (IllegalAccessException e) {
			logger.warn("IllegalAccessException", e);
		} catch (ClassNotFoundException e) {
			logger.warn("ClassNotFoundException", e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

}
