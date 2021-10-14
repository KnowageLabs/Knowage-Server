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
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%-- <%@page import="it.eng.spagobi.commons.bo.UserProfile"%> --%>
<%@page import="it.eng.spagobi.services.security.bo.SpagoBIUserProfile"%>
<%@page import="java.util.Enumeration"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.tools.dataset.federation.FederationDefinition"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8"%>



<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spago.configuration.*"%>
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.services.common.EnginConf"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="it.eng.spagobi.commons.dao.IRoleDAO"%>


<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%

	IEngUserProfile profile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);;
	profile.getUserAttributeNames();
	String getUserId = ((UserProfile)profile).getUserId().toString();	
	UserProfile myUserProfile=(UserProfile)profile;
	String[] names=session.getValueNames();
    session.getAttribute("REQUEST_CONTAINER");
    //boolean isAdmin=UserUtilities.isAdministrator(profile);
    boolean isAdmin=UserUtilities.hasAdministratorRole(profile);
    UserUtilities.isAdministrator(profile);
    String admin=isAdmin+"";
    String userNameOwner=(String)myUserProfile.getUserId();
	
    boolean isDev=UserUtilities.hasDeveloperRole(profile);
    boolean isUser=UserUtilities.hasUserRole(profile);
    
	boolean adminView=false,userView=false,devView=false;
	if(isAdmin){ adminView=true;}
	if(isDev && !isAdmin){ devView=true;}
	if(isUser && !isDev && !isAdmin){ userView=true; } 


%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<script type="text/javascript">
	// var isAdminGlobal=<%=admin.toString()%>;
	var isAdminGlobal=<%=adminView%>;
	var isUserGlobal=<%=userView%>;
	var isDevGlobal=<%=devView%>;
	var ownerUserName="<%=userNameOwner.toString()%>";
</script>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="functionsCatalogControllerModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script>

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/functionsCatalog/functionsCatalog.js")%>"></script>

<!-- Codemirror  -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css")%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css")%>">  
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js")%>"></script>  
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/ui-codemirror.js")%>"></script> 
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/mathematicaModified.js")%>"></script>  
<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css")%>" />
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/mode/python/python.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/mode/r/r.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/display/autorefresh.js")%>"></script>

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "node_modules/ng-wysiwyg/dist/wysiwyg.min.js")%>"></script>	
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "node_modules/ng-wysiwyg/dist/editor.min.css")%>"> 





<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Functions Catalog</title>
</head>

