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
	
	String messageErrorToken = (String)request.getAttribute("messageErrorToken");
	String messageErrorChangePws = (String)request.getAttribute("messageErrorChangePws");
	String userId = (String)request.getAttribute("userId");

%>
<head>
    <meta charset="UTF-8">
    <title><%=msgBuilder.getMessage("signup.forgotPassword.changePassword.title")%></title>
    <link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "themes/sbi_default/img/favicon.ico")%>" />
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "node_modules/bootstrap/dist/css/bootstrap.min.css")%>">
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "node_modules/font-awesome/css/font-awesome.min.css")%>">
    
    <link rel='StyleSheet' href='<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>' type='text/css' />
    <style>
        .chpas-section {
            margin-top: 20px;
        }
        .chpas-section label{
            color: black;
            text-shadow: none;
        }
         .chpas-section input{
            margin-bottom:8px;
        }
        .chpas-error {
            font-size: 0.8rem;
            font-family: monospace;
            padding: 16px;
            margin:8px 0;
            background-color: #ffbdbd;
			border: 1px solid #a32c2c;
        }
    </style>
    
</head>
<body class="kn-login">
  		<div class="container-fluid" style="height:100%;">  
        	<div class="col-12 col-lg-5 offset-lg-7" style="height:100%;display:flex;align-items:center;justify-content:center;background-color:white;padding:20px;">
                <div class="col-8">
            	    <img id="profile-img" src='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/img/defaultTheme/logoCover.svg", "defaultTheme")%>' />
            
					<%if(messageErrorToken==null){ %>
                    <form name="myForm" action="<%=contextName%>/restful-services/signup/changePassword" method="post" onsubmit="return validateForm()">
                       <div class="chpas-section">
                       		<label for="chpas"><%=msgBuilder.getMessage("signup.forgotPassword.changePassword.title")%>:</label>
                             <input type="hidden" name="userId" value="<%=userId %>">
                            <input type="password" id="password" name="password" class="form-control" placeholder="<%=msgBuilder.getMessage("signup.form.password")%>" required>
		        			<input type="password" id="confirmPassword" name="confirmPassword" class="form-control" placeholder="<%=msgBuilder.getMessage("signup.form.confirmpassword")%>" required>
                       		<%if(messageErrorChangePws!=null){ %>
                       		<div class="chpas-error">
                       			<%=messageErrorChangePws%>
                       		</div>
                       		<%}%>
                       		<button class="btn btn-lg btn-primary btn-block btn-signin" type="submit"><%=msgBuilder.getMessage("signup.forgotPassword.submit")%></button>
                        </div>

                    </form>
                    <%} else {%>
                   <form name="myForm" action="<%=contextName%>/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE" method="post">
                    	<div class="chpas-error">
                            <%=messageErrorToken%>          
                        </div>
                        <button class="btn btn-lg btn-primary btn-block btn-signin" type="submit"><%=msgBuilder.getMessage("login")%></button>
                    </form>
                    <%}%>
                </div>
            </div>
        </div>

</body>
</html>