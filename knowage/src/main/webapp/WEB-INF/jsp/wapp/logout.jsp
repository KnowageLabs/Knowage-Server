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

<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>    
<%@page import="it.eng.spagobi.commons.SingletonConfig"%>
<%@page import="it.eng.spagobi.commons.utilities.AuditLogUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spagobi.security.google.config.GoogleSignInConfig"%>
<%@page import="it.eng.spago.base.SessionContainer"%>
<%@page import="it.eng.spago.base.RequestContainer"%>

<iframe id='invalidSessionJasper'
                 name='invalidSessionJasper'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>jasperreportengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

</iframe>  

<iframe id='invalidSessionQbe'
                 name='invalidSessionQbe'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>qbeengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionBirt'
                 name='invalidSessionBirt'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>birtreportengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>  

<iframe id='invalidSessionTalend'
                 name='invalidSessionTalend'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>talendengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionWhatIf'
                 name='invalidSessionChart'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>whatifengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe>

<iframe id='invalidSessionCockpit'
                 name='invalidSessionCockpit'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>cockpitengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionGis'
                 name='invalidSessionGis'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>georeportengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionKpi'
                 name='invalidSessionKpi'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>kpiengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionMeta'
                 name='invalidSessionMeta'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>meta/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionSvg'
                 name='invalidSessionSvg'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>svgengine/invalidateSession.jsp'
                 height='0'
                 width='0'
                 frameborder='0' >
</iframe> 

<iframe id='invalidSessionCommonj'
                 name='invalidSessionCommonj'
                 src='<%=KnowageSystemConfiguration.getKnowageContext()%>commonjengine/invalidateSession.jsp'
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

RequestContainer reqCont = RequestContainer.getRequestContainer();
SessionContainer sessCont = reqCont.getSessionContainer();
SessionContainer permSess = sessCont.getPermanentContainer();
IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
if (profile != null) {
	// removing user profile object from permanent container
	permSess.setAttribute(IEngUserProfile.ENG_USER_PROFILE, null);
	HashMap<String, String> logParam = new HashMap<String, String>();
	logParam.put("USER", profile.toString());
	AuditLogUtilities.updateAudit(request, profile, "SPAGOBI.Logout", logParam, "OK");
}

// invalidate http session
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

function redirect() {
	var redirectUrl = "<%= redirectUrl %>";
	redirectUrl = resolveDynamicParameters(redirectUrl);
    window.location = redirectUrl;
};

function resolveDynamicParameters(url) {
	return url.replace("<%= "${id_token}" %>", window.sessionStorage.getItem("id_token"));
}

function setTimeoutToRedirect() {
	setTimeout(function(){
		redirect()
	}, 1000);
};

</script>


<% if (GoogleSignInConfig.isEnabled()) { %>

	<%-- Resources for Google Sign-In authentication --%>
	<script src="https://apis.google.com/js/platform.js?onload=onLoad" async defer></script>
	<meta name="google-signin-client_id" content="<%= GoogleSignInConfig.getClientId() %>">
	<script>
	function googleSignOut(callback, fail) {
		var auth2 = gapi.auth2.getAuthInstance();
		auth2.signOut().then(function() {
				auth2.disconnect();
				callback();
			}, 
			fail
		);
	};

	function onLoad() {
		gapi.load('auth2', function() {
			gapi.auth2.init().then(function () {
			
				googleSignOut(setTimeoutToRedirect, function () {
					alert("An error occurred during Google logout");
				});
			
			});
		});
	};

	</script>
	
<% }  else { %>

	<script>
	setTimeoutToRedirect();
	</script>
	
<% } %>


