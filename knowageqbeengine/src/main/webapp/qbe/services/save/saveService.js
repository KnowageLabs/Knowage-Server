var saveQbe = angular.module('saveservice', [ 'sbiModule' ]);

queries.service('save_service', function(sbiModule_restServices, sbiModule_messaging,sbiModule_config, $q, $rootScope) {
	this.getDomainTypeCategory = function() {
		return sbiModule_restServices.promiseGet("qbequery/domainCategories", "")
	}

	this.getDomainTypeScope = function() {
		return sbiModule_restServices.promiseGet("qbequery/domainScope", "")
	}

	this.saveQbeDataSet = function(body) {
		var q = "?SBI_EXECUTION_ID="+ sbiModule_config.sbiExecutionID;
		sbiModule_restServices.promisePost('qbequery/saveDataSet', q, body).then(
			function(response) {
				sbiModule_messaging.showSuccessMessage("QBE dataset succesflly saved", 'Success!');
			},
			function(response) {
				var message = "";
				if (response.status == 500) {
					message = response.data.RemoteException.message;
				} else {
					message = response.data.errors[0].message;
				}
				sbiModule_messaging.showErrorMessage(message,'Error');
			});
	}
});