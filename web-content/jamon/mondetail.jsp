<%@ page language="java" buffer="8kb" autoFlush="true" isThreadSafe="true" isErrorPage="false"  %>
<%@ page import="java.util.*, java.util.regex.*, java.text.*, com.jamonapi.*, com.jamonapi.proxy.*, com.jamonapi.utils.*, com.fdsapi.*, com.fdsapi.arrays.*" %>

<%


FormattedDataSet fds=new FormattedDataSet();


MonKey key=(MonKey)session.getAttribute("monKey");
String listenerType = "value";
if (request.getParameter("listenertype")==null  && session.getAttribute("listenerType")!=null)
  listenerType=(String)session.getAttribute("listenerType");
else if (request.getParameter("listenertype")!=null) {
  listenerType=(String)request.getParameter("listenertype");
} 
String currentListenerName=getValue(request.getParameter("currentlistener"),"JAMonBufferListener");


// Set formatting rules per the requests Locale (as opposed to the servers locale).
// This will format data per the users preference (note this sets it for the given thread/servlet)
LocaleContext.setLocale(request.getLocale());

String outputType= getValue(request.getParameter("outputTypeValue"),"html");
String formatter = getValue(request.getParameter("formatterValue"), "#,###");
String arraySQL  = getValue(request.getParameter("ArraySQL"),"");
String sortOrder = getValue(request.getParameter("sortOrder"), "desc");
int sortCol      = getNum(request.getParameter("sortCol"), "1");
String bufferSize    = getValue(request.getParameter("bufferSize"),"No Action");
int textSize     = getNum(request.getParameter("TextSize"), "0");
String highlightString=getValue(request.getParameter("highlight"),"");



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
query+="&outputTypeValue="+outputType;
query+="&formatterValue="+java.net.URLEncoder.encode(formatter);
query+="&ArraySQL="+java.net.URLEncoder.encode(arraySQL);
query+="&TextSize="+textSize;
query+="&highlight="+highlightString;   
query+="&listenertype="+listenerType;
query+="&currentlistener="+currentListenerName;


Map map=new HashMap();
// used for html page
map.put("sortPageName", "mondetail.jsp");
map.put("query", query);
map.put("imagesDir","images/");


// used for xml1 page.
map.put("rootElement", "JAMonXML");

String outputText="";


if (key==null)
  outputText="<div align='center'><br><br><b>A Monitor was not specified.  Select one from jamonadmin.jsp</b></div>";
