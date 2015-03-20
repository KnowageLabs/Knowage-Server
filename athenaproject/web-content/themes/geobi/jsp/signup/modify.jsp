<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
         import="it.eng.spago.base.*,
                 it.eng.spagobi.commons.constants.SpagoBIConstants,
                  it.eng.spagobi.commons.utilities.urls.IUrlBuilder,
         		 it.eng.spagobi.commons.utilities.messages.IMessageBuilder"
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.community.mapping.SbiCommunity"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>  


<% 
	String defaultOrganization = msgBuilder.getMessage("profileattr.company",locale); 
	String defaultName = msgBuilder.getMessage("profileattr.firstname",locale);
	String defaultSurname = msgBuilder.getMessage("profileattr.lastname",locale);
	//String defaultUsername = msgBuilder.getMessage("username",locale);
	//String defaultPassword = msgBuilder.getMessage("password",locale);
	String defaultUsername = "Username";
	String defaultPassword = "Password";
	String defaultEmail = msgBuilder.getMessage("profileattr.email",locale);
	String defaultConfirmPwd = msgBuilder.getMessage("confirmPwd",locale);         
	String confirmDelete = msgBuilder.getMessage("signup.msg.confirmDelete",locale);    
	
	String msgConfirm = msgBuilder.getMessage(
			"signup.msg.confirmDelete", locale);
	String registrationSuccessMsg = msgBuilder.getMessage(
			"signup.msg.modifySuccess", locale);

	Map data = (request.getAttribute("data")==null)?new HashMap():(Map)request.getAttribute("data");
	String myCommunity = (data.get("community")==null)?"":(String)data.get("community");
%> 

<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/home40/standard.css",currTheme)%>'/>

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

function changefield(el){
    document.getElementById(el+"box").innerHTML = "<input id=\""+el+"\" type=\"password\" name=\""+el+"\" />";
    document.getElementById(el).focus();
 
}
	
function modify() {

	
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
this.services["update"]= Sbi.config.serviceRegistry.getRestServiceUrl({
	serviceName: 'signup/update',
	baseParams: {}
});

    var form             = document.myForm;
    var myCommunity		 = '<%=myCommunity%>';
    var nome             = document.getElementById("nome").value;
    var cognome          = document.getElementById("cognome").value;
    var username         = document.getElementById("username").value;
	var password         = document.getElementById("password").value;
	var confermaPassword = document.getElementById("confermaPassword").value;
    var email            = document.getElementById("email").value;
    var azienda          = document.getElementById("azienda").value;
	
	var params = new Object();
	params.locale	   = '<%=locale%>';
	params.useCaptcha  = "false";
	params.nome        = nome;
	params.cognome     = cognome;
	params.username    = username;
	params.password    = password;
	params.confermaPassword = confermaPassword;
	params.email       = email;
	params.azienda     = azienda;
		
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
	/*
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
	*/
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

<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/home40/standard.css",currTheme)%>'/>
<html>

  <body>
    <main class="main main-maps-list main-list" id="main">
        	<div class="aux">
            	<div class="reserved-area-container">
            		<h1><%=msgBuilder.getMessage("modifyAccount",locale)%></h1>
                    <form name="myForm" method="post"  class="reserved-area-form">
                        <fieldset>
                            <div class="field organization">
                                <label for="organization">Company</label>
                                <input type="text" name="azienda" id="azienda" value="${data['community']}" onKeyPress="document.getElementById('aziendaIsChanged').value=true;" onfocus="if(value=='<%=defaultOrganization%>') value = ''" onblur="if (this.value=='') this.value = '<%=defaultOrganization%>'"/>
                                <input type="hidden" name="aziendaIsChanged" id="aziendaIsChanged">
                            </div>
                            <div class="field name">
                                <label for="name">Name</label>
                                <input type="text" name="nome" id="nome" value="${data['name']}" onfocus="if(value=='<%=defaultName%>') value = ''" onblur="if (this.value=='') this.value = '<%=defaultName%>'"/>
                            </div>
                            <div class="field surname">
                                <label for="surname">Cognome</label>
                                <input type="text" name="cognome" id="cognome" value="${data['surname']}" onfocus="if(value=='<%=defaultSurname%>') value = ''" onblur="if (this.value=='') this.value = '<%=defaultSurname%>'"/>
                            </div>
                            <div class="field username">
                                <label for="username">Username</label>
                                <input type="text" name="username" id="username" value="${data['username']}" readonly />
                            </div>
                            <div class="field email">
                                <label for="email">Email</label>
                                <input type="text" name="email" id="email" value="${data['email']}" onfocus="if(value=='<%=defaultEmail%>') value = ''" onblur="if (this.value=='') this.value = '<%=defaultEmail%>'"/>
                            </div>
                            <div class="field password" id="passwordbox">
                                <label for="password">Password</label>
                                <input type="text" name="password" id="password" value="Password" onfocus="changefield('password');" onblur="if (this.value=='') this.value = '<%=defaultPassword%>'" />
                            </div>
                            <div class="field confirm" id="confermaPasswordbox">
                                <label for="confirm">Confirm Password</label>
                                <input type="text" name="confermaPassword" id="confermaPassword" value="<%=defaultConfirmPwd%>" onfocus="changefield('confermaPassword');" onblur="if (this.value=='') this.value = '<%=defaultConfirmPwd%>'" />
                            </div>
                            <div class="submit">
                                <input type="text" value="<%=msgBuilder.getMessage("modify",locale)%>" onclick="javascript:modify();"/>
                          
								 <c:if test="${data['userIn'] == data['username']}">
	                                <p class="delete"><%=msgBuilder.getMessage("deleteAccount",locale)%> <a href="#" onclick="javascript:cancel();"><%=msgBuilder.getMessage("delete",locale)%></a></p>
	                             </c:if>
                            </div>
                        </fieldset>
                    </form>
                </div>
            </div>
        </main>
  </body>
</html>
