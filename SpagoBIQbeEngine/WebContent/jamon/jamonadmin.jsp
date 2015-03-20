<%@ page language="java" buffer="8kb" autoFlush="true" isThreadSafe="true" isErrorPage="false"  %>
<%@ page import="java.util.*, java.util.regex.*, java.text.*, com.jamonapi.*, com.jamonapi.proxy.*, com.jamonapi.utils.*, com.fdsapi.*, com.fdsapi.arrays.*" %>

<%

FormattedDataSet fds=new FormattedDataSet();

// Set formatting rules per the requests Locale (as opposed to the servers locale).
// This will format data per the users preference (note this sets it for the given thread/servlet)
LocaleContext.setLocale(request.getLocale());

// Assign request parameters to local variables.
String action    = getValue(request.getParameter("action"),"Refresh");
String monProxyAction = getValue(request.getParameter("monProxyAction"),"No Action");

String outputType= getValue(request.getParameter("outputTypeValue"),"html");
String formatter = getValue(request.getParameter("formatterValue"), "#,###");
String arraySQL  = getValue(request.getParameter("ArraySQL"),"");
String sortOrder = getValue(request.getParameter("sortOrder"), "asc");
int sortCol      = getNum(request.getParameter("sortCol"), "2");
int textSize     = getNum(request.getParameter("TextSize"), "0");
String highlightString=getValue(request.getParameter("highlight"),"");

// Assign defaults to RangeName and displayTypeValue if they weren't part of the request.
String displayType=getValue(request.getParameter("displayTypeValue"), "BasicColumns");
String rangeName=getValue(request.getParameter("RangeName"),"AllMonitors");

// AllMonitors can't display ranges so allways change displayType to BasicColumns in this case
displayType=(rangeName.equalsIgnoreCase("AllMonitors")) ? "BasicColumns" : displayType;

// Assign defaults for arraysql.  If nothing is provided use 'select * from array'.  If the first word is not 
// 'select' assume they want to do a like on the first column (label).
String arraySQLExec = getValue(arraySQL,"select * from array");


if (arraySQLExec.trim().toLowerCase().startsWith("select")) 
  arraySQLExec=arraySQLExec;// noop all is ok full select entered
else if (arraySQLExec.trim().toLowerCase().startsWith("where")) 
   arraySQLExec="select * from array "+arraySQL;// where clause entered:  where hits>100 and total<50000
else
   arraySQLExec="select * from array where col1 like '"+arraySQL+"'";

arraySQLExec = (arraySQLExec.trim().toLowerCase().startsWith("select")) ? arraySQLExec : "select * from array where col1 like '"+arraySQL+"'";

// Build the request parameter query string that will be part of every clickable column.
String query="";
query+="&displayTypeValue="+displayType;
query+="&RangeName="+rangeName;
query+="&outputTypeValue="+outputType;
query+="&formatterValue="+java.net.URLEncoder.encode(formatter);
query+="&ArraySQL="+java.net.URLEncoder.encode(arraySQL);
query+="&TextSize="+textSize;
query+="&highlight="+highlightString;   


executeAction(action);
enableMonProxy(monProxyAction);


Map map=new HashMap();
// used for html page
map.put("sortPageName", "jamonadmin.jsp");
map.put("query", query);
map.put("imagesDir","images/");


// used for xml1 page.
map.put("rootElement", "JAMonXML");


String outputText;
MonitorComposite mc=MonitorFactory.getComposite(rangeName);
session.setAttribute("monitorComposite",mc);


if (!MonitorFactory.isEnabled())
  outputText="<div align='center'><br><br><b>JAMon is currently disabled.  To enable monitoring you must select 'Enable'</b></div>";
else if (!mc.hasData())
  outputText="<div align='center'><br><br><b>No data was returned</b></div>";
