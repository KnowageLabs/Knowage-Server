angular.module('sbi_chart_toolbar',[])
.directive('sbiChartToolbar',function(){
	return{
		restrict:"E",
		replace: true,
		templateUrl:'/knowagewhatifengine/html/template/right/chartToolbar.html'
	}
});