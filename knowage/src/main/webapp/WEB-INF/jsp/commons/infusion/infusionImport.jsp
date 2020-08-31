<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">

<%-- ---------------------------------------------------------------------- --%>
<%-- INFUSION																--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities" %>
<%@page import="it.eng.spagobi.commons.bo.AccessibilityPreferences" %>


<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/infusion/src/lib/normalize/css/normalize.css")%>" />
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/infusion/src/framework/core/css/fluid.css")%>" />
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/infusion/src/framework/preferences/css/Enactors.css")%>" />
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/infusion/src/framework/preferences/css/PrefsEditor.css")%>" />
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/infusion/src/framework/preferences/css/SeparatedPanelPrefsEditor.css")%>" />

<script type="text/javascript"          src="<%=urlBuilder.getResourceLink(request, "js/lib/infusion/infusion-all.js")%>"></script>


<%
   String preferences= null;
   if(ap!= null){
	   preferences= ap.getPreferences();
   }


%>
<script>
   var contextName= '<%= KnowageSystemConfiguration.getKnowageContext() %>';
   var host= window.location.origin;
   var baseUrl= ''+host +''+ contextName + '/restful-services';
   var preferences = '<%= preferences %>';
</script>