<%-- 
   Knowage, Open Source Business Intelligence suite
   Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
   
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

<%-- 
author: Andrea Gioia (andrea.gioia@eng.it)
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>



<%@page import="it.eng.spago.configuration.*"%>
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.engines.commonj.*"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	

	String spagobiContext;
	String spagobiSpagoController;
	String docId;
	
	
	CommonjEngineConfig qcommonJEngineConfig = CommonjEngineConfig.getInstance();

    spagobiContext = KnowageSystemConfiguration.getKnowageContext();
    spagobiSpagoController = "/servlet/AdapterHTTP";
    docId = request.getParameter("document");
    
	Map parsMap=request.getParameterMap();
	Map<String,String> paramsMapToSend = new HashMap<String, String>();
	String parametersString="";
	for (Iterator iterator = parsMap.keySet().iterator(); iterator.hasNext();) {
		String url= (String) iterator.next();
		if(url.equals("ACTION_NAME")) continue;
		Object val=parsMap.get(url);
		// take only String or numbers
		if(val!=null && val instanceof String[]){
			String[] strs=(String[])val;
			String toAdd="";			
			if(strs.length==1){
				paramsMapToSend.put(url, strs[0].toString());
				parametersString+="&"+url+"="+strs[0];	
			}
			else{
			parametersString+="&"+url+"=[";
			toAdd+="&"+url+"=[";
			for(int i=0; i< strs.length;i++){
				String valS=strs[i];
				if(i==0){
					parametersString+=valS;
					toAdd+=valS;
				}
			else{
				parametersString+=","+valS;				
				toAdd+=","+valS;
			}
			}
				parametersString+="]";
				toAdd+="]";
				paramsMapToSend.put(url, toAdd.toString());
			}
		}
		else
		if(val!=null && (val instanceof String || val instanceof Integer)) {
			parametersString+="&"+url+"="+val.toString();
			paramsMapToSend.put(url, val.toString());
		}
	}
	if(parametersString.indexOf('&')==0){
		parametersString=parametersString.substring(1,parametersString.length());
	}

	// Check if I am in scheduler mode!	in that case call directly the start Engine
	Object userIdO = parsMap.get("user_id");
	String userIdNow = null;
	if(userIdO != null){
	 if(userIdO instanceof String[]){
		 String[] userIdArray = (String[])userIdO;
		 userIdNow = userIdArray[0];
		 }
	 else if(userIdO instanceof String){
		userIdNow = userIdO.toString();	 
	 }
	
	 if(userIdNow.startsWith("scheduler")){
			// call directly startWorkEngineService
			// TODO: separate logic code from action so it can be use as a class		
			//auditProxy = new AuditServiceProxy(getAuditId(), getUserIdentifier(), getHttpSession());
		   //EventServiceProxy eventProxy = new EventServiceProxy(userIdNow, ses);

		   // I have to convert the parmeters MAP to a Map of strings
		   
			StartWorkAction	startWorkAction = new StartWorkAction();
	     	 startWorkAction.serviceStart(userIdNow, docId, paramsMapToSend, session, request, false);
	 }
	
	}
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>

<%@page import="it.eng.spagobi.engines.commonj.services.StartWorkAction"%>
<%@page import="it.eng.spagobi.utilities.engines.AuditServiceProxy"%>
<%@page import="it.eng.spagobi.services.proxy.EventServiceProxy"%>
<%@page import="java.util.HashMap"%>

<html>

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jsp"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css">
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.x/job/jobManagement.js">
	</script>

</head>

<body ng-cloack ng-app="jobManagementModule">
    
	<div ng-controller="JobController as jobCtrl" layout="row"
		ng-init="jobCtrl.initValues('<%=docId%>', '<%=parametersString%>');">


     <div flex="20" layout-align="center" layout-fill>  
        <md-button ng-click="jobCtrl.startJob()" class="md-raised"
            ng-show="jobCtrl.currentStatus==='notstarted'"> 
                {{translate.load("sbi.commonj.start")}}
        </md-button>
        <md-button ng-click="jobCtrl.stopJob()" class="md-raised"
            ng-show="jobCtrl.currentStatus==='started'"> 
                        {{translate.load("sbi.commonj.stop")}}
            </md-button>
        <md-button ng-click="" class="md-no-focus" ng-disabled="true"
            ng-show="jobCtrl.currentStatus==='completed' || jobCtrl.currentStatus==='rejected'"> 
            </md-button>            
      </div>


     <div>
		<angular-table flex id='jobTableId' ng-model='jobCtrl.jobList'
			columns='jobColumnNames'
			> </angular-table>
     </div>
      
	</div>



</body>

</html>