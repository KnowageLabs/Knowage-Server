<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="java.util.Enumeration"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>

<STYLE>
	
	.div_form_container {
    	border: 1px solid #cccccc;
    	background-color:#fafafa;
    	float: left;
    	margin: 5px;
    	font-size: 10pt;
		font-weight: normal;
	}
	
	.div_form_margin {
		margin: 5px;
		float: left;
	}
	
	.div_form_row {
		clear: both;
		padding-bottom:5px;
	}
	
	.div_form_label {	
		float: left;
		width:150px;
		margin-right:20px;
	}
	
	.div_form_label_large {	
		float: left;
		width:300px;
		margin-right:20px;
	}
	
	.div_form_field {
	}

    .div_form_message {	
		float: left;
		margin:20px;
	}
	
    .nowraptext {
    	white-space:nowrap;
    }
    
    .div_loading {
        width:20%;
    	position:absolute;
    	left:20%;
    	top:40%;
    	border:1px solid #bbbbbb;
    	background:#eeeeee;
    	padding-left:100px;padding-right:100px;
    	display:none;
    }
    
</STYLE>
<spagobi:error/>
<div class="div_form_container">
	<div class="div_form_margin" >
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.a" />
				</span>
			</div>
			<div class='div_form_field'>
				<input id="sendtoto" class='portlet-form-input-field' type="text" name="a" size="50" value=""  >
			    &nbsp;*
			</div>
		</div>
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.cc" />
				</span>
			</div>
			<div class='div_form_field'>
				<input id="sendtocc" class='portlet-form-input-field' type="text" name="cc" size="50" value="" >
			</div>
		</div>
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.object" />
				</span>
			</div>
			<div class='div_form_field'>
				<input id="sendtoobject" class='portlet-form-input-field' type="text" name="object" size="50" value=""  >
			</div>
		</div>
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.message" />
				</span>
			</div>
			<div class='div_form_field'>
			    <textarea id="sendtomessage" class='portlet-form-input-field' name='message' cols='40' rows='10'></textarea>
			</div>
		</div>
		<br>
		<div class="div_form_row" >
			
				 <span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.yourAccount" />
				</span>
			
		</div>
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.Login" />
				</span>
			</div>
			<div class='div_form_field'>
				<input id="sendtologin" class='portlet-form-input-field' type="text" name="login" size="50" value=""  >
			    
			</div>
		</div>
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.Password" />
				</span>
			</div>
			<div class='div_form_field'>
				<input id="sendtopwd" class='portlet-form-input-field' type="password" name="pwd" size="50" value="" >
				 
			</div>
		</div>
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "sbi.execution.sendTo.ReplyTo" />
				</span>
			</div>
			<div class='div_form_field'>
				<input id="replyto" class='portlet-form-input-field' type="text" name="replyto" size="50" value="" >
			</div>
		</div>
	</div>
</div>

<div>
	<div>
		<a style="text-decoration:none;" href='javascript:sendTo()' >
			<img width="32px" height="32px"
				src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/next.png", currTheme)%>'
				alt='<%=msgBuilder.getMessage("sbi.execution.send", "messages", request)%>'
				title='<%=msgBuilder.getMessage("sbi.execution.send", "messages", request)%>' />
		</a>
		<br/>
		<div id="messageSendToDiv" style="font-size:11px;font-family:arial;color:#074B88;"></div>
 	</div>
</div>



<script>

function sendTo() {
	var sendtoto = document.getElementById('sendtoto').value ;
        	  	  
	if( sendtoto == '' ) {
		Ext.MessageBox.WARNING;
		Ext.MessageBox.show({
           title: 'WARNING',
           msg: "<spagobi:message key = 'sbi.execution.sendTo.SendToMissing' />",
           buttons: Ext.MessageBox.OK
       });
       return;
	}
	var emailPat=/^(.+)@(.+)$/ ;
	var matchArray = sendtoto.match(emailPat)
	if (matchArray == null) {
		Ext.MessageBox.WARNING;
		Ext.MessageBox.show({
           title: 'WARNING',
           msg: "<spagobi:message key = 'sbi.execution.sendTo.SendToIncorrect' />",
           buttons: Ext.MessageBox.OK
       });
		return;
	}						 	  	  	
			
	var replyto = document.getElementById('replyto').value ;
	var sendtologin = document.getElementById('sendtologin').value ;
	var sendtopwd = document.getElementById('sendtopwd').value ;
    if( replyto != '' && ( sendtologin == '' || sendtopwd == '' )){ 
    	Ext.MessageBox.WARNING;
		Ext.MessageBox.show({
           title: 'WARNING',
           msg: "<spagobi:message key = 'sbi.execution.sendTo.MissingAccount' />",
           buttons: Ext.MessageBox.OK
       });
		return;
    }
    if( sendtologin != '' && ( replyto == '' || sendtopwd == '' )){ 
        Ext.MessageBox.WARNING;
		Ext.MessageBox.show({
           title: 'WARNING',
           msg: "<spagobi:message key = 'sbi.execution.sendTo.MissingAccount' />",
           buttons: Ext.MessageBox.OK
       });
		return;
    }
    if( sendtopwd != '' && ( replyto == '' || sendtologin == '' )){ 
    	Ext.MessageBox.WARNING;
		Ext.MessageBox.show({
           title: 'WARNING',
           msg:"<spagobi:message key = 'sbi.execution.sendTo.MissingAccount' />",
           buttons: Ext.MessageBox.OK
       });
		return;
   	}
      
    url="<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userId)%>";
    pars = "&ACTION_NAME=SEND_TO_ACTION&";
    <%
	Enumeration parKeys = request.getParameterNames();
	while(parKeys.hasMoreElements()) {
		String parkey = parKeys.nextElement().toString();
		if (parkey.equals("ACTION_NAME")) continue;
		if (parkey.equalsIgnoreCase(SsoServiceInterface.USER_ID)) continue;
		String parvalue = request.getParameter(parkey);
		%>
		pars += "&<%=parkey%>=<%=parvalue%>";
		<%
	}
	%>
      
	pars += "&to=" + document.getElementById('sendtoto').value;
	pars += "&cc=" + document.getElementById('sendtocc').value;
	pars += "&login=" + document.getElementById('sendtologin').value;
	pars += "&pwd=" + document.getElementById('sendtopwd').value;
	pars += "&object=" + document.getElementById('sendtoobject').value;
	pars += "&message=" + document.getElementById('sendtomessage').value;
	pars += "&replyto=" + document.getElementById('replyto').value;
	mstd = document.getElementById('messageSendToDiv');
	mstd.innerHTML = "<spagobi:message key="sbi.execution.waiting" />";
	Ext.Ajax.request({
		url: url,
		method: 'post',
		success: function (result, request) {
			response = result.responseText || "";
			showSendToResult(response);
		},
		params: pars,
		failure: somethingWentWrongSendTo
	});
}
 
function somethingWentWrongSendTo() {
	mstd = document.getElementById('messageSendToDiv');
	mess = getMessageFromCode(mstd);
	mstd.innerHTML = mess;
}
    
function showSendToResult(response) {
	mstd = document.getElementById('messageSendToDiv');
	mess = getMessageFromCode(response);
	mstd.innerHTML = mess;
}
    
function getMessageFromCode(messcode) {
	if(messcode=="10")
		return "<spagobi:message key="sbi.execution.send.ok" />";
	if(messcode=="20")
		return "<spagobi:message key="sbi.execution.send.error" />";
	if(messcode=="50")
		return "<spagobi:message key="sbi.execution.send.error" />";
}    
</script>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>