<script>

sbiM.factory('datamining_template',function(){
	var config = {};
	var urlSettings = {
	    sbihost:  '<%=spagobiServerHost%>'    
	    , protocol: '<%= request.getScheme()%>'
    	, host: '<%= request.getServerName()%>'
    	, port: '<%= request.getServerPort()%>'
    	, contextPath: '/<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
    };

    var externalUrl =  urlSettings.sbihost+"/"+ urlSettings.contextPath+"/restful-services/";
	
    config.urlSettings = urlSettings;
    config.externalUrl = externalUrl;
     
    var params = {
    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
    };
	
    config.ajaxBaseParams = params;
	
	return config;
});

</script>