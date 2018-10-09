var saveQbe = angular.module('saveservice', [ 'sbiModule' ]);

queries
		.service(
				'save_service',
				function(sbiModule_restServices, sbiModule_messaging,
						sbiModule_config, $q, $rootScope, $httpParamSerializer) {
					this.getDomainTypeCategory = function() {
						var queryparams = {
							sbiExecution : sbiModule_config.sbiExecutionID,
						}
						sbiModule_restServices
								.promiseGet("qbequery/getCategories", "",
										$httpParamSerializer(queryparams))
								.then(
										function(response) {
											return response.data;
										},
										function(response) {
											var message = "";
											if (response.status == 500) {
												message = response.data.RemoteException.message;
											} else {
												message = response.data.errors[0].message;
											}
											sbiModule_messaging
													.showErrorMessage(message,
															'Error');
										});
					}

					this.getDomainTypeScope = function() {
						sbiModule_restServices
								.promiseGet("qbequery/getScopes")
								.then(
										function(response) {
											return response.data;
										},
										function(response) {
											var message = "";
											if (response.status == 500) {
												message = response.data.RemoteException.message;
											} else {
												message = response.data.errors[0].message;
											}
											sbiModule_messaging
													.showErrorMessage(message,
															'Error');
										});
					}

					this.saveQbeDataSet = function(body) {
						var q = "?SBI_EXECUTION_ID="
								+ sbiModule_config.sbiExecutionID;
						sbiModule_restServices
								.promisePost('qbequery/saveDataSet', q, body)
								.then(
										function(response) {
											sbiModule_messaging
													.showSuccessMessage(
															"QBE dataset succesflly saved",
															'Success!');
										},
										function(response) {
											var message = "";
											if (response.status == 500) {
												message = response.data.RemoteException.message;
											} else {
												message = response.data.errors[0].message;
											}
											sbiModule_messaging
													.showErrorMessage(message,
															'Error');

										});
					}

				});