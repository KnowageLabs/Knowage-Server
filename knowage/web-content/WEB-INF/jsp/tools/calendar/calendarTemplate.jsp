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


<%@ page language="java" pageEncoding="utf-8" session="true"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<%
	IEngUserProfile profile = (IEngUserProfile) request.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	boolean canSee = false;
	
	if (UserUtilities.haveRoleAndAuthorization(profile, null, new String[] { SpagoBIConstants.MANAGE_CALENDAR })
			|| UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[0])) {
		canSee = true;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="calendar">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/calendar/calendarStyle.css")%>">

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/calendar/calendarController.js")%>"></script>

</head>


<body class="bodyStyle">
	<angular-list-detail ng-controller="Controller"
		 full-screen=true>
	<list label='translate.load("sbi.calendar")' <%= canSee? "new-function='newCalendar'":"" %>> 
	<angular-table flex
		id='layerlist' ng-model=calendarList
		columns='[{"label":"Name","name":"calendar"},{"label":"Start Date","name":"dateStartToShow"},{"label":"End Date","name":"dateEndToShow"},{"label":"type","name":"calType"}]'
		columnsSearch='["calendar"]' show-search-bar=true
		highlights-selected-item=true click-function="loadCalendar(item);"
		scope-functions=tableFunction  <%= canSee? "speed-menu-option=calendarTable ":"" %> > </angular-table>

	</list> 
	<detail label='selectCalendar.calendar==undefined? "New Calendar*" : selectCalendar.calendar' cancel-function="cancel" <%= canSee? "save-function='saveCalendar'":"" %>>
		<!-- if is a new calendar to add -->
		<md-whiteframe class="md-whiteframe-4dp layout-padding" layout-margin>
				<div>
						<div>
							<md-input-container class="small counter" class="small counter">
							<label>{{translate.load("sbi.behavioural.lov.details.name")}}</label>
							<input class="input_class DisableColor" ng-model="selectCalendar.calendar" required 
								maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="selectCalendar.calendarId!=undefined"> </md-input-container>
						</div>
						<div>
							<md-input-container class="small counter" class="small counter">
							<label>{{translate.load("sbi.udp.type")}}</label>
							<input class="input_class DisableColor" ng-model="selectCalendar.calType" 
								maxlength="30" ng-maxlength="30" md-maxlength="30"   ng-disabled="selectCalendar.calendarId!=undefined"> </md-input-container>
						</div>
						<div>
							<label>{{translate.load("sbi.target.startvalidity")}}</label>
							<md-datepicker ng-model="selectCalendar.calStartDay" name="Select Data"
								 md-placeholder={{translate.load("sbi.target.selectdate");}}   ng-disabled="selectCalendar.calendarId!=undefined"></md-datepicker>
							<label>{{translate.load("sbi.target.endvalidity")}}</label>
							<md-datepicker ng-model="selectCalendar.calEndDay" name="Select Data"
								 md-placeholder={{translate.load("sbi.target.selectdate");}}  ng-disabled="selectCalendar.calendarId!=undefined"></md-datepicker>
						</div>
				</div>
				<div layout="row"> 
					<span flex></span>
					<div layout="row" layout-sm="column" id="preview" layout-align="space-around" ng-show="showCircularGenera">
				     	<md-progress-circular md-mode="indeterminate" ></md-progress-circular>
				 	</div>
					<md-button <%= canSee? "ng-click='generate()'":"" %> ng-disabled ="selectCalendar.realDateGenerated.length>0 || disableGenera" ng-show="!showCircularGenera && selectCalendar.calendarId!=undefined ">Genera</md-button>
				</div>
				<div  <%= canSee? "style='display:none'":"" %> style="position:absolute; z-index:1000;background:transparent;" layout-fill>
				</div>
				</md-whiteframe>
				<md-whiteframe ng-show="selectCalendar.realDateGenerated.length>0"  class="md-whiteframe-4dp layout-padding" layout-margin>
				<angular-table flex ng-show="selectCalendar.realDateGenerated.length>0" layout-fill 
					id='dayslist' ng-model=selectCalendar.realDateGenerated
					columns=columns
					highlights-selected-item=true click-function=""
					scope-functions=tableFunction <%= canSee? "speed-menu-option=measureMenuOption ":"" %>
					current-page-number = "tablePage" initial-sorting="'date'"> </angular-table>
					
			</md-whiteframe>
				


	</detail> </angular-list-detail>

</body>
</html>
