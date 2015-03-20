<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
	
	<%@ page         import="it.eng.spago.base.*,
	 				         it.eng.spago.configuration.ConfigSingleton,
	 				         it.eng.spagobi.mapcatalogue.bo.GeoMap,
	 				         it.eng.spagobi.mapcatalogue.bo.GeoFeature,
	 				         it.eng.spagobi.commons.dao.DAOFactory,
	 				         it.eng.spago.navigation.LightNavigationManager,
	 				         it.eng.spagobi.commons.constants.SpagoBIConstants,
	 				         java.util.Map,java.util.HashMap,java.util.List" %>
	 				         
	<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
	
	<%
		SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailMapModule"); 
		GeoMap map = (GeoMap)moduleResponse.getAttribute("mapObj");
		String modality = (String)moduleResponse.getAttribute("modality");
		String selectedFeatureId = (moduleResponse.getAttribute("selectedFeatureId")==null)?"":(String)moduleResponse.getAttribute("selectedFeatureId");
		String subMessageDet = ((String)moduleResponse.getAttribute("SUBMESSAGEDET")==null)?"":(String)moduleResponse.getAttribute("SUBMESSAGEDET");
		//System.out.println("subMessageDet: " + subMessageDet);
		
		Map formUrlPars = new HashMap();
		//if(ChannelUtilities.isPortletRunning()) {
		//	formUrlPars.put("PAGE", "DetailMapPage");	
  		//	formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");	
		//}
		String formUrl = urlBuilder.getUrl(request, formUrlPars);
		
		Map backUrlPars = new HashMap();
		//backUrlPars.put("PAGE", "detailMapPage");
		backUrlPars.put("PAGE", "ListMapsPage");
		backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
		String backUrl = urlBuilder.getUrl(request, backUrlPars);
	
		Map lookupFeatureUrlPars = new HashMap();
		lookupFeatureUrlPars.put("PAGE", "FeaturesLookupPage");
		lookupFeatureUrlPars.put("MESSAGEDET", modality);
		lookupFeatureUrlPars.put("MAP_ID", new Integer(map.getMapId()));
		lookupFeatureUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
		String lookupFeraturesUrl = urlBuilder.getUrl(request, lookupFeatureUrlPars);	
		
		String downloadUrl = GeneralUtilities.getSpagoBIProfileBaseUrl(userUniqueIdentifier) +"&PAGE=DetailMapPage&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";
				
   	    downloadUrl += "&MESSAGEDET=DOWNLOAD_MAP&BIN_ID="+  map.getBinId();
	   
		//checks if there are some features that will be erased (if user wants)
		List lstFeaturesOld = (List)moduleResponse.getAttribute("lstFeaturesOld");	
		String msgDelete = "";
		String msgDelFeatures = "false";
		if (lstFeaturesOld != null && lstFeaturesOld.size() > 0){
			msgDelete = msgBuilder.getMessage("5025", "component_mapcatalogue_messages", request);
			for (int i=0; i<lstFeaturesOld.size(); i++){
				msgDelete = msgDelete + " " + ((String)lstFeaturesOld.get(i)).toUpperCase() + ((i < lstFeaturesOld.size()-1)? ", ":"");		        
			}
			msgDelete = msgDelete + " ?";
			msgDelFeatures = "true";
		}
		//gets map's features list
		List lstMapFeatures = (List)moduleResponse.getAttribute("lstMapFeatures");	
		
		String msgWarningSave = msgBuilder.getMessage("5029", "component_mapcatalogue_messages", request);
	%>
	
	

