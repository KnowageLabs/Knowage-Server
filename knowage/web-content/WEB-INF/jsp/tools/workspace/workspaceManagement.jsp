<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS                                                           --%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetAction" %>
<%@page import="java.util.Map" %>
<%@page import="org.json.JSONObject"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%  
  SelfServiceDatasetAction ssa= new SelfServiceDatasetAction();
  Map<String,String> parameters= ssa.getParameters((UserProfile)userProfile,locale);
  JSONObject selfServiceParameters=new JSONObject(parameters);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="workspaceManager">

	<head>
	
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<%@include file="/WEB-INF/jsp/commons/workspace/workspaceImport.jsp"%>
		
		<!-- Styles -->
		<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
		
		<script language="javascript" type="text/javascript">		   
		   /* 
	   			Take the information if we are coming from the interface for a creation of a Cockpit document.
	   			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	   		*/
		   var whereAreWeComingFrom = <%=request.getParameter("comingFrom")%>;
		   var datasetParameters=<%=selfServiceParameters%>;  
		</script>
		
	</head>
	
	<body ng-controller="workspaceController" class="workspace kn-documentBrowser" id="workspaceWebPageBody"> 
		<% if (parameters.containsKey("error")==false) {%>	
		
		<md-content layout="column" flex layout-fill>
			
			<main-toolbar-workspace></main-toolbar-workspace>
			
			<md-content layout="row" flex>
			
				<!-- 
					Directive that will render the left main menu of the Workspace web page. 
					@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				-->
				<md-content flex=15 class="leftRightPanelHeight">
					<left-main-menu-workspace></left-main-menu-workspace>
				</md-content>
				
				<!-- 
					The right part of the Workspace main page (Search toolbar and document details pages).
					@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
				-->
				<md-content flex class="layout-column">
				
						<!-- 
							The progress circular animation will be shown until all the data for the Workspace is collected. 
							@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
						-->
						<md-progress-circular 	loading ng-show="showEl || searching" md-mode="indeterminate" md-diameter="75%" 
												style="position:fixed;top:50%;left:50%;z-index:500;background:rgba(255,255,255,0);">
						</md-progress-circular>
										
						<!-- 
							Pages for all items available in the left menu (table), e.g. Recent, Favorites, Documents, etc. 
							@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
						-->						
						<recent-view-workspace 			ng-show="currentOptionMainMenu.length==0 
															|| currentOptionMainMenu=='recent'">			</recent-view-workspace>	
						<favorites-view-workspace 		ng-show="currentOptionMainMenu=='favorites'">		</favorites-view-workspace>	
						<documents-view-workspace 		ng-show="currentOptionMainMenu=='documents'">		</documents-view-workspace>			
						<datasets-view-workspace 		ng-show="currentOptionMainMenu=='datasets'">		</datasets-view-workspace>
						<models-view-workspace			ng-show="currentOptionMainMenu=='models'">	    	</models-view-workspace>
						<smartfilters-view-workspace	ng-show="currentOptionMainMenu=='smartfilters'">	</smartfilters-view-workspace>
						<analysis-view-workspace 		ng-show="currentOptionMainMenu=='analysis'">		</analysis-view-workspace>	
						
				</md-content>
				
			</md-content>
			
		</md-content>
		<% }
		else {
			
		%>    
			<script  language="javascript" type="text/javascript">alert(datasetParameters.error);</script>
		<% } %>
		
	</body>
	
</html>