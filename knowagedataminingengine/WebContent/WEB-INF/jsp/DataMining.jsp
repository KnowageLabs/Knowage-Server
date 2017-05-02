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

<%-- 
author:...
--%>

<%@page import="org.json.JSONObject"%>
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spago.configuration.*"%>
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.engines.datamining.DataMiningEngineConfig"%>
<%@page import="it.eng.spagobi.engines.datamining.DataMiningEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.services.common.EnginConf"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>
<%@page import="it.eng.spagobi.engines.datamining.model.DataMiningDataset"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	DataMiningEngineInstance dataMiningEngineInstance;
	String documentId;
	String documentLabel;
	UserProfile profile;
	Locale locale;
	String isFromCross;
	String spagobiServerHost;
	String spagobiContext;
	String spagobiSpagoController;
	String executionOutput;
	Boolean doUploadDatasets= false;
	
	ExecutionSession es = new ExecutionSession(request, request.getSession());
	
	dataMiningEngineInstance = (DataMiningEngineInstance)es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE );
	//documentId = (String)es.getAttributeFromSession(EngineConstants.DOCUMENT_ID );
	documentLabel= (String)es.getAttributeFromSession(EngineConstants.ENV_DOCUMENT_LABEL);	
	
	profile = (UserProfile)dataMiningEngineInstance.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	locale = (Locale)dataMiningEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);
	
	isFromCross = (String)dataMiningEngineInstance.getEnv().get("isFromCross");
	if (isFromCross == null) {
		isFromCross = "false";
	}
    
    spagobiServerHost = request.getParameter(SpagoBIConstants.SBI_HOST);
    spagobiContext = request.getParameter(SpagoBIConstants.SBI_CONTEXT);
    spagobiSpagoController = request.getParameter(SpagoBIConstants.SBI_SPAGO_CONTROLLER);
    
    Map analyticalDriversMap=dataMiningEngineInstance.getAnalyticalDrivers();
    analyticalDriversMap.toString();
    JSONObject analyticalDriverJSON=new JSONObject(analyticalDriversMap);
	String analyticalDriverString=analyticalDriverJSON.toString();
%>

<script type="text/javascript">
	
	var analyticalDriverString=<%=analyticalDriverString%>;

</script>

	
<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<!doctype html>
<html ng-app="dataMiningApp">
	
	<head>
		<%@include file="commons/angular/angularImport.jsp"%>
		<%@include file="commons/angular/dataminingModule.jsp"%>
		<%@include file="commons/includeMessageResource.jspf" %>
		
<%-- 
		<script type="text/javascript" src="/knowagedataminingengine/js/src/angular_1.4/datamining/dataminingController.js"></script>
		<link rel="stylesheet" type="text/css" href="/knowagedataminingengine/css/generalStyle.css">
		<link rel="stylesheet" type="text/css" href="/knowagedataminingengine/css/datamining.css">	
 --%>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/datamining/dataminingController.js"></script>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/generalStyle.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/datamining.css">	
	</head>
