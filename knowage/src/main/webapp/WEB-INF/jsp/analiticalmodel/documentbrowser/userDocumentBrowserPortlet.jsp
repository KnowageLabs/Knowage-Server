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
    
    
    
    
    
