/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 7-lug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * Contains some SpagoBI's general utilities.
 */
public class SpagoBIUtilities {

	private static transient Logger logger = Logger.getLogger(SpagoBIUtilities.class);

	public static final int MAX_DEFAULT_TEMPLATE_SIZE = 5242880;
	public static String SPAGOBI_HOST = null;    


	/**
	 * The Main method.
	 * 
	 * @param args String for command line arguments
	 */
	public static void main(String[] args) {
	}

	








	/**
	 * Cleans a string from spaces and tabulation characters.
	 * 
	 * @param original The input string
	 * 
	 * @return The cleaned string
	 */
	public static String cleanString(String original) {
		logger.debug("IN");
		StringBuffer sb = new StringBuffer();
		char[] arrayChar = original.toCharArray();
		for (int i = 0; i < arrayChar.length; i++) {
			if ((arrayChar[i] == '\n') || (arrayChar[i] == '\t') || (arrayChar[i] == '\r')) {

			} else {
				sb.append(arrayChar[i]);
			}
		}
		logger.debug("OUT:" + sb.toString().trim());
		return sb.toString().trim();
	}

	/**
	 * Checks if the Spago errorHandler contains only validation errors.
	 * 
	 * @param errorHandler The error handler to check
	 * 
	 * @return true if the errorHandler contains only validation error, false if
	 * erroHandler is empty or contains not only validation error.
	 */
	public static boolean isErrorHandlerContainingOnlyValidationError(EMFErrorHandler errorHandler) {
		logger.debug("IN");
		boolean contOnlyValImpl = false;
		Collection errors = errorHandler.getErrors();
		if (errors != null && errors.size() > 0) {
			if (errorHandler.isOKByCategory(EMFErrorCategory.INTERNAL_ERROR)
					&& errorHandler.isOKByCategory(EMFErrorCategory.USER_ERROR)) {
				contOnlyValImpl = true;
			}
		}
		logger.debug("OUT" + contOnlyValImpl);
		return contOnlyValImpl;
	}

	/**
	 * Given an <code>InputStream</code> as input, gets the correspondent
	 * bytes array.
	 * 
	 * @param is The input stream
	 * 
	 * @return An array of bytes obtained from the input stream.
	 */
	public static byte[] getByteArrayFromInputStream(InputStream is) {
		logger.debug("IN");
		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos);

