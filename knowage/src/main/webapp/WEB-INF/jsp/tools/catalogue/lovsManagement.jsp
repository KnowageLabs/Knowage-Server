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


<!-- 
	The main JSP page for the management of the LOV catalog.
	
	Author: Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
	Author: Stefan Petrovic (spetrovic, Stefan.Petrovic@mht.net)
-->

<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html ng-app="lovsManagementModule">
	
	<head>
	
		<!-- HTML meta data -->
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		
		<!-- JSP files included -->
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		
		<link rel="stylesheet" type="text/css"    href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
		
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/lovsManagement.js")%>"></script>		
		
		<!-- Codemirror  -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css")%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css")%>">  
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js")%>"></script>  
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/ui-codemirror.js")%>"></script> 
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css")%>" />
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/display/autorefresh.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/mode/javascript/javascript.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/mode/groovy/groovy.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/mode/sql/sql.js")%>"></script>

		
		<title>LOVS Management</title>
		
	</head>
	
	<body class="bodyStyle kn-layerCatalogue kn-lovCatalog" ng-controller="lovsManagementController as LOVSctrl" >
	
	<rest-loading></rest-loading>
	<angular-list-detail show-detail="showMe">
 		<list label='translate.load("sbi.behavioural.lov.title")' new-function="createLov"> 

			 	
				
				 <angular-table
			flex
			id="listOfLovs_id" 
			ng-model="listOfLovs"
			columns='[
					  {"label":"Label","name":"label"},
					  {"label":"Description","name":"description"},
					  {"label":"Type","name":"itypeCd"}
					]'
			show-search-bar=true
			highlights-selected-item=true
			speed-menu-option="lovsManagementSpeedMenu"
			click-function="itemOnClick(item)">
		     </angular-table>
		

		</list>
		
		<extra-button>
			  <md-button class="md-flat" ng-click="openPreviewDialog()" ng-show="showMe" >{{translate.load("sbi.ds.test")}}</md-button>
		</extra-button>
		
		<extra-button>
			  <md-button class="md-flat" ng-click="testLov()" ng-show="showMe" ng-disabled="!enableTest" >{{translate.load("sbi.datasource.testing")}}</md-button>
		</extra-button>
		
		<extra-button>
			  <md-button class="md-flat" ng-click="updateLovWithoutProvider()" ng-show="selectedLov.id != undefined" >{{translate.load("sbi.generic.save")}}</md-button>
		</extra-button>
		
		<detail label=' selectedLov.label==undefined? "" : selectedLov.label'  save-function="saveLov"
		cancel-function="cancel"
		disable-save-button="!attributeForm.$valid"
		show-save-button="false" show-cancel-button="showMe">
		<form name="attributeForm" ng-submit="attributeForm.$valid && testLov()">
	
			           <md-card layout-padding  ng-show="showMe">
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="md-block">
							<label>{{translate.load("sbi.ds.label")}}</label>
							<input name="lovLbl" ng-model="selectedLov.label" ng-required="true"
							maxlength="20" ng-change="setDirty()">
							
							<div  ng-messages="attributeForm.lbl.$error" ng-show="selectedLov.label== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
							
							 </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="md-block">
							<label>{{translate.load("sbi.ds.name")}}</label>
							<input name="lovName" ng-model="selectedLov.name"  ng-required = "true"
						    maxlength="40" ng-change="setDirty()">
						    
						    <div  ng-messages="attributeForm.name.$error" ng-show="selectedLov.name== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
						    
						    
						     </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="md-block">
								<label>{{translate.load("sbi.ds.description")}}</label>
								<input ng-model="selectedLov.description"  
						        maxlength="160" ng-change="setDirty()"> 
				             </md-input-container>
						</div>
					</div>
				
				<div layout="row" layout-wrap>
      				<div flex=100>
				       <md-input-container class="md-block" > 
				       <label>{{translate.load("sbi.analytical.drivers.usemode.lovtype")}}</label>
				       <md-select  aria-label="dropdown" placeholder ="{{translate.load('sbi.modalities.check.details.check_type')}}"
				       	name ="typeLovDropdown" 
				        ng-required = "true"
				        ng-model="selectedLov.itypeCd"
				        ng-change="changeLovType(selectedLov.itypeCd)"
				        > <md-option 
				        ng-repeat="l in listOfInputTypes track by $index" value="{{l.VALUE_CD}}">{{l.VALUE_NM}} </md-option>
				       </md-select>
				       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedLov.itypeCd == null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>   
				        </md-input-container>
				   </div>
			</div>
	
	</md-card>
		      
	<md-card>
					<md-toolbar class="secondaryToolbar">
				      <div class="md-toolbar-tools">
				        <h2>
				          <span>{{toolbarTitle}}</span>
				        </h2>
				   		<span flex></span>
				   		
				        <md-button class="md-icon-button" aria-label="Info" ng-click="openInfoFromLOV()">
				          <md-icon md-font-icon="fa fa-info-circle" class="fa"></md-icon>
				        </md-button>
				        <md-button class="md-icon-button" aria-label="Profiles" ng-click="openAttributesFromLOV()">
				          <md-icon md-font-icon="fa fa-users" class="fa"></md-icon>
				        </md-button>
				   		
				      </div>
				    </md-toolbar>
				    <md-card-content>
			<div ng-show="selectedLov.itypeCd == lovItemEnum.SCRIPT">    
				    <div layout="row" layout-wrap>
      				<div flex=100>
				       <md-input-container class="md-block" > 
				       <label>{{translate.load("sbi.functionscatalog.language")}}</label>
				       <md-select  aria-label="dropdown" placeholder ="{{translate.load('sbi.behavioural.lov.placeholder.script')}}"
				       	name ="scriptLanguageDropdown" 
				        ng-required = "selectedLov.itypeCd == lovItemEnum.SCRIPT"
				        ng-model="selectedScriptType.language"
				        ng-change="changeType(selectedScriptType.language,'script');modeChanged(selectedScriptType.language);"
				        > <md-option 
				        ng-repeat="l in listOfScriptTypes track by $index" value="{{l.VALUE_CD}}">{{l.VALUE_NM}} </md-option>
				       </md-select>
				       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedScriptType.language == null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>   
				        </md-input-container>
				   </div>
			</div>
			
			<label>{{translate.load("sbi.functionscatalog.script")}}</label>
			<md-input-container class="md-block">
		          
		          <textarea flex ui-codemirror="cmOption" ng-model="selectedScriptType.text" ></textarea>
        	</md-input-container>
		</div>
		
		<div ng-show="selectedLov.itypeCd == lovItemEnum.QUERY">    
				    <div layout="row" layout-wrap>
      				<div flex=100>
				       <md-input-container class="md-block" > 
				       <label>{{translate.load("sbi.datasource.label")}}</label>
				       <md-select  aria-label="dropdown" placeholder ="{{translate.load('sbi.behavioural.lov.placeholder.datasource')}}"
				       	name ="queryDsDropdown" 
				        ng-required = "electedLov.itypeCd == lovItemEnum.QUERY"
				        ng-model="selectedQuery.datasource"
				        ng-change="changeDatasourceCombo(selectedQuery.datasource);modeChanged(selectedQuery.datasource);"
				        > <md-option 
				        ng-repeat="l in listOfDatasources track by $index" value="{{l.label}}">{{l.label}} </md-option>
				       </md-select>
				       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedQuery.datasource == null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>   
				        </md-input-container>
				   </div>
			</div>
			<label>{{translate.load("sbi.tools.dataset.qbedatasetswizard.query")}}</label>
			 <md-input-container class="md-block">
		          
		          <textarea flex ui-codemirror="cmOption" ng-model="selectedQuery.query"></textarea>
        	</md-input-container>
		</div>
		
		<div ng-if="selectedLov.itypeCd == lovItemEnum.FIX_LOV">    
			
						<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="md-block">
							<label>{{translate.load("sbi.generic.value")}}</label>
							<input name="fixLovValue" ng-model="selectedFIXLov.VALUE"
							ng-maxlength="20" ng-change="setDirty()">
							
							 </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="md-block">
							<label>{{translate.load("sbi.generic.descr")}}</label>
							<input name="fixLovDescription" ng-model="selectedFIXLov.DESCRIPTION"
						    ng-maxlength="160" ng-change="setDirty()">
						    
						     </md-input-container>
						</div>
					</div>
					<div layout="row">
						<span flex></span><md-button class="md-flat" ng-click="addNewFixLOV()">{{translate.load("sbi.generic.save");}}</md-button>
					</div>
					
					
				   <div>
				   
			
				    <angular-table
				    	style="height:27%;"
						flex
						id="listForFixLov_id" 
						ng-model="listForFixLov"
						columns='[
								  {"label":"Value","name":"VALUE"},
								  {"label":"Description","name":"DESCRIPTION"}
								]'
						show-search-bar ="false"		
						highlights-selected-item=true
						speed-menu-option="fixLovSpeedMenu"
						click-function="itemOnClickFixLov(item)">
		     </angular-table>
				   
				   
				   </div>
		</div>
		
		<div ng-if="selectedLov.itypeCd == lovItemEnum.JAVA_CLASS">    
			
			<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="md-block">
							<label>{{translate.load("sbi.ds.jclassName")}}</label>
							<input name="javaClassName" ng-model="selectedJavaClass.name" ng-required="selectedLov.itypeCd == lovItemEnum.JAVA_CLASS"
							ng-maxlength="160" ng-change="setDirty()">
							
							<div  ng-messages="attributeForm.lbl.$error" ng-show="selectedJavaClass.name == null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
							
							 </md-input-container>
						</div>
					</div>
				   
		</div>
		
		<div ng-if="selectedLov.itypeCd == lovItemEnum.DATASET">    
			
			<div layout="row" layout-wrap>
      				<div flex=100>
				       <md-input-container class="md-block" > 
				       <label>{{translate.load("sbi.datasource.label")}}</label>
				       <md-select  aria-label="dropdown" placeholder ="{{translate.load('sbi.behavioural.lov.placeholder.dataset')}}"
				       	name ="datasetDropdown" 
				        ng-required = "selectedLov.itypeCd == lovItemEnum.DATASET"
				        ng-model="selectedDataset.id"
				        ng-change="changeType(selectedDataset.name,'dataset')"
				        > <md-option 
				        ng-repeat="l in listOfDatasets track by $index" value="{{l.id}}">{{l.label}} </md-option>
				       </md-select>
				       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedDataset.id == null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>   
				        </md-input-container>
				   </div>
			</div>
		
				   
		</div>
					    
		 	</md-card-content>
		 	</md-card>
		
		</form>
		</detail>
	</angular-list-detail>
</body>
	
</html>
