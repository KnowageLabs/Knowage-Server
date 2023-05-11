/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.knowage.api.dossier.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class FileUtilities {

	private static final Logger LOGGER = Logger.getLogger(FileUtilities.class);

	private FileUtilities() {

	}

	public static File createFile(String fileName, String fileExtension, String randomKey, List<PlaceHolder> placeHolders) {
		LOGGER.debug("IN");

		LOGGER.debug("Creating a file called " + fileName + "." + fileExtension);

		File dossierFolder = DossierExecutionUtilities.getDossierExecutionFolder();
		List<String> placeHoldersValues = new ArrayList<>();
		for (PlaceHolder placeHolder : placeHolders) {
			String value = placeHolder.getValue();
			placeHoldersValues.add(value);
		}
		// if we have multiple placeholder value we concatenate the values with -
		String placeHolderConcatenation = StringUtilities.join(placeHoldersValues, "-");
		String path = dossierFolder.getAbsolutePath() + File.separator + randomKey + File.separator + placeHolderConcatenation;
		File documentsFolder = new File(path);
		documentsFolder.mkdirs();
		File createdFile = new File(documentsFolder, fileName + fileExtension);
		try {
			createdFile.createNewFile();
		} catch (IOException e) {
			LOGGER.error("Error creating a new file");
			throw new SpagoBIRuntimeException("Error creating a new file", e);
		}

		LOGGER.debug("OUT");

		return createdFile;
	}

	public static File createErrorFile(BIObject biObj, Throwable error, List<PlaceHolder> placeHolders,String randomKey) {
		LOGGER.debug("IN");
		File toReturn = null;
		LOGGER.debug("Creating error file for biObject with label [" + biObj.getLabel() + "]");

		try {
			String fileName = "Error " + biObj.getLabel() + "-" + biObj.getName();
			toReturn = createFile(fileName, ".txt", randomKey, placeHolders);
			try (FileWriter fw = new FileWriter(toReturn)) {
				fw.write("Error while executing biObject " + biObj.getLabel() + " - " + biObj.getName() + "\n");
				if (error != null) {
					StackTraceElement[] errs = error.getStackTrace();
					for (int i = 0; i < errs.length; i++) {
						String err = errs[i].toString();
						fw.write(err + "\n");
					}
				}
				fw.flush();
			}
		} catch (Exception e) {
			LOGGER.error("Error in writing error file for biObj " + biObj.getLabel());

			throw new SpagoBIServiceException("Error in wirting error file for biObj " + biObj.getLabel(), e);
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	public static void writeFile(File file,byte[] byteArray){
		try(FileOutputStream stream = new FileOutputStream(file)) {
			stream.write(byteArray);

			stream.flush();

			LOGGER.debug("create an export file named " + file.getName());
		} catch (FileNotFoundException e) {
			LOGGER.error("File with path "+file.getPath()+" doesn't exists",e);
			throw new SpagoBIRuntimeException("File with path "+file.getPath()+" doesn't exists");
		} catch (IOException e) {
			LOGGER.error("Error while writing a file "+file.getName(),e);
			throw new SpagoBIRuntimeException("Error while writing a file "+file.getName(),e);
		}
	}

	public static  String cleanFileName(String name) {
		LOGGER.debug("IN");
		char[] forbiddenCharList = { '/', '?', '!', ';', ':', '.', ',', '*', '#', '@', '\'', '%', '&', '(', ')' };

		for (int i = 0; i < forbiddenCharList.length; i++) {
			char f = forbiddenCharList[i];
			name = name.replace(f, '_');
		}
		LOGGER.debug("OUT");
		return name;
	}
}
