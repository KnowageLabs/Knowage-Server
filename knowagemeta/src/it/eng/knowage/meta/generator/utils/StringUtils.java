/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.generator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {

	private static Logger logger = LoggerFactory.getLogger(StringUtils.class);

	/**
	 * Inserts a given character at the beginning and at the end of the specified string. For example if the string is <tt>extreme</tt> and the char is
	 * <tt>'</tt> then the returned string is <tt>'exterme'</tt>.
	 */
	public static String quote(String str, char c) {

		assert (str != null);
		StringBuffer buffer = new StringBuffer(str.length() + 2);
		buffer.append(c);
		buffer.append(str);
		buffer.append(c);
		return buffer.toString();
	}

	public static String doubleQuote(String str) {
		return quote(str, '"');
	}

	/**
	 * Returns the argument string with the first char upper-case.
	 */
	public static String initUpper(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	public static String strReplaceAll(String str, String pattern, String replaceStr) {
		if (str == null) {
			return null;
		}
		if (replaceStr == null)
			replaceStr = "";
		if (pattern == null || pattern.equals("")) {
			return str;
		}
		int index = str.indexOf(pattern);
		while (index >= 0) {
			str = str.substring(0, index) + replaceStr + str.substring(index + pattern.length());
			index = str.indexOf(pattern, index + replaceStr.length());
		}

		return str;
	}

	/**
	 * Utility methods used to convert DB object names to appropriate Java type and field name
	 */
	public static String pluralise(String name) {
		if (name == null || "".equals(name))
			return "";
		String result = name;
		if (name.length() == 1) {
			result += 's';
		} else if (!seemsPluralised(name)) {
			String lower = name.toLowerCase();
			if (!lower.endsWith("data")) { // orderData --> orderDatas is dumb
				char secondLast = lower.charAt(name.length() - 2);
				if (!isVowel(secondLast) && lower.endsWith("y")) {
					// city, body etc --> cities, bodies
					result = name.substring(0, name.length() - 1) + "ies";
				} else if (lower.endsWith("ch") || lower.endsWith("s")) {
					// switch --> switches or bus --> buses
					result = name + "es";
				} else {
					result = name + "s";
				}
			}
		}
		return result;
	}

	private static boolean seemsPluralised(String name) {
		name = name.toLowerCase();
		boolean pluralised = false;
		pluralised |= name.endsWith("es");
		pluralised |= name.endsWith("s");
		pluralised &= !(name.endsWith("ss") || name.endsWith("us"));
		return pluralised;
	}

	private final static boolean isVowel(char c) {
		boolean vowel = false;
		vowel |= c == 'a';
		vowel |= c == 'e';
		vowel |= c == 'i';
		vowel |= c == 'o';
		vowel |= c == 'u';
		vowel |= c == 'y';
		return vowel;
	}

	public static String getStringFromFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuffer buffer = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffer.append(line + "\n");
		}

		return buffer.toString();
	}

	public static String getStringFromStream(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		StringBuffer buffer = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffer.append(line + "\n");
		}

		return buffer.toString();
	}

	public static String join(List<String> collectionOfStrings, String delim) {

		StringBuilder result = new StringBuilder();
		for (String string : collectionOfStrings) {
			result.append(string);
			result.append(delim);
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
	}
}
