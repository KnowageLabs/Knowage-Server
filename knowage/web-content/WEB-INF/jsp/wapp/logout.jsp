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


<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="it.eng.spagobi.commons.SingletonConfig"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>

<iframe id='invalidSessionJasper'
                 name='invalidSessionJasper'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/knowagejasperreportengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionJPivot'
                 name='invalidSessionJPivot'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/knowagejpivotengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionQbe'
                 name='invalidSessionQbe'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/knowageqbeengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionGeo'
                 name='invalidSessionGeo'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/knowagegeoengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionBirt'
                 name='invalidSessionBirt'
                 src='<%=GeneralUtilities.getSpagoBiHost()%>/knowagebirtreportengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionTalend'
                 name='invalidSessionTalend'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/knowagetalendengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionChart'
                 name='invalidSessionChart'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/knowagechartengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionCockpit'
                 name='invalidSessionCockpit'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/knowagecockpitengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionSocialAnalysis'
                 name='invalidSessionSocialAnalysis'
                 src='<%=GeneralUtilities.getSpagoBiHost()	%>/knowagesocialanalysis/invalidateSession.jsp'
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



