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


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ page import="java.util.Map,java.util.HashMap"%>
<%@page import="java.util.List"%>

<%
String title = "";

ConfigSingleton configure = ConfigSingleton.getInstance();
SourceBean moduleBean = (SourceBean) configure
		.getFilteredSourceBeanAttribute("MODULES.MODULE", "NAME",
				"ListModelTreeModule");

if (moduleBean.getAttribute("CONFIG.TITLE") != null)
	title = (String) moduleBean.getAttribute("CONFIG.TITLE");

  Map backUrlPars = new HashMap();
  backUrlPars.put("PAGE", "ModelPage");
  String backUrl = urlBuilder.getUrl(request, backUrlPars);
  String messageBundle = "component_kpi_messages";
%>
<table class='header-table-portlet-section'>
  <tr class='header-row-portlet-section'>
    <td class='header-title-column-portlet-section'
      style='vertical-align: middle; padding-left: 5px;'>
      <spagobi:message key="sbi.kpi.list.model.tree.title" bundle="<%=messageBundle%>"/></td>
    <td class='header-empty-column-portlet-section'>&nbsp;</td>
    <td class='header-button-column-portlet-section'><a
      href='<%=backUrl%>'> <img
      class='header-button-image-portlet-section'
      title='<spagobi:message key="sbi.kpi.button.back.title" bundle="<%=messageBundle%>" />'
      src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>'
      alt='<spagobi:message key = "sbi.kpi.button.back.title" bundle="<%=messageBundle%>" />' /> </a></td>
  </tr>
</table>

	<spagobi:treeObjects moduleName="ListModelTreeModule"
		htmlGeneratorClass="it.eng.spagobi.kpi.model.presentation.ModelStructureTreeHtmlGenerator" />
  		
<spagobi:error />

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
