/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;



/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class FileDestination extends JavaClassDestination {

	public static final String OUTPUT_FILE_DIR = "D:\\Export_Report\\";
	public static final String OUTPUT_FILE_NAME = "output.dat";
	
	private static transient Logger logger = Logger.getLogger(FileDestination.class);
    
	
	public void execute() {
		File outputDir;
		File outputFile;
		OutputStream out;
		byte[] content = this.getDocumentByte();
		String outputFileName;
		
		
		logger.debug("IN");
		
		outputFile = null;
		out = null;
		try {
			
			outputFileName = getFileName();
			
			logger.debug("Output dir [" + OUTPUT_FILE_DIR + "]");
			logger.debug("Output filename [" + outputFileName + "]");
			
			outputDir = new File(OUTPUT_FILE_DIR);
			outputFile = new File(outputDir, outputFileName);
			
			
			
			if(!outputDir.exists()) {
				logger.debug("Output dir [" + OUTPUT_FILE_DIR + "] does not exist");
				logger.debug("Creating output dir [" + OUTPUT_FILE_DIR + "] ...");
				if(outputDir.mkdirs()) {
					logger.debug("Output dir [" + OUTPUT_FILE_DIR + "] succesfully created");
				} else {
					throw new SpagoBIRuntimeException("Impossible to create outputd dir [" + OUTPUT_FILE_DIR + "]");
				}
			} else {
				if(!outputDir.isDirectory()) {
					throw new SpagoBIRuntimeException("Outputd dir [" + OUTPUT_FILE_DIR + "] i not a valid directory");
				}
			}
			
			if(outputFile.exists()) {
				logger.debug("Output file [" + outputFile.getName() + "] alredy exists. It wil be overwritten");
			}
			
			
			try {
				out = new BufferedOutputStream(new FileOutputStream(outputFile));
			} catch (FileNotFoundException e) {
				throw new SpagoBIRuntimeException("Impossible to open a byte stream to file [" + outputFile.getName() + "]", e);
			} 
			Assert.assertNotNull(out, "Output stream cannot be null");
			
			try {
				out.write(content);
			} catch (IOException e) {
				throw new SpagoBIRuntimeException("Impossible to write on file [" + outputFile.getName() + "]", e);
			}
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("An unpredicted error occurs while saving document to file [" + outputFile.getName() + "]", t);
		} finally {
			if(out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					throw new SpagoBIRuntimeException("Impossible to properly close file [" + outputFile.getName() + "]", e);
				}
				
			}
			logger.debug("OUT");
		}
		
	}
	
	private String getFileName() {
		String filename = "";
		BIObject analyticalDoc;
		List analyticalDrivers; // questi sono i parametri associati al doc
		BIObjectParameter analyticalDriver;
		String extension = "pdf";
		
		analyticalDoc = getBiObj();
		analyticalDrivers = analyticalDoc.getBiObjectParameters();
		for(int i = 0; i < analyticalDrivers.size(); i++) {
			analyticalDriver = (BIObjectParameter)analyticalDrivers.get(i);
			
			String parameterUrlName = analyticalDriver.getParameterUrlName();
			List values = analyticalDriver.getParameterValues();
			
			if(!parameterUrlName.equalsIgnoreCase("outputType")) {
				filename += values.get(0);
			} else {
				extension = "" + values.get(0);
			}
			
		}
		
		filename =  filename.replaceAll("[^a-zA-Z0-9]", "_");
		filename += "." + extension;
		
		return filename;
	}
	

}
