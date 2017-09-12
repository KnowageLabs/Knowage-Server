<%@ page language="java" buffer="8kb" autoFlush="true" isThreadSafe="true" isErrorPage="false"  %>
<%@ page import="java.sql.*, javax.sql.*, java.util.*, java.text.*, javax.naming.*, com.jamonapi.*, com.jamonapi.utils.*, com.jamonapi.proxy.*, com.fdsapi.*, com.fdsapi.arrays.*" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"> 
<html>
<head>
<META http-equiv="Content-Type" content="text/html"; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/JAMonStyles.css">
<title>JAMon Test Queries</title>
<script type="text/javascript">
<!--
// Row highlighter
var objClass

function rollOnRow(obj, txt) {
    objClass = obj.className
    obj.className = "rowon";
    obj.title = txt;
}

function rollOffRow(obj) {
    obj.className = objClass;
}

function selectAll(obj, numRows) {
    state = (obj.checked) ? true : false;

    for (var i = 1; i < numRows + 1; i ++) {
        currRow = eval("obj.form.row_" + i);
        currRow.checked = state;
    }
}

function helpWin() {
    newWin = window.open('JAMonHelp.htm', 'helpWin', 'resizable=no,scrollbars=yes,height=550,width=450,screenX=100,screenY=100');
    if (newWin.opener == null) newWin.opener = self;
}
// -->
</script>

</head>
<body>

<br><br>
<div align="center">
<form action="query.jsp" method="post">  
   <input type="submit" name="generateData" value="Generate Data!">
 </form>

<form action="query.html" method="get">  
   <input type="submit" name="showCode" value="Show Code!">
</form>

</div>


<br><br>
<hr>

<%
boolean generateData=(request.getParameter("generateData")==null) ? false : true;

if (generateData) {
%>

<%


FormattedDataSet fds=new FormattedDataSet();
// Load db driver to be monitored. Although the example uses HSQLDB, any JDBC Connection will do.
Class.forName("org.hsqldb.jdbcDriver");

// monitor table accesses.  Accesses to these tables will show up in the JAMon report
List tables=new ArrayList();
tables.add("SYSTEM_TYPEINFO");
tables.add("SYSTEM_TABLES");
MonProxyFactory.setMatchStrings(tables);
  
// Connect to the database and monitor the returned Connection.  That is all you have to do!!!
// All SQL, Exceptions and method calls on JDBC interfaces will now be monitored!
Connection conn = MonProxyFactory.monitor(DriverManager.getConnection("jdbc:hsqldb:.",  "sa", ""));
Statement st=conn.createStatement();
ResultSet rs=st.executeQuery("select * from SYSTEM_TYPEINFO where LOCAL_TYPE_NAME IN ('INTEGER', 'DECIMAL', 'TINYINT') order by 1 desc"); 	  


// The formattedDataSet is another API of mine that renders TabularData among other things.        
String html="";
html=fds.getFormattedDataSet(new ResultSetConverter(rs), "htmlTable");                                       
rs.close();
st.close();

// Monitor the PreparedStatement.  Note the SQL Detail report will show how many times the PreparedStatement 
// was reused.
PreparedStatement ps=conn.prepareStatement("select * from SYSTEM_TYPEINFO where LOCAL_TYPE_NAME=?");
ps.setString(1, "INTEGER");

rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();
rs = ps.executeQuery();

// create html for the last resultSet only
String html1="";
html1=fds.getFormattedDataSet(new ResultSetConverter(rs), "htmlTable");      	  


// Run and Monitor another couple queries
st=conn.createStatement();
rs=st.executeQuery("select * from SYSTEM_TABLES"); 
String html2="";
html2=fds.getFormattedDataSet(new ResultSetConverter(rs), "htmlTable");      	  

rs=st.executeQuery("select * from SYSTEM_USERS"); 
String html3="";
html3=fds.getFormattedDataSet(new ResultSetConverter(rs), "htmlTable");    

// Throw an exception and show that it is also monitored in jamonadmin.jsp and exceptions.jsp
// Note also even though the catch block is empty it will show up in these pages.
try { 
 // get a query to throw an Exception.  If enabled will show in jamon report and sql details.
 st.executeQuery("select * from i_do_not_exist");  

} catch (Exception e) {} 
 	  
                            

conn.close();

// Show that that MonProxy also works with ANY interface.  In this case an inner class is used.
MyInterface myObj=(MyInterface) MonProxyFactory.monitor(new MyObject());

// method calls will show in the jamon report
myObj.myOpen();
myObj.myClose(); 


%>

<p><b>You can see the results from these queries in the various JAMon support pages.</b></p>

Query=select * from SYSTEM_TYPEINFO where LOCAL_TYPE_NAME IN ('INTEGER', 'DECIMAL', 'TINYINT') order by 1 desc
<%=html%>

<br>
Query=select * from SYSTEM_TYPEINFO where LOCAL_TYPE_NAME=?
<%=html1%>

<br>
Query=select * from SYSTEM_TABLES
<%=html2%>

<br>
Query=select * from SYSTEM_USERS
<%=html3%>

<%
} // end if (generateData)

%>

<br>
<div align="center" style="padding-top : 30px;">
<hr width="580" align="center" />
<a href="menu.jsp">Home</a> | <a href="jamonadmin.jsp">JAMonAdmin</a> | <a href="monmanage.jsp">Manage Mon</a> | <a href="mondetail.jsp">View Listener</a> |<a href="sql.jsp">SQL Buffer</a> | <a href="exceptions.jsp">Exceptions</a>
<hr width="580" align="center" />
</div>

<br><br>
<td><table border='0' align='center' width='25%'>
    <tr>
    <th nowrap><a href="http://www.jamonapi.com"><img src="images/jamon_small.jpg" id="monLink" border="0" /></a></th>
    <th nowrap>JAMon <%=MonitorFactory.getVersion()%></th>
    <th nowrap><a href="http://www.fdsapi.com"><img height=40 width=80 src="images/fds_logo_small.jpg" id="monLink" border="0" /></a></th>
    </tr>
</table></td>



</body>
</html>

<%!


	

  // interface and class to show interface monitoring work on a custom class.
  public interface MyInterface {
     public int myOpen();
     public int myClose();
    
  }


  private static class MyObject implements MyInterface {
     public int myOpen(){
        return 1;
     }

     public int myClose(){
        return 1;
     }


  } 

%>
