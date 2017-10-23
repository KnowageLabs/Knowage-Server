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
<%@page import="java.util.Locale"%>

<script type="text/javascript" src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/ext-all-debug.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/examples/ux/IFrame.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/ux/RowExpander.js'></script>

<% 

String sbiMode = "WEB";
IUrlBuilder urlBuilder = null;
urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);

String strLocale = request.getParameter("locale"); 	
Locale locale=new Locale(strLocale.substring(0,strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_")+1), "");
String accountId = request.getParameter("accountId"); 
	
IMessageBuilder msgBuilder = MessageBuilderFactory
		.getMessageBuilder();
%>


<script type="text/javascript">
  Ext.ns("Sbi.config");
  Sbi.config.loginUrl = "";
  
  function active(accountId) {
  var locale = '<%= strLocale %>';
  
  //Service Registry creation
  var url = {
    	host: '<%= request.getServerName()%>'
    	, port: '<%= request.getServerPort()%>'
    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
    	, controllerPath: null // no cotroller just servlets   
    };
  
  Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
	baseUrl: url
    , baseParams: params
  });
  
  this.services = [];

  //Adding a new service to the registry
  this.services["active"]= Sbi.config.serviceRegistry.getRestServiceUrl({
	serviceName: 'signup/active',	
	baseParams: {}
  });

  var params = new Object();
  params.accountId = accountId;
  params.locale = locale;
  
  Ext.Ajax.request({
	   url: this.services["active"],
	   method: "POST",
	   params: params,			
	   success : function(response, options) {	
		
	    if(response != undefined  && response.responseText != undefined ) {
			if( response.responseText != null && response.responseText != undefined ){
		      var jsonData = Ext.decode( response.responseText );
		      Sbi.exception.ExceptionHandler.showInfoMessage(jsonData.message, '<%=msgBuilder.getMessage("signup.active.title")%>', {});
			}		
		}
		else {
			  Sbi.exception.ExceptionHandler.showErrorMessage('<%=msgBuilder.getMessage("sbi.generic.serviceResponseEmpty")%>', '<%=msgBuilder.getMessage("sbi.generic.serviceError")%>');
		}
	    
	    setTimeout(
	    		function(){
	    			window.location.href = '${pageContext.request.contextPath}/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE';	
	    	}, 3000);
	    
	    
      },
	  scope: this,
	  failure: Sbi.exception.ExceptionHandler.handleFailure
    });
  }
  
  active('<%= request.getParameter("accountId") %>');
  
</script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/src/ext/sbi/service/ServiceRegistry.js'></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/src/ext/sbi/exception/ExceptionHandler.js'></script>

<link id="extall"     rel="styleSheet" href ="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/resources/css/ext-all.css" type="text/css" />
<link id="theme-gray" rel="styleSheet" href ="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/resources/css/ext-all-gray.css" type="text/css" />
<link id="spagobi-ext-4" rel="styleSheet" href ="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/overrides/resources/css/spagobi.css" type="text/css" />

<html>
  <head>
  <style media="screen" type="text/css">

	input.login    {
	display:block;
	border: 1px solid #a9a9a9; 
	color: #7b7575;
	background: #d4d4d4; 
	height: 25px;
	width: 300px;
	-webkit-box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.3);
	-moz-box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.3);
	box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.3);
	}
	body {
background: #dedede; /* Old browsers */
background: -moz-linear-gradient(top,  #dedede 0%, #efefef 100%); /* FF3.6+ */
background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#dedede), color-stop(100%,#efefef)); /* Chrome,Safari4+ */
background: -webkit-linear-gradient(top,  #dedede 0%,#efefef 100%); /* Chrome10+,Safari5.1+ */
background: -o-linear-gradient(top,  #dedede 0%,#efefef 100%); /* Opera 11.10+ */
background: -ms-linear-gradient(top,  #dedede 0%,#efefef 100%); /* IE10+ */
background: linear-gradient(to bottom,  #dedede 0%,#efefef 100%); /* W3C */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#dedede', endColorstr='#efefef',GradientType=0 ); /* IE6-9 */
height: 100%;
margin: 0;
background-repeat: no-repeat;
background-attachment: fixed;
	}
	td.login-label{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 10 px;
	color: #7d7d7d;
}

a:link{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 9px;
	color: #7d7d7d;
}
a:visited{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 9px;
	color: #7d7d7d;
}
a:hover{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 9px;
	color: #7d7d7d;
}

 </style>
  
  
  <script type="text/javascript">
    function signup(){
    	var form = document.getElementById('formId');
    	var act = '${pageContext.request.contextPath}/restful-services/signup/prepare';
    	form.action = act;
    	form.submit();
    	
    }
	function escapeUserName(){
	
	userName = document.login.userID.value;
	
		if (userName.indexOf("<")>-1 || userName.indexOf(">")>-1 || userName.indexOf("'")>-1 || userName.indexOf("\"")>-1 || userName.indexOf("%")>-1)
			{
			alert('Invalid username');
			return false;
			}
		else
			{return true;}
	}
	
	function setUser(userV, pswV){
		var password = document.getElementById('password');
		var user = document.getElementById('userID');
		password.value = pswV;
		user.value = userV;
	//	document.forms[0].submit();
	}
	</script>
	<link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "img/favicon.ico")%>" />
    <title>Knowage</title>
    <style>
      body {
	       padding: 0;
	       margin: 0;
      }
    </style> 
  </head>

  <body onload="javascript:active('<%= request.getParameter("accountId") %>')">
  
  <LINK rel='StyleSheet' 
    href='${pageContext.request.contextPath}/css/spagobi_shared.css' 
    type='text/css' />
    
  </body>
</html>
