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


<!-- openlayer import -->

<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/openlayers/3.x.x/ol.js")%>"></script> 


<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/openlayers/3.x.x/ol.css")%>" type="text/css">



<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script>
   google.load('visualization', '1.0', {'packages':['corechart', 'bar']});
</script>
	    
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/mapFilter/utils/Ellipsoid.js")%>"></script>


<!-- geo-map import -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/mapFilter/geoModule.js")%>"></script>

<script>
var sbiModule=angular.module('sbiModule');
sbiModule.factory('sbiModule_config',function(){
	return {
		protocol: '<%= request.getScheme()%>' ,
		host: '<%= request.getServerName()%>',
	    port: '<%= request.getServerPort()%>',
	    contextName: '/<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>',
	    <%-- 
	    controllerPath: null ,// no cotroller just servlets   
	    docLabel :"<%=docLabel%>", 
		docVersion : "<%=docVersion%>",
	 	docName :"<%=docName.replace('\n', ' ')%>",
	 	docDescription: "<%=docDescription.replace('\n', ' ')%>",
	 	docIsPublic: "<%=docIsPublic%>",
	 	docIsVisible: "<%=docIsVisible%>",
	 	docPreviewFile: "<%=docPreviewFile%>",
	 	docCommunities: "<%=docCommunity%>",
	 	docFunctionalities: "<%=docFunctionalities%>",
	 	docDatasetLabel: "<%=docDatasetLabel%>",
	 	docDatasetName: "<%=docDatasetName%>",
	 	visibleDataSet: "<%=visibleDataSet%>",
	 	userId : "<%=userId%>",
	 	docAuthor :"<%=docAuthor%>",
	    --%>
	 	externalBasePath:"<%=KnowageSystemConfiguration.getKnowageContext()%>/"
	};
});
</script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/mapFilter/utils/geoUtils.js")%>"></script>

<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "themes/sbi_default/css/mapFilter/mapFilter.css")%>" type="text/css">

