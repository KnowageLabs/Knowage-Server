<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


 <%@ page language="java"
		 extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPage"
		 contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"
		 session="true"
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
		    
		  </tr>
		</table>
		
	</body>
</HTML>