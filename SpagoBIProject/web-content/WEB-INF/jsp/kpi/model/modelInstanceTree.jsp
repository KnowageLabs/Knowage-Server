<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ page import="java.util.Map,java.util.HashMap"%>
<%@page import="java.util.List"%>

<%

String title = "";

ConfigSingleton configure = ConfigSingleton.getInstance();
SourceBean moduleBean = (SourceBean) configure
		.getFilteredSourceBeanAttribute("MODULES.MODULE", "NAME",
				"ListModelInstanceTreeModule");

if (moduleBean.getAttribute("CONFIG.TITLE") != null)
	title = (String) moduleBean.getAttribute("CONFIG.TITLE");

  Map backUrlPars = new HashMap();
  backUrlPars.put("PAGE", "ModelInstancePage");
  String backUrl = urlBuilder.getUrl(request, backUrlPars);
  String messageBundle = "component_kpi_messages";
%>
<table class='header-table-portlet-section'>
  <tr class='header-row-portlet-section'>
    <td class='header-title-column-portlet-section'
      style='vertical-align: middle; padding-left: 5px;'>
      <spagobi:message key='<%=title%>' bundle='<%=messageBundle%>' />
      </td>
    <td class='header-empty-column-portlet-section'>&nbsp;</td>
    <td class='header-button-column-portlet-section'>
 			<a href='#' id="openInfo"> 
				<img class='header-button-image-portlet-section'
				src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/info22.png", currTheme)%>' 
				title='<spagobi:message key = "sbi.kpi.button.info.title" bundle="<%=messageBundle%>" />'
				alt='<spagobi:message key = "sbi.kpi.button.info.title" bundle="<%=messageBundle%>"/>'
				/> 
			</a>
	</td>
    <td class='header-button-column-portlet-section'><a
      href='<%=backUrl%>'> <img
      class='header-button-image-portlet-section'
      title='<spagobi:message key="sbi.kpi.button.back.title" bundle="<%=messageBundle%>" />'
      src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>'
      alt='<spagobi:message key = "sbi.kpi.button.back.title" bundle="<%=messageBundle%>" />' /> </a></td>
  </tr>
</table>

	<spagobi:treeObjects moduleName="ListModelInstanceTreeModule"
		htmlGeneratorClass="it.eng.spagobi.kpi.model.presentation.ModelInstanceStructureTreeHtmlGenerator" />
  		
<spagobi:error />
<spagobi:infoTag fileName="modelInstinfo" infoTitle="Model Instance Informations" buttonId="openInfo"/>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>