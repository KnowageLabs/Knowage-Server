<script>

sbiM.factory('datamining_template',function(){
	var config = {};
	var urlSettings = {
	    contextPathDatamining: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
    };

    config.urlSettings = urlSettings;
     
    var params = {
    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
    };
	
    config.ajaxBaseParams = params;
	
	return config;
});

</script>