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
