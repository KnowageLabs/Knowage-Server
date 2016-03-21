/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Zipper {
	
	private static Logger logger = LoggerFactory.getLogger(Zipper.class);
	
	/**
	 * This method compress all the content of targetDir into jar file outFile. If outFile alredy exist it
	 * will be overwritten.
	 * 
	 * @params targetDir the folder that must be compressed into a jar
	 * @params outFile the output jar file generated compressing targetDir content
	 */
	public void compressToJar(File targetDir, File outFile){
		
		logger.trace("IN");
		
		try {
			Assert.assertNotNull("Input parametr [targetDir] cannot be null", targetDir);
			Assert.assertTrue("Input parametr [targetDir] must be a valid folder", targetDir.exists() && targetDir.isDirectory());
			Assert.assertNotNull("Input parametr [outFile] cannot be null", outFile);
			
			if(!outFile.getParentFile().exists()) {
				logger.warn("Output folder [{}] does not exist. It will be created", outFile.getParentFile().getAbsolutePath());
				outFile.getParentFile().mkdir();
			}
			
			if (outFile.exists()) {
				logger.warn("A mapping jar file named [{}] alredy exists. It will be overwritten", outFile.getAbsoluteFile());
				outFile.delete();
			}
			
			FileOutputStream fileOutputStream = new FileOutputStream(outFile);
			JarOutputStream zipOutputStream = new JarOutputStream(fileOutputStream);
			//zipOutputStream.setMethod(ZipOutputStream.STORED);
			

			compressFolder(targetDir, targetDir, zipOutputStream);
			
			zipOutputStream.flush();
			zipOutputStream.close();
			
			logger.info("Mapping jar created succesfully: [{}]", true);
		
		} catch(Exception e) {
			throw new RuntimeException("An error occurred while compressing folder [" + targetDir +"] into jar file [" + outFile + "]", e);
		} finally {
			logger.trace("OUT");
		}

	}
	
	private void compressFolder(File rootTargetDir, File targetDir, JarOutputStream out)  {

		String[] entries;
		byte[] buffer = new byte[4096];
		int bytes_read;
		FileInputStream in = null;
		File fileToCompress = null;
		
		logger.trace("IN");
		
		try {
			entries = targetDir.list();

			for (int i = 0; i < entries.length; i++) {
				fileToCompress = new File(targetDir, entries[i]);
				logger.trace("Compress file [{}]", fileToCompress);
				if (fileToCompress.isDirectory()) {
					compressFolder(rootTargetDir, fileToCompress, out);
				} else {
					in = new FileInputStream(fileToCompress);
					String fileToCompressAbsolutePath = fileToCompress.getAbsolutePath();
					String binDirAbsolutePath = rootTargetDir.getAbsolutePath();
					String relativeFileName = fileToCompress.getName();
					if (fileToCompressAbsolutePath.lastIndexOf(binDirAbsolutePath) != -1) {
						int index = fileToCompressAbsolutePath.lastIndexOf(binDirAbsolutePath);
						int len = binDirAbsolutePath.length();
						relativeFileName = fileToCompressAbsolutePath.substring(index + len + 1);
					}
					
					relativeFileName = relativeFileName.replaceAll("\\\\", "/");
					
					JarEntry entry = new JarEntry(relativeFileName);
					out.putNextEntry(entry);
					while ((bytes_read = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytes_read);
					}
					in.close();
					logger.trace("File compressed into [{}]", entry);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while compressing sub folder [" + targetDir +"]", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				throw new RuntimeException("An error occurred while closing file [" + fileToCompress  + "]",e);
			}
			logger.trace("OUT");
		}
	}
}
