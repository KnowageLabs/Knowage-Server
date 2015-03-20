<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>

<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="org.json.JSONArray"%>
<%@page import="it.eng.spagobi.commons.serializer.SerializerFactory"%>
<%@page import="it.eng.spagobi.commons.bo.Domain"%>

<%
	List <Domain> domains = DAOFactory.getDomainDAO().loadListDomainsByType("PAR_TYPE");
	Iterator <Domain> it = domains.iterator();
	while(it.hasNext()){
		Domain domain = it.next();
		String i18nName = domain.getTranslatedValueName(locale);
		String i18nDS = domain.getTranslatedValueDescription(locale);
		domain.setValueName(i18nName);
		domain.setValueDescription(i18nDS);
	}
    JSONArray domainsJson = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(domains, locale); 
%>

<script type="text/javascript">

	var domains = <%=domainsJson.toString()%>;
	
	var url = {
    	host: '<%= request.getServerName()%>'
    	, port: '<%= request.getServerPort()%>'
    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
    	   				  request.getContextPath().substring(1):
    	   				  request.getContextPath()%>'
    	    
    };

    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
    	baseUrl: url
    });

	Ext.onReady(function(){
		Ext.QuickTips.init();
		var manageConfig = new Sbi.config.ManageConfig({});
		var viewport = new Ext.Viewport({
			layout: 'border'
			, items: [
			    {
			       region: 'center',
			       layout: 'fit',
			       items: [manageConfig]
			    }
			]
	
		});
	   	
	});


</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>