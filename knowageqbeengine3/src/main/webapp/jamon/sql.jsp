<%@ page language="java" buffer="8kb" autoFlush="true" isThreadSafe="true" isErrorPage="false"  %> 
<%@ page import="java.util.*, java.util.regex.*, java.text.*, com.jamonapi.*, com.jamonapi.proxy.*, com.jamonapi.utils.*, com.fdsapi.*, com.fdsapi.arrays.*" %>  

<%  
// Set formatting rules per the requests Locale (as opposed to the servers locale). 
// This will format data per the users preference (note this sets it for the given thread/servlet) 
FormattedDataSet fds=new FormattedDataSet(); 

LocaleContext.setLocale(request.getLocale());  
// Assign request parameters to local variables.  
String action    = getValue(request.getParameter("action"),"Refresh");
String outputType= getValue(request.getParameter("outputTypeValue"),"html"); 
String bufferSize    = getValue(request.getParameter("bufferSize"),"No Action");
String arraySQL  = getValue(request.getParameter("ArraySQL"),""); 
String sortOrder = getValue(request.getParameter("sortOrder"), "desc"); 
int sortCol      = getNum(request.getParameter("sortCol"), "1");
int textSize     = getNum(request.getParameter("TextSize"), "0");   
// Assign defaults for arraysql.  If nothing is provided use 'select * from array'.  If the first word is not  
// 'select' assume they want to do a like on the first column (label). 
String arraySQLExec = getValue(arraySQL,"select * from array");   
String highlightString=getValue(request.getParameter("highlight"),"");

if (arraySQLExec.trim().toLowerCase().startsWith("select"))    
  arraySQLExec=arraySQLExec;// noop all is ok full select entered 
else if (arraySQLExec.trim().toLowerCase().startsWith("where"))     
  arraySQLExec="select * from array "+arraySQL;// where clause entered:  where hits>100 and total<50000 
else    {
  arraySQLExec="select * from array where sql like '"+arraySQL+"'";
} 

arraySQLExec = (arraySQLExec.trim().toLowerCase().startsWith("select")) ? arraySQLExec : "select * from array where sql like '"+arraySQL+"'";  
// Build the request parameter query string that will be part of every clickable column. 
String query=""; 
query+="&outputTypeValue="+outputType; 
query+="&ArraySQL="+java.net.URLEncoder.encode(arraySQL);
query+="&TextSize="+textSize;   
query+="&highlight="+highlightString;   

executeAction(action);
setBufferSize(bufferSize);

Map map=new HashMap(); // used for html page 
map.put("sortPageName", "sql.jsp"); 
map.put("query", query); 
map.put("imagesDir","images/");   
// used for xml1 page. 
map.put("rootElement", "JAMonSQLXML");   

String outputText=""; 
ResultSetConverter rsc=getResultSetConverter(MonProxyFactory.getSQLDetailHeader(), MonProxyFactory.getSQLDetail(), arraySQLExec);      
ArrayConverter ac=getArrayConverter(textSize,highlightString );     
fds.setArrayConverter(ac);



if (rsc.isEmpty())
  outputText="<div align='center'><br><br><b>No data was returned</b></div>";
else {      
  if ("xml".equalsIgnoreCase(outputType)) {       
    rsc=new ResultSetConverter(rsc.getMetaData(), rsc.getResultSet());       
    outputText=fds.getFormattedDataSet(rsc, map, "xml1");     
  } else if  ("csv".equalsIgnoreCase(outputType)) {
      rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
      outputText=fds.getFormattedDataSet(rsc, map, "csv");
 } else if ("excel".equalsIgnoreCase(outputType) || "spreadsheet".equalsIgnoreCase(outputType)) {       
    rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));       
    outputText=fds.getFormattedDataSet(rsc, map, "basicHtmlTable");     
  } else        
    outputText=fds.getSortedText(rsc.getMetaData(), rsc.getResultSet(), map, sortCol, sortOrder, getJAMonTemplate(fds));   

}
%>  

