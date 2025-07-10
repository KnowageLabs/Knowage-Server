<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%
    IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
    String contextName = ChannelUtilities.getSpagoBIContextName(request);
%>
<head>
    <meta charset="UTF-8">
    <title><%=msgBuilder.getMessage("mfa.form.title")%></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 20px;
        }

        form {
            background-color: #ffffff;
            padding: 20px;
            border-radius: 8px;
            max-width: 400px;
            margin: auto;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }

        h1, p {
            text-align: center;
        }

        label {
            display: block;
            margin-top: 15px;
            font-weight: bold;
        }

        input[type="text"] {
            width: 100%;
            padding: 10px;
            margin-top: 5px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        input[type="submit"] {
            margin-top: 20px;
            width: 100%;
            padding: 10px;
            background-color: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        input[type="submit"]:hover {
            background-color: #0056b3;
        }

        .error {
            color: red;
            margin-top: 10px;
            text-align: center;
        }

        .qr-section {
            text-align: center;
            margin-top: 20px;
            font-size: 0.8em;
        }

        ul {
			list-style-position: outside; 
    		padding-left: 14px;
    		margin: 5px 0;
        }

        ul li {
             margin-bottom: 2px;
   			 line-height: 1.2;
    		 text-indent: -4px;      
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
    </script>
</head>
<body>

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
            <div><%= request.getAttribute("secretFormat") %></div>
        </div>
    <% } %>

    <% if (request.getAttribute("codeError") != null) { %>
        <div class="error"><%= request.getAttribute("codeError") %></div>
    <% } %>

    <label for="code"><%=msgBuilder.getMessage("mfa.form.code")%>:</label>
    <input type="text" id="code" name="code" placeholder="<%=msgBuilder.getMessage("mfa.form.code.message")%>">
    <input type="submit" name="Invio" value="<%=msgBuilder.getMessage("mfa.form.submit")%>">
</form>

</body>
</html>