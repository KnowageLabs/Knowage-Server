function editTemporalHierarchyController($scope,sbiModule_translate,sbiModule_restServices,metaModelServices ,$mdPanel,mdPanelRef,sbiModule_config,selectedBusinessModel,$mdDialog,originalOlapModels){
	$scope.translate=sbiModule_translate;
	var olapModels=angular.copy(originalOlapModels);

	$scope.hierarchyList=[];
	//load the hierarchy of the selected model if present
	if(olapModels.length>0){

		for(var i=0;i<olapModels[0].dimensions.length;i++){
			var currDim=olapModels[0].dimensions[i];
			if(angular.equals(currDim.table,selectedBusinessModel.uniqueName)){
				for(var d=0;d<currDim.hierarchies.length;d++){
					var currH=currDim.hierarchies[d];
					var tmpH={};
					tmpH.name=currH.name;
					tmpH.properties={};
					for(var p=0;p<currH.properties.length;p++){
						var currProp=currH.properties[p];
						var key = Object.keys(currProp)[0];
						var val=currProp[key].value || currProp[key].propertyType.defaultValue;
						if(angular.equals(val,"true")){
							val=true;
						}
						if(angular.equals(val,"false")){
							val=false;
						}
						tmpH.properties[currProp[key].propertyType.id.split(".")[1]]=val;
					}

					tmpH.levels=[];
					for(var l=0;l<currH.levels.length;l++){
						var currL=currH.levels[l];

						//load the current levels levelType from the properties
						for(var pro=0;pro<currL.properties.length;pro++){
							var key = Object.keys(currL.properties[pro])[0];
							if(angular.equals(key.split(".")[1],"leveltype")){
								currL.leveltype=currL.properties[pro][key].value;
							}
						}


						tmpH.levels.push(angular.copy(currL));
					}

					$scope.hierarchyList.push(tmpH)
				}
			}
		}
	}

	$scope.hierarchyColumns=[
		                         {
		                        	label:sbiModule_translate.load("sbi.generic.name"),
		                        	name:"name"
		                         },

		                         {
		                        	 label:sbiModule_translate.load("sbi.meta.isDefaultHierarchy"),
		                        	 name:"l",
		                        	 transformer:function(row){
		                        		 return '<md-checkbox ng-checked="row.properties.defaultHierarchy==true" ng-click="scopeFunctions.toggleDefault(row)"></md-checkbox>';

		                        	 }
		                         },

		                         {
		                        	 label:sbiModule_translate.load("sbi.meta.hierarchy"),
		                        	 name:"levels",
		                        	 transformer:function(levels){
		                        		 var lis=[];
		                        		 angular.forEach(levels,function(item){
		                        			 this.push(item.name)
		                        		 },lis)
		                        		 return lis.join(" - ");
		                        	 }
		                         },

	                         ];


	$scope.hierarchyActions=[
									{
										label : 'edit',
										icon:'fa fa-edit' ,
											action : function(item,event) {
												$scope.manageHierarchy(item);
											 }
									 },
									 {
										 label : 'delete',
										 icon:'fa fa-trash' ,
										 action : function(item,event) {
											 $scope.removeHierarchy(item);
										 }
									 },
	                         ];


	$scope.saveConfiguration=function(){

		var dataToSend={};
		dataToSend.businessModelUniqueName=selectedBusinessModel.uniqueName;
		dataToSend.hierarchy=$scope.hierarchyList;
		 sbiModule_restServices.promisePost("1.0/metaWeb","alterTemporalHierarchy",metaModelServices.createRequestRest(dataToSend))
		   .then(function(response){
				metaModelServices.applyPatch(response.data);
		   },function(response){
			   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		   })
		mdPanelRef.close();
	};
	$scope.cancelConfiguration=function(){
		mdPanelRef.close();
	};


	$scope.removeHierarchy=function(item){
		var confirm = $mdDialog.confirm()
		 .title( sbiModule_translate.load("sbi.meta.delete.hierarchy")  )
		 .ariaLabel('delete hierarchy')
		 .ok(sbiModule_translate.load("sbi.general.continue"))
		 .cancel(sbiModule_translate.load("sbi.general.cancel"));
		   $mdDialog.show(confirm).then(function() {
			   //delete the item;
			   $scope.hierarchyList.splice($scope.hierarchyList.indexOf(item),1);
		   }, function() {
		   });



	}

	$scope.manageHierarchy=function(item){
		var config = {
				attachTo:  angular.element(document.body),
				controller: addTemporalHierarchyController,
				disableParentScroll: true,
				templateUrl: sbiModule_config.contextName + '/js/src/meta/templates/addTemporalHierarchy.jsp',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {
					selectedBusinessModel:selectedBusinessModel,
					hierarchyList:$scope.hierarchyList,
					currentHierarchy:item
					},

		};

		$mdPanel.open(config);
	}

	$scope.hierarchyTableScope={
			translate:sbiModule_translate,
			manageHierarchy:function(){
				$scope.manageHierarchy();
			},
			toggleDefault:function(row){
				angular.forEach($scope.hierarchyList,function(item,index){
					if(item.properties==undefined){
						item.properties={}
					}
					item.properties.defaultHierarchy=angular.equals(item,row);
				})
			}
	}

}

