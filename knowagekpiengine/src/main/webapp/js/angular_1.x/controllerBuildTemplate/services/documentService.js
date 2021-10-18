(function() {
	
angular.module('templateBuild')

.factory('documentService',["$mdDialog",
							"sbiModule_config",
							"sbiModule_translate",
							"sbiModule_restServices",
							"$mdToast",
							function($mdDialog,
									sbiModule_config,
									sbiModule_translate,
									sbiModule_restServices,
									$mdToast){
	var service = {};
	
	service.configuration = new Configuration();
	service.save = function(template){
		console.log("saving ...")
		service.configuration.customData.templateContent = template;
		if(sbiModule_config.docLabel){
			service.configuration.action = "MODIFY_KPI";
			service.configuration.document.name = sbiModule_config.docName;
			service.configuration.document.label = sbiModule_config.docName;
			saveKPI(service.configuration);
		}else{
			service.configuration.action = "DOC_SAVE";
			promtNameDialog();
		}
		
		
		
		
		
		console.log(service)
		 
	}
	
	var saveKPI = function(configuration){
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );

		sbiModule_restServices.promisePost("2.0", 'saveDocument',configuration).then(
						function(response) {
							console.log(response.data);

							var saveSuccessMsg = sbiModule_translate.load("sbi.kpidocumentdesigner.save.success");
							showAction(saveSuccessMsg);
						},function(response) {
							sbiModule_restServices.errorHandler(response.data,"");
						});
	}
	
	var showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};
	
	
	var promtNameDialog = function(){
		
		var confirm = $mdDialog.prompt()
	      .title(sbiModule_translate.load("kn.saving.menu.title"))
	      .textContent(sbiModule_translate.load("kn.saving.menu.textContent"))
	      .placeholder(sbiModule_translate.load("kn.saving.menu.placeHolder"))
	      .ariaLabel(sbiModule_translate.load("kn.saving.menu.textContent"))
	      .initialValue('')
	      .ok("ok")
	      .cancel("cancel");
		var promise = $mdDialog.show(confirm)
		  promise.then(function(result){
			  if(!result)promtNameDialog();
			  service.configuration.document.name = result;
			  service.configuration.document.label = result;
			  saveKPI(service.configuration)
			  
		  });
		return promise;
	}
	
	return service;
	
}])

function Document(type){
	
	this.name = "";
	this.label = "";
	this.description = "";
	this.type = type;

}

function CustomData(){
	this.templateContent = {}
}

function Configuration(){
	this.document =  new Document("KPI");
	this.customData = new CustomData();
}

})();