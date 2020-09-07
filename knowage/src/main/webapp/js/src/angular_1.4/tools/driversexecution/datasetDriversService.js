
(function() {
	angular.module('driversExecutionModule')
		.service('datasetDriversService',['sbiModule_restServices', 'sbiModule_messaging', 'docExecute_urlViewPointService', 'driversDependencyService', 'execProperties', function(sbiModule_restServices, sbiModule_messaging, docExecute_urlViewPointService, driversDependencyService, execProperties){
			var datasetService = {};
			datasetService.datasetsListTemp = [];
			datasetService.datasetMapById = {};
			datasetService.selectedDataSet = {};

			datasetService.getDatasetParametersFromBusinessModel = function (){
				sbiModule_restServices.post("dataset","drivers/", datasetService.dataset.qbeDatamarts).then(function(response){
					datasetService.dataset.drivers = angular.copy(response.data.filterStatus);
					angular.copy(response.data.filterStatus, execProperties.parametersData.documentParameters);
					docExecute_urlViewPointService.prepareDrivers(response.data, driversDependencyService.buildCorrelation);
				})
			}

			datasetService.getDatasetById = function(id) {
				sbiModule_restServices.promiseGet("1.0/datasets","dataset/id/"+id)
				.then(function(response) {
					datasetService.dataset = angular.copy(response.data[0]);
					if(datasetService.dataset.dsTypeCd == "Qbe") {
						datasetService.getDatasetParametersFromBusinessModel();
					}
				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				});
			}

			return datasetService;
		}])

	})();