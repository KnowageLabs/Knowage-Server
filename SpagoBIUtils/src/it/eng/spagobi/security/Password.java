/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;

import it.eng.spagobi.commons.SingletonConfig;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * @author Franco vuoto (franco.vuoto@eng.it)
 * @author Alessandro Pegoraro (alessandro.pegoraro@eng.it)
 * 
 */
public class Password {

	private String value = "";
	private String encValue = "";

	private int contaAlfaUpperCase = 0;
	private int contaAlfaLowerCase = 0;
	
	private int contaNum = 0;
	private int contaNonAlfa = 0;
	
	static private Logger logger = Logger.getLogger(Password.class);

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
			if ((c >= 'A') && (c <= 'Z') ) {
				contaAlfaUpperCase++;
			}if ( (c >= 'a') && (c <= 'z')) {
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
		return ( (contaAlfaUpperCase>=1 )  && (contaAlfaLowerCase>=1) );
		}

	public boolean hasDigits() {

		return (contaNum>0);
	}

	public boolean isEnoughLong() {
		return (value.length() >=8);
	}

	/**
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public String getEncValue() throws InvalidKeyException, NoSuchAlgorithmException{

		if (encValue != null) {
			AsymmetricProviderSingleton bs = AsymmetricProviderSingleton.getInstance();
			encValue = "#SHA#" + bs.enCrypt(value);
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
	public static String encriptPassword(String password) throws Exception {
		if (password != null){
			String enable=SingletonConfig.getInstance().getConfigValue("internal.security.encript.password");
			if ("true".equalsIgnoreCase(enable)){
					// enable the password encription
					if (!password.startsWith("#SHA#")){
						Password hashPass = new Password(password);
						try {
							password = (hashPass.getEncValue());
						} catch (InvalidKeyException e) {
							logger.error("HASH not valid", e);
							throw new Exception("HASH not valid",e);
						} catch (NoSuchAlgorithmException e) {
							logger.error("Impossibile to calcolate l'HASH", e);
							throw new Exception("Impossibile to calcolate l'HASH",e);
						}
					}
			}
		}
		return password;
	}
}
