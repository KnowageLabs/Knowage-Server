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

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS                                                           --%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetAction" %>
<%@page import="it.eng.spagobi.commons.SingletonConfig" %>
<%@page import="java.util.Map" %>
<%@page import="org.json.JSONObject"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%  
  SelfServiceDatasetAction ssa= new SelfServiceDatasetAction();
  Map<String,String> parameters= ssa.getParameters((UserProfile)userProfile,locale);
  JSONObject selfServiceParameters=new JSONObject(parameters);
  boolean isAdmin = UserUtilities.isAdministrator(userProfile);
  boolean isTechnicalUser =  UserUtilities.isTechnicalUser(userProfile);
  boolean isDeveloper =  UserUtilities.hasDeveloperRole(userProfile);
  SingletonConfig serverConfig1 = SingletonConfig.getInstance();
  
  String initialOptionMainMenu = request.getParameter("currentOptionMainMenu");
  String maxSizeStr = serverConfig1.getConfigValue("SPAGOBI.DATASET_FILE_MAX_SIZE");
   
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="workspaceManager">

	<head>
	
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<%@include file="/WEB-INF/jsp/commons/workspace/workspaceImport.jsp"%>
		
		<!-- Styles -->
		<link rel="stylesheet" type="text/css"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>"> 
		
		
		<!--Drivers  execution-->
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/driversexecution/driversExecutionService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementController.js")%>"></script>                
    <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/parameterViewPointHandler/parameterViewPointHandlerController.js")%>"></script>          
  	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/driversexecution/renderparameters/renderParameters.js")%>"></script>
 
	
		<script language="javascript" type="text/javascript">		   
		   /* 
	   			Take the information if we are coming from the interface for a creation of a Cockpit document.
	   			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   		*/
		   var whereAreWeComingFrom = <%=request.getParameter("comingFrom")%>;
		   var datasetParameters=<%=selfServiceParameters%>;
		   var isAdmin =<%=isAdmin%>;
		   var isTechnicalUser = <%=isTechnicalUser%>;
		   var isDeveloper = <%=isDeveloper%>;
		   var maxSizeStr = <%=maxSizeStr%>; 
		   var initialOptionMainMenu = '<%=initialOptionMainMenu != null ? initialOptionMainMenu : ""%>';

		   </script>
		
	</head>
	
	<body ng-controller="workspaceController" class="kn-workspace" id="workspaceWebPageBody"> 
	 <%if(includeInfusion){ %> 
            <%@include file="/WEB-INF/jsp/commons/infusion/infusionTemplate.html"%>
      
     <%} %>	

		
			<!-- 
				The progress circular animation will be shown until all the data for the Workspace is collected. 
				@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
			-->		
			<div loading ng-show="showEl || processing" class="loadingMask">
			 	<md-progress-circular md-mode="indeterminate" md-diameter="75%" class="progressCircularWorkspace"></md-progress-circular>		 
			</div>
			
			
			
			<div layout="column" flex layout-fill>
				
				<main-toolbar-workspace></main-toolbar-workspace>
				
				<md-content layout="row" flex>
				
					<!-- 
						Directive that will render the left main menu of the Workspace web page. 
						@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					-->
	
					<left-main-menu-workspace></left-main-menu-workspace>
					
					<!-- 
						The right part of the Workspace main page (Search toolbar and document details pages).
						@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
					-->
					<md-content flex class="layout-column">
					
							<!-- 
								The progress circular animation will be shown until all the data for the Workspace is collected. 
								@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
							-->		
							<div ng-show="searching || clearSearch" class="searchMask"> <!-- || clearSearch -->
							 	<md-progress-circular md-mode="indeterminate" md-diameter="75%" class="progressCircularWorkspaceSearch">
							 	</md-progress-circular>		 
							</div>
											
							<!-- 
								Pages for all items available in the left menu (table), e.g. Recent, Favorites, Documents, etc. 
								@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
							-->			
										
							<recent-view-workspace 			ng-show="currentOptionMainMenu.length==0 
																|| currentOptionMainMenu=='recent'">			</recent-view-workspace>
			
							<datasets-view-workspace 		ng-show="currentOptionMainMenu=='datasets'">		</datasets-view-workspace>
							<models-view-workspace			ng-show="currentOptionMainMenu=='models'">	    	</models-view-workspace>
							<analysis-view-workspace 		ng-show="currentOptionMainMenu=='analysis'">		</analysis-view-workspace>
							<schedulation-view-workspace 	ng-show="currentOptionMainMenu=='schedulation'">		</schedulation-view-workspace>	
							<smartfilters-view-workspace	ng-show="currentOptionMainMenu=='smartfilters'">	</smartfilters-view-workspace>							
							<documents-view-workspace 		ng-show="currentOptionMainMenu=='documents'">		</documents-view-workspace>	

					</md-content>
					
				</md-content>
				
			</div>
			

		
	</body>
	
</html>