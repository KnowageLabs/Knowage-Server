<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
 <%@ page language="java"
		 extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPage"
		 contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"
		 session="true"
		 errorPage="/jsp/errors/error.jsp"
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPage"%>
<%@page import="it.eng.spagobi.engines.qbe.*"%>
<%@page import="it.eng.spago.error.*"%>
<%@page import="java.util.*"%>
<%@page import="it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException"%>






<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	Exception exception = null;

	String message = "No message";
	List hints = new ArrayList();
	String description = "no description available";

	try {
		EMFErrorHandler errorHandler = getErrorHandler(request);
		Iterator it = errorHandler.getErrors().iterator();
		EMFInternalError error = (EMFInternalError)it.next();	
		exception = error.getNativeException();
				
		if(exception instanceof SpagoBIEngineRuntimeException) {
			SpagoBIEngineRuntimeException engineException = (SpagoBIEngineRuntimeException)exception;
			description = engineException.getDescription();
			hints = engineException.getHints();
		}
	} catch(Throwable t) {
		t.printStackTrace();
		message = t.getClass().getName() + " : " +  t.getMessage();
	}
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>




<HTML>
	<HEAD>
		<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<TITLE>Service Error</TITLE>
	</HEAD>
	<body>

		<table cellspacing="20px">
		  <tr>
		    <td width="20%" valign="top">
		      <image height="150px"  src="../img/error.gif"/>
		    </td>
		    
		    <td width="80%" valign="top">
		    
		    <H1>Error</H1>
		    <hr>
		    <H2><%=exception.getMessage() %></H2>
		    <hr>
		    <br/>
		    <b>Description:</b> <%=description %> 
		    
		    <br/><br/>
		    <b>How to fix it:</b> <br>
		    <ul>
		    <% if (hints == null || hints.size() == 0) {%>
		    
		    <%} else { 
		    	for(int i = 0; i < hints.size(); i++) {
		    		String hint = (String)hints.get(i);
		    %>
		    <li><%= hint%>
		    <%  }
		      }
		    %>
		    </ul>
		    
		    <br>
		    If none of these possible fixes work, please ask on <a href="http://forge.objectweb.org/forum/forum.php?forum_id=862">Spagobi Forum</a> for futher help
		    
		    </td>
		  </tr>
		</table>
		
	</body>
</HTML>
