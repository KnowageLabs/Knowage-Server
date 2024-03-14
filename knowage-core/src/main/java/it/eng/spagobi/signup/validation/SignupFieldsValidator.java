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
package it.eng.spagobi.signup.validation;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLDecoder;
import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.rest.validation.IFieldsValidator;
import it.eng.spagobi.utilities.json.JSONUtils;

public class SignupFieldsValidator implements IFieldsValidator {

	private static transient Logger logger = Logger.getLogger(SignupFieldsValidator.class);
	private static final String regex_password = "[^\\d][a-zA-Z0-9]{7,15}";
	private static final String regex_email = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
	private static final String regex_date = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d";
	private static final String defaultPassword = "Password";
	private static final String defaultPasswordConfirm = "Confirm Password";

	private boolean validatePassword(String password, String username) {

		if (username != null && password.indexOf(username) != -1)
			return false;
		return password.matches(regex_password);
	}

	private boolean validateEmail(String email) {

		return email.matches(regex_email);
	}

	private boolean validateDate(String date) {

		return date.matches(regex_date);
	}

	@Override
	public JSONArray validateFields(MultivaluedMap<String, String> parameters) {
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();

		JSONArray validationErrors = new JSONArray();

		String strLocale = GeneralUtilities.trim(parameters.getFirst("locale"));
		Locale locale = new Locale(strLocale.substring(0, strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_") + 1));

		String name = GeneralUtilities.trim(parameters.getFirst("name"));
		String surname = GeneralUtilities.trim(parameters.getFirst("surname"));
		String username = GeneralUtilities.trim(parameters.getFirst("username"));
		String password = GeneralUtilities.trim(parameters.getFirst("password"));
		String confirmPassword = GeneralUtilities.trim(parameters.getFirst("confirmPassword"));
		String email = GeneralUtilities.trim(parameters.getFirst("email"));
		String birthDate = GeneralUtilities.trim(parameters.getFirst("birthDate"));
		String strUseCaptcha = (parameters.getFirst("useCaptcha") == null) ? "true" : parameters.getFirst("useCaptcha");
		boolean useCaptcha = Boolean.valueOf(strUseCaptcha);
		String captcha = GeneralUtilities.trim(parameters.getFirst("captcha"));
		String terms = parameters.getFirst("terms");
		String modify = GeneralUtilities.trim(parameters.getFirst("modify"));

		try {
			if (name != null)
				name = URLDecoder.decode(name, UTF_8.name());
			if (surname != null)
				surname = URLDecoder.decode(surname, UTF_8.name());
			if (username != null)
				username = URLDecoder.decode(username, UTF_8.name());
			if (password != null)
				password = URLDecoder.decode(password, UTF_8.name());
			if (confirmPassword != null)
				confirmPassword = URLDecoder.decode(confirmPassword, UTF_8.name());
			if (email != null)
				email = URLDecoder.decode(email, UTF_8.name());
			if (birthDate != null)
				birthDate = URLDecoder.decode(birthDate, UTF_8.name());

		} catch (Exception ex) {
			logger.error(ex.getMessage());
			throw new RuntimeException(ex);
		}

		try {

			if (email == null)
				// validationErrors.put( new JSONObject("{message: 'Field Email mandatory'}") );
				validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.emailMandatory", locale) + "\"}"));
			else {
				if (!validateEmail(email))
					// validationErrors.put( new JSONObject("{message: 'Field Email invalid syntax'}") );
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.emailInvalid", locale) + "\"}"));
			}
			if (birthDate != null)
				if (!validateDate(birthDate))
					// validationErrors.put( new JSONObject("{message: 'Field Birthday invalid syntax'}") );
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.birthdayInvalid", locale) + "\"}"));

			if (name == null)
				// validationErrors.put( new JSONObject("{message: 'Field Name mandatory'}") );
				validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.nameMandatory", locale) + "\"}"));
			if (surname == null)
				// validationErrors.put( new JSONObject("{message: 'Field Surname mandatory'}") );
				validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.surnameMandatory", locale) + "\"}"));

			if (modify == null) {
				if (password == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.pwdMandatory", locale) + "\"}"));
				else {
					if (!validatePassword(password, username)) {
						// String errorMsg = "Field Password invalid syntax. \n " +
						// " Correct syntax: \n "+
						// " 	- minimum 8 chars \n "+
						// "	- not start with number \n "+
						// "	- not contain the usename ";
						String errorMsg = msgBuilder.getMessage("signup.check.pwdInvalid", locale);
						validationErrors.put(new JSONObject("{message: '" + JSONUtils.escapeJsonString(errorMsg) + "'}"));
					}
				}

				if (username == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.usernameMandatory", locale) + "\"}"));

				if (confirmPassword == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.confirmPwdMandatory", locale) + "\"}"));

				if (useCaptcha && !Boolean.valueOf(terms))
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.agreeMandatory", locale) + "\"}"));

				if (password != null && !password.equals(defaultPassword) && confirmPassword != null && !confirmPassword.equals(defaultPasswordConfirm))
					if (!password.equals(confirmPassword))
						validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.pwdNotEqual", locale) + "\"}"));
				if (useCaptcha && captcha == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.captchaMandatory", locale) + "\"}"));
			}
		} catch (JSONException e1) {
			logger.error(e1.getMessage());
			throw new RuntimeException(e1);
		}

		return validationErrors;

	}
}
