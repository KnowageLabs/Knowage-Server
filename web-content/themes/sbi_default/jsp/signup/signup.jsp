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
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="it.eng.spagobi.community.mapping.SbiCommunity"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>  

<%
	String defaultOrganization = msgBuilder.getMessage("profileattr.company",request); 
	String defaultName = msgBuilder.getMessage("profileattr.firstname",request);
	String defaultSurname = msgBuilder.getMessage("profileattr.lastname",request);
	String defaultUsername = msgBuilder.getMessage("username",request); //"Username";
	String defaultPassword = msgBuilder.getMessage("password",request); //"Password";
	String defaultEmail = msgBuilder.getMessage("profileattr.email",request);
	String defaultConfirmPwd = msgBuilder.getMessage("confirmPwd",request);
	String defaultCaptcha = msgBuilder.getMessage("signup.form.captcha",request); 
	String defaultLocation = msgBuilder.getMessage("signup.form.location",request);
	String defaultLanguage = msgBuilder.getMessage("signup.form.language",request);
	String defaultItalian = msgBuilder.getMessage("signup.form.langItalian",request);
	String defaultEnglish = msgBuilder.getMessage("signup.form.langEnglish",request);
	String defaultFrench = msgBuilder.getMessage("signup.form.langFrench",request);
	String defaultSpanish = msgBuilder.getMessage("signup.form.langSpanish",request);
	String defaultBirthday = msgBuilder.getMessage("signup.form.birthday",request);
	String defaultGender = msgBuilder.getMessage("signup.form.gender",request);
	String defaultMan = msgBuilder.getMessage("signup.form.genderMan",request);
	String defaultWoman = msgBuilder.getMessage("signup.form.genderWoman",request);
	String defaultCommunity = msgBuilder.getMessage("signup.form.community",request);
	String defaultShortBio = msgBuilder.getMessage("signup.form.shortBio",request);
		
	String registrationSuccessMsg = msgBuilder.getMessage("signup.msg.success",request);
	Locale localeSignup =  (request.getAttribute("locale")==null)?null:(Locale)request.getAttribute("locale");
	List comunities = (request.getAttribute("communities")==null)?new ArrayList():(List)request.getAttribute("communities");
	
%>

<script type="text/javascript" src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/ext-all-debug.js'/></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/examples/ux/IFrame.js'/></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/ux/RowExpander.js'/></script>

<script type="text/javascript">

Ext.ns("Sbi.config");
Sbi.config.loginUrl = "";


function nascondi(){
  var optDiv = document.getElementById("optional");
  var a = document.getElementById("nascondi");
  if (optDiv.style.display == 'none') {
    optDiv.style.display = '';
    a.innerHTML = '<%=msgBuilder.getMessage("signup.form.hideOptional",request) %>';
  }
  else {
    optDiv.style.display = 'none';
    a.innerHTML = '<%=msgBuilder.getMessage("signup.form.showOptional",request) %>';
  }
}

