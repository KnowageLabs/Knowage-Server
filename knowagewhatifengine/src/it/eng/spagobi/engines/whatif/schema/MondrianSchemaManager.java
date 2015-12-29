/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.whatif.schema;

import it.eng.spagobi.services.proxy.ArtifactServiceProxy;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

public class MondrianSchemaManager {

	private static Logger logger = Logger.getLogger(MondrianSchemaManager.class);

	private static Map<Integer, File> cache = new HashMap<Integer, File>();

	private ArtifactServiceProxy proxy = null;

	public MondrianSchemaManager(ArtifactServiceProxy artifactProxy) {
		this.proxy = artifactProxy;
	}

	public String getMondrianSchemaURI(Integer contentId) {
		File file = cache.get(contentId);
		if (file == null // if file is not in cache
				|| // OR
				!file.isFile() || !file.exists()) { // file in cache does not
													// actually exists
			file = this.storeContent(contentId);
		}
		cache.put(contentId, file);
		return file.getAbsolutePath();
	}

	private File storeContent(Integer contentId) {
		this.deletePreviousFile(contentId);
		File newFile = null;
		DataHandler dh = proxy.getArtifactContentById(contentId);
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			newFile = new File(this.getFilePath(contentId));
			fos = new FileOutputStream(newFile);
			is = dh.getInputStream();
			int c = 0;
			byte[] b = new byte[1024];
			while ((c = is.read(b)) != -1) {
				if (c == 1024) {
					fos.write(b);
				} else {
					fos.write(b, 0, c);
				}
			}
			fos.flush();
		} catch (IOException e) {
			logger.error("Error while storing Mondrian schema into a file", e);
			throw new SpagoBIEngineRuntimeException("Error while storing Mondrian schema into a file", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logger.error("Error while closing DataHandler input stream", e);
			}
			try {
				fos.close();
			} catch (IOException e) {
				logger.error("Error while closing file output stream", e);
			}
		}
		return newFile;
	}

	private void deletePreviousFile(Integer contentId) {
		String path = this.getFilePath(contentId);
		File file = new File(path);
		if (file.exists()) {
			boolean result = file.delete();
			if (!result) {
				throw new SpagoBIEngineRuntimeException("Cannot delete previuos file [" + path + "]");
			}
		}
	}

	private String getRepositoryPath() {
		String path = System.getProperty("java.io.tmpdir")
				+ File.separator + "mondrian" + File.separator
				+ "schemas";
		File fileDir = new File(path);
		if (!fileDir.exists()) {
			boolean result = fileDir.mkdirs();
			if (!result) {
				throw new SpagoBIEngineRuntimeException("Cannot create repository path for Mondrian schemas [" + path + "]");
			}
		}
		return path;
	}

	private String getFilePath(Integer contentId) {
		return this.getRepositoryPath() + File.separator + contentId.toString() + ".xml";
	}

}
