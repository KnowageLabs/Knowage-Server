var app = angular.module('impExpUsers', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'angular_list','sbiModule','file_upload', 'angular_2_col']);
app.directive("fileread", [function () {
	return {
		scope: {
			fileread: "="
		},
		link: function (scope, element, attributes) {
			element.bind("change", function (changeEvent) {
				var reader = new FileReader();
				reader.onload = function (loadEvent) {
					scope.$apply(function () {
						scope.fileread = loadEvent.target.result;
					});
				}
				reader.readAsDataURL(changeEvent.target.files[0]);
			});
		}
	}
}]);

app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);

function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	//variables
	$scope.translate=sbiModule_translate;
	$scope.selectedTab=0;
	$scope.users = [];
	$scope.usersSelected = [];
	$scope.flagCheck=false;
	$scope.nameExport="";
	$scope.viewDownload =false;
	$scope.download = sbiModule_download;
	$scope.importFile = {};
	$scope.uploadProcessing = [];
	$scope.upload = [];
	$scope.wait = false;
	$scope.currentRoles=[];
	$scope.exportedRoles=[];
	$scope.exportedUser=[];
	$scope.exportingUser = [];
	$scope.selectedUser = [];
	$scope.typeSaveUser = 'Missing'
	$scope.flagShowUser=false;
	
	$scope.loadAllUsers = function(){
		sbiModule_restServices.get("1.0/serverManager/importExport/users", 'getusers').success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						
						$scope.users = data;
						console.log($scope.users);
					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
	}
	
	$scope.upload = function(ev){
		if($scope.importFile.fileName == "" || $scope.importFile.fileName == undefined){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missinguploadfile"));
		}else{
			var fd = new FormData();
		
			fd.append('exportedArchive', $scope.importFile.file);
			console.log(fd);
			sbiModule_restServices.post("1.0/serverManager/importExport/users", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
			.success(function(data, status, headers, config) {
				console.log("role--->",data)
				if(data.STATUS=="NON OK"){
					$mdToast.show($mdToast.simple().content("data.ERROR").position('top').action(
					'OK').highlightAction(false).hideDelay(5000));
				}
				else if(data.STATUS=="OK"){
					//check role missing
					$scope.flagShowUser=true;
					$scope.currentRoles=data.currentRoles;
					$scope.exportedRoles=data.exportedRoles;
					
					if($scope.checkRole()){
						$scope.exportedUser = data.exportedUser;
					}
					
				}
				
				
			})
			.error(function(data, status, headers, config) {
				$mdToast.show($mdToast.simple().content("errore").position('top').action(
				'OK').highlightAction(false).hideDelay(5000));
				
			});
		}
	}
	
	
	$scope.prepare = function(ev){
		$scope.wait = true;
		if($scope.usersSelected.length == 0){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingcheck"));
		} else if($scope.nameExport==""){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missingnamefile"));
		}else{
			//modulo download zip
			var config={"USERS_LIST":$scope.usersSelected ,
					"EXPORT_FILE_NAME":$scope.nameExport,
					"EXPORT_SUB_OBJ":false,
					"EXPORT_SNAPSHOT":false};
			
			sbiModule_restServices.post("1.0/serverManager/importExport/users", 'export',config).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("layer non Ottenuti");
						} else {
							if(data.hasOwnProperty("STATUS") && data.STATUS=="OK"){
								//da usare poi
								$scope.viewDownload = true;
								$scope.downloadFile();
							}
								
						}

					}).error(function(data, status, headers, config) {
						console.log("layer non Ottenuti " + status);

					})
		}
		
	}
	
		
	$scope.downloadFile= function(){
			var data={"FILE_NAME":$scope.nameExport};
			var config={"responseType": "arraybuffer"};
			sbiModule_restServices.post("1.0/serverManager/importExport/users","downloadExportFile",data,config)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					showToast(data.errors[0].message,4000);
				}else if(status==200){
					console.log(data);
					$scope.download.getBlob(data,$scope.nameExport,'application/zip','zip');
					$scope.viewDownload = false;
					$scope.wait = false;
					$scope.showAction(sbiModule_translate.load("sbi.importusers.downloadOK"));
				}
			}).error(function(data, status, headers, config) {
				showToast("ERRORS "+status,4000);
			})
		}
	
	
	//import utilities
	
	$scope.save = function(ev){
		if($scope.exportingUser.length == 0){
			//if not selected no one users
			$scope.showAction(sbiModule_translate.load("sbi.importusers.anyuserchecked"));
		}else{
			if($scope.typeSaveUser == 'Missing'){
				//missing user
				console.log("save");
				sbiModule_restServices.post("1.0/serverManager/importExport/users","missingusers",$scope.exportingUser)
				.success(function(data, status, headers, config) {
					console.log("Saved ok");
					$scope.showAction(sbiModule_translate.load("sbi.importusers.importuserok"));
					
				}).error(function(data, status, headers, config) {
					showToast("ERRORS "+status,4000);
				})
			}else{
				//override
				console.log("save");
				sbiModule_restServices.post("1.0/serverManager/importExport/users","overrideusers",$scope.exportingUser)
				.success(function(data, status, headers, config) {
					console.log("Saved ok");
					$scope.showAction(sbiModule_translate.load("sbi.importusers.importuserok"));
				}).error(function(data, status, headers, config) {
					showToast("ERRORS "+status,4000);
				})
			}
		}
	}
	$scope.checkRole = function(){
		
		for(var i=0;i<$scope.exportedRoles.length;i++){
			var index = $scope.indexRoleInList($scope.exportedRoles[i],$scope.currentRoles );
			if(index==-1){
				$scope.showConfirm();
				return false;
				
			}
		}
		//all roles present
		return true;
		
	}
	$scope.showConfirm = function() {
	    // Appending dialog to document.body to cover sidenav in docs app
	    var confirm = $mdDialog.alert()
	          .title('There is a role that missing in your Database... Import failed')
	          .ariaLabel('Lucky day')
	          .ok('Ok')
	         
	    $mdDialog.show(confirm).then(function() {

	    }, function() {
	      
	    });
	  };
	$scope.selectUser = function(item){
		//if is present remove
		var index = $scope.indexInList(item,$scope.selectedUser);
		if(index!=-1){
			$scope.selectedUser.splice(index,1);
		}else{
			//if not present add
			$scope.selectedUser.push(item);
			
		}
		
		
	}
	$scope.addUser = function(){

		for(var i=0;i<$scope.selectedUser.length;i++){
			
			//add inf exportig user
			var index = $scope.indexInList($scope.selectedUser[i],$scope.exportingUser);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.exportingUser.push($scope.selectedUser[i]);
				
			}
			//remove from exportedUser
			var index = $scope.indexInList($scope.selectedUser[i],$scope.exportedUser);
			if(index!=-1){
				//if present
				$scope.exportedUser.splice(index,1);
			}else{
				//if not present add nothing action
				
			}
		}
		
		
		$scope.selectedUser=[];
		
	}
	$scope.removeUser = function(){
		
		for(var i=0;i<$scope.selectedUser.length;i++){
			//add inf exportig user
			var index = $scope.indexInList($scope.selectedUser[i],$scope.exportedUser);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.exportedUser.push($scope.selectedUser[i]);
				
			}
			//remove from exportedUser
			var index = $scope.indexInList($scope.selectedUser[i],$scope.exportingUser);
			if(index!=-1){
				//if present
				$scope.exportingUser.splice(index,1);
			}else{
				//if not present add nothing action
				
			}
		}
	
	
	$scope.selectedUser=[];
		
	}
	
	$scope.addAllUser = function(){
		$scope.selectedUser=[];
		if($scope.exportingUser.length!=0){
			for(var i=0;i<$scope.exportedUser.length;i++){
				$scope.exportingUser.push($scope.exportedUser[i]);
			}
		}else{
			$scope.exportingUser = $scope.exportedUser;
		}
		
		$scope.exportedUser=[];
	}
	
	$scope.removeAllUser = function(){
		$scope.selectedUser=[];
		if($scope.exportedUser.length!=0){
			for(var i=0;i<$scope.exportingUser.length;i++){
				$scope.exportedUser.push($scope.exportingUser[i]);
			}
		}else{
			$scope.exportedUser = $scope.exportingUser;
		}
		
		$scope.exportingUser=[];
	}
	//export utilities
	
	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item, list);

		if(index != -1){
			$scope.usersSelected.splice(index,1);
		}else{
			$scope.usersSelected.push(item);
		}

	};

	$scope.exists = function (item, list) {

		return  $scope.indexInList(item, list)>-1;

	};

	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.userId==item.userId){
				return i;
			}
		}

		return -1;
	};
	$scope.indexRoleInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				return i;
			}
		}

		return -1;
	};
	$scope.selectAll = function(){
		if(!$scope.flagCheck){
			//if it was false then the user check 
			$scope.flagCheck=true;
			$scope.usersSelected=[];
			for(var i=0;i<$scope.users.length;i++){
				$scope.usersSelected.push($scope.users[i]);
			}
		}else{
			$scope.flagCheck=false;
			$scope.usersSelected=[];
		}
		
		
	}
	
	$scope.setTab = function(Tab){
		console.log(Tab);
		if(Tab=='Export'){
			$scope.loadAllUsers();
		}
		$scope.selectedTab = Tab;
	}
	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}
	$scope.showAction = function(text) {
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
	
}