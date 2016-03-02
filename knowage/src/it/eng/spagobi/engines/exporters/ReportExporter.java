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



package it.eng.spagobi.engines.exporters;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.utilities.ExecutionProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class ReportExporter {

	private static transient Logger logger=Logger.getLogger(ReportExporter.class);

	
	
	
	/**  return a PDF file containg the result of the jasper report execution
	 * 
	 * @param obj : the biObject to export
	 * @param profile : user Profile
	 * @return
	 */

	public File getReport(BIObject obj, IEngUserProfile profile, String output){
		logger.debug("IN");
		File toReturn = null;
		if (obj == null){
			logger.error("object is null");
			return null;
		}

		ExecutionProxy proxy = new ExecutionProxy();
		proxy.setBiObject(obj);

		byte[] returnByteArray = proxy.exec(profile, "SDK", output);
		
		String fileExtension = proxy.getFileExtensionFromContType(proxy.getReturnedContentType());

		if(returnByteArray == null || returnByteArray.length==0){
			logger.error("error during execution; null result from execution Proxy");
			return null;
		}

		// identity string for object execution
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuid_local = uuidGen.generateTimeBasedUUID();
		String executionId = uuid_local.toString();
		executionId = executionId.replaceAll("-", "");


		FileOutputStream fos=null;

		try{
			// file creation
			//Create temp file
			String dirS=System.getProperty("java.io.tmpdir");
			String dirSS=dirS+"/reportExport";;
			File dir = new File(dirSS);
			dir.mkdirs();
			
			logger.debug("Create Temp File");

			toReturn=File.createTempFile(obj.getLabel()+executionId, fileExtension, dir);
			fos=new FileOutputStream(toReturn);
			fos.write(returnByteArray);

		}
		catch (IOException e) {
			logger.error("error in writing the file", e);	
			return null;
		}
		finally {
			try{
				fos.flush();
				fos.close();
			}
			catch (IOException e) {
				logger.error("IO Exception: could not close the output stream",e);	
			}
			//tmpFile.delete();
		}
		
		logger.debug("OUT");				
		return toReturn;



	}

}
