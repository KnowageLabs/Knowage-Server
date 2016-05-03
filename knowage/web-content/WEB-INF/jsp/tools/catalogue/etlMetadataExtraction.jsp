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


<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page
	import="it.eng.spagobi.tools.dataset.federation.FederationDefinition"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="etlMetadata">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/catalogues/etlMetadataExtraction.js"></script>


<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ETL Metadata Extraction</title>
</head>
<body class="kn-etlmetadata" ng-controller="etlMetadataExtractionController as ctrl">
    <form id="etlUploadForm">
        <div layout="column" layout-wrap>
            <md-toolbar class="header">
                <div class="md-toolbar-tools">
                    <i class="fa fa-database fa-2x"></i>
                    <h2 class="md-flex">{{translate.load("sbi.metadata.etl.title")}}</h2>
                </div>
            </md-toolbar>
            <md-content class="mainContainer" layout="column" layout-wrap>
                <md-card>
                    <md-toolbar class="cardHeader">
                        <div class="md-toolbar-tools">
                            <h2 class="md-flex">{{translate.load("sbi.metadata.etl.upload.title")}}</h2>
                        </div>
                    </md-toolbar>
                    <p>
                    {{translate.load("sbi.metadata.etl.description")}}                
                    </p>
                    <md-content layout="row" >
                        
                                <md-input-container flex="30">
                                    <label>{{translate.load("sbi.metadata.etl.context.name")}}</label>
                                    <input ng-model="contextName" required ng-maxlength="100">
                                </md-input-container>
                                
                                    <p flex="10" layout-align="center center">{{translate.load("sbi.ds.file.upload.button")}}:</p>
                                
                                <file-upload flex="40" ng-model="fileObj" id="etlFile" ng-click="fileChange();checkChange()"></file-upload>
                                	<div flex="20">
                                   		 <md-button  ng-click="importMetadata()" ng-show="!bmImportingShow" class="md-raised">{{translate.load("sbi.catalogues.generic.import")}}</md-button>
                                	</div>
                                    <div layout="row" flex="10" layout-sm="column" layout-align="space-around">
                                       <md-progress-circular  ng-show="bmImportingShow" class=" md-hue-4" md-mode="indeterminate" md-diameter="70"> </md-progress-circular>
                                    
                                    </div>
                                
                      
                    </md-content>
        </div>
        </md-card>
        </md-content>
        </div>
    </form>
</body>

</html>

