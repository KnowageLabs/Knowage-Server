/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.commonj;

/**
 * 
 * LICENSE: see 'LICENSE.sbi.drivers.talend.txt' file
 * 
 */


import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.SubreportDAOHibImpl;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.events.EventsManager;
import it.eng.spagobi.events.bo.EventLog;
import it.eng.spagobi.events.handlers.IEventPresentationHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonjEventPresentationHandler implements IEventPresentationHandler {

	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.handlers.IEventPresentationHandler#loadEventInfo(it.eng.spagobi.events.bo.EventLog, it.eng.spago.base.SourceBean)
	 */
	public void loadEventInfo(EventLog event, SourceBean response) throws SourceBeanException, EMFUserError {
		response.setAttribute("firedEvent", event);
		Map eventParams = EventsManager.parseParamsStr(event.getParams());
		String startEventId = (String) eventParams.get("startEventId");
		if (startEventId != null) {
			// it's an end process event
			response.setAttribute("startEventId", startEventId);
			String result = (String) eventParams.get("operation-result");
			response.setAttribute("operation-result", result);
		} else {
			// it's an end process event, nothing more to do
		}
		IBIObjectDAO biObjectDAO = DAOFactory.getBIObjectDAO();
		String biobjectIdStr = (String) eventParams.get("biobjectId");
		Integer biObjectId = new Integer(biobjectIdStr);
		BIObject biObject = biObjectDAO.loadBIObjectById(biObjectId);

		response.setAttribute("biobject", biObject);
		SubreportDAOHibImpl subreportDAOHibImpl = new SubreportDAOHibImpl();
		List list = subreportDAOHibImpl.loadSubreportsByMasterRptId(biObject.getId());
		List biObjectList = new ArrayList();
		for(int i = 0; i < list.size(); i++) {
			Subreport subreport = (Subreport)list.get(i);
			BIObject biobj = biObjectDAO.loadBIObjectForDetail(subreport.getSub_rpt_id());
			biObjectList.add(biobj);
		}
		response.setAttribute("linkedBIObjects", biObjectList);
		response.setAttribute("PUBLISHER_NAME", "CommonjExecutionEventLogDetailPublisher");
	}
	
}