<body class="bodyStile" ng-controller="Controller" ng-class="{'loading-body' : pendingRequest > 0}" ng-cloak>
	<div id="popupContainer">
	</div>
		<md-content class="no-margin-bottom" ng-hide = "pendingRequest > 0" layout-margin layout="row" layout-align = "start stretch ">
			<md-input-container flex="50">
				<md-select placeholder="Select Command" ng-model="cmd" ng-change="calculateResult(cmd)">
		          <md-option ng-repeat="cmd in commands" ng-value="cmd">
		            {{cmd.label}}
		          </md-option>
		        </md-select>
			</md-input-container>
			<md-button layout="column" class="md-fab md-raised" ng-click="toogleVariableForm(cmd)" ng-if = "cmd.variables !== null && cmd.variables !== 'null' && cmd.variables !== undefined && cmd.variables.length > 0">
				<md-tooltip md-direction="bottom" ng-if="!variableForm">
				    {{translate.load("sbi.datamining.setcommandvariables");}} 
				</md-tooltip>
				<md-icon ng-show="!variableForm" class="fa fa-cogs center-ico"></md-icon>
					<md-tooltip md-direction="bottom" ng-if="variableForm">
				    	{{translate.load("sbi.datamining.returnoutputpage");}} 
					</md-tooltip>
				<md-icon ng-show="variableForm" class="fa fa-undo center-ico"></md-icon>	
			</md-button>
		</md-content>
		<div class="loading-message" ng-if="pendingRequest > 0" layout="row" layout-align="center center">
				<i class="fa fa-spinner fa-spin fa-5x"></i>
				<span class="loading-padding">
				{{translate.load("sbi.dm.execution.loading");}} 
				</span>
		</div>
		<md-content class="no-margin-top" ng-hide = "pendingRequest > 0" layout="column" layout-margin>
			<md-content class="no-margin-top" ng-if="variableForm" layout-wrap>
				<div class = "border-container" >
					<md-toolbar class="md-blue minihead element-border">
					    <div class="md-toolbar-tools">
					 		{{translate.load("sbi.datamining.updatevariablesofcommand");}} '<b>{{cmd.label}}</b>' 
					 	</div>
					 </md-toolbar>
				 	<div ng-repeat = "variable in cmd.variables">
						 <md-content layout='row' layout-wrap layout-align="center center">
							 <md-input-container flex='70'>
							 		<label>
							 		 <b>{{ variable.name }}</b> 
							 		</label>
								 	<input type='text' ng-model="variable.currentVal" aria-label = "variable value"/>
							 </md-input-container>
						 	<md-button class="md-button md-raised md-ExtraMini" arial-label="Update" ng-click="setVariable(cmd, undefined, variable, 'set', 'command')" ng-disabled="variable.currentVal.length <= 0">
			 				 	{{translate.load("sbi.datamining.set");}}
							</md-button>
							<md-button class="md-button md-raised md-ExtraMini" arial-label="Reset" ng-click="setVariable(cmd, undefined, variable, 'reset','command')" ng-disabled="variable.currentVal == variable.defaultVal">
			 				 	{{translate.load("sbi.datamining.reset");}}
							</md-button>
						 </md-content>
					</div>
				</div>
			</md-content>
			<md-content class="no-margin-top no-margin-bottom" ng-if = "!variableForm && visibleUploadButton && cmd !== undefined" layout='row' layout-wrap>
				<div flex="30">
					<md-select placeholder="Select Dataset to Upload" ng-model="dataset">
						<md-option ng-value="ds" ng-repeat="ds in datasets[cmd.name]" > 
							{{ds.label}}
						</md-option>
					</md-select>
				</div>
				<div ng-if="dataset" layout="row" layout-align="start center">
					<div ng-if="visibilityRerunButton">
						<md-button class="md-button md-raised md-ExtraMini md-larger" aria-label="Rerun Script" ng-click="rerunScript(cmd)">
							{{translate.load("sbi.datamining.runscript");}}
						</md-button>
					</div>
					<!-- Simulate button and link the click event with the input type='file' -->
					<label class="md-button md-raised md-ExtraMini" md-ink-ripple for="fileInput">
						<span >File</span>
					</label>
					<input id="fileInput" type="file" class="ng-hide" aria-label="fileName" onchange='angular.element(this).scope().setFileName(this)'>
					<md-input-container >
						<input type='text' ng-disabled="true" ng-model="fileName" aria-label="fileName"/>
					</md-input-container>
					<md-button class="md-fab md-raised" ng-disabled="!fileName || fileName.length == 0" arial-label="Upload File" ng-click="uploadFile(cmd,dataset)">
						<md-icon class="fa fa-upload center-ico"></md-icon>
						<md-tooltip md-direction="bottom">
	         				{{translate.load("sbi.datamining.uploadfile");}}
	       				</md-tooltip>
					</md-button>
				</div>
			</md-content>
		
			<md-content ng-if="!variableForm" layout-wrap>
				<md-tabs md-selected="idx_output" layout="column" md-dynamic-height> 
					<md-tab class="mini-tabs" ng-repeat="out in cmd.outputs" label="{{out.ouputLabel}}" md-on-select="getOutputResultFromTabClick(cmd,out)">
						<md-content layout="column" layout-padding>
							<md-content class="no-padding-top">
								<md-content class="no-margin-top" layout = 'row' layout-wrap>
									<div ng-if = "out.variables !== 'null' && out.variables !== undefined && out.variables.length > 0">
										<md-button class="md-button md-raised md-ExtraMini md-larger" ng-click = "toogleOuputVariables()" arial-label = "button output variable" >
											<span style="font-size: 65%;">{{translate.load("sbi.datamining.outputvariables")}} </span>
										</md-button>
									</div>
								</md-content>
								
								<div class = "border-container little-margin-bottom" ng-if = "visibleOuputVariables && out.variables !== undefined && out.variables.length > 0" >
									<md-toolbar class="md-blue minihead element-border">
									    <div class="md-toolbar-tools">
									 		{{translate.load("sbi.datamining.updatevariablesofoutput")}} '<b>{{out.ouputLabel}}</b>'
									 	</div>
									 </md-toolbar>
									 <div ng-repeat = "variable in out.variables">
										 <md-content layout='row' layout-wrap layout-align="center center">
											 <md-input-container flex='70'>
											    <label><b>{{ variable.name }}</b></label>
											 	<input type='text' ng-model="variable.currentVal" aria-label = "variable value"/>
											 </md-input-container>
										 	<md-button class="md-button md-raised md-ExtraMini" arial-label="Update" ng-click="setVariable(cmd, out, variable, 'set','output')" ng-disabled="variable.currentVal.length <= 0">
							 				 	{{translate.load("sbi.datamining.set");}}
											</md-button>
											<md-button class="md-button md-raised md-ExtraMini" arial-label="Reset" ng-click="setVariable(cmd, out, variable, 'reset','output')" ng-disabled="variable.currentVal == variable.defaultVal">
							 				 	{{translate.load("sbi.datamining.reset");}}
											</md-button>
										 </md-content>
									 </div>
								</div>
								<div class = "border-container">
									<md-toolbar class="minihead element-border" ng-class="{'error-toolbar' : results[cmd.name][out.outputName].error}">
										    <div class="md-toolbar-tools">
										 		<span ng-if="!results[cmd.name][out.outputName].error">{{translate.load("sbi.datamining.results");}}</span>
										 		<span ng-if="results[cmd.name][out.outputName].error">{{translate.load("sbi.datamining.errors");}}</span> 
										 	</div>
								 	</md-toolbar>
								 	<md-content layout-margin layout-align="stretch center">
								 		<div ng-if="results[cmd.name][out.outputName].error">
								 			{{results[cmd.name][out.outputName].error}}
								 		</div>
										<div class="div-image" ng-if = "results[cmd.name][out.outputName].outputType == 'image' || results[cmd.name][out.outputName].outputType == 'Image' ">
											<div layout="row" layout-align="end start">
												<md-input-container>
													<label>Zoom %</label>
													<input type="number" min="0" ng-model="results[cmd.name][out.outputName].zoomX">
												</md-input-container>
												
												<md-button class="md-fab md-raised" arial-label="Download Image" ng-click="downloadImage(results[cmd.name][out.outputName].result)">
													<md-icon class="fa fa-download center-ico"></md-icon>
													<md-tooltip md-direction="bottom">
		         										{{translate.load("sbi.datamining.downloadimage");}}
		       										</md-tooltip>
												</md-button>
											</div>
											
											<!--  <div layout="row" layout-align="center center">
												<md-input-container>
													 <label>Zoom (%)</label>
													<input type="number" min="0" ng-model="results[cmd.name][out.outputName].zoomX">
												</md-input-container>
												<div flex="5"></div>
												<md-input-container>
													<label>Zoom Y (%)</label>
													<input type="number" min="0" ng-model="results[cmd.name][out.outputName].zoomY">
												</md-input-container>
											</div>-->
											
											
											<div layout="row" layout-align="center center" ng-if="results[cmd.name][out.outputName].result != null && results[cmd.name][out.outputName].result.length > 0">

												
												<div layout="row" layout-align="center center" >
													<md-content>
														<img id="img" ng-if="isChrome" style="zoom: {{results[cmd.name][out.outputName].zoomX}}%; -moz-transform: scale({{results[cmd.name][out.outputName].zoomX / 100}});" alt="Result for '{{results[cmd.name][out.outputName].plotName}}'" src="{{results[cmd.name][out.outputName].outputType == 'image' ? results[cmd.name][out.outputName].result: null}}" />
														<img id="img" ng-if="!isChrome" style="width: {{results[cmd.name][out.outputName].zoomX}}%; height:{{results[cmd.name][out.outputName].zoomX}}%" alt="Result for '{{results[cmd.name][out.outputName].plotName}}'" src="{{results[cmd.name][out.outputName].outputType == 'image' ? results[cmd.name][out.outputName].result: null}}" />
													</md-content>
												</div>	
												<br>
											</div>
																						
										</div>
										<div layout-padding class="div-text" ng-if = "results[cmd.name][out.outputName].outputType == 'text' ">
											<h3 class="md-subhead">	
												{{out.outputName}} = {{results[cmd.name][out.outputName].result}}
											</h3>
										</div>
																
										<div layout-padding class="div-text" ng-if = "results[cmd.name][out.outputName].outputType == 'Dataset'|| results[cmd.name][out.outputName].outputType == 'dataset' || results[cmd.name][out.outputName].outputType == 'spagobi_ds' ">
											<h3 class="md-subhead">	
												{{out.outputName}} = {{translate.load("sbi.datamining.spagobidatasetsaved")}} &nbsp; {{results[cmd.name][out.outputName].result}}
											</h3>
										</div>
										
										
										<div layout-padding class="div-text" ng-if = "results[cmd.name][out.outputName].outputType == 'html' ">
										  	<div ng-bind-html="putSafeHtml(results[cmd.name][out.outputName].result)">
											<!--<div >
												<p>{{results[cmd.name][out.outputName].result}}</p>
												<iframe srcdoc="{{results[cmd.name][out.outputName].result}}"></iframe>
												<iframe srcdoc="<html><body>Hello, <b>world</b>.</body></html>"></iframe>
												
												<!--  								
												<iframe srcdoc="{{htmlShow}}"></iframe>
												{{htmlShow}} P -->
												
												
											</div>
										</div>
									</md-content>
								</div>
								
							</md-content>
						</md-content>
						<md-content style="height : 20px;">
						<!-- This content avoids to cut the previous contents, so the results are well formatted -->
						</md-content>
					</md-tab>
				</md-tabs>
			</md-content>
		</md-content>
</body>	
	
</html>
