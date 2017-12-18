<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@page import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetStartAction"%>
<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/main.css", currTheme)%>'/>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/listview.css", currTheme)%>'/>

<% String isMyData =((String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_MYDATA)!=null)?(String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_MYDATA):"FALSE";
if (isMyData.equalsIgnoreCase("FALSE")) {%>
	<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/catalogue-item-small.css",currTheme)%>'/>
<%}else{%>
	<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/catalogue-item-big.css",currTheme)%>'/>	
<%} %>
<!--  <link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/home40/standard.css",currTheme)%>'/>-->
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/browser/standard.css",currTheme)%>'/>

<%
    String urlToCallBase = (String) aResponseContainer.getServiceResponse().getAttribute("urlToCallBase");
	String urlToCallParams = (String) aResponseContainer.getServiceResponse().getAttribute("urlToCallParams");
%>

<script type="text/javascript">



    Ext.onReady(function(){
		var selfService = Ext.create('Sbi.selfservice.SelfServiceExecutionIFrame',{hideExtraSaveButton : true});
		var datasetListViewport = Ext.create('Ext.container.Viewport', {
			layout:'fit',
	     	items: [selfService]	     	
	    });
		var urlToCallBase = '<%=urlToCallBase%>';
		Sbi.debug(urlToCallBase);
		var urlToCallParams = '<%=urlToCallParams%>';
		Sbi.debug(urlToCallParams);
		
		urlToCallParams = Ext.JSON.decode(urlToCallParams);
		urlToCallParams = Ext.Object.toQueryString(urlToCallParams);
		
		
		var urlToCall = urlToCallBase + "?" + urlToCallParams;
		//alert(urlToCall);
		selfService.load(urlToCall);
    });
	
</script>
 

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
