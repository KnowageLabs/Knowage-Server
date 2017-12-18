<%@ page language="java" buffer="8kb" autoFlush="true" isThreadSafe="true" isErrorPage="false"  %> 
<%@ page import="java.util.*, java.util.regex.*, java.text.*, com.jamonapi.*, com.jamonapi.proxy.*, com.jamonapi.utils.*, com.fdsapi.*, com.fdsapi.arrays.*" %>  

<%@page contentType="text/html"%>

<%
String enableAll = request.getParameter("enableAll");

// Enable/Disable jamon as a whole and the proxy capability.  ResultSets will be disabled by default.
if ("Enable Monitoring!".equals(enableAll)) {
  MonitorFactory.enable();
  MonProxyFactory.enableAll(true);
  MonProxyFactory.enableResultSet(false); 

} else if ("Disable Monitoring!".equals(enableAll)) {
  MonitorFactory.disable();
  MonProxyFactory.enableAll(false);

} 
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/JAMonStyles.css"> 
<title>JAMon <%=MonitorFactory.getVersion()%> Menu - Support Pages</title>
</head>
<body>

<div align="center">
<a href="http://www.jamonapi.com"><img src="images/jamon_banner3.jpg" id="monLink" border="0" /></a>
</div>

<br><br>
<hr width="75%">  
<br><br>


<div align="center">
<form action="menu.jsp" method="post">  
    <input type="submit" name="enableAll" value="Enable Monitoring!" >
    <input type="submit" name="enableAll" value="Disable Monitoring!" >
</form>
</div>

<br><br>

<div align="center">
 <table border="1" class="layoutMain" cellpadding="2" cellspacing="0" width="450">
<tr align="left">
<th class="sectHead">JAMon <%=MonitorFactory.getVersion()%> Support Pages</th>
</tr>
<tr>
<td><a href="jamonadmin.jsp">JAMon Admin Page</a> - Manage the JAMon summary statistics (Currently 
<%=enabled("JAMon = ",MonitorFactory.isEnabled())%>, 
<%=enabled("SQL Summary = ",MonProxyFactory.isSQLSummaryEnabled())%>, 
<%=enabled("Exception Summary = ",MonProxyFactory.isExceptionSummaryEnabled())%>, 
<%=enabled("Interface = ",MonProxyFactory.isInterfaceEnabled())%>, 
<%=enabled("ResultSet = ",MonProxyFactory.isResultSetEnabled())%> 

)</td>


</tr>
<tr>
<td><a href="monmanage.jsp">Manage Monitor page </a> - Manage previously selected monitor. To select a monitor use <a href="jamonadmin.jsp">jamonadmin.jsp</a></td>
</tr>
<tr>
<td><a href="mondetail.jsp">View Listener Details</a> - View previously selected listener. To select a monitor listener use <a href="jamonadmin.jsp">jamonadmin.jsp</a></td>
</tr>
<tr>
<td><a href="sql.jsp">SQL Details page </a> - Manage the most recent <%=MonProxyFactory.getSQLBufferSize() %> SQL commands executed. (currently <%=enabled(MonProxyFactory.isSQLDetailEnabled())%>)</td>
</tr>
<tr>
<td><a href="exceptions.jsp">Exception Details page </a> - Manage the most recent <%=MonProxyFactory.getExceptionBufferSize() %> exceptions. (currently <%=enabled(MonProxyFactory.isExceptionDetailEnabled())%>)</td>
</tr>
<tr>
<td><a href="query.jsp">Generate Monitoring Data</a> - Generate JAMon data using a JDBC proxied connection. This is useful to run when learning JAMon.</td>
</tr></table>
</div>

<hr width="75%">  


<td><table border='0' align='center' width='25%'>
    <tr>
    <th nowrap>JAMon <%=MonitorFactory.getVersion()%></th>
    </tr>
</table></td>

</body>
</html>

<%!
private static String enabled(boolean isEnabled) {
   return isEnabled ? "enabled" : "disabled";
}

private static String enabled(String prefix, boolean isEnabled) {
   return prefix+enabled(isEnabled);
}



%>