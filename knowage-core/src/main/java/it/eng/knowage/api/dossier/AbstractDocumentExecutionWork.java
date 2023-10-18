package it.eng.knowage.api.dossier;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import commonj.work.Work;
import it.eng.knowage.api.dossier.utils.FileUtilities;
import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.knowage.engines.dossier.template.AbstractDossierTemplate;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;
import it.eng.knowage.engines.dossier.template.report.Report;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
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
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.view.dao.ISbiViewDAO;
import it.eng.spagobi.view.metadata.SbiView;

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
		ProgressThreadManager progressThreadManager = null;

		Thread thread = Thread.currentThread();
		Long threadId = thread.getId();

		LOGGER.debug("Started thread Id {} from user id: {}", threadId, ((UserProfile) userProfile).getUserId());

		Integer totalDocs = documents.size();
		LOGGER.debug("# of documents: {}", totalDocs);

		progressThreadManager = new ProgressThreadManager();
		progressThreadManager.setStatusStarted(progressThreadId);

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
			String executionRole = activity.getExecutionRole();
			JSONArray jsonArray = null;

			Map<String, String> paramMap = new HashMap<>();

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

				// If execution role was specified, we'll use it
				if (StringUtils.isNotEmpty(executionRole)) {
					roles.retainAll(Collections.singletonList(executionRole));
				}

				for (String role : roles) {

					if (!ObjectsAccessVerifier.canExec(biObject, userProfile)) {
						String message = "user " + ((UserProfile) userProfile).getUserName()
								+ " cannot execute document " + biObject.getName();
						throw new SpagoBIRuntimeException(message);
					}

					String serviceUrl = null;
					switch (biObject.getEngineLabel()) {
					case "knowagecockpitengine":
						serviceUrl = getCockpitServiceUrl(biObject, userUniqueIdentifier, jsonArray, paramMap,
								reportToUse, cockpitDocument, docId, role);
						break;
					case "knowagedashboardengine":
						serviceUrl = getDashboardServiceUrl(biObject, userUniqueIdentifier, jsonArray, paramMap,
								reportToUse, docId, role);
						break;
					default:
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

		} catch (Exception e) {
			progressThreadManager.setStatusError(progressThreadId);
			createErrorFile(biObject, e);
			LOGGER.error("Error while creating dossier activity", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		}

	}

	/**
	 * @param biObject
	 * @param userUniqueIdentifier
	 * @param jsonArray
	 * @param paramMap
	 * @param reportToUse
	 * @param docId
	 * @param role
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 * @throws URISyntaxException
	 */
	private String getDashboardServiceUrl(BIObject biObject, String userUniqueIdentifier, JSONArray jsonArray,
			Map<String, String> paramMap, Report reportToUse, Integer docId, String role)
			throws UnsupportedEncodingException, JSONException, URISyntaxException {
		String docLabel = biObject.getLabel();
		String hostUrl = getServiceHostUrl();

		URIBuilder serviceUrlBuilder = new URIBuilder(hostUrl);
		serviceUrlBuilder
				.setPath(KnowageSystemConfiguration.getKnowageCockpitEngineContext() + "/api/1.0/pages/execute/png");
		serviceUrlBuilder.setParameter("user_id", userUniqueIdentifier);
		serviceUrlBuilder.setParameter("document", Integer.toString(docId));
		serviceUrlBuilder.setParameter("DOCUMENT_LABEL", docLabel);
		serviceUrlBuilder.setParameter("toolbar", "false");
		serviceUrlBuilder.setParameter("role", role);
		serviceUrlBuilder.setParameter("menu", "false");

		addParametersToServiceUrl(biObject, jsonArray, paramMap, reportToUse, serviceUrlBuilder);
		addRenderOptionsToServiceUrl(biObject, reportToUse, serviceUrlBuilder);

		return serviceUrlBuilder.toString();
	}

	private void addParametersToServiceUrl(BIObject biObject, JSONArray jsonArray, Map<String, String> paramMap,
			Report reportToUse, URIBuilder serviceUrlBuilder) throws UnsupportedEncodingException, JSONException {
		if (reportToUse.getViewId() != null && StringUtils.isNotBlank(reportToUse.getViewId())) {
			addViewParametersToServiceUrl(reportToUse, serviceUrlBuilder);
		} else {
			addClassicParametersToServiceUrl(progressThreadId, biObject, reportToUse, serviceUrlBuilder, jsonArray,
					paramMap, true);
		}
	}

	private void addViewParametersToServiceUrl(Report reportToUse, URIBuilder serviceUrlBuilder) {
		/*
		 * /workspace/dashboard-view/SIL_01_DASHBOARD?viewName=SIL_01_VIEW_01&viewId=aab67015-af09-444e-8293-5815a51c50c3
		 */
		String viewId = reportToUse.getViewId();
		ISbiViewDAO dao = DAOFactory.getSbiViewDAO();
		dao.setUserProfile(userProfile);

		try {
			SbiView view = dao.read(viewId);
			serviceUrlBuilder.setParameter("viewName", view.getName());
			serviceUrlBuilder.setParameter("viewId", viewId);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("View with following id doesn't exist: " + viewId, e);
		}
	}

	/**
	 * @param biObject
	 * @param reportToUse
	 * @param serviceUrlBuilder
	 * @throws JSONException
	 */
	private void addRenderOptionsToServiceUrl(BIObject biObject, Report reportToUse, URIBuilder serviceUrlBuilder)
			throws JSONException {
		RenderOptions renderOptions = RenderOptions.defaultOptions();
		if (reportToUse.getSheetHeight() != null && !reportToUse.getSheetHeight().isEmpty()
				&& reportToUse.getSheetWidth() != null && !reportToUse.getSheetWidth().isEmpty()) {
			serviceUrlBuilder.setParameter("pdfWidth", reportToUse.getSheetWidth());
			serviceUrlBuilder.setParameter("pdfHeight", reportToUse.getSheetHeight());
		}

		if (reportToUse.getDeviceScaleFactor() != null && !reportToUse.getDeviceScaleFactor().isEmpty()) {
			serviceUrlBuilder.setParameter("pdfDeviceScaleFactor", reportToUse.getDeviceScaleFactor());
		} else {
			serviceUrlBuilder.setParameter("pdfDeviceScaleFactor",
					renderOptions.getDimensions().getDeviceScaleFactor());
		}
		JSONObject templateJSON = new JSONObject(new String(biObject.getActiveTemplate().getContent()));
		boolean isMultiSheet = false;
		if (templateJSON.has("sheets") && templateJSON.getJSONArray("sheets").length() > 1) {
			isMultiSheet = true;
		}
		serviceUrlBuilder.setParameter("isMultiSheet", Boolean.toString(isMultiSheet));
	}

	/**
	 * @param biObject
	 * @param userUniqueIdentifier
	 * @param jsonArray
	 * @param paramMap
	 * @param reportToUse
	 * @param cockpitDocument
	 * @param docId
	 * @param role
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 * @throws URISyntaxException
	 */
	private String getCockpitServiceUrl(BIObject biObject, String userUniqueIdentifier, JSONArray jsonArray,
			Map<String, String> paramMap, Report reportToUse, String cockpitDocument, Integer docId, String role)
			throws UnsupportedEncodingException, JSONException, URISyntaxException {
		String docName = biObject.getName();
		String hostUrl = getServiceHostUrl();
		Locale locale = GeneralUtilities.getDefaultLocale();

		URIBuilder serviceUrlBuilder = new URIBuilder(hostUrl);
		serviceUrlBuilder
				.setPath(KnowageSystemConfiguration.getKnowageCockpitEngineContext() + "/api/1.0/pages/execute/png");
		serviceUrlBuilder.setParameter("user_id", userUniqueIdentifier);
		serviceUrlBuilder.setParameter("DOCUMENT_LABEL", cockpitDocument);
		serviceUrlBuilder.setParameter("DOCUMENT_OUTPUT_PARAMETERS", "[]");
		serviceUrlBuilder.setParameter("DOCUMENT_IS_VISIBLE", "true");
		serviceUrlBuilder.setParameter("SBI_EXECUTION_ROLE", role);
		serviceUrlBuilder.setParameter("DOCUMENT_DESCRIPTION", "");
		serviceUrlBuilder.setParameter("document", Integer.toString(docId));
		serviceUrlBuilder.setParameter("IS_TECHNICAL_USER", "true");
		serviceUrlBuilder.setParameter("DOCUMENT_NAME", docName);
		serviceUrlBuilder.setParameter("NEW_SESSION", "TRUE");
		serviceUrlBuilder.setParameter("SBI_ENVIRONMENT", "DOCBROWSER");
		serviceUrlBuilder.setParameter("IS_FOR_EXPORT", "true");
		serviceUrlBuilder.setParameter("documentMode", "VIEW");
		serviceUrlBuilder.setParameter("export", "true");
		serviceUrlBuilder.setParameter("outputType", "PNG");
		serviceUrlBuilder.setParameter("knowage_sys_country", locale.getCountry());
		serviceUrlBuilder.setParameter("knowage_sys_language", locale.getLanguage());
		serviceUrlBuilder.setParameter("SBI_LANGUAGE", locale.getLanguage());
		serviceUrlBuilder.setParameter("SBI_COUNTRY", locale.getCountry());
		serviceUrlBuilder.setParameter("SBI_SCRIPT", locale.getScript());

		addParametersToServiceUrl(biObject, jsonArray, paramMap, reportToUse, serviceUrlBuilder);
		addRenderOptionsToServiceUrl(biObject, reportToUse, serviceUrlBuilder);

		return serviceUrlBuilder.toString();
	}

	protected void setTenant() {
		UserProfile profile = (UserProfile) this.getProfile();
		String tenant = profile.getOrganization();
		LOGGER.debug("Tenant : {}", tenant);
		TenantManager.setTenant(new Tenant(tenant));
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

	public void addClassicParametersToServiceUrl(Integer progressthreadId, BIObject biObject, Report reportToUse,
			URIBuilder serviceUrlBuilder, JSONArray jsonArray, Map<String, String> paramMap, boolean dashboard)
			throws UnsupportedEncodingException, JSONException {
		JSONArray jsonParams = new JSONArray();

		List<BIObjectParameter> drivers = biObject.getDrivers();
		String viewId = reportToUse.getViewId();

		// This control doesn't make sense for parameters which use a view
		if (StringUtils.isEmpty(viewId) && drivers != null) {
			List<Parameter> parameter = reportToUse.getParameters();
			if (drivers.size() != parameter.size()) {
				throw new SpagoBIRuntimeException(
						"There are a different number of parameters/drivers between document and template");
			}
			Collections.sort(drivers);
			ParametersDecoder decoder = new ParametersDecoder();

			for (BIObjectParameter biObjectParameter : drivers) {
				boolean found = false;
				String outParamValue = "";
				String outParamName = "";
				for (Parameter templateParameter : parameter) {

					String currParamType = templateParameter.getType();
					String currParamValue = templateParameter.getValue();

					if (currParamType.equals("dynamic")) {

						if (currParamValue != null && !currParamValue.isEmpty()) {

							// filled by fillParametersValues in DossierExecutionResource
							outParamValue = currParamValue;

							List<?> currParamValueDecoded = decoder.decode(outParamValue);

							if (biObjectParameter.getParameterUrlName().equals(templateParameter.getUrlName())) {
								outParamName = templateParameter.getUrlName();
								if (dashboard) {

									JSONObject param = new JSONObject();
									param.put("multivalue", decoder.isMultiValues(outParamValue));
									param.put("urlName", biObjectParameter.getParameterUrlName());

									JSONArray paramValueArray = new JSONArray();
									for (Object currValue2 : currParamValueDecoded) {
										JSONObject paramValue = new JSONObject();

										String currValue2AsString = currValue2.toString();

										if (currValue2AsString.startsWith("'") && currValue2AsString.endsWith("'")) {
											currValue2AsString = currValue2AsString.substring(1,
													currValue2AsString.length() - 1);
										}

										paramValue.put("value", URLEncoder.encode(currValue2AsString,
												StandardCharsets.UTF_8.toString()));
										paramValue.put("description",
												URLEncoder.encode(templateParameter.getUrlNameDescription(),
														StandardCharsets.UTF_8.toString()));

										paramValueArray.put(paramValue);
									}
									param.put("value", paramValueArray);

									jsonParams.put(param);
								} else {
									serviceUrlBuilder.setParameter(biObjectParameter.getParameterUrlName(),
											outParamValue);
									serviceUrlBuilder.setParameter(biObjectParameter.getParameterUrlName(),
											templateParameter.getUrlNameDescription());
								}
								found = true;

								break;
							}
						}
					} else {
						if (biObjectParameter.getParameterUrlName().equals(templateParameter.getUrlName())) {
							serviceUrlBuilder.setParameter(biObjectParameter.getParameterUrlName(), currParamValue);
							outParamValue = currParamValue;
							outParamName = templateParameter.getUrlName();
							// We need a description for static parameter, we force the value if it's missing
							if (isEmpty(templateParameter.getUrlNameDescription())) {
								templateParameter.setUrlNameDescription(currParamValue);
							}
							// description
							serviceUrlBuilder.setParameter(biObjectParameter.getParameterUrlName(),
									templateParameter.getUrlNameDescription());

							found = true;
							break;

						}
					}
				}
				paramMap.put(outParamName, outParamValue);
				if (!found && biObjectParameter.isRequired()) {
					throw new SpagoBIRuntimeException(
							"There is no match between document parameters and template parameters.");
				}

			}
		}
		if (dashboard) {
			byte[] jsonParamsByteArray = jsonParams.toString().getBytes();
			String encodedJsonParams = new String(Base64.getEncoder().withoutPadding().encode(jsonParamsByteArray));
			serviceUrlBuilder.setParameter("params", encodedJsonParams);
		}

	}

	public String getServiceHostUrl() {
		String serviceURL = SpagoBIUtilities
				.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGOBI_SERVICE_JNDI"));
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
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(responseAsByteArray))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File newFile = FileUtilities.createFile(FilenameUtils.removeExtension(zipEntry.getName()), ".png",
						randomKey, new ArrayList<>());
				try (FileOutputStream fos = new FileOutputStream(newFile)) {
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		}

		// array of supported extensions (use a List if you prefer)
		String[] extensions = new String[] { "gif", "png", "bmp" // and other formats you need
		};
		// filter to identify images based on their extensions
		FilenameFilter imageFilter = (dir, name) -> {
			for (final String ext : extensions) {
				if (name.endsWith("." + ext) && name.startsWith("sheet")) {
					return (true);
				}
			}
			return (false);
		};

		String documentLabel = reportToUse.getLabel();

		if (outFolder.isDirectory()) {
			for (final File f : outFolder.listFiles(imageFilter)) {

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
		try {
			threadDAO.deleteProgressThread(progressThreadId);
		} catch (EMFUserError e1) {
			LOGGER.error("Error in deleting the row with the progress id {}", progressThreadId);
		}
	}

	public File createErrorFile(BIObject biObj, Throwable error) {
		File toReturn = null;
		ArrayList<PlaceHolder> list = new ArrayList<>();
		PlaceHolder p = new PlaceHolder();
		p.setValue("ERROR");
		list.add(p);
		try {
			if (biObj == null) {
				toReturn = FileUtilities.createFile("errorLog", ".txt", randomKey, list);
				try (FileWriter fw = new FileWriter(toReturn)) {
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
			} else {
				String fileName = "Error " + biObj.getLabel() + "-" + biObj.getName();
				toReturn = FileUtilities.createFile(fileName, ".txt", randomKey, list);
				try (FileWriter fw = new FileWriter(toReturn)) {
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
			}
		} catch (Exception e) {
			LOGGER.error("Error in writing error file for biObj {}", biObj);
			deleteDBRowInCaseOfError(progressThreadDAO, progressThreadId);
			throw new SpagoBIServiceException("Error in wirting error file for biObj " + biObj, e);
		}
		return toReturn;
	}

	public JSONObject getJsonObjectTemplate() {
		return jsonObjectTemplate;
	}

	public void setJsonObjectTemplate(JSONObject jsonObjectTemplate) {
		this.jsonObjectTemplate = jsonObjectTemplate;
	}
}
