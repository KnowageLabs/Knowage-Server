<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ include file="/WEB-INF/jsp/analiticalmodel/execution/header.jsp"%>

<%
// built the url for the content recovering
String url = GeneralUtilities.getSpagoBIProfileBaseUrl(userId) + "&ACTION_NAME=GET_SNAPSHOT_CONTENT&" 
		+ SpagoBIConstants.SNAPSHOT_ID + "=" + snapshot.getId() + "&" + ObjectsTreeConstants.OBJECT_ID + "=" + obj.getId() + "&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";

//tries to get from the session the heigh of the output area
String heightArea = (String) aSessionContainer.getAttribute(SpagoBIConstants.HEIGHT_OUTPUT_AREA);
String heightStr = "";
if (heightArea == null || heightArea.trim().equals("")) {
%>
<!-- 
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
		alert(err.description + ' Cannot resize the document view area');
	}

	try {
		window.onload = SbiJsInitializer.initialize;
	} catch (err) {
		alert('Cannot execute javascript initialize functions');
	}
	</script>
	-->
	<script>
		
		pos<%=uuid%> = null; 
	
		function adaptSize<%=uuid%>Funct() {
				
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
  			iframeEl.style.height = heightFrame + 'px';
		}
	
		try {
			SbiJsInitializer.adaptSize<%=uuid%> = adaptSize<%=uuid%>Funct;
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

<div id="divIframe<%=uuid%>" style="width:98%;float:left;padding-left:2%;<%= heightStr %>">
	<iframe src="<%=url%>"
		style='display:inline;' 
		id='iframeexec<%=uuid%>' 
		name='iframeexec<%=uuid%>'
		frameborder=0  
		width='100%' >
	</iframe> 
</div>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>