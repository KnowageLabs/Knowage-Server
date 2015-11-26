<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.engines.georeport.GeoReportEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="org.json.JSONObject"%>
<%
    Map driverParamsMap = new HashMap();
	for(Object key : engineInstance.getAnalyticalDrivers().keySet()){
		if(key instanceof String && !key.equals("widgetData")){
			String value = request.getParameter((String)key);
			if(value!=null){
				driverParamsMap.put(key, value);
			}
		}
	}
	String driverParams = new JSONObject(driverParamsMap).toString(0);
%>

<script>
	
/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

geoM.factory('geoModule_template',function(geoReportCompatibility){
	
	var t= <%= template %>;
	
	if(t.hasOwnProperty('role')) {
		t.role = t.role.charAt(0) == '/'? t.role.charAt(0): '/' + t.role.charAt(0);
	}
	
	var executionRole = '<%= executionRole %>';
	t.role = executionRole || t.role;
	
	var executionContext = {};
    <% 
    Iterator it = analyticalDrivers.keySet().iterator();
	while(it.hasNext()) {
		String parameterName = (String)it.next();
		String parameterValue = (String)analyticalDrivers.get(parameterName);
	 	String quote = (parameterValue.startsWith("'"))? "" : "'";
		if ( parameterValue.indexOf(",") >= 0){
	 %>
			executionContext ['<%=parameterName%>'] = [<%=quote%><%=parameterValue%><%=quote%>];
	<%	}else{
	%>
			executionContext ['<%=parameterName%>'] = <%=quote%><%=parameterValue%><%=quote%>;
	 <%
	 	}		
	 } //while
    %>
    t.executionContext = executionContext;
    geoReportCompatibility.resolveCompatibility(t);
  
    if(!t.hasOwnProperty("selectedIndicator")){
    	t.selectedIndicator = null;
    }
    
    if(!t.hasOwnProperty("selectedFilters")){
    	t.selectedFilters={};
    }

    return t;
});

geoM.factory('geoModule_driverParameters',function(geoReportCompatibility){
	var driverParamsAsString = '<%=driverParams%>';
	
	var driverParamsToReturn = JSON.parse(driverParamsAsString);
	
	return driverParamsToReturn;
});


</script>