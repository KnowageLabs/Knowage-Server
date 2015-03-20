<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<% 
    AuditManager auditManager = AuditManager.getInstance();
	//START UPDATE AUDIT
	try{
		if (executionAuditId_chart != null) {
		    auditManager.updateAudit(executionAuditId_chart, null,new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null,null);
		}
	}catch (Exception e) {
	    if(executionAuditId_chart!=null){
		    auditManager.updateAudit(executionAuditId_chart, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e.getMessage(), null);		
	    }
	return;    
	}
	//END UPDATE AUDIT
%>