function register() {

	
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
this.services["create"]= Sbi.config.serviceRegistry.getRestServiceUrl({
	serviceName: 'signup/create',
	baseParams: {}
});

    var form             = document.myForm;
	var nome             = document.getElementById("nome").value;
	var cognome          = document.getElementById("cognome").value;
	var username         = document.getElementById("username").value;
	var password         = document.getElementById("password").value;
	var confermaPassword = document.getElementById("confermaPassword").value;
	var email            = document.getElementById("email").value;
	var sesso            = document.getElementById("sesso").value;
	var dataNascita      = document.getElementById("dataNascita").value;
	var indirizzo        = document.getElementById("indirizzo").value;
	var azienda          = document.getElementById("azienda").value;
	var biografia        = document.getElementById("biografia").value;
	var lingua           = document.getElementById("lingua").value;
	var captcha          = document.getElementById("captcha").value;
	var check            = document.getElementById("termini");
	var termini = 'false';
    if( check.checked ) termini = 'true';
    
	var params = new Object();
	params.locale	= '<%=localeSignup%>';
	params.nome     = nome;
	params.cognome  = cognome;
	params.username = username;
	params.password = password;
	params.confermaPassword 
	                = confermaPassword;
	params.email    = email;
	params.sesso    = sesso;
	params.dataNascita = dataNascita;
	params.indirizzo   = indirizzo;
	params.azienda     = azienda;
	params.biografia   = biografia;
	params.termini     = termini;
	params.lingua      = lingua;
	params.captcha     = captcha;
		
	//params.modify      = true;

	var goOn = false;
	if (document.getElementById("aziendaIsChanged").value == "true"){
		Ext.MessageBox.confirm(
				  "Warning",
				  "<%=msgBuilder.getMessage("signup.msg.confirmCreateComm", request)%>",
				  function(btn, text){					  
					  if (btn=='yes') {
						  execCreation(params);
						  return;
					  }
				  }
		);
	}else 
		goOn=true;
	
	if (goOn){
		execCreation(params);
	}
}

function execCreation(params){
	var registrationSuccessMsg = "<%=registrationSuccessMsg%>";
	
	Ext.Ajax.request({
		url: this.services["create"],
		method: "POST",
		params: params,			
		success : function(response, options) {	
			
		    if(response != undefined  && response.responseText != undefined ) {
				if( response.responseText != null && response.responseText != undefined ){
			    var jsonData = Ext.decode( response.responseText );
			    if( jsonData.message != undefined && jsonData.message != null && jsonData.message == 'validation-error' ){
			      Sbi.exception.ExceptionHandler.handleFailure(response);
			    }else{
			      Sbi.exception.ExceptionHandler.showInfoMessage(registrationSuccessMsg, 'OK', {});
			    }		
			  }		
			}
			else {
				
			  Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			}
	    },
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure
		});
}
</script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/src/ext/sbi/service/ServiceRegistry.js'/></script>
<script type="text/javascript" src='${pageContext.request.contextPath}/js/src/ext/sbi/exception/ExceptionHandler.js'/></script>

<link id="extall"     rel="styleSheet" href ="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/resources/css/ext-all.css" type="text/css" />
<link id="theme-gray" rel="styleSheet" href ="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/resources/css/ext-all-gray.css" type="text/css" />
<link id="spagobi-ext-4" rel="styleSheet" href ="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/overrides/resources/css/spagobi.css" type="text/css" />

<html>
  <head>
  <style media="screen" type="text/css">

	
	body {
		background: #dedede; /* Old browsers *
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
	font-size: 14px;
	color: #F8A400;
	text-decoration:none;
}
a:visited{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 14px;
	color: #F8A400;
	text-decoration:none;
}
a:hover{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 14px;
	color: #F8A400;
	text-decoration:none;
}

