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


%>
<head>
    <meta charset="UTF-8">
    <title><%=msgBuilder.getMessage("mfa.form.title")%></title>
    <link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "themes/sbi_default/img/favicon.ico")%>" />
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "node_modules/bootstrap/dist/css/bootstrap.min.css")%>">
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "node_modules/font-awesome/css/font-awesome.min.css")%>">
    
    <link rel='StyleSheet' href='<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>' type='text/css' />
    <style>
        .qr-section {
            text-align: center;
            margin-top: 20px;
        }
        .qr-section ul{
            list-style: none;
            display: flex;
            padding:0;
            justify-content: space-evenly;
            align-items: center;
            font-size: .8rem;
        }
        .qr-section ul li{
            padding: 8px;
            border: 1px solid #ccc;
        }
        .code-section {
            margin-top: 20px;
        }
        .code-section label{
            color: black;
            text-shadow: none;
        }
         .code-section input{
            margin-bottom:8px;
        }
        .secret {
            padding: 8px;
            background-color: #cccccc;
            border: 1px solid #bbb;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .secret span {
            font-weight: bold;
            font-size: 1.2rem;
            font-family: monospace;
        }
        .secret i{
            cursor: pointer;
        }
        .error {
            padding: 8px;
            border: 1px solid red;
            text-align: center;
            font-size: .8rem;
            background-color: #ffe2e2;
        }
        @media (max-width: 768px) {
            .qr-section ul {
                flex-direction: column;
                align-items: center;
            }
            .qr-section ul li {
                margin-bottom: 5px;
            }
        }
    </style>
    
    <script>
    function validateForm() {
        const code = document.forms["myForm"]["code"].value;
        if (code.trim() === "") {
            alert("Inserisci il codice one-time.");
            return false;
        }
        return true;
    }
    function copyToClipboard() {
        const secretElement = document.querySelector('.secret span');
        const secretText = secretElement.textContent;
        
        navigator.clipboard.writeText(secretText)
    }
    </script>
</head>
<body class="kn-login">
  		<div class="container-fluid" style="height:100%;">  
        	<div class="col-12 col-lg-5 offset-lg-7" style="height:100%;display:flex;align-items:center;justify-content:center;background-color:white;padding:20px;">
                <div class="col-8">
            	    <img id="profile-img" src='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/img/defaultTheme/logoCover.svg", "defaultTheme")%>' />
            

                    <form name="myForm" action="<%=contextName%>/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE" method="post" onsubmit="return validateForm()">
                        <input type="hidden" name="userID" value="<%= request.getAttribute("userID") %>">
                        <input type="hidden" name="password" value="<%= request.getAttribute("password") %>">

                        <% if (request.getAttribute("qrCode") != null) { %>
                            <input type="hidden" name="secret" value="<%= request.getAttribute("secret") %>">
                            <div class="qr-section">
                                <span><b><%=msgBuilder.getMessage("mfa.form.qrcode.msg.1")%></b></span>
                                <p><%=msgBuilder.getMessage("mfa.form.qrcode.msg.2")%></p>
                                <ul>
                                    <li>FreeOTP</li>
                                    <li>Microsoft Authenticator</li>
                                    <li>Google Authenticator</li>
                                </ul>
                                <img src="<%= request.getAttribute("qrCode") %>" width="150" height="150" alt="QR Code" />
                                <p><%=msgBuilder.getMessage("mfa.form.qrcode.msg.3")%></p>
                                <div class="secret"><span><%= request.getAttribute("secretFormat") %></span><i class="fa fa-regular fa-clipboard" title="Copy to clipboard" onclick="copyToClipboard()"></i></div>
                            </div>
                        <% } %>

                        
                        <% if (request.getAttribute("codeError") != null) { %>
                            <div class="error"><%= request.getAttribute("codeError") %></div>
                        <% } %>
                        

                        <div class="code-section">
                            <label for="code"><%=msgBuilder.getMessage("mfa.form.code")%>:</label>
                            <input type="text" id="code" name="code" class="form-control" placeholder="<%=msgBuilder.getMessage("mfa.form.code.message")%>" required autofocus>
                            <button class="btn btn-lg btn-primary btn-block btn-signin" type="submit"><%=msgBuilder.getMessage("mfa.form.submit")%></button>
                        </div>

                    </form>
                </div>
            </div>
        </div>

</body>
</html>