<form method='POST' action='<%=formUrl%>' id='mapForm' name='mapForm' enctype='multipart/form-data' >

	<%-- if(ChannelUtilities.isWebRunning()) { --%>
		<input type='hidden' name='PAGE' value='DetailMapPage' />
		<input type='hidden' name='<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>' value='true' />
	<%-- } --%>

	<input type='hidden' value='<%=modality%>' name='MESSAGEDET' />	
	<input type='hidden' value='<%=map.getMapId()%>' name='id' />
	<input type='hidden' value='<%=map.getBinId()%>' name='BIN_ID' />
	<input type='hidden' value='<%=map.getUrl()%>' name='sourceUrl' />
	<input type='hidden' value='<%=subMessageDet%>' name='SUBMESSAGEDET' />
	<input type='hidden' value='<%= lstFeaturesOld %>' name='lstFeaturesOld' />
 	<input type='hidden' value='<%=msgDelFeatures%>' name='msgDelFeatures' />
    <input type='hidden' value='false' name='modifyFeature' />
	
	<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section' 
			    style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key = "SBIMapCatalogue.detailMap" bundle="component_mapcatalogue_messages" />
			</td>
	<!-- 		<td class='header-empty-column-portlet-section'>&nbsp;</td>-->
			<td class='header-button-column-portlet-section'>
				<a href="javascript:saveMap('SAVE')"> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "SBIMapCatalogue.saveButton"  bundle="component_mapcatalogue_messages"/>' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "SBIMapCatalogue.saveButton" bundle="component_mapcatalogue_messages"/>' 
	      			/> 
				</a>
			</td>		 
			<td class='header-button-column-portlet-section'>
				<input type='image' name='saveAndGoBack' id='saveAndGoBack' onClick="javascript:saveMap('SAVEBACK')" value='true' class='header-button-image-portlet-section'
				       src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/saveAndGoBack.png", currTheme)%>' 
      				   title='<spagobi:message key = "SBIMapCatalogue.saveButtBack" bundle="component_mapcatalogue_messages" />'  
                       alt='<spagobi:message key = "SBIMapCatalogue.saveButtBack" bundle="component_mapcatalogue_messages"/>' 
			   />
			</td>
			<td class='header-button-column-portlet-section'>
				<a href='javascript:goBack("<%=msgWarningSave%>", "<%=backUrl%>")'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "SBIMapCatalogue.backButton"  bundle="component_mapcatalogue_messages"/>' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "SBIMapCatalogue.backButton" bundle="component_mapcatalogue_messages" />' 
	      			/>
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
		<%
			  String sReadonly = "";
			  if (modality.equalsIgnoreCase("DETAIL_MOD")){
			  		sReadonly = "readonly";
			  }
			  String name = map.getName();
			   if((name==null) || (name.equalsIgnoreCase("null"))  ) {
				   name = "";
			   }
		%>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" <%=sReadonly %>
				   name="NAME" size="50" value="<%=StringEscapeUtils.escapeHtml(name)%>" maxlength="45" />
			&nbsp;*
		</div>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>	
				<spagobi:message key = "SBIMapCatalogue.columnDescr" />
			</span>
		</div>
		<div class='div_detail_form'>
		<%
			   String desc = map.getDescr();
			   if((desc==null) || (desc.equalsIgnoreCase("null"))  ) {
			   	   desc = "";
			   }
		%>
			<input class='portlet-form-input-field' type="text" name="DESCR" 
				   size="50" value="<%= StringEscapeUtils.escapeHtml(desc) %>" maxlength="130" />
		</div>
	
		<!-- DISPLAY FORM FOR TEMPLATE  UPLOAD -->
		<div id="form_upload" > 
			<div class='div_detail_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "SBIMapCatalogue.columnUrl" />
				</span>
			</div>
			<div class='div_detail_form'>
	      		<input class='portlet-form-input-field' type="file" 
	      		       size="58" name="uploadFile" id="uploadFile" value="<%= map.getUrl() %>"  onchange='fileToUploadInserted()'/>
				<input style="height:19px;vertical-align: middle;font-size: 12px;" type="button" value='<spagobi:message key = "SBIMapCatalogue.downloadMap" bundle="component_mapcatalogue_messages"/>'
	      		       name="URL_DOWNLOAD" value=""  onClick="downloadFile('<%=downloadUrl%>');" />      		      

			</div>
		</div> 
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>	
				<spagobi:message key = "SBIMapCatalogue.columnFormat" />
			</span>
		</div>
		<div class='div_detail_form'>
		<%
			   String format = map.getFormat();
			   if((format==null) || (format.equalsIgnoreCase("null"))  ) {
				   format = "";
			   }
		%>
			<select class='portlet-form-input-field' name="FORMAT" >
				<option value="" <% if (format.equalsIgnoreCase("")) out.print(" selected='selected' ");  %>>&nbsp;</option>
				<option value="SVG" <% if (format.equalsIgnoreCase("SVG")) out.print(" selected='selected' ");  %>>SVG</option>
			</select>
	   </div>
	
	
	</td><!-- CLOSE COLUMN WITH DATA FORM  -->
		
		
		<spagobi:error/>
	</tr>
	</table>   <!-- CLOSE TABLE FORM ON LEFT AND VERSION ON RIGHT  -->
	
	 <!--</div>  background -->
	
	
	
	<%
	if(modality.equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
	%>
	</div>
	</form>
	<%
	} else if(modality.equalsIgnoreCase(SpagoBIConstants.DETAIL_MOD)) {
	%>
	<!-- GESTIONE TAB FEATURES -->
	  <table  class='header-sub-table-portlet-section' >		
	  	<tr class='header-sub-row-portlet-section'>		
	  		<td class='header-sub-title-column-portlet-section'>
	  		   <spagobi:message key = "SBIMapCatalogue.detailFeature" />
	  		</td>
			<% if (!selectedFeatureId.equals("-1") && !selectedFeatureId.equals("")) { %>
	  		<td class='header-button-column-portlet-section'>		
				<a href='javascript:deleteMapFeature();'>
					<img 	src= '<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.gif", currTheme) %>'
						title='<spagobi:message key = "SBIMapCatalogue.eraseButton" bundle="component_mapcatalogue_messages"/>' alt='<spagobi:message key = "SBIMapCatalogue.eraseButton" bundle="component_mapcatalogue_messages"/>'
					/>
				</a>
			</td>
			<% } %>
	  	</tr>
	  </table>
	 
	 <div style='width:100%;visibility:visible;' class='UITabs' id='tabPanelWithJavascript' name='tabPanelWithJavascript'>
	   <div class="first-tab-level" style="background-color:#f8f8f8">
	     <div style="overflow: hidden; width:  100%">
	       <input type='hidden' id='selectedFeatureId' name='selectedFeatureId' value='<%=selectedFeatureId%>'/>    
	 <%
	         GeoFeature tmpFeature = new GeoFeature();
	         GeoFeature featureSel = new GeoFeature();
	         String linkClass = "tab";
	         if (lstMapFeatures != null){
		     	  for (int i=0; i < lstMapFeatures.size(); i++) {
		     	     tmpFeature = (GeoFeature)lstMapFeatures.get(i); 
		     	     if (String.valueOf(tmpFeature.getFeatureId()).equals(selectedFeatureId)) {
		         	linkClass = "tab selected";
		         	featureSel = tmpFeature;
		         	 }
		         	 else linkClass = "tab";
	     %>
		            <div class='<%=linkClass%>'>
		            	<a href='javascript:changeFeature("<%= String.valueOf(tmpFeature.getFeatureId()) %>")'
		            	   style="color:black;"> 
		            		<%=tmpFeature.getName()%>
		            	</a>
		    			</div> 			
		  <%	} //for
	  }
	  if (selectedFeatureId.equals ("NEW") ) 
			linkClass = "tab selected";
		else 
			linkClass = "tab";
	  %>
						<div class='<%= linkClass%>'>
							<a href='javascript:changeFeature("NEW")'
							   style="color:black;"> 
								<spagobi:message key = "SBIMapCatalogue.newMapFeature" bundle="component_mapcatalogue_messages" />
						    </a>
						</div>
	  			</div>
			</div>
	  </div>	
	
		<div class="div_detail_area_sub_forms">
	    <div class='div_detail_label'>
	    	<span class='portlet-form-field-label'>
	    		<spagobi:message key = "SBIMapCatalogue.columnName" bundle="component_mapcatalogue_messages"/>
	    	</span>
	    </div>
	    <div class='div_detail_form'>    
	        <% String nameFeature = featureSel.getName();
			   if((nameFeature==null) || (nameFeature.equalsIgnoreCase("null"))  ) {
				   nameFeature = "";
			   } 
			%>
	    	<input class='portlet-form-input-field' type="text" name="NAME_FEATURE" 
	    	 size="50" value="<%=StringEscapeUtils.escapeHtml(nameFeature)%>" readonly />
	    	&nbsp;
	    	 <% 
    		if (selectedFeatureId.equals("NEW")){
			%>
			
				<a style="text-decoration:none;" href='<%=lookupFeraturesUrl%>'> 
	      			<img  
	 				 src= '<%=urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme) %>'
	      		 	 title='<spagobi:message key = "SBIMapCatalogue.lookupFeaturesButton" bundle="component_mapcatalogue_messages"/>' 
	      		 	 alt='<spagobi:message key = "SBIMapCatalogue.lookupFeaturesButton" bundle="component_mapcatalogue_messages"/>' />
				</a>
				<input type='hidden' name='' value='' id='loadFeaturesLookup' />
			<% 			
			}
			%>
	    	</div>			
		
	    <div class='div_detail_label'>
	    	<span class='portlet-form-field-label'>
	    		<spagobi:message key = "SBIMapCatalogue.columnDescr" bundle="component_mapcatalogue_messages" />
	    	</span>
	    </div>
	    <div class='div_detail_form'>
	    	<% String descFeature = featureSel.getDescr();
			   if((descFeature==null) || (descFeature.equalsIgnoreCase("null"))  ) {
				   descFeature = "";
			   } 
			%>
	    	<input class='portlet-form-input-field' type="text" name="DESCR_FEATURE" 
	    	 size="50" value="<%=StringEscapeUtils.escapeHtml(descFeature)%>"  readonly />
	    	&nbsp;
	    	</div>			
		
	    <div class='div_detail_label'>
	    	<span class='portlet-form-field-label'>
	    		<spagobi:message key = "SBIMapCatalogue.columnType" bundle="component_mapcatalogue_messages"/>
	    	</span>
	    </div>
	    <div class='div_detail_form'>
	        <% String typeFeature = featureSel.getType();
			   if((typeFeature==null) || (descFeature.equalsIgnoreCase("null"))  ) {
				   typeFeature = "";
			   } 
			%>
	    	<input class='portlet-form-input-field' type="text" name="TYPE_FEATURE" 
	    	 size="50" value="<%=StringEscapeUtils.escapeHtml(typeFeature)%>" readonly />
	    	&nbsp;
	    	</div>			
		</div>
	
	  </div>
	</form>
		
	  <% 
	  }%>
	
	<script>
	
	var fileUploadChanged = 'false';
	function fileToUploadInserted() {
		fileUploadChanged = 'true';
	}
	
	
	function isBIMapFormChanged () {
	
	var biMapFormModified = 'false';
		
	var name = document.mapForm.NAME.value;
	var description = document.mapForm.DESCR.value;	
	var format = document.mapForm.FORMAT.value;

	if ((name != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(map.getName()))%>')
		|| (description != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(map.getDescr()))%>')
		|| (format != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(map.getFormat()))%>')
		|| fileUploadChanged=='true') {
			
		biMapFormModified = 'true';
	}
	
	return biMapFormModified;
	
}

	
	function goBack(message, url) {
	  
	  var mapFormModified = isBIMapFormChanged();
	  if (mapFormModified == 'true'){
	  	  if (confirm(message)) {
	  	      document.getElementById('saveAndGoBack').click(); 
	  	  } else {
			location.href = url;
    	  }	         
       } else {
			location.href = url;
       }	  
	}
	
	function saveMap(type) {
  	  	  document.mapForm.SUBMESSAGEDET.value=type;
      	  document.getElementById('mapForm').submit();
	}
	
	function checkMessage() {
		var msg = "<%=msgDelete%>";  
		
	  if (msg == "") return;	
		
		if (!confirm (msg))
			document.mapForm.MESSAGEDET.value='DETAIL_SELECT';			
		else
		  document.mapForm.MESSAGEDET.value='DEL_MAP_FEATURE';
		  
		document.mapForm.submit();
		
	}
	
	function changeFeature(featureId) {
		
		document.mapForm.selectedFeatureId.value = featureId;
		document.mapForm.MESSAGEDET.value='DETAIL_SELECT';
		document.mapForm.submit();
	}
	
	function deleteMapFeature(){
		var msg = "<%=msgBuilder.getMessage("5026", "component_mapcatalogue_messages", request) %>";
		if (confirm (msg)){	 			 
		  document.mapForm.lstFeaturesOld.value='';
			document.mapForm.MESSAGEDET.value='DEL_MAP_FEATURE';
			document.mapForm.submit();
		}
	}
	
	function downloadFile(url){	 	
		location.href = url;
	}
	
	checkMessage();
	
	</script>
	
	<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
