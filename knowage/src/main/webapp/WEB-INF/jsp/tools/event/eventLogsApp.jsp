<%@ page language="java" pageEncoding="UTF-8" session="true"%>
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
	
	<body ng-controller="eventController" class="bodyStyle kn-EventManagement">
		<angular-list-detail show-detail="showDetail">
			<list label="Events">
				<div layout="row" layout-align="start center">
			  		<md-datepicker flex ng-model="startDate">Start Date</md-datepicker>
				  	<md-datepicker flex ng-model="endDate">End Date</md-datepicker>
				  	<md-input-container flex>
				  		<md-select ng-model="type" placeholder="Select favorite type" flex>
				    		<md-option ng-value="i" ng-repeat="i in eventSelectModel">{{ i }}</md-option>
				  		</md-select>
				  	</md-input-container>
			   		<md-button ng-click="searchEvents()" class="md-icon-button">
		       			<md-icon md-font-icon="fa fa-search"></md-icon>
		       		</md-button>
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
					click-function="loadDetail(item)"
					no-pagination=false
					current-page-number=currentPageNum
					total-item-count=totalItemCountt
					page-changed-function=pageChangedFun(itemsPerPage,currentPageNumber)
					>
				</angular-table>
				<!--  div ag-grid="eventsGrid" class="ag-theme-balham noMargin ag-theme-knowage" style="height:500px;width:100%;"></div-->
			</list>
			
			<detail>
			
				<md-card layout-padding>
					<md-card-content layout="column">
						<md-input-container >
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
					</md-card-content>
				</md-card>
			</detail>
		
		</angular-list-detail>
	
	</body>
</html>