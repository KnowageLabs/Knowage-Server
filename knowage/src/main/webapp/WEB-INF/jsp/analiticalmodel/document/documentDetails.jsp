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
    
<%@ page contentType="text/html; charset=UTF-8" %>

	<html>
		<head>
			<meta charset="UTF-8">
			
			<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf" %>
			<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
			<%@include file="/WEB-INF/jsp/analiticalmodel/document/documentDetailsImport.jsp" %>

		</head>
		<body ng-app="DocumentDetails" class="kn-documentDetails">
	
			<rest-loading></rest-loading>
			<div class="kn-documentDetails" ng-controller="DocumentDetailsController as ddc" >
			  	<form name = "documentDetailsForm" >
			    <md-toolbar class="primaryToolbar">
			        <div class="md-toolbar-tools" layout="row">
			            <h2>{{ddc.title}}</h2>
			            <span flex></span>			          
			            <md-button ng-disabled="documentDetailsForm.$invalid || ddc.noSelectedFunctionalities()" ng-click="ddc.savingFunction()">Save</md-button>
			              <md-button  ng-click="ddc.cancelFunction()">Cancel</md-button>
			        </div>
			    </md-toolbar>
			    <md-content ng-cloak class="tabContainer">
			        <md-tabs md-border-bottom layout-fill>	
			       
			        		        
			            <md-tab>
			            	<md-tab-label>			            	
			            		<div ng-class="documentDetailsForm.informations.$invalid ? 'kn-dangerous':''">Informations</div>			            	 
			            	</md-tab-label>
							<md-tab-body>
			                	<div ng-include="'<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/templates/informations.html")%>'" ></div>
			                 </md-tab-body>
			            </md-tab>		
			            	          		              
			            <md-tab ng-if="ddc.docId">
			            	<md-tab-label>
			            		<div ng-class="documentDetailsForm.drivers.$invalid ? 'kn-dangerous':''">Drivers</div>
			           		 </md-tab-label>
			           		 <md-tab-body>
			           		 	<div ng-include="'<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/templates/drivers.html")%>'" ></div>
			           		 </md-tab-body>
			            </md-tab>
			            
			            <md-tab ng-if="ddc.docId">
			            	<md-tab-label>
			            		<div ng-class="documentDetailsForm.outputparameters.$invalid ? 'kn-dangerous':''">Output Parameters</div>
			            	</md-tab-label>
			            	<md-tab-body>
			            		<div ng-include="'<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/templates/outputParameters.html")%>'" ></div>			            
			                </md-tab-body>
			            </md-tab>			            
			            
			            <md-tab ng-if="ddc.docId">
				            <md-tab-label>
				            	<div ng-class="documentDetailsForm.datalineage.$invalid ? 'kn-dangerous':''">Data Lineage</div>
				            </md-tab-label>
				            <md-tab-body>
				            	<div ng-include="'<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/templates/dataLineage.html")%>'" ></div>		    				                	
			                </md-tab-body>		                
			            </md-tab>		
			                        
			            <md-tab ng-if="ddc.docId">
			            	<md-tab-label>
			            		<div ng-class="documentDetailsForm.templates.$invalid ? 'kn-dangerous':''">History</div>
			            	</md-tab-label>
			            	<md-tab-body>
			            		<div ng-include="'<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/templates/templates.html")%>'" ></div>				                	
			                </md-tab-body>
			            </md-tab>

			            <md-tab ng-if="ddc.docId && ddc.typeCode=='REPORT' && ddc.engine=='knowagejasperreporte'" ng-click="ddc.getAllDocuments()">
			            	<md-tab-label>
			            		<div ng-class="documentDetailsForm.subreports.$invalid ? 'kn-dangerous':''">Subreports</div>
			            	</md-tab-label>
			            	<md-tab-body>
			            		<div ng-include="'<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/templates/subreports.html")%>'" ></div>			            
			                </md-tab-body>
			            </md-tab>
			            
			        </md-tabs>
			    </md-content>
			</form>	
			</div>
		
		</body>
	</html>