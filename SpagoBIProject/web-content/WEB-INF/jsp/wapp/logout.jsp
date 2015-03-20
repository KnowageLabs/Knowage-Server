<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="it.eng.spagobi.commons.SingletonConfig"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>

<iframe id='invalidSessionJasper'
                 name='invalidSessionJasper'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/SpagoBIJasperReportEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionJPivot'
                 name='invalidSessionJPivot'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/SpagoBIJPivotEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionQbe'
                 name='invalidSessionQbe'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/SpagoBIQbeEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionGeo'
                 name='invalidSessionGeo'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/SpagoBIGeoEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionBirt'
                 name='invalidSessionBirt'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/SpagoBIBirtReportEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionTalend'
                 name='invalidSessionTalend'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/SpagoBITalendEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionChart'
                 name='invalidSessionChart'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/SpagoBIChartEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionCockpit'
                 name='invalidSessionCockpit'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/SpagoBICockpitEngine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionSocialAnalysis'
                 name='invalidSessionSocialAnalysis'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/SpagoBISocialAnalysis/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 
<%

boolean backUrlB=false;
String backUrl="";
String redirectUrl ="";

if(session.getAttribute(SpagoBIConstants.BACK_URL)!=null){
	backUrl=(String)session.getAttribute(SpagoBIConstants.BACK_URL);
	backUrlB=true;
}


session.invalidate();


//Check if SSO is active

String active = SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.ACTIVE");
String strUsePublicUser = SingletonConfig.getInstance().getConfigValue(SpagoBIConstants.USE_PUBLIC_USER);
Boolean usePublicUser = (strUsePublicUser == null)?false:Boolean.valueOf(strUsePublicUser);

if ((active == null || active.equalsIgnoreCase("false")) && !backUrlB) {
	String context = request.getContextPath();
	if (usePublicUser){
		context += "/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE";
		redirectUrl = context;
	}else{
		redirectUrl = context;
	}
}
else if (active != null && active.equalsIgnoreCase("true")) {

	String urlLogout =  SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.SECURITY_LOGOUT_URL");
	if(backUrlB==true){
		redirectUrl = backUrl; 
	}
	redirectUrl = urlLogout;

} %>
 

<script>

var myVar=setTimeout(function(){redirect()},1000);

function redirect()
{
    window.location = "<%=redirectUrl%>"
}

</script>



