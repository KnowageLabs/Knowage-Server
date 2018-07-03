<%@ page language="java" pageEncoding="utf-8" session="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="eventModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!-- angular imports -->
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@include file="/WEB-INF/jsp/commons/angular/eventLogsImport.jsp" %>

<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
</head>

<body ng-controller="eventController" class="bodyStyle kn-usersManagement">
<angular-list-detail show-detail="showDetail">
	<list label="Events">
<!-- 			show-search-bar="true" -->
<!-- 			search-function = "functionSearchEvents(searchValue, itemsPerPage, currentPageNumber , columnsSearch, columnOrdering, reverseOrdering)" -->
		<div layout="row">
	
		  <md-input-container flex>
		  <md-datepicker aria-label="startDate" ng-model="startDate">Start Date</md-datepicker>
		  </md-input-container>
		  <md-input-container flex>
		  <md-datepicker aria-label="endDate" ng-model="endDate">End Date</md-datepicker>
		  </md-input-container>
		  <md-input-container flex>
		  <md-select ng-model="type" placeholder="Select favorite type" flex>
		    <md-option ng-value="i" ng-repeat="i in eventSelectModel">{{ i }}</md-option>
		  </md-select>
		  </md-input-container>
		  <md-input-container flex>
		   <md-button ng-click="getQEvents()">
          <md-icon md-font-icon="fas fa-search" class="fa"></md-icon>
           </md-button>
		</md-input-container>
		</div>
		
		
		<angular-table

			flex
			id="events_id"
			ng-model="events"
			columns='[
						{"label":"User","name":"user"},
						{"label":"Date","name":"formattedDate"},
						{"label":"Type","name":"type"}
					]'

			highlights-selected-item=true
			click-function="loadDetail(item)">
		</angular-table>
	<!-- 			column-search='["user","type"]'		 -->
	</list>
	
	<detail>
			<md-input-container>
			  <label>User</label>
			  <input type="text" ng-model="selectedDetail.user">
			</md-input-container>
			
			<md-input-container>
			  <label>Date</label>
			  <input type="text" ng-model="selectedDetail.date">
			</md-input-container>
			
			<md-input-container>
			  <label>Type</label>
			  <input type="text" ng-model="selectedDetail.type">
			</md-input-container>
			
			<md-input-container>
			  <label>Description</label>
			  <input type="text" ng-model="selectedDetail.desc">
			</md-input-container>
	</detail>

</angular-list-detail>

</body>
</html>