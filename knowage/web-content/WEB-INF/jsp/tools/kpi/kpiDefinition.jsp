<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="kpiDefinitionManager">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>KPI definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/kpiDefinitionController.js"></script>

<style>
.cardinalityTable .measureCell{
text-align: center;
height: 30px;
 
}


.cardinalityTable .invalidCell{
 color: lightgray;
line-height: 30px;
}


.cardinalityTable .selectedCell{
color: green;
 line-height: 30px;
}
.cardinalityTable .selectableCell{
color: lightgray;
 line-height: 30px;
}

.cardinalityTable{
width:100% ;
 table-layout: fixed;
 border-collapse: collapse;
}

.cardinalityTable .attributeRow{
 border-bottom: 1px solid #ECEFF1; 
}

.disabledCell{
background-color: gray;}

.absolute{
position:absolute
}

.headerColumnMeasure{
text-align: center;
font-size: 14px;
}
</style>
</head>
<body>
	<angular-list-detail ng-controller="kpiDefinitionMasterController">
		<list label="translate.load('sbi.kpi.list')">lista</list>
		<detail>
		
		<md-tabs layout-fill class="absolute">
			
				<md-tab id="tab1">
       				<md-tab-label>{{translate.load("sbi.ds.query")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./kpiTemplate/cardinalityTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				
				<md-tab id="tab2">
       				<md-tab-label>{{translate.load("sbi.execution.executionpage.toolbar.metadata")}}</md-tab-label>
        			<md-tab-body>
        			metadati
					</md-tab-body>
				</md-tab>
				
				<md-tab id="tab3">
       				<md-tab-label>Cardinality</md-tab-label>
        			<md-tab-body>
        			
					</md-tab-body>
				</md-tab>
				
			</md-tabs>
		
	


		</detail>
	</angular-list-detail>
</body>
</html>