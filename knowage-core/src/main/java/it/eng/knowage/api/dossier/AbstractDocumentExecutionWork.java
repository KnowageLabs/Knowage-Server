package it.eng.knowage.api.dossier;

import static it.eng.spagobi.commons.constants.ConfigurationConstants.SPAGOBI_SPAGOBI_SERVICE_JNDI;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import commonj.work.Work;
import it.eng.knowage.api.dossier.utils.FileUtilities;
import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.knowage.engines.dossier.template.AbstractDossierTemplate;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;
import it.eng.knowage.engines.dossier.template.report.Report;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.DocumentMetadataProperty;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.dossier.dao.ISbiDossierActivityDAO;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class AbstractDocumentExecutionWork extends DossierExecutionClient implements Work {

	private static final Logger LOGGER = LogManager.getLogger(AbstractDocumentExecutionWork.class);
	public static final String PREPARED = "PREPARED";
	public static final String STARTED = "STARTED";
	public static final String DOWNLOAD = "DOWNLOAD";
	public static final String ERROR = "ERROR";
	protected List<String> imageNames = new ArrayList<>();
	protected IEngUserProfile userProfile;
	protected List<BIObjectPlaceholdersPair> documents;
	protected boolean completeWithoutError = false;
	protected Integer progressThreadId;
	protected String randomKey;
	IProgressThreadDAO progressThreadDAO;
	protected AbstractDossierTemplate dossierTemplate = null;
	protected JSONObject jsonObjectTemplate = new JSONObject();

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	protected void runInternal(JSONObject jsonObjectTemplate) {
		LOGGER.debug("IN");

		ProgressThreadManager progressThreadManager = null;
		IObjMetadataDAO metaDAO = null;
		IObjMetacontentDAO contentDAO = null;

		Thread thread = Thread.currentThread();
		Long threadId = thread.getId();

		LOGGER.debug("Started thread Id {} from user id: {}", threadId, ((UserProfile) userProfile).getUserId());

		Integer totalDocs = documents.size();
		LOGGER.debug("# of documents: {}", totalDocs);

		progressThreadManager = new ProgressThreadManager();
		progressThreadManager.setStatusStarted(progressThreadId);
		try {

			metaDAO = DAOFactory.getObjMetadataDAO();
			contentDAO = DAOFactory.getObjMetacontentDAO();

		} catch (Exception e) {
			LOGGER.error("Error setting DAO");
			progressThreadManager.deleteThread(progressThreadId);
			throw new SpagoBIServiceException("Error setting DAO", e);
		}

		BIObject biObject = null;
		try {
			/**/
			String userUniqueIdentifier = (String) userProfile.getUserUniqueIdentifier();

			ObjectMapper objectMapper = new ObjectMapper();
			String dossierTemplateJson = objectMapper.writeValueAsString(dossierTemplate);
			Map<String, String> imagesMap = null;

			Set<String> executedDocuments = new HashSet<>();
			String path = SpagoBIUtilities.getResourcePath() + File.separator + "dossierExecution" + File.separator;

			this.validImage(dossierTemplate.getReports());

			ISbiDossierActivityDAO daoAct = DAOFactory.getDossierActivityDao();
			daoAct.setUserProfile(userProfile);
			DossierActivity activity = null;

			while (activity == null) {
				activity = daoAct.loadActivityByProgressThreadId(progressThreadId);
			}
			String dbArray = activity.getConfigContent();
			JSONArray jsonArray = null;

			HashMap<String, String> paramMap = new HashMap<>();

			if (dbArray != null && !dbArray.isEmpty()) {
				jsonArray = new org.json.JSONArray(dbArray);
			} else {
				jsonArray = new JSONArray();
			}
			jsonArray.put(jsonObjectTemplate);

			for (Report reportToUse : dossierTemplate.getReports()) {

				String cockpitDocument = reportToUse.getLabel();

				String imageName = reportToUse.getImageName(); // image format is .png

				LOGGER.debug("executing post service to execute documents");
				biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(cockpitDocument);

				if (biObject == null) { // it should mean that a cockpit doesn't exist: template error
					throw new SpagoBIRuntimeException(
							"Template error: the cockpit " + cockpitDocument + " doesn't exist, check the template");
				}
				Integer docId = biObject.getId();

				Collection<String> roles = userProfile.getRoles();
				for (String role : roles) {

					if (!ObjectsAccessVerifier.canExec(biObject, userProfile)) {
						String message = "user " + ((UserProfile) userProfile).getUserName()
								+ " cannot execute document " + biObject.getName();
						throw new SpagoBIRuntimeException(message);
					}

					String docName = biObject.getName();

					String hostUrl = getServiceHostUrl();

					StringBuilder serviceUrlBuilder = new StringBuilder();
					serviceUrlBuilder.append(hostUrl);
					serviceUrlBuilder.append("/knowagecockpitengine/api/1.0/pages/execute/png?");
					serviceUrlBuilder.append("user_id=");
					serviceUrlBuilder.append(userUniqueIdentifier);
					serviceUrlBuilder.append("&DOCUMENT_LABEL=");
					serviceUrlBuilder.append(cockpitDocument);
					serviceUrlBuilder
							.append("&DOCUMENT_OUTPUT_PARAMETERS=%5B%5D&DOCUMENT_IS_VISIBLE=true&SBI_EXECUTION_ROLE=");
					serviceUrlBuilder.append(URLEncoder.encode(role, StandardCharsets.UTF_8.toString()));
					serviceUrlBuilder.append("&DOCUMENT_DESCRIPTION=&document=");
					serviceUrlBuilder.append(docId);
					serviceUrlBuilder.append("&IS_TECHNICAL_USER=true&DOCUMENT_NAME=");
					serviceUrlBuilder.append(docName);
					serviceUrlBuilder.append(
							"&NEW_SESSION=TRUE&SBI_ENVIRONMENT=DOCBROWSER&IS_FOR_EXPORT=true&documentMode=VIEW&export=true&outputType=PNG");

					Locale locale = GeneralUtilities.getDefaultLocale();

					serviceUrlBuilder.append("&knowage_sys_country=" + locale.getCountry());
					serviceUrlBuilder.append("&knowage_sys_language=" + locale.getLanguage());

					serviceUrlBuilder.append("&SBI_LANGUAGE=" + locale.getLanguage());
					serviceUrlBuilder.append("&SBI_COUNTRY=" + locale.getCountry());
					serviceUrlBuilder.append("&SBI_SCRIPT=" + locale.getScript());

					RenderOptions renderOptions = RenderOptions.defaultOptions();
					if (reportToUse.getSheetHeight() != null && !reportToUse.getSheetHeight().isEmpty()
							&& reportToUse.getSheetWidth() != null && !reportToUse.getSheetWidth().isEmpty()) {
						serviceUrlBuilder.append("&pdfWidth=" + Integer.valueOf(reportToUse.getSheetWidth()));
						serviceUrlBuilder.append("&pdfHeight=" + Integer.valueOf(reportToUse.getSheetHeight()));
					}

					if (reportToUse.getDeviceScaleFactor() != null && !reportToUse.getDeviceScaleFactor().isEmpty()) {
						serviceUrlBuilder
								.append("&pdfDeviceScaleFactor=" + Double.valueOf(reportToUse.getDeviceScaleFactor()));
					} else {
						serviceUrlBuilder.append("&pdfDeviceScaleFactor="
								+ Double.valueOf(renderOptions.getDimensions().getDeviceScaleFactor()));
					}
					JSONObject templateJSON = new JSONObject(new String(biObject.getActiveTemplate().getContent()));
					boolean isMultiSheet = false;
					if (templateJSON.has("sheets") && templateJSON.getJSONArray("sheets").length() > 1) {
						isMultiSheet = true;
					}
					serviceUrlBuilder.append("&isMultiSheet=" + isMultiSheet);
					String serviceUrl = addParametersToServiceUrl(progressThreadId, biObject, reportToUse,
							serviceUrlBuilder, jsonArray, paramMap);

					if (executedDocuments.contains(serviceUrl)) {
						progressThreadManager.incrementPartial(progressThreadId);
						break;
					}

					// Images creation
					Response images = executePostService(null, serviceUrl, userUniqueIdentifier, MediaType.TEXT_HTML,
							dossierTemplateJson);
					byte[] responseAsByteArray = images.readEntity(byte[].class);

					List<Object> list = images.getMetadata().get("Content-Type");
					Iterator<Object> it = list.iterator();
					boolean isZipped = false;
					while (it.hasNext()) {
						String contentType = (String) it.next();

						isZipped = contentType.contains("application/zip");
						break;

					}

					// DOC with images replaced creation
					imagesMap = new HashMap<>();
					if (isZipped) {
						String message = "Document has more than one single sheet. Screenshot is replaced with an empty image.";
						LOGGER.debug(message);
						handleAllPicturesFromZipFile(responseAsByteArray, randomKey, imagesMap, reportToUse);

					} else {

						File f = FileUtilities.createFile(imageName, ".png", randomKey, new ArrayList<>());
						FileOutputStream outputStream = new FileOutputStream(f);
						outputStream.write(responseAsByteArray);
						outputStream.close();
						imagesMap.put(imageName, path);
					}

					progressThreadManager.incrementPartial(progressThreadId);
					LOGGER.debug("progress Id incremented");
					executedDocuments.add(serviceUrl);
					break;
				}

			}
			// Activity creation
			imageNames.clear();
			ParametersDecoder decoder = new ParametersDecoder();
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				String metadataMessage = entry.getKey() + "=" + decoder.decodeParameter(entry.getValue());
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("TYPE", "PARAMETER");
				jsonObject.put("MESSAGE", metadataMessage);
				jsonArray.put(jsonObject);
			}
			activity.setConfigContent(jsonArray.toString());
			daoAct.updateActivity(activity);

			progressThreadManager.setStatusDownload(progressThreadId);
			LOGGER.debug("Thread row in database set as download state");

			LOGGER.debug("OUT");

			/**/
		} catch (Exception e) {
			progressThreadManager.setStatusError(progressThreadId);
			createErrorFile(biObject, e);
			LOGGER.error("Error while creating dossier activity", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} finally {
			LOGGER.debug("OUT");
		}

	}

	protected void setTenant() {
		LOGGER.debug("IN");
		UserProfile profile = (UserProfile) this.getProfile();
		String tenant = profile.getOrganization();
		LOGGER.debug("Tenant : [{}]", tenant);
		TenantManager.setTenant(new Tenant(tenant));
		LOGGER.debug("OUT");
	}

	public void validImage(List<Report> reports) {
		for (Report reportToUse : reports) {
			String image = reportToUse.getImageName();
			if (!imageNames.contains(image)) {
				imageNames.add(image);
			} else {
				throw new SpagoBIRuntimeException("Image names must be different inside template");
			}
		}
	}

	public String addParametersToServiceUrl(Integer progressthreadId, BIObject biObject, Report reportToUse,
			StringBuilder serviceUrlBuilder, JSONArray jsonArray, HashMap<String, String> paramMap)
			throws UnsupportedEncodingException, JSONException {
		List<BIObjectParameter> drivers = biObject.getDrivers();
		if (drivers != null) {
			List<Parameter> parameter = reportToUse.getParameters();
			if (drivers.size() != parameter.size()) {
				throw new SpagoBIRuntimeException(
						"There are a different number of parameters/drivers between document and template");
			}
			Collections.sort(drivers);
			ParametersDecoder decoder = new ParametersDecoder();

			for (BIObjectParameter biObjectParameter : drivers) {
				boolean found = false;
				String value = "";
				String paramName = "";
				for (Parameter templateParameter : parameter) {

					if (templateParameter.getType().equals("dynamic")) {

						if (templateParameter.getValue() != null && !templateParameter.getValue().isEmpty()) {

							// filled by fillParametersValues in DossierExecutionResource
							value = templateParameter.getValue();
							if (decoder.isMultiValues(value) && value.contains("STRING"))
								value.replaceAll("'", "");

							if (biObjectParameter.getParameterUrlName().equals(templateParameter.getUrlName())) {
								paramName = templateParameter.getUrlName();
								serviceUrlBuilder
										.append(String.format("&%s=%s", biObjectParameter.getParameterUrlName(),
												URLEncoder.encode(value, StandardCharsets.UTF_8.toString())));

								// description
								serviceUrlBuilder.append(
										String.format("&%s_description=%s", biObjectParameter.getParameterUrlName(),
												URLEncoder.encode(templateParameter.getUrlNameDescription(),
														StandardCharsets.UTF_8.toString())));
								found = true;
								break;
							}
						}
					} else {
						if (biObjectParameter.getParameterUrlName().equals(templateParameter.getUrlName())) {
							serviceUrlBuilder
									.append(String.format("&%s=%s", biObjectParameter.getParameterUrlName(), URLEncoder
											.encode(templateParameter.getValue(), StandardCharsets.UTF_8.toString())));
							value = templateParameter.getValue();
							paramName = templateParameter.getUrlName();
							if (templateParameter.getUrlNameDescription() == null) {
								throw new SpagoBIRuntimeException(
										"There is no description field inside template parameters. It is mandatory for static types.");

							}
							// description
							serviceUrlBuilder
									.append(String.format("&%s_description=%s", biObjectParameter.getParameterUrlName(),
											URLEncoder.encode(templateParameter.getUrlNameDescription(),
													StandardCharsets.UTF_8.toString())));

							found = true;
							break;

						}
					}
				}
				paramMap.put(paramName, value);
				if (!found && biObjectParameter.isRequired()) {
					throw new SpagoBIRuntimeException(
							"There is no match between document parameters and template parameters.");
				}
			}
		}
		return serviceUrlBuilder.toString();
	}

	public String getServiceHostUrl() {
		String serviceURL = SpagoBIUtilities
				.readJndiResource(SingletonConfig.getInstance().getConfigValue(SPAGOBI_SPAGOBI_SERVICE_JNDI));
		serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf('/'));
		return serviceURL;
	}

	protected void handleAllPicturesFromZipFile(byte[] responseAsByteArray, String randomKey,
			Map<String, String> imagesMap, Report reportToUse) throws IOException {

		String outFolderPath = SpagoBIUtilities.getResourcePath() + File.separator + "dossierExecution" + File.separator
				+ randomKey + File.separator;
		File dossierExDir = new File(SpagoBIUtilities.getResourcePath() + File.separator + "dossierExecution");
		if (!dossierExDir.exists()) {
			dossierExDir.mkdir();
		}
		File outFolder = new File(outFolderPath);

		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(responseAsByteArray));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = FileUtilities.createFile(FilenameUtils.removeExtension(zipEntry.getName()), ".png",
					randomKey, new ArrayList<>());
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();

		// array of supported extensions (use a List if you prefer)
		String[] EXTENSIONS = new String[] { "gif", "png", "bmp" // and other formats you need
		};
		// filter to identify images based on their extensions
		FilenameFilter IMAGE_FILTER = new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {
				for (final String ext : EXTENSIONS) {
					if (name.endsWith("." + ext) && name.startsWith("sheet")) {
						return (true);
					}
				}
				return (false);
			}
		};

		String documentLabel = reportToUse.getLabel();

		if (outFolder.isDirectory()) {
			for (final File f : outFolder.listFiles(IMAGE_FILTER)) {

				try {

					File to = FileUtilities.createFile(FilenameUtils.removeExtension(documentLabel + "_" + f.getName()),
							".png", randomKey, new ArrayList<>());

					FileUtils.copyFile(f, to);
					if (reportToUse.getImageName().contains(FilenameUtils.removeExtension(f.getName()))) {
						imagesMap.put(reportToUse.getImageName(), to.getAbsolutePath());
					} else
						imagesMap.put(reportToUse.getImageName() + "_" + f.getName(), to.getAbsolutePath());

					FileUtils.deleteQuietly(f);

				} catch (final IOException e) {
					// handle errors here
				}
			}

		}
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	private List<DocumentMetadataProperty> getMetaDataAndContent(IObjMetadataDAO metaDao,
			IObjMetacontentDAO metaContentDAO, BIObject obj) throws Exception {
		LOGGER.debug("IN");
		List toReturn = null;

		try {
			DocumentMetadataProperty objMetaDataAndContent = null;
			List<ObjMetadata> allMetas = metaDao.loadAllObjMetadata();
			Map<Integer, ObjMetacontent> values = new HashMap<>();

			List list = metaContentDAO.loadObjOrSubObjMetacontents(obj.getId(), null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				ObjMetacontent content = (ObjMetacontent) iterator.next();
				Integer metaid = content.getObjmetaId();
				values.put(metaid, content);
			}

			for (Iterator iterator = allMetas.iterator(); iterator.hasNext();) {
				ObjMetadata meta = (ObjMetadata) iterator.next();
				objMetaDataAndContent = new DocumentMetadataProperty();
				objMetaDataAndContent.setMetadataPropertyDefinition(meta);
				objMetaDataAndContent.setMetadataPropertyValue(values.get(meta.getObjMetaId()));
				if (toReturn == null)
					toReturn = new ArrayList<DocumentMetadataProperty>();
				toReturn.add(objMetaDataAndContent);
			}

		} catch (Exception e) {
			LOGGER.error("error in retrieving metadata and metacontent for biobj id {}", obj.getId(), e);
			throw e;
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	@Override
	public boolean isDaemon() {
		return false;
	}

	@Override
	public void release() {
	}

	public List getBiObjects() {
		return documents;
	}

	public void setBiObjects(List biObjects) {
		this.documents = biObjects;
	}

	/**
	 * Checks if is complete without error.
	 *
	 * @return true, if is complete without error
	 */
	public boolean isCompleteWithoutError() {
		return completeWithoutError;
	}

	public IEngUserProfile getProfile() {
		return userProfile;
	}

	public void setProfile(IEngUserProfile profile) {
		this.userProfile = profile;
	}

	public void deleteDBRowInCaseOfError(IProgressThreadDAO threadDAO, Integer progressThreadId) {
		LOGGER.debug("IN");
		try {
			threadDAO.deleteProgressThread(progressThreadId);
		} catch (EMFUserError e1) {
			LOGGER.error("Error in deleting the row with the progress id {}", progressThreadId);
		}
		LOGGER.debug("OUT");

	}

	public File createErrorFile(BIObject biObj, Throwable error) {
//		public File createErrorFile(BIObject biObj, Throwable error, Map randomNamesToName) {
		LOGGER.debug("IN");
		File toReturn = null;
		FileWriter fw = null;
		ArrayList<PlaceHolder> list = new ArrayList<>();
		PlaceHolder p = new PlaceHolder();
		p.setValue("ERROR");
		list.add(p);
		try {
			if (biObj == null) {
				toReturn = FileUtilities.createFile("errorLog", ".txt", randomKey, list);
				fw = new FileWriter(toReturn);
				fw.write(error + "\n");
				if (error != null) {
					StackTraceElement[] errs = error.getStackTrace();
					for (int i = 0; i < errs.length; i++) {
						String err = errs[i].toString();
						fw.write(err + "\n");
					}
				}
				fw.flush();
			} else {
				String fileName = "Error " + biObj.getLabel() + "-" + biObj.getName();
				toReturn = FileUtilities.createFile(fileName, ".txt", randomKey, list);
//			randomNamesToName.put(toReturn.getName(), fileName + ".txt");
				fw = new FileWriter(toReturn);
				fw.write("Error while executing biObject " + biObj.getLabel() + " - " + biObj.getName() + "\n");
				fw.write(error + "\n");
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
			LOGGER.error("Error in wirting error file for biObj {}", biObj.getLabel());
			deleteDBRowInCaseOfError(progressThreadDAO, progressThreadId);
			throw new SpagoBIServiceException("Error in wirting error file for biObj " + biObj.getLabel(), e);
		} finally {
			if (fw != null) {
				try {
					fw.flush();
					fw.close();
				} catch (IOException e) {
				}
			}
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	public JSONObject getJsonObjectTemplate() {
		return jsonObjectTemplate;
	}

	public void setJsonObjectTemplate(JSONObject jsonObjectTemplate) {
		this.jsonObjectTemplate = jsonObjectTemplate;
	}
}
