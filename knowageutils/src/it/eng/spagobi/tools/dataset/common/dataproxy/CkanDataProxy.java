/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;

/* @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.ckan.CKANClient;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

public class CkanDataProxy extends AbstractDataProxy {

	String fileName;

	int maxResultsReader = -1;

	private static transient Logger logger = Logger.getLogger(CkanDataProxy.class);

	public CkanDataProxy(String resourcePath) {
		this.resPath = resourcePath;
	}

	public IDataStore load(String statement, IDataReader dataReader) throws EMFUserError {
		throw new UnsupportedOperationException("method CkanDataProxy not yet implemented");
	}

	@Override
	public IDataStore load(IDataReader dataReader) {

		IDataStore dataStore = null;
		InputStream inputStream = null;

		try {
			Map profileAttributes = this.getProfile();
			Assert.assertNotNull(profileAttributes, "User profile attributes not found!!");
			// the ckan api key is the user unique identifier: see it.eng.spagobi.security.OAuth2SecurityServiceSupplier
			// String ckanApiKey = (String) profileAttributes.get("userUniqueIdentifier");
			// Assert.assertNotNull(ckanApiKey, "User unique identifier not found!!");
			String ckanApiKey = null;

			// recover the file from resources!
			String filePath = this.resPath;
			inputStream = getInputStreamFromURL(filePath, ckanApiKey);
			Assert.assertNotNull(inputStream, "Impossible to get http stream for web resource");
			dataReader.setMaxResults(this.getMaxResultsReader());
			dataStore = dataReader.read(inputStream);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to load dataset", t);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("Error closing input stream", e);
				}
			}
		}
		return dataStore;
	}

	// public String getCompleteFilePath() {
	// return resPath + File.separatorChar + fileName;
	// }

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private InputStream getInputStreamFromURL(String fileURL, String ckanApiKey) throws IOException {
		logger.debug("IN");
		HttpClient httpClient = CKANClient.getHttpClient();
		GetMethod httpget = new GetMethod(fileURL);
		InputStream is = null;
		try {
			int statusCode = -1;
			// For FIWARE CKAN instance
			if (ckanApiKey != null) {
				httpget.setRequestHeader("X-Auth-Token", ckanApiKey);
			}
			// For ANY CKAN instance
			// httpget.setRequestHeader("Authorization", ckanApiKey);
			statusCode = httpClient.executeMethod(httpget);
			if (statusCode == HttpStatus.SC_OK) {
				is = httpget.getResponseBodyAsStream();
			}
			logger.debug("OUT");
		} catch (Throwable t) {
			logger.error("Error while saving file into server: " + t);
			throw new SpagoBIServiceException("Error while saving file into server", t);
		}
		// return input stream from the HTTP connection
		return is;
	}

	private byte[] createChecksum() {
		logger.debug("IN");
		byte[] toReturn = null;
		InputStream fis = null;
		try {
			String filePath = this.resPath;
			String ckanApiKey = "740f922c-3929-4715-9273-72210e7982e8";
			fis = getInputStreamFromURL(filePath, ckanApiKey);

			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			toReturn = complete.digest();

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot get file checksum", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("Error closing input stream", e);
				}
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	public String getMD5Checksum() {
		logger.debug("IN");
		byte[] checksum = this.createChecksum();
		BASE64Encoder encoder = new BASE64Encoder();
		String encoded = encoder.encode(checksum);
		logger.debug("OUT: returning [" + encoded + "]");
		return encoded;
	}

	public int getMaxResultsReader() {
		return maxResultsReader;
	}

	public void setMaxResultsReader(int maxResultsReader) {
		this.maxResultsReader = maxResultsReader;
	}
}