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


	<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>


	<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
	
	<% if ( "WEB".equalsIgnoreCase(sbiMode) ) { %>
		
		<%@ include file="/WEB-INF/jsp/commons/includeMessageResource.jspf" %>
	   	<%@ include file="/WEB-INF/jsp/commons/importSbiJS.jspf"%>
	   	 
	    <%
	    String georeportEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute("georeportServiceUrl");
	    %>
	    
	    
	    <script type="text/javascript">
		    Ext.BLANK_IMAGE_URL = '<%=urlBuilder.getResourceLink(request, "/js/lib/ext-2.0.1/resources/images/default/s.gif")%>';
		    
		    if (Sbi.config == undefined) Sbi.config = {};
		
		    // the user language
		    Sbi.config.language = '<%= locale.getLanguage() %>';
			// the user country
		    Sbi.config.country = '<%= locale.getCountry() %>';
		    // the date format localized according to user language and country
		    Sbi.config.localizedDateFormat = '<%= GeneralUtilities.getLocaleDateFormatForExtJs(permanentSession) %>';
		    
		    // the date format to be used when communicating with server
		    Sbi.config.clientServerDateFormat = '<%= GeneralUtilities.getServerDateFormatExtJs() %>';
		    // the timestamp format to be used when communicating with server
		    Sbi.config.clientServerTimestampFormat = '<%= GeneralUtilities.getServerTimestampFormatExtJs() %>';
		    
		    Sbi.config.isTechnicalUser = <%= isTechnicalUser %>;
		    
		    var url = {
		    	host: '<%= request.getServerName()%>'
		    	, port: '<%= request.getServerPort()%>'
		    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
		    	   				  request.getContextPath().substring(1):
		    	   				  request.getContextPath()%>'
		    	    
		    };
		
		    var params = {
		    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
    	, SBI_ENVIRONMENT: 'DOCBROWSER'
		    	, LIGHT_NAVIGATOR_DISABLED: 'TRUE'
		    };
		   
		    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
		    	baseUrl: url
		        , baseParams: params
		    });
		
		    var browserConfig = <%= aServiceResponse.getAttribute("metaConfiguration")%>;    
		    var engineUrls =  <%= aServiceResponse.getAttribute("engineUrls")%>;
		    
		    var config = {};
		    config.browserConfig = browserConfig;
		    config.engineUrls = engineUrls;
		    
		    Ext.onReady(function(){
		      Ext.QuickTips.init();              
		      var tabbedBrowser = new Sbi.browser.DocBrowserContainer(config);      
		      var viewport = new Ext.Viewport(tabbedBrowser);
			  // utility class for invoking export from an external application
			  Sbi.execution.ExporterUtils.setDocumentsBrowser( tabbedBrowser );
		    });
	    
	    </script>
	    
 <% } else { 
	 
	 String labelSubTreeNode = null;
	 
	 labelSubTreeNode = ChannelUtilities.getPreferenceValue(aRequestContainer, "PATH_SUBTREE", "");
	 
	 String url =GeneralUtilities.getSpagoBIProfileBaseUrl(userUniqueIdentifier)+  "&ACTION_NAME=DOCUMENT_USER_BROWSER_START_PORTLET_ACTION";
	 url += "&SBI_EXECUTION_ID=" + request.getParameter("SBI_EXECUTION_ID");	 
	 if (labelSubTreeNode != null && !labelSubTreeNode.trim().equals("")) url += "&LABEL_SUBTREE_NODE=" + labelSubTreeNode;
	 url += "&LANGUAGE=" + locale.getLanguage();
	 url += "&COUNTRY=" + locale.getCountry();
 %>

 	<iframe 
 		id='browserIframe'
 		name='browserIframe'
 		src='<%= url %>'
 		frameBorder = 0
 		width=100%
 		height=<%= ChannelUtilities.getPreferenceValue(aRequestContainer, "HEIGHT", "600") %>
 	/>
 <% } %>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