else {
  ResultSetConverter rsc;  // contains the header, and data after ArraySQL is applied.  This is a thin wrapper for arrays.

  if (displayType.equalsIgnoreCase("BasicColumns") || rangeName.equalsIgnoreCase("AllMonitors")) 
     rsc=getResultSetConverter(mc.getBasicHeader(), mc.getBasicData(), arraySQLExec);
  else if (displayType.equalsIgnoreCase("RangeColumns")) 
     rsc=getResultSetConverter(mc.getDisplayHeader(), mc.getDisplayData(), arraySQLExec);
  else if (displayType.equalsIgnoreCase("AllColumns")) 
     rsc=getResultSetConverter(mc.getHeader(), mc.getData(), arraySQLExec);
  else 
     rsc=getResultSetConverter(mc.getBasicHeader(), mc.getBasicData(), arraySQLExec);

  if (rsc.isEmpty())
    outputText="<div align='center'><br><br><b>No data was returned</b></div>";
  else {
   // rsc=rsc.execute("select rowNum() as Modify, * from array");
    ArrayConverter ac=getArrayConverter(rsc.getMetaData(), formatter, textSize, highlightString, false);
    fds.setArrayConverter(ac);

    if ("xml".equalsIgnoreCase(outputType)) {
      rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
      outputText=fds.getFormattedDataSet(rsc, map, "xml1");
    } else if  ("csv".equalsIgnoreCase(outputType)) {
       rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
       outputText=fds.getFormattedDataSet(rsc, map, "csv");
    } else if ("excel".equalsIgnoreCase(outputType) || "spreadsheet".equalsIgnoreCase(outputType)) {
      rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
      outputText=fds.getFormattedDataSet(rsc, map, "basicHtmlTable");
    }
    else  {
        ac=getArrayConverter(rsc.getMetaData(), formatter, textSize, highlightString, true);
        fds.setArrayConverter(ac);
        outputText=fds.getSortedText(rsc.getMetaData(), rsc.getResultSet(), map, sortCol, sortOrder, getJAMonTemplate(fds));
    }
  }
}
%>

<%
if ("html".equalsIgnoreCase(outputType)) {
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<META http-equiv="Content-Type" content="text/html"; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/JAMonStyles.css">
<title>JAMon - Administration/Reporting - <%=now()%></title>
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

<!-- arraySQLExec=<%=arraySQLExec%>-->

<form action="jamonadmin.jsp" method="post">

<table border="0" cellpadding="1" cellspacing="0" align="center">
<tr>
<td><h2 style="color:#03487F;">JAMon - Administration/Reporting - <%=now()%> - <a href="menu.jsp">Home</a></h2></td>
</tr>
<tr>
<td><table class="layoutmain" border="0" cellpadding="4" cellspacing="0" width="750" align="left">
    <tr class="sectHead">
    <th>JAMon Action</th>
    <th>Mon Proxy Action</th>
    <th>Output</th>
    <th>Range/Units</th>
    <th>Display Columns</th>
    <th>Cell Format</th>
    <th>Filter (optional)</th>
    <th>Highlight (optional)</th>  
    <th>Text Display Size (optional)</th>     
    <th align="right"><a href="javascript:helpWin();" style="color:#C5D4E4;">Help</a></th>
    </tr>
    <tr class="even">
    <th><%=fds.getDropDownListBox(actionHeader, actionBody, "")%></th>
    <th><%=fds.getDropDownListBox(monProxyHeader, getMonProxyBody() , "")%></th>
    <th><%=fds.getDropDownListBox(outputTypeHeader, outputTypeBody, outputType)%></th>
    <th><%=fds.getDropDownListBox(MonitorFactory.getRangeHeader(), getRangeNames(), rangeName)%></th>
    <th><%=fds.getDropDownListBox(displayTypeHeader, displayTypeBody, displayType)%></th>
    <th><%=fds.getDropDownListBox(formatHeader, formatBody, formatter)%></th>
    <th><input type='text' name='ArraySQL' value="<%=arraySQL%>" size="45"></th>
    <th><input type='text' name='highlight' value="<%=highlightString%>" size="20"></th>     
    <th><input type='text' name='TextSize' value="<%=(textSize<=0) ? "" : ""+textSize%>" size="15"></th>
    <td><input type="submit" name="actionSbmt" value="Go !" ></td>
    </tr>
</table></td>
</tr>
<tr>
<td><table border="0" cellpadding='0' cellspacing='0' align="center">
    <tr>
    <td><%=outputText%></td>
    </tr>
</table></td>
</tr>
</table>

</form>



<br>
<div align="center" style="padding-top : 30px;">
<hr width="580" align="center" />
<a href="menu.jsp">Home</a> | JAMonAdmin | <a href="monmanage.jsp">Manage Mon</a> | <a href="mondetail.jsp">View Listener</a> | <a href="sql.jsp">SQL Details</a> | <a href="exceptions.jsp">Exception Details</a>
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

<%
} else if ("xml".equalsIgnoreCase(outputType)) {
%>
<?xml version="1.0"?>
<%=outputText%>
<%
} else {
  response.setContentType("application/vnd.ms-excel");
%>


<%=outputText%>



<%
}
%>

