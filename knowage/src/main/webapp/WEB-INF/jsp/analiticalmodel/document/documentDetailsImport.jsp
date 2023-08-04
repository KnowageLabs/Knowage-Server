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

<%@page import="it.eng.spagobi.engines.config.dao.IEngineDAO"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.commons.dao.IDomainDAO"%>
<%@page import="it.eng.spagobi.services.serialization.JsonConverter"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO"%>
<%@page import="it.eng.spagobi.commons.utilities.SpagoBIUtilities"%>
<%@page import="it.eng.spagobi.tools.dataset.dao.IDataSetDAO"%>
<%@page import="it.eng.spagobi.tools.dataset.bo.IDataSet"%>
<%@page import="it.eng.spagobi.tools.datasource.bo.IDataSource"%>
<%@page import="it.eng.spagobi.tools.datasource.dao.IDataSourceDAO"%>
<%@page import="it.eng.spagobi.user.UserProfileManager"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.knowage.security.ProductProfiler"%>


<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/resourceService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/documentDetails.controller.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/modules/driversModule.js")%>"></script>

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/controllers/drivers.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/controllers/templates.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/controllers/outputParameters.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/controllers/informations.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/controllers/dataLineage.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/controllers/subreports.js")%>"></script>



<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/documentDataService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/interceptors/httpInterceptor.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/closingIFrame.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/templateService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/outputParametersService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/dataLineageService.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/src/angular_1.4/tools/documentdetails/services/subreportsService.js")%>"></script>




<% 
String resourcePath = SpagoBIUtilities.getResourcePath();
String folderId = request.getParameter(SpagoBIConstants.FUNCTIONALITY_ID);
String documentNumber = request.getParameter(SpagoBIConstants.OBJECT_ID);
Integer documentId = null;
if(documentNumber != null){
documentId = Integer.parseInt(documentNumber);
}%>

<script>
var documentAndInfo = {}
</script>

<%
	//domain and engine informations
IDomainDAO domainDao = DAOFactory.getDomainDAO();
IEngineDAO engineDao = DAOFactory.getEngineDAO();
IBIObjectParameterDAO driversDao = DAOFactory.getBIObjectParameterDAO();
IBIObjectDAO documentDao = DAOFactory.getBIObjectDAO();
IParameterDAO analyticalDriversDao = DAOFactory.getParameterDAO();
IDataSetDAO datasetDAO = DAOFactory.getDataSetDAO();
IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
List types =  ProductProfiler.filterDocumentTypesByProduct(domainDao.loadListDomainsByTypeAndTenant("BIOBJ_TYPE"));
List engines =  ProductProfiler.filterEnginesByProduct(engineDao.loadAllEnginesByTenant());
List states = domainDao.loadListDomainsByType("STATE");
List<Parameter> analyticalDrivers = analyticalDriversDao.loadAllParameters();

List<IDataSource> datasources = dataSourceDAO.loadAllDataSources();

String JSONengines = JsonConverter.objectToJson(engines, engines.getClass());
String JSONtypes = JsonConverter.objectToJson(types, types.getClass());
String JSONstates = JsonConverter.objectToJson(states, states.getClass());
String JSONanalyticalDrivers = JsonConverter.objectToJson(analyticalDrivers, analyticalDrivers.getClass());
String JSONdatasources = JsonConverter.objectToJson(datasources, datasources.getClass());

int idDriver;
	//Informations about document if u are changing existing
if(documentNumber != null){
	
	List<BIObjectParameter> drivers  = driversDao.loadBIObjectParametersById(documentId);
	BIObject document = documentDao.loadBIObjectById(documentId);
	ObjTemplate documentTemplate = document.getActiveTemplate();
	String JSONdocument = JsonConverter.objectToJson(document, document.getClass());
	String JSONdrivers = JsonConverter.objectToJson(drivers, drivers.getClass());

	if(documentTemplate != null){
	String JSONtemplate = JsonConverter.objectToJson(documentTemplate, documentTemplate.getClass());%>
	<script>documentAndInfo.template = <%= JSONtemplate %></script>
	<%}%>
	<script>
	documentAndInfo.document = <%= JSONdocument %>
	documentAndInfo.drivers = <%= JSONdrivers %>
	
	</script>
<% 

}
%>
<script>
	 documentAndInfo.resourcePath ='<%= resourcePath %>'
	 documentAndInfo.folderId = <%= folderId %>
	 documentAndInfo.types = <%= JSONtypes %>
<%-- 	 documentAndInfo.datasets = <%= JSONdatasets %> --%>
	 documentAndInfo.datasources = <%= JSONdatasources %>
	 documentAndInfo.engines = <%= JSONengines %>
	 documentAndInfo.states = <%= JSONstates %>
	 documentAndInfo.analyticalDrivers =<%= JSONanalyticalDrivers %>
</script>

