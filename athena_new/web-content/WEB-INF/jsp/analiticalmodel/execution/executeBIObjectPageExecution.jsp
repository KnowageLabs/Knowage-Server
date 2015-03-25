<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
  
<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="it.eng.spagobi.analiticalmodel.document.service.ExecuteBIObjectModule"%>

<%
ExecutionInstance instanceO = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
String modalityO = instanceO.getExecutionModality();
if (modalityO != null && modalityO.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION)) {
	SourceBean moduleResponseO = (SourceBean) aServiceResponse.getAttribute("ExecuteBIObjectModule");
	Map documentParametersMap = (Map) moduleResponseO.getAttribute(ObjectsTreeConstants.REPORT_CALL_URL);
	Map executionParameters = new HashMap();
	if (documentParametersMap != null) executionParameters.putAll(documentParametersMap);
	executionParameters.put(SpagoBIConstants.SBI_CONTEXT, GeneralUtilities.getSpagoBiContext());
	//executionParameters.put(SpagoBIConstants.SBI_BACK_END_HOST, GeneralUtilities.getSpagoBiHostBackEnd());
	executionParameters.put(SpagoBIConstants.SBI_HOST, GeneralUtilities.getSpagoBiHost());
	executionParameters.put("SBI_EXECUTION_ID", instanceO.getExecutionId());
	executionParameters.put("EXECUTION_CONTEXT", "DOCUMENT_COMPOSITION");
	// Auditing
	AuditManager auditManager = AuditManager.getInstance();
	Integer executionAuditId = auditManager.insertAudit(instanceO.getBIObject(), null, userProfile, instanceO.getExecutionRole(), instanceO.getExecutionModality());
	// adding parameters for AUDIT updating
	if (executionAuditId != null) {
		executionParameters.put(AuditManager.AUDIT_ID, executionAuditId.toString());
	}
	String uuid = instanceO.getExecutionId();
	%>
	
	<%-- div with "wait while loading" message: it will disappear when iframe below will be loaded --%>
	<div id="divLoadingMessage<%= uuid %>" style="display:block;text-align:left;">
		<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/analiticalmodel/loading.gif", currTheme)%>' />
		<spagobi:message key='sbi.execution.pleaseWait'/>
	</div>
	
	<iframe id="iframeexec<%=uuid%>" name="iframeexec<%=uuid%>" src="" style="width:100%;height:100%" frameborder="0" >
	</iframe>
	
	<form name="formexecution<%=uuid%>" id='formexecution<%=uuid%>' method="get"
         	      action="<%=instanceO.getBIObject().getEngine().getUrl()%>"
         	      target='_self'>
	<%
	java.util.Set keys = executionParameters.keySet();
	Iterator iterKeys = keys.iterator();
	while(iterKeys.hasNext()) {
		String key = iterKeys.next().toString();
		String value = executionParameters.get(key).toString();
		%>
		<input type="hidden" name="<%=key%>" value="<%=value%>" />
		<%
	}
	%>
	</form>
	
	<script>
	document.getElementById('formexecution<%=uuid%>').submit();
	</script>
	<%
} else {
%>
<%@ include file="/WEB-INF/jsp/analiticalmodel/execution/header.jsp"%>
<%
// tries to get the heigh of the output area
String heightArea = ChannelUtilities.getPreferenceValue(aRequestContainer, "HEIGHT_AREA", "");
String heightStr = "";
if (heightArea == null || heightArea.trim().equals("")) {
%>
<%-- Script for iframe automatic resize (by mitico Fisca (Luca Fiscato)) --%>
<%--
<script>
		
	function adaptSize<%= uuid %>Funct() {
		// evaluates the iframe current height
		iframeEl = document.getElementById('iframeexec<%= uuid %>');
		offsetHeight = 0;
		clientHeight = 0;
		if(isIE5) {
			offsetHeight = iframeEl.contentWindow.document.body.scrollHeight;
			clientHeight = iframeEl.clientHeight;
		}
		if(isIE6) {
			offsetHeight = iframeEl.contentWindow.document.body.scrollHeight;
			clientHeight = iframeEl.clientHeight;
		}
		if(isIE7) {
			offsetHeight = iframeEl.contentWindow.document.body.scrollHeight;
			clientHeight = iframeEl.clientHeight;
		}
		if(isMoz) {
			offsetHeight = iframeEl.contentWindow.document.body.offsetHeight;
			clientHeight = iframeEl.clientHeight;
		}
		// adjusts current iframe height
		if (offsetHeight != clientHeight + 40) {
			heightFrame = offsetHeight + 40;
			iframeEl.style.height = heightFrame + 'px';
		}
		// saves the current iframe height into a variable
		iframeHeight<%= uuid %> = heightFrame;
	}
	
	try {
		SbiJsInitializer.adaptSize<%=uuid%> = adaptSize<%=uuid%>Funct;
    } catch (err) {
		alert('Cannot resize the document view area');
	}
	</script>
	--%>
	<script>
		
		pos<%=uuid%> = null; 
	
		function adaptSize<%=uuid%>Funct() {
		
			if (window != parent) {
				// case when document is executed inside main iframe in SpagoBI Web application
				heightMainIFrame = 0;
				// calculates the iframe height
				if(isIE5()) { heightMainIFrame = document.body.clientHeight; }
				if(isIE6()) { heightMainIFrame = document.body.clientHeight; }
				if(isIE7()) { heightMainIFrame = document.body.clientHeight; }
				if(isMoz()) { heightMainIFrame = innerHeight; }
				// minus a fixed size (header height)
				heightExecIFrame = heightMainIFrame - 70;
				iframeEl = document.getElementById('iframeexec<%=uuid%>');
				iframeEl.style.height = heightExecIFrame + 'px';
				return;
			}
			
			// calculate height of the visible area
			heightVisArea = 0;

			if(isIE5()) { heightVisArea = top.document.body.clientHeight; }
			if(isIE6()) { heightVisArea = top.document.body.clientHeight; }
			if(isIE7()) { heightVisArea = top.document.documentElement.clientHeight }
			if(isMoz()) { heightVisArea = top.innerHeight; }
	
			// get the frame div object
			diviframeobj = document.getElementById('divIframe<%=uuid%>');
			// find the frame div position
			pos<%=uuid%> = findPos(diviframeobj);	
						
			// calculate space below position frame div
			spaceBelowPos = heightVisArea - pos<%=uuid%>[1];
			// set height to the frame
			iframeEl = document.getElementById('iframeexec<%=uuid%>');
			iframeEl.style.height = spaceBelowPos + 'px';
	
	    	// to give time to the browser to update the dom (dimension of the iframe)
		  	setTimeout("adaptSize<%=uuid%>_2Part()", 250);
		}
	
	  	function adaptSize<%=uuid%>_2Part() {
        
        	// calculate height of the win area and height footer
			heightWinArea = 0;
  			heightFooter = 0;
  			if(isIE5()) {
  				heightWinArea = document.body.scrollHeight;
  				heightFooter = heightWinArea - heightVisArea;
  			}
  			if(isIE6()) {
  				heightWinArea = document.body.scrollHeight;
  				heightFooter = heightWinArea - heightVisArea;
  			}
  			if(isIE7()) {
  				heightWinArea = document.body.offsetHeight;
  				heightFooter = heightWinArea - heightVisArea;
  			}
  			if(isMoz()) {
  				heightWinArea = document.body.offsetHeight;
  				heightFooter = (heightWinArea - heightVisArea);
  			}	 
  	
  			// calculate height of the frame
  			heightFrame = heightVisArea - pos<%=uuid%>[1] - heightFooter;
  			iframeEl = document.getElementById('iframeexec<%=uuid%>');
  			if (heightFrame <= 0) heightFrame = 600;
  			iframeEl.style.height = heightFrame + 'px';
		}

		
		function hideLoadingMessage<%=uuid%>Funct() {
			document.getElementById('divLoadingMessage<%= uuid %>').style.display = 'none';
		}

		try {
			SbiJsInitializer.adaptSize<%=uuid%> = adaptSize<%=uuid%>Funct;
			SbiJsInitializer.hideLoadingMessage<%=uuid%> = hideLoadingMessage<%=uuid%>Funct;
	    } catch (err) {
			alert('Cannot resize the document view area');
		}
		
		try {
			window.onload = SbiJsInitializer.initialize;
		} catch (err) {
			alert('Cannot execute javascript initialize functions');
		}
		
	</script>
<%
} else {
	heightStr = "height:"+heightArea+"px;";
}
%>

<%-- div with "wait while loading" message: it will disappear when window is loaded thanks to SbiJsInitializer.hideLoadingMessage property --%>
<div id="divLoadingMessage<%= uuid %>" style="display:block;text-align:left;">
	<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/analiticalmodel/loading.gif", currTheme)%>' />
	<spagobi:message key='sbi.execution.pleaseWait'/>
</div>

<%-- Start execution iframe --%>
<div id="divIframe<%= uuid %>" style="width:100%;overflow=auto;border: 0;display:inline;<%= heightStr %>">
	<iframe id="iframeexec<%= uuid %>" name="iframeexec<%= uuid %>" src="<%= StringEscapeUtils.escapeHtml(GeneralUtilities.getUrl(obj.getEngine().getUrl(), executionParameters)) %>&EXECUTION_ID=<%= uuid %>" style="width:100%;height:300px;z-index:0;" frameborder="0" >
	</iframe>
</div>

<%-- End execution iframe --%>

<%-- start cross navigation scripts --%>
<%
Map crossNavigationParameters = new HashMap();
//crossNavigationParameters.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
crossNavigationParameters.put(ObjectsTreeConstants.ACTION, SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);
crossNavigationParameters.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.EXEC_CROSS_NAVIGATION);
crossNavigationParameters.put("EXECUTION_FLOW_ID", executionFlowId);
crossNavigationParameters.put(ObjectsTreeConstants.MODALITY, modality);
crossNavigationParameters.put("SOURCE_EXECUTION_ID", uuid);
crossNavigationParameters.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "TRUE");
String crossNavigationUrl = urlBuilder.getUrl(request, crossNavigationParameters);
%>