<%!

String[] actionHeader={"action","actionDisplay"};
Object[][] actionBody={
                 {"Refresh", "Refresh"}, 
                 {"Reset", "Reset"}, 
	         {"Enable","Enable"}, 
	         {"Disable","Disable"}, 
                };

String[] monProxyHeader={"monProxyAction","monProxyActionDisplay"};
private static Object[][] getMonProxyBody() {

   String enableInfo="No Action (currently="+
      (MonProxyFactory.isSQLSummaryEnabled() ? "T" : "F")+
      (MonProxyFactory.isExceptionSummaryEnabled() ? "T" : "F")+
      (MonProxyFactory.isInterfaceEnabled() ? "T" : "F")+
      (MonProxyFactory.isResultSetEnabled() ? "T" : "F")+")";

   String sql=enabledMessage(MonProxyFactory.isSQLSummaryEnabled())+"SQL";
   String exceptions=enabledMessage(MonProxyFactory.isExceptionSummaryEnabled())+"Exceptions";
   String interfaces=enabledMessage(MonProxyFactory.isInterfaceEnabled())+"Interfaces";
   String resultSet=enabledMessage(MonProxyFactory.isResultSetEnabled())+"ResultSet";

   return new Object[][] {
                 {"No Action", enableInfo}, 
                 {"Enable All", "Enable All"}, 
                 {"Disable All", "Disable All"}, 
	         {sql,sql}, 
	         {exceptions,exceptions}, 
	         {interfaces,interfaces}, 
	         {resultSet,resultSet}, 
                };
}


private static String enabledMessage(boolean isEnabled) {
  return isEnabled ? "Disable " : "Enable ";
}

String[] displayTypeHeader={"displayTypeValue","displayType"};
Object[][] displayTypeBody={
                 {"RangeColumns", "Basic/Range Cols"}, 
                 {"BasicColumns", "Basic Cols Only"}, 
	           {"AllColumns","All Cols"}, 
                };

String[] outputTypeHeader={"outputTypeValue","outputType"};
Object[][] outputTypeBody={
                 {"html", "HTML"}, 
                 {"xml", "XML"}, 
                 {"csv", "Comma Separated"}, 
	              {"excel","MS Excel"}, 
                };


Template jamonTemplate;

String[] formatHeader={"formatterValue","formatterDisplay"};
Object[][] formatBody= {
                       {"#,###","#,###"},
                       {"#,###.#","#,###.#"},
                       {"#,###.##","#,###.##"},
                       {"#,###.###","#,###.###"},
                       {"#,###.####","#,###.####"},
                       {"raw","No Format"},  
                      };

// Format time String in current locale.
private String now() {
  return LocaleContext.getDateFormatter().format(new Date());
}




