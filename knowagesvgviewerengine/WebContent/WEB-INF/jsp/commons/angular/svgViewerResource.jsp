<script>
 
<%-- the JSP variable are present in angularResources.jsp--%>

function getDriverParameters(){
	var driverParamsAsString = '<%=driverParams%>';
	var driverParamsToReturn = JSON.parse(driverParamsAsString);
	
	return driverParamsToReturn;
}


</script>