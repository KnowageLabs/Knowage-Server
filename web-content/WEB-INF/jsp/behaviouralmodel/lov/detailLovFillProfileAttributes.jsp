<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants,
			it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue,
			it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail,
			it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail,
			it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail,
			it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse,
			it.eng.spagobi.commons.dao.DAOFactory,
			it.eng.spago.navigation.LightNavigationManager,
			java.util.List,
			java.util.ArrayList,
			java.util.Iterator"%>
<%@page import="it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory"%>
<%@page import="it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

<%

	SourceBean detailMR = (SourceBean) aServiceResponse.getAttribute("DetailModalitiesValueModule"); 
	List profAttrToFill = (List)detailMR.getAttribute(SpagoBIConstants.PROFILE_ATTRIBUTES_TO_FILL);

    String modality = null;
	if (detailMR != null) modality = (String) detailMR.getAttribute("modality");
	if (modality == null) modality = (String) aSessionContainer.getAttribute(SpagoBIConstants.MODALITY);
  	String messagedet = "";
  	if (modality.equals(SpagoBIConstants.DETAIL_INS))
		messagedet = SpagoBIConstants.DETAIL_INS;
	else messagedet = SpagoBIConstants.DETAIL_MOD;
		
  	Map backUrlPars = new HashMap();
  	backUrlPars.put("PAGE", "DetailModalitiesValuePage");
  	backUrlPars.put(SpagoBIConstants.MESSAGEDET, messagedet);
  	backUrlPars.put("modality", modality);
  	// backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
  	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
  	backUrlPars.put("RETURN_FROM_TEST_MSG", "DO_NOT_SAVE");
    String backUrl = urlBuilder.getUrl(request, backUrlPars);
  	
    Map testUrlPars = new HashMap();
    testUrlPars.put("PAGE", "detailModalitiesValuePage");
    testUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    testUrlPars.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.MESSAGE_TEST_AFTER_ATTRIBUTES_FILLING);
    String testUrl = urlBuilder.getUrl(request, testUrlPars);
    
%>


<form id="formTest" method="post" action="<%=testUrl%>" >



<!-- TITLE -->

<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section'
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBIDev.predLov.profileAttrToFill" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		
		<td class='header-button-column-portlet-section' id='testButton'>
			<input type='image' class='header-button-image-portlet-section' id='testButtonImage'
					name="testLovBeforeSave" value="testLovBeforeSave" 
					src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/test.png", currTheme)%>' 
					title='<spagobi:message key = "SBIDev.predLov.TestBeforeSaveLbl" />'  
					alt='<spagobi:message key = "SBIDev.predLov.TestBeforeSaveLbl" />' 
			/>
		</td>
		
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		
		<td class='header-button-column-portlet-section'>
			<a href="<%=backUrl%>"> 
      				<img class='header-button-image-portlet-section' 
      				     title='<spagobi:message key = "SBISet.Funct.backButt" />' 
      				     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
      				     alt='<spagobi:message key = "SBISet.Funct.backButt" />' />
			</a>
		</td>
	</tr>
</table>




<!-- BODY -->


<div style="padding-left:10px;" class='div_background_no_img' >

	
		


		
		<div class="div_detail_area_forms" >
		
		  <div class='portlet-form-field-label' >
		     <spagobi:message key = "SBIDev.lov.needProfAttr" /> 
			 <br/>
			 <spagobi:message key = "SBIDev.lov.profNotContProfAttr" /> 
			 <br/>
			 <spagobi:message key = "SBIDev.lov.assignValToProfAttr" /> 			
		  </div> 
		  
		  <br/>
		  <br/>
		
		<%
			Iterator iterProfAttr = profAttrToFill.iterator();
		    while(iterProfAttr.hasNext()) {
		    	String profAttrName = (String)iterProfAttr.next();
		%>
			<div class='div_detail_label' style="width:200px;">
				<span class='portlet-form-field-label'>
					<%= profAttrName %>
				</span>
			</div>
			<div class='div_detail_form'>
				<input class='portlet-form-input-field' type="text" name="<%=StringEscapeUtils.escapeHtml(profAttrName)%>" size="35" >
	    		&nbsp;*
			</div>
		<%    	
		    }
		%>
		</div>
		
		<br/>

</div>
				
</form>


