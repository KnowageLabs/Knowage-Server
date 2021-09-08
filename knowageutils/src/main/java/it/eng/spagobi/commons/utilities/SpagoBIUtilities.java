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
package it.eng.spagobi.commons.utilities;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Contains some SpagoBI's general utilities.
 */
public class SpagoBIUtilities {

	private static transient Logger logger = Logger.getLogger(SpagoBIUtilities.class);

	public static final int MAX_DEFAULT_TEMPLATE_SIZE = 5242880;
	public static String SPAGOBI_HOST = null;

	public static final String MULTI_VALUE_PROFILE_ATTRIBUTE_REGEXP = "^{.+{.*}}$";

	/**
	 * The Main method.
	 *
	 * @param args String for command line arguments
	 */
	public static void main(String[] args) {

		String[] okvalues = new String[] { "{;{a;b;c}}", "{;{abc}}", "{;{ab;c}}", "{ {a b c}}", "{,{a,b,c}}", "{;g{a;b;c}}", "{a;{a;b;c}}" };

		String[] kovalues = new String[] { "a{;{a;b;c}}", "{;{a;b;c}}a", "Davide", " f s ", "{{a;b;c}}" };

		for (int i = 0; i < okvalues.length; i++) {
			logger.debug(okvalues[i] + " : " + isMultivalueProfileAttribute(okvalues[i]));
		}
		logger.debug("***************************");
		for (int i = 0; i < kovalues.length; i++) {
			logger.debug(kovalues[i] + " : " + isMultivalueProfileAttribute(kovalues[i]));
		}

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
	 * @return true if the errorHandler contains only validation error, false if erroHandler is empty or contains not only validation error.
	 */
	public static boolean isErrorHandlerContainingOnlyValidationError(EMFErrorHandler errorHandler) {
		logger.debug("IN");
		boolean contOnlyValImpl = false;
		Collection errors = errorHandler.getErrors();
		if (errors != null && errors.size() > 0) {
			if (errorHandler.isOKByCategory(EMFErrorCategory.INTERNAL_ERROR) && errorHandler.isOKByCategory(EMFErrorCategory.USER_ERROR)) {
				contOnlyValImpl = true;
			}
		}
		logger.debug("OUT" + contOnlyValImpl);
		return contOnlyValImpl;
	}

	/**
	 * Given an <code>InputStream</code> as input, gets the correspondent bytes array.
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
	 * Reads the content from the input <code>InputStream</code> and stores it into a byte array. If the byte array exceeds the max size specified in input, a
	 * <code>SecurityException</code> is thrown.
	 *
	 * @param is      The input stream
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
	 * Given an <code>InputStream</code> as input flushs the content into an OutputStream and then close the input and output stream.
	 *
	 * @param is           The input stream
	 * @param os           The output stream
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
					logger.error(" Error closing streams", e);
				}

			}
			logger.debug("OUT");
		}
	}

	/**
	 * From a String identifying the complete name for a file, gets the relative file names, which are substrings of the starting String, according to the java
	 * separator "/".
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
	 * Returns the context for SpagoBI
	 *
	 * @return A String with SpagoBI's context
	 */

	public static String readJndiResource(String jndiName) {
		logger.debug("IN.jndiName=" + jndiName);
		String value = null;
		try {
			Context ctx = new InitialContext();
			value = (String) ctx.lookup(jndiName);
			logger.debug("jndiName: " + value);

		} catch (NamingException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		} catch (Throwable t) {
			logger.error(t);
		} finally {
			logger.debug("OUT.value=" + value);
		}
		return value;
	}

	/**
	 * Returns the address for SpagoBI as an URL and puts it into a string. The information contained are the Server name and port. Before saving, both them are
	 * written into the output console.
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
	 * Find the attribute values in case of multi value attribute. The sintax is: {splitter character{list of values separated by the splitter}}. Examples:
	 * {;{value1;value2;value3....}} {|{value1|value2|value3....}}
	 *
	 * @param attributeValue The String representing the list of attribute values
	 * @return The array of attribute values
	 * @throws Exception in case of sintax error
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

	public static String[] decodeProfileAttribute(String value) {
		if (isMultivalueProfileAttribute(value)) {
			try {
				return findAttributeValues(value);
			} catch (Exception e) {
				throw new RuntimeException("");
			}
		} else {
			return new String[] { value };
		}
	}

	private static boolean isMultivalueProfileAttribute(String value) {
		return GenericValidator.matchRegexp(value, MULTI_VALUE_PROFILE_ATTRIBUTE_REGEXP);
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
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "getAllProfileAttributes method invoked with null input profile object");
		HashMap profileattrs = new HashMap();
		Collection profileattrsNames = profile.getUserAttributeNames();
		if (profileattrsNames == null || profileattrsNames.size() == 0)
			return profileattrs;
		Iterator it = profileattrsNames.iterator();
		while (it.hasNext()) {
			Object profileattrName = it.next();
			Object profileattrValue = profile.getUserAttribute(profileattrName.toString());
			profileattrs.put(profileattrName, profileattrValue);
			logger.info("Add new Attribute:" + profileattrName.toString() + "/" + profileattrValue);
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
	 * Substitutes the substrings with sintax "${code,bundle}" or "${code}" (in the second case bundle is assumed to be the default value "messages") with the
	 * correspondent internationalized messages in the input String. This method calls <code>PortletUtilities.getMessage(key, bundle)</code>.
	 *
	 * @param message The string to be modified
	 *
	 * @return The message with the internationalized substrings replaced.
	 */

	/**
	 * Questo metodo permette di sostituire una parte di una stringa con un'altra.
	 *
	 * @param toParse   stringa da manipolare.
	 * @param replacing parte di stringa da sostituire.
	 * @param replaced  stringa nuova.
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
	 * Questo metodo implementa la stessa logica della funzione javascript <em>escape</em>.
	 *
	 * @param input stringa da manipolare.
	 *
	 * @return the string
	 */
	public static String encode(String input) {
		/*
		 * input = replace(input, "%", "%25"); input = replace(input, " ", "%20"); input = replace(input, "\"", "%22"); input = replace(input, "'", "%27");
		 * input = replace(input, "<", "%3C"); input = replace(input, "<", "%3E"); input = replace(input, "?", "%3F"); input = replace(input, "&", "%26");
		 */
		// input = replace(input, " ", "&#160;");
		input = replace(input, " ", "_");
		return input;
	}

	/**
	 * Questo metodo implementa la stessa logica della funzione javascript <em>escape</em>.
	 *
	 * @param input stringa da manipolare.
	 *
	 * @return the string
	 */
	public static String decode(String input) {
		/*
		 * input = replace(input, "%25", "%"); input = replace(input, "%20", " "); input = replace(input, "%22", "\""); input = replace(input, "%27", "'");
		 * input = replace(input, "%3C", "<"); input = replace(input, "%3E", "<"); input = replace(input, "%3F", "?"); input = replace(input, "%26", "&");
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
		if (value == null)
			value = "";
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
	 * @param values    the values
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
	 * Checks if the String in input contains a reference to System property with the syntax ${property_name}, and, in case, substitutes the reference with the
	 * actual value.
	 *
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
		if (beginIndex != -1) {
			int endIndex = input.indexOf("}", beginIndex);
			if (endIndex != -1) {
				String propertyName = toReturn.substring(beginIndex + 2, endIndex);
				logger.debug("Found reference to property " + propertyName);
				String propertyValue = System.getProperty(propertyName);
				logger.debug("Property with name = [" + propertyName + "] has value = [" + propertyValue + "]");
				if (propertyValue != null && !propertyValue.trim().equals("")) {
					if (propertyValue.endsWith(File.separator) && toReturn.substring(endIndex + 1).startsWith(File.separator)) {
						propertyValue = propertyValue.substring(0, propertyValue.length() - File.separator.length());
					}
					toReturn = toReturn.substring(0, beginIndex) + propertyValue + toReturn.substring(endIndex + 1);
				} else {
					logger.warn("Property with name = [" + propertyName + "] has no proper value.");
				}
			}
		}
		logger.debug("OUT: toReturn = [" + toReturn + "]");
		return toReturn;
	}

	/**
	 * Get the root resource path
	 *
	 * @return the path for resources
	 */
	public static String getRootResourcePath() {
		String resourcePath = EnginConf.getInstance().getRootResourcePath();
		if (StringUtils.isEmpty(resourcePath)) {
			SingletonConfig configSingleton = SingletonConfig.getInstance();
			String path = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			String systemPathVar = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_SYSTEMVAR_JNDI_NAME");
			logger.debug("Resource path systemPathVar " + systemPathVar);
			if (systemPathVar == null || systemPathVar.length() == 0) {
				resourcePath = SpagoBIUtilities.readJndiResource(path);
				logger.debug("Resource path loaded by jndi  " + resourcePath);
			} else {
				// search the resource folder from system variable (can be argument -D..... on lunching configuration of server)
				// this approach is good when you work with a cluster architecture
				logger.debug("loading the resource path from system variable");
				String systemPathVarSuffix = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_FROM_SYSTEMVAR_SUFFIX");

				logger.debug("Resource path systemPathVarSuffix " + systemPathVarSuffix);
				if (systemPathVar != null) {
					resourcePath = System.getProperty(systemPathVar);
				}
				logger.debug("Resource path resourcePath via systemvar " + resourcePath);
				if (systemPathVarSuffix != null) {
					if (!resourcePath.endsWith(File.separatorChar + "")) {
						resourcePath = resourcePath + File.separatorChar;
					}
					resourcePath = resourcePath + systemPathVarSuffix;
				}
				logger.debug("Resource path resourcePath via systemvar with suffix " + resourcePath);
			}
			logger.debug("Resource path " + resourcePath);
		}
		return resourcePath;
	}

	/**
	 * Get the tenant resource path. This is likely the one that should be used 99% of the times
	 *
	 * @return the path for tenant resources
	 */
	public static String getResourcePath() {
		Tenant tenant = TenantManager.getTenant();
		if (tenant == null) {
			throw new SpagoBIRuntimeException("Tenant is not set. Impossible to get the tenant resource path.");
		}
		String resourcePath = getRootResourcePath() + (getRootResourcePath().endsWith(File.separatorChar + "") ? "" : File.separatorChar) + tenant.getName();
		return resourcePath;
	}

	/**
	 * Get the dataset resource path.
	 *
	 * @return the path for dataset resources without '/' at the end
	 */
	public static String getDatasetResourcePath() {
		return getResourcePath() + File.separatorChar + DataSetConstants.RESOURCE_RELATIVE_FOLDER;
	}

	/**
	 * Get the file dataset resource path.
	 *
	 * @return the path for file dataset resources without '/' at the end
	 */
	public static String getFileDatasetResourcePath() {
		return getDatasetResourcePath() + File.separatorChar + DataSetConstants.FILES_DATASET_FOLDER;
	}

	public static String getHmacKey() {
		String resourceHmac = EnginConf.getInstance().getHmacKey();
		if (resourceHmac == null || resourceHmac.isEmpty()) {
			SingletonConfig configSingleton = SingletonConfig.getInstance();
			String hmac = configSingleton.getConfigValue(HMACUtils.HMAC_JNDI_LOOKUP);
			resourceHmac = SpagoBIUtilities.readJndiResource(hmac);
		}
		return resourceHmac;
	}

	public static String getImageAsBase64(String path, String type) throws IOException {
		logger.debug("IN");
		logger.debug("The image file path is [" + path + "]");
		File fileImage = new File(path);
		if (!fileImage.exists()) {
			throw new SpagoBIRuntimeException("The path [" + path + " does not point to any image.");
		}
		BufferedImage image = ImageIO.read(fileImage);
		logger.debug("The image will be base64 encoded using image type [" + type + "]");
		String encodedImage = encodeBase64ToString(image, type);
		logger.debug("OUT");
		return encodedImage;
	}

	public static String encodeBase64ToString(BufferedImage image, String type) throws IOException {
		logger.debug("IN");
		ByteArrayOutputStream bos = null;
		String encodedImage = null;
		try {
			bos = new ByteArrayOutputStream();
			ImageIO.write(image, type, bos);
			byte[] imageBytes = bos.toByteArray();
			encodedImage = Base64.encodeBase64String(imageBytes);
		} finally {
			if (bos != null) {
				bos.close();
			}
		}
		logger.debug("OUT");
		return encodedImage;
	}

	public static String getGenericLicensePath() {
		logger.debug("IN");

		String resourceLicPath = SpagoBIUtilities.getRootResourcePath()
				+ (SpagoBIUtilities.getRootResourcePath().endsWith(File.separatorChar + "") ? "" : File.separatorChar) + SpagoBIConstants.LICENSE_PATH_SUFFIX;
		logger.debug("Resource license path " + resourceLicPath);

		logger.debug("OUT");
		return resourceLicPath;
	}

	public static String getCurrentHostName() {
		logger.debug("IN");
		InetAddress addr;
		String hostname;
		try {
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
			logger.debug("Hostname is " + hostname);
		} catch (UnknownHostException e) {
			logger.error("Error in retrieving host name", e);
			throw new SpagoBIRuntimeException("Error in retrieving host name", e);
		}
		logger.debug("OUT");
		return hostname;
	}

}
