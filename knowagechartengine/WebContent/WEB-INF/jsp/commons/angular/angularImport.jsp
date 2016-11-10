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
author: Danilo Ristovski (danristo, danilo.ristovski@mht.net)
--%>

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">

<!-- Font awesome CSS for fancy icons. -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css">

<!-- angular reference-->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/js/bower_components/angular/angular.js"></script>

<!-- angular-material-->
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/js/bower_components/angular-material_1.0.9/angular-material.min.js"></script>
<link rel="stylesheet" type="text/css" href="<%= GeneralUtilities.getSpagoBiContext() %>/js/bower_components/angular-material_1.0.9/angular-material.css">
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/js/bower_components/angular-animate/angular-animate.min.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/js/bower_components/angular-aria/angular-aria.min.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/js/bower_components/angular-sanitize/angular-sanitize.min.js"></script>
<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/js/bower_components/angular-messages/angular-messages.min.js"></script>





<!-- toastr -->
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/sbi_default/css/angular-toastr.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular/commons/angular-toastr.tpls.js"></script>

<!-- 
	Specifying all Angular services that we are eventually going to use (inject into the controller that is created for the 
	execution of the ChartEngine). For example, services such as sbiModule_translate, sbiModule_messaging
	@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 -->
<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>