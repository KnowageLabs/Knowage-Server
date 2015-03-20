<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
         import="it.eng.spago.base.*"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>


<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("ViewBuilderModule"); 
	List containerList = (List)moduleResponse.getAttribute("CONTAINERS_LIST"); 
	Integer viewHeight = (Integer)moduleResponse.getAttribute("VIEW_HEIGHT");
	Integer viewWidth = (Integer)moduleResponse.getAttribute("VIEW_WIDTH");
	Iterator containerIter = containerList.iterator();
%>

<html>
  <head>
    <title>SpagoBI Home</title>
    
    <script type="text/javascript">
	     var djConfig = {isDebug: false, debugAtAllCosts: false};
    </script>
    <script type="text/javascript" src="/spagobi/js/dojo/dojo.js"></script>
    <script language="JavaScript" type="text/javascript">
    	dojo.require("dojo.widget.TabContainer");
		dojo.require("dojo.widget.LinkPane");
		dojo.require("dojo.widget.ContentPane");
		dojo.require("dojo.widget.LayoutContainer");
    	dojo.require("dojo.widget.FisheyeList");
    	dojo.hostenv.writeIncludes();
    </script>

    
    <style>
      body {
	       font-family: Arial, Helvetica, sans-serif;
	       padding: 0;
	       margin: 0;
      }
      .dojoTabPaneWrapper {
  		padding : 10px 10px 10px 10px;
	  }
    </style> 
    
  </head>



  <body>
	<%
		while(containerIter.hasNext()) {
			SourceBean containerSB = (SourceBean)containerIter.next();
			String widthcont = (String)containerSB.getAttribute("width");
			String heightcont = (String)containerSB.getAttribute("height");
			String top = (String)containerSB.getAttribute("top");
			String left = (String)containerSB.getAttribute("left");
			String idCont = (String)containerSB.getAttribute("id");
			String style = (String)containerSB.getAttribute("style");
			if(style==null) {
				style="";
			}

			List tabs = containerSB.getAttributeAsList("TAB");
			Iterator iterTab = tabs.iterator();
	%>
			<div id="mainTabContainer_<%=idCont%>" dojoType="TabContainer" style="position:absolute;width:<%=widthcont%>; height:<%=heightcont%>;left:<%=left%>;top:<%=top%>;<%=style%>" selectedTab="tab1_<%=idCont%>" >
	<%
			int prog = 1;
			while(iterTab.hasNext()) {
				SourceBean tabSB = (SourceBean)iterTab.next();
				String title = (String)tabSB.getAttribute("title");
				String idTab = (String)tabSB.getAttribute("id");
				SourceBean documentSB = (SourceBean)tabSB.getAttribute("SBIDOCUMENT");
				String docLabel = (String)documentSB.getAttribute("documentLabel");
				String docParameters = (String)documentSB.getAttribute("parameters");
				String link = "/spagobi/servlet/AdapterHTTP?PAGE=DirectExecutionPage" + 
						      "&DOCUMENT_LABEL=" + docLabel + 
						      "&DOCUMENT_PARAMETERS="+docParameters;
				// calculate width iframe
				String widthContStr = widthcont;
				if(widthContStr.endsWith("%")) {
					widthContStr = widthContStr.substring(0, widthContStr.length()-1);
				}
				Integer widthContInt = new Integer(widthContStr);
				int widthiframe = (viewWidth.intValue() * widthContInt.intValue()) / 100;
				widthiframe = widthiframe - 30;
				
				// calculate height iframe
				String heightContStr = heightcont;
				if(heightContStr.endsWith("%")) {
					heightContStr = heightContStr.substring(0, heightContStr.length()-1);
				}
				Integer heightContInt = new Integer(heightContStr);
				int heightframe = (viewHeight.intValue() * heightContInt.intValue()) / 100;
				heightframe = heightframe - 50;
				
	%>
				<div id="tab<%=prog%>_<%=idCont%>" dojoType="ContentPane" label="<%=title%>">
					<iframe id="<%=idCont%>_<%=idTab%>" width="<%=widthiframe%>" width="<%=heightframe%>" src="<%=link%>" frameborder="0" scrolling="no"></iframe>
        		</div>
	<%
				prog ++;
			}
	%>			
			</div>			
	<%
		}
	%>
  </body>
  
</html>







