package it.eng.spagobi.commons.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.security.Password;

/**
 * Password checker singleton.
 */
public class PasswordChecker {
	private static Logger logger = Logger.getLogger(PasswordChecker.class);

	private static final String PROP_NODE = "changepwdmodule.";
	
	private static final PasswordChecker INSTANCE = new PasswordChecker();

	private PasswordChecker() {
	}

	public static PasswordChecker getInstance() {
		return INSTANCE;
	}

	/**
	 * This method checks the syntax of new pwd.
	 *
	 * @return true if the new password is correct
	 */
	public boolean isValid(String newPwd, String newPwd2) throws Exception {
//		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
//		List<Config> configChecks = configDao.loadConfigParametersByProperties(PROP_NODE);
//		logger.debug("checks found on db: " + configChecks.size());
		logger.debug("IN");
		
		Map<String, String> configChecks = new HashMap<>();
		configChecks.put("changepwdmodule.len_min", "8");
		configChecks.put("changepwdmodule.alphabetical", "abcdefghjklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		configChecks.put("changepwdmodule.lower_char", "abcdefghjklmnopqrstuwxyz");
		configChecks.put("changepwdmodule.upper_char", "ABCDEFGJKLMNOPQRSTUVWXYZ");
		configChecks.put("changepwdmodule.special_char", "_|-#$");
		configChecks.put("changepwdmodule.number", "0123456789");

		if (StringUtils.isEmpty(newPwd) || StringUtils.isEmpty(newPwd2)) {
			logger.debug("The new password is empty.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14011);
		}

		if (!newPwd.equals(newPwd2)) {
			logger.debug("The two passwords are not the same.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14000);
		}

//		for (Config check : configChecks) {
		for (Map.Entry<String, String> check : configChecks.entrySet()) {

//			if (check.getValueTypeId() != null && check.getValueCheck() == null) {
//				logger.debug("The value configuration on db isn't valorized.");
//				List<String> v = new ArrayList<>();
//				v.add(check.getLabel());
//				throw new EMFUserError(EMFErrorSeverity.ERROR, 14009, v, Collections.emptyMap());
//			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_LEN_MIN)) {
				int pwdLen = newPwd.length();
				if (pwdLen < Integer.parseInt(check.getValue())) {
					logger.debug("The password's length isn't correct.");
					List<String> v = new ArrayList<>();
					v.add(check.getValue());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14001, v, Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_ALPHA)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain alphabetical char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14002, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_LOWER_CHAR)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain lower char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14004, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_UPPER_CHAR)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain upper char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14005, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_SPECIAL_CHAR)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain special char.");
					List<String> v = new ArrayList<>();
					v.add(check.getValue());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14003, v, Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_NUMBER)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain numeric char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14006, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

		}

		return true;
	}

	/**
	 * This method checks the syntax of new pwd.
	 *
	 * @return <code>true</code> if the new password is correct, exception otherwise
	 */
	public boolean isValid(final SbiUser tmpUser, String oldPwd, boolean isEncrypted, String newPwd, String newPwd2)
			throws Exception {
		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
		List<Config> configChecks = configDao.loadConfigParametersByProperties(PROP_NODE);
		logger.debug("checks found on db: " + configChecks.size());

		if (isValid(newPwd, newPwd2)) {
			if (oldPwd != null && StringUtils.isEmpty(oldPwd)) {
				logger.debug("The old password is empty.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 14011);
			}

			String oldPwdEnc = !isEncrypted
					? Password.hashPassword(oldPwd,
							tmpUser.getPassword().startsWith(Password.PREFIX_SHA_SECRETPHRASE_ENCRIPTING))
					: oldPwd;
			if (tmpUser == null || tmpUser != null && !oldPwdEnc.equals(tmpUser.getPassword())) {
				logger.debug("The old pwd is uncorrect.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 14010);
			}

			for (Config check : configChecks) {

				if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_CHANGE)) {
					if (oldPwd != null && oldPwd.equalsIgnoreCase(newPwd)) {
						logger.debug("The password's doesn't be equal the lastest.");
						throw new EMFUserError(EMFErrorSeverity.ERROR, 14007, Collections.emptyList(),
								Collections.emptyMap());
					}
				}
			}
		}

		return true;
	}

	public boolean isValid(final SbiUser tmpUser, String oldPwd, String newPwd, String newPwd2) throws Exception {
		return isValid(tmpUser, oldPwd, false, newPwd, newPwd2);
	}
	
	/**
	 * This method checks the syntax of new user password.
	 *
	 * @return <code>true</code> if the new user password is correct, exception otherwise
	 */
	
	public boolean checkPwd(String newPwd) throws Exception {
		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
//		List<Config> configChecks = configDao.loadConfigParametersByProperties(PROP_NODE);
//		logger.debug("checks found on db: " + configChecks.size());
		logger.debug("IN");
		
		Map<String, String> configChecks = new HashMap<>();
		configChecks.put("changepwdmodule.len_min", "8");
		configChecks.put("changepwdmodule.alphabetical", "abcdefghjklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		configChecks.put("changepwdmodule.lower_char", "abcdefghjklmnopqrstuwxyz");
		configChecks.put("changepwdmodule.upper_char", "ABCDEFGJKLMNOPQRSTUVWXYZ");
		configChecks.put("changepwdmodule.special_char", "_|-#$");
		configChecks.put("changepwdmodule.number", "0123456789");

		if (StringUtils.isEmpty(newPwd)) {
			logger.debug("The new password is empty.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14011);
		}

//		for (Config check : configChecks) {
		for (Map.Entry<String, String> check : configChecks.entrySet()) {

//			if (check.getValueTypeId() != null && check.getValueCheck() == null) {
//				logger.debug("The value configuration on db isn't valorized.");
//				List<String> v = new ArrayList<>();
//				v.add(check.getLabel());
//				throw new EMFUserError(EMFErrorSeverity.ERROR, 14009, v, Collections.emptyMap());
//			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_LEN_MIN)) {
				int pwdLen = newPwd.length();
				if (pwdLen < Integer.parseInt(check.getValue())) {
					logger.debug("The password's length isn't correct.");
					List<String> v = new ArrayList<>();
					v.add(check.getValue());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14001, v, Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_ALPHA)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain alphabetical char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14002, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_LOWER_CHAR)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain lower char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14004, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_UPPER_CHAR)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain upper char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14005, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_SPECIAL_CHAR)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain special char.");
					List<String> v = new ArrayList<>();
					v.add(check.getValue());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14003, v, Collections.emptyMap());
				}
			}

			if (check.getKey().equals(SpagoBIConstants.CHANGEPWDMOD_NUMBER)) {
				char[] pwdChars = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValue().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain numeric char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14006, Collections.emptyList(),
							Collections.emptyMap());
				}
			}

		}

		return true;
	}
}
