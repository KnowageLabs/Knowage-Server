<%@ page language="java"
	extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	session="true"
	import="it.eng.spago.base.*,
                 it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page
	import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page
	import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
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
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.community.mapping.SbiCommunity"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>

<script type="text/javascript"
	src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/ext-all-debug.js' /></script>
<script type="text/javascript"
	src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/examples/ux/IFrame.js' /></script>
<script type="text/javascript"
	src='${pageContext.request.contextPath}/js/lib/ext-4.1.1a/ux/RowExpander.js' /></script>

<%
	String defaultOrganization = msgBuilder.getMessage(
			"profileattr.company", locale);
	String defaultName = msgBuilder.getMessage("profileattr.firstname",
			locale);
	String defaultSurname = msgBuilder.getMessage(
			"profileattr.lastname", locale);
	String defaultUsername = "Username";
	String defaultPassword = "Password";
	String defaultEmail = msgBuilder.getMessage("profileattr.email",
			locale);
	String defaultConfirmPwd = msgBuilder.getMessage("confirmPwd",
			locale);
	String defaultCaptcha = msgBuilder.getMessage(
			"signup.form.captcha", locale);
	String defaultLocation = msgBuilder.getMessage(
			"signup.form.location", locale);
	String defaultLanguage = msgBuilder.getMessage(
			"signup.form.language", locale);
	String defaultItalian = msgBuilder.getMessage(
			"signup.form.langItalian", locale);
	String defaultEnglish = msgBuilder.getMessage(
			"signup.form.langEnglish", locale);
	String defaultFrench = msgBuilder.getMessage(
			"signup.form.langFrench", locale);
	String defaultSpanish = msgBuilder.getMessage(
			"signup.form.langSpanish", locale);
	String defaultBirthday = msgBuilder.getMessage(
			"signup.form.birthday", locale);
	String defaultMan = msgBuilder.getMessage("signup.form.genderMan",
			locale);
	String defaultWoman = msgBuilder.getMessage(
			"signup.form.genderWoman", locale);
	String defaultCommunity = msgBuilder.getMessage(
			"signup.form.community", locale);
	String defaultShortBio = msgBuilder.getMessage(
			"signup.form.shortBio", locale);

	String msgConfirm = msgBuilder.getMessage(
			"signup.msg.confirmDelete", locale);
	String registrationSuccessMsg = msgBuilder.getMessage(
			"signup.msg.modifySuccess", locale);
	
	List comunities = (request.getAttribute("communities")==null)?new ArrayList():(List)request.getAttribute("communities");
	Map data = (request.getAttribute("data")==null)?new HashMap():(Map)request.getAttribute("data");
	String myCommunity = (data.get("community")==null)?"":(String)data.get("community");
	
%>
<script type="text/javascript">

Ext.ns("Sbi.config");
Sbi.config.loginUrl = "";

function cancel(){
	
	Ext.MessageBox.confirm(
			  "Warning",
			  "<%=msgConfirm%>",
			  
			  function(btn, text){
				  
				  if (btn=='yes') {
					  //Service Registry creation
					  var url = {
					    	host: '<%=request.getServerName()%>'
					    	, port: '<%=request.getServerPort()%>'
					    	, contextPath: '<%=request.getContextPath().startsWith("/")
										|| request.getContextPath().startsWith("\\") ? request
										.getContextPath().substring(1) : request.getContextPath()%>'
					    	, controllerPath: null // no cotroller just servlets   
					    };
					  
					  Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
						baseUrl: url
					    , baseParams: params
					  });
				
				
					this.services = [];
				
					//Adding a new service to the registry
					this.services["delete"]= Sbi.config.serviceRegistry.getRestServiceUrl({
						serviceName: 'signup/delete',
						baseParams: {}
					});
				
					    var form             = document.myForm;	
						var params = new Object();
						
					     Ext.Ajax.request({
							url: this.services["delete"],
							method: "POST",
							params: params,			
							success : function(response, options) {	
							
						    if(response != undefined  && response.responseText != undefined ) {
								if( response.responseText != null && response.responseText != undefined ){
							    var jsonData = Ext.decode( response.responseText );
							    if( jsonData.message != undefined && jsonData.message != null && jsonData.message == 'validation-error' ){
							      Sbi.exception.ExceptionHandler.handleFailure(response);
							    }else{
							     // Sbi.exception.ExceptionHandler.showInfoMessage('<%=registrationSuccessMsg%>', 'Saved OK', {});
							    	//redirect out of the container
									var logoutUrl = "${pageContext.request.contextPath}/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE";
									var sessionExpiredSpagoBIJSFound = false;
									try {
										var currentWindow = window;
										var parentWindow = parent;
										while (parentWindow != currentWindow) {
											if (parentWindow.sessionExpiredSpagoBIJS) {
												parentWindow.location = logoutUrl;
												sessionExpiredSpagoBIJSFound = true;
												break;
											} else {
												currentWindow = parentWindow;
												parentWindow = currentWindow.parent;
											}
										}
									} catch (err) {}
									
									if (!sessionExpiredSpagoBIJSFound) {
										window.location = '<%= GeneralUtilities.getSpagoBiContext() %>';
									}
							    }		
							  }		
							}
							else {
								
							  Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
							}
					    },
						scope: this,
						failure: Sbi.exception.ExceptionHandler.handleFailure
					  })
			 }}
			);
}
	
