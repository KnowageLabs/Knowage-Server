<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
         import="it.eng.spago.base.*,
                 it.eng.spagobi.commons.constants.SpagoBIConstants"
%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.Enumeration"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<html>
<head>
<style>



.layer
{
			height:100%;
			width:100%;
			position:fixed;
			left:0;
			top:0;
			z-index:1 !important;
			background-image: url('../img/backgroundLogo.jpg');
			background-size: cover;
}

img {
    position: absolute;
    top: 10px;
    right: 10px;
    margin: 10px;
    width: 15%;
}

.text {
    position: absolute;
    bottom: 40px;
    left: 0px;
    margin: 10px;
    font-family: Sans-Serif;
    font-weight: 600;
    color: rgb(189, 80, 128);
}

</style> 
<link id="spagobi-angular" rel="styleSheet"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>" type="text/css" /> 
 
</head>
<body class="landingPageAdmin">
  

	<div class="layer">
		<img src="../img/adminLogo.png" class="logo"/>
		<div class="text">Open menu here to begin</div>

	</div>

</body>
</html>