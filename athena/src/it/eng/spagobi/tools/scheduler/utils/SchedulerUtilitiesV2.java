package it.eng.spagobi.tools.scheduler.utils;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.to.JobTrigger;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SchedulerUtilitiesV2 {

	// Serialization of JobTrigger save
	// parameter----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void serializeSaveParameterOptions(StringBuffer message, JobTrigger trigg, boolean runImmediately, IEngUserProfile profile)
			throws EMFUserError {

		message.append("   <PARAMETERS>");

		Map<String, DispatchContext> saveOptions = trigg.getSaveOptions();
		Set<String> uniqueDispatchContextNames = saveOptions.keySet();

		for (String uniqueDispatchContextName : uniqueDispatchContextNames) {
			DispatchContext dispatchContext = saveOptions.get(uniqueDispatchContextName);

			String saveOptString = "";

			saveOptString += serializeSaveAsSnapshotOptions(dispatchContext);
			saveOptString += serializeSaveAsFileOptions(dispatchContext);
			saveOptString += serializeSaveAsJavaClassOptions(dispatchContext);
			saveOptString += serializeSaveAsDocumentOptions(dispatchContext);
			saveOptString += serializeSaveAsMailOptions(dispatchContext);
			saveOptString += serializeSaveAsDistributionListOptions(dispatchContext, uniqueDispatchContextName, trigg, runImmediately, profile);

			message.append("   	   <PARAMETER name=\"biobject_id_" + uniqueDispatchContextName + "\" value=\"" + saveOptString + "\" />");
		}

		message.append("   </PARAMETERS>");
	}

	private static String serializeSaveAsSnapshotOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isSnapshootDispatchChannelEnabled()) {
			saveOptString += "saveassnapshot=true%26";
			if ((dispatchContext.getSnapshotName() != null) && !dispatchContext.getSnapshotName().trim().equals("")) {
				saveOptString += "snapshotname=" + dispatchContext.getSnapshotName() + "%26";
			}
			if ((dispatchContext.getSnapshotDescription() != null) && !dispatchContext.getSnapshotDescription().trim().equals("")) {
				saveOptString += "snapshotdescription=" + dispatchContext.getSnapshotDescription() + "%26";
			}
			if ((dispatchContext.getSnapshotHistoryLength() != null) && !dispatchContext.getSnapshotHistoryLength().trim().equals("")) {
				saveOptString += "snapshothistorylength=" + dispatchContext.getSnapshotHistoryLength() + "%26";
			}
		}

		return saveOptString;
	}

	private static String serializeSaveAsJavaClassOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isJavaClassDispatchChannelEnabled()) {
			saveOptString += "sendtojavaclass=true%26";
			if ((dispatchContext.getJavaClassPath() != null) && !dispatchContext.getJavaClassPath().trim().equals("")) {
				saveOptString += "javaclasspath=" + dispatchContext.getJavaClassPath() + "%26";
			}
		}

		return saveOptString;
	}

	private static String serializeSaveAsFileOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isFileSystemDispatchChannelEnabled()) {
			saveOptString += "saveasfile=true%26";
			if (StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder())) {
				saveOptString += "destinationfolder=" + dispatchContext.getDestinationFolder() + "%26";
			}
			if (StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder())) {
				saveOptString += "destinationfolder=" + dispatchContext.getDestinationFolder() + "%26";
			}
			if (dispatchContext.isDestinationFolderRelativeToResourceFolder()) {
				saveOptString += "isrelativetoresourcefolder=true%26";
			} else {
				saveOptString += "isrelativetoresourcefolder=false%26";
			}

			if (dispatchContext.isProcessMonitoringEnabled()) {
				saveOptString += "isprocessmonitoringenabled=true%26";
			} else {
				saveOptString += "isprocessmonitoringenabled=false%26";
			}
		}

		if (dispatchContext.isZipFileDocument()) {
			saveOptString += "zipFileDocument=true%26";
		}
		if (dispatchContext.getFileName() != null) {
			saveOptString += "fileName=" + dispatchContext.getFileName() + "%26";
		}

		if (dispatchContext.getZipFileName() != null) {
			saveOptString += "zipFileName=" + dispatchContext.getZipFileName() + "%26";
		}

		return saveOptString;
	}

	private static String serializeSaveAsDocumentOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isFunctionalityTreeDispatchChannelEnabled()) {
			saveOptString += "saveasdocument=true%26";
			if ((dispatchContext.getDocumentName() != null) && !dispatchContext.getDocumentName().trim().equals("")) {
				saveOptString += "documentname=" + dispatchContext.getDocumentName() + "%26";
			}
			if ((dispatchContext.getDocumentDescription() != null) && !dispatchContext.getDocumentDescription().trim().equals("")) {
				saveOptString += "documentdescription=" + dispatchContext.getDocumentDescription() + "%26";
			}
			if (dispatchContext.isUseFixedFolder() && dispatchContext.getFoldersTo() != null && !dispatchContext.getFoldersTo().trim().equals("")) {
				saveOptString += "foldersTo=" + dispatchContext.getFoldersTo() + "%26";
			}
			if (dispatchContext.isUseFolderDataSet() && dispatchContext.getDataSetFolderLabel() != null
					&& !dispatchContext.getDataSetFolderLabel().trim().equals("")) {
				saveOptString += "datasetFolderLabel=" + dispatchContext.getDataSetFolderLabel() + "%26";
				if (dispatchContext.getDataSetFolderParameterLabel() != null && !dispatchContext.getDataSetFolderParameterLabel().trim().equals("")) {
					saveOptString += "datasetFolderParameterLabel=" + dispatchContext.getDataSetFolderParameterLabel() + "%26";
				}
			}
			if ((dispatchContext.getDocumentHistoryLength() != null) && !dispatchContext.getDocumentHistoryLength().trim().equals("")) {
				saveOptString += "documenthistorylength=" + dispatchContext.getDocumentHistoryLength() + "%26";
			}
			if ((dispatchContext.getFunctionalityIds() != null) && !dispatchContext.getFunctionalityIds().trim().equals("")) {
				saveOptString += "functionalityids=" + dispatchContext.getFunctionalityIds() + "%26";
			}
		}

		return saveOptString;
	}

	private static String serializeSaveAsMailOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isMailDispatchChannelEnabled()) {
			saveOptString += "sendmail=true%26";
			if (dispatchContext.isUseFixedRecipients() && dispatchContext.getMailTos() != null && !dispatchContext.getMailTos().trim().equals("")) {
				saveOptString += "mailtos=" + dispatchContext.getMailTos() + "%26";
			}
			if (dispatchContext.isUseDataSet() && dispatchContext.getDataSetLabel() != null && !dispatchContext.getDataSetLabel().trim().equals("")) {
				saveOptString += "datasetLabel=" + dispatchContext.getDataSetLabel() + "%26";
				if (dispatchContext.getDataSetParameterLabel() != null && !dispatchContext.getDataSetParameterLabel().trim().equals("")) {
					saveOptString += "datasetParameterLabel=" + dispatchContext.getDataSetParameterLabel() + "%26";
				}
			}
			if (dispatchContext.isUseExpression() && dispatchContext.getExpression() != null && !dispatchContext.getExpression().trim().equals("")) {
				saveOptString += "expression=" + dispatchContext.getExpression() + "%26";
			}
			if ((dispatchContext.getMailSubj() != null) && !dispatchContext.getMailSubj().trim().equals("")) {
				saveOptString += "mailsubj=" + dispatchContext.getMailSubj() + "%26";
			}
			if ((dispatchContext.getMailTxt() != null) && !dispatchContext.getMailTxt().trim().equals("")) {
				saveOptString += "mailtxt=" + dispatchContext.getMailTxt() + "%26";
			}

			// Mail
			if (dispatchContext.isZipMailDocument()) {
				saveOptString += "zipMailDocument=true%26";
			}
			if (dispatchContext.isReportNameInSubject()) {
				saveOptString += "reportNameInSubject=true%26";
			}

			if (dispatchContext.isUniqueMail()) {
				saveOptString += "uniqueMail=true%26";
			}

			if (dispatchContext.getContainedFileName() != null) {
				saveOptString += "containedFileName=" + dispatchContext.getContainedFileName() + "%26";
			}
			if (dispatchContext.getZipMailName() != null) {
				saveOptString += "zipMailName=" + dispatchContext.getZipMailName() + "%26";
			}

		}

		return saveOptString;
	}

	private static String serializeSaveAsDistributionListOptions(DispatchContext dispatchContext, String uniqueDispatchContextName, JobTrigger triggerInfo,
			boolean runImmediately, IEngUserProfile profile) throws EMFUserError {
		String saveOptString = "";

		JobInfo jobInfo = triggerInfo.getJobInfo();

		if (dispatchContext.isDistributionListDispatchChannelEnabled()) {
			String xml = "";
			if (!runImmediately) {
				xml += "<SCHEDULE ";
				xml += " jobName=\"" + jobInfo.getJobName() + "\" ";
				xml += " triggerName=\"" + triggerInfo.getTriggerName() + "\" ";
				xml += " startDate=\"" + triggerInfo.getStartDate() + "\" ";
				xml += " startTime=\"" + triggerInfo.getStartTime() + "\" ";
				xml += " chronString=\"" + triggerInfo.getChrono() + "\" ";
				String enddate = triggerInfo.getEndDate();
				String endtime = triggerInfo.getEndTime();
				if (!enddate.trim().equals("")) {
					xml += " endDate=\"" + enddate + "\" ";
					if (!endtime.trim().equals("")) {
						xml += " endTime=\"" + endtime + "\" ";
					}
				}

				String repeatinterval = triggerInfo.getRepeatInterval();
				if (!repeatinterval.trim().equals("")) {
					xml += " repeatInterval=\"" + repeatinterval + "\" ";
				}
				xml += ">";

				String params = "<PARAMETERS>";

				List biObjects = jobInfo.getDocuments();
				Iterator iterbiobj = biObjects.iterator();
				int index = 0;
				while (iterbiobj.hasNext()) {
					index++;
					BIObject biobj = (BIObject) iterbiobj.next();
					String objpref = biobj.getId().toString() + "__" + new Integer(index).toString();
					if (uniqueDispatchContextName.equals(objpref)) {

						List pars = biobj.getBiObjectParameters();
						Iterator iterPars = pars.iterator();
						String queryString = "";
						while (iterPars.hasNext()) {
							BIObjectParameter biobjpar = (BIObjectParameter) iterPars.next();
							String concatenatedValue = "";
							List values = biobjpar.getParameterValues();
							if (values != null) {
								Iterator itervalues = values.iterator();
								while (itervalues.hasNext()) {
									String value = (String) itervalues.next();
									concatenatedValue += value + ",";
								}
								if (concatenatedValue.length() > 0) {
									concatenatedValue = concatenatedValue.substring(0, concatenatedValue.length() - 1);
									queryString += biobjpar.getParameterUrlName() + "=" + concatenatedValue + "%26";
								}
							}
						}
						if (queryString.length() > 0) {
							queryString = queryString.substring(0, queryString.length() - 3);
						}
						params += "<PARAMETER name=\"" + biobj.getLabel() + "__" + index + "\" value=\"" + queryString + "\" />";
					} else {
						continue;
					}
				}
				params += "</PARAMETERS>";

				xml += params;
				xml += "</SCHEDULE>";
			}

			saveOptString += "sendtodl=true%26";

			List l = dispatchContext.getDlIds();
			if (!l.isEmpty()) {

				String dlIds = "dlId=";
				int objId = dispatchContext.getBiobjId();
				Iterator iter = l.iterator();
				while (iter.hasNext()) {

					Integer dlId = (Integer) iter.next();
					try {
						if (!runImmediately) {
							IDistributionListDAO dao = DAOFactory.getDistributionListDAO();
							dao.setUserProfile(profile);
							DistributionList dl = dao.loadDistributionListById(dlId);
							dao.insertDLforDocument(dl, objId, xml);
						}
					} catch (Exception ex) {

						throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
					}

					if (iter.hasNext()) {
						dlIds += dlId.intValue() + ",";
					} else {
						dlIds += dlId.intValue();
					}

				}
				saveOptString += dlIds + "%26";

			}
		}

		return saveOptString;
	}

	// Creation of JobTrigger save
	// paramater----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static Map<String, DispatchContext> getSaveOptionsFromRequest(JSONArray docum) throws EMFUserError, JSONException {
		// TriggerInfo triggerInfo = (TriggerInfo) sessionContainer.getAttribute(SpagoBIConstants.TRIGGER_INFO);
		// TriggerInfo triggerInfo = null;
		// JobInfo jobInfo = triggerInfo.getJobInfo();
		// List<Integer> biobjIds = jobInfo.getDocumentIds();

		Map<String, DispatchContext> saveOptions = new HashMap<String, DispatchContext>();
		for (int i = 0; i < docum.length(); i++) {
			DispatchContext dispatchContext = new DispatchContext();
			getSaveAsSnapshotOptions(docum.getJSONObject(i), dispatchContext);
			getSaveAsFileOptions(docum.getJSONObject(i), dispatchContext);
			getSaveAsJavaClassOptions(docum.getJSONObject(i), dispatchContext);
			getSaveAsDocumentOptions(docum.getJSONObject(i), dispatchContext);
			getSaveAsMailOptions(docum.getJSONObject(i), dispatchContext);
			getSaveAsDistributionListOptions(docum.getJSONObject(i), dispatchContext);

			saveOptions.put(docum.getJSONObject(i).getString("labelId"), dispatchContext);
		}

		// int index = 0;
		// for (Integer biobId : biobjIds) {
		// index++;
		// DispatchContext dispatchContext = new DispatchContext();
		//
		// // getSaveAsSnapshotOptions(request, dispatchContext, biobId, index);
		// // getSaveAsFileOptions(request, dispatchContext, biobId, index);
		// // getSaveAsJavaClassOptions(request, dispatchContext, biobId, index);
		// // getSaveAsDocumentOptions(request, dispatchContext, biobId, index);
		// // getSaveAsMailOptions(request, dispatchContext, biobId, index);
		// // getSaveAsDistributionListOptions(request, dispatchContext, biobId, index);
		//
		// saveOptions.put(biobId + "__" + index, dispatchContext);
		// }
		return saveOptions;
	}

	private static void getSaveAsSnapshotOptions(JSONObject request, DispatchContext dispatchContext) throws JSONException {
		Boolean saveassnap = request.optBoolean("saveassnapshot");
		if (saveassnap) {
			dispatchContext.setSnapshootDispatchChannelEnabled(true);

			String snapshotName = request.optString("snapshotname");
			dispatchContext.setSnapshotName(snapshotName);

			String snapshotDescription = request.optString("snapshotdescription");
			dispatchContext.setSnapshotDescription(snapshotDescription);

			String snapshotHistoryLength = request.optString("snapshothistorylength");
			dispatchContext.setSnapshotHistoryLength(snapshotHistoryLength);
		}
	}

	private static void getSaveAsFileOptions(JSONObject request, DispatchContext dispatchContext) throws JSONException {
		Boolean saveasfile = request.optBoolean("saveasfile");
		if (saveasfile) {
			dispatchContext.setFileSystemDisptachChannelEnabled(true);
			dispatchContext.setProcessMonitoringEnabled(false);
			dispatchContext.setDestinationFolderRelativeToResourceFolder(true);

			String destinationFolder = request.optString("destinationfolder");
			dispatchContext.setDestinationFolder(destinationFolder);

			boolean zipFileDocument = request.optBoolean("zipFileDocument");
			dispatchContext.setZipFileDocument(zipFileDocument);

			String fileName = request.optString("fileName");
			if (fileName != null && !fileName.equals("")) {
				dispatchContext.setFileName(fileName);
			}
			// set Zip File Name if chosen
			String zipFileName = request.optString("zipFileName");
			if (zipFileName != null && !zipFileName.equals("")) {
				dispatchContext.setZipFileName(zipFileName);
			}

		}
	}

	private static void getSaveAsJavaClassOptions(JSONObject request, DispatchContext dispatchContext) throws JSONException {
		Boolean sendToJavaClass = request.optBoolean("sendtojavaclass");
		if (sendToJavaClass) {
			dispatchContext.setJavaClassDispatchChannelEnabled(true);
			String javaClassPath = request.optString("javaclasspath");
			JavaClassDestination tryClass = null;
			try {
				tryClass = (JavaClassDestination) Class.forName(javaClassPath).newInstance();
			} catch (ClassCastException e) {
				// logger.error("Error in istantiating class");
				EMFValidationError emfError = new EMFValidationError(EMFErrorSeverity.ERROR, "sendtojavaclass", "12200");
				// errorHandler.addError(emfError);

			} catch (Exception e) {
				// logger.error("Error in istantiating class");
				EMFValidationError emfError = new EMFValidationError(EMFErrorSeverity.ERROR, "sendtojavaclass", "12100");
				// errorHandler.addError(emfError);
			}
			dispatchContext.setJavaClassPath(javaClassPath);
		}
	}

	private static void getSaveAsDocumentOptions(JSONObject request, DispatchContext dispatchContext) throws JSONException {
		Boolean saveasdoc = request.optBoolean("saveasdocument");
		if (saveasdoc) {
			dispatchContext.setFunctionalityTreeDispatchChannelEnabled(true);
			String docname = request.optString("documentname");
			dispatchContext.setDocumentName(docname);
			String docdescr = request.optString("documentdescription");
			dispatchContext.setDocumentDescription(docdescr);
			boolean useFixedFolder = request.optBoolean("useFixedFolder");
			dispatchContext.setUseFixedFolder(useFixedFolder);
			if (useFixedFolder) {
				String functIdsConcat = "";
				JSONArray ja = request.optJSONArray("funct");
				for (int i = 0; i < ja.length(); i++) {
					functIdsConcat += ja.optString(i);
					if (i != ja.length() - 1) {
						functIdsConcat += ",";
					}
				}

				dispatchContext.setFunctionalityIds(functIdsConcat);
			}
			// gestire acquisizione folder
			boolean useFolderDataset = request.optBoolean("useFolderDataset");
			dispatchContext.setUseFolderDataSet(useFolderDataset);
			if (useFolderDataset) {
				String dsLabel = request.optString("datasetFolderLabel");
				dispatchContext.setDataSetFolderLabel(dsLabel);
				String datasetParameterLabel = request.optString("datasetFolderParameter");
				dispatchContext.setDataSetFolderParameterLabel(datasetParameterLabel);
				if (dsLabel == null || dsLabel.trim().equals("")) {
					// BIObject biobj = DAOFactory.optBIObjectDAO().loadBIObjectById(biobId);
					// List params = new ArrayList();
					// params.add(biobj.getName());
					// this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSet", params,
					// "component_scheduler_messages"));
				}
				if (datasetParameterLabel == null || datasetParameterLabel.trim().equals("")) {
					// BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					// List params = new ArrayList();
					// params.add(biobj.getName());
					// this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSetParameter", params,
					// "component_scheduler_messages"));
				}
				// sInfo.setFunctionalityIds(functIdsConcat);
			}
		}
	}

	private static void getSaveAsMailOptions(JSONObject request, DispatchContext dispatchContext) throws EMFUserError, JSONException {
		Boolean sendmail = request.optBoolean("sendmail");
		if (sendmail) {
			dispatchContext.setMailDispatchChannelEnabled(true);
			boolean useFixedRecipients = request.optBoolean("useFixedRecipients");
			dispatchContext.setUseFixedRecipients(useFixedRecipients);
			if (useFixedRecipients) {
				String mailtos = request.optString("mailtos");
				dispatchContext.setMailTos(mailtos);
				if (mailtos == null || mailtos.trim().equals("")) {
					// BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					// List params = new ArrayList();
					// params.add(biobj.getName());
					// this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingFixedRecipients", params,
					// "component_scheduler_messages"));
				}
			}
			boolean useDataset = request.optBoolean("useDataset");
			dispatchContext.setUseDataSet(useDataset);
			if (useDataset) {
				String dsLabel = request.optString("datasetLabel");
				dispatchContext.setDataSetLabel(dsLabel);
				String datasetParameterLabel = request.optString("datasetParameter");
				dispatchContext.setDataSetParameterLabel(datasetParameterLabel);
				if (dsLabel == null || dsLabel.trim().equals("")) {
					// BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					// List params = new ArrayList();
					// params.add(biobj.getName());
					// this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSet", params,
					// "component_scheduler_messages"));
				}
				if (datasetParameterLabel == null || datasetParameterLabel.trim().equals("")) {
					// BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					// List params = new ArrayList();
					// params.add(biobj.getName());
					// this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSetParameter", params,
					// "component_scheduler_messages"));
				}
			}
			boolean useExpression = request.optBoolean("useExpression");
			dispatchContext.setUseExpression(useExpression);
			if (useExpression) {
				String expression = request.optString("expression");
				dispatchContext.setExpression(expression);
				if (expression == null || expression.trim().equals("")) {
					// BIObject biobj = DAOFactory.optBIObjectDAO().loadBIObjectById(biobId);
					// List params = new ArrayList();
					// params.add(biobj.optName());
					// this.optErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingExpression", params,
					// "component_scheduler_messages"));
				}
			}

			if (!useFixedRecipients && !useDataset && !useExpression) {
				// BIObject biobj = DAOFactory.optBIObjectDAO().loadBIObjectById(biobId);
				// List params = new ArrayList();
				// params.add(biobj.optName());
				// this.optErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingRecipients", params,
				// "component_scheduler_messages"));
			}

			String mailsubj = request.optString("mailsubj");
			dispatchContext.setMailSubj(mailsubj);
			String mailtxt = request.optString("mailtxt");
			dispatchContext.setMailTxt(mailtxt);

			// Mail
			boolean zipMailDocument = request.optBoolean("zipMailDocument");
			dispatchContext.setZipMailDocument(zipMailDocument);

			boolean reportNameInSubject = request.optBoolean("reportNameInSubject");
			dispatchContext.setReportNameInSubject(reportNameInSubject);

			boolean uniqueMail = request.optBoolean("uniqueMail");
			dispatchContext.setUniqueMail(uniqueMail);

			// set File Name if chosen
			String containedFileName = request.optString("containedFileName");
			if (containedFileName != null && !containedFileName.equals("")) {
				dispatchContext.setContainedFileName(containedFileName);
			}

			// set Zip File Name if chosen
			String zipMailName = request.optString("zipMailName");
			if (zipMailName != null && !zipMailName.equals("")) {
				dispatchContext.setZipMailName(zipMailName);
			}
		}
	}

	private static void getSaveAsDistributionListOptions(JSONObject request, DispatchContext dispatchContext) throws EMFUserError, JSONException {
		Boolean sendtodl = request.optBoolean("saveasd");
		if (sendtodl) {
			dispatchContext.setDistributionListDispatchChannelEnabled(true);
			return;
			// dispatchContext.setBiobjId(request.getInt("id"));
			// List dlist = DAOFactory.getDistributionListDAO().loadAllDistributionLists();
			// Iterator it = dlist.iterator();
			// while (it.hasNext()) {
			// DistributionList dl = (DistributionList) it.next();
			// int dlId = dl.getId();
			// String listID = (String) request.getAttribute("sendtodl_" + dlId + "_" + biobId + "__" + index);
			// if (listID != null) {
			// dispatchContext.addDlId(new Integer(listID));
			// } else {
			// String triggername = (String) request.getAttribute("triggername");
			// DAOFactory.getDistributionListDAO().eraseDistributionListObjects(dl, biobId, triggername);
			// }
			//
			// }

		}
	}

	// Load of JobTrigger
	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static JobTrigger getJobTriggerInfo(String jobName, String jobGroupName, String triggerName, String triggerGroup) {
		try {
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String respStr_gt = schedulerService.getJobSchedulationDefinition(triggerName, triggerGroup);
			SourceBean triggerDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(respStr_gt);
			String respStr_gj = schedulerService.getJobDefinition(jobName, jobGroupName);
			SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(respStr_gj);
			if (triggerDetailSB != null) {
				if (jobDetailSB != null) {
					JobTrigger jt = getJobTriggerFromTriggerSourceBean(triggerDetailSB, jobDetailSB);
					return jt;
					// TriggerInfo tInfo = SchedulerUtilities.getTriggerInfoFromTriggerSourceBean(triggerDetailSB, jobDetailSB);
					// return tInfo;
				} else {
					throw new Exception("Detail not recovered for job " + jobName + "associated to trigger " + triggerName);
				}
			} else {
				throw new Exception("Detail not recovered for trigger " + triggerName);
			}

		} catch (Exception ex) {

			throw new SpagoBIRuntimeException("Error while getting detail of the schedule(trigger)", ex);
		}
	}

	private static JobTrigger getJobTriggerFromTriggerSourceBean(SourceBean triggerInfoSB, SourceBean jobInfoSB) {

		JobTrigger triggerInfo = new JobTrigger();
		String triggerName = (String) triggerInfoSB.getAttribute("triggerName");
		String triggerDescription = (String) triggerInfoSB.getAttribute("triggerDescription");
		String startdate = (String) triggerInfoSB.getAttribute("triggerStartDate");
		String startTime = (String) triggerInfoSB.getAttribute("triggerStartTime");
		String chronString = (String) triggerInfoSB.getAttribute("triggerChronString");
		String endDate = (String) triggerInfoSB.getAttribute("triggerEndDate");
		if (endDate == null) {
			endDate = "";
		}
		String endTime = (String) triggerInfoSB.getAttribute("triggerEndTime");
		if (endTime == null) {
			endTime = "";
		}
		String triggerRepeatInterval = (String) triggerInfoSB.getAttribute("triggerRepeatInterval");
		if (triggerRepeatInterval == null) {
			triggerRepeatInterval = "";
		}
		triggerInfo.setEndDate(endDate);
		triggerInfo.setEndTime(endTime);
		triggerInfo.setRepeatInterval(triggerRepeatInterval);
		triggerInfo.setStartDate(startdate);
		triggerInfo.setStartTime(startTime);
		triggerInfo.setChrono(chronString);
		triggerInfo.setTriggerDescription(triggerDescription);
		triggerInfo.setTriggerName(triggerName);

		JobInfo jobInfo = SchedulerUtilities.getJobInfoFromJobSourceBean(jobInfoSB);
		triggerInfo.setJobInfo(jobInfo);

		Map<String, DispatchContext> saveOptions = new HashMap<String, DispatchContext>();
		List<Integer> biobjIds = jobInfo.getDocumentIds();
		int index = 0;
		for (Integer biobjId : biobjIds) {
			index++;
			DispatchContext dispatchContext = new DispatchContext();
			SourceBean dispatchContextSB = (SourceBean) triggerInfoSB.getFilteredSourceBeanAttribute("JOB_PARAMETERS.JOB_PARAMETER", "name", "biobject_id_"
					+ biobjId.toString() + "__" + index);
			if (dispatchContextSB != null) {
				String encodedDispatchContext = (String) dispatchContextSB.getAttribute("value");
				dispatchContext = SchedulerUtilities.decodeDispatchContext(encodedDispatchContext);
			}
			saveOptions.put(biobjId + "__" + index, dispatchContext);
		}

		triggerInfo.setSaveOptions(saveOptions);

		return triggerInfo;
	}

	public static JSONArray serializeSaveOptions(JobTrigger triggerInfo) {
		JSONArray saveOptionsJSONArray = new JSONArray();

		try {
			IBIObjectDAO biObjectDAO = DAOFactory.getBIObjectDAO();

			Map<String, DispatchContext> saveOptions = triggerInfo.getSaveOptions();
			if (!saveOptions.isEmpty()) {
				// iterate Map
				for (Map.Entry<String, DispatchContext> entry : saveOptions.entrySet()) {
					String objIdentifier = entry.getKey();
					if (objIdentifier.contains("__")) {
						JSONObject documentJSONObject = new JSONObject();

						String objId = objIdentifier.substring(0, objIdentifier.indexOf("__"));
						BIObject biObject = biObjectDAO.loadBIObjectById(Integer.valueOf(objId));
						if (biObject != null) {
							String documentLabel = biObject.getLabel();
							documentJSONObject.put("documentLabel", documentLabel);

							DispatchContext dispatchContext = entry.getValue();
							if (dispatchContext != null) {
								String mailTos = dispatchContext.getMailTos();
								documentJSONObject.put("mailTos", mailTos);
								String zipMailName = dispatchContext.getZipMailName();
								if (zipMailName == null) {
									zipMailName = "";
								}
								documentJSONObject.put("zipMailName", zipMailName);
								String datasetLabel = dispatchContext.getDataSetLabel();

								if (datasetLabel == null) {
									datasetLabel = "";
								}
								documentJSONObject.put("datasetLabel", datasetLabel);
								String mailSubject = dispatchContext.getMailSubj();
								documentJSONObject.put("mailSubject", mailSubject);
								String mailTxt = dispatchContext.getMailTxt();
								documentJSONObject.put("mailTxt", mailTxt);
								String containedFileName = dispatchContext.getContainedFileName();
								if (containedFileName == null) {
									containedFileName = "";
								}
								documentJSONObject.put("containedFileName", containedFileName);

							}

							// put JSONObject in JSONArray
							saveOptionsJSONArray.put(documentJSONObject);
						}
					}
				}
			}

		} catch (Exception ex) {

			throw new SpagoBIRuntimeException("Cannot fill response container", ex);
		}

		return saveOptionsJSONArray;
	}

}