private synchronized Template getJAMonTemplate(FormattedDataSet fds) {
   // start from sortedHTMLTable template and add some jamon display capabilities (highlighting and descriptive text appearing where the mouse is)
   if (jamonTemplate==null) {
     jamonTemplate=fds.getTemplate("sortedHTMLTable").copy();

     // highlighting for odd and even rows of the JAMon report.  Affects coloring, 
     // and what happens when the rows is highlighted with the mouse.
     String odd="Odd==   <tr class='odd' onMouseOver='rollOnRow(this, \"\")' onMouseOut='rollOffRow(this)'>\n";// label was removed from rollover due to red hightlighting messing it up
     String even="Even==   <tr class='even' onMouseOver='rollOnRow(this, \"\")' onMouseOut='rollOffRow(this)'>\n";
     jamonTemplate.initialize("BODY_ROW_PREFIX",0,0,"Type==Alternating "+odd+" "+even);
     jamonTemplate.initialize("<!--body_cell_data 0,1--><a href='monmanage.jsp?key=##1'><IMG src='images/monmodify.gif' border=0></a><!--body_cell_data 0,1-->");
  
}

   return jamonTemplate;
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


private static void executeAction(String action) {
 if ("Reset".equals(action))
    MonitorFactory.reset();
 else if ("Enable".equals(action)) 
    MonitorFactory.setEnabled(true);
 else if ("Disable".equals(action))  
    MonitorFactory.setEnabled(false);

}

// Enable/Disable jamon summary stats for MonProxyFactory
private static void enableMonProxy(String monProxyAction) {
if ("Enable All".equals(monProxyAction)) {
  MonProxyFactory.enableSQLSummary(true);
  MonProxyFactory.enableExceptionSummary(true);
  MonProxyFactory.enableInterface(true);
  MonProxyFactory.enableResultSet(true);
 
} else if ("Enable SQL".equals(monProxyAction))
  MonProxyFactory.enableSQLSummary(true);
else if ("Enable Exceptions".equals(monProxyAction))  
  MonProxyFactory.enableExceptionSummary(true);
else if ("Enable Interfaces".equals(monProxyAction))  
  MonProxyFactory.enableInterface(true);
else if ("Enable ResultSet".equals(monProxyAction))  
  MonProxyFactory.enableResultSet(true);

else if ("Disable All".equals(monProxyAction)) {
  MonProxyFactory.enableSQLSummary(false);
  MonProxyFactory.enableExceptionSummary(false);
  MonProxyFactory.enableInterface(false);
  MonProxyFactory.enableResultSet(false);
} else if ("Disable SQL".equals(monProxyAction))
  MonProxyFactory.enableSQLSummary(false);
else if ("Disable Exceptions".equals(monProxyAction))  
  MonProxyFactory.enableExceptionSummary(false);
else if ("Disable Interfaces".equals(monProxyAction))  
  MonProxyFactory.enableInterface(false);
else if ("Disable ResultSet".equals(monProxyAction))  
  MonProxyFactory.enableResultSet(false);
}


private static ResultSetConverter getResultSetConverter(String[] header, Object[][] data, String arraySQLExec) {
     ResultSetConverter rsc=new ResultSetConverter(header, data).execute("select rowNum() as Modify, * from array");
     ArraySQL asql=new ArraySQL(rsc.getMetaData(), arraySQLExec );
     ArrayConverter ac=new ArrayConverter(rsc.getMetaData());

     // replace all booleans with strings as you can't sort booleans (they don't support Comparable interface for some reason)
     Map map=new HashMap();
     map.put(new Boolean(true), "true");
     map.put(new Boolean(false), "false");

     ac.setDefaultConverter(new ConverterMapValue(map));
     asql.setArrayConverter(ac);

     rsc = new ResultSetConverter(rsc.getMetaData(), asql.convert(rsc.getResultSet()));
     //MonitorFactory.add("cellCount","count",rsc.getColumnCount()*rsc.getRowCount());
     return rsc;
}

private static Map getDefaultsMap() {
  Map map=new HashMap();

  Double max=new Double(-Double.MAX_VALUE);
  Double min=new Double(Double.MAX_VALUE);
  Date noDate=new Date(0);

  map.put(min,"");
  map.put(max,"");
  map.put(noDate,"");

  return map;
}


private static ArrayConverter getArrayConverter(String[] header, String pattern, int textSize, String highlightMe, boolean isHtml) {
  if (pattern==null || "".equals(pattern))
    pattern="#,###";
  else if ("raw".equalsIgnoreCase(pattern)) {
    ArrayConverter ac=new ArrayConverter();
    ac.setDefaultConverter(new NullConverter());
    return ac;
  }

  // used to format the data in the report
  ArrayConverter ac=new ArrayConverter(header);
  // replace the normal extreme values of default min,max, and acccess date with an empty default.
  // If the key is in the data stream the value will be used instead of the key.
  Map map=getDefaultsMap();

  DecimalFormat decimalFormat=LocaleContext.getFloatingPointFormatter();
  decimalFormat.applyPattern(pattern);
  DateFormat dateFormat=LocaleContext.getDateFormatter();

  // Set the converter to take action on data passed to the FormattedDataSet
  ac.setDefaultConverter(new ConverterNumToString(decimalFormat, new ConverterDateToString(dateFormat, new ConverterMapValue(map, new ConverterEscapeChars()))));
  Converter lastConverter=new ConverterEscapeChars();
  if (isHtml)
    lastConverter=new ConverterRegExp("\n","\n<br>", new ConverterEscapeChars());

  ac.setTypeConverter(String.class, new TruncateString(textSize,highlightMe,"#00ff99;",lastConverter));
  ac.setColConverter("Modify", new NullConverter());
  return ac;
}


// This version of the FormattedDataSet requires a value as well as the display value.  The same column is used for both 
// below.
private static Object[][] getRangeNames() {
   Object[][] range=MonitorFactory.getRangeNames();
   Object[][] data=new Object[range.length][];
   for (int i=0;i<range.length;i++) {
      data[i]=new Object[2];
      data[i][0]=data[i][1]=range[i][0];
   }
  
   return data;
     

}


public static class TruncateString extends ConverterBase {
    Pattern pattern;

    int size;
    String stringToColorize;
    String color;

    public TruncateString(int size, String stringToColorize, String color, Converter converter) {
       super(converter);
       this.size=size;
       this.stringToColorize=stringToColorize;
       this.color=color;
       if (stringToColorize!=null && !stringToColorize.equals(""))
         pattern=Pattern.compile("(?i)"+stringToColorize);

    }

    public Object convert(Object inputObj) {
        inputObj=decoratorConvert(inputObj);

       // Data input validation - if the passed object is null it is ok, but no logic needs to be performed. 
       if (inputObj==null)
         return null;
       else if (inputObj instanceof String && size>=1  && inputObj.toString().length()>size) { // prevent end of array bounds exception
        // truncate string and replace returns with returns/<br> for readability in browser and view source 
        String returnStr=inputObj.toString().substring(0,size); 
        if (pattern!=null) {
          Matcher matcher=pattern.matcher(returnStr);
          returnStr=matcher.replaceAll("<b style='color:black;background-color:"+color+"'>"+stringToColorize+"</b>");
        }
       
        return returnStr;
       } else if (inputObj instanceof String) {
        String returnStr=inputObj.toString(); 
        if (pattern!=null) {
          Matcher matcher=pattern.matcher(returnStr);
          returnStr=matcher.replaceAll("<b style='color:black;background-color:"+color+"'>"+stringToColorize+"</b>");
        }
       
        return returnStr;    

       } else 
         return inputObj;

     }



    protected Converter createInstance(Converter nextConverter) {
       return new TruncateString(size, stringToColorize, color, nextConverter);
    }
    
} 


%>