<% if ("html".equalsIgnoreCase(outputType)) { %> 
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"> 
<html> 
<head> 
<META http-equiv="Content-Type" content="text/html"; charset=UTF-8"> 
<link rel="stylesheet" type="text/css" href="css/JAMonStyles.css"> 
<title>JAMon - SQL Detail (<%=MonProxyFactory.getSQLBufferSize()%> rows are <%=enabled(MonProxyFactory.isSQLDetailEnabled())%>) - <%=now()%></title> 
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
    newWin = window.open('sql.htm', 'helpWin', 'resizable=no,scrollbars=yes,height=550,width=450,screenX=100,screenY=100');
    if (newWin.opener == null) newWin.opener = self;
}
// -->
</script>


</head> 
<body>  
<!-- arraySQLExec=<%=arraySQLExec%>-->  
<form action="sql.jsp" method="post">  
<table border="0" cellpadding="1" cellspacing="0" align="center"> 
  <tr> <td><h2 style="color:#03487F;">JAMon - SQL Detail (<%=MonProxyFactory.getSQLBufferSize()%> rows are <%=enabled(MonProxyFactory.isSQLDetailEnabled())%>) - <%=now()%> - <a href="menu.jsp">Home</a></h2></td> </tr> 
  <tr> <td><table class="layoutmain" border="0" cellpadding="4" cellspacing="0" width="750" align="left">     
    <tr class="sectHead">     
    <th>SQL Detail Action</th>
    <th>Output</th>     
    <th>Set Buffer Size (optional)</th>     
    <th>Text Display Length (optional)</th>     
    <th>Highlight (optional)</th>     
    <th>Filter (optional)</th>     
    <th align="right"><a href="javascript:helpWin();" style="color:#C5D4E4;">Help</a></th>     
    </tr>     
   <tr class="even">     
    <th><%=fds.getDropDownListBox(actionHeader, actionBody, "")%></th>
    <th><%=fds.getDropDownListBox(outputTypeHeader, outputTypeBody, outputType)%></th>     
    <th><%=fds.getDropDownListBox(bufferSizeHeader, bufferSizeBody, "")%></th>     
    <th><input type='text' name='TextSize' value="<%=(textSize<=0) ? "" : ""+textSize%>" size="15"></th>
    <th><input type='text' name='highlight' value="<%=highlightString%>" size="20"></th>     
    <th><input type='text' name='ArraySQL' value="<%=arraySQL%>" size="45"></th>    
    <td><input type="submit" name="actionSbmt" value="Go !" ></td>     
  </tr> 
 </table></td> 
</tr> 
<tr> <td><table border="0" cellpadding='0' cellspacing='0' align="center">     
   <tr>     <td><%=outputText%></td>     </tr> 
   </table></td> 
 </tr> 
</table>  
</form>  



<br>
<div align="center" style="padding-top : 30px;">
<hr width="580" align="center" />
<a href="menu.jsp">Home</a> | <a href="jamonadmin.jsp">JAMonAdmin</a> | <a href="monmanage.jsp">Manage Mon</a> | <a href="mondetail.jsp">View Listener</a> |SQL Details | <a href="exceptions.jsp">Exception Details</a>
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
<% 
} else if ("xml".equalsIgnoreCase(outputType)) { 
%> 
<?xml version="1.0"?> <%=outputText%> 
<% } else {   
  response.setContentType("application/vnd.ms-excel"); 
%>   
<%=outputText%>  
<% } %>  
<%!    


String[] outputTypeHeader={"outputTypeValue","outputType"}; 
Object[][] outputTypeBody={ {"html", "HTML"},{"xml", "XML"}, {"csv", "Comma Separated"}, {"excel","MS Excel"},};   
Template jamonTemplate;  
 
String[] actionHeader={"action","actionDisplay"};
Object[][] actionBody={
                 {"Refresh", "Refresh"}, 
                 {"Reset", "Reset"}, 
	         {"Enable","Enable"}, 
	         {"Disable","Disable"}, 
                };

String[] bufferSizeHeader={"bufferSize","bufferSizeDisplay"};
Object[][] bufferSizeBody={
                 {"No Action", "No Action"}, 
                 {"100", "100 rows"}, 
                 {"250", "250 rows"}, 
	         {"500","500 rows"}, 
	         {"1000","1000 rows"}, 
               };


