angular.module("cockpitModule").controller("dataIndexesController",['$scope','cockpitModule_template','cockpitModule_datasetServices','$mdDialog','sbiModule_translate','$q','sbiModule_messaging','cockpitModule_documentServices','$timeout','sbiModule_restServices',dataIndexesControllerFunction]);

angular.module("cockpitModule").filter('metatype', function() {
	return function(data) {
		var d= data.split(".");
		return d[d.length-1];
	}
})
angular.module("cockpitModule").filter('parametertype', function() {
	return function(data) {
		var d= {STRING:"String",NUM:"Number",DATE:"Date"}
		return d[data];
	}
})

function dataIndexesControllerFunction($scope,cockpitModule_template,cockpitModule_datasetServices,$mdDialog,sbiModule_translate,$q,sbiModule_messaging,cockpitModule_documentServices,$timeout,sbiModule_restServices){
	$scope.displayIndexesContent=false;
	$timeout(function(){$scope.displayIndexesContent=true;},0);
	var emptyInd={description:"",fields:[]};
	$scope.utils.currentInd=angular.copy(emptyInd);
	$scope.jsonCurrentInd={};	//this is used to have direct response of data
	$scope.tmpEditCurrInd={};
	$scope.showIndented = true;

	$scope.toggleJsonIndented = function() {
		$scope.showIndented = !$scope.showIndented;
	};

	$scope.toggleIndexes=function(objLabel,fieldName,type){
		var found=false;
		//check if this indexes is present
		for(var i=0;i<$scope.utils.currentInd.fields.length;i++){
			if(angular.equals($scope.utils.currentInd.fields[i].store,objLabel) && angular.equals($scope.utils.currentInd.fields[i].type,type)){
				//dataset have one indexes
				if(angular.equals($scope.utils.currentInd.fields[i].column,fieldName)){
					//remove
					$scope.utils.currentInd.fields.splice(i,1);
					delete $scope.jsonCurrentInd[objLabel];
				}else{
					//change
					$scope.jsonCurrentInd = {};
					$scope.utils.currentInd.fields[i].store=objLabel;
					$scope.utils.currentInd.fields[i].column=fieldName;
					$scope.jsonCurrentInd[objLabel]=type+fieldName;
				}

				found=true;
				break;
			}
		}

		if(!found){
			//add it
			$scope.utils.currentInd.fields.pop();
			$scope.jsonCurrentInd = {};
			//delete $scope.jsonCurrentInd[objLabel];
			$scope.utils.currentInd.fields.push({column:fieldName,store:objLabel,type:type});
			$scope.jsonCurrentInd[objLabel]=type+fieldName;
		}

		$scope.refreshIndexesDescriptor($scope.utils.currentInd);
	 }

	 $scope.refreshIndexesDescriptor=function(assoc){
		 var tmpData=[];
		 angular.forEach(assoc.fields,function(item){
			 this.push(item.store+"."+item.column);
		 },tmpData);
		 assoc.description=tmpData.join("=");
	 }

	 $scope.generateIndexesId=function(){
		 var max=0;
		 angular.forEach($scope.tmpIndexes,function(item){
			 var num=parseInt(item.id.split("#")[1]);
			 if(num>max){
				 max=num
			 }
		 });
		 return "#"+(max+1);
	 }

	 $scope.isValidIndexes=function(){
		 var deferred = $q.defer();
		 var stop=false;

		 if ($scope.tmpIndexes == undefined) {
			 stop=true;
		 } else {

			 var copyOfcurrentInd=angular.copy($scope.utils.currentInd);
			 delete copyOfcurrentInd.$$hashKey;
			 delete copyOfcurrentInd.description;
			 delete copyOfcurrentInd.id;
			 for(var k=0; k<$scope.tmpIndexes.length; k++){
				 var tmpInd= angular.copy($scope.tmpIndexes[k]);
				 delete tmpInd.$$hashKey;
				 delete tmpInd.description;
				 delete tmpInd.id;

				 if(angular.equals(tmpInd,copyOfcurrentInd)){
						deferred.reject(sbiModule_translate.load("sbi.cockpit.index.editor.msg.duplicate"));
						stop=true;
						break;
				 }
			 }
		 }
		 if(!stop){
			 deferred.resolve();
		 }

		 return deferred.promise;
	 }

	 $scope.saveCurrentIndexes=function(){
		 $scope.isValidIndexes().then(
				 function(){
					 if($scope.utils.currentInd.id==undefined){
						 $scope.utils.currentInd.id=$scope.generateIndexesId();
					 }
					 $scope.tmpIndexes.unshift( $scope.utils.currentInd);
					 $scope.deleteCurrentIndexes();
					 $scope.tmpEditCurrInd={};
				 },
				 function(message){
				          if(message != undefined) {
                               sbiModule_messaging.showErrorMessage(message, "");
                          }
			     }
		);
	 }

	$scope.deleteCurrentIndexes=function(){
		if(Object.keys($scope.tmpEditCurrInd).length>0){
			//modify of present ass
			$scope.tmpIndexes.unshift( $scope.tmpEditCurrInd);
		}
		$scope.tmpEditCurrInd={};
		$scope.utils.currentInd=angular.copy(emptyInd);
		$scope.jsonCurrentInd={};
	}

	 $scope.deleteIndexes=function(ass){

			var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.cockpit.indexes.delete.title"))
	        .textContent(sbiModule_translate.load("sbi.cockpit.indexes.delete.text"))
	        .ariaLabel('delete indexes')
	        .ok(sbiModule_translate.load("sbi.ds.wizard.confirm"))
	        .cancel(sbiModule_translate.load("sbi.ds.wizard.cancel"));

			$mdDialog.show(confirm).then(function() {
				 $scope.tmpIndexes.splice( $scope.tmpIndexes.indexOf(ass),1);
				 $scope.tmpEditCurrInd={};
				 $scope.deleteCurrentIndexes();
		  });
	 }

	 $scope.editIndexes=function(ass){
		 angular.copy(ass,$scope.tmpEditCurrInd);
		 $scope.tmpIndexes.splice( $scope.tmpIndexes.indexOf(ass),1);
		 angular.copy(ass,$scope.utils.currentInd);
		 $scope.jsonCurrentInd={};
		 angular.forEach(ass.fields,function(item){
			 this[item.store]=item.type+item.column;
		 },$scope.jsonCurrentInd);
	 }

	 $scope.clearIndexes=function(){
		 var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.cockpit.indexes.delete.title"))
	        .textContent(sbiModule_translate.load("sbi.cockpit.indexes.delete.text"))
	        .ariaLabel('delete indexes')
	        .ok(sbiModule_translate.load("sbi.ds.wizard.confirm"))
	        .cancel(sbiModule_translate.load("sbi.ds.wizard.cancel"));

			$mdDialog.show(confirm).then(function() {
				 angular.copy([],$scope.tmpIndexes);
				 $scope.tmpEditCurrInd={};
				 $scope.deleteCurrentIndexes();
		  });
	 }

	 $scope.autodetect=function(){
		var tmpAvaiableDatasetCopy = [];
		angular.copy($scope.tmpAvaiableDataset, tmpAvaiableDatasetCopy);
		for (var k = 0; k < $scope.tmpAssociations.length; k++) {
			var indFields = $scope.tmpAssociations[k].fields;
			for (var j = 0; j < indFields.length; j++) {
				/* Dataset useCache is true */
				var tmp = tmpAvaiableDatasetCopy.filter(function (test) {
					  return test.label == indFields[j].store;
					}
				);
				if (tmp && tmp[0].useCache) {
					var obj = {column:indFields[j].column,store:indFields[j].store,type:indFields[j].type};

					if(!$scope.utils.currentInd.fields) {
						$scope.utils.currentInd.fields= [];
					}

					if (!$scope.tmpIndexes) {
						angular.copy(emptyInd, $scope.tmpIndexes);
					}

					var found = false;
					for (var ll = 0; ll < $scope.tmpIndexes.length; ll++){
						for (var lll = 0; lll < $scope.tmpIndexes[ll].fields.length; lll++){
							var o = $scope.tmpIndexes[ll].fields[lll];
							if (o.column === indFields[j].column &&
									o.store === indFields[j].store &&
									o.type === indFields[j].type){
								found = true;
								break;
							}
						}
						if (found)
							break;
					}

					if (!found) {
						$scope.utils.currentInd.fields.push(obj);
						$scope.jsonCurrentInd[indFields[j].store]=indFields[j].type+indFields[j].column;
						$scope.refreshIndexesDescriptor($scope.utils.currentInd);
						if($scope.utils.currentInd.id==undefined){
							 $scope.utils.currentInd.id=$scope.generateIndexesId();
						 }
						 $scope.tmpIndexes.unshift( $scope.utils.currentInd);
						 $scope.deleteCurrentIndexes();
						 $scope.tmpEditCurrInd={};
					}
				}
			}
		}

	 }
}