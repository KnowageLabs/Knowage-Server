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
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>



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
    String userNameOwner=(String)myUserProfile.getUserUniqueIdentifier();
	
    boolean isDev=UserUtilities.hasDeveloperRole(profile);
    boolean isUser=UserUtilities.hasUserRole(profile);
    
	boolean adminView=false,userView=false,devView=false;
	if(isAdmin){ adminView=true;}
	if(isDev && !isAdmin){ devView=true;}
	if(isUser && !isDev && !isAdmin){ userView=true; } 


%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<script type="text/javascript">
	// var isAdminGlobal=<%=admin.toString()%>
	var isAdminGlobal=<%=adminView%>
	var isUserGlobal=<%=userView%>
	var isDevGlobal=<%=devView%>
</script>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="functionsCatalogControllerModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/sbi_default/css/FunctionsCatalog/functionsCatalog.css">

<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/functionsCatalog/functionsCatalog.js"></script>

<!-- Codemirror  -->
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/mathematicaModified.js"></script>  
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/python/python.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/r/r.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/display/autorefresh.js"></script>

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/wysiwyg.min.js")%>"></script>	
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/editor.min.css")%>"> 





<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Functions Catalog</title>
</head>

<body  class="bodyStyle" ng-controller="functionsCatalogController" ng-cloak ng-init="userId='<%=getUserId.toString()%>'; isAdmin=<%=adminView%>; ownerUserName='<%=userNameOwner.toString()%>'; isDev=<%=devView%>; isUser=<%=userView%>">  <!-- only one between isAdmin, isDev, isUser is true (see java code)-->
	
	<angular-list-detail show-detail="showDetail"  >
		
		<% 
		String addFunction="";
		if(isAdmin || isDev){
			addFunction="addFunction";
			
		   } 
		%>
		
		<list label="Functions"  new-function="<%=addFunction%>" layout-column> 
    		<angular-table
    				id="functionsTable" 
					flex
					ng-show=true
					ng-model="functionsList"
					columns='[{"label":"Function Name","name":"name"}]' 
					columns-search='["name","keywords","description"]'
					show-search-bar=true
					highlights-selected-item=true
					speed-menu-option="acSpeedMenu"
					click-function ="leftTableClick(item)"
					selected-item="tableSelectedFunction"					
			>						
			</angular-table>
    	
    	
    	 
    	</list>
    	
    	
       <detail label='shownFunction.name==undefined? "Demo" : "Demo: "+shownFunction.name' save-function="saveFunction" 	cancel-function="cancelFunction"
       	disable-cancel-button=false
		disable-save-button=false
		show-save-button="isAdmin || (isDev && shownFunction.owner==ownerUserName)">
       		<md-tabs layout-fill> 
       		
       			<md-tab label='{{translate.load("sbi.functionscatalog.general");}}'>
					<md-content layout-padding>
						
						<md-input-container class="md-block">
        					<label>{{translate.load("sbi.functionscatalog.functionname");}}</label>
        					<input ng-model=shownFunction.name ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">  <!-- prima era !isAdmin senza apici-->
      					</md-input-container>
  
  						<md-input-container class="md-block">
        					<label>{{translate.load("sbi.functionscatalog.label");}}</label>
        					<input ng-model=shownFunction.label ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
      					</md-input-container>    					
      					
      					<md-input-container class="md-block">
        					<label>{{translate.load("sbi.functionscatalog.owner");}}</label>
        					<input ng-model=shownFunction.owner ng-disabled=true>
      					</md-input-container> 

						<md-input-container class="md-block">
        					<label>{{translate.load("sbi.functionscatalog.type");}}</label>
        					<md-select ng-model=shownFunction.type ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
              					<md-option ng-repeat="functionType in functionTypesList" value="{{functionType.valueCd}}">
                					{{functionType.valueCd}}
              					</md-option>
           					</md-select>
      					</md-input-container> 

          				<label>{{translate.load("sbi.functionscatalog.description");}}</label>
	          			<wysiwyg-edit ng-if="(isAdmin || (isDev && shownFunction.owner==ownerUserName))" content="shownFunction.description"  layout-fill config="editorConfig"></wysiwyg-edit>
	          			<!--  <textarea ui-refresh="true" ng-model="shownFunction.description" ng-disabled=!isAdmin></textarea> -->
	          			<div ng-if="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))" ng-bind-html=shownFunction.description><!-- {{shownFunction.description | htmlSafe }}--></div> 
	          			<!--  <iframe srcdoc="{{shownFunction.description}}" flex layout-fill>
						</iframe> -->
					
						
	          			<!-- <div ng-if="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))" ng-bind-html="$compile(shownFunction.description)"></div> -->
	          				
        				
        				<md-input-container class="md-block">
        					<label>{{translate.load("sbi.functionscatalog.keywords");}}</label>
        					<br></br>        					
							<md-chips ng-model="shownFunction.keywords" readonly="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))" ></md-chips>
						</md-input-container
						
 
        				
					</md-content>
				</md-tab>
       		
				<md-tab label='{{translate.load("sbi.functionscatalog.input");}}'>
					<md-content layout-padding>
  						<div>
  							{{translate.load("sbi.functionscatalog.inputdatasets");}}
  							<i class="fa fa-plus-square" ng-click="input=addInputDataset()" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></i>  							
  							<div ng-if="shownFunction.inputDatasets.length==0 && !isAdmin" layout-align="start center">
  								&emsp;&emsp;{{translate.load("sbi.functionscatalog.noinputdatasetsrequired");}}</br></br>
  							</div>
  							<div ng-repeat="d in shownFunction.inputDatasets" layout-gt-sm="row" layout-align="start center">

	     						<div layout="row">
	     							    								
      								<md-input-container class="md-block" flex-gt-sm>
            							<label>{{translate.load("sbi.functionscatalog.datasetlabel");}}</label>
            							<md-select ng-model="d.label" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
              								<md-option ng-repeat="datasetLabel in datasetLabelsList" value="{{datasetLabel}}">
                								{{datasetLabel}}
              								</md-option>
           								</md-select>
          							</md-input-container>
									<div layout-padding layout-margin>
      									<md-button class="md-raised md-ExtraMini" ng-click="datasetPreview(d.label)">{{translate.load("sbi.functionscatalog.datasetpreview");}}</md-button>   								
									</div>
      								
	     						</div> 						
								
								<div ng-if="d.type=='Simple Input'" layout="row">
	     							
	     							<md-input-container>
        								<label>{{translate.load("sbi.functionscatalog.inputname");}}</label>  
        								<input ng-model="d.name" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
      								</md-input-container>
      								
      								<md-input-container>
        								<label>{{translate.load("sbi.functionscatalog.inputvalue");}}</label>  
        								<input ng-model="d.value" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
      								</md-input-container>
      								
	     						</div> 	
									      						
	      						<div>
									<i class="fa fa-minus-square" ng-click="output=removeInputDataset(i)" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></i> 	
								</div>	      						
  							</div>
  						</div>
						
						<div>
  							Input Variables
  							<i class="fa fa-plus-square" ng-click="input=addInputVariable()" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></i>
  							<div ng-if="shownFunction.inputVariables.length==0  &&  !(isAdmin || (isDev && shownFunction.owner==ownerUserName))  " layout-align="start center">
  								&emsp;&emsp; {{translate.load("sbi.functionscatalog.noinputvariablesrequired");}}</br></br>
  							</div>
  							
  							<div ng-repeat="v in shownFunction.inputVariables" layout-gt-sm="row" layout-align="start center">

	     						<div layout="row"> 								
      								<md-input-container class="md-block" flex-gt-sm>
            							<label>{{translate.load("sbi.functionscatalog.variablename");}}</label>
        								<input ng-model="v.name" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
      								</md-input-container>
      								
      								<md-input-container class="md-block" flex-gt-sm>
            							<label>{{translate.load("sbi.functionscatalog.variablevalue");}}</label>
        								<input ng-model="v.value" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
      								</md-input-container>
	     						</div> 						
				      						
	      						<div>
									<i class="fa fa-minus-square" ng-click="output=removeInputVariable(i)" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></i> 	
								</div>	      						
  							</div>
  						</div>
						
						
						
						
					</md-content>
				</md-tab>
				<md-tab label='{{translate.load("sbi.functionscatalog.script");}}' ng-if="(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
					<md-content layout-padding>
  					
      					<md-input-container class="md-block" flex-gt-sm>
            				<label>{{translate.load("sbi.functionscatalog.language");}}</label>
            				<md-select ng-model="shownFunction.language">
              					<md-option ng-repeat="language in languages" value="{{language}}">
                					{{language}}
              					</md-option>
           					</md-select>
          				</md-input-container>
      						
  						<md-input-container class="md-block" flex>
          					<label>{{translate.load("sbi.functionscatalog.script");}}</label>
          					<textarea flex ui-refresh="true" ng-model="shownFunction.script" ui-codemirror ui-codemirror-opts="editorOptions"></textarea>
        				</md-input-container>
									
        			</md-content>	
  						    					
				</md-tab>
				<md-tab label='{{translate.load("sbi.functionscatalog.output");}}'>
					<md-content layout-padding>
  						<div>
  							{{translate.load("sbi.functionscatalog.output");}} 
  							<i class="fa fa-plus-square" ng-click="output=addOutputItem()" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></i>
  							
  							<div ng-if="shownFunction.outputItems.length==0  && !(isAdmin || (isDev && shownFunction.owner==ownerUserName))" layout-align="start center">
  								&emsp;&emsp;{{translate.load("sbi.functionscatalog.nooutputexpected");}}</br></br>
  							</div>
  							
  							<div ng-repeat="o in shownFunction.outputItems" layout-gt-sm="row" layout-align="start center">	      						
	      						<!--<div>
									<md-button class="md-raised md-ExtraMini">Show Preview</md-button>
	      						</div>-->
	  							<md-input-container>
	        						<label>{{translate.load("sbi.functionscatalog.label");}}</label>
	        						<input ng-model="o.label" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
	      						</md-input-container>
	      						
	      						<md-input-container aria-hidden="true">
	      							<label>{{translate.load("sbi.functionscatalog.type");}}</label>
	        						<md-select ng-model="o.type" ng-disabled="!(isAdmin || (isDev && shownFunction.owner==ownerUserName))">
              							<md-option ng-repeat="type in outputTypes" value="{{type}}">
                							{{type}}
              							</md-option>
           							</md-select>
	      						</md-input-container>
	      						
	      						<div>
									<i class="fa fa-minus-square" ng-click="removeOutputItem(o)" aria-hidden="true" ng-show="(isAdmin || (isDev && shownFunction.owner==ownerUserName))"></i>	
								</div>	  
								
  							</div>
  						</div>
						
					</md-content>
				</md-tab>
		
							
       		</md-tabs>
       
       </detail>
	
		
	</angular-list-detail>



</div>

</body>
</html>
