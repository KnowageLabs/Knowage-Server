<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ taglib uri='http://java.sun.com/portlet' prefix='portlet'%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<portlet:defineObjects/>

<% String messageBunle = "component_kpi_messages"; %>

<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section-no-buttons'
			style='vertical-align: middle; padding-left: 5px;'>
			<spagobi:message key="sbi.kpi.kpiConfiguration.title" bundle="<%=messageBunle%>"/></td>
	</tr>
</table>


<div class="div_background">
<table>

  <tr class="portlet-font">
    <td width="100" align="center">
      <img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/kpi64.png", currTheme)%>' />
    </td>
    <td width="20">&nbsp;</td>
    <td vAlign="middle">
      <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="INIT_MANAGE_KPIS_GUI_ACTION"/></portlet:actionURL>'
         class="link_main_menu"> 
        <spagobi:message key="sbi.kpi.kpiDefinition.label" bundle="<%=messageBunle%>"/>
      </a>
    </td>
  </tr>

  <tr class="portlet-font">
    <td width="100" align="center">
      <img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/thresholds64.png", currTheme)%>' />
    </td>
	<td width="20">&nbsp;</td>
	<td vAlign="middle">
	  <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="INIT_MANAGE_THRESHOLDS_GUI_ACTION"/></portlet:actionURL>'
	    class="link_main_menu">
		<spagobi:message key="sbi.kpi.thresholdDefinition.label" bundle="<%=messageBunle%>" />
  	  </a>
    </td>
  </tr>

  <tr class="portlet-font">
		<td width="100" align="center"><img
			src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/model64.png", currTheme)%>' />
		</td>
		<td width="20">&nbsp;</td>
		<td vAlign="middle">
		  <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="INIT_MANAGE_MODELS_GUI_ACTION"/>
									  </portlet:actionURL>'
			class="link_main_menu">
			<spagobi:message key="sbi.kpi.modelDefinition.label" bundle="<%=messageBunle%>" />
		  </a>
		</td>
	</tr>
	
	  <tr class="portlet-font">
		<td width="100" align="center"><img
			src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/modelinst64.png", currTheme)%>' />
		</td>
		<td width="20">&nbsp;</td>
		<td vAlign="middle">
		  <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="INIT_MANAGE_MODEL_INSTANCES_GUI_ACTION"/>
									  </portlet:actionURL>'
			class="link_main_menu">
			<spagobi:message key="sbi.kpi.modelInstanceDefinition.label" bundle="<%=messageBunle%>" />
		  </a>
		</td>
	</tr>
	
	<tr class="portlet-font">
		<td width="100" align="center"><img
			src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/resources64.png", currTheme)%>' />
		</td>
		<td width="20">&nbsp;</td>
		<td vAlign="middle">
		  <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="INIT_MANAGE_RESOURCES_GUI_ACTION"/>
									  </portlet:actionURL>'
			class="link_main_menu">
			<spagobi:message key="sbi.kpi.resourcesDefinition.label" bundle="<%=messageBunle%>" />
		  </a>
		</td>
	</tr>
	
	<tr class="portlet-font">
		<td width="100" align="center"><img
			src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/alarms64.png", currTheme)%>' />
		</td>
		<td width="20">&nbsp;</td>
		<td vAlign="middle">
		  <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="INIT_MANAGE_ALARMS_GUI_ACTION"/>
									  </portlet:actionURL>'
			class="link_main_menu">
			<spagobi:message key="sbi.kpi.alarmsDefinition.label" bundle="<%=messageBunle%>" />
		  </a>
		</td>
	</tr>
	
	<tr class="portlet-font">
		<td width="100" align="center"><img
			src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/contacts64.png", currTheme)%>' />
		</td>
		<td width="20">&nbsp;</td>
		<td vAlign="middle">
		  <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="INIT_MANAGE_CONTACTS_GUI_ACTION"/>
									  </portlet:actionURL>'
			class="link_main_menu">
			<spagobi:message key="sbi.kpi.contactsDefinition.label" bundle="<%=messageBunle%>" />
		  </a>
		</td>
	</tr>
	
	<tr class="portlet-font">
		<td width="100" align="center"><img
			src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/kpi/grants64.png", currTheme)%>' />
		</td>
		<td width="20">&nbsp;</td>
		<td vAlign="middle">
		  <a href='<portlet:actionURL><portlet:param name="ACTION_NAME" value="MANAGE_OU_EMPTY_ACTION"/>
									  </portlet:actionURL>'
			class="link_main_menu">
			<spagobi:message key="sbi.kpi.grantsDefinition.label" bundle="<%=messageBunle%>" />
		  </a>
		</td>
	</tr>

</table>

</div>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
