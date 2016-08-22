function editTemporalHierarchyController($scope,sbiModule_translate,$mdPanel,mdPanelRef,sbiModule_config,selectedBusinessModel,$mdDialog){
	$scope.translate=sbiModule_translate;

	//to-do manage edit mode
	$scope.hierarchyList=[];
	$scope.hierarchyColumns=[
		                         {
		                        	label:sbiModule_translate.load("sbi.generic.name"),
		                        	name:"name"
		                         }
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
		mdPanelRef.close();
	};
	$scope.cancelConfiguration=function(){
		mdPanelRef.close();
	};


	$scope.removeHierarchy=function(item){
		alert("remove")
		var confirm = $mdDialog.confirm()
		 .title( sbiModule_translate.load("sbi.meta.delete.hierarchy")  )
		 .ariaLabel('delete hierarchy')
		 .ok(sbiModule_translate.load("sbi.general.continue"))
		 .cancel(sbiModule_translate.load("sbi.general.cancel"));
		   $mdDialog.show(confirm).then(function() {
			   //delete the item;
			   $scope.hierarchyList.splice($scope.hierarchyList.indexOf(item),1);

//			   sbiModule_restServices.promisePost("1.0/metaWeb",(isBusinessClass ? "deleteBusinessClass" : "deleteBusinessView"),metaModelServices.createRequestRest({name:$scope.selectedBusinessModel.uniqueName}))
//			   .then(function(response){
//					metaModelServices.applyPatch(response.data);
//					$scope.selectedBusinessModel=undefined;
//			   },function(response){
//				   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
//			   })


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
			}
	}

}

function addTemporalHierarchyController($scope,sbiModule_translate,$mdPanel,mdPanelRef,selectedBusinessModel,hierarchyList,currentHierarchy){
	$scope.translate=sbiModule_translate;
	$scope.selectedBusinessModel=selectedBusinessModel;
	$scope.columns=angular.copy($scope.selectedBusinessModel.simpleBusinessColumns)

	$scope.currentHierarchy=currentHierarchy==undefined ? {} : currentHierarchy;

	//manage edit
	if(true){
		$scope.currentHierarchy.name=$scope.selectedBusinessModel.name
	}
	$scope.saveConfiguration=function(){
		if(currentHierarchy==undefined){
			//new
			hierarchyList.push($scope.currentHierarchy);
		}else{
			//alter
		}
		mdPanelRef.close();
	};
	$scope.cancelConfiguration=function(){
		mdPanelRef.close();
	};

	$scope.addCol=function(item,index){
		if($scope.currentHierarchy.attributes==undefined){
			$scope.currentHierarchy.attributes=[];
		}
		$scope.currentHierarchy.attributes.push(angular.copy(item));
		item.used=true;
	};
	$scope.removeCol=function(item){
		 for(var i=0;i<$scope.columns.length;i++){
			 if(angular.equals($scope.columns[i].uniqueName,item.uniqueName)){
				 $scope.columns[i].used=false;
				 break;
			 }
		 }
		 $scope.currentHierarchy.attributes.splice($scope.currentHierarchy.attributes.indexOf(item),1);
	}

	$scope.levelType=[
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.millenium"),
	                  		name:"millenium"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.century"),
	                  		name:"century"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.year"),
	                  		name:"year"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.month"),
	                  		name:"month"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.day"),
	                  		name:"day"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.semester"),
	                  		name:"semester"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.quarter"),
	                  		name:"quarter"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.bimester"),
	                  		name:"Bimester"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.week_of_year"),
	                  		name:"week_of_year"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.day_of_year"),
	                  		name:"day_of_year"
	                  	},
	                  	{
	                  		label:sbiModule_translate.load("sbi.meta.hierarchy.level.day_of_week"),
	                  		name:"day_of_week"
	                  	},

	                  ];

	$scope.attributesColumns=[
	                          {
	                        	  name:"name",
	                        	  label:sbiModule_translate.load("sbi.generic.name")
	                          },
	                          {
	                        	  name:"levelType",
	                        	  label:sbiModule_translate.load("sbi.meta.hierarchy.level.type"),
	                        	  transformer:function(item){
	                        		  var template= '<md-input-container layout-fill>'+
	                        		  				'	<md-select ng-model="row.levelType">'+
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
	                        		  var template= '<md-button ng-click="scopeFunctions.moveUp($parent.$parent.$index)" ng-disabled="$parent.$parent.$index==0" class="md-icon-button md-primary" aria-label="MoveUp">'+
	                        		  				'	<md-icon md-font-icon="fa fa-arrow-up"></md-icon>'+
                    		  						'</md-button>'+
                    		  						'<md-button ng-click="scopeFunctions.moveDown($parent.$parent.$index)" ng-disabled="$parent.$parent.$last" class="md-icon-button md-primary" aria-label="MoveDown">'+
                        		  					'	<md-icon md-font-icon="fa fa-arrow-down"></md-icon>'+
                    		  						'</md-button>';

	                        		  return template;
	                        	  }
	                          },

	                         ];
	$scope.attributesActions= [
								 {
									label : 'delete',
									icon:'fa fa-trash' ,
							 		action : function(item,event) {
							 			$scope.removeCol(item);
							 		 }
								 }
								 ];
	$scope.attributesTableScope={
			moveUp:function(row){
				$scope.currentHierarchy.attributes.splice(row-1, 0, $scope.currentHierarchy.attributes.splice(row, 1)[0]);
			},
			moveDown:function(row){
				$scope.currentHierarchy.attributes.splice(row+1, 0, $scope.currentHierarchy.attributes.splice(row, 1)[0]);
			},
			leveltype:$scope.levelType
	}



}