<form id="crossNavigationForm<%= uuid %>" method="post" action="<%= crossNavigationUrl %>" style="display:none;">
	<input type="hidden" id="targetDocumentLabel<%= uuid %>" name="<%= ObjectsTreeConstants.OBJECT_LABEL %>" value="" />
	<input type="hidden" id="targetDocumentParameters<%= uuid %>" name="<%= ObjectsTreeConstants.PARAMETERS %>" value="" />
</form>

<script>
function execCrossNavigation(windowName, label, parameters) {
	var uuid = windowName.substr('iframeexec'.length);
	document.getElementById('targetDocumentLabel' + uuid).value = label;
	document.getElementById('targetDocumentParameters' + uuid).value = parameters;
	document.getElementById('crossNavigationForm' + uuid).submit();
}
</script>
<%-- end cross navigation scripts --%>


<%-- start refresh Scripts --%>
<%
Integer refreshSeconds=obj.getRefreshSeconds();
if(refreshSeconds!=null && refreshSeconds.intValue()>0){
Integer refreshConvert=new Integer(refreshSeconds.intValue()*1000);
%>

<script type="text/javascript">

  
  function doRefresh() {
    var iframe = document.getElementById("iframeexec<%=uuid%>");
    iframe.src = iframe.src;
    setTimeout("doRefresh()",<%=refreshConvert%>);
  }
  
  setTimeout('doRefresh()', <%=refreshConvert%>);
   
</script>
<%} %>
<%-- end refresh Scripts --%>

<% } %>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
