
(function() {
	angular.module('driversExecutionModule')
		.service('datasetDriversService',['sbiModule_restServices', 'sbiModule_messaging', 'docExecute_urlViewPointService', 'driversDependencyService', 'execProperties', function(sbiModule_restServices, sbiModule_messaging, docExecute_urlViewPointService, driversDependencyService, execProperties){
			var datasetService = {};

			datasetService.getDatasetDriversByDocumentId = function(id) {
				sbiModule_restServices.promiseGet("2.0/datasets","documentDrivers/"+id)
				.then(function(response) {
					angular.copy(response.data.filterStatus, execProperties.parametersData.documentParameters);
					if(response.data && response.data.filterStatus && response.data.filterStatus.length > 0) {
						docExecute_urlViewPointService.prepareDrivers(response.data, driversDependencyService.buildCorrelation);
					}
				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				});
			}

			return datasetService;
		}])

	})();