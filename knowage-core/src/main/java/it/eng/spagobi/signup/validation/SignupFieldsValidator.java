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

import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.esapi.errors.EncodingException;
import org.owasp.esapi.reference.DefaultEncoder;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.rest.validation.IFieldsValidator;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

public class SignupFieldsValidator implements IFieldsValidator {

	private static final Logger LOGGER = LogManager.getLogger(SignupFieldsValidator.class);
	private static final String REGEX_PASSPHRASE = "[^\\d][a-zA-Z0-9]{7,15}";
	private static final String REGEX_EMAIL = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
	private static final String REGEX_DATE = "(0[1-9]|[12][\\d]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d";
	private static org.owasp.esapi.Encoder esapiEncoder = DefaultEncoder.getInstance();
	
	private boolean validatePassword(String password, String username) {

		if (username != null && password.indexOf(username) != -1)
			return false;
		return password.matches(REGEX_PASSPHRASE);
	}

	private boolean validateEmail(String email) {

		return email.matches(REGEX_EMAIL);
	}

	private boolean validateDate(String date) {

		return date.matches(REGEX_DATE);
	}

	@Override
	public JSONArray validateFields(MultivaluedMap<String, String> parameters) throws EncodingException{
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
		boolean useCaptcha = Boolean.parseBoolean(strUseCaptcha);
		String captcha = GeneralUtilities.trim(parameters.getFirst("captcha"));
		String terms = parameters.getFirst("terms");
		String modify = GeneralUtilities.trim(parameters.getFirst("modify"));

		try {
			if (name != null)
				name = esapiEncoder.decodeFromURL(name);
			if (surname != null)
				surname = esapiEncoder.decodeFromURL(surname);
			if (username != null)
				username = esapiEncoder.decodeFromURL(username);
			if (password != null)
				password = esapiEncoder.decodeFromURL(password);
			if (confirmPassword != null)
				confirmPassword = esapiEncoder.decodeFromURL(confirmPassword);
			if (email != null)
				email = esapiEncoder.decodeFromURL(email);
			if (birthDate != null)
				birthDate = esapiEncoder.decodeFromURL(birthDate);

		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
			throw new SpagoBIRuntimeException(ex);
		}

		try {

			if (email == null)
				validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.emailMandatory", locale) + "\"}"));
			else {
				if (!validateEmail(email))
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.emailInvalid", locale) + "\"}"));
			}
			if (birthDate != null && !validateDate(birthDate))
				validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.birthdayInvalid", locale) + "\"}"));

			if (name == null)
				validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.nameMandatory", locale) + "\"}"));
			
			if (surname == null)
				validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.surnameMandatory", locale) + "\"}"));

			if (modify == null) {
				if (password == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.pwdMandatory", locale) + "\"}"));
				else {
					if (!validatePassword(password, username)) {
						String errorMsg = msgBuilder.getMessage("signup.check.pwdInvalid", locale);
						validationErrors.put(new JSONObject("{message: '" + JSONUtils.escapeJsonString(errorMsg) + "'}"));
					}
				}

				if (username == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.usernameMandatory", locale) + "\"}"));

				if (confirmPassword == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.confirmPwdMandatory", locale) + "\"}"));

				if (useCaptcha && !Boolean.parseBoolean(terms))
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.agreeMandatory", locale) + "\"}"));

				if (password != null && !password.equals("Password") && 
						confirmPassword != null && 
						!confirmPassword.equals("Confirm Password") &&
						!password.equals(confirmPassword))
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.pwdNotEqual", locale) + "\"}"));
				
				if (useCaptcha && captcha == null)
					validationErrors.put(new JSONObject("{message: \"" + msgBuilder.getMessage("signup.check.captchaMandatory", locale) + "\"}"));
			}
		} catch (JSONException e1) {
			LOGGER.error(e1.getMessage());
			throw new SpagoBIRuntimeException(e1);
		}

		return validationErrors;

	}
}