function modify() {

	
  //Service Registry creation
  var url = {
    	host: '<%=request.getServerName()%>'
    	, port: '<%=request.getServerPort()%>'
    	, contextPath: '<%=request.getContextPath().startsWith("/")
					|| request.getContextPath().startsWith("\\") ? request
					.getContextPath().substring(1) : request.getContextPath()%>'
    	, controllerPath: null // no cotroller just servlets   
    };
  
  Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
	baseUrl: url
    , baseParams: params
  });


this.services = [];

//Adding a new service to the registry
this.services["update"]= Sbi.config.serviceRegistry.getRestServiceUrl({
	serviceName: 'signup/update',
	baseParams: {}
});

    var form             = document.myForm;
	var myCommunity		 = '<%=myCommunity%>';
    var nome             = document.getElementById("nome").value;
    var cognome          = document.getElementById("cognome").value;
	var password         = document.getElementById("password").value;
	var confermaPassword = document.getElementById("confermaPassword").value;
    var email            = document.getElementById("email").value;
	var dataNascita      = document.getElementById("dataNascita").value;
	var indirizzo        = document.getElementById("indirizzo").value;
	var azienda          = document.getElementById("azienda").value;
	var biografia        = document.getElementById("biografia").value;
	var lingua           = document.getElementById("lingua").value;
	var username         = document.getElementById("username").value;
	
	var params = new Object();
	params.locale	   = '<%=locale%>';
	params.nome        = nome;
	params.cognome     = cognome;
	params.password    = password;
	params.confermaPassword = confermaPassword;
	params.email       = email;
	params.dataNascita = dataNascita;
	params.indirizzo   = indirizzo;
	params.azienda     = azienda;
	params.biografia   = biografia;
	params.lingua      = lingua;
	params.useCaptcha  = false;
	params.termini	   = true;
	params.username    = username;
	if (password == null || password == "")
		params.modify = true;
	
	var goOn = false;
	if (myCommunity !== "" && document.getElementById("aziendaIsChanged").value == "true"){
		Ext.MessageBox.confirm(
				  "Warning",
				  "<%=msgBuilder.getMessage("signup.msg.confirmUpdateComm", locale)%>",
				  function(btn, text){					  
					  if (btn=='yes') {
						  execUpdate(params);
						  return;
					  }
				  }
		);
	}else 
		goOn=true;
	
	if (goOn){
		execUpdate(params);
	}    
}

