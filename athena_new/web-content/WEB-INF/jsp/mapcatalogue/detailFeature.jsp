<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page         import="it.eng.spago.base.*,
 				 it.eng.spago.configuration.ConfigSingleton,
 				 it.eng.spagobi.mapcatalogue.bo.GeoFeature,
 				 it.eng.spagobi.commons.dao.DAOFactory,
 				 it.eng.spago.navigation.LightNavigationManager,it.eng.spagobi.commons.utilities.PortletUtilities,
 				 java.util.Map,java.util.HashMap" %>
<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailFeatureModule"); 
	GeoFeature feature = (GeoFeature)moduleResponse.getAttribute("featureObj");
	String modality = (String)moduleResponse.getAttribute("modality");
	
	Map formUrlPars = new HashMap();
	formUrlPars.put("PAGE", "detailFeaturePage");
	formUrlPars.put("MESSAGEDET", modality);
	formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
	String formUrl = urlBuilder.getUrl(request, formUrlPars);
	
	Map backUrlPars = new HashMap();
//	backUrlPars.put("PAGE", "detailFeaturePage");
	backUrlPars.put("PAGE", "ListFeaturesPage");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
	
	//String msgWarningSave = PortletUtilities.getMessage("5029", "component_mapcatalogue_messages");
	String msgWarningSave=msgBuilder.getMessage("5029", "component_mapcatalogue_messages",  request);
%>

<form method='POST' action='<%=formUrl%>' id='featureForm' name='featureForm'>
<input type='hidden' value='<%=feature.getFeatureId()%>' name='ID' />

<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBIMapCatalogue.detailFeature" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<input type='image' name='save' id='save' onClick="javascript:saveMap('SAVE')" value='true' class='header-button-image-portlet-section'
				       src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
      				   title='<spagobi:message key = "SBIMapCatalogue.saveButton" bundle="component_mapcatalogue_messages" />'  
                       alt='<spagobi:message key = "SBIMapCatalogue.saveButton" bundle="component_mapcatalogue_messages"/>' 
			   />
			   <!-- 
			<a href="javascript:document.getElementById('featureForm').submit()"> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBIMapCatalogue.saveButton" />' src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' alt='<spagobi:message key = "SBIMapCatalogue.saveButton" bundle="component_mapcatalogue_messages"/>' /> 
			</a>
			-->
		</td>
		<td class='header-button-column-portlet-section'>
			<a  href='javascript:goBack("<%=msgWarningSave%>", "<%=backUrl%>")'> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBIMapCatalogue.backButton" />' src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' alt='<spagobi:message key = "SBIMapCatalogue.backButton" bundle="component_mapcatalogue_messages"/>' />
			</a>
		</td>
	</tr>
</table>
<div class='div_background' style='padding-top:5px;padding-left:5px;'>
<table width="100%" cellspacing="0" border="0" id = "fieldsTable" >
<tr>
  <td>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIMapCatalogue.columnName" />
		</span>
	</div>
	<%String sReadonly = "";
	  if (modality.equalsIgnoreCase("DETAIL_MOD")){
	  		sReadonly = "readonly";
	  }
	  String name = feature.getName();
	   if((name==null) || (name.equalsIgnoreCase("null"))  ) {
		   name = "";
	   } %>
	<div class='div_detail_form'>
		<input class='portlet-form-input-field' type="text" <%=sReadonly %>
			   name="NAME" size="50" value="<%=StringEscapeUtils.escapeHtml(name)%>" maxlength="45">
		&nbsp;*
	</div>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>	
			<spagobi:message key = "SBIMapCatalogue.columnDescr" />
		</span>
	</div>
	<div class='div_detail_form'>
	<% String desc = feature.getDescr();
	   if((desc==null) || (desc.equalsIgnoreCase("null"))  ) {
	   	   desc = "";
	   } 
	%>
		<input class='portlet-form-input-field' type="text" name="DESCR" 
			   size="50" value="<%= StringEscapeUtils.escapeHtml(desc) %>" maxlength="130">
	</div>
	
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIMapCatalogue.columnType" />
		</span>	
	</div>
	<div class='div_detail_form'>
	<% String type = feature.getType();
	   if((type==null) || (type.equalsIgnoreCase("null"))  ) {
		   type = "";
	   } 
	%>

		<select class='portlet-form-input-field' name="TYPE" >
			<option value="" <% if (type.equalsIgnoreCase("")) out.print(" selected='selected' ");  %>>&nbsp;</option>
			<option value=<spagobi:message key="SBIMapCatalogue.TypeTerritorial" bundle="component_mapcatalogue_messages" /> <% if (type.equalsIgnoreCase(msgBuilder.getMessage("SBIMapCatalogue.TypeTerritorial", "component_mapcatalogue_messages",  request))) out.print(" selected='selected' ");  %>><spagobi:message key="SBIMapCatalogue.TypeTerritorial" bundle="component_mapcatalogue_messages" /></option>
			<option value=<spagobi:message key="SBIMapCatalogue.TypePositional" bundle="component_mapcatalogue_messages" />  <% if (type.equalsIgnoreCase(msgBuilder.getMessage("SBIMapCatalogue.TypePositional", "component_mapcatalogue_messages",  request)))  out.print(" selected='selected' ");  %>><spagobi:message key="SBIMapCatalogue.TypePositional" bundle="component_mapcatalogue_messages" /></option>
		</select>
	&nbsp;*
	</div>


	</td><!-- CLOSE COLUMN WITH DATA FORM  -->
	<spagobi:error/>
</tr>
</table>   <!-- CLOSE TABLE FORM ON LEFT AND VERSION ON RIGHT  -->

 </div> <!-- background -->


</form>

<script>
	function isBIFeatureFormChanged () {
	
	var biFeatureFormModified = 'false';
		
	var name = document.featureForm.NAME.value;
	var description = document.featureForm.DESCR.value;	
	var type = document.featureForm.TYPE.value;

	if ((name != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(feature.getName()))%>')
		|| (description != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(feature.getDescr()))%>')
		|| (type != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(feature.getType()))%>')) {
			
		biFeatureFormModified = 'true';
	}
	
	return biFeatureFormModified;
	
}

	
	function goBack(message, url) {
	  
	  var featureFormModified = isBIFeatureFormChanged();
	  if (featureFormModified == 'true'){
	  	  if (confirm(message)) {
	  	      document.getElementById('save').click(); 
	  	  }	  
	  }
	  location.href = url;

	}

</script>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