function addTemporalHierarchyController($scope,sbiModule_translate,$mdPanel,mdPanelRef,selectedBusinessModel,hierarchyList,currentHierarchy){
	$scope.translate=sbiModule_translate;
	$scope.selectedBusinessModel=selectedBusinessModel;
	$scope.columns=angular.copy($scope.selectedBusinessModel.simpleBusinessColumns)

	$scope.currentHierarchy= {};
	if(hierarchyList.length==0){
		$scope.currentHierarchy.properties={defaultHierarchy:true};
	}
	if(currentHierarchy==undefined){
		$scope.currentHierarchy.name="";
	}else{
		$scope.currentHierarchy=currentHierarchy;

		//check as used all the column present in the hierarchy
		for(var i=0;i<$scope.columns.length;i++){
			for(var j=0;j<$scope.currentHierarchy.levels.length;j++){
				if(angular.equals($scope.columns[i].uniqueName,$scope.currentHierarchy.levels[j].column.uniqueName)){
					$scope.columns[i].used=true;
					break;
				}
			}
		}


	}
	$scope.saveConfiguration=function(){
		if(currentHierarchy==undefined){
			//new
			hierarchyList.push($scope.currentHierarchy);
		}else{
			//alter
		}
		mdPanelRef.close();
		$scope.$destroy();
	};
	$scope.cancelConfiguration=function(){
		mdPanelRef.close();
		$scope.$destroy();
	};

	$scope.addCol=function(item,index){
		if($scope.currentHierarchy.levels==undefined){
			$scope.currentHierarchy.levels=[];
		}
		var newItem={
				name:item.name,
				uniqueName:item.uniqueName,
				leveltype: $scope.levelType[$scope.levelType.length-1].name,
				column:angular.copy(item)
		}

		$scope.currentHierarchy.levels.push(newItem);
		item.used=true;
	};
	$scope.removeCol=function(item){
		 for(var i=0;i<$scope.columns.length;i++){
			 if(angular.equals($scope.columns[i].uniqueName,item.uniqueName)){
				 $scope.columns[i].used=false;
				 break;
			 }
		 }
		 $scope.currentHierarchy.levels.splice($scope.currentHierarchy.levels.indexOf(item),1);
	}

	$scope.levelType=[
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.millennium"),
	                  		name:"MILLENNIUM"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.century"),
	                  		name:"CENTURY"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.year"),
	                  		name:"YEAR"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.month"),
	                  		name:"MONTH"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.day"),
	                  		name:"DAY"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.semester"),
	                  		name:"SEMESTER"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.quarter"),
	                  		name:"QUARTER"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.bimester"),
	                  		name:"BIMESTER"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.week_of_year"),
	                  		name:"WEEK_OF_YEAR"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.day_of_year"),
	                  		name:"DAY_OF_YEAR"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.day_of_week"),
	                  		name:"DAY_OF_WEEK"
	                  	},

	                  ];

	$scope.levelsColumns=[
	                          {
	                        	  name:"name",
	                        	  label:sbiModule_translate.load("sbi.generic.name")
	                          },
	                          {
	                        	  name:"levelType",
	                        	  label:sbiModule_translate.load("sbi.meta.hierarchy.level.type"),
	                        	  transformer:function(item){
	                        		  var template= '<md-input-container layout-fill>'+
	                        		  				'	<md-select ng-model="row.leveltype">'+
                    		  						'	<md-option ng-repeat="type in scopeFunctions.leveltype" ng-value="type.name">'+
                    		  						'	{{type.label}}'+
                    		  						'	</md-option>'+
                        		  					'	</md-select>'+
                    		  						'</md-input-container>';

	                        		  return template;
	                        	  }
	                          },
	                          {
	                        	  name:"order",
	                        	  size:"100px",
	                        	  label:" ",
	                        	  transformer:function(item){
	                        		  var template= '<md-button ng-click="scopeFunctions.moveUp($parent.$parent.$index)" ng-disabled="$parent.$parent.$index==0" class="md-icon-button" aria-label="MoveUp">'+
	                        		  				'	<md-icon md-font-icon="fa fa-arrow-up"></md-icon>'+
                    		  						'</md-button>'+
                    		  						'<md-button ng-click="scopeFunctions.moveDown($parent.$parent.$index)" ng-disabled="$parent.$parent.$last" class="md-icon-button" aria-label="MoveDown">'+
                        		  					'	<md-icon md-font-icon="fa fa-arrow-down"></md-icon>'+
                    		  						'</md-button>';

	                        		  return template;
	                        	  }
	                          },

	                         ];
	$scope.levelsActions= [
								 {
									label : 'delete',
									icon:'fa fa-trash' ,
							 		action : function(item,event) {
							 			$scope.removeCol(item);
							 		 }
								 }
								 ];
	$scope.levelsTableScope={
			moveUp:function(row){
				$scope.currentHierarchy.levels.splice(row-1, 0, $scope.currentHierarchy.levels.splice(row, 1)[0]);
			},
			moveDown:function(row){
				$scope.currentHierarchy.levels.splice(row+1, 0, $scope.currentHierarchy.levels.splice(row, 1)[0]);
			},
			leveltype:$scope.levelType
	}

	$scope.isValidHierarchy=function(){
		if($scope.currentHierarchy.levels==undefined || $scope.currentHierarchy.levels.length==0){
			return false;
		}
		if(angular.equals($scope.currentHierarchy.name.trim(),"")){
			return false;
		}

		return true;
	}

}