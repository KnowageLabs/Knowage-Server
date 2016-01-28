<%-- 
SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
--%>

<%-- 
author:...
--%>

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
	

	
	profile = (UserProfile)dataMiningEngineInstance.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	locale = (Locale)dataMiningEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);
	
	isFromCross = (String)dataMiningEngineInstance.getEnv().get("isFromCross");
	if (isFromCross == null) {
		isFromCross = "false";
	}
    
    spagobiServerHost = request.getParameter(SpagoBIConstants.SBI_HOST);
    spagobiContext = request.getParameter(SpagoBIConstants.SBI_CONTEXT);
    spagobiSpagoController = request.getParameter(SpagoBIConstants.SBI_SPAGO_CONTROLLER);
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<!doctype html>
<html ng-app="dataMiningApp">
	
	<head>
		<%@include file="commons/angular/angularImport.jsp"%>
		<%@include file="commons/angular/dataminingModule.jsp"%>
		<script type="text/javascript" src="/knowagedataminingengine/js/src/angular_1.4/datamining/dataminingController.js"></script>
		<link rel="stylesheet" type="text/css" href="/knowagedataminingengine/css/generalStyle.css">
		<link rel="stylesheet" type="text/css" href="/knowagedataminingengine/css/datamining.css">	
	</head>
<body>
	<div div ng-controller="Controller as ctrl" ng-cloak>
		<md-input-container flex="50">
			<md-select placeholder="Select Command" ng-model="cmd" ng-change="calculateResult(cmd)">
	          <md-option ng-repeat="cmd in commands" ng-value="cmd">
	            {{cmd.label}}
	          </md-option>
	        </md-select>
		</md-input-container>
		{{cmd.name}}
		<md-tabs md-selected="idx_command" layout="column"  md-dynamic-height> 
			<md-tab  ng-repeat="cmd in commands" label={{cmd.label}} md-on-select="calculateResult(cmd)">
				<md-content layout="column" layout-padding>
					<md-content ng-if = "visibleUploadButton" layout='row' layout-margin>
						<div ng-if="visibilityRerunButton">
							<md-button class="md-button md-raised md-ExtraMini md-larger" arial-label="Rerun Script" ng-click="rerunScript(cmd)">
								 Run Script
							</md-button>
						</div>
						<!-- Simulate button and link the click event with the input type='file' -->
						<label class="md-button md-raised md-ExtraMini" md-ink-ripple for="fileInput">
							<span >File</span>
						</label>
						<input id="fileInput" type="file" class="ng-hide" onchange='angular.element(this).scope().setFileName(this)'>
						<md-input-container flex='40'>
							<input type='text' disabled ng-model="fileName"/>
						</md-input-container>
						<md-button class="md-fab md-raised" arial-label="Upload File" ng-click="uploadFile(cmd)">
							<md-icon md-svg-src="/knowagedataminingengine/img/upload3.svg"></md-icon>
							<md-tooltip md-direction="bottom">
	          					Upload File
	        				</md-tooltip>
						</md-button>
					</md-content>
				
					<md-content>
						<md-tabs class="mini-tabs" md-selected="idx_output" layout="column"> 
							<md-tab class="mini-tabs" ng-repeat="out in cmd.outputs" label="{{out.ouputLabel}}">
								<md-content layout="column" layout-padding>
									<md-content>
										<md-content layout = 'row' layout-margin>
											<div ng-if = "out.variables !== 'null' && out.variables !== undefined && out.variables.length > 0">
												<md-button class="md-button md-raised md-ExtraMini md-larger" ng-click = "toogleOuputVariables()" arial-label = "button output variable" >
													<span style="font-size: 65%;">Outputs Variables</span>
												</md-button>
											</div>
										</md-content>
										
										<div class = "border-container" ng-if = "visibleOuputVariables && out.variables !== undefined && out.variables.length > 0" >
											<md-toolbar class="md-blue minihead element-border">
											    <div class="md-toolbar-tools">
											 		Update variables of output '<b>{{out.ouputLabel}}</b>'
											 	</div>
											 </md-toolbar>
											 <div ng-repeat = "variable in out.variables">
												 <md-content layout='row' layout-margin layout-align="center center">
													 <md-input-container flex='70'>
													    <label><b>{{ variable.name }}</b></label>
													 	<input type='text' ng-model="variable.currentVal" aria-label = "variable value"/>
													 </md-input-container>
												 	<md-button class="md-button md-raised md-ExtraMini" arial-label="Update" ng-click="setVariable(cmd, out, variable, 'set','output')" ng-disabled="variable.currentVal.length <= 0">
									 				 Set
													</md-button>
													<md-button class="md-button md-raised md-ExtraMini" arial-label="Reset" ng-click="setVariable(cmd, out, variable, 'reset','output')" ng-disabled="variable.currentVal == variable.defaultVal">
									 				 Reset
													</md-button>
												 </md-content>
											 </div>
										</div>
										<br>
										<div class = "border-container">
											<md-toolbar class="md-blue minihead element-border">
												    <div class="md-toolbar-tools">
												 		Results 
												 	</div>
										 	</md-toolbar>
										 	<md-content layout-margin layout-align="stretch center">
												<div class="div-image" ng-if = "results[cmd.name][out.outputName].outputType == 'image' " ng-bind-html="results[cmd.name][out.outputName].html"></div>
												<div layout-padding class="div-text" ng-if = "results[cmd.name][out.outputName].outputType == 'text' ">
													<h3 class="md-subhead">	
														{{results[cmd.name][out.outputName].variablename}} = {{results[cmd.name][out.outputName].result}}
													</h3>
												</div>
											</md-content>
										</div>
										
									</md-content>
								</md-content>
								<md-content style="height : 20px;">
								<!-- This content avoids to cut the previous contents, so the results are well formatted -->
								</md-content>
							</md-tab>
							<md-tab layout="column" class="mini-tabs" ng-if = "cmd.variables !== null && cmd.variables !== 'null' && cmd.variables !== undefined && cmd.variables.length > 0" label= "Command Variable">
								<md-content  layout-padding>
									<div class = "border-container" >
										<md-toolbar class="md-blue minihead element-border">
										    <div class="md-toolbar-tools">
										 		Update variables of command '<b>{{cmd.label}}</b>' 
										 	</div>
										 </md-toolbar>
									 	<div ng-repeat = "variable in cmd.variables">
											 <md-content layout='row' layout-margin layout-align="center center">
												 <md-input-container flex='70'>
												 		<label>
												 		 <b>{{ variable.name }}</b> 
												 		</label>
													 	<input type='text' ng-model="variable.currentVal" aria-label = "variable value"/>
												 </md-input-container>
											 	<md-button class="md-button md-raised md-ExtraMini" arial-label="Update" ng-click="setVariable(cmd, out, variable, 'set', 'command')" ng-disabled="variable.currentVal.length <= 0">
								 				 	Set
												</md-button>
												<md-button class="md-button md-raised md-ExtraMini" arial-label="Reset" ng-click="setVariable(cmd, out, variable, 'reset','command')" ng-disabled="variable.currentVal == variable.defaultVal">
								 				 	Reset
												</md-button>
											 </md-content>
										</div>
									</div>
								</md-content>
								<md-content style="height : 20px;">
									<!-- This content avoids to cut the previous contents, so the results are well formatted -->
								</md-content>
							</md-tab>
						</md-tabs>
					</md-content>
				</md-content>
			</md-tab>
		</md-tabs>
	</div>
</body>	
	
</html>
