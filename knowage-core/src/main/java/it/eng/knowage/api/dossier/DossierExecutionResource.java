package it.eng.knowage.api.dossier;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkItem;
import de.myfoo.commonj.work.FooRemoteWorkItem;
import it.eng.knowage.api.dossier.utils.DossierExecutionUtilities;
import it.eng.knowage.engines.dossier.template.AbstractDossierTemplate;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.report.Report;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.ZipUtility;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

@Path("/1.0/dossier")
public class DossierExecutionResource extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(DossierExecutionResource.class);

	@POST
	@Path("/executedocuments")
	@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
	@Consumes(MediaType.APPLICATION_JSON)
	public String executeDocuments(AbstractDossierTemplate dossierTemplate) {
		logger.debug("IN");
		Integer progressThreadId = null;

		try {

			// TODO: check this and also authorization in business-map.xml
			// get user Profile
			UserProfile profile = getUserProfile();

			// get reports information form Dossier Template

			if (profile != null) {
				List<BIObjectPlaceholdersPair> documentsToExecute = getBIObjectFromTemplate(dossierTemplate);

				String randomName = getRandomName();
				ProgressThread progressThread = new ProgressThread(profile.getUserId().toString(), documentsToExecute.size(), null, null, randomName,
						ProgressThread.TYPE_DOSSIER_EXECUTION);
				IProgressThreadDAO progressThreadDAO = DAOFactory.getProgressThreadDAO();
				progressThreadId = progressThreadDAO.insertProgressThread(progressThread);

				IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();
				Config config = configDAO.loadConfigParametersByLabel(SpagoBIConstants.JNDI_THREAD_MANAGER);
				if (config == null) {
					logger.debug("Impossible to retrive from the configuration the property [" + SpagoBIConstants.JNDI_THREAD_MANAGER + "]");
					throw new SpagoBIRuntimeException(
							"Impossible to retrive from the configuration the property [" + SpagoBIConstants.JNDI_THREAD_MANAGER + "]");
				}
				WorkManager workManager = new WorkManager(config.getValueCheck());
				Work documentExportWork = getExecutionWork(dossierTemplate, documentsToExecute, profile, progressThreadId, randomName, "PPT");
				FooRemoteWorkItem remoteWorkItem = workManager.buildFooRemoteWorkItem(documentExportWork, null);

				// Check if work was accepted
				if (remoteWorkItem.getStatus() != WorkEvent.WORK_ACCEPTED) {
					int statusWI = remoteWorkItem.getStatus();
					throw new SpagoBIRuntimeException("Dossier Execution Work thread with id [" + progressThreadId + "] was rejected with status " + statusWI);
				} else {
					logger.debug("Running (Dossier) Work Item with id: " + progressThreadId);
					WorkItem workItem = workManager.runWithReturnWI(documentExportWork, null);
					int statusWI = workItem.getStatus();
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("progressThreadId", progressThreadId);
				return progressThreadId.toString();
			}

		} catch (SpagoBIRuntimeException e) {
			if (progressThreadId != null) {
				deleteDBRowInCaseOfError(progressThreadId);
			}
			logger.error("Error while generating PDF documents for PPT template.", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);

		} catch (Throwable t) {
			if (progressThreadId != null) {
				deleteDBRowInCaseOfError(progressThreadId);
			}
			logger.error("Error while generating PDF documents for PPT template.", t.getCause());
			throw new SpagoBIRuntimeException(t.getMessage(), t);
		}
		return null;

	}

	@POST
	@Path("/executedocumentsForImagesReplacing")
	@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
	@Consumes(MediaType.APPLICATION_JSON)
	public String executeDocumentsForImagesReplacing(AbstractDossierTemplate dossierTemplate) {
		logger.debug("IN");
		Integer progressThreadId = null;

		try {

			// TODO: check this and also authorization in business-map.xml
			// get user Profile
			UserProfile profile = getUserProfile();

			// get reports information form Dossier Template

			if (profile != null) {
				List<BIObjectPlaceholdersPair> documentsToExecute = getBIObjectFromTemplate(dossierTemplate);

				String randomName = getRandomName();
				ProgressThread progressThread = new ProgressThread(profile.getUserId().toString(), documentsToExecute.size(), null, null, randomName,
						ProgressThread.TYPE_DOSSIER_EXECUTION);
				IProgressThreadDAO progressThreadDAO = DAOFactory.getProgressThreadDAO();
				progressThreadId = progressThreadDAO.insertProgressThread(progressThread);

				IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();
				Config config = configDAO.loadConfigParametersByLabel(SpagoBIConstants.JNDI_THREAD_MANAGER);
				if (config == null) {
					logger.debug("Impossible to retrive from the configuration the property [" + SpagoBIConstants.JNDI_THREAD_MANAGER + "]");
					throw new SpagoBIRuntimeException(
							"Impossible to retrive from the configuration the property [" + SpagoBIConstants.JNDI_THREAD_MANAGER + "]");
				}
				WorkManager workManager = new WorkManager(config.getValueCheck());
				Work documentExportWork = getExecutionWork(dossierTemplate, documentsToExecute, profile, progressThreadId, randomName, "DOC");
				FooRemoteWorkItem remoteWorkItem = workManager.buildFooRemoteWorkItem(documentExportWork, null);

				// Check if work was accepted
				if (remoteWorkItem.getStatus() != WorkEvent.WORK_ACCEPTED) {
					int statusWI = remoteWorkItem.getStatus();
					throw new SpagoBIRuntimeException("Dossier Execution Work thread with id [" + progressThreadId + "] was rejected with status " + statusWI);
				} else {
					logger.debug("Running (Dossier) Work Item with id: " + progressThreadId);
					WorkItem workItem = workManager.runWithReturnWI(documentExportWork, null);
					int statusWI = workItem.getStatus();
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("progressThreadId", progressThreadId);
				return progressThreadId.toString();
			}

		} catch (SpagoBIRuntimeException e) {
			if (progressThreadId != null) {
				deleteDBRowInCaseOfError(progressThreadId);
			}
			logger.error("Error while generating PDF documents for PPT template.", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);

		} catch (Throwable t) {
			if (progressThreadId != null) {
				deleteDBRowInCaseOfError(progressThreadId);
			}
			logger.error("Error while generating PDF documents for PPT template.", t.getCause());
			throw new SpagoBIRuntimeException(t.getMessage(), t);
		}
		return null;

	}

	public Work getExecutionWork(AbstractDossierTemplate dossierTemplate, List<BIObjectPlaceholdersPair> documentsToExecute, UserProfile profile,
			Integer progressThreadId, String randomName, String type) {
		Work work = null;
		switch (type) {
		case "DOC":
			work = new DocumentExecutionWorkForDoc(dossierTemplate, documentsToExecute, profile, progressThreadId, randomName);
			break;
		case "PPT":
			work = new DocumentExecutionWork(documentsToExecute, profile, progressThreadId, randomName);
			break;
		default:
			break;
		}

		return work;

	}

	// ***********************************************
	// UTILITY METHODS
	// ***********************************************

	private void fillParametersValues(BIObject document, List<Parameter> parameters) {
		logger.debug("IN");
		ParametersDecoder decoder = new ParametersDecoder();
		logger.debug("fill values of object " + document.getLabel());
		List<BIObjectParameter> documentParameters = document.getDrivers();
		for (BIObjectParameter documentParameter : documentParameters) {
			String parameterUrl = documentParameter.getParameterUrlName();
			logger.debug("search value for obj par with label  " + parameterUrl);
			boolean hasValue = false;
			List<String> documentParameterValues = new ArrayList<String>();
			List<String> documentParameterValuesDescription = new ArrayList<String>();
			for (Parameter parameter : parameters) {
				if (parameter.getUrlName().equals(parameterUrl)) {
					String value = parameter.getValue();
					if (value != null) {
						value = decoder.decodeParameter(value);

						if (decoder.isMultiValues(value) && value.contains("STRING"))
							value.replaceAll("'", "");
						documentParameterValues.add(value);
						logger.debug("value is " + value);

						documentParameterValuesDescription.add(parameter.getUrlNameDescription());
						hasValue = true;
						break;
					}
				}
			}
			if (!hasValue) {
				logger.warn("Value not defined for parameter with url: " + parameterUrl);
				// check for mandatory violation
				if (documentParameter.isRequired()) {
					throw new SpagoBIRuntimeException("Parameter " + documentParameter.getParameterUrlName() + " must have a value");

				}
			}

			logger.debug("insert for " + documentParameter.getLabel() + " value" + documentParameterValues.toString());
			documentParameter.setParameterValues(documentParameterValues);
			documentParameter.setParameterValuesDescription(documentParameterValuesDescription);

		}

		logger.debug("OUT");
	}

	private List<BIObjectPlaceholdersPair> getBIObjectFromTemplate(AbstractDossierTemplate dossierTemplate) throws EMFUserError {
		List<BIObjectPlaceholdersPair> documentsToExecute = new ArrayList<BIObjectPlaceholdersPair>();
		IBIObjectDAO biobjectDAO = DAOFactory.getBIObjectDAO();
		List<Report> reports = dossierTemplate.getReports();
		for (Report report : reports) {
			String documentLabel = report.getLabel();
			BIObject biObject = biobjectDAO.loadBIObjectByLabel(documentLabel);
			if (biObject != null) {
				List<Parameter> parameters = report.getParameters();
				// fill parameters
				fillParametersValues(biObject, parameters);
				BIObjectPlaceholdersPair pair = new BIObjectPlaceholdersPair();
				pair.setBiObject(biObject);
				pair.setPlaceholders(report.getPlaceholders());
				documentsToExecute.add(pair);
			}
		}
		return documentsToExecute;
	}

	private void deleteDBRowInCaseOfError(Integer progressThreadId) {
		IProgressThreadDAO threadDAO;

		logger.debug("IN");
		try {
			threadDAO = DAOFactory.getProgressThreadDAO();
			threadDAO.deleteProgressThread(progressThreadId);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occuerd while deleting the row with progress id equal to [" + progressThreadId + "]", t);
		} finally {
			logger.debug("OUT");
		}

	}

	private String getRandomName() {
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yy hh:mm:ss.SSS");
		String randomName = formatter.format(new Date());
		randomName = randomName.replaceAll(" ", "_");
		randomName = randomName.replaceAll(":", "-");
		return randomName;

	}

	private File getZippedResult(String randomKey) {
		logger.debug("IN");
		File dossierExecutionFolder = DossierExecutionUtilities.getDossierExecutionFolder();
		String path = dossierExecutionFolder.getAbsolutePath() + File.separator + randomKey;
		File documentsFolder = new File(path);
		File zip = null;
		if (documentsFolder.exists()) {
			logger.info("Found dossier documents directory for specific execution: " + path);
			zip = ZipUtility.generateZipFile(path, randomKey + ".zip");
		} else {
			logger.error("Cannot find dossier documents directory for specific execution: " + path);
			throw new SpagoBIRuntimeException("Cannot find dossier documents directory for specific execution: " + path);
		}
		logger.debug("OUT");
		return zip;

	}

}
