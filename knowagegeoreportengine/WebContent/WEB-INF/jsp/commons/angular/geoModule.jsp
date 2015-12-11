<script>
/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
 
<%-- the JSP variable are present in angularResources.jsp--%>
	
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
    if(!t.hasOwnProperty("selectedMultiIndicator")){
    	//era = null
    	t.selectedMultiIndicator = [];
    }
    if(!t.hasOwnProperty("selectedFilters")){
    	t.selectedFilters={};
    }
    
    if(!t.hasOwnProperty("currentView")){
    	t.currentView={center:[0, 0], zoom: 2 };
    }
    
    if(!t.hasOwnProperty("analysisConf")){
    	t.analysisConf={};
    }
    
    if(!t.analysisConf.hasOwnProperty("choropleth")){
    	t.analysisConf.choropleth={"method":"CLASSIFY_BY_EQUAL_INTERVALS","classes":3,"fromColor":"#FFFF00","toColor":"#008000"};
    }
    
    if(!t.analysisConf.hasOwnProperty("proportionalSymbol")){
    	t.analysisConf.proportionalSymbol={"minRadiusSize":2,"maxRadiusSize":20,color:"#FFFF00"};
    }
    
    if(!t.analysisConf.hasOwnProperty("chart")){
    	t.analysisConf.chart={indicator_1:"red",indicator_2:"green",indicator_3:"blue"};
    }

    return t;
});

geoM.factory('geoModule_driverParameters',function(geoReportCompatibility){
	var driverParamsAsString = '<%=driverParams%>';
	
	var driverParamsToReturn = JSON.parse(driverParamsAsString);
	
	return driverParamsToReturn;
});


</script>