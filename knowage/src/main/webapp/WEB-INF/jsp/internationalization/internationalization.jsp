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

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
 
<%--------- Java Imports  --------%>
 <%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>
 <%@page import="it.eng.spago.security.IEngUserProfile"%>
 
 <%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
 <%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
 <%@include file="/WEB-INF/jsp/internationalization/internationalizationImport.jsp"%>
   
<%--------- Java Code  --------%>
  <%
	boolean isTechnicalUser = UserUtilities.isTechnicalUser(userProfile);
  %>
   
   <script>
		var isTechnicalUser = <%= isTechnicalUser %>
   </script>
        
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="InternationalizationModule">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Internationalization</title>
</head>
<body ng-controller="internationalizationController">	
	<rest-loading></rest-loading>
	<div>
		<md-content>
			<md-tabs md-dynamic-height md-border-bottom md-selected="selectedTab">
				<md-tab ng-repeat="language in availableLanguages | orderBy: 'defaultLanguage'" md-on-select="getMessages(language)">
					<md-tab-label>
						{{language.language}}
						<span ng-if="language.defaultLanguage">{{translate.load("kn.internationalization.default")}}</span>						
					</md-tab-label>
					<md-tab-body class="md-padding">
						<div class="in-tab-content">
							<div>
								<md-input-container class="in-search-field">
									<label> {{translate.load("kn.internationalization.search")}} </label>
									<input type="text" aria-label="data change" ng-model="searchText">
								</md-input-container>
								<md-checkbox ng-model="emptyMessage.value" ng-change="toggleEmptyMessages()" aria-label="Find Empty Fields">
									{{translate.load("kn.internationalization.checkbox.showBlankMessages")}}
								</md-checkbox> 
							</div>					
						  	<table class="kn-table">
						  		<thead>
							  		<tr>
							  			<th>{{translate.load("kn.internationalization.table.label")}}</th>
							  			<th ng-if="!language.defaultLanguage">{{translate.load("kn.internationalization.table.defaultMessageCode")}}</th>							  			
							  			<th>{{translate.load("kn.internationalization.table.messageCode")}}</th>
							 			<th ng-if="language.defaultLanguage && isTechnicalUser" class="in-add-label-column">
							 				<md-button class="" ng-click="addLabel()">
                               					{{translate.load("kn.internationalization.table.addLabel")}}
                          					</md-button>
							 			</th>
							 			<th ng-if="language.defaultLanguage && !isTechnicalUser">&nbsp</th>
							 			<th ng-if="!language.defaultLanguage">&nbsp</th>
							  		</tr>
						  		</thead>
						  		<tbody>
						  			<tr ng-repeat="mess in messages | filter:searchText">
						  				<td ng-if="language.defaultLanguage && isTechnicalUser">
						  					<div>
						  						<md-input-container>
						  							<input type="text" ng-model="mess.label" aria-label="data change">
						  						</md-input-container>
						  					</div>						  					
						  				</td>
						  				<td ng-if="language.defaultLanguage && !isTechnicalUser">
						  					<div>
						  						<md-input-container>
						  							<input type="text" ng-model="mess.label" aria-label="data change" disabled>
						  						</md-input-container>
						  					</div>						  					
						  				</td>
						  				<td ng-if="!language.defaultLanguage">
						  					<div>
						  						<md-input-container>
						  							<input type="text" ng-model="mess.label" aria-label="data change" disabled>
						  						</md-input-container>
						  					</div>						  					
						  				</td>
						  				<td ng-if="!language.defaultLanguage">
						  					<div>
						  						<md-input-container>
						  							<input type="text" ng-model="mess.defaultMessageCode" aria-label="data change" disabled>
						  						</md-input-container>
						  					</div>
						  				</td>							  				
						  				<td>
						  					<div>
						  						<md-input-container>
						  							<input type="text" ng-model="mess.message" aria-label="data change">
						  						</md-input-container>
						  					</div>
						  				</td>
						  				<td class="in-add-label-column">
						  					<div>
						  						<div class="in-btns saveBtn">
						  							<md-tooltip>{{translate.load("kn.internationalization.table.tooltip.save")}}</md-tooltip>
						  							<md-icon ng-click="saveLabel(language, mess)" md-font-icon="fa fa-save fa-2x"></md-icon>
						  						</div>
						  						<div class="in-btns">
						  							<md-tooltip>{{translate.load("kn.internationalization.table.tooltip.delete")}}</md-tooltip>
						  							<md-icon md-font-icon="fa fa-trash fa-2x" ng-click="deleteLabel(language, mess, $event)"></md-icon>
						  						</div>						  						
						  					</div>
						  				</td>
						  			</tr>
						  		</tbody>
						  	</table>																											
						</div>
					</md-tab-body>					
				</md-tab>				
			</md-tabs>
		</md-content>		
	</div>

</body>
</html>