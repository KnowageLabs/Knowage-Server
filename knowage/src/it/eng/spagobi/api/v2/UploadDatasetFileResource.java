package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/selfservicedataset")
@ManageAuthorization
public class UploadDatasetFileResource extends AbstractSpagoBIResource {
	private static transient Logger logger = Logger.getLogger(UploadDatasetFileResource.class);

	private static final String UPLOADED_FILE = "UPLOADED_FILE";
	private static final String SKIP_CHECKS = "SKIP_CHECKS";
	private static final String DATASET_FILE_MAX_SIZE = "DATASET_FILE_MAX_SIZE";

	String fileExtension = "";

	@Path("/fileupload")
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	public Response uploadFileDataset(@Context HttpServletRequest req) {
		

	
			try {

				FileItem uploaded = handleMultipartForm(req);

				Object skipChecksObject = null;
				Boolean skipChecks = false;
				if (skipChecksObject != null) {
					skipChecks = ((String) skipChecksObject).equals("on");
				}
				if (uploaded == null) {
					throw new SpagoBIServiceException(getActionName(), "No file was uploaded");
				}

				checkDatasetFileMaxSize(uploaded, getUserProfile());

				// check if the file is zip or gz
				uploaded = checkArchiveFile(uploaded);

				checkUploadedFile(uploaded);

				File file = checkAndCreateDir(uploaded);

				/*
				 * if(!skipChecks){ checkFile(uploaded, file); }
				 */

				logger.debug("Saving file...");
				saveFile(uploaded, file);
				logger.debug("File saved");

				return Response.ok().build();
			} catch (Throwable t) {
				logger.error("Error while uploading dataset file", t);
				return Response.serverError().build();
			} finally {
				logger.debug("OUT");
			}
	
	
		

	}
	
	private FileItem handleMultipartForm(HttpServletRequest request) 
	    	throws Exception{
	    	
	    	// Create a factory for disk-based file items
	    	FileItemFactory factory = new DiskFileItemFactory();
	    	
	    	// This is done to make upload work in Unix solaris
	    	//((DiskFileItemFactory)factory).setSizeThreshold(5242880);
	        
	    	// Create a new file upload handler
	    	ServletFileUpload upload = new ServletFileUpload(factory);

	    	//upload.setFileSizeMax(5242880);
	    	//upload.setSizeMax(5242880);
	    	
	    	
	        // Parse the request
	        List fileItems = upload.parseRequest(request);
	        Iterator iter = fileItems.iterator();
	        while (iter.hasNext()) {
	            FileItem item = (FileItem) iter.next();

	            if (item.isFormField()) {
	                String name = item.getFieldName();
	                String value = item.getString();
	            } else {
	                return item;
	            }
	        }
	        return null;
	    }

	private void checkUploadedFile(FileItem uploaded) {

		logger.debug("IN");
		try {

			// check if the uploaded file is empty
			if (uploaded.getSize() == 0) {
				throw new SpagoBIServiceException(getActionName(), "The uploaded file is empty");
			}

			fileExtension = uploaded.getName().lastIndexOf('.') > 0 ? uploaded.getName().substring(uploaded.getName().lastIndexOf('.') + 1) : null;
			logger.debug("File extension: [" + fileExtension + "]");

			// check if the extension is valid (XLS, CSV)
			if (!"CSV".equalsIgnoreCase(fileExtension) && !"XLS".equalsIgnoreCase(fileExtension)) {
				throw new SpagoBIServiceException(getActionName(), "The uploaded file has an invalid extension. Choose a CSV or XLS file.");
			}

		} finally {
			logger.debug("OUT");
		}
	}

