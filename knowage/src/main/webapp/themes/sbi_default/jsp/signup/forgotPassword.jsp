<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>


<%
    IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
    String contextName = ChannelUtilities.getSpagoBIContextName(request);

    String sbiMode = "WEB";
	IUrlBuilder urlBuilder = null;
	urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);
	
	String sendMail = (String)request.getAttribute("sendMail");

%>
<head>
    <meta charset="UTF-8">
    <title><%=msgBuilder.getMessage("signup.forgotPassword.title")%></title>
    <link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "themes/sbi_default/img/favicon.ico")%>" />
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "node_modules/bootstrap/dist/css/bootstrap.min.css")%>">
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "node_modules/font-awesome/css/font-awesome.min.css")%>">
    
    <link rel='StyleSheet' href='<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>' type='text/css' />
    <style>
        .mail-section {
            margin-top: 20px;
        }
        .mail-section label{
            color: black;
            text-shadow: none;
        }
         .mail-section input{
            margin-bottom:8px;
        }
        .mail-hint {
            font-size: 0.8rem;
            font-family: monospace;
            padding: 16px;
            border: 1px solid #43749e;
            background-color: #d2e7fa;
            margin-top:8px;
        }
    </style>
</head>
<body class="kn-login">
  		<div class="container-fluid" style="height:100%;">  
        	<div class="col-12 col-lg-5 offset-lg-7" style="height:100%;display:flex;align-items:center;justify-content:center;background-color:white;padding:20px;">
                <div class="col-8">
            	    <img id="profile-img" src='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/img/defaultTheme/logoCover.svg", "defaultTheme")%>' />
            
					<%if(sendMail==null){ %>
                    <form name="myForm" action="<%=contextName%>/restful-services/signup/forgotPasswordMail" method="post" onsubmit="return validateForm()">
                       <div class="mail-section">
                            <label for="mail"><%=msgBuilder.getMessage("signup.forgotPassword.mail")%>:</label>
                            <input type="text" id="mail" name="mail" class="form-control" placeholder="<%=msgBuilder.getMessage("signup.forgotPassword.mail.message")%>" required autofocus>
                            <button class="btn btn-lg btn-primary btn-block btn-signin" type="submit"><%=msgBuilder.getMessage("signup.forgotPassword.submit")%></button>
                            <div class="mail-hint"><%=msgBuilder.getMessage("signup.forgotPassword.message")%></div>
                        </div>
                    </form>
                    <%} else {%>
                    	<div class="mail-hint">
                    		<%=msgBuilder.getMessage("signup.forgotPassword.message1")%>
                        </div>
                    <%}%>
                </div>
            </div>
        </div>

</body>
</html>