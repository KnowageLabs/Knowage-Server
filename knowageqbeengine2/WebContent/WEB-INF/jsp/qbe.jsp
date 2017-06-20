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

<%@ page language="java"  contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>     
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/qbeImport.jsp"%>
<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>

<html ng-app="qbeManager">
	
	<head>
	
	
	</head>
	
	<body ng-controller="qbeController" class="kn-qbe">
	
	<angular-list-detail>
	 	<list>
		<qbe-expander-list flex drag-action="droppedFunction(data)" ng-model="model" entities-actions="entitiesFunctions" fields-actions="fieldsFunctions" colors="colors">
        </qbe-expander-list>
		</list>
		
		<detail>
			 <qbe-custom-table></qbe-custom-table>
		    <qbe-filter></qbe-filter>
		    <qbe-advanced-visulalization></qbe-advanced-visualization>
		</detail>
	</angular-list-detail>
    
   
    
	</body>

</html>
