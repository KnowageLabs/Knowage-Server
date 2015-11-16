<%@ page language="java" buffer="8kb" autoFlush="true" isThreadSafe="true" isErrorPage="false"  %>
<%@ page import="java.util.*, java.util.regex.*, java.text.*, com.jamonapi.*, com.jamonapi.proxy.*, com.jamonapi.utils.*, com.fdsapi.*, com.fdsapi.arrays.*" %>

<%

FormattedDataSet fds=new FormattedDataSet();

// Assign request parameters to local variables.
String action    = getValue(request.getParameter("monitormgmt"),"");
MonKey key=null;

int keyNum=0;
if (request.getParameter("key")==null  && session.getAttribute("monKey")!=null) {
//  keyNum=Integer.parseInt((String)session.getAttribute("keyNum"));
  key=(MonKey) session.getAttribute("monKey");
} else if (request.getParameter("key")!=null) {
  keyNum=getNum(request.getParameter("key"), "1")-1;
  key=getMonKey((MonitorComposite) session.getAttribute("monitorComposite"), keyNum);
  session.setAttribute("monKey", key);
} 

String listenerType = "value";
if (request.getParameter("listenertype")==null  && session.getAttribute("listenerType")!=null)
  listenerType=(String)session.getAttribute("listenerType");
else if (request.getParameter("listenertype")!=null) {
  listenerType=(String)request.getParameter("listenertype");
} 
  
 

//session.setAttribute("keyNum",new Integer(keyNum).toString());
//session.setAttribute("monKey", key);
session.setAttribute("listenerType", listenerType);

executeAction(action, key);
addListeners(request, key);
removeListeners(request, key);

Monitor mon=null;
boolean hasListeners=false;
boolean enabled=false;
if (MonitorFactory.exists(key)) {
  mon=MonitorFactory.getMonitor(key);
  hasListeners=mon.hasListeners();
  enabled=mon.isEnabled();
}

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<META http-equiv="Content-Type" content="text/html"; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/JAMonStyles.css">
<title>Manage Monitor - <%=key%> - <%=now()%></title>
<script type="text/javascript">
<!--
// Row highlighter
var objClass

function rollOnRow(obj, txt) {
    objClass = obj.className
    obj.className = "rowon";
    //obj.title = txt;
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
    newWin = window.open('jamonhelp.htm', 'helpWin', 'resizable=no,scrollbars=yes,height=550,width=450,screenX=100,screenY=100');
    if (newWin.opener == null) newWin.opener = self;
}
// -->
</script>

</head>
<body>

<div align="center">



<table bgcolor='#DCE2E8'>
<th>
<fieldset>
    <legend>Monitor Management <%= (enabled) ? "(enabled)" : "(disabled)" %></legend>
<form class="even" action="monmanage.jsp" method="post">

<input type="radio" name="monitormgmt" value="enable"> Enable
<input type="radio" name="monitormgmt" value="disable"> Disable
<input type="radio" name="monitormgmt" value="reset"> Reset
<br><br>
<input type="submit" name="monitoraction" title="Take selected action on a Monitor" value="Submit" >


</form>


<form class="even" action="monmanage.jsp" method="post">


<fieldset>
<legend>Listener Management <%= (hasListeners) ? "(has listeners)" : "(no listeners)" %>

</legend>
<br>


<input type="radio" name="listenertype" value="value" onClick="this.form.action='monmanage.jsp'; this.form.submit();" <%=checked("value", listenerType)%> > Value
<input type="radio" name="listenertype" value="max" onClick="this.form.action='monmanage.jsp'; this.form.submit();" <%=checked("max", listenerType)%> > Max
<input type="radio" name="listenertype" value="min" onClick="this.form.action='monmanage.jsp'; this.form.submit();" <%=checked("min", listenerType)%> > Min
<input type="radio" name="listenertype" value="maxactive" onClick="this.form.action='monmanage.jsp'; this.form.submit();" <%=checked("maxactive", listenerType)%> > MaxActive
<br><br><hr>





<table>
<th>
Available:<br>
   <%=fds.getMultiSelectListBox(getAvailableListeners() , new String[]{""},5)%>
<br>
&nbsp;

</th>

<th>
    <input type="submit" name="addlistener" title="Add Listener" value="--->" onClick="this.form.action='monmanage.jsp'; this.form.submit();"><br><br>
    <input type="submit" name="removelistener" title="Remove Listener" value="<---" onClick="this.form.action='monmanage.jsp'; this.form.submit();">
</th>


<th>
Current:<br>
    <%=fds.getMultiSelectListBox(getCurrentListeners(key, listenerType) , new String[]{""}, 5)%>
<br>

<input type="submit" name="displaylistener" title="Display Listener" value="Display" onClick="this.form.action='mondetail.jsp'; this.form.submit();" >
</th>




</table>





</fieldset>
</form>



</fieldset>

</th>
</table>


<div class="monkey"><b><%=key%></b></div>
</div>

<br>
<div align="center" style="padding-top : 30px;">
<hr width="580" align="center" />
<a href="menu.jsp">Home</a> | <a href="jamonadmin.jsp">JAMonAdmin</a> | Manage Mon | <a href="mondetail.jsp">View Listener</a> | <a href="sql.jsp">SQL Details</a> | <a href="exceptions.jsp">Exception Details</a>
<hr width="580" align="center" />
</div>

<br>
<div align="left">
Monitor: <%=mon%>
<br>

<br><br>
<td><table border='0' align='center' width='25%'>
    <tr>
    <th nowrap><a href="http://www.jamonapi.com"><img src="images/jamon_small.jpg" id="monLink" border="0" /></a></th>
    <th nowrap>JAMon <%=MonitorFactory.getVersion()%></th>
    <th nowrap><a href="http://www.fdsapi.com"><img height=40 width=80 src="images/fds_logo_small.jpg" id="monLink" border="0" /></a></th>
    </tr>
</table></td>

<%
  String debugStr="";
  if (request.getParameter("debug")!=null) 
    session.setAttribute("debugjamon","true");

   if (session.getAttribute("debugjamon")!=null)
    debugStr=fds.getFormattedDataSet(new String[]{"parameter","value"}, Utils.getParameters(request), "htmlTable");
%>

<%=debugStr%>

</body>
</html>
<%!



private String checked(String compareType, String listenerType) {
   return (compareType.equalsIgnoreCase(listenerType)) ? "checked" : "";
}

private MonKey getMonKey(MonitorComposite mc, int keyNum) {
   if (mc==null)
     return null;

   Monitor[] monitors=mc.getMonitors();

   if (monitors==null || monitors[keyNum]==null)
     return null;

   return monitors[keyNum].getMonKey();

   
}

private void addListeners(HttpServletRequest request, MonKey key) {
  String addListener=request.getParameter("addlistener");
  String listenerType=request.getParameter("listenertype");
  if (addListener!=null && listenerType!=null) {
   
   String[] add=request.getParameterValues("availablelistener");
   int rows=(add==null) ? 0 : add.length;

   if (MonitorFactory.exists(key)) {
     Monitor mon=MonitorFactory.getMonitor(key);
     for (int i=0;i<rows;i++) {

     if (!mon.getListenerType(listenerType).hasListener(add[i])) {
         JAMonListener listener=JAMonListenerFactory.get(add[i]);
         mon.getListenerType(listenerType).addListener(listener);

        }
     }

   }

  }
}

private void removeListeners(HttpServletRequest request, MonKey key) {
  String removeListener=request.getParameter("removelistener");
  String listenerType=request.getParameter("listenertype");
  if (removeListener!=null && listenerType!=null) {
   
   String[] remove=request.getParameterValues("currentlistener");
   int rows=(remove==null) ? 0 : remove.length;

   if (MonitorFactory.exists(key)) {
     Monitor mon=MonitorFactory.getMonitor(key);
     for (int i=0;i<rows;i++) {
        if (mon.getListenerType(listenerType).hasListener(remove[i])) {
          mon.getListenerType(listenerType).removeListener(remove[i]);
        }
     }

   }

  }

}

private ResultSetConverter getAvailableListeners() {
   return new ResultSetConverter(JAMonListenerFactory.getHeader(), JAMonListenerFactory.getData()).execute("select col0 as availablelistener,col0 as availabillistenerdisp from array order by col0 asc");
 
}

private ResultSetConverter getCurrentListeners(MonKey key, String listenerType) {

   if (MonitorFactory.exists(key)  && MonitorFactory.getMonitor(key).getListenerType(listenerType).hasListeners()) {
     return getCurrentListeners(MonitorFactory.getMonitor(key).getListenerType(listenerType).getListener());
   }
   
   return new ResultSetConverter(new String[]{"none"}, new Object[][]{{"No Listeners"}}).execute("select col0 as currentlistener,col0 as currentlistenerdisp from array order by col0 asc");
 
}

private ResultSetConverter getCurrentListeners(JAMonListener listener) {
  if (listener==null)
    return null;

  // don't need to wrap listener if it is already a compositelistener, but
  // it makes the following code easier.
  CompositeListener l=new CompositeListener();
  return new ResultSetConverter(l.getHeader(), l.addListener(listener).getData()).execute("select col0 as currentlistener,col0 as currentlistenerdisp from array order by col0 asc");

}

// Format time String in current locale.
private String now() {
  return LocaleContext.getDateFormatter().format(new Date());
}





// if the value is null then return the passed in default else return the value
private static String getValue(String value, String defaultValue) {
  return (value==null || "".equals(value.trim())) ? defaultValue: value;
}


// convert arg to an int or return the default
private static int getNum(String value, String defaultValue) {

  String retValue=getValue(value, defaultValue);
  char[] digits=retValue.toCharArray();
  boolean isDigit=true;
  for (int i=0;i<digits.length;i++) {
     if (!Character.isDigit(digits[i]))
       isDigit=false;
  }

  if (!isDigit)
    retValue=defaultValue;

  return Integer.parseInt(retValue);
}


private static void executeAction(String action, MonKey key) {
if (MonitorFactory.exists(key)) {
 if ("Reset".equalsIgnoreCase(action))
    MonitorFactory.getMonitor(key).reset();
 else if ("Enable".equalsIgnoreCase(action)) 
    MonitorFactory.getMonitor(key).enable();
 else if ("Disable".equalsIgnoreCase(action))  
    MonitorFactory.getMonitor(key).disable();
 }
}





%>