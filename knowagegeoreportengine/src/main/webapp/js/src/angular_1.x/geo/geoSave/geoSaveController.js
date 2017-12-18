/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

angular.module('geoModule')
.directive('geoSave',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoSave/templates/geoSaveTemplate.jsp',
		controller: geoSaveControllerFunction,
		require: "^geoMap",
		scope: {
			id:"@"
		},
		disableParentScroll:true,
	}
})

 
function geoSaveControllerFunction($scope,geoModule_template,sbiModule_restServices,sbiModule_config,$map,$mdDialog,sbiModule_translate,$mdToast,geoModule_driverParameters ){
	$scope.validateAndSave=function(){
		
		//message to user for the save of current view
		
			    var confirm = $mdDialog.confirm()
			          .title(sbiModule_translate.load("gisengine.info.message.save.progress"))
			          .textContent(sbiModule_translate.load("gisengine.info.message.save.saveView"))
			          .ariaLabel('Salva vista')
			          .ok(sbiModule_translate.load("gisengine.info.message.yes"))
			          .cancel(sbiModule_translate.load("gisengine.info.message.no"));
			    
			    $mdDialog.show(confirm).then(function() {
			    	//alter current view of the map
					geoModule_template.currentView.center=$map.getView().getCenter();
					geoModule_template.currentView.zoom=$map.getView().getZoom();
					$scope.save();
			    }, function() {
			    	$scope.save();
			    });
		 
	}
	$scope.save=function(){ 
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath+'restful-services/');
		var doclabel= angular.isArray(geoModule_driverParameters.DOCUMENT_LABEL) ? geoModule_driverParameters.DOCUMENT_LABEL[0] : geoModule_driverParameters.DOCUMENT_LABEL;
		var dataToSend={
				DOCUMENT_LABEL:doclabel,
				TEMPLATE:geoModule_template
		}
		
		sbiModule_restServices.promisePost("1.0/documents", 'saveGeoReportTemplate',dataToSend).then(
				function(response, status, headers, config) {
					 $mdToast.show(
						      $mdToast.simple()
						        .textContent(sbiModule_translate.load("sbi.generic.ok.msg"))
						        .position('top')
						        .hideDelay(3000)
						    );
				},function(response, status, headers, config) {
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.error.msg"));
					  });
	}
}