	private File checkAndCreateDir(FileItem uploaded) {
		logger.debug("IN");
		try {
			String fileName = SpagoBIUtilities.getRelativeFileNames(uploaded.getName());
			String resourcePath = SpagoBIUtilities.getResourcePath();
			File datasetFileDir = new File(resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + "temp");
			if (!datasetFileDir.exists()) {
				// Create Directory \dataset\files\temp under \resources if
				// don't exists
				boolean mkdirResult = datasetFileDir.mkdirs();
				if (!mkdirResult) {
					throw new SpagoBIServiceException(getActionName(), "Cannot create \\dataset\\files directory into server resources");
				}
			}

			return new File(datasetFileDir, fileName);
		} catch (Throwable t) {
			logger.error("Error while saving file into server: " + t);
			throw new SpagoBIServiceException(getActionName(), "Error while saving file into server", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void saveFile(FileItem uploaded, File saveTo) {
		logger.debug("IN");
		try {
			uploaded.write(saveTo);
		} catch (Throwable t) {
			logger.error("Error while saving file into server: " + t);
			throw new SpagoBIServiceException(getActionName(), "Error while saving file into server", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void checkFile(FileItem uploaded, File saveTo) {

		int used = getDatasetsNumberUsingFile(uploaded);
		if (used > 0) {
			throw new SpagoBIServiceException(getActionName(), "{NonBlockingError: true, error:\"USED\", used:\"" + used + "\"}");
		}
		boolean alreadyExist = checkFileIfExist(saveTo);
		if (alreadyExist) {
			throw new SpagoBIServiceException(getActionName(), "{NonBlockingError: true, error:\"EXISTS\"}");
		}

	}

	private boolean checkFileIfExist(File file) {
		return (file.exists());
	}

	/**
	 * Gets the number of datasets that use the fiel
	 *
	 * @param fileName
	 *            the name of the file
	 * @return the number of datasets using the file
	 * @throws EMFUserError
	 */
	private int getDatasetsNumberUsingFile(FileItem uploaded) {
		String configuration;
		String fileName = SpagoBIUtilities.getRelativeFileNames(uploaded.getName());
		String fileToSearch = "\"fileName\":\"" + fileName + "\"";
		IDataSet iDataSet;
		int datasetUisng = 0;

		try {
			IDataSetDAO ds = DAOFactory.getDataSetDAO();
			List<IDataSet> datasets = ds.loadDataSets();
			if (datasets != null) {
				for (Iterator<IDataSet> iterator = datasets.iterator(); iterator.hasNext();) {
					iDataSet = iterator.next();
					configuration = iDataSet.getConfiguration();
					if (configuration.indexOf(fileToSearch) >= 0) {
						datasetUisng++;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error checking if the file is used by other datasets ", e);
			throw new SpagoBIServiceException(getActionName(), "Error checking if the file is used by other datasets ", e);
		}
		return datasetUisng;

	}

	private FileItem checkArchiveFile(FileItem uploaded) {

		// check if the uploaded file is empty
		if (uploaded.getSize() == 0) {
			throw new SpagoBIServiceException(getActionName(), "The uploaded file is empty");
		}

		fileExtension = uploaded.getName().lastIndexOf('.') > 0 ? uploaded.getName().substring(uploaded.getName().lastIndexOf('.') + 1) : null;

		if ("ZIP".equalsIgnoreCase(fileExtension)) {
			logger.debug("Decompress zip file");
			uploaded = unzipUploadedFile(uploaded);

		} else if ("GZ".equalsIgnoreCase(fileExtension)) {
			logger.debug("Decompress gzip file");
			uploaded = ungzipUploadedFile(uploaded);
		}

		return uploaded;
	}

	private FileItem unzipUploadedFile(FileItem uploaded) {

		logger.debug("Method unzipUploadedFile(): Start");

		FileItem tempFileItem = null;

		try {
			ZipInputStream zippedInputStream = new ZipInputStream(uploaded.getInputStream());
			ZipEntry zipEntry = null;

			if ((zipEntry = zippedInputStream.getNextEntry()) != null) {

				String zipItemName = zipEntry.getName();

				logger.debug("Method unzipUploadedFile(): Zip entry [ " + zipItemName + " ]");

				if (zipEntry.isDirectory()) {
					throw new SpagoBIServiceException(getActionName(), "The uploaded file is a folder. Zip directly the file.");
				}

				DiskFileItemFactory factory = new DiskFileItemFactory();
				tempFileItem = factory.createItem(uploaded.getFieldName(), "application/octet-stream", uploaded.isFormField(), zipItemName);
				OutputStream tempFileItemOutStream = tempFileItem.getOutputStream();

				IOUtils.copy(zippedInputStream, tempFileItemOutStream);

				tempFileItemOutStream.close();
			}

			zippedInputStream.close();

			logger.debug("Method unzipUploadedFile(): End");
			return tempFileItem;

		} catch (Throwable t) {
			logger.error("Error while unzip file. Invalid archive file: " + t);
			throw new SpagoBIServiceException(getActionName(), "Error while unzip file. Invalid archive file", t);

		}
	}

	private FileItem ungzipUploadedFile(FileItem uploaded) {

		logger.debug("Method ungzipUploadedFile(): Start");

		FileItem tempFileItem = null;

		try {
			GZIPInputStream zippedInputStream = new GZIPInputStream(uploaded.getInputStream());

			String gzipItemName = uploaded.getName().lastIndexOf('.') > 0 ? uploaded.getName().substring(0, uploaded.getName().lastIndexOf('.')) : null;

			if (gzipItemName == null || gzipItemName.equals("")) {
				throw new SpagoBIServiceException(getActionName(), "Invalid filename for gzip file");
			}

			DiskFileItemFactory factory = new DiskFileItemFactory();
			tempFileItem = factory.createItem(uploaded.getFieldName(), "application/octet-stream", uploaded.isFormField(), gzipItemName);
			OutputStream tempFileItemOutStream = tempFileItem.getOutputStream();

			IOUtils.copy(zippedInputStream, tempFileItemOutStream);

			tempFileItemOutStream.close();

			zippedInputStream.close();

			logger.debug("Method ungzipUploadedFile(): End");
			return tempFileItem;

		} catch (Throwable t) {
			logger.error("Error while unzip file. Invalid archive file: " + t);
			throw new SpagoBIServiceException(getActionName(), "Error while unzip file. Invalid archive file", t);

		}
	}

	private void checkDatasetFileMaxSize(FileItem uploaded, UserProfile userProfile) {

		logger.debug("Method checkDatasetFileMaxSize(): Start");

		// first check that the user profile isn't null
		if (userProfile == null) {
			throw new SpagoBIServiceException(getActionName(), "Impossible to check [ " + DATASET_FILE_MAX_SIZE + "] attribute without a valide user profile");
		}

		try {

			if (userProfile.getUserAttributes().containsKey(DATASET_FILE_MAX_SIZE)) {

				// the user profile contains the attribute that defines dataset
				// file max size

				logger.debug("Method checkDatasetFileMaxSize(): Attribute [ " + DATASET_FILE_MAX_SIZE + " ] defined for [" + userProfile.getUserName()
						+ " ] profile. Validation needed");

				String datasetFileMaxSizeStr = (String) userProfile.getUserAttribute(DATASET_FILE_MAX_SIZE);

				if (!datasetFileMaxSizeStr.equals("")) {

					long datasetFileMaxSize = Long.parseLong(datasetFileMaxSizeStr);

					if (uploaded.getSize() > datasetFileMaxSize) {

						throw new SpagoBIServiceException(getActionName(), "The uploaded file exceeds the maximum size assigned to the user, that is "
								+ datasetFileMaxSize + " bytes");
					}
				} else {

					checkDatasetFileMaxSizeSystem(uploaded);
				}

			} else {
				logger.debug("Method checkDatasetFileMaxSize(): Attribute [ " + DATASET_FILE_MAX_SIZE + " ] not defined for [" + userProfile.getUserName()
						+ " ] profile. Check default system max dimension");
				// check if the uploaded file exceeds the maximum default
				// dimension
				checkDatasetFileMaxSizeSystem(uploaded);
			}

			logger.debug("Method checkDatasetFileMaxSize(): End");
		} catch (Throwable t) {
			logger.error("Error retrieving user attribute [ " + DATASET_FILE_MAX_SIZE + " ] " + t);
			throw new SpagoBIServiceException(getActionName(), "The uploaded file exceeds the maximum size", t);

		}

	}

	private void checkDatasetFileMaxSizeSystem(FileItem uploaded) {
		int maxSize = GeneralUtilities.getDataSetFileMaxSize();
		if (uploaded.getSize() > maxSize) {
			throw new SpagoBIServiceException(getActionName(), "The uploaded file exceeds the maximum size, that is " + maxSize + " bytes");
		}
	}

	private String getActionName() {
		return "FILE_DATASET_UPLOAD";
	}
}
