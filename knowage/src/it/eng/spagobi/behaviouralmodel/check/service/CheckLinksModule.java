/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.check.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.analiticalmodel.document.service.BIObjectsModule;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractHibernateConnectionCheckListModule;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Gioia
 *
 */
public class CheckLinksModule extends AbstractHibernateConnectionCheckListModule {
	private static transient Logger logger = Logger.getLogger(CheckLinksModule.class);
	protected IEngUserProfile profile = null;
	protected String initialPath = null;
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.AbstractBasicCheckListModule#save()
	 */
	public void save() throws Exception {
		logger.debug( "IN" );
		super.save();
		RequestContainer requestContainer = this.getRequestContainer();	
		SessionContainer sessionContainer = requestContainer.getSessionContainer();
		SessionContainer permanentSession = sessionContainer.getPermanentContainer();
		profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String subjectIdName = (String)((SourceBean) config.getAttribute("KEYS.SUBJECT")).getAttribute("key");
		String masterReportIdStr = (String)getAttribute(subjectIdName, _request);
		Integer masterReportId = new Integer(masterReportIdStr);		
		SourceBean checkedObjects = getCheckedObjects();
		List checkedObjectsList = checkedObjects.getAttributeAsList(OBJECT);
				
		try {
			ISubreportDAO subrptdao = DAOFactory.getSubreportDAO();
			subrptdao.setUserProfile(profile);
			subrptdao.eraseSubreportByMasterRptId(masterReportId);
			for(int i = 0; i < checkedObjectsList.size(); i++) {
				SourceBean subreport = (SourceBean)checkedObjectsList.get(i);
				String key = getObjectKey(subreport);
				Integer subReportId = new Integer(key);
				subrptdao.insertSubreport(new Subreport(masterReportId, subReportId));
			}
			
		} catch (Exception e) {
			logger.error("Cannot erase/insert subreports from/into db", e);
		}
		logger.debug( "OUT" );
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.AbstractHibernateConnectionCheckListModule#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		logger.debug( "IN" );
		RequestContainer requestContainer = this.getRequestContainer();	
		SessionContainer sessionContainer = requestContainer.getSessionContainer();
		SessionContainer permanentSession = sessionContainer.getPermanentContainer();
		profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		//String actor = (String) sessionContainer.getAttribute(SpagoBIConstants.ACTOR);
		//String filterOrder = (request.getAttribute("FIELD_ORDER") == null || ((String)request.getAttribute("FIELD_ORDER")).equals(""))?
        //		"label":(String)request.getAttribute("FIELD_ORDER");
		
		String currentFieldOrder = (request.getAttribute("FIELD_ORDER") == null || ((String)request.getAttribute("FIELD_ORDER")).equals(""))?"":(String)request.getAttribute("FIELD_ORDER");
		if (currentFieldOrder.equals("")){
			currentFieldOrder = "label";
			request.delAttribute("FIELD_ORDER");
			request.setAttribute("FIELD_ORDER", currentFieldOrder);
		}
		
		String currentTypOrder = (request.getAttribute("TYPE_ORDER") == null || ((String)request.getAttribute("TYPE_ORDER")).equals(""))?"":(String)request.getAttribute("TYPE_ORDER");		
		if (currentTypOrder.equals("")){
			currentTypOrder = " ASC";
			request.delAttribute("TYPE_ORDER");
			request.setAttribute("TYPE_ORDER",currentTypOrder);			
		}

		String modality = ChannelUtilities.getPreferenceValue(requestContainer, BIObjectsModule.MODALITY, BIObjectsModule.ENTIRE_TREE);
		if (modality != null && modality.equalsIgnoreCase(BIObjectsModule.FILTER_TREE)) {
			initialPath = (String)ChannelUtilities.getPreferenceValue(requestContainer, TreeObjectsModule.PATH_SUBTREE, "");
		}
        String objIdStr = (String) sessionContainer.getAttribute("SUBJECT_ID");
        Integer objId = null;
        if (objIdStr != null) objId = new Integer (objIdStr);
        response.setAttribute("SUBJECT_ID",objIdStr);
		
		PaginatorIFace paginator = new GenericPaginator();		
		IBIObjectDAO objDAO = DAOFactory.getBIObjectDAO();
		List objectsList = null;
		if (initialPath != null && !initialPath.trim().equals("")) {
			objectsList = objDAO.loadAllBIObjectsFromInitialPath(initialPath, currentFieldOrder+ " " + currentTypOrder);
		} else {
			objectsList = objDAO.loadAllBIObjects(currentFieldOrder + " " + currentTypOrder);
		}
		
		String checked = (String)request.getAttribute("checked");
		if (checked  == null )
			checked = (String)request.getAttribute("optChecked");
		if(checked==null){
			checked = "true";
		}
		request.delAttribute("optChecked");
		request.setAttribute("optChecked", checked);
		response.setAttribute("optChecked", (String)request.getAttribute("optChecked"));
		if(checked.equals("true")){
		//if the request is to show only checked objects (it is settled by default when page is loaded at the first time
			for (Iterator it = objectsList.iterator(); it.hasNext(); ) {
				BIObject obj = (BIObject) it.next();
				if (objId != null && obj.getId().equals(objId)) continue;
				//boolean bool = isCheckedObject(obj.getId().toString());
				if(isCheckedObject(obj.getId().toString())){
					SourceBean rowSB = null;
					/*if (SpagoBIConstants.ADMIN_ACTOR.equalsIgnoreCase(actor)) {
						rowSB = makeAdminListRow(obj);
					} else if (SpagoBIConstants.DEV_ACTOR.equalsIgnoreCase(actor)) {
						rowSB = makeDevListRow(obj);
					}
					*/
					if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
						rowSB = makeAdminListRow(obj);
					} else if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) &&
							   profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
						rowSB = makeDevListRow(obj);
					}
						if (rowSB != null) paginator.addRow(rowSB);
				}
			}
		}
		//else if it is false, show all objects
		else if (checked.equals("false")){
			for (Iterator it = objectsList.iterator(); it.hasNext(); ) {
				BIObject obj = (BIObject) it.next();
				if (objId != null && obj.getId().equals(objId)) continue;
				//boolean bool = isCheckedObject(obj.getId().toString());
				SourceBean rowSB = null;
				/*
					if (SpagoBIConstants.ADMIN_ACTOR.equalsIgnoreCase(actor)) {
						rowSB = makeAdminListRow(obj);
					} else if (SpagoBIConstants.DEV_ACTOR.equalsIgnoreCase(actor)) {
						rowSB = makeDevListRow(obj);
					}
					*/
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
					rowSB = makeAdminListRow(obj);
				} else if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) &&
						   profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
					rowSB = makeDevListRow(obj);
				}
				if (rowSB != null) paginator.addRow(rowSB);
			}
		}
		
		ListIFace list = new GenericList();
		list.setPaginator(paginator);
		logger.debug( "OUT" );
		return list;
	}
	
	private SourceBean makeDevListRow(BIObject obj) throws Exception {
		String rowSBStr = "<ROW ";
		rowSBStr += "		OBJ_ID=\"" + obj.getId() + "\"";
		rowSBStr += "		LABEL=\"" + obj.getLabel() + "\"";
		rowSBStr += "		NAME=\"" + obj.getName() + "\"";
		rowSBStr += "		DESCRIPTION=\"" + obj.getDescription() + "\"";
		
		int visibleInstances = 0;
		List functionalities = obj.getFunctionalities();
		for (Iterator funcIt = functionalities.iterator(); funcIt.hasNext(); ) {
			Integer funcId = (Integer) funcIt.next();
			if (ObjectsAccessVerifier.canDev(obj.getStateCode(), funcId, profile)
					|| ObjectsAccessVerifier.canExec(obj.getStateCode(), funcId, profile)) {
				visibleInstances++;
			}
		}
		
		if (visibleInstances == 0) {
			// the document does not belong to any folder where the profile has the rigth permissions
			// (i.e.: the document is in REL state but belongs to folders where the profile cannot execute it
			// OR the document is in DEV state but belongs to folders where the profile cannot develope it)
			return null;
		}
		
		// at this point the document is in DEV or REL state and there is one or more visible instances
		rowSBStr += " 		/>";
		SourceBean rowSB = SourceBean.fromXMLString(rowSBStr);
		return rowSB;
	}
	
	private SourceBean makeAdminListRow(BIObject obj) throws Exception {
		
		if (initialPath != null && !initialPath.trim().equals("")) {
			boolean isVisible = false;
			List functionalitiesId = obj.getFunctionalities();
			Iterator it = functionalitiesId.iterator();
			while (it.hasNext()) {
				Integer id = (Integer) it.next();
				LowFunctionality folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(id, false);
				String folderPath = folder.getPath();
				if (folderPath.equalsIgnoreCase(initialPath) || folderPath.startsWith(initialPath + "/")) {
					isVisible = true;
					break;
				}
			}
			if (!isVisible) return null;
		}

		String rowSBStr = "<ROW ";
		rowSBStr += "		OBJ_ID=\"" + obj.getId() + "\"";
		rowSBStr += "		LABEL=\"" + obj.getLabel() + "\"";
		rowSBStr += "		NAME=\"" + obj.getName() + "\"";
		rowSBStr += "		DESCRIPTION=\"" + obj.getDescription() + "\"";
		rowSBStr += " 		/>";
		SourceBean rowSB = SourceBean.fromXMLString(rowSBStr);
		return rowSB;
	}
	
	/**
	 * Checks if is checked object.
	 * 
	 * @param objectID the object id
	 * 
	 * @return true, if is checked object
	 * 
	 * @throws Exception the exception
	 */
	public boolean isCheckedObject(String objectID) throws Exception{
		boolean isChecked = false;
		SourceBean checkedObjects = getCheckedObjects();
		List checkedObjectsList = checkedObjects.getAttributeAsList("OBJECT");
		Iterator i = checkedObjectsList.iterator();
		while (i.hasNext()){
			SourceBean source = (SourceBean)i.next();
			String objID = (String)source.getAttribute("OBJ_ID");
			if(objID.equals(objectID)){
				isChecked = true;
			}
		}
		
		return isChecked;
	}
}
