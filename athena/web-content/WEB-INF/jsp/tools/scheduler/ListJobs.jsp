<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<!--  uncomment this for Old GUI Spago based -->
<!-- %@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"% -->
<!-- spagobi:list moduleName="JobManagementModule" filter="disabled"/ -->

 
<%@page import="it.eng.spagobi.commons.bo.UserProfile" %>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>

<%
	Boolean superadmin =(Boolean)((UserProfile)userProfile).getIsSuperadmin();
	if(superadmin != null && superadmin){
		
	}
	
    String contextName = ChannelUtilities.getSpagoBIContextName(request);
	

%>

<script type="text/javascript">
	var conf = {
		isSuperadmin: false,
		contextName:''
	};
	<%if(superadmin != null && superadmin){%>
		conf.isSuperadmin=true;
	<%}%>
		conf.contextName = '<%= StringEscapeUtils.escapeJavaScript(contextName) %>' ;

    Ext.onReady(function(){
		var schedulerDetail = Ext.create('Sbi.tools.scheduler.SchedulerListDetailPanel',conf); //by alias
		var schedulerDetail = Ext.create('Ext.container.Viewport', {
			layout:'fit',
	     	items: [schedulerDetail]
	    });
    });
	

</script>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