			int c = 0;
			byte[] b = new byte[1024];
			while ((c = is.read(b)) != -1) {
				if (c == 1024)
					bos.write(b);
				else
					bos.write(b, 0, c);
			}
			bos.flush();
			byte[] ret = baos.toByteArray();
			bos.close();
			return ret;
		} catch (IOException ioe) {
			logger.error("IOException", ioe);
			return null;
		} finally {
			logger.debug("OUT");
		}

	}
	

	/**
	 * Reads the content from the input <code>InputStream</code> and stores it into a byte array.
	 * If the byte array exceeds the max size specified in input, a <code>SecurityException</code> is thrown.
	 * @param is The input stream 
	 * @param maximum The maximum number of bytes to read
	 * @return An array of bytes obtained from the input stream.
	 */
	public static byte[] getByteArrayFromInputStream(InputStream is, int maximum) {
		logger.debug("IN");
		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos);

			int c = 0;
			int counter = 0;
			byte[] b = new byte[1024];
			while ((c = is.read(b)) != -1) {
				if (c == 1024)
					bos.write(b);
				else
					bos.write(b, 0, c);
				counter += c;
				if (counter > maximum) {
					throw new SecurityException("Maximum size [" + maximum + "] exceeded");
				}
			}
			bos.flush();
			byte[] ret = baos.toByteArray();
			bos.close();
			return ret;
		} catch (IOException ioe) {
			logger.error("IOException", ioe);
			return null;
		} finally {
			logger.debug("OUT");
		}

	}
	

	/**
	 * Given an <code>InputStream</code> as input flushs the content into an
	 * OutputStream and then close the input and output stream.
	 * 
	 * @param is The input stream
	 * @param os The output stream
	 * @param closeStreams the close streams
	 */
	public static void flushFromInputStreamToOutputStream(InputStream is, OutputStream os, boolean closeStreams) {
		logger.debug("IN");
		try {
			int c = 0;
			byte[] b = new byte[1024];
			while ((c = is.read(b)) != -1) {
				if (c == 1024)
					os.write(b);
				else
					os.write(b, 0, c);
			}
			os.flush();
		} catch (IOException ioe) {
			logger.error("IOException", ioe);
		} finally {
			if (closeStreams) {
				try {
					if (os != null)
						os.close();
					if (is != null)
						is.close();
				} catch (IOException e) {
					logger.error( " Error closing streams", e);
				}

			}
			logger.debug("OUT");
		}
	}

	/**
	 * From a String identifying the complete name for a file, gets the relative
	 * file names, which are substrings of the starting String, according to the
	 * java separator "/".
	 * 
	 * @param completeFileName The string representing the file name
	 * 
	 * @return relative names substring
	 */
	public static String getRelativeFileNames(String completeFileName) {
		logger.debug("IN");
		String linuxSeparator = "/";
		String windowsSeparator = "\\";
		if (completeFileName.indexOf(linuxSeparator) != -1) {
			completeFileName = completeFileName.substring(completeFileName.lastIndexOf(linuxSeparator) + 1);
		}
		if (completeFileName.indexOf(windowsSeparator) != -1) {
			completeFileName = completeFileName.substring(completeFileName.lastIndexOf(windowsSeparator) + 1);
		}
		logger.debug("OUT:" + completeFileName);
		return completeFileName;

	}

	/**
	 * Returns a string containing the localhost IP address.
	 * 
	 * @return The IP address String
	 */
	public static String getLocalIPAddressAsString() {
		logger.debug("IN");
		String ipAddrStr = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();
			byte[] ipAddr = addr.getAddress();

			// Convert to dot representation

			for (int i = 0; i < ipAddr.length; i++) {
				if (i > 0) {
					ipAddrStr += ".";
				}
				ipAddrStr += ipAddr[i] & 0xFF;
			}
		} catch (UnknownHostException e) {
			logger.error("UnknownHostException:", e);
		}
		logger.debug("OUT:" + ipAddrStr);
		return ipAddrStr;
	}


	/**
	 * Returns the context  for SpagoBI 
	 * 
	 * @return A String with SpagoBI's context 
	 */

	public static String readJndiResource(String jndiName) {
		logger.debug("IN.jndiName="+jndiName);
		String value=null;
		try {
			Context ctx = new InitialContext();
			value  = (String)ctx.lookup(jndiName);
			logger.debug("jndiName: " + value);

		} catch (NamingException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		} catch (Throwable t) {
			logger.error(t);
		} finally {
			logger.debug("OUT.value="+value);
		}
		return value;
	}

	/**
	 * Returns the address for SpagoBI as an URL and puts it into a
	 * string. The information contained are the Server name and port. Before
	 * saving, both them are written into the output console.
	 * 
	 * @return A String with SpagoBI's adderss
	 */









	/*
	 * This method exists since jdk 1.5 (java.util.regexp.Patter.quote())
	 */
	/**
	 * Quote.
	 * 
	 * @param s the s
	 * 
	 * @return the string
	 */
	public static String quote(String s) {
		logger.debug("IN");
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E";

		StringBuffer sb = new StringBuffer(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		logger.debug("OUT");
		return sb.toString();
	}

	/**
	 * Find the attribute values in case of multi value attribute. The sintax
	 * is: {splitter character{list of values separated by the splitter}}.
	 * Examples: {;{value1;value2;value3....}} {|{value1|value2|value3....}}
	 * 
	 * @param attributeValue
	 *                The String representing the list of attribute values
	 * @return The array of attribute values
	 * @throws Exception
	 *                 in case of sintax error
	 */
	public static String[] findAttributeValues(String attributeValue) throws Exception {
		logger.debug("IN");
		String sintaxErrorMsg = "Multi value attribute sintax error.";
		if (attributeValue.length() < 6)
			throw new Exception(sintaxErrorMsg);
		if (!attributeValue.endsWith("}}"))
			throw new Exception(sintaxErrorMsg);
		if (attributeValue.charAt(2) != '{')
			throw new Exception(sintaxErrorMsg);
		char splitter = attributeValue.charAt(1);
		String valuesList = attributeValue.substring(3, attributeValue.length() - 2);
		String[] values = valuesList.split(String.valueOf(splitter));
		logger.debug("OUT");
		return values;
	}

	/**
	 * Gets the all profile attributes.
	 * 
	 * @param profile the profile
	 * 
	 * @return the all profile attributes
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public static HashMap getAllProfileAttributes(IEngUserProfile profile) throws EMFInternalError {
		logger.debug("IN");
		if (profile == null)
			throw new EMFInternalError(EMFErrorSeverity.ERROR,
			"getAllProfileAttributes method invoked with null input profile object");
		HashMap profileattrs = new HashMap();
		Collection profileattrsNames = profile.getUserAttributeNames();
		if (profileattrsNames == null || profileattrsNames.size() == 0)
			return profileattrs;
		Iterator it = profileattrsNames.iterator();
		while (it.hasNext()) {
			Object profileattrName = it.next();
			Object profileattrValue = profile.getUserAttribute(profileattrName.toString());
			profileattrs.put(profileattrName, profileattrValue);
			logger.info("Add new Attribute:"+profileattrName.toString()+"/"+profileattrValue);
		}
		logger.debug("OUT");
		return profileattrs;
	}

	/**
	 * Delete a folder and its contents.
	 * 
	 * @param dir The java file object of the directory
	 * 
	 * @return the result of the operation
	 */
	public static boolean deleteDir(File dir) {
		logger.debug("IN");
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		logger.debug("OUT");
		return dir.delete();
	}

	/**
	 * Delete contents of a directory.
	 * 
	 * @param dir The java file object of the directory
	 * 
	 * @return the result of the operation
	 */
	public static boolean deleteContentDir(File dir) {
		logger.debug("IN");
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					logger.debug("OUT");
					return false;
				}
			}
		}
		logger.debug("OUT");
		return true;
	}

	/**
	 * Substitutes the substrings with sintax "${code,bundle}" or "${code}" (in
	 * the second case bundle is assumed to be the default value "messages")
	 * with the correspondent internationalized messages in the input String.
	 * This method calls <code>PortletUtilities.getMessage(key, bundle)</code>.
	 * 
	 * @param message The string to be modified
	 * 
	 * @return The message with the internationalized substrings replaced.
	 */


	/**
	 * Questo metodo permette di sostituire una parte di una stringa con
	 * un'altra.
	 * 
	 * @param toParse stringa da manipolare.
	 * @param replacing parte di stringa da sostituire.
	 * @param replaced stringa nuova.
	 * 
	 * @return the string
	 */
	public static String replace(String toParse, String replacing, String replaced) {
		logger.debug("IN");
		if (toParse == null) {
			return toParse;
		} // if (toParse == null)
		if (replacing == null) {
			return toParse;
		} // if (replacing == null)
		if (replaced != null) {
			int parameterIndex = toParse.indexOf(replacing);
			while (parameterIndex != -1) {
				String newToParse = toParse.substring(0, parameterIndex);
				newToParse += replaced;
				newToParse += toParse.substring(parameterIndex + replacing.length(), toParse.length());
				toParse = newToParse;
				parameterIndex = toParse.indexOf(replacing, parameterIndex + replaced.length());
			} // while (parameterIndex != -1)
		} // if (replaced != null)
		logger.debug("OUT");
		return toParse;
	} // public static String replace(String toParse, String replacing, String
	// replaced)

	/**
	 * Questo metodo implementa la stessa logica della funzione javascript
	 * <em>escape</em>.
	 * 
	 * @param input stringa da manipolare.
	 * 
	 * @return the string
	 */
	public static String encode(String input) {
		/*
		 * input = replace(input, "%", "%25"); input = replace(input, " ",
		 * "%20"); input = replace(input, "\"", "%22"); input = replace(input,
		 * "'", "%27"); input = replace(input, "<", "%3C"); input =
		 * replace(input, "<", "%3E"); input = replace(input, "?", "%3F");
		 * input = replace(input, "&", "%26");
		 */
		// input = replace(input, " ", "&#160;");
		input = replace(input, " ", "_");
		return input;
	}

	/**
	 * Questo metodo implementa la stessa logica della funzione javascript
	 * <em>escape</em>.
	 * 
	 * @param input stringa da manipolare.
	 * 
	 * @return the string
	 */
	public static String decode(String input) {
		/*
		 * input = replace(input, "%25", "%"); input = replace(input, "%20", "
		 * "); input = replace(input, "%22", "\""); input = replace(input,
		 * "%27", "'"); input = replace(input, "%3C", "<"); input =
		 * replace(input, "%3E", "<"); input = replace(input, "%3F", "?");
		 * input = replace(input, "%26", "&");
		 */
		// input = replace(input, "&#160;", " ");
		input = replace(input, "_", " ");
		return input;
	}


	/**
	 * Substitute quotes into string.
	 * 
	 * @param value the value
	 * 
	 * @return the string
	 */
	public static String substituteQuotesIntoString(String value) {
		logger.debug("IN");
		if (value == null) value = "";
		String singleQuoteString = "'";
		String doubleQuoteString = new String();
		char doubleQuoteChar = '"';
		doubleQuoteString += doubleQuoteChar;
		String singleQuoteReplaceString = "&#39;";
		String doubleQuotesReplaceString = "&#34;";
		value = value.replaceAll(singleQuoteString, singleQuoteReplaceString);
		value = value.replaceAll(doubleQuoteString, doubleQuotesReplaceString);
		logger.debug("OUT:" + value);
		return value;

	}


	/**
	 * From list to string.
	 * 
	 * @param values the values
	 * @param separator the separator
	 * 
	 * @return the string
	 */
	public static String fromListToString(List values, String separator) {
		logger.debug("IN");
		String valStr = "";
		if (values == null) {
			return valStr;
		}
		Iterator iterVal = values.iterator();
		while (iterVal.hasNext()) {
			String val = (String) iterVal.next();
			valStr += val + separator;
		}
		if (valStr.length() != 0) {
			valStr = valStr.substring(0, valStr.length() - separator.length());
		}
		logger.debug("OUT:" + valStr);
		return valStr;
	}





	/**
	 * Checks if the String in input contains a reference to System property with the syntax
	 * ${property_name}, and, in case, substitutes the reference with the actual value.
	 * @return the string with reference to System property replaced with actual value.
	 */
	public static String checkForSystemProperty(String input) {
		logger.debug("IN");
		if (input == null) {
			logger.debug("Input string is null; returning null");
			return null;
		}
		String toReturn = input;
		int beginIndex = input.indexOf("${");
		if (beginIndex != - 1) {
			int endIndex = input.indexOf("}", beginIndex);
			if (endIndex != -1) {
				String propertyName = toReturn.substring(beginIndex + 2, endIndex);
				logger.debug("Found reference to property " + propertyName);
				String propertyValue = System.getProperty(propertyName);
				logger.debug("Property with name = [" + propertyName + "] has value = [" + propertyValue + "]");
				if (propertyValue != null && !propertyValue.trim().equals("")) {
					toReturn = toReturn.substring(0, beginIndex) + propertyValue + toReturn.substring(endIndex + 1);
				} else {
					logger.warn("Property with name = [" + propertyName + "] has no proper value.");
				}
			}
		}
		logger.debug("OUT: toReturn = [" + toReturn + "]");
		return toReturn;
	}


}
