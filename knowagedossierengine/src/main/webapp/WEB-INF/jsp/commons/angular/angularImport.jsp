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

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">

<link rel="stylesheet" href="<%=request.getContextPath()%>/themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css">

<!-- angular reference-->
<!-- START-DEBUG -->
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular_1.x/angular.js"></script> 
<!-- END-DEBUG -->

<!-- START-PRODUCTION 
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular_1.x/angular.min.js"></script> 
END-PRODUCTION -->

<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular_1.x/angular-animate.min.js"></script> 
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular_1.x/angular-aria.min.js"></script> 
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular_1.x/angular-sanitize.min.js"></script> 
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular_1.x/angular-messages.min.js"></script> 
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular_1.x/angular-cookies.js"></script> 

<!-- angular-material-->
<link rel="stylesheet" href="<%=request.getContextPath()%>/js/lib/angular-material_1.1.0/angular-material.min.css">
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular-material_1.1.0/angular-material.min.js"></script> 

<!--pagination-->
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/pagination/dirPagination.js"></script>

<!-- angular tree -->
<link rel="stylesheet" 	href="<%=request.getContextPath()%>/js/lib/angular-tree/angular-ui-tree.min.css">
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/angular-tree/angular-ui-tree.js"></script> 

<!-- context menu -->
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/contextmenu/ng-context-menu.js"></script>  

<!-- angular table -->
<script type="text/javascript" src="<%=request.getContextPath()%>/js/src/angular-table/AngularTable.js"></script> 
<script type="text/javascript" src="<%=request.getContextPath()%>/js/src/angular-table/utils/daff.js"></script> 
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/js/src/angular-table/AngularTable.css">
 		

<!-- IE Patch (official polyfill provided by https://developer.mozilla.org )-->
<script type="text/javascript">
if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(searchString, position){
      position = position || 0;
      return this.substr(position, searchString.length) === searchString;
  };
}
if (!String.prototype.endsWith) {
	String.prototype.endsWith = function(searchString, position) {
		var subjectString = this.toString();
		if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
			position = subjectString.length;
		}
		position -= searchString.length;
		var lastIndex = subjectString.lastIndexOf(searchString, position);
		return lastIndex !== -1 && lastIndex === position;
	};
}
</script>

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>
