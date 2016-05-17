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
package it.eng.spagobi.tools.scheduler.utils;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SchedulerUtilitiesV2 {

	public static JSONObject isValidJobTrigger(JobTrigger jobt) throws JSONException {

		JSONArray ja = new JSONArray();

		if (jobt.getTriggerName() == null || jobt.getTriggerName().trim().isEmpty()) {
			ja.put("Empty name");
		}

		boolean validStartDate = true;
		if (jobt.getStartDate() == null || jobt.getStartDate().trim().isEmpty()) {
			ja.put("Null or not Valid Start date");
			validStartDate = false;
		}
		if (jobt.getStartTime() == null || jobt.getStartTime().trim().isEmpty()) {
			ja.put("Null start time");
			validStartDate = false;
		} else {
			String[] tp = jobt.getStartTime().split(":");
			int h = Integer.parseInt(tp[0]);
			int m = Integer.parseInt(tp[1]);
			if (h < 0 || h > 23) {
				ja.put(" start time hours not valid ");
				validStartDate = false;
			}
			if (m < 0 || m > 59) {
				ja.put(" start time minutes not valid ");
				validStartDate = false;
			}
		}

		if (validStartDate && (jobt.getEndDate() != null && !jobt.getEndDate().equals(""))) {
			boolean validTime = true;
			String[] tp = jobt.getEndTime().split(":");
			int h = Integer.parseInt(tp[0]);
			int m = Integer.parseInt(tp[1]);
			if (h < 0 || h > 23) {
				ja.put(" end time hours not valid ");
				validTime = false;
			}
			if (m < 0 || m > 59) {
				ja.put(" end time minutes not valid ");
				validTime = false;
			}

			if (validTime) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
				try {
					Date dateStart = sdf.parse(jobt.getStartDate() + " " + jobt.getStartTime());
					Date dateEnd = sdf.parse(jobt.getEndDate() + " " + jobt.getEndTime());
					if (dateEnd.before(dateStart)) {
						ja.put(" End time is before Start time  ");
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		// TODO controls in documents data

		JSONObject jo = new JSONObject();
		if (ja.length() >= 0) {
			jo.put("Status", "NON OK");
			jo.put("Errors", ja);
		} else {
			jo.put("Status", "OK");
		}
		return jo;

	}

	public static JobTrigger getJobTriggerFromJsonRequest(JSONObject jsonObject, JSONArray jerr) throws Exception {

		JobTrigger jobTrigger = new JobTrigger();
		ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
		String jobDetail = schedulerService.getJobDefinition((String) jsonObject.opt(JobTrigger.JOB_NAME), (String) jsonObject.opt(JobTrigger.JOB_GROUP));
		SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(jobDetail);
		if (jobDetailSB == null) {
			throw new Exception("Cannot recover job " + (String) jsonObject.opt(JobTrigger.JOB_NAME));
		}
		JobInfo jobInfo = SchedulerUtilities.getJobInfoFromJobSourceBean(jobDetailSB);
		jobTrigger.setJobInfo(jobInfo);
		jobTrigger.setTriggerName((String) jsonObject.opt(JobTrigger.TRIGGER_NAME));
		if (jobTrigger.getTriggerName() == null || jobTrigger.getTriggerName().trim().isEmpty()) {
			jerr.put("Empty name");
		}
		jobTrigger.setTriggerDescription((String) jsonObject.opt(JobTrigger.TRIGGER_DESCRIPTION));
		jobTrigger.setStartDate(jsonObject.optString(JobTrigger.START_DATE));
		jobTrigger.setStartTime(jsonObject.optString(JobTrigger.START_TIME));

		boolean validStartDate = true;
		if (jobTrigger.getStartDate() == null || jobTrigger.getStartDate().trim().isEmpty()) {
			jerr.put("Null or not Valid Start date");
			validStartDate = false;
		}
		if (jobTrigger.getStartTime() == null || jobTrigger.getStartTime().trim().isEmpty()) {
			jerr.put("Null start time");
			validStartDate = false;
		} else {
			String[] tp = jobTrigger.getStartTime().split(":");
			int h = Integer.parseInt(tp[0]);
			int m = Integer.parseInt(tp[1]);
			if (h < 0 || h > 23) {
				jerr.put(" start time hours not valid ");
				validStartDate = false;
			}
			if (m < 0 || m > 59) {
				jerr.put(" start time minutes not valid ");
				validStartDate = false;
			}
		}

		jobTrigger.setEndDate(jsonObject.optString(JobTrigger.END_DATE));
		jobTrigger.setEndTime(jsonObject.optString(JobTrigger.END_TIME));

		if (validStartDate && (jobTrigger.getEndDate() != null && !jobTrigger.getEndDate().equals(""))) {
			boolean validTime = true;
			String[] tp = jobTrigger.getEndTime().split(":");
			int h = Integer.parseInt(tp[0]);
			int m = Integer.parseInt(tp[1]);
			if (h < 0 || h > 23) {
				jerr.put(" end time hours not valid ");
				validTime = false;
			}
			if (m < 0 || m > 59) {
				jerr.put(" end time minutes not valid ");
				validTime = false;
			}

			if (validTime) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
				try {
					Date dateStart = sdf.parse(jobTrigger.getStartDate() + " " + jobTrigger.getStartTime());
					Date dateEnd = sdf.parse(jobTrigger.getEndDate() + " " + jobTrigger.getEndTime());
					if (dateEnd.before(dateStart)) {
						jerr.put(" End time is before Start time  ");
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		jobTrigger.setChrono(((JSONObject) jsonObject.opt(JobTrigger.CHRONO)).toString().replaceAll("\"", "'"));
		JSONArray ja = (JSONArray) jsonObject.opt(JobTrigger.DOCUMENTS);
		Map<String, DispatchContext> saveOptions = getSaveOptionsFromRequest(ja, jerr);
		jobTrigger.setSaveOptions(saveOptions);

		return jobTrigger;
	}

	public static JSONObject JobTriggerToJson(JobTrigger trigg) throws EMFUserError, JSONException {
		String xml = getSchedulingMessage(trigg, false, null).toString();
		JSONObject j = new JSONObject();
		j.put("jobName", trigg.getJobInfo().getJobName());
		j.put("jobGroup", trigg.getJobInfo().getJobGroupName());
		j.put("triggerName", trigg.getTriggerName());
		j.put("triggerDescription", trigg.getTriggerDescription());
		j.put("startDate", trigg.getStartDate());
		j.put("startDateRFC3339", trigg.getStartDateRFC3339());
		j.put("startTime", trigg.getStartTime());
		String enddate = trigg.getEndDate();
		if (enddate != null && !enddate.trim().equals("")) {
			j.put("endDate", enddate);
			j.put("endDateRFC3339", trigg.getEndDateRFC3339());
			j.put("endTime", trigg.getEndTime());
		}

		j.put("chrono", new JSONObject(trigg.getChrono()));

		Map<String, DispatchContext> saveOptions = trigg.getSaveOptions();
		Set<String> uniqueDispatchContextNames = saveOptions.keySet();
		JSONArray ja = new JSONArray();

		for (String uniqueDispatchContextName : uniqueDispatchContextNames) {
			DispatchContext dispatchContext = saveOptions.get(uniqueDispatchContextName);
			StringBuffer message = new StringBuffer();

			JSONObject obj = new JSONObject();
			obj.put("labelId", uniqueDispatchContextName);
			String objId = uniqueDispatchContextName.substring(0, uniqueDispatchContextName.indexOf("__"));
			BIObject biObject = DAOFactory.getBIObjectDAO().loadBIObjectById(Integer.valueOf(objId));
			if (biObject != null) {
				obj.put("id", objId);
				obj.put("label", biObject.getLabel());
			}

			SaveAsSnapshotOptionsToJson(dispatchContext, obj);
			SaveAsFileOptionsToJson(dispatchContext, obj);
			SaveAsJavaClassOptionsToJson(dispatchContext, obj);
			SaveAsDocumentOptionsToJson(dispatchContext, obj);
			SaveAsMailOptionsToJson(dispatchContext, obj);
			SaveAsDistributionListOptionsToJson(dispatchContext, obj);

			ja.put(obj);
		}
		j.put("documents", ja);

		return j;
	}

	public static StringBuffer getSchedulingMessage(JobTrigger trigg, boolean runImmediately, IEngUserProfile profile) throws EMFUserError, JSONException {
		StringBuffer message = new StringBuffer();
		JobInfo jobInfo = trigg.getJobInfo();

		message.append("<SERVICE_REQUEST ");

		message.append(" jobName=\"" + jobInfo.getJobName() + "\" ");

		message.append(" jobGroup=\"" + jobInfo.getJobGroupName() + "\" ");
		if (runImmediately) {
			message.append(" runImmediately=\"true\" ");
			message.append(" chronString=\"" + trigg.getChrono() + "\" ");
			message.append(" originalTriggerName=\"" + trigg.getTriggerName() + "\" ");
		} else {
			message.append(" triggerName=\"" + trigg.getTriggerName() + "\" ");

			message.append(" triggerDescription=\"" + trigg.getTriggerDescription() + "\" ");
			message.append(" startDate=\"" + trigg.getStartDate() + "\" ");

			message.append(" startTime=\"" + trigg.getStartTime() + "\" ");

			message.append(" chronString=\"" + trigg.getChrono() + "\" ");

			String enddate = trigg.getEndDate();
			String endtime = trigg.getEndTime();
			if (enddate != null && !enddate.trim().equals("")) {
				message.append(" endDate=\"" + enddate + "\" ");

				if (endtime != null && !endtime.trim().equals("")) {
					message.append(" endTime=\"" + endtime + "\" ");

				}
			}
		}
		String repeatinterval = trigg.getRepeatInterval();
		if (!repeatinterval.trim().equals("")) {
			message.append(" repeatInterval=\"" + repeatinterval + "\" ");

		}
		message.append(">");

		serializeSaveParameterOptions(message, trigg, runImmediately, profile);

		message.append("</SERVICE_REQUEST>");

		return message;

	}

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
	public static Map<String, DispatchContext> getSaveOptionsFromRequest(JSONArray docum, JSONArray jerr) throws EMFUserError, JSONException {
		Map<String, DispatchContext> saveOptions = new HashMap<String, DispatchContext>();
		for (int i = 0; i < docum.length(); i++) {
			DispatchContext dispatchContext = new DispatchContext();
			getSaveAsSnapshotOptions(docum.getJSONObject(i), dispatchContext, jerr);
			getSaveAsFileOptions(docum.getJSONObject(i), dispatchContext, jerr);
			getSaveAsJavaClassOptions(docum.getJSONObject(i), dispatchContext, jerr);
			getSaveAsDocumentOptions(docum.getJSONObject(i), dispatchContext, jerr);
			getSaveAsMailOptions(docum.getJSONObject(i), dispatchContext, jerr);
			getSaveAsDistributionListOptions(docum.getJSONObject(i), dispatchContext, jerr);
			saveOptions.put(docum.getJSONObject(i).getString("labelId"), dispatchContext);
		}
		return saveOptions;
	}

	private static void getSaveAsSnapshotOptions(JSONObject request, DispatchContext dispatchContext, JSONArray jerr) throws JSONException {
		Boolean saveassnap = request.optBoolean("saveassnapshot");
		if (saveassnap) {
			dispatchContext.setSnapshootDispatchChannelEnabled(true);

			String snapshotName = request.optString("snapshotname");
			dispatchContext.setSnapshotName(snapshotName);
			if (snapshotName.trim().equals("")) {
				jerr.put("Empty snapshotName");
			}

			String snapshotDescription = request.optString("snapshotdescription");
			dispatchContext.setSnapshotDescription(snapshotDescription);

			String snapshotHistoryLength = request.optString("snapshothistorylength");
			dispatchContext.setSnapshotHistoryLength(snapshotHistoryLength);
		}
	}

	private static void getSaveAsFileOptions(JSONObject request, DispatchContext dispatchContext, JSONArray jerr) throws JSONException {
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

	private static void getSaveAsJavaClassOptions(JSONObject request, DispatchContext dispatchContext, JSONArray jerr) throws JSONException {
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
				jerr.put("Java class not valid");

			} catch (Exception e) {
				// logger.error("Error in istantiating class");
				EMFValidationError emfError = new EMFValidationError(EMFErrorSeverity.ERROR, "sendtojavaclass", "12100");
				jerr.put("Error in setting java class ");
			}
			dispatchContext.setJavaClassPath(javaClassPath);
		}
	}

	private static void getSaveAsDocumentOptions(JSONObject request, DispatchContext dispatchContext, JSONArray jerr) throws JSONException {
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
					jerr.put("errors.trigger.missingDataSet");
				}
				if (datasetParameterLabel == null || datasetParameterLabel.trim().equals("")) {
					// BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					// List params = new ArrayList();
					// params.add(biobj.getName());
					// this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSetParameter", params,
					// "component_scheduler_messages"));
					jerr.put("errors.trigger.missingDataSetParameter ");
				}

			}
		}
	}

	private static void getSaveAsMailOptions(JSONObject request, DispatchContext dispatchContext, JSONArray jerr) throws EMFUserError, JSONException {
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

	private static void getSaveAsDistributionListOptions(JSONObject request, DispatchContext dispatchContext, JSONArray jerr) throws EMFUserError,
			JSONException {
		Boolean sendtodl = request.optBoolean("saveasdl");
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
								documentJSONObject.put("mailtxt", mailTxt);
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

	private static void SaveAsSnapshotOptionsToJson(DispatchContext dispatchContext, JSONObject jo) throws JSONException {
		if (dispatchContext.isSnapshootDispatchChannelEnabled()) {
			jo.put("saveassnapshot", true);
			if ((dispatchContext.getSnapshotName() != null) && !dispatchContext.getSnapshotName().trim().equals("")) {
				jo.put("snapshotname", dispatchContext.getSnapshotName());
			}
			if ((dispatchContext.getSnapshotDescription() != null) && !dispatchContext.getSnapshotDescription().trim().equals("")) {
				jo.put("snapshotdescription", dispatchContext.getSnapshotDescription());
			}
			if ((dispatchContext.getSnapshotHistoryLength() != null) && !dispatchContext.getSnapshotHistoryLength().trim().equals("")) {
				jo.put("snapshothistorylength", dispatchContext.getSnapshotHistoryLength());
			}
		}
	}

	private static void SaveAsJavaClassOptionsToJson(DispatchContext dispatchContext, JSONObject jo) throws JSONException {
		if (dispatchContext.isJavaClassDispatchChannelEnabled()) {
			jo.put("sendtojavaclass", true);
			if ((dispatchContext.getJavaClassPath() != null) && !dispatchContext.getJavaClassPath().trim().equals("")) {
				jo.put("javaclasspath", dispatchContext.getJavaClassPath());
			}
		}
	}

	private static void SaveAsFileOptionsToJson(DispatchContext dispatchContext, JSONObject jo) throws JSONException {
		if (dispatchContext.isFileSystemDispatchChannelEnabled()) {
			jo.put("saveasfile", true);
			if (StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder())) {
				jo.put("destinationfolder", dispatchContext.getDestinationFolder());
			}
			if (StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder())) {
				jo.put("destinationfolder", dispatchContext.getDestinationFolder());
			}
			jo.put("isrelativetoresourcefolder", dispatchContext.isDestinationFolderRelativeToResourceFolder());
			jo.put("isprocessmonitoringenabled", dispatchContext.isProcessMonitoringEnabled());
		}

		if (dispatchContext.isZipFileDocument()) {
			jo.put("zipFileDocument", true);
		}
		if (dispatchContext.getFileName() != null) {
			jo.put("fileName", dispatchContext.getFileName());
		}

		if (dispatchContext.getZipFileName() != null) {
			jo.put("zipFileName", dispatchContext.getZipFileName());
		}

	}

	private static void SaveAsDocumentOptionsToJson(DispatchContext dispatchContext, JSONObject jo) throws JSONException {
		if (dispatchContext.isFunctionalityTreeDispatchChannelEnabled()) {
			jo.put("saveasdocument", true);
			if ((dispatchContext.getDocumentName() != null) && !dispatchContext.getDocumentName().trim().equals("")) {
				jo.put("documentname", dispatchContext.getDocumentName());
			}
			if ((dispatchContext.getDocumentDescription() != null) && !dispatchContext.getDocumentDescription().trim().equals("")) {
				jo.put("documentdescription", dispatchContext.getDocumentDescription());
			}
			if (dispatchContext.isUseFixedFolder() && dispatchContext.getFoldersTo() != null && !dispatchContext.getFoldersTo().trim().equals("")) {
				jo.put("foldersTo", dispatchContext.getFoldersTo());
			}
			if (dispatchContext.isUseFolderDataSet() && dispatchContext.getDataSetFolderLabel() != null
					&& !dispatchContext.getDataSetFolderLabel().trim().equals("")) {
				jo.put("useFolderDataset", true);
				jo.put("datasetFolderLabel", dispatchContext.getDataSetFolderLabel());
				if (dispatchContext.getDataSetFolderParameterLabel() != null && !dispatchContext.getDataSetFolderParameterLabel().trim().equals("")) {
					jo.put("datasetFolderParameter", dispatchContext.getDataSetFolderParameterLabel());
				}
			}
			if ((dispatchContext.getDocumentHistoryLength() != null) && !dispatchContext.getDocumentHistoryLength().trim().equals("")) {
				jo.put("documenthistorylength", dispatchContext.getDocumentHistoryLength());
			}

			if (dispatchContext.isUseFixedFolder()) {
				jo.put("useFixedFolder", true);
			}

			if ((dispatchContext.getFunctionalityIds() != null) && !dispatchContext.getFunctionalityIds().trim().equals("")) {
				JSONArray ja = new JSONArray();
				String[] st = dispatchContext.getFunctionalityIds().split(",");
				for (String s : st) {
					ja.put(Integer.parseInt(s));
				}
				jo.put("funct", ja);
			}

		}

	}

	private static void SaveAsMailOptionsToJson(DispatchContext dispatchContext, JSONObject jo) throws JSONException {
		if (dispatchContext.isMailDispatchChannelEnabled()) {
			jo.put("sendmail", true);
			if (dispatchContext.isUseFixedRecipients() && dispatchContext.getMailTos() != null && !dispatchContext.getMailTos().trim().equals("")) {
				jo.put("useFixedRecipients", true);
				jo.put("mailtos", dispatchContext.getMailTos());
			}
			if (dispatchContext.isUseDataSet() && dispatchContext.getDataSetLabel() != null && !dispatchContext.getDataSetLabel().trim().equals("")) {
				jo.put("useDataset", true);
				jo.put("datasetLabel", dispatchContext.getDataSetLabel());
				if (dispatchContext.getDataSetParameterLabel() != null && !dispatchContext.getDataSetParameterLabel().trim().equals("")) {
					jo.put("datasetParameter", dispatchContext.getDataSetParameterLabel());
				}
			}
			if (dispatchContext.isUseExpression() && dispatchContext.getExpression() != null && !dispatchContext.getExpression().trim().equals("")) {
				jo.put("useExpression", true);
				jo.put("expression", dispatchContext.getExpression());
			}
			if ((dispatchContext.getMailSubj() != null) && !dispatchContext.getMailSubj().trim().equals("")) {
				jo.put("mailsubj", dispatchContext.getMailSubj());
			}
			if ((dispatchContext.getMailTxt() != null) && !dispatchContext.getMailTxt().trim().equals("")) {
				jo.put("mailtxt", dispatchContext.getMailTxt());
			}

			// Mail
			if (dispatchContext.isZipMailDocument()) {
				jo.put("zipMailDocument", true);
			}
			if (dispatchContext.isReportNameInSubject()) {
				jo.put("reportNameInSubject", true);
			}

			if (dispatchContext.isUniqueMail()) {
				jo.put("uniqueMail", true);
			}

			if (dispatchContext.getContainedFileName() != null) {
				jo.put("containedFileName", dispatchContext.getContainedFileName());
			}
			if (dispatchContext.getZipMailName() != null) {
				jo.put("zipMailName", dispatchContext.getZipMailName());
			}

		}

	}

	private static void SaveAsDistributionListOptionsToJson(DispatchContext dispatchContext, JSONObject jo) throws JSONException {

		if (dispatchContext.isDistributionListDispatchChannelEnabled()) {
			jo.put("saveasdl", true);
		}

	}

	public static JSONArray toJsonTreeLowFunctionality(List<LowFunctionality> functionalities) throws JSONException {
		JSONArray tmp = new JSONArray();
		for (LowFunctionality lf : functionalities) {

			if (lf.getParentId() == null) {
				tmp.put(lowFuncToJson(lf));
			} else {
				for (int i = 0; i < tmp.length(); i++) {
					if (insertDocChild(tmp.getJSONObject(i), lf)) {
						break;
					}
				}
			}

			// funct.put(JSON.parse(JsonConverter.objectToJson(lf, LowFunctionality.class)));
		}
		return tmp;
	}

	private static boolean insertDocChild(JSONObject node, LowFunctionality child) throws JSONException {
		if (node.getInt("id") == child.getParentId()) {
			if (!node.has("childs")) {
				node.put("childs", new JSONArray());
			}
			node.getJSONArray("childs").put(lowFuncToJson(child));
			return true;
		} else {
			if (node.has("childs")) {
				JSONArray t = node.getJSONArray("childs");
				for (int i = 0; i < t.length(); i++) {
					if (insertDocChild(t.getJSONObject(i), child)) {
						return true;
					}
				}
			} else {
				return false;
			}
		}
		return false;
	};

	public static JSONObject lowFuncToJson(LowFunctionality lf) throws JSONException {
		JSONObject tmp = new JSONObject();
		tmp.put("id", lf.getId());
		tmp.put("path", lf.getPath());
		tmp.put("name", lf.getName());
		tmp.put("description", lf.getDescription());
		return tmp;
	}

	public static JSONObject JobInfoToJson(JobInfo job) throws JSONException {
		JSONObject jo = new JSONObject();
		jo.put("jobName", job.getJobName());
		jo.put("jobDescription", job.getJobDescription());
		jo.put("jobGroupName", job.getJobGroupName());

		JSONArray docParam = new JSONArray();
		List<BIObject> listDoc = job.getDocuments();
		for (BIObject b : listDoc) {
			JSONObject docum = new JSONObject();
			docum.put("id", b.getId());
			docum.put("label", b.getLabel());
			docum.put("name", b.getName());
			docum.put("description", b.getDescription());

			List<BIObjectParameter> param = b.getBiObjectParameters();
			JSONArray pararr = new JSONArray();
			for (BIObjectParameter p : param) {
				pararr.put(p.getLabel());
			}
			docum.put("parameters", pararr);

			docParam.put(docum);
		}

		jo.put("documents", docParam);

		return jo;
	}

}
