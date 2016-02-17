var app = angular.module('templateManagement', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'document_tree','sbiModule','componentTreeModule', 'angular_2_col']);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
	'blue-grey');
});

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);


function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	//main Controller
	
	//variables
	$scope.dateSelected={};
	$scope.translate=sbiModule_translate;
	$scope.dateSelected.data="";
	$scope.data_format=null;
	$scope.documents=[];
	$scope.docChecked=[];
	$scope.flagCheck=false;
	$scope.flagSelect=false;
	$scope.confirm="";
	$scope.folders=[];
	$scope.tree=[];
	
	
	$scope.loadDocuments = function(ev){
		if($scope.data_format){
			//get the date selected
			sbiModule_restServices.get("2.0", "folders","includeDocs=true").success(function(data){
				//if not errors in response, copy the data
				if (data.errors === undefined){
					$scope.folders=angular.copy(data);
					$scope.tree = angular.copy(data);
					$scope.createTree();
					console.log("folder",$scope.folders);
				}else{
					$scope.folders=[];
				}
			}).error(function(data, status){
				$scope.folders=[];
				$scope.tree=[];
				$scope.log.error('GET RESULT error of ' + data + ' with status :' + status);
			});
			
			sbiModule_restServices.get("2.0/documents", 'withData',"data="+$scope.data_format).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("layer non Ottenuti");
							$scope.flagSelect=false;
						} else {
							
							$scope.documents=data;
							$scope.flagSelect=true;
						}

					}).error(function(data, status, headers, config) {
						$scope.flagSelect=false;
						console.log("layer non Ottenuti " + status);
					})
					$scope.parseDate();
			
			
			
			
		}else{
			$scope.openDialog(ev,sbiModule_translate.load("sbi.templatemanagemenent.alertdate"));
		}
		
	}
	$scope.createTree = function(){
		for(var i=0;i<$scope.folders.length;i++){
			$scope.tree[i].biObjects=[];
			if($scope.folders[i]["biObjects"].length!=0){
				for(var j=0;j<$scope.folders[i]["biObjects"].length;j++){
					for(var k=0;k<$scope.documents.length;k++){
					if($scope.folders[i]["biObjects"][j].name==$scope.documents[k].name){
						$scope.tree[i].biObjects.push($scope.folders[i]["biObjects"][j]);
					}
					}
				}
			}
		}
	}
	  $scope.indexInList=function(item, list) {

			for (var i = 0; i < list.length; i++) {
				for(var j=0;j< list[i]["biObjects"].length;j++){
					var object = list[i]["biObjects"][j];
					if(object.name==item.name){
						return i;
					}
				}

			}

			return -1;
		};
	$scope.parseDate = function(){
		//parse the date in the format yyyy-mm-gg
		if($scope.dateSelected.data){
			$scope.data_format = new Date($scope.dateSelected.data);
			var year= $scope.data_format.getFullYear();
			var month=$scope.data_format.getMonth()+1;
			var day=$scope.data_format.getDate();
			$scope.data_format=year+"-"+month+"-"+day;
		}else{
			$scope.data_format = null;
		}
		
	}
	
	
	$scope.deleteTemplate=function(ev){
		if(!$scope.data_format){
			//if missing date
			$scope.openDialog(ev,sbiModule_translate.load("sbi.templatemanagemenent.alertdate"));
		}
		if($scope.docChecked.length==0 && $scope.data_format ){
			//if any document is check
			$scope.openDialog(ev,sbiModule_translate.load("sbi.templatemanagemenent.alertmissingdocument"));
		}
		if($scope.data_format && $scope.docChecked.length!=0){
			//confim delete
		
			 $scope.confirm =  $mdDialog.confirm()
			          .title(sbiModule_translate.load("sbi.templatemanagemenent.alertdatedelete"))
			          .ariaLabel('Lucky day')
			          .targetEvent(ev)
			          .ok('Ok')
			          .cancel('Cancel');
			 
			 $mdDialog.show( $scope.confirm).then(function() {
				
				 var request = [];
					for(var i=0;i<$scope.docChecked.length;i++){
						var obj={id:$scope.docChecked[i]["id"], data:$scope.data_format};
						request.push(obj);
					}
					sbiModule_restServices.post("template",'deleteTemplate',request).success(
							function(data, status, headers, config) {
								if (data.hasOwnProperty("errors")) {
									console.log("layer non Ottenuti");
									$scope.showActionOK(sbiModule_translate.load("sbi.templatemanagemenent.templatedeletederror"));

								} else {
									$scope.loadDocuments(null);
									$scope.showActionOK(sbiModule_translate.load("sbi.templatemanagemenent.templatedeleted"));
								}

							}).error(function(data, status, headers, config) {
								console.log("layer non Ottenuti " + status);
								$scope.showActionOK(sbiModule_translate.load("sbi.templatemanagemenent.templatedeletederror"));
							})
			    }, function() {
			    	//else nothing ACTION
			    });
		
				
	
			
		}
			
	}
	
	$scope.showActionOK = function(text) {
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
    // to prevent interaction outside of dialog
	$scope.openDialog = function(ev, insTitle,conferm){
	
			 
		if(ev){
			 $mdDialog.show(
				      $mdDialog.alert()
				        .parent(angular.element(document.querySelector('#popupContainer')))
				        .clickOutsideToClose(true)
				        .title(insTitle)
				        
				        .ariaLabel('Alert Dialog Demo')
				        .ok('Ok')
				        .targetEvent(ev)
				    );
		} else{
			
		}
		
		
	   
	}
	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item.id, list);

		if(index != -1){
			$scope.docChecked.splice(index,1);
		}else{
			$scope.docChecked.push(item.id);
		}

	};

	$scope.exists = function (item, list) {

		return  $scope.indexInList(item.id, list)>-1;

	};

	$scope.indexInList= function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object==item){
				return i;
			}
		}

		return -1;
	};
	$scope.selectAll = function(){
		if(!$scope.flagCheck){
			//if it was false then the user check 
			$scope.flagCheck=true;
			$scope.docChecked=[];
			for(var i=0;i<$scope.documents.length;i++){
				$scope.docChecked.push($scope.documents[i].id);
			}
		}else{
			$scope.flagCheck=false;
			$scope.docChecked=[];
		}
		
		
	}
}

