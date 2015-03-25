/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.files.service;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class DownloadZipAction extends AbstractBaseHttpAction{ //AbstractHttpAction {

	static private Logger logger = Logger.getLogger(DownloadZipAction.class);

	static byte[] buf = new byte[1024]; 

	public static final String DIRECTORY = "DIRECTORY";
	public static final String BEGIN_DATE = "BEGIN_DATE";
	public static final String BEGIN_TIME = "BEGIN_TIME";
	public static final String END_DATE = "END_DATE";
	public static final String END_TIME = "END_TIME";
	public static final String PREFIX1 = "PREFIX1";
	public static final String PREFIX2 = "PREFIX2";

	public static final String PARAMETERS_DATE_FORMAT="dd/MM";
	public static final String PARAMETERS_TIME_FORMAT="hh:mm";
	public static final String PARAMETERS_FORMAT="ddMM hhmm";
	public static final String FILES_DATE_FORMAT="MMddhhmmss";

	public static final String SERVICE_NAME = "DOWNLOAD_ZIP";

	public String getActionName(){return SERVICE_NAME;}


	public void service(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		freezeHttpResponse();

		HttpServletRequest httpRequest = getHttpRequest();
		HttpServletResponse httpResponse = getHttpResponse();


		// get Attribute DATE, MINUTE, DIRECTORY
		String directory = (String) request.getAttribute(DIRECTORY);

		String beginDate = (String) request.getAttribute(BEGIN_DATE);
		String beginTime = (String) request.getAttribute(BEGIN_TIME);
		String endDate = (String) request.getAttribute(END_DATE);
		String endTime = (String) request.getAttribute(END_TIME);
		String prefix1 = (request.getAttribute(PREFIX1) != null)?(String) request.getAttribute(PREFIX1)+"_": "";
		String prefix2 = (request.getAttribute(PREFIX2) != null)?(String) request.getAttribute(PREFIX2)+"_": "";
		//String prefix1 = (request.getAttribute(PREFIX1) != null)?(String) request.getAttribute(PREFIX1): "";
		//String prefix2 = (request.getAttribute(PREFIX2) != null)?(String) request.getAttribute(PREFIX2): "";

		// build begin Date			

		if(directory==null){
			logger.error("search directory not specified");
			throw new SpagoBIServiceException(SERVICE_NAME, "Missing directory parameter");
		}

		if(beginDate==null || beginTime==null){
			throw new SpagoBIServiceException(SERVICE_NAME, "Missing begin date parameter");
		}

		if(endDate==null || endTime==null){
			throw new SpagoBIServiceException(SERVICE_NAME, "Missing end date parameter");
		}

		try {

			// remove / from days name
			beginDate = beginDate.replaceAll("/", "");
			endDate = endDate.replaceAll("/", "");
			beginTime = beginTime.replaceAll(":", "");
			endTime = endTime.replaceAll(":", "");

			String beginWhole=beginDate+" "+beginTime;
			String endWhole=endDate+" "+endTime;

			java.text.DateFormat myTimeFormat = new java.text.SimpleDateFormat(PARAMETERS_FORMAT);

			Date begin=myTimeFormat.parse(beginWhole);
			Date end=myTimeFormat.parse(endWhole);


			logger.debug("earch file from begin date " + begin.toString() + " to end date " + end.toString());

			directory=directory.replaceAll("\\\\", "/");

			File dir=new File(directory);
			if(!dir.isDirectory()){
				logger.error("Not a valid directory specified");
				return;
			}


			// get all files that has to be zipped
			Vector filesToZip = searchDateFiles(dir, begin, end, prefix1 + prefix2);
			if (filesToZip.size() == 0){
				/*throw new Exception ("Warning: Files not found with these parameters: <p><br>" +
									 " <b>Directory:</b> " + dir + "<p>" +
									 " <b>Begin date:</b> " + begin + "<p>" + 
									 " <b>End date:</b> " + end + "<p>" + 
									 " <b>Prefix:</b> " + prefix1 + prefix2 ); */
				throw new Exception ("Warning: Missing files in specified interval!");
			}

			Date today=(new Date());
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yy hh:mm:ss");
			//date = (Date)formatter.parse("11-June-07");    
			String randomName = formatter.format(today);			
			randomName=randomName.replaceAll(" ", "_");
			randomName=randomName.replaceAll(":", "-");

			String directoryZip=System.getProperty("java.io.tmpdir");			
			if (!directoryZip.endsWith(System.getProperty("file.separator"))) {
				directoryZip += System.getProperty("file.separator");
			}
			String fileZip=randomName+".zip";
			String pathZip=directoryZip+fileZip;
			pathZip=pathZip.replaceAll("\\\\","/");			
			directoryZip=directoryZip.replaceAll("\\\\", "/");

//			String directoryZip="C:/logs";
//			String fileZip="prova.zip";
//			String pathZip=directoryZip+"/"+fileZip;

			createZipFromFiles(filesToZip, pathZip, directory);

			//Vector<File> filesToZip = searchDateFiles(dir, beginDate, endDate)

			manageDownloadZipFile(httpRequest, httpResponse, directoryZip, fileZip);

			//manageDownloadExportFile(httpRequest, httpResponse);
		} catch (Throwable t) {
			logger.error("Error in writing the zip ",t);
			throw new SpagoBIServiceException(SERVICE_NAME, t.getMessage() , t);
			/* this manage defines a file with the error message and returns it.
			try{							
				File file = new File("exception.txt");
				String text = t.getMessage() + " \n" + (( t.getStackTrace()!=null)?t.getStackTrace():"");
				FileUtils.writeStringToFile(file, text, "UTF-8");
				writeBackToClient(file, null, false, "exception.txt", "text/plain");								
			}catch(Throwable t2){
				logger.error("Error in defining error file ",t2);
				throw new SpagoBIServiceException(SERVICE_NAME, "Server Error in writing the zip", t2);
			}
			 */
		}finally {
			logger.debug("OUT");
		}
	}

	public String generateMatch(String date, String time){
		logger.debug("IN");
		String toReturn=null;

		date=date.replaceAll("\\\\", "-");
		date=date.replaceAll("/", "-");
		time=time.replaceAll(":", "-");
		toReturn=date+" "+time;
		logger.debug("OUT");

		return toReturn;
	}


//	public void searchDateFiles(Vector<String> vector, File  file, String match){
//	logger.debug("IN");
//	if(file.isDirectory() && file.list()!=null && file.list().length!=0){
//	for (int i = 0; i < file.list().length; i++) {
//	String childFileName=file.list()[i];
//	File childFile=new File(file.getAbsolutePath()+"/"+childFileName);
//	if(!childFile.exists()){
//	logger.warn(childFile.getName()+" not exists");
//	}
//	else{
//	searchDateFiles(vector, childFile, match);
//	}
//	}
//	}
//	else{
//	String fileName=file.getName();
//	if(fileName.indexOf(match)!=-1){
//	vector.add(file.getAbsolutePath());
//	}
//	}
//	logger.debug("OUT");
//	}





	public void createZipFromFiles(Vector<File> files, String outputFileName, String folderName) throws IOException{
		logger.debug("IN");
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFileName)); 
		// Compress the files 
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();

			FileInputStream in = new FileInputStream(file); 

			String fileName = file.getName();
			// The name to Inssert: remove the parameter folder
