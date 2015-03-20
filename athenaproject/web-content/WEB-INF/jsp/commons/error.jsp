<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spago.error.EMFErrorHandler, 
                 java.util.Collection, 
                 it.eng.spago.error.EMFAbstractError,
                 it.eng.spago.navigation.LightNavigationManager,
                 java.util.HashMap,
                 java.util.Set,
                 java.util.Iterator" %>
<%@page import="java.util.Map"%>


<%
    // delete validation xml envelope from session
    //List sessAttrs = aSessionContainer.getAttributeNames();
    //Iterator iterAttrs = sessAttrs.iterator();
    //String nameAttrs = null;
    //while(iterAttrs.hasNext()) {
    //	nameAttrs = (String)iterAttrs.next();
    //	if(nameAttrs.startsWith("VALIDATE_PAGE_")) {
    //		aSessionContainer.delAttribute(nameAttrs);
    //	}
    //}


    // recover error handler and error collection 
    EMFErrorHandler errorHandler = aResponseContainer.getErrorHandler();  
	Collection errors = errorHandler.getErrors();
	Iterator iter = errors.iterator();  
	
	// try to get addition info from one of the errors (only the first added info found will be considered)
	Object addInfo = null;
	while(iter.hasNext()) {
		EMFAbstractError abErr = (EMFAbstractError)iter.next();
		Object errAddInfo = abErr.getAdditionalInfo();
	    if(errAddInfo!=null) {
	    	addInfo = errAddInfo;
	    	break;
	    }
	}

    // build back url
	String backUrl = null;
   	Map backUrlPars = new HashMap();     	
	if( (addInfo!=null) && (addInfo instanceof HashMap) ) {
	     HashMap map = (HashMap)addInfo;
	     Set keys = map.keySet();
	     Iterator iterKey = keys.iterator();
	     while(iterKey.hasNext()) {
			String key = (String)iterKey.next();
			String value = (String)map.get(key);
			backUrlPars.put(key, value);	     
	     }
	 } else {
		 String lightNavigatorDisabled = (String) aServiceRequest.getAttribute(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED);
		 if (lightNavigatorDisabled != null && lightNavigatorDisabled.trim().equalsIgnoreCase("true")) {
			 backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "0");
		 } else {
			 backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
		 }
		 
	 }
	 backUrl = urlBuilder.getUrl(request, backUrlPars);
    
%>

<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section'>
			<spagobi:message key = "SBIErrorPage.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl.toString() %>'> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message key = "SBIErrorPage.backButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBIErrorPage.backButt" />' />
			</a>
		</td>
	</tr>
</table>



<div style='width:100%;text-align:center;'>
	<div class="portlet-msg-error">
    	<% 
    	iter = errors.iterator(); 
        EMFAbstractError error = null;
        String description = "";
    	while(iter.hasNext()) {
    		error = (EMFAbstractError)iter.next();
 		    description = error.getDescription();
 		    if(addInfo==null) {
 		    	addInfo = error.getAdditionalInfo();
 		    }
    	%>
		<%= description %>
		<br/>
		<% } %>
	</div>
</div>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>