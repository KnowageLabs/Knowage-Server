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
<%@page import="it.eng.spagobi.tools.dataset.federation.FederationDefinition"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="functionsCatalogControllerModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/sbi_default/css/FunctionsCatalog/functionsCatalog.css">

<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/functionsCatalog/functionsCatalog.js"></script>
	
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Functions Catalog</title>
</head>

<body  class="bodyStyle" ng-controller="functionsCatalogController" ng-cloak>
	<div  layout-wrap layout-fill>
	
	<angular-list-detail show-detail="showDetail" layout-column >
		
		<list label="Functions"  new-function="addFunction"> 
    		<angular-table 
					flex
					ng-show=true
					ng-model="functionsList"
					columns='[{"label":"Function Name","name":"name"}]' 
					columns-search='["name"]'
					show-search-bar=true
					highlights-selected-item=true
					speed-menu-option="acSpeedMenu"
					click-function ="leftTableClick(item)"
					selected-item="tableSelectedFunction"					
			>						
			</angular-table>
    	
    	
    	
    	</list>
    	
    	
       <detail label='shownFunction.name==undefined? "" : shownFunction.name' save-function="saveFunction" 	cancel-function="cancelFunction"
       	disable-cancel-button=false
		disable-save-button=false
		show-save-button=true>
       		<md-tabs layout-fill> 
				<md-tab label="Input">
					<md-content layout-padding>
  						<div>
  							Input dataset
  							<i class="fa fa-plus-square" ng-click="input=addInputDataset()" aria-hidden="true"></i>
  							<div ng-repeat="d in shownFunction.inputDatasets" layout-gt-sm="row" layout-align="start center">

	     						<div layout="row">
	     							    								
      								<md-input-container class="md-block" flex-gt-sm>
            							<label>Dataset Label</label>
            								<md-select ng-model="d.label">
              									<md-option ng-repeat="datasetLabel in datasetLabelsList" value="{{datasetLabel}}">
                									{{datasetLabel}}
              									</md-option>
           									</md-select>
          							</md-input-container>
									<div layout-padding layout-margin>
      									<md-button class="md-raised md-ExtraMini">Dataset Preview</md-button>   								
									</div>
      								
	     						</div> 						
								
								<div ng-if="d.type=='Simple Input'" layout="row">
	     							
	     							<md-input-container>
        								<label>Input Name</label>  
        								<input ng-model="d.name">
      								</md-input-container>
      								
      								<md-input-container>
        								<label>Input Value</label>  
        								<input ng-model="d.value">
      								</md-input-container>
      								
	     						</div> 	
									      						
	      						<div>
									<i class="fa fa-minus-square" ng-click="output=removeInputDataset(i)" aria-hidden="true"></i> 	
								</div>	      						
  							</div>
  						</div>
						
					</md-content>
				</md-tab>
				<md-tab label="Script">
					<md-content layout-padding>
					    <md-input-container>
        					<label>Function Name</label>
        					<input ng-model=shownFunction.name>
      					</md-input-container>
      					
      					<md-input-container class="md-block" flex-gt-sm>
            				<label>Language</label>
            				<md-select ng-model="shownFunction.language">
              					<md-option ng-repeat="language in languages" value="{{language}}">
                					{{language}}
              					</md-option>
           					</md-select>
          				</md-input-container>
    
  						
  
  						<md-input-container class="md-block">
          					<label>Script</label>
          					<textarea ng-model="shownFunction.script" md-maxlength="150" md-select-on-focus></textarea>
        				</md-input-container>
        				
        				
        				
        			</md-content>	
  						    					
				</md-tab>
				<md-tab label="Output">
					<md-content layout-padding>
  						<div>
  							Output 
  							<i class="fa fa-plus-square" ng-click="output=addOutputItem()" aria-hidden="true"></i>
  							<div ng-repeat="o in shownFunction.outputItems" layout-gt-sm="row" layout-align="start center">	      						
	      						<div>
									<md-button class="md-raised md-ExtraMini">Show Preview</md-button>
	      						</div>
	  							<md-input-container>
	        						<label>Label</label>
	        						<input ng-model="o.label">
	      						</md-input-container>
	      						
	      						<md-input-container aria-hidden="true">
	      							<label>Type</label>
	        						<md-select ng-model="o.type">
              							<md-option ng-repeat="type in outputTypes" value="{{type}}">
                							{{type}}
              							</md-option>
           							</md-select>
	      						</md-input-container>
	      						
	      						<div>
									<i class="fa fa-minus-square" ng-click="removeOutputFunc(o)" aria-hidden="true"></i>	
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
