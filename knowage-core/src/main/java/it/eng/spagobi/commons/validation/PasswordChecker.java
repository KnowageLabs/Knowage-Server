package it.eng.spagobi.commons.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.StringUtilities;
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
		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
		List configChecks = configDao.loadConfigParametersByProperties(PROP_NODE);
		logger.debug("checks found on db: " + configChecks.size());

		if (StringUtilities.isEmpty(newPwd) || StringUtilities.isEmpty(newPwd2)) {
			logger.debug("The new password is empty.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14011);
		}

		if (!newPwd.equals(newPwd2)) {
			logger.debug("The two passwords are not the same.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14000);
		}

		for (Object lstConfigCheck : configChecks) {
			Config check = (Config) lstConfigCheck;

			if (check.getValueTypeId() != null && check.getValueCheck() == null) {
				logger.debug("The value configuration on db isn't valorized.");
				Vector v = new Vector();
				v.add(check.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, 14009, v, new HashMap());
			}

			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_LEN_MIN)) {
				int pwdLen = newPwd.length();
				if (pwdLen < Integer.parseInt(check.getValueCheck())) {
					logger.debug("The password's length isn't correct.");
					Vector v = new Vector();
					v.add(check.getValueCheck());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14001, v, new HashMap());
				}
			}

			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_ALPHA)) {
				char pwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValueCheck().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain alphabetical char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14002, new Vector(), new HashMap());
				}
			}

			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_LOWER_CHAR)) {
				char pwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValueCheck().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain lower char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14004, new Vector(), new HashMap());
				}
			}

			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_UPPER_CHAR)) {
				char pwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValueCheck().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain upper char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14005, new Vector(), new HashMap());
				}
			}

			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_SPECIAL_CHAR)) {
				char pwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValueCheck().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain special char.");
					Vector v = new Vector();
					v.add(check.getValueCheck());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14003, v, new HashMap());
				}
			}

			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_NUMBER)) {
				char pwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), pwdChars, 0);
				boolean containsChar = false;
				for (char pwdChar : pwdChars) {
					if (check.getValueCheck().contains(String.valueOf(pwdChar))) {
						containsChar = true;
						break;
					}
				}
				if (!containsChar) {
					logger.debug("The password's doesn't contain numeric char.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14006, new Vector(), new HashMap());
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
	public boolean isValid(final SbiUser tmpUser, String oldPwd, boolean isEncrypted, String newPwd, String newPwd2) throws Exception {
		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
		List configChecks = configDao.loadConfigParametersByProperties(PROP_NODE);
		logger.debug("checks found on db: " + configChecks.size());

		if (isValid(newPwd, newPwd2)) {
			if (oldPwd != null && StringUtilities.isEmpty(oldPwd)) {
				logger.debug("The old password is empty.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 14011);
			}

			String oldPwdEnc = !isEncrypted ? Password.encriptPassword(oldPwd, tmpUser.getPassword().startsWith(Password.PREFIX_SHA_PWD_ENCRIPTING)) : oldPwd;
			if (tmpUser == null || tmpUser != null && !oldPwdEnc.equals(tmpUser.getPassword())) {
				logger.debug("The old pwd is uncorrect.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 14010);
			}

			for (Object lstConfigCheck : configChecks) {
				Config check = (Config) lstConfigCheck;

				if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_CHANGE)) {
					if (oldPwd != null && oldPwd.equalsIgnoreCase(newPwd)) {
						logger.debug("The password's doesn't be equal the lastest.");
						throw new EMFUserError(EMFErrorSeverity.ERROR, 14007, new Vector(), new HashMap());
					}
				}
			}
		}

		return true;
	}

	public boolean isValid(final SbiUser tmpUser, String oldPwd, String newPwd, String newPwd2) throws Exception {
		return isValid(tmpUser, oldPwd, false, newPwd, newPwd2);
	}
}
