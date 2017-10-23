/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class EngineMessageBundle {

	private static final String DEFAULT_BUNDLE = "messages";
	private static HashMap bundles = null;
	
    static {
        bundles = new HashMap();
    }
	
    /**
     * Returns an internazionalized message.
     * 
     * @param code the code of the message.
     * @param bundle the message bundle.
     * @param userLocale the user locale
     * 
     * @return the internazionalized message.
     */
    public static String getMessage(String code, String bundle, Locale userLocale) {
        
    	if (code == null) return null;
    	if (userLocale == null) return code;
    	//logger.debug("Input parameters: code = [" + code + "] ; bundle = [" + bundle + "] ; " +
    	//		"userlocale = [" + userLocale + "]");
    	if (bundle == null || bundle.trim().equals("")) {
        //	logger.debug("Bundle not specified; considering \"" + DEFAULT_BUNDLE + "\" as default value");
    		bundle = DEFAULT_BUNDLE;
    	}
    	
        String bundleKey = bundle + "_" + userLocale.getLanguage() + "_" + userLocale.getCountry();
        ResourceBundle messages = null;
        if (bundles.containsKey(bundleKey)) {
            messages = (ResourceBundle) bundles.get(bundleKey);
        } else {
            // First access to this bundle
            try {
                messages = ResourceBundle.getBundle(bundle, userLocale);
            } catch (java.util.MissingResourceException ex) {
                //logger.error("ResourceBundle with bundle = [" + bundle + "] and locale = " +
                //		"[" + userLocale + "] missing.");
            }
            
            // Put bundle in cache
            bundles.put(bundleKey, messages);
        }
        
        if (messages == null) {
            // Bundle non existent
            return code;
        } // if (messages == null)

        String message = null;
        try {
            message = messages.getString(code);
        } // try
        catch (Exception ex) {
            // No trace: may be this is not an error
        } // catch (Exception ex)
        if (message == null) return code;
        else return message;
    }
	
    /**
     * Gets the message.
     * 
     * @param code the code
     * @param userLocale the user locale
     * 
     * @return the message
     */
    public static String getMessage(String code, Locale userLocale) {
    	return getMessage(code, DEFAULT_BUNDLE, userLocale);
    }
    
    /**
     * Gets the message.
     * 
     * @param code the code
     * @param bundle the bundle
     * @param userLocale the user locale
     * @param arguments the arguments
     * 
     * @return the message
     */
    public static String getMessage(String code, String bundle, Locale userLocale, String[] arguments) {
    	String message = getMessage(code, DEFAULT_BUNDLE, userLocale);
        for (int i = 0; i < arguments.length; i++){
        	message = replace(message, i, arguments[i].toString());
        }
    	return message;
    }
    
    /**
     * Gets the message.
     * 
     * @param code the code
     * @param userLocale the user locale
     * @param arguments the arguments
     * 
     * @return the message
     */
    public static String getMessage(String code, Locale userLocale, String[] arguments) {
    	return getMessage(code, DEFAULT_BUNDLE, userLocale, arguments);
    }
    
    /**
     * Substitutes the message value to the placeholders.
     * 
     * @param messageFormat The String representing the message format
     * @param iParameter	The numeric value defining the replacing string
     * @param value	Input object containing parsing information
     * @return	The parsed string
     */
    protected static String replace(String messageFormat, int iParameter, Object value) {
		if (value != null) {
			String toParse = messageFormat;
			String replacing = "%" + iParameter;
			String replaced = value.toString();
			StringBuffer parsed = new StringBuffer();
			int parameterIndex = toParse.indexOf(replacing);
			while (parameterIndex != -1) {
				parsed.append(toParse.substring(0, parameterIndex));
				parsed.append(replaced);
				toParse = toParse.substring(
						parameterIndex + replacing.length(), toParse.length());
				parameterIndex = toParse.indexOf(replacing);
			} // while (parameterIndex != -1)
			parsed.append(toParse);
			return parsed.toString();
		} else {
			return messageFormat;
		}
	}
	
}
