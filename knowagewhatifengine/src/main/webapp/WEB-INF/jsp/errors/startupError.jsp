<%-- 
 
<%@page import="it.eng.spagobi.commons.utilities.StringUtilities"%>
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="it.eng.spagobi.commons.utilities.StringUtilities"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	Throwable t = null;

	String message = "No message";
	List hints = new ArrayList();
	String description = "No description available. Please check log files";

	ExecutionSession es = new ExecutionSession(request,
			request.getSession());
	t = (Throwable) es.getAttributeFromSession(EngineConstants.STARTUP_ERROR);
	if (t instanceof SpagoBIEngineRuntimeException) {
		SpagoBIEngineRuntimeException engineException = (SpagoBIEngineRuntimeException) t;
		message = engineException.getMessage();
		if (StringUtilities.isNotEmpty( engineException.getDescription() )) {
			description = engineException.getDescription();
		}
		hints = engineException.getHints();
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
		      <image height="150px"  src="../../img/error.gif"/>
		    </td>
		    
		    <td width="80%" valign="top">
		    
		    <H1>Error</H1>
		    <hr>
		    <H2><%= message %></H2>
		    <hr>
		    <br/>
		    <b>Description:</b> <%= description != null ? description : "" %> 
		    
		    <br/><br/>
		    <% if (hints != null && hints.size() > 0) {%>
		    <b>How to fix it:</b> <br>
		    <ul>
		    <% for(int i = 0; i < hints.size(); i++) {
		    		String hint = (String)hints.get(i);
		    %>
		    <li><%= hint%>
		    <%  }
		      }
		    %>
		    </ul>
		    

		  </tr>
		</table>
		
	</body>
</HTML>