function execUpdate(params){
	
	 Ext.Ajax.request({
			url: this.services["update"],
			method: "POST",
			params: params,			
			success : function(response, options) {	
				
			    if(response != undefined  && response.responseText != undefined ) {
					if( response.responseText != null && response.responseText != undefined ){
				    var jsonData = Ext.decode( response.responseText );
				    if( jsonData.message != undefined && jsonData.message != null && jsonData.message == 'validation-error' ){
				      Sbi.exception.ExceptionHandler.handleFailure(response);
				    }else{
				      Sbi.exception.ExceptionHandler.showInfoMessage('<%=registrationSuccessMsg%>', 'Saved OK', {});
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
<script type="text/javascript"
	src='${pageContext.request.contextPath}/js/src/ext/sbi/service/ServiceRegistry.js' /></script>
<script type="text/javascript"
	src='${pageContext.request.contextPath}/js/src/ext/sbi/exception/ExceptionHandler.js' /></script>

<link id="extall" rel="styleSheet"
	href="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/resources/css/ext-all.css"
	type="text/css" />
<link id="theme-gray" rel="styleSheet"
	href="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/resources/css/ext-all-gray.css"
	type="text/css" />
<link id="spagobi-ext-4" rel="styleSheet"
	href="${pageContext.request.contextPath}/js/lib/ext-4.1.1a/overrides/resources/css/spagobi.css"
	type="text/css" />

<html>
<head>
<style media="screen" type="text/css">
body {
	background: #dedede; /* Old browsers */
	background: -moz-linear-gradient(top, #dedede 0%, #efefef 100%);
	/* FF3.6+ */
	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #dedede),
		color-stop(100%, #efefef) ); /* Chrome,Safari4+ */
	background: -webkit-linear-gradient(top, #dedede 0%, #efefef 100%);
	/* Chrome10+,Safari5.1+ */
	background: -o-linear-gradient(top, #dedede 0%, #efefef 100%);
	/* Opera 11.10+ */
	background: -ms-linear-gradient(top, #dedede 0%, #efefef 100%);
	/* IE10+ */
	background: linear-gradient(to bottom, #dedede 0%, #efefef 100%);
	/* W3C */
	filter: progid:DXImageTransform.Microsoft.gradient(  startColorstr='#dedede',
		endColorstr='#efefef', GradientType=0 ); /* IE6-9 */
}

td.login-label {
	font-family: Tahoma, Verdana, Geneva, Helvetica, sans-serif;
	font-size: 10 px;
	color: #7d7d7d;
}

a:link{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 14px;
	/*font-weight:bold;*/
	color: #F8A400;
	text-decoration:none;
}
a:visited{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 14px;
	color: #F8A400;
	/*font-weight:bold;*/
	text-decoration:none;
}
a:hover{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 14px;
	color: #F8A400;
	/*font-weight:bold;*/
	text-decoration:none;
}

.submit{clear:both;width:auto;padding-top:12px;text-align:center}
.submit input{border:0;height:42px;width:198px;padding:2px 31px 0;text-align:center;cursor:pointer;margin:0 0 20px;font-family:cabinregular,arial,helvetica,sans-serif;font-size:1.33em;font-weight:bold;line-height:100%;text-transform:uppercase;background:#F8A400;color:#fff;-webkit-border-radius:4px;-moz-border-radius:4px;border-radius:4px}
.submit input:hover{opacity:0.9;filter:alpha(opacity = 90)}

.deleteMsg {color:#193B54;}
.delete{font-size:1.08em;color:red}!
.delete a{color:red  !important}

</style>

<link rel="shortcut icon"
	href="<%=urlBuilder.getResourceLink(request, "img/favicon.ico")%>" />
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

	<form name="myForm" method="post">
		<input type="hidden" name="username" id="username" value="${data['username']}" />
		<div id="content" style="height: 100%">
			<div style="padding: 40px">
				<!--
		        	DO NOT DELETE THIS COMMENT
		        	If you change the tag table with this one  you can have the border of the box with the shadow via css
		        	the problem is that it doesn't work with ie	
		     		
		     		<table style="background: none repeat scroll 0 0 #fff; border-radius: 10px 10px 10px 10px;  box-shadow: 0 0 10px #888; color: #009DC3; display: block; font-size: 14px; line-height: 18px; padding: 20px;">
		        	 -->

				<table border="0" align="center"
					style="border-collapse: separate; background: none repeat scroll 0 0; border-radius: 5px 5px 5px 5px; box-shadow: 0px 0px 10px #888; -webkit-box-shadow: 0px 0px 10px #888; -moz-box-shadow: 0px 0px 10px #888; color: #009DC3; display: block; font-size: 14px; line-height: 18px; padding: 20px;">
					<tr>
						<td></td>
						<td><img
							src='${pageContext.request.contextPath}/themes/sbi_default/img/wapp/spagobi40logo.png'
							width='180px' height='51px' style="margin: 20px 0px" /></td>
						<td width='50px'></td>
						<td></td>
					</tr>
					<tr valign="top">
						<td width="120px">&nbsp;</td>
						<td width="350px">

							<table border="0">
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">*&nbsp;<%=defaultName%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><input id="nome" name="nome" type="text" size="25"
										class="login" value="${data['name']}"></td>
									<td></td>

								</tr>

								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">&nbsp;<%=defaultPassword%>:</td>
									<td width="25px">&nbsp;</td> 					
								</tr>
								<tr>
									<td valign="top"><input id="password" name="password"
										type="password" size="25" class="login">
									</td>
									<td></td> 									
								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">*&nbsp;<%=defaultEmail%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><input id="email" name="email" type="text" size="25"
										class="login" value="${data['email']}"></td>
									<td></td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultBirthday%>
										(dd/mm/yyyy):</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><input id="dataNascita" name="dataNascita" type="text"
										size="25" class="login" value="${data['birth_date']}" /></td>
									<td></td>
								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultLanguage%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><select class="login" name="lingua" id="lingua">
											<c:choose>
												<c:when test="${data['language'] == ''}">
													<option value="" selected="selected"></option>
												</c:when>
												<c:otherwise>
													<option value=""></option>
												</c:otherwise>
											</c:choose>
											<c:choose>
												<c:when test="${data['language'] == 'it_IT'}">
													<option value="it_IT" selected="selected"><%=defaultItalian%></option>
												</c:when>
												<c:otherwise>
													<option value="it_IT"><%=defaultItalian%></option>
												</c:otherwise>
											</c:choose>
											<c:choose>
												<c:when test="${data['language'] == 'en_US'}">
													<option value="en_US" selected="selected"><%=defaultEnglish%></option>
												</c:when>
												<c:otherwise>
													<option value="en_US"><%=defaultEnglish%></option>
												</c:otherwise>
											</c:choose>
											<c:choose>
												<c:when test="${data['language'] == 'fr_FR'}">
													<option value="fr_FR" selected="selected"><%=defaultFrench%></option>
												</c:when>
												<c:otherwise>
													<option value="fr_FR"><%=defaultFrench%></option>
												</c:otherwise>
											</c:choose>
											<c:choose>
												<c:when test="${data['language'] == 'es_ES'}">
													<option value="es_ES" selected="selected"><%=defaultSpanish%></option>
												</c:when>
												<c:otherwise>
													<option value="es_ES"><%=defaultSpanish%></option>
												</c:otherwise>
											</c:choose>

									</select></td>
									<td></td>
								</tr>

							<!-- 	<tr>
									<td colspan="2" height="30px">&nbsp;</td>
								</tr>

								<tr>
									<td colspan="2" height="30px">&nbsp;</td>
								</tr>
 							-->
							</table>
						</td>
						<td width='50px'></td>
						<td width="350px">
							<table border="0">

								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">*&nbsp;<%=defaultSurname%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><input id="cognome" name="cognome" type="text"
										size="25" class="login" value="${data['surname']}"></td>
									<td></td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left">&nbsp;<%=defaultConfirmPwd%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><input id="confermaPassword" name="confermaPassword" type="password"
										size="25" class="login" value=""></td>
									<td></td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultLocation%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><input id="indirizzo" name="indirizzo" type="text"
										size="25" class="login" value="${data['location']}" /></td>
									<td></td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultCommunity%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td>
										<!-- <input id="azienda" name="azienda" type="text"size="25" class="login" value="${data['community']}" />-->
										<div class="login" style="position:relative;width:200px;height:25px;border:0;padding:0;margin:0;">
											<select style="position:absolute;top:0px;left:0px;width:214px; height:25px;line-height:20px;margin:0;padding:0;" onchange="document.getElementById('azienda').value=this.options[this.selectedIndex].text;document.getElementById('aziendaIsChanged').value=true">											
												<option></option>	
												<%for(int i=0; i<comunities.size(); i++){
													SbiCommunity objComm = (SbiCommunity)comunities.get(i);
													
													if (objComm.getName() == myCommunity){%>
														<option  value="<%=objComm.getName()%>" selected><%=objComm.getName() %>  </option>	
												<%  } else {%>
														<option value="<%=objComm.getName()%>"><%=objComm.getName() %></option>
												<%	} 
												}%>																															
											</select>											
											<input type="text"  value="<%=myCommunity %>" name="azienda" placeholder="" id="azienda" style="position:absolute;top:1px;left:2px;width:183px;height:23px;border:0px;" onfocus="this.select()" onKeyPress="document.getElementById('aziendaIsChanged').value=true;">											
											<input type="hidden" name="aziendaIsChanged" id="aziendaIsChanged"> 									
										</div>
									</td>
									<td></td>

								</tr>
								<tr class='header-row-portlet-section'>
									<td class='login-label' width="90px" align="left"><%=defaultShortBio%>:
									</td>
									<td width="25px">&nbsp;</td>

								</tr>
								<tr>
									<td><textarea class="login" rows="5" cols="35"
											name="biografia" id="biografia">${data['short_bio']}</textarea>
									</td>
									<td></td>

								</tr>
								

							</table>
						</td>
					</tr>
				<!-- 	<tr>
						<td colspan="4" align="center"><a href="#"
							onclick="javascript:modify();"> <img
								src='${pageContext.request.contextPath}/themes/sbi_default/img/wapp/confirm_button.png'
								title="aggiorna" alt="aggiorna" />
						</a> <a href="#" onclick="javascript:cancel();"> <img
								src='${pageContext.request.contextPath}/themes/sbi_default/img/wapp/cancel_button.png'
								title="elimina" alt="elimina" />
						</a></td>

					</tr>
					 -->					
					 <tr>
						 <td colspan="4" align="center">
							<div class="submit">
		                              <input type="text" value="<%=msgBuilder.getMessage("modify",locale)%>" onclick="javascript:modify();"/>
		                              <c:if test="${data['userIn'] == data['username']}">
		                             	 <p  class="deleteMsg"><%=msgBuilder.getMessage("deleteAccount",locale)%> <a class="delete" href="#" onclick="javascript:cancel();"><%=msgBuilder.getMessage("delete",locale)%></a></p>
		                              </c:if>
		                    </div>
	                    </td>
					</tr>
					
				</table>
			</div>
		</div>
	</form>


</body>
</html>