//			int lastIndex=folderName.length();
//			String fileToInsert=fileName.substring(lastIndex+1);

			logger.debug("Adding to zip entry "+fileName);
			ZipEntry zipEntry=new ZipEntry(fileName);

			// Add ZIP entry to output stream. 
			out.putNextEntry(zipEntry); 

			// Transfer bytes from the file to the ZIP file 
			int len; 
			while ((len = in.read(buf)) > 0) 
			{ 
				out.write(buf, 0, len); 
			} 
			// Complete the entry 
			out.closeEntry(); 
			in.close(); 
		} 
		// Complete the ZIP file 
		out.close(); 
		logger.debug("OUT");
	}

	/**
	 * Handle a download request of an importation zip file. Reads the file, sends it as
	 * an http response attachment.
	 */
	private void manageDownloadZipFile(HttpServletRequest request, HttpServletResponse response, String folderName, String exportFileName) {
		logger.debug("IN");
		try {
			String fileExtension = "zip";
			manageDownload(exportFileName, fileExtension, folderName, response, false);
		} catch (Exception e) {
			logger.error("Error while downloading importation log file", e);
		} finally {
			logger.debug("OUT");
		}
	}


	private void manageDownload(String fileName, String fileExtension, String folderPath, HttpServletResponse response, boolean deleteFile) {
		logger.debug("IN");
		try {
			File exportedFile = new File(folderPath + "/" + fileName);

			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileExtension + "\";");
			byte[] exportContent = "".getBytes();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(exportedFile);
				exportContent = GeneralUtilities.getByteArrayFromInputStream(fis);
			} catch (IOException ioe) {
				logger.error("Cannot get bytes of the download file", ioe);
			}
			response.setHeader("Content-Disposition","attachment; filename=\"" + fileName + "\";");		
			response.setContentLength(exportContent.length);
			response.getOutputStream().write(exportContent);
			response.getOutputStream().flush();
			if (fis != null)
				fis.close();
			if (deleteFile) {
				exportedFile.delete();
			}
		} catch (IOException ioe) {
			logger.error("Cannot flush response", ioe);
		} finally {
			logger.debug("OUT");
		}
	}

	/** Extract Date from files name that are in format W0301140201.log where date is first March at time 14:02:01
	 * 
	 * @param fileName
	 * @return
	 * @throws ParseException
	 */

	static public Date extractDate(String fileName, String prefix) throws ParseException{
		// remove prefix
		//fileName = fileName.substring(1);
		int prefixToRemove = prefix.length();
		fileName = fileName.substring(prefixToRemove);

		// remove extension
		// int point=fileName.indexOf('.');
		// fileName = fileName.substring(0, point);
		int dateLenghtToRemove = FILES_DATE_FORMAT.length();

		fileName = fileName.substring(0, dateLenghtToRemove);

		java.text.DateFormat myTimeFormat = new java.text.SimpleDateFormat(FILES_DATE_FORMAT);
		Date timeFile=myTimeFormat.parse(fileName);
		return timeFile;

	}


	/** Search all files in directory wich in their name has timestamp from begin date to end date
	 * 
	 * @param vector
	 * @param dir
	 * @param beginDate
	 * @param endDate
	 * @return
	 * @throws ParseException
	 */

	static public Vector<File> searchDateFiles(File  dir, Date beginDate, Date endDate, String prefix) {

		Vector<File> toReturn=new Vector<File>();

		// if directory ha files 
		if(dir.isDirectory() && dir.list()!=null && dir.list().length!=0){

//			serach only files starting with prefix

			// get sorted array
			File[] files=getSortedArray(dir, prefix);

			if (files == null){
				throw new SpagoBIServiceException(SERVICE_NAME, "Missing files in specified interval");
			}

			// cycle on all files
			boolean exceeded = false;
			for (int i = 0; i < files.length && !exceeded; i++) {
				File childFile = files[i];

				// extract date from file Name
				Date fileDate=null;
				try {
					fileDate = extractDate(childFile.getName(), prefix);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					logger.error("error in parsing log file date, file will be ignored!",e);
					continue;
				}

				// compare beginDate and timeDate, if previous switch file, if later see end date
				// compare then end date, if previous then endDate add file, else exit

				// if fileDate later than begin Date
				if(fileDate !=null  && fileDate.after(beginDate)){
					// if end date later than file date
					if(endDate.after(fileDate)){
						// it is in the interval, add to list!
						toReturn.add(childFile);
					}
					else { // if file date is later then end date, we are exceeding interval
						exceeded = true;
					}

				}
			}
		}
		return toReturn;
	}


	/** getSortedArray.
	 * 
	 * @return File[]
	 */
	static public final File[] getSortedArray(File directory, String prefix) {
		File [] allFiles = directory.listFiles();
		Vector labelFilesVector = new Vector<File>();
		for (int i = 0; i < allFiles.length; i++) {
			String fileName = allFiles[i].getName();
			if(fileName.startsWith(prefix)){
				labelFilesVector.add(allFiles[i]);
			}
		}

		if(labelFilesVector.size() == 0){
			return null;
		}

		// I need an array
		int j = 0;
		File[] labelFiles = new File[labelFilesVector.size()];
		for (Iterator iterator = labelFilesVector.iterator(); 
		iterator.hasNext();) {
			File object = (File) iterator.next();
			labelFiles[j] = object;
			j++;
		}		


		Arrays.sort(labelFiles, new Comparator()
		{
			public int compare(final Object o1, final Object o2) {

				if (((File) o1).lastModified() > ((File) o2).lastModified()) {
					return +1;
				} else if (((File) o1).lastModified() 
						< ((File) o2).lastModified()) {
					return -1;
				} else {
					return 0;
				}
			}

		});

		return labelFiles;
	}



}
