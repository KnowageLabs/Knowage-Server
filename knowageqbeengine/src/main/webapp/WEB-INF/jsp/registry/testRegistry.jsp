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

  
  <%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
  <%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
  <%@include file="/WEB-INF/jsp/registry/registryImport.jsp"%>
  
<html>
<head></head>
<body ng-app="RegistryDocument" class="kn-registry">

	<rest-loading></rest-loading>
    <ng-include src="'<%=urlBuilder.getResourcePath(qbeEngineContext, "/js/src/registry/registry.tpl.html")%>'"></ng-include> 
	
</body>

</html>