else {

  JAMonListener listener=null;

  if (MonitorFactory.exists(key))
    listener=MonitorFactory.getMonitor(key).getListenerType(listenerType).getListener(currentListenerName);

  if (listener==null)
    outputText="<div align='center'><br><br><b>Null listener returned. Ensure there is a listener for this monitor</b></div>";
  else if (!(listener instanceof JAMonBufferListener))
    outputText="<div align='center'><br><br><b>The selected Listener is not of type JAMonBufferListener and so data can not be displayed.</b></div>";
  else {
  
    setBufferSize((JAMonBufferListener) listener, bufferSize);

    DetailData detailData=((JAMonBufferListener)listener).getDetailData();
    ResultSetConverter rsc=getResultSetConverter(detailData.getHeader(), detailData.getData(),arraySQLExec);

    if (rsc.isEmpty())
     outputText="<div align='center'><br><br><b>No data was returned</b></div>";
    else {
      ArrayConverter ac=getArrayConverter(rsc.getMetaData(), formatter, textSize, highlightString, false);
      fds.setArrayConverter(ac);
      
     // Object[][] data=ac.convert(rsc.getResultSet());
     // rsc=rsc.execute("select rowNum() as Row, * from array");
      

      if ("xml".equalsIgnoreCase(outputType)) {
       rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
       outputText=fds.getFormattedDataSet(rsc, map, "xml1");
      } else if  ("csv".equalsIgnoreCase(outputType)) {
          rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
          outputText=fds.getFormattedDataSet(rsc, map, "csv");
      } else if ("excel".equalsIgnoreCase(outputType) || "spreadsheet".equalsIgnoreCase(outputType)) {
       rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
       outputText=fds.getFormattedDataSet(rsc, map, "basicHtmlTable");
      } else   {
          ac=getArrayConverter(rsc.getMetaData(), formatter, textSize, highlightString, true);
          fds.setArrayConverter(ac);
          outputText=fds.getSortedText(rsc.getMetaData(), rsc.getResultSet(), map, sortCol, sortOrder, getJAMonTemplate(fds));
      }

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
<title>JAMon - Monitor Detail - <%=now()%></title>
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
    newWin = window.open('mondetailhelp.htm', 'helpWin', 'resizable=no,scrollbars=yes,height=550,width=450,screenX=100,screenY=100');
    if (newWin.opener == null) newWin.opener = self;
}
// -->
</script>

</head>
<body>

<!-- arraySQLExec=<%=arraySQLExec%>-->

<form action="mondetail.jsp" method="post">

<input type="hidden"  name="listenertype" value="<%=listenerType%>" />
<input type="hidden"  name="currentlistener" value="<%=currentListenerName%>" />

<table border="0" cellpadding="1" cellspacing="0" align="center">
<tr>
<td><h2 style="color:#03487F;">JAMon - Monitor Detail - <%=now()%> - <a href="menu.jsp">Home</a>  </h2></td>
</tr>
<tr>
<td><table class="layoutmain" border="0" cellpadding="4" cellspacing="0" width="750" align="left">
    <tr class="sectHead">
    <th>Output</th>
    <th>Cell Format</th>
    <th>Set Buffer Size (optional)</th>     
    <th>Text Display Size (optional)</th>     
    <th>Highlight (optional)</th>  
    <th>Filter (optional)</th>
    <th align="right"><a href="javascript:helpWin();" style="color:#C5D4E4;">Help</a></th>
    </tr>
    <tr class="even">
    <th><%=fds.getDropDownListBox(outputTypeHeader, outputTypeBody, outputType)%></th>
    <th><%=fds.getDropDownListBox(formatHeader, formatBody, formatter)%></th>
    <th><%=fds.getDropDownListBox(bufferSizeHeader, bufferSizeBody, "")%></th>     
    <th><input type='text' name='TextSize' value="<%=(textSize<=0) ? "" : ""+textSize%>" size="15"></th>
    <th><input type='text' name='highlight' value="<%=highlightString%>" size="20"></th>     
    <th><input type='text' name='ArraySQL' value="<%=arraySQL%>" size="45"></th>
    <td><input type="submit" name="actionSbmt" value="Go !" ></td>
    </tr>
</table></td>
</tr>
<tr>
<td>

<font color="#000099"><b>
<div align='center' class="monkey"><%=key+"<br>"+"ListenerType="+listenerType+", ListenerName="+currentListenerName%></div>
</b></font>

<table border="0" cellpadding='0' cellspacing='0' align="center">
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
<a href="menu.jsp">Home</a> | <a href="jamonadmin.jsp">JAMonAdmin</a> | <a href="monmanage.jsp">Manage Mon</a> | View Listener | <a href="sql.jsp">SQL Details</a> | <a href="exceptions.jsp">Exception Details</a>
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


String[] bufferSizeHeader={"bufferSize","bufferSizeDisplay"};
Object[][] bufferSizeBody={
                 {"No Action", "No Action"},                  {"50", "50 rows"}, 
                 {"100", "100 rows"},                  {"250", "250 rows"}, 	             {"500","500 rows"}, 	             {"1000","1000 rows"}, 	             {"2000","2000 rows"},            };


// There is no technical limit to buffer size, so other possibilities could be added.
private static void setBufferSize(JAMonBufferListener listener, String bufferSize) {

  if ("50".equals(bufferSize))
	listener.getBufferList().setBufferSize(50);
  else if ("100".equals(bufferSize))    listener.getBufferList().setBufferSize(100);  else if ("250".equals(bufferSize))     listener.getBufferList().setBufferSize(250);  else if ("500".equals(bufferSize))    listener.getBufferList().setBufferSize(500);  else if ("1000".equals(bufferSize))    listener.getBufferList().setBufferSize(1000);  else if ("2000".equals(bufferSize))    listener.getBufferList().setBufferSize(2000);

}


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






private static ResultSetConverter getResultSetConverter(String[] header, Object[][] data, String arraySQLExec) {
     ResultSetConverter rsc=new ResultSetConverter(header, data).execute("select rowNum() as RowNum, * from array");
     ArraySQL asql=new ArraySQL(rsc.getMetaData(), arraySQLExec );
     ArrayConverter ac=new ArrayConverter(rsc.getMetaData());

     // replace all booleans with strings as you can't sort booleans (they don't support Comparable interface for some reason)
     Map map=new HashMap();
     map.put(new Boolean(true), "true");
     map.put(new Boolean(false), "false");

     ac.setDefaultConverter(new ConverterMapValue(map));
     asql.setArrayConverter(ac);

     rsc = new ResultSetConverter(rsc.getMetaData(), asql.convert(rsc.getResultSet()));
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
    ArrayConverter ac=new ArrayConverter(header);
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