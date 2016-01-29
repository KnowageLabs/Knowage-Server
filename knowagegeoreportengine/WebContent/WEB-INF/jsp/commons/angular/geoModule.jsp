<script>
/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
 
<%-- the JSP variable are present in angularResources.jsp--%>
var template;
geoM.factory('geoModule_template',function(geoReportCompatibility){
	console.log("------------->template")
	 template= <%= template %>;
	
	if(template.hasOwnProperty('role')) {
		template.role = template.role.charAt(0) == '/'? template.role.charAt(0): '/' + template.role.charAt(0);
	}
	
	var executionRole = '<%= executionRole %>';
	template.role = executionRole || template.role;
	
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
    template.executionContext = executionContext;
    geoReportCompatibility.resolveCompatibility(template);
  
    if(!template.hasOwnProperty("analysisType")){
    	template.analysisType = "choropleth";
    }
    if(!template.hasOwnProperty("selectedIndicator")){
    	template.selectedIndicator = null;
    }
    if(!template.hasOwnProperty("selectedMultiIndicator")){
    	//era = null
    	template.selectedMultiIndicator = [];
    }
    if(!template.hasOwnProperty("selectedFilters")){
    	template.selectedFilters={};
    }
    
    if(!template.hasOwnProperty("currentView")){
    	template.currentView={center:[0, 0], zoom: 2 };
    }
    
    if(!template.hasOwnProperty("analysisConf")){
    	template.analysisConf={};
    }
    
    if(!template.hasOwnProperty("layersLoaded")){
    	template.layersLoaded={};
    }
    
    if(!template.analysisConf.hasOwnProperty("choropleth")){
    	template.analysisConf.choropleth={"method":"CLASSIFY_BY_QUANTILS","classes":3,"fromColor":"#FFFF00","toColor":"#008000"};
    }
    
    if(!template.analysisConf.hasOwnProperty("proportionalSymbol")){
    	template.analysisConf.proportionalSymbol={"minRadiusSize":2,"maxRadiusSize":20,color:"#FFFF00"};
    }
    
    if(!template.analysisConf.hasOwnProperty("chart")){
    	template.analysisConf.chart={indicator_1:"red",indicator_2:"green",indicator_3:"blue"};
    }
    
    if(!template.hasOwnProperty("filtersApplied")){
    	template.filtersApplied={};
    }
    return template;
});

geoM.factory('geoModule_driverParameters',function(geoReportCompatibility){
	var driverParamsAsString = '<%=driverParams%>';
	
	var driverParamsToReturn = JSON.parse(driverParamsAsString);
	
	return driverParamsToReturn;
});


</script>