/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.scheduler.utils;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.tools.scheduler.Formula;
import it.eng.spagobi.tools.scheduler.FormulaParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.RuntimeLoadingParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.to.TriggerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SchedulerUtilities {

	/**
	 * Gets the named history snapshot.
	 * 
	 * @param allsnapshots
	 *            the allsnapshots
	 * @param namesnap
	 *            the namesnap
	 * @param hist
	 *            the hist
	 * 
	 * @return the named history snapshot
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Snapshot getNamedHistorySnapshot(List allsnapshots, String namesnap, int hist) throws Exception {
		Map snapshots = new HashMap();
		List snapDates = new ArrayList();
		Iterator iterAllSnap = allsnapshots.iterator();
		while (iterAllSnap.hasNext()) {
			Snapshot snap = (Snapshot) iterAllSnap.next();
			if (snap.getName().equals(namesnap)) {
				Date creationDate = snap.getDateCreation();
				Long creationLong = new Long(creationDate.getTime());
				snapDates.add(creationLong);
				snapshots.put(creationLong, snap);
			}
		}
		// check if history is out of range
		if ((hist < 0) || (snapDates.size() - 1 < hist)) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, SchedulerUtilities.class.getName(), "getNamedHistorySnapshot", "History step out of range");
			throw new Exception("History step out of range");
		}
		// get the right snapshot
		Collections.sort(snapDates);
		Collections.reverse(snapDates);
		Object key = snapDates.get(hist);
		Snapshot snap = (Snapshot) snapshots.get(key);
		return snap;
	}

	/**
	 * Gets the snapshots by name.
	 * 
	 * @param allsnapshots
	 *            the allsnapshots
	 * @param namesnap
	 *            the namesnap
	 * 
	 * @return the snapshots by name
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static List getSnapshotsByName(List allsnapshots, String namesnap) throws Exception {
		List snaps = new ArrayList();
		Iterator iterAllSnap = allsnapshots.iterator();
		while (iterAllSnap.hasNext()) {
			Snapshot snap = (Snapshot) iterAllSnap.next();
			if (snap.getName().equals(namesnap)) {
				snaps.add(snap);
			}
		}
		return snaps;
	}

	/**
	 * Gets the sB from web service response.
	 * 
	 * @param response
	 *            the response
	 * 
	 * @return the sB from web service response
	 */
	public static SourceBean getSBFromWebServiceResponse(String response) {
		SourceBean schedModRespSB = null;
		try {
			schedModRespSB = SourceBean.fromXMLString(response);
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, SchedulerUtilities.class.getName(), "getSBFromWebServiceResponse",
					"Error while parsing service response", e);
		}
		return schedModRespSB;
	}

	/**
	 * Check result of ws call.
	 * 
	 * @param resultSB
	 *            the result sb
	 * 
	 * @return true, if successful
	 */
	public static boolean checkResultOfWSCall(SourceBean resultSB) {
		boolean result = true;
		SourceBean execOutSB = null;
		if (!resultSB.getName().equals("EXECUTION_OUTCOME")) {
			execOutSB = (SourceBean) resultSB.getAttribute("EXECUTION_OUTCOME");
		} else {
			execOutSB = resultSB;
		}
		if (execOutSB != null) {
			String outcome = (String) execOutSB.getAttribute("outcome");
			if (outcome.equalsIgnoreCase("fault")) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Gets the job info from job source bean.
	 * 
	 * @param jobDetSB
	 *            the job det sb
	 * 
	 * @return the job info from job source bean
	 */
	public static JobInfo getJobInfoFromJobSourceBean(SourceBean jobDetSB) {
		JobInfo jobInfo = new JobInfo();
		try {
			List biobjects = new ArrayList();
			String jobNameRecovered = (String) jobDetSB.getAttribute("jobName");
			String jobDescriptionRecovered = (String) jobDetSB.getAttribute("jobDescription");
			String jobGroupNameRecovered = (String) jobDetSB.getAttribute("jobGroupName");
			jobInfo.setJobName(jobNameRecovered);
			jobInfo.setJobDescription(jobDescriptionRecovered);
			jobInfo.setJobGroupName(jobGroupNameRecovered);
			// set all documents and parameters
			SourceBean jobParSB = (SourceBean) jobDetSB.getAttribute("JOB_PARAMETERS");
			if (jobParSB != null) {
				IBIObjectDAO biobjdao = DAOFactory.getBIObjectDAO();
				IBIObjectParameterDAO biobjpardao = DAOFactory.getBIObjectParameterDAO();
				SourceBean docLblSB = (SourceBean) jobParSB.getFilteredSourceBeanAttribute("JOB_PARAMETER", "name", "documentLabels");
				String docLblStr = (String) docLblSB.getAttribute("value");
				String[] docLbls = docLblStr.split(",");
				for (int i = 0; i < docLbls.length; i++) {
					// BIObject biobj = biobjdao.loadBIObjectByLabel(docLbls[i]);
					BIObject biobj = biobjdao.loadBIObjectByLabel(docLbls[i].substring(0, docLbls[i].indexOf("__")));
					List biobjpars = biobjpardao.loadBIObjectParametersById(biobj.getId());
					biobj.setBiObjectParameters(biobjpars);
					String biobjlbl = biobj.getLabel() + "__" + (i + 1);
					SourceBean queryStringSB = (SourceBean) jobParSB.getFilteredSourceBeanAttribute("JOB_PARAMETER", "name", biobjlbl);
					SourceBean iterativeSB = (SourceBean) jobParSB.getFilteredSourceBeanAttribute("JOB_PARAMETER", "name", biobjlbl + "_iterative");
					List iterativeParameters = new ArrayList();
					if (iterativeSB != null) {
						String iterativeParametersStr = (String) iterativeSB.getAttribute("value");
						String[] iterativeParametersArray = iterativeParametersStr.split(";");
						iterativeParameters.addAll(Arrays.asList(iterativeParametersArray));
					}
					SourceBean loadAtRuntimeSB = (SourceBean) jobParSB.getFilteredSourceBeanAttribute("JOB_PARAMETER", "name", biobjlbl + "_loadAtRuntime");
					Map<String, String> loadAtRuntimeParameters = new HashMap<String, String>();
					if (loadAtRuntimeSB != null) {
						String loadAtRuntimeStr = (String) loadAtRuntimeSB.getAttribute("value");
						String[] loadAtRuntimeArray = loadAtRuntimeStr.split(";");
						for (int count = 0; count < loadAtRuntimeArray.length; count++) {
							String loadAtRuntime = loadAtRuntimeArray[count];
							int parameterUrlNameIndex = loadAtRuntime.lastIndexOf("(");
							String parameterUrlName = loadAtRuntime.substring(0, parameterUrlNameIndex);
							String userAndRole = loadAtRuntime.substring(parameterUrlNameIndex + 1, loadAtRuntime.length() - 1);
							loadAtRuntimeParameters.put(parameterUrlName, userAndRole);
						}
					}
					SourceBean useFormulaSB = (SourceBean) jobParSB.getFilteredSourceBeanAttribute("JOB_PARAMETER", "name", biobjlbl + "_useFormula");
					Map<String, String> useFormulaParameters = new HashMap<String, String>();
					if (useFormulaSB != null) {
						String useFormulaStr = (String) useFormulaSB.getAttribute("value");
						String[] useFormulaArray = useFormulaStr.split(";");
						for (int count = 0; count < useFormulaArray.length; count++) {
							String useFormula = useFormulaArray[count];
							int parameterUrlNameIndex = useFormula.lastIndexOf("(");
							String parameterUrlName = useFormula.substring(0, parameterUrlNameIndex);
							String fName = useFormula.substring(parameterUrlNameIndex + 1, useFormula.length() - 1);
							useFormulaParameters.put(parameterUrlName, fName);
						}
					}

					String queryString = (String) queryStringSB.getAttribute("value");
					String[] parCouples = queryString.split("%26");
					Iterator iterbiobjpar = biobjpars.iterator();
					while (iterbiobjpar.hasNext()) {
						BIObjectParameter biobjpar = (BIObjectParameter) iterbiobjpar.next();
						if (iterativeParameters.contains(biobjpar.getParameterUrlName())) {
							biobjpar.setIterative(true);
						} else {
							biobjpar.setIterative(false);
						}
						if (loadAtRuntimeParameters.containsKey(biobjpar.getParameterUrlName())) {
							RuntimeLoadingParameterValuesRetriever strategy = new RuntimeLoadingParameterValuesRetriever();
							String userRoleStr = loadAtRuntimeParameters.get(biobjpar.getParameterUrlName());
							String[] userRole = userRoleStr.split("\\|");
							strategy.setUserIndentifierToBeUsed(userRole[0]);
							strategy.setRoleToBeUsed(userRole[1]);
							biobjpar.setParameterValuesRetriever(strategy);
						} else if (useFormulaParameters.containsKey(biobjpar.getParameterUrlName())) {
							FormulaParameterValuesRetriever strategy = new FormulaParameterValuesRetriever();
							String fName = useFormulaParameters.get(biobjpar.getParameterUrlName());
							Formula f = Formula.getFormula(fName);
							strategy.setFormula(f);
							biobjpar.setParameterValuesRetriever(strategy);
						} else {
							for (int j = 0; j < parCouples.length; j++) {
								String parCouple = parCouples[j];
								String[] parDef = parCouple.split("=");
								if (biobjpar.getParameterUrlName().equals(parDef[0])) {
									String parameterValues = parDef[1];
									String[] valuesArr = parameterValues.split(";");
									List values = Arrays.asList(valuesArr);
									biobjpar.setParameterValues(values);
									break;
								}
							}
						}
					}
					// calculate parameter
					biobjects.add(biobj);
				}
				jobInfo.setDocuments(biobjects);
			}
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, SchedulerUtilities.class.getName(), "getJobInfoFromJobSourceBean",
					"Error while extracting job info from xml", e);
		}
		return jobInfo;
	}

	/**
	 * Gets the trigger info from trigger source bean.
	 * 
	 * @param triggerInfoSB
	 *            the trigger det sb
	 * @param jobInfoSB
	 *            the job det sb
	 * 
	 * @return the trigger info from trigger source bean
	 */
	public static TriggerInfo getTriggerInfoFromTriggerSourceBean(SourceBean triggerInfoSB, SourceBean jobInfoSB) {

		TriggerInfo triggerInfo = new TriggerInfo();
		String triggerName = (String) triggerInfoSB.getAttribute("triggerName");
		String triggerDescription = (String) triggerInfoSB.getAttribute("triggerDescription");
		String startdate = (String) triggerInfoSB.getAttribute("triggerStartDate");
		String startTime = (String) triggerInfoSB.getAttribute("triggerStartTime");
		String chronString = (String) triggerInfoSB.getAttribute("triggerChronString");
		String endDate = (String) triggerInfoSB.getAttribute("triggerEndDate");
		if (endDate == null)
			endDate = "";
		String endTime = (String) triggerInfoSB.getAttribute("triggerEndTime");
		if (endTime == null)
			endTime = "";
		String triggerRepeatInterval = (String) triggerInfoSB.getAttribute("triggerRepeatInterval");
		if (triggerRepeatInterval == null)
			triggerRepeatInterval = "";
		triggerInfo.setEndDate(endDate);
		triggerInfo.setEndTime(endTime);
		triggerInfo.setRepeatInterval(triggerRepeatInterval);
		triggerInfo.setStartDate(startdate);
		triggerInfo.setStartTime(startTime);
		triggerInfo.setChronString(chronString);
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

	/**
	 * From save info string.
	 * 
	 * @param encodedDispatchContext
	 *            the encoded dispatch context
	 * 
	 * @return the save info
	 */
	public static DispatchContext decodeDispatchContext(String encodedDispatchContext) {
		DispatchContext dispatchContext = new DispatchContext();

		String[] couples = encodedDispatchContext.split("%26");
		for (int i = 0; i < couples.length; i++) {
			String couple = couples[i];
			if (couple.trim().equals("")) {
				continue;
			}
			String[] couplevals = couple.split("=");
			String name = couplevals[0];
			String value = couplevals[1];

			if (name.equals("saveasfile")) {
				dispatchContext.setFileSystemDisptachChannelEnabled(true);
			}
			if (name.equals("destinationfolder")) {
				dispatchContext.setDestinationFolder(value);
			}
			if (name.equals("isprocessmonitoringenabled")) {
				if ("true".equalsIgnoreCase(value)) {
					dispatchContext.setProcessMonitoringEnabled(true);
				} else {
					dispatchContext.setProcessMonitoringEnabled(false);
				}

			}
			if (name.equals("isrelativetoresourcefolder")) {
				if ("true".equalsIgnoreCase(value)) {
					dispatchContext.setDestinationFolderRelativeToResourceFolder(true);
				} else {
					dispatchContext.setDestinationFolderRelativeToResourceFolder(false);
				}

			}
			if (name.equals("functionalitytreefolderlabel")) {
				dispatchContext.setFunctionalityTreeFolderLabel(value);
			}
			if (name.equals("owner")) {
				dispatchContext.setOwner(value);
			}
			if (name.equals("saveassnapshot")) {
				dispatchContext.setSnapshootDispatchChannelEnabled(true);
			}
			if (name.equals("snapshotname")) {
				dispatchContext.setSnapshotName(value);
			}
			if (name.equals("snapshotdescription")) {
				dispatchContext.setSnapshotDescription(value);
			}
			if (name.equals("snapshothistorylength")) {
				dispatchContext.setSnapshotHistoryLength(value);
			}
			if (name.equals("sendtojavaclass")) {
				dispatchContext.setJavaClassDispatchChannelEnabled(true);
			}
			if (name.equals("javaclasspath")) {
				dispatchContext.setJavaClassPath(value);
			}
			if (name.equals("saveasdocument")) {
				dispatchContext.setFunctionalityTreeDispatchChannelEnabled(true);
			}
			if (name.equals("documentname")) {
				dispatchContext.setDocumentName(value);
			}
			if (name.equals("documentdescription")) {
				dispatchContext.setDocumentDescription(value);
			}
			if (name.equals("documenthistorylength")) {
				dispatchContext.setDocumentHistoryLength(value);
			}
			if (name.equals("datasetFolderLabel")) {
				dispatchContext.setUseFolderDataSet(true);
				dispatchContext.setDataSetFolderLabel(value);
			}
			if (name.equals("datasetFolderParameterLabel")) {
				dispatchContext.setDataSetFolderParameterLabel(value);
			}
			if (name.equals("functionalityids")) {
				dispatchContext.setUseFixedFolder(true);
				dispatchContext.setFunctionalityIds(value);
			}
			if (name.equals("sendmail")) {
				dispatchContext.setMailDispatchChannelEnabled(true);
			}
			if (name.equals("mailtos")) {
				dispatchContext.setUseFixedRecipients(true);
				dispatchContext.setMailTos(value);
			}
			if (name.equals("datasetLabel")) {
				dispatchContext.setUseDataSet(true);
				dispatchContext.setDataSetLabel(value);
			}
			if (name.equals("datasetParameterLabel")) {
				dispatchContext.setDataSetParameterLabel(value);
			}
			if (name.equals("expression")) {
				dispatchContext.setUseExpression(true);
				dispatchContext.setExpression(value);
			}
			if (name.equals("mailsubj")) {
				dispatchContext.setMailSubj(value);
			}
			if (name.equals("mailtxt")) {
				dispatchContext.setMailTxt(value);
			}
			if (name.equals("sendtodl")) {
				dispatchContext.setDistributionListDispatchChannelEnabled(true);
			}
			if (name.equals("dlId")) {

				String[] dlIds = value.split(",");
				for (int j = 0; j < dlIds.length; j++) {
					String dlId = dlIds[j];
					dispatchContext.addDlId(new Integer(dlId));
				}

				dispatchContext.setDistributionListDispatchChannelEnabled(true);
			}

			// Mail
			if (name.equals("zipMailDocument")) {
				dispatchContext.setZipMailDocument(true);
			}
			if (name.equals("zipMailName")) {
				dispatchContext.setZipMailName(value);
			}
			if (name.equals("containedFileName")) {
				dispatchContext.setContainedFileName(value);
			}

			if (name.equals("uniqueMail")) {
				dispatchContext.setUniqueMail(true);
			}

			if (name.equals("reportNameInSubject")) {
				dispatchContext.setReportNameInSubject(true);
			}

			// File
			if (name.equals("zipFileDocument")) {
				dispatchContext.setZipFileDocument(true);
			}
			if (name.equals("fileName")) {
				dispatchContext.setFileName(value);
			}
			if (name.equals("zipFileName")) {
				dispatchContext.setZipFileName(value);
			}

		}
		return dispatchContext;
	}

}
