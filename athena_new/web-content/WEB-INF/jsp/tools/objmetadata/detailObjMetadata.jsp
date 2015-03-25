<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
	<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
	
	<%@ page         import="it.eng.spagobi.tools.objmetadata.bo.ObjMetadata,
	 				         it.eng.spago.navigation.LightNavigationManager,
	 				         java.util.Map,java.util.HashMap,java.util.List,
	 				         java.util.Iterator,
	 				         it.eng.spagobi.commons.bo.Domain,
	 				         it.eng.spagobi.tools.objmetadata.service.DetailObjMetadataModule" %>
	 				         
	<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
	
	<%@page import="it.eng.spago.util.JavaScript"%>
<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

	<%
		SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailObjMetadataModule"); 
		ObjMetadata meta = (ObjMetadata)moduleResponse.getAttribute("metaObj");
		List listDataType = (List) moduleResponse.getAttribute(DetailObjMetadataModule.OBJMETA_DATA_TYPE);
		
		String modality = (String)moduleResponse.getAttribute("modality");
		String subMessageDet = ((String)moduleResponse.getAttribute("SUBMESSAGEDET")==null)?"":(String)moduleResponse.getAttribute("SUBMESSAGEDET");
		String msgWarningSave = msgBuilder.getMessage("13002", request);
		
		Map formUrlPars = new HashMap();
		if(ChannelUtilities.isPortletRunning()) {
			formUrlPars.put("PAGE", "DetailObjMetadataPage");	
  			formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");	
		}
		String formUrl = urlBuilder.getUrl(request, formUrlPars);
		
		Map backUrlPars = new HashMap();
		backUrlPars.put("PAGE", "ListObjMetadataPage");
		backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
		String backUrl = urlBuilder.getUrl(request, backUrlPars);		
	%>
	
	

<form method='POST' action='<%=formUrl%>' id='metaForm' name='metaForm' >

	<% if(ChannelUtilities.isWebRunning()) { %>
		<input type='hidden' name='PAGE' value='DetailObjMetadataPage' />
		<input type='hidden' name='<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>' value='true' />
	<% } %>

	<input type='hidden' value='<%=modality%>' name='MESSAGEDET' />	
	<input type='hidden' value='<%=subMessageDet%>' name='SUBMESSAGEDET' />
	<input type='hidden' value='<%=meta.getObjMetaId()%>' name='id' />
	
	<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section' 
			    style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key = "metadata.TitleDetail"  />
			</td>
			<td class='header-button-column-portlet-section'>
				<a href="javascript:saveObjMeta('SAVE')"> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "metadata.saveButton" />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "metadata.saveButton"/>' 
	      			/> 
				</a>
			</td>		 
			<td class='header-button-column-portlet-section'>
				<input type='image' name='saveAndGoBack' id='saveAndGoBack' onClick="javascript:saveObjMeta('SAVEBACK')" class='header-button-image-portlet-section'
				       src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/saveAndGoBack.png", currTheme)%>' 
      				   title='<spagobi:message key = "metadata.saveBackButton" />'  
                       alt='<spagobi:message key = "metadata.saveBackButton" />' 
			   />
			</td>
			<td class='header-button-column-portlet-section'>
				<a href='javascript:goBack("<%=msgWarningSave%>", "<%=backUrl%>")'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "metadata.backButton"  />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "metadata.backButton" />' 
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
				<spagobi:message key = "metadata.metaLabel" />
			</span>
		</div>
		<%
			  String isReadonly = "";
			  if (modality.equalsIgnoreCase("DETAIL_MOD")){
			  		isReadonly = "readonly";
			  }
			  String label = meta.getLabel();
			   if((label==null) || (label.equalsIgnoreCase("null"))  ) {
				   label = "";
			   }
		%>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" 
				   name="LABEL" size="50" value="<%=StringEscapeUtils.escapeHtml(label)%>" maxlength="50" <%=isReadonly%>/>
			&nbsp;*
		</div>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "metadata.metaName" />
			</span>
		</div>
		<%
			  String name = meta.getName();
			   if((name==null) || (name.equalsIgnoreCase("null"))  ) {
				   name = "";
			   }
		%>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" 
				   name="NAME" size="50" value="<%=StringEscapeUtils.escapeHtml(name)%>" maxlength="50" />
			&nbsp;*
		</div>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>	
				<spagobi:message key = "metadata.metaDescr" />
			</span>
		</div>
		<div class='div_detail_form'>
		<%
			   String desc = meta.getDescription();
			   if((desc==null) || (desc.equalsIgnoreCase("null"))  ) {
			   	   desc = "";
			   }
		%>
			<input class='portlet-form-input-field' type="text" name="DESCR" 
				   size="50" value="<%= StringEscapeUtils.escapeHtml(desc) %>" maxlength="160" />
		</div>
		
		<div class='div_detail_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "metadata.metaType" />
				</span>
		</div>
		
	
		<div class='div_detail_form'>
      		<select class='portlet-form-input-field' style='width:250px;' 
					name="DATA_TYPE" id="DATA_TYPE" >
			<% if (listDataType != null){
				Iterator iterDialect = listDataType.iterator();
			   
      			while(iterDialect.hasNext()) {
      				Domain dataType = (Domain)iterDialect.next();
      				Integer objDataType = meta.getDataType();
      				Integer currDataType = dataType.getValueId();
                    boolean isDataType = false;
      		    	if(objDataType.intValue() == currDataType.intValue()){
      		    		isDataType = true;   
      		    	}
      		%>
      			<option value="<%=dataType.getValueId() %>"<%if(isDataType) out.print(" selected='selected' ");  %>><%=StringEscapeUtils.escapeHtml(dataType.getTranslatedValueName(locale))%></option>
      		<% 	
      			}
			}
      		%>
      		</select>
		</div> 
		
	</td><!-- CLOSE COLUMN WITH DATA FORM  -->
		
		
		<spagobi:error/>
	</tr>
	</table>   <!-- CLOSE TABLE FORM ON LEFT AND VERSION ON RIGHT  -->
	
	 <!--</div>  background -->
	
	<script>
	
	function isMetaFormChanged () {
	
	var bFormModified = 'false';
		
	var dataType = document.metaForm.DATA_TYPE.value;
	var label = document.metaForm.LABEL.value;
	var description = document.metaForm.DESCR.value;	
	var name = document.metaForm.NAME.value;
	

	
	if ((label != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(meta.getLabel()))%>')
	    || (dataType != '<%=(meta.getDataType()==null)?"":meta.getDataType().toString()%>')
		|| (description != '<%=(StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(meta.getDescription()))==null)?"":StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(meta.getDescription()))%>')
		|| ( name != '<%=(StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(meta.getName()))==null)?"":StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(meta.getName()))%>')
		) {
			
		bFormModified = 'true';
	}
	
	return bFormModified;
	
	}

	
	function goBack(message, url) {
	  
	  var bFormModified = isMetaFormChanged();
	  
	  if (bFormModified == 'true'){
	  	  if (confirm(message)) {
	  	      document.getElementById('saveAndGoBack').click(); 
	  	  } else {
			location.href = url;	
    	  }	         
       } else {
			location.href = url;
       }	  
	}
	
	function saveObjMeta(type) {	
  	  	  document.metaForm.SUBMESSAGEDET.value=type;
  	  	  if (type == 'SAVE')
      		  document.getElementById('metaForm').submit();
	}
	
	</script>
	
	<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
	
