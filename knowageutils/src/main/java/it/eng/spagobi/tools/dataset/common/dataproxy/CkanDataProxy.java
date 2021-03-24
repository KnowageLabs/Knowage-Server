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
package it.eng.spagobi.tools.dataset.common.dataproxy;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

/* @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.client.ProxyClientUtilities;

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

			// recover the file from resources!
			String url = this.resPath;
			Map<String, String> headers = new HashMap<>();

			inputStream = RestUtilities.makeRequestGetStream(HttpMethod.Get, url, headers, "", null, false);
			Assert.assertNotNull(inputStream, "Impossible to get http stream for web resource");
			dataReader.setMaxResults(this.getMaxResultsReader());
			dataReader.setCalculateResultNumberEnabled(calculateResultNumberOnLoad);
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
		WebTarget target = ProxyClientUtilities.getTarget(fileURL);
		Builder request = target.request(MediaType.APPLICATION_OCTET_STREAM);
		InputStream stream = null;
		try {
			// For FIWARE CKAN instance
			if (ckanApiKey != null) {
				request.header("X-Auth-Token", ckanApiKey);
			}
			// For ANY CKAN instance
			// httpget.setRequestHeader("Authorization", ckanApiKey);
			stream = request.get(InputStream.class);

			logger.debug("OUT");
		} catch (Throwable t) {
			logger.error("Error while saving file into server: " + t);
			throw new SpagoBIServiceException("Error while saving file into server", t);
		}
		// return input stream from the HTTP connection
		return stream;
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
		Base64.Encoder encoder = Base64.getEncoder();
		String encoded = encoder.encodeToString(checksum);
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