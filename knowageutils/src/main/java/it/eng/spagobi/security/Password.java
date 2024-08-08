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
package it.eng.spagobi.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * @author Franco vuoto (franco.vuoto@eng.it)
 * @author Alessandro Pegoraro (alessandro.pegoraro@eng.it)
 *
 */
public class Password {

	private static final Logger LOGGER = Logger.getLogger(Password.class);

	public static final String PREFIX_V2_SHA_SECRETPHRASE_ENCRIPTING = "v2#SHA#";
	public static final String PREFIX_SHA_SECRETPHRASE_ENCRIPTING = "#SHA#";

	private String value = "";
	private String encValue = "";

	private int contaAlfaUpperCase = 0;
	private int contaAlfaLowerCase = 0;

	private int contaNum = 0;
	private int contaNonAlfa = 0;

	public Password() {
		value = "";
		encValue = "";
	}

	private void validate() {

		contaAlfaUpperCase = 0;
		contaAlfaLowerCase = 0;

		contaNum = 0;
		contaNonAlfa = 0;

		for (int i = 0; i < value.length(); i++) {
			int c = value.charAt(i);
			if ((c >= 'A') && (c <= 'Z')) {
				contaAlfaUpperCase++;
			}
			if ((c >= 'a') && (c <= 'z')) {
				contaAlfaLowerCase++;
			} else if ((c >= '0') && (c <= '9')) {
				contaNum++;
			} else {
				contaNonAlfa++;
			}

		}
	}

	public Password(String value) {
		this.value = value;
		encValue = "";
		validate();
	}

	public boolean hasAltenateCase() {
		return ((contaAlfaUpperCase >= 1) && (contaAlfaLowerCase >= 1));
	}

	public boolean hasDigits() {

		return (contaNum > 0);
	}

	public boolean isEnoughLong() {
		return (value.length() >= 8);
	}

	/**
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IOException
	 */
	public String getEncValue(boolean before72) {

		if (encValue != null) {
			if (before72) {
				encValue = PREFIX_SHA_SECRETPHRASE_ENCRIPTING + PasswordEncrypterHolder.OLD_INSTANCE.hash(value);
			} else {
				encValue = PREFIX_V2_SHA_SECRETPHRASE_ENCRIPTING + PasswordEncrypterHolder.INSTANCE.hash(value);
			}
		}
		return encValue;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param string
	 */
	public void setValue(String string) {
		value = string;
		validate();
	}

	/**
	 * public method used to store passwords on DB.
	 *
	 * @param clear password to encrypt
	 * @return encrypted password
	 * @throws Exception wrapping InvalidKeyException and NoSuchAlgorithmException
	 */
	public static String hashPassword(String password, boolean before72) {
		if (password != null) {
			Password hashPass = new Password(password);
			password = (hashPass.getEncValue(before72));
		}
		return password;
	}

	public static String hashPassword(String password) {
		return hashPassword(password, false);
	}
}
