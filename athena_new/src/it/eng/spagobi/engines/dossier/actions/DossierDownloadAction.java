/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.actions;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.dossier.bo.DossierPresentation;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;
import it.eng.spagobi.engines.dossier.dao.DossierDAOHibImpl;
import it.eng.spagobi.engines.dossier.dao.IDossierDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;


public class DossierDownloadAction extends AbstractHttpAction {
	
	public static final String ACTION_NAME = "DOSSIER_DOWNLOAD_ACTION";
	
	static private Logger logger = Logger.getLogger(DossierDownloadAction.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception {
		logger.debug("IN");
		freezeHttpResponse();
		HttpServletResponse response = getHttpResponse();
		OutputStream out = null;
		String task = "";
		try{
	 		task = (String) serviceRequest.getAttribute(DossierConstants.DOSSIER_SERVICE_TASK);		
	 		out = response.getOutputStream();
	 		if(task.equalsIgnoreCase(DossierConstants.DOSSIER_SERVICE_TASK_GET_TEMPLATE_IMAGE)){
	 			String logicalNameForStoring = (String)serviceRequest.getAttribute(DossierConstants.DOSSIER_SERVICE_IMAGE);
			 	File imgFile = new File(DossierDAOHibImpl.tempBaseFolder, logicalNameForStoring + ".jpg");
			 	preventPathTraversalAttacks(imgFile);
			 	FileInputStream fis = new FileInputStream(imgFile);
			 	byte[] content = GeneralUtilities.getByteArrayFromInputStream(fis);
			 	out.write(content);
			 	out.flush();
	            fis.close();
	            imgFile.delete();
	            return;
	 		} else if(task.equalsIgnoreCase(DossierConstants.DOSSIER_SERVICE_TASK_DOWN_FINAL_DOC)){
	 			String activityKey = (String) serviceRequest.getAttribute(SpagoBIConstants.ACTIVITYKEY);
	 			JbpmContext jbpmContext = null;
	 			Integer dossierId = null;
	 			Long workflowProcessId = null;
	 			try {
		 			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
		 	    	        jbpmContext = jbpmConfiguration.createJbpmContext();
		 			long activityKeyId = Long.valueOf(activityKey).longValue();
		 			TaskInstance taskInstance = jbpmContext.getTaskInstance(activityKeyId);
		 			ContextInstance contextInstance = taskInstance.getContextInstance();
		 			ProcessInstance processInstance = contextInstance.getProcessInstance();
		 			workflowProcessId = new Long(processInstance.getId());
		 			String dossierIdStr = (String) contextInstance.getVariable(DossierConstants.DOSSIER_ID);
		 			dossierId = new Integer(dossierIdStr);
	 			} finally {
	 				if (jbpmContext != null) jbpmContext.close();
	 			}
	 			if (dossierId != null) {
	 				BIObject dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(dossierId);
	 				IDossierPresentationsDAO dpDAO = DAOFactory.getDossierPresentationDAO();
	 				DossierPresentation presentation = dpDAO.getCurrentPresentation(dossierId, workflowProcessId);
	 				byte[] finalDocBytes = presentation.getContent();
				 	response.setHeader("Content-Disposition","attachment; filename=\"" + dossier.getName() + ".ppt" + "\";");
		 			response.setContentLength(finalDocBytes.length);
		 			out.write(finalDocBytes);
		 			out.flush();
	 			} else {
	 				logger.error("Dossier configuration path not found!");
	 			}
	            return;
		 		
	 		} else if(task.equalsIgnoreCase(DossierConstants.DOSSIER_SERVICE_TASK_DOWN_PRESENTATION_VERSION)) {
	 			String dossierIdStr = (String) serviceRequest.getAttribute(DossierConstants.DOSSIER_ID);
	 			Integer dossierId = new Integer(dossierIdStr);
	 			String versionStr = (String) serviceRequest.getAttribute(DossierConstants.VERSION_ID);
	 			Integer versionId = new Integer(versionStr);
 				BIObject dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(dossierId);
 				IDossierPresentationsDAO dpDAO = DAOFactory.getDossierPresentationDAO();
 				byte[] finalDocBytes = dpDAO.getPresentationVersionContent(dossierId, versionId);
			 	response.setHeader("Content-Disposition","attachment; filename=\"" + dossier.getName() + ".ppt" + "\";");
	 			response.setContentLength(finalDocBytes.length);
	 			out.write(finalDocBytes);
	            return;
	            
	 		} else if(task.equalsIgnoreCase(DossierConstants.DOSSIER_SERVICE_TASK_DOWN_OOTEMPLATE)) {
	 			String tempFolder = (String) serviceRequest.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
	 			IDossierDAO dossierDao = new DossierDAOHibImpl();
	 			String templateFileName = dossierDao.getPresentationTemplateFileName(tempFolder);
	 			InputStream templateIs = dossierDao.getPresentationTemplateContent(tempFolder);
	 			byte[] templateByts = GeneralUtilities.getByteArrayFromInputStream(templateIs);
	 			response.setHeader("Content-Disposition","attachment; filename=\"" + templateFileName + "\";");
	 			response.setContentLength(templateByts.length);
	 			out.write(templateByts);
	 			out.flush();
	            return;
	 			
	 		} else if(task.equalsIgnoreCase(DossierConstants.DOSSIER_SERVICE_TASK_DOWN_WORKFLOW_DEFINITION)) {
	 			String tempFolder = (String) serviceRequest.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
	 			IDossierDAO dossierDao = new DossierDAOHibImpl();
	 			String workDefName = dossierDao.getProcessDefinitionFileName(tempFolder);
	 			InputStream workIs = dossierDao.getProcessDefinitionContent(tempFolder);
	 			byte[] workByts = GeneralUtilities.getByteArrayFromInputStream(workIs);
	 			response.setHeader("Content-Disposition","attachment; filename=\"" + workDefName + "\";");
	 			response.setContentLength(workByts.length);
	 			out.write(workByts);
	 			out.flush();
	            return;
	 		}
	 		logger.debug("OUT");
	 	} catch(Exception e) {
	 		logger.error("Exception during execution of task " + task, e);
	 	}
	 }

	private void preventPathTraversalAttacks(File imgFile) {
	 	File parent = imgFile.getParentFile();
	 	// Prevent directory traversal (path traversal) attacks
	 	if (!parent.equals(DossierDAOHibImpl.tempBaseFolder)) {
	 		logger.error("Trying to access the file [" + imgFile.getAbsolutePath() 
	 	                 + "] that is not inside [" + DossierDAOHibImpl.tempBaseFolder + "]!!!");
	 		throw new SecurityException("Trying to access the file [" 
	 	                 + imgFile.getAbsolutePath() + "] that is not inside [" 
	 	                 + DossierDAOHibImpl.tempBaseFolder.getAbsolutePath() + "]!!!");
	 	}
	}
	
}
