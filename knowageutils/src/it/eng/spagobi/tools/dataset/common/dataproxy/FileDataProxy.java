/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FileDataProxy extends AbstractDataProxy {

	String fileName;

	boolean useTempFile = false;

	int maxResultsReader = -1;

	private static transient Logger logger = Logger.getLogger(FileDataProxy.class);

	public FileDataProxy(String resourcePath) {
		this.resPath = resourcePath;
	}

	public IDataStore load(String statement, IDataReader dataReader) throws EMFUserError {
		throw new UnsupportedOperationException("metothd FileDataProxy not yet implemented");
	}

	public IDataStore load(IDataReader dataReader) {

		IDataStore dataStore = null;
		FileInputStream inputStream = null;

		try {
			// recover the file from resources!
			String filePath = getCompleteFilePath();
			inputStream = new FileInputStream(filePath);
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

	public String getCompleteFilePath() {

		String filePath = resPath;
		if (useTempFile) {
			filePath += File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + "temp";
		} else {
			filePath += File.separatorChar + "dataset" + File.separatorChar + "files";
		}
		filePath += File.separatorChar + fileName;
		return filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private byte[] createChecksum() {
		logger.debug("IN");
		byte[] toReturn = null;
		InputStream fis = null;
		try {
			String filePath = this.getCompleteFilePath();
			fis = new FileInputStream(filePath);

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

	/**
	 * @return the useTempFile
	 */
	public boolean isUseTempFile() {
		return useTempFile;
	}

	/**
	 * @param useTempFile
	 *            the useTempFile to set
	 */
	public void setUseTempFile(boolean useTempFile) {
		this.useTempFile = useTempFile;
	}

	public int getMaxResultsReader() {
		return maxResultsReader;
	}

	public void setMaxResultsReader(int maxResultsReader) {
		this.maxResultsReader = maxResultsReader;
	}

}
