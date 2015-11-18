
<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="mondrianSchemasCatalogueModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">

<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/mondrianSchemasCatalogue.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body class="bodyStyle"
	ng-controller="mondrianSchemasCatalogueController as ctrl">

	<angular_2_col> 
	
		<left-col>
		 
			<div class = "leftBox">
			
				<md-toolbar class="md-blue minihead">
					<div class="md-toolbar-tools">
						<div>Mondrian Schemas Catalogue NEW</div>
						<md-button 
							class="md-fab md-ExtraMini addButton"
							style="position:absolute; right:11px; top:0px;"
							ng-click="createDragan()"> 
							<md-icon
								md-font-icon="fa fa-plus" 
								style=" margin-top: 6px ; color: white;">
							</md-icon> 
						</md-button>
					</div>
				</md-toolbar>
			
			<md-content layout-padding style="background-color: rgb(236, 236, 236);" class="ToolbarBox miniToolbar noBorder leftListbox">
					<angular-table 
						layout-fill
						id="Dragan"
						ng-model="Dragan"
						columns='["NAME","DESCRIPTION"]'
						columns-search='["NAME","DESCRIPTION"]'
						show-search-bar=true
						highlights-selected-item=true
											
					>						
					</angular-table>
				</md-content>
			
			</div>
	
		</left-col> 
	
		<right-col>

		</right-col> 
	
	</angular_2_col>

</body>
</html>