<body  ng-controller="functionsCatalogController" class="bodyStyle kn-functionsCatalog" ng-cloak ng-init="userId='<%=getUserId.toString()%>'; isAdmin=<%=adminView%>; ownerUserName='<%=userNameOwner.toString()%>'; isDev=<%=devView%>; isUser=<%=userView%>">  <!-- only one between isAdmin, isDev, isUser is true (see java code)-->
	<%if(includeInfusion){ %> 
            <%@include file="/WEB-INF/jsp/commons/infusion/infusionTemplate.html"%> 
<%} %>
	<angular-list-detail full-screen=true layout="column">
		
		<% 
		String addFunction="";
		if(isAdmin || isDev){
			addFunction="addFunction";
			
		   } 
		%>

		<list label="Functions"  new-function="<%=addFunction%>" layout-column> 
			<md-content>
				<div layout-gt-xs="row" layout="column" class="functionsCardContainer">
				
					<md-card layout="column" layout-align="center center" class="functionsCard"  ng-class="{'active':selectedType == functionType.valueCd}" ng-repeat="functionType in functionTypesList" ng-click="functionsList=filterByType(functionType)" ng-style="{'background-image':functionType.valueDescription}" flex>
						<md-card-content layout="column" layout-align="center center" class="noPadding" flex>
		          				<span class="md-headline ng-binding">{{functionType.valueCd}}</span>
                                <span class="md-subhead ng-binding smallGrey">{{functionType.domainName}}</span>
						</md-card-content>
					
		      		</md-card>
		      		
					<md-card  class="functionsCard image_all" ng-class="{'active':selectedType == 'All'}" ng-click="functionsList=filterByType({valueCd:'All'})" flex layout="column" layout-align="center center"> 
						<md-card-content layout="column" layout-align="center center" class="noPadding" flex>
		            			<span class="md-headline ng-binding" >{{translate.load("sbi.functionscatalog.all")}}</span>
		            			<span class="md-subhead ng-binding smallGrey">{{translate.load("sbi.functionscatalog.allmessage")}}</span>
						</md-card-content>

		      		</md-card>
				</div>	
					<div class="functionsChipsContainer" layout="row" layout-align="center" layout-wrap>
						<div class="functionsChips" ng-repeat="chip in searchTags" ng-click="chipFilter(chip)" ng-class="{'chipSelected':selectedChip==chip}">
							{{chip}}
						</div>
					</div>
					
					<div layout="row" layout-align="center center" ng-if="!functionsList || functionsList.length == 0">
						<div class="kn-noItems" flex="60" flex-xs="100">
							<p class="ng-binding">{{translate.load("sbi.functionscatalog.nofunctions")}}</p>
						</div>
					</div>
			    		<angular-table
			    				id="functionsTable" 
								ng-show="functionsList && functionsList.length >0"
								ng-model="functionsList"
								columns='[{"label":"Function Label","name":"label","size":200},{"label":"Function Name","name":"name","size":200},{"label":"Type","name":"type","size":200},{"label":"Language","name":"language","size":200},{"label":"Owner","name":"owner","size":200}]'  
								columns-search='["label","name","description","owner"]'
								show-search-bar=true
								highlights-selected-item=true
								speed-menu-option="acSpeedMenu"
								click-function ="leftTableClick(item)"
								selected-item="tableSelectedFunction"
								no-pagination=true
								layout-padding					
						>						
						</angular-table>
			</md-content>
    	</list>
    	
       <detail label='shownFunction.name==undefined? "" : shownFunction.name' save-function="saveFunction" 	cancel-function="cancelFunction" disable-cancel-button=false disable-save-button=false	show-save-button="isAdmin || (isDev && shownFunction.owner==ownerUserName)">
       		<md-tabs layout-fill> 
       		
       			<md-tab label='{{translate.load("sbi.functionscatalog.general");}}'>
					<md-card>
						<md-card-content>
							<div layout="row">
								<md-input-container class="md-block" flex>
		        					<label>{{translate.load("sbi.functionscatalog.functionname");}}</label>
		        					<input ng-model=shownFunction.name ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
		        					<div class="hint">Name of the function</div>
		      					</md-input-container>
		  
		  						<md-input-container class="md-block" flex>
		        					<label>{{translate.load("sbi.functionscatalog.label");}}</label>
		        					<input ng-model=shownFunction.label ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
		        					<div class="hint">Name of the function that will be displayed</div>
		      					</md-input-container> 
							</div>
							   					
	      					<div layout="row">
		      					<md-input-container class="md-block" flex>
		        					<label>{{translate.load("sbi.functionscatalog.owner");}}</label>
		        					<input ng-model=shownFunction.owner ng-disabled=true>
		      					</md-input-container> 
		
								<md-input-container class="md-block" flex>
		        					<label>{{translate.load("sbi.functionscatalog.type");}}</label>
		        					<md-select ng-model=shownFunction.type ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
		              					<md-option ng-repeat="functionType in functionTypesList" value="{{functionType.valueCd}}">
		                					{{functionType.valueCd}}
		              					</md-option>
		           					</md-select>
		      					</md-input-container>
	      					</div>
	      					
							<md-chips ng-model="shownFunction.tags" readonly="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))" placeholder="{{translate.load('sbi.functionscatalog.keywords');}}" class="noPadding" style="margin-bottom:10px;"></md-chips>
	      					
	          				<label class="customLabel">{{translate.load("sbi.functionscatalog.description");}}</label>
		          			<wysiwyg-edit ng-if="(isAdmin || (isDev && shownFunction.owner==ownerUserName))" content="shownFunction.description"  layout-fill config="editorConfig"></wysiwyg-edit>
		          			<div ng-if="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))" ng-bind-html=shownFunction.description></div> 
		          			<br>
		          			<label class="customLabel">{{translate.load("sbi.functionscatalog.benchmarks");}}</label>
		          			<wysiwyg-edit ng-if="(isAdmin || (isDev && shownFunction.owner==ownerUserName))" content="shownFunction.benchmark"  layout-fill config="editorConfig"></wysiwyg-edit>
		          			<div ng-if="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))" ng-bind-html=shownFunction.description></div>
						</md-card-content>
					</md-card>
				</md-tab>
       		
				<md-tab label='{{translate.load("sbi.functionscatalog.input");}}' >
				
					<md-card class="noMdError smallInputs">
						<md-toolbar class="secondaryToolbar">
							<div class="md-toolbar-tools">
								<h2>{{translate.load("sbi.functionscatalog.inputcolumns");}}</h2>
								<div flex></div>
								<md-button class="md-secondary" ng-click="input=addInputColumn()" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))">{{translate.load("sbi.functionscatalog.addcolumn");}}</md-button>
							</div>
						</md-toolbar>	
						<md-card-content>					
							<md-list>
								<md-list-item ng-if="shownFunction.inputColumns.length==0" class="messageItem" layout-align="center center">
									&emsp;&emsp;{{translate.load("sbi.functionscatalog.noinputcolumnsrequired");}}
								</md-list-item>	
								<md-list-item ng-repeat="c in shownFunction.inputColumns">
									<md-input-container class="md-block" flex>
	           							<label>{{translate.load("sbi.functionscatalog.columnname");}}</label>
	       								<input ng-model="c.name" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
	   								</md-input-container>
	   								<md-input-container class="md-block" flex>
	  									<label>{{translate.load("sbi.functionscatalog.columntype");}}</label>
	  									<md-select ng-model="c.type" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
		    								<md-option ng-repeat="columnType in inputColumnTypes" value="{{columnType}}">
		      								{{columnType}}
		    								</md-option>
	 									</md-select>
									</md-input-container>
	  								<md-icon class="md-secondary" md-font-icon="fa fa-trash" ng-click="output=removeInputColumn(c)" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></md-icon>
								</md-list-item>
						  	</md-list>
						</md-card-content>
					</md-card>
					
					<md-card class="noMdError smallInputs">
						<md-toolbar class="secondaryToolbar">
							<div class="md-toolbar-tools">
								<h2>{{translate.load("sbi.functionscatalog.inputvariables");}}</h2>
								<div flex></div>
								<md-button class="md-secondary" ng-click="input=addInputVariable()" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))">{{translate.load("sbi.functionscatalog.addinputvariable");}}</md-button> 
							</div>		
						</md-toolbar>	
						<md-card-content>
							<md-list>
								<md-list-item ng-if="shownFunction.inputVariables.length==0" class="messageItem" layout-align="center center">
									&emsp;&emsp;{{translate.load("sbi.functionscatalog.noinputvariablesrequired");}}
								</md-list-item>
								<md-list-item ng-repeat="v in shownFunction.inputVariables">
									<md-input-container class="md-block" flex>
	           							<label>{{translate.load("sbi.functionscatalog.variablename");}}</label>
	       								<input ng-model="v.name" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
	   								</md-input-container>
	   								<md-input-container class="md-block" flex>
	           							<label>{{translate.load("sbi.functionscatalog.variabletype");}}</label>
	       								<md-select ng-model="v.type" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
		    								<md-option ng-repeat="variableType in inputVariableTypes" value="{{variableType}}">
		      								{{variableType}}
		    								</md-option>
	 									</md-select>
	   								</md-input-container>
	   								<md-input-container class="md-block" flex>
	           							<label>{{translate.load("sbi.functionscatalog.variabledefaultvalue");}}</label>
	       								<input ng-model="v.value" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
	   								</md-input-container>
	  								<md-icon class="md-secondary" md-font-icon="fa fa-trash" ng-click="output=removeInputVariable(v)" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></md-icon>
								</md-list-item>
							</md-list>
						</md-card-content>
					</md-card>
				</md-tab>
				
				<md-tab label='{{translate.load("sbi.functionscatalog.script");}}' ng-if="(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
					<md-card>
						<md-card-content>
	    					<md-input-container class="md-block">
	            				<label>{{translate.load("sbi.functionscatalog.language");}}</label>
	            				<md-select ng-model="shownFunction.language">
	              					<md-option ng-repeat="language in languages" value="{{language}}">
	                					{{language}}
	              					</md-option>
	           					</md-select>
	          				</md-input-container>
	      						
	  						<md-input-container class="md-block md-input-has-value">
	          					<label class="customCodeMirrorLabel">{{translate.load("sbi.functionscatalog.script");}}</label>
	          					<textarea flex ui-refresh="true" ng-model="shownFunction.onlineScript" ui-codemirror ui-codemirror-opts="editorOptions"></textarea>
	        				</md-input-container>
	        				
	        				<md-input-container class="md-block md-input-has-value" ng-if="shownFunction.family=='offline'">
	          					<label class="customCodeMirrorLabel">{{translate.load("sbi.functionscatalog.trainmodel");}}</label>
	          					<textarea flex ui-refresh="true" ng-model="shownFunction.offlineScriptTrain" ui-codemirror ui-codemirror-opts="editorOptions"></textarea>
	        				</md-input-container>
	        				
	        				<md-input-container class="md-block md-input-has-value" ng-if="shownFunction.family=='offline'">
	          					<label class="customCodeMirrorLabel">{{translate.load("sbi.functionscatalog.usemodel");}}</label>
	          					<textarea flex ui-refresh="true" ng-model="shownFunction.offlineScriptUse" ui-codemirror ui-codemirror-opts="editorOptions"></textarea>
	        				</md-input-container>
						
	        			</md-card-content>
	        		</md-card>			    					
				</md-tab>
				
				<md-tab label='{{translate.load("sbi.functionscatalog.output");}}'>
					<md-card class="noMdError smallInputs">
						<md-toolbar class="secondaryToolbar">
							<div class="md-toolbar-tools">
								<h2>{{translate.load("sbi.functionscatalog.outputcolumns");}}</h2>
								<div flex></div>
								<md-button class="md-secondary" ng-click="input=addOutputColumn()" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))">{{translate.load("sbi.functionscatalog.addcolumn");}}</md-button>
							</div>
						</md-toolbar>	
						<md-card-content>					
							<md-list>
								<md-list-item ng-if="shownFunction.outputColumns.length==0" class="messageItem" layout-align="center center">
									&emsp;&emsp;{{translate.load("sbi.functionscatalog.noinputcolumnsrequired");}}
								</md-list-item>	
								<md-list-item ng-repeat="c in shownFunction.outputColumns">
									<md-input-container class="md-block" flex>
	           							<label>{{translate.load("sbi.functionscatalog.columnname");}}</label>
	       								<input ng-model="c.name" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
	   								</md-input-container>
	   								<md-input-container class="md-block" flex>
	  									<label>{{translate.load("sbi.functionscatalog.columnfieldtype");}}</label>
	  									<md-select ng-model="c.fieldType" ng-change="resetType(c)" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
		    								<md-option ng-repeat="columnFieldType in outputColumnFieldTypes" value="{{columnFieldType}}">
		      								{{columnFieldType}}
		    								</md-option>
	 									</md-select>
									</md-input-container>
	   								<md-input-container class="md-block" flex>
	  									<label>{{translate.load("sbi.functionscatalog.columntype");}}</label>
	  									<md-select ng-model="c.type" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
		    								<md-option ng-if="c.fieldType=='ATTRIBUTE' || columnType=='NUMBER'" ng-repeat="columnType in outputColumnTypes" value="{{columnType}}">
		      								{{columnType}}
		    								</md-option>
	 									</md-select>
									</md-input-container>
	  								<md-icon class="md-secondary" md-font-icon="fa fa-trash" ng-click="output=removeOutputColumn(c)" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></md-icon>
								</md-list-item>
						  	</md-list>
						</md-card-content>
					</md-card>
				</md-tab>
	
       		</md-tabs>
       
       </detail>

	</angular-list-detail>
</div>

</body>
</html>
