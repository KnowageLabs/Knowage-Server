<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<html>
<head></head>
<body ng-app="BlankApp">
	<div ng-controller="HelloController">
		<h2>{{message}}</h2>
	</div>
	<script type="text/javascript">
		var app = angular.module("BlankApp", []);
		app.controller("HelloController", function($scope) {
			$scope.message = "Hello, AngularJS";
		});
		var params = {
			SBI_EXECUTION_ID :
	<%=request.getParameter("SBI_EXECUTION_ID") != null
					? "'" + request.getParameter("SBI_EXECUTION_ID") + "'"
					: "null"%>
		};
		var a = 1;
		var b = 4;
	</script>

	<p>
		<%=request.getParameter("SBI_EXECUTION_ID")%>
	</p>
	<p></p>
	<h1>Hello World +</h1>

</body>

</html>