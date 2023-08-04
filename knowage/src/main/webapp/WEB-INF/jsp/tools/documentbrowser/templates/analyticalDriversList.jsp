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


<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>

<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

 <%
 
            String objId = request.getParameter(ObjectsTreeConstants.OBJECT_ID);
            String selectedObjParId = request.getParameter("selected_obj_par_id");
            String parurl_nm = request.getParameter("parurl_nm");
            String objParLabel = request.getParameter("objParLabel");
            String priority = request.getParameter("priority");
            String view_fl = request.getParameter("view_fl");
            String req_fl = request.getParameter("req_fl");
            String mult_fl = request.getParameter("mult_fl");
            String toDriversList = request.getParameter("toDriversList");
            
 			Map saveUrlPars = new HashMap();
 			saveUrlPars.put("PAGE", "detailBIObjectPage");
 			saveUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO,"0");
 			saveUrlPars.put("LIGHT_NAVIGATOR_DISABLED","TRUE");
 			
 			saveUrlPars.put("MESSAGEDET", "DETAIL_SELECT");
            saveUrlPars.put("OBJECT_ID", objId);
 			saveUrlPars.put("selected_obj_par_id", selectedObjParId); 
 			saveUrlPars.put("parurl_nm", parurl_nm);
            saveUrlPars.put("objParLabel", objParLabel);
            saveUrlPars.put("priority", priority);
            saveUrlPars.put("view_fl", view_fl);
            saveUrlPars.put("req_fl", req_fl);
            saveUrlPars.put("mult_fl", mult_fl);
            saveUrlPars.put("toDriversList", toDriversList);
 			
 			String saveUrl = urlBuilder.getUrl(request, saveUrlPars);
 
 			Map closeUrlPars = new HashMap();
 			closeUrlPars.put("PAGE", "detailBIObjectPage");
 			closeUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO,"0");
 			closeUrlPars.put("MESSAGEDET", "DETAIL_SELECT");
 			String closeUrl = urlBuilder.getUrl(request, closeUrlPars); 			
		%>
		
		<script>
			
			var saveUrl =  '<%= saveUrl %>';
			var closeUrl =  '<%= closeUrl %>';
		</script> 



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

	<head>
	
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Analytical Drivers List</title>
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentbrowser/analyticalDriversList.js")%>"></script>
		<link rel="stylesheet" type="text/css"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
	
		</head>

	<body class="bodyStyle" ng-app="analyticalDriversListModule" id="ng-app">
	
	<div ng-controller="analyticalDriversListCTRL" layout-fill>
		
		<md-card   layout-fill layout="column" class="flexCard" style="position:absolute">
 <md-card-content flex layout="column" class="noPadding">
  <md-toolbar >
      <div class="md-toolbar-tools">
      {{translate.load("sbi.analytical.drivers.title");}}
      </div>
  </md-toolbar>
  <md-content flex layout="column" >

    <angular-table flex
			id="adList_id" 
			ng-model="adList"
			columns='[
					  {"label":"Label","name":"label"},
					  {"label":"Name","name":"name"},
					  {"label":"Type","name":"type"}
					]'
			columns-search='["label","name","type"]'
			show-search-bar=true
			highlights-selected-item=true
			click-function="selectAD(item)">
		     </angular-table>
					
  </md-content>
</md-card-content>
<md-card-actions layout="row" layout-align="end center">
<md-button ng-click="close()">{{translate.load("sbi.generic.cancel");}}</md-button>
<md-button ng-click="goBackandSave()">{{translate.load("sbi.generic.save");}}</md-button>
</md-card-actions>
</md-card>
		
		
		
		
	</div>
	</body>

</html>
