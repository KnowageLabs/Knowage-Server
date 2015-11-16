/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.util.StringUtils;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.events.EventsManager;
import it.eng.spagobi.events.bo.EventLog;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * This class shows events' notification log 
 * 
 * @author Gioia
 *
 */			 
public class ListEventsLogModule extends AbstractBasicListModule {
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		RequestContainer requestContainer = this.getRequestContainer();	
		ResponseContainer responseContainer = this.getResponseContainer();	
		SessionContainer session = requestContainer.getSessionContainer();
		SessionContainer permanentSession = session.getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		 
		//Start writing log in the DB
		Session aSession =null;
		try {
			aSession = HibernateSessionManager.getCurrentSession();
			AuditLogUtilities.updateAudit(((HttpServletRequest)requestContainer.getRequestContainer().getInternalRequest()),  profile, "EVENT_LIST.OPEN", null, "OK");
		} catch (HibernateException he) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		//End writing log in the DB
		
		EventsManager eventsManager = EventsManager.getInstance();		
		List firedEventsList = eventsManager.getRegisteredEvents(profile);
        SingletonConfig config = SingletonConfig.getInstance();
        String formatSB = config.getConfigValue("SPAGOBI.DATE-FORMAT.format");
        String format = (formatSB==null)?"": formatSB;
	    format = format.replaceAll("D", "d");
	    format = format.replaceAll("m", "M");
	    format = format.replaceAll("Y", "y");
		PaginatorIFace paginator = new GenericPaginator();
		Iterator it = firedEventsList.iterator();
		while (it.hasNext()) {
			EventLog eventLog = (EventLog) it.next();
			String rowSBStr = "<ROW ";
			rowSBStr += "		ID=\"" + eventLog.getId() + "\"";
			String date = StringUtils.dateToString(eventLog.getDate(), format);
			rowSBStr += "		DATE=\"" + date + "\"";
			rowSBStr += "		USER=\"" + eventLog.getUser() + "\"";
			String description = eventLog.getDesc();
			if (description != null) {
				description = GeneralUtilities.replaceInternationalizedMessages(description);
				description = description.replaceAll("<br/>", " ");
				if (description.length() > 50) description = description.substring(0, 50) + "...";
				description = description.replaceAll(">", "&gt;");
				description = description.replaceAll("<", "&lt;");
				description = description.replaceAll("\"", "&quot;");
			}
			rowSBStr += "		DESCRIPTION=\"" + (description != null ? description : "") + "\"";
			rowSBStr += " 		/>";
			SourceBean rowSB = SourceBean.fromXMLString(rowSBStr);
			paginator.addRow(rowSB);
		}
		ListIFace list = new GenericList();
		list.setPaginator(paginator);
		// filter the list 
		String valuefilter = (String) request.getAttribute(SpagoBIConstants.VALUE_FILTER);
		if (valuefilter != null) {
			String columnfilter = (String) request
					.getAttribute(SpagoBIConstants.COLUMN_FILTER);
			String typeFilter = (String) request
					.getAttribute(SpagoBIConstants.TYPE_FILTER);
			String typeValueFilter = (String) request
					.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
			list = DelegatedBasicListService.filterList(list, valuefilter, typeValueFilter, 
					columnfilter, typeFilter, getResponseContainer().getErrorHandler());
		}
		
		return list;
	}
}
