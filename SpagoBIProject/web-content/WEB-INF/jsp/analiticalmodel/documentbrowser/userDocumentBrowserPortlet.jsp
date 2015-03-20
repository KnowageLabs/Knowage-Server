<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page language="java" 
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
   
<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf" %>
   <%@ include file="/WEB-INF/jsp/commons/importSbiJS.jspf"%>
   	   	
   
       
    <script type="text/javascript">
    Ext.BLANK_IMAGE_URL = '<%=urlBuilder.getResourceLink(request, "/js/lib/ext-2.0.1/resources/images/default/s.gif")%>';
    
    Sbi.config = {};
    
    var url = {
		host: '<%= request.getServerName()%>'
	    , port: '<%= request.getServerPort()%>'
	    , contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
	        				  request.getContextPath().substring(1):
	        				  request.getContextPath()%>'
	    
    };

	var params = {
		user_id:  <%= userUniqueIdentifier!=null?"'" + userUniqueIdentifier +"'": "null" %>
		, SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
		, LIGHT_NAVIGATOR_DISABLED: 'TRUE'
	};

    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
        baseUrl: url
        , baseParams: params
    });



	
    var browserConfig = <%= aServiceResponse.getAttribute("metaConfiguration")%>;
	//var lang = '<%=locale.getLanguage()%>';
    //alert('language: ' + lang);
    	       
   
    browserConfig = Ext.apply(browserConfig, {
        //rootFolderId: 3
        /*
        metaDocument: [
   			  {id:"label", 				groupable:true, maxChars:20, visible:true, showLabel:false, sortable:false, searchable:true}
   			, {id:"name", 				groupable:true, maxChars:20, visible:true, showLabel:false, sortable:true, searchable:true}
   			, {id:"extendedDescription",groupable:false, maxChars:20, visible:true, showLabel:true, sortable:false}
   			, {id:"description", 		groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"engine", 			groupable:false, maxChars:5, visible:false, showLabel:true, sortable:true, searchable:true}
   			, {id:"creationUser", 		groupable:true, maxChars:20, visible:true, showLabel:true, sortable:false}
   			, {id:"creationDate",		groupable:true, maxChars:20, visible:true, showLabel:false, sortable:true, searchable:true}
   			, {id:"typeCode", 			groupable:false, maxChars:20, visible:true, showLabel:true, sortable:false}
   			, {id:"encrypt", 			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"profiledVisibility", groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"datasource", 		groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"dataset", 			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"uuid",				groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"relname", 			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"stateCode",			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"stateId", 			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"functionalities", 	groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"language", 			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"objectve", 			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"keywords", 			groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   			, {id:"refreshSeconds",		groupable:false, maxChars:20, visible:false, showLabel:true, sortable:false}
   		]
   		, metaFolder: [
   		     {maxChars:20, visible:true, showLabel:false, id:"id"}
   		   , {maxChars:20, visible:true, showLabel:false, id:"name"}
   		   , {maxChars:20, visible:true, showLabel:false, id:"description"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"code"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"codType"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"path"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"parentId"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"devRoles"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"testRoles"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"execRoles"}
   		   , {maxChars:20, visible:false, showLabel:false, id:"biObjects"}
   		]
   		*/
   	});
    
    Ext.onReady(function(){
      Ext.QuickTips.init();              
      var tabbedBrowser = new Sbi.browser.DocBrowserContainer(browserConfig);
      var viewport = new Ext.Viewport(tabbedBrowser);     
    });
    
    </script>
    
    
    
    
    