.submit{clear:both;width:auto;padding-top:12px;text-align:center}
.submit input{border:0;height:42px;width:198px;padding:2px 31px 0;text-align:center;cursor:pointer;margin:0 0 20px;font-family:cabinregular,arial,helvetica,sans-serif;font-size:1.33em;font-weight:bold;line-height:100%;text-transform:uppercase;background:#F8A400;color:#fff;-webkit-border-radius:4px;-moz-border-radius:4px;border-radius:4px}
.submit input:hover{opacity:0.9;filter:alpha(opacity = 90)}

.footerMsg {color:#193B54;}

 </style>
  
  <link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "img/favicon.ico")%>" />
  <title>SpagoBI signup</title>
  <LINK rel='StyleSheet' 
    href='${pageContext.request.contextPath}/themes/sbi_default/css/spagobi_shared.css' 
    type='text/css' />
  
  <style>
      body {
	       padding: 0;
	       margin: 0;
      }
  </style> 
  </head>

  <body>
   
	 <div id="content" style="height:100%">  
    		<form name="myForm" method="post" action="${pageContext.request.contextPath}/">
            	<input type="hidden" id="locale" name="locale" value="<%=locale%>" />
       
		        	<div style="padding: 20px " >
		        	<!--
		        	DO NOT DELETE THIS COMMENT
		        	If you change the tag table with this one  you can have the border of the box with the shadow via css
		        	the problem is that it doesn't work with ie	
		     		
		     		<table style="background: none repeat scroll 0 0 #fff; border-radius: 10px 10px 10px 10px;  box-shadow: 0 0 10px #888; color: #009DC3; display: block; font-size: 14px; line-height: 18px; padding: 20px;">
		        	 -->

				<table border="0" align="center" style="border-collapse:separate; background: none repeat scroll 0 0; border-radius: 5px 5px 5px 5px;  box-shadow: 0px 0px 10px #888;  -webkit-box-shadow:  0px 0px 10px #888;  -moz-box-shadow:  0px 0px 10px #888; color: #009DC3; display: block; font-size: 14px; line-height: 18px; padding: 20px;">
				   <tr>
						<td></td>
						<td><img
							src='${pageContext.request.contextPath}/themes/sbi_default/img/wapp/spagobi40logo.png'
							width='180px' height='51px' style="margin: 20px 0px"/>
						</td>
						<td width='50px'></td>
						<td></td>
					</tr> 
					<tr>
						<td width="120px">&nbsp;</td>
						<td width="550px">
                            
							<table border="0">
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">*&nbsp;<%=defaultName%>:</td>
									<td width="75px">&nbsp;</td>

									<td class='login-label'>*&nbsp;<%=defaultSurname%>:</td>

								</tr>
								<tr>
									<td><input id="nome" name="nome" type="text" size="25"
										class="login"></td>
									<td></td>

									<td><input id="cognome" name="cognome" type="text"
										size="25" class="login"></td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">*
										<%=defaultUsername%>:</td>
									<td width="25px"></td>
									<td class='login-label'>*&nbsp;<%=defaultEmail%>:</td>

								</tr>
								<tr>
									<td><input id="username" name="username" type="text"
										size="25" class="login">
									</td>
									<td></td>

									<td><input id="email" name="email" type="text" size="25"
										class="login">
									</td>

								</tr>


								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">*&nbsp;<%=defaultPassword%>:</td>
									<td width="25px">&nbsp;</td>

									<td class='login-label'>*&nbsp;<%=defaultConfirmPwd%>:</td>

								</tr>
								<tr>
									<td valign="top"><input id="password" name="password"
										type="password" size="25" class="login">
									</td>
									<td></td>

									<td><input id="confermaPassword" name="confermaPassword"
										type="password" size="25" class="login">
									</td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">*&nbsp;<%=defaultCaptcha%>:</td>
									<td width="25px">&nbsp;</td>
                                    <td></td>
							    </tr>
								<tr>
									<td colspan="3"><input id="captcha" name="captcha"
										type="text" size="25" class="login" />
									</td>
								</tr>
								<tr height="7px">
									<td></td>
									<td></td>
									<td></td>
								</tr>
								<tr>
									<td colspan="3"><div id="sticky"><img
										src='${pageContext.request.contextPath}/stickyImg'
										width='250px' height='75px' /></div>
									</td>
									
								</tr>
								
								<tr>
								  <td colspan="3" class='login-label'>*&nbsp;<%=msgBuilder.getMessage("signup.form.agreeTerms",request) %>:</td>
							    </tr>
								<tr>
									<td colspan="3" height="30px"><input type="checkbox"
										name="termini" id="termini" class="login" />
									</td>
								</tr>
								<tr><td colspan="3"><a href="#" onclick="javascript:nascondi();"><div id="nascondi" style="font-weight:bold"><%=msgBuilder.getMessage("signup.form.showOptional",request) %></div></a></td></tr>
							</table>
							<div id="optional" style="display:none">
							<table border="0">
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultLocation%>:</td>
									<td width="75px">&nbsp;</td>

									<td class='login-label'><%=defaultLanguage %>:</td>

								</tr>

								<tr>
									<td><input id="indirizzo" name="indirizzo" type="text"
										size="25" class="login" />
									</td>
									<td></td>

									<td><select class="login" name="lingua" id="lingua"
										style="width: 214px">
											<option value=""></option>
											<option value="it_IT"><%=defaultItalian%></option>
											<option value="en_US"><%=defaultEnglish%></option>
											<option value="fr_FR"><%=defaultFrench%></option>
											<option value="es_ES"><%=defaultSpanish%></option>
									</select>
									</td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultGender %>:</td>
									<td width="25px">&nbsp;</td>

									<td class='login-label'><%=defaultBirthday %> (dd/mm/yyyy):</td>

								</tr>

								<tr>
									<td><select class="login" name="sesso" id="sesso"
										style="width: 214px">
											<option value=""></option>
											<option value="Uomo"><%=defaultMan %></option>
											<option value="Donna"><%=defaultWoman %></option>
									</select>
									</td>
									<td></td>

									<td><input id="dataNascita" name="dataNascita" type="text"
										size="25" class="login" />
									</td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left" ><%=defaultCommunity %>:</td>
									<td></td>
									<td class='login-label'></td>
								</tr>
								<tr>
									<td>
										<!-- <input id="azienda" name="azienda" type="text" size="25" class="login"/> --> 
										<div class="login" style="position:relative;width:200px;height:25px;border:0;padding:0;margin:0;">
											<select style="position:absolute;top:0px;left:0px;width:214px; height:25px;line-height:20px;margin:0;padding:0;" onchange="document.getElementById('azienda').value=this.options[this.selectedIndex].text;">											
												<option></option>	
												<%for(int i=0; i<comunities.size(); i++){
													SbiCommunity objComm = (SbiCommunity)comunities.get(i); %>
													<option value="<%=objComm.getName()%>"><%=objComm.getName() %></option>	
												<%} %>																															
											</select>											
											<input type="text" name="azienda" placeholder="" id="azienda" style="position:absolute;top:1px;left:2px;width:183px;height:23px;border:0px;" onfocus="this.select()" onKeyPress="document.getElementById('aziendaIsChanged').value=true">											
											<input type="hidden" name="aziendaIsChanged" id="aziendaIsChanged"> 									
										</div>
									<td></td>
									<td></td>
								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultShortBio %>:</td>
									<td width="75px">&nbsp;</td>
									<td></td>

								</tr>
								<tr>
									<td colspan="3"><textarea style="width: 500px"
											class="login" rows="5" cols="35" name="biografia"
											id="biografia"></textarea>
									</td>

								</tr>
								</table>
								</div>
								<table border="0">
								  <tr>
								    <td colspan="0">
								     <table border="0">
								      <tr height="20px"><td colspan="3">&nbsp;</td></tr>
								      <tr>
									 <!--  <td align="right">
									    <a href="${pageContext.request.contextPath}/" >
									      <img src='${pageContext.request.contextPath}/themes/sbi_default/img/wapp/back.png' width='100px' height='37px' />
									    </a>
									  </td> -->
									  <td width="50px">&nbsp;</td>
									  <!-- <td>
									    <a href="#" onclick="javascript:register();">
									      <img src='${pageContext.request.contextPath}/themes/sbi_default/img/wapp/register.png' title="Register" alt="Register" />
									    </a>                              
									  </td> -->
									    <div class="submit">
			                                <input type="text" value="<%=msgBuilder.getMessage("signup",request)%>" onclick="javascript:register();" />
			                                <p class="footerMsg"><%=msgBuilder.getMessage("yesAccount",request)%> <a href="${pageContext.request.contextPath}/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE">Login</a></p>                                
			                            </div>
									  </tr>
									</table>									
								   </td>

								</tr>
							   </table>
							</td>
						
						<td style="padding-top: 20px">
						</td>
					</tr>					

				</table>
			</div>
			</form>
	        </div>
  </body>
</html>