private static String enabled(boolean isEnabled) {
   return isEnabled ? "enabled" : "disabled";
}

// Format time String in current locale. 
private String now() {   return LocaleContext.getDateFormatter().format(new Date()); }    

// if the value is null then return the passed in default else return the value 
private static String getValue(String value, String defaultValue) 
{   
  return (value==null || "".equals(value.trim())) ? defaultValue: value; 
}  

private static void executeAction(String action) {

  if ("Reset".equals(action))
    MonProxyFactory.resetSQLDetail();
  else if ("Enable".equals(action)) 
    MonProxyFactory.enableSQLDetail(true);
  else if ("Disable".equals(action))  
    MonProxyFactory.enableSQLDetail(false);

}

// There is no technical limit to buffer size, so other possibilities could be added.
private static void setBufferSize(String bufferSize) {

  if ("100".equals(bufferSize))
    MonProxyFactory.setSQLBufferSize(100);
  else if ("250".equals(bufferSize)) 
    MonProxyFactory.setSQLBufferSize(250);
  else if ("500".equals(bufferSize)) 
    MonProxyFactory.setSQLBufferSize(500);
  else if ("1000".equals(bufferSize)) 
    MonProxyFactory.setSQLBufferSize(1000);

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
  ArraySQL asql=new ArraySQL(header, arraySQLExec );       
  ResultSetConverter rsc = new ResultSetConverter(header, asql.execute(data));      
  //MonitorFactory.add("cellCount","count",rsc.getColumnCount()*rsc.getRowCount());      
  return rsc; 
}    

private static ArrayConverter getArrayConverter(int textSize, String highlightMe) {    
  // used to format the data in the report   
  ArrayConverter ac=new ArrayConverter();
  // replace the normal extreme values of default min,max, and acccess date with an empty default.   
  // If the key is in the data stream the value will be used instead of the key.    
  DecimalFormat decimalFormat=LocaleContext.getFloatingPointFormatter();   
  decimalFormat.applyPattern("#,###");   
  DateFormat dateFormat=LocaleContext.getDateFormatter();    
  // Set the converter to take action on data passed to the FormattedDataSet
  ac.setDefaultConverter(new ConverterNumToString(decimalFormat, new ConverterDateToString(dateFormat, new TruncateString(textSize,highlightMe,"#00ff99;"))));     
  return ac;  
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

 public static class TruncateString implements Converter {
    Pattern pattern;

    int size;
    String stringToColorize;
    String color;
    public TruncateString(int size, String stringToColorize, String color) {
       this.size=size;
       this.stringToColorize=stringToColorize;
       this.color=color;
       if (stringToColorize!=null && !stringToColorize.equals(""))
         pattern=Pattern.compile("(?i)"+stringToColorize);

    }

    public Object convert(Object inputObj) {


       // Data input validation - if the passed object is null it is ok, but no logic needs to be performed. 
       if (inputObj==null)
         return null;
       else if (inputObj instanceof String && size>=1  && inputObj.toString().length()>size) { // prevent end of array bounds exception
        // truncate string and replace returns with returns/<br> for readability in browser and view source 
        String returnStr=inputObj.toString().substring(0,size).replaceAll("\n","\n<br>"); 
        if (pattern!=null) {
          Matcher matcher=pattern.matcher(returnStr);
          returnStr=matcher.replaceAll("<b style='color:black;background-color:"+color+"'>"+stringToColorize+"</b>");
        }
       
        return returnStr;
       } else if (inputObj instanceof String) {
        String returnStr=inputObj.toString().replaceAll("\n","\n<br>"); 
        if (pattern!=null) {
          Matcher matcher=pattern.matcher(returnStr);
          returnStr=matcher.replaceAll("<b style='color:black;background-color:"+color+"'>"+stringToColorize+"</b>");
        }
       
        return returnStr;    

       } else 
         return inputObj;

     }


  public Converter createInstance() {
    return new TruncateString(size, stringToColorize, color);
  } 

} 

 %>