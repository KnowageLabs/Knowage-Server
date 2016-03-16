angular.module('olap_panel',[])
.directive('olapPanel',function(){
	return{
		restrict: "E",
		replace: 'true',
		templateUrl: '/knowagewhatifengine/html/template/main/olap/olapPanel.html',
		controller: olapPanelController
	}
});

function olapPanelController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {
	
	$scope.drillDown = function(axis, position, member, uniqueName,positionUniqueName) {
		sbiModule_restServices.promiseGet
		("1.0",'/member/drilldown/'+ axis+ '/'+ position+ '/'+ member+ '/'+ positionUniqueName+ '/'+ uniqueName+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured by drill down functionality", 'Error');
			
		});		
	}

	$scope.drillUp = function(axis, position, member, uniqueName,positionUniqueName) {
		sbiModule_restServices.promiseGet
		("1.0",'/member/drillup/'+ axis+ '/'+ position+ '/'+ member+ '/'+ positionUniqueName+ '/'+ uniqueName+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured by drill down functionality", 'Error');
			
		});		
	}

	$scope.swapAxis = function() {
		sbiModule_restServices.promisePost("1.0/axis/swap?SBI_EXECUTION_ID="+JSsbiExecutionID,"")
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured during swap axis functionality", 'Error');
			
		});	
	}
	
	$scope.drillThrough = function(ordinal) {

		var c = JSON.stringify($scope.dtAssociatedLevels);
		console.log(c);
		 switch (arguments.length) {
		    case 0:
		    	console.log("FROM DIALOG");
		    	sbiModule_restServices.promiseGet
				("1.0",'/member/drilltrough/'+ $scope.ord+ '/'+c+ '/'+ $scope.dtMaxRows+ '/' + '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
				.then(function(response,ev) {
					$scope.dt = response.data;
					$scope.dtData = response.data;
					$scope.dtColumns = Object.keys(response.data[0]);
					$scope.formateddtColumns =$scope.formatColumns($scope.dtColumns);
				    }, function(response) {
					sbiModule_messaging.showErrorMessage("error", 'Error');
					
						});
		        break;
		    case 1:
		    	console.log("FROM CELL");
		    	$scope.ord = ordinal;
		    	sbiModule_restServices.promiseGet
				("1.0",'/member/drilltrough/'+ ordinal + '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
				.then(function(response,ev) {
					$scope.dt = response.data;
					console.log($scope.dt);
					$scope.dtData = response.data;
					$scope.dtColumns = Object.keys(response.data[0]);
					$scope.formateddtColumns =$scope.formatColumns($scope.dtColumns);
					console.log($scope.formateddtColumns);
					$scope.getCollections();
					$scope.openDtDialog();

				    }, function(response) {
					sbiModule_messaging.showErrorMessage("error", 'Error');
					
						});
		        break;
		    default:
		        break;
		    }
		 $scope.exportDrill = function() {
				
				var json = JSON.stringify($scope.dtData);
				delete $scope.dtData.$$hashKey;
				console.log(json);
					sbiModule_restServices.promiseGet
					("1.0",'/member/drilltrough/export/'+ json+ '/' + '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
					.then(function(response) {
						$scope.dtData = [];
					    }, function(response) {
						sbiModule_messaging.showErrorMessage("error", 'Error');
						
							});
		}
		}
	
	$scope.getProps = function() {
		sbiModule_restServices.promiseGet
		("1.0",'/member/properties/'+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
		.then(function(response) {
			console.log(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured by drill down functionality", 'Error');
			
		});		
	}
	
		$scope.formatColumns = function(array){
			var arr = [];
			for (var i = 0; i < array.length; i++) {
				var obj = {};
				obj.label = array[i].toUpperCase();
				obj.name = array[i];
				obj.size = "100px";
				arr.push(obj);
			}
			return arr;
		}
		
		$scope.switchPosition = function(data){
			 
			$scope.moveHierarchies(data.axis, data.uniqueName, data.positionInAxis+1,1,data);
			 if(data.axis == 0){			 
				 var pom = $scope.columns[data.positionInAxis];
				 var pia = data.positionInAxis;

				 $scope.columns[pia].positionInAxis = pia + 1;
				 $scope.columns[pia+1].positionInAxis = pia;
				 
				 $scope.columns[pia] = $scope.columns[pia+1];
				 $scope.columns[pia+1] = pom;
				 
			 }
			 else if(data.axis == 1){			 
				 var pom = $scope.rows[data.positionInAxis];
				 var pia = data.positionInAxis;

				 $scope.rows[pia].positionInAxis = pia + 1;
				 $scope.rows[pia+1].positionInAxis = pia;
				 
				 $scope.rows[pia] = $scope.rows[pia+1];
				 $scope.rows[pia+1] = pom;
				 
			 }


		};
		
	$scope.getCollections = function() {
		
		sbiModule_restServices.promiseGet
		("1.0",'/member/drilltrough/levels/?SBI_EXECUTION_ID=' + JSsbiExecutionID)
		.then(function(response) {
			console.log(response);
			$scope.dtTree = response.data;
		    }, function(response) {
			sbiModule_messaging.showErrorMessage("error", 'Error');
			
				});
		}

	
	
	/**dragan*/
	/*writeback funtionality*/
	/**
	 * @property {String} lastEditedFormula
	 *  the last edited formula. To restore the formula
	 */
	$scope.lastEditedFormula=null;
	
	/**
	 * @property {String} lastEditedCell
	 *  the last edited formula. To restore the formula
	 */
	$scope.lastEditedCell= null,
		
	$scope.value = null;
		
	$scope.makeEditable = function(id,measureName){
		
		var unformattedValue = "";
		var modelStatus = null;
		
		modelStatus = $scope.modelConfig.status;
		
		if(modelStatus  == 'locked_by_other' || modelStatus  == 'unlocked'){
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.writeback.edit.no.locked'), 'Error');
			console.log('sbi.olap.writeback.edit.no.locked');
			return;
		}
		if($scope.modelConfig && $scope.isMeasureEditable(measureName)){
			
			var cell = angular.element(document.querySelector("[id='"+id+"']"));
			console.log(cell[0].childNodes[0].data);
		}
		

			//check if the user is editing the same cell twice. If so we present again the last formula
			if($scope.lastEditedFormula && $scope.lastEditedCell && id.startsWith($scope.lastEditedCell)){
				unformattedValue = $scope.lastEditedFormula;
			}else{
				var type = "float";
				var originalValue = "";

			
					originalValue = (cell[0].childNodes[0].data).trim();
					if (originalValue  == '') { // in case the cell was empty, we type 0
						unformattedValue = 0;
					} else {
						unformattedValue = parseFloat(originalValue.replace(',','.'));//Sbi.whatif.commons.Format.cleanFormattedNumber(originalValue, Sbi.locale.formats[type]);
						console.log(originalValue);
						console.log(unformattedValue);
					}
				
					//Sbi.error("Error loading the value of the cell to edit" + err);
				

				//it's not possible to edit a cell with value 0
				if(unformattedValue ==0){
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.writeback.edit.no.zero'),'Error');
					return;
				}
				
			$scope.showEditCell(cell,id,originalValue);
				
				
			}		
	}
	
	/**
	*checks if measure is editable
	* @param measureName the name of the measure to check
	* @returns {Boolean} return if the measure is editable
	*/
	$scope.isMeasureEditable = function(measureName){
		if($scope.modelConfig && $scope.modelConfig.writeBackConf){
			if($scope.modelConfig.writeBackConf.editableMeasures  == null || $scope.modelConfig.writeBackConf.editableMeasures.length ==0){
				return true;
			}else{
				var measures = ($scope.modelConfig.writeBackConf.editableMeasures);
				
				for(measureNameCheck in measures){
					if (measureNameCheck === measureName);
					var contained = measureName;
					return contained;
				}
				
			
		}
		return false;
	}
		
	}
	$scope.writeBackCell = function(id, value, startValue, originalValue){
		console.log("writeBackCell");
		var type = "float";
		if ( startValue ) {
			startValue = parseFloat(startValue);//Sbi.whatif.commons.Format.cleanFormattedNumber(startValue, Sbi.locale.formats[type]);
		}
		if ( value != startValue ) {
			var position = "";
			var unformattedValue = value;

			if ( id ) {
				var endPositionIndex = id.indexOf("!");
				position= id.substring(0, endPositionIndex);
			}

			
				if ( !isNaN(value) ) {
					//Value is a number
					unformattedValue = parseFloat(value);//Sbi.whatif.commons.Format.formatInJavaDouble(value, Sbi.locale.formats[type]);
				} else {
					//Value is a string/expression
					unformattedValue = value;
				}
			

			//update the last edited values
			this.lastEditedFormula = unformattedValue;
			var separatorIndex = id.lastIndexOf('!');
			this.lastEditedCell = id.substring(0,separatorIndex);
			console.log(unformattedValue+'!!!!!!!!!!!!!!!!');
			$scope.sendWriteBackCellService(position, unformattedValue);
		} else {
			/*Sbi.debug("The new value is the same as the old one");
			var cell = Ext.get(id);
			cell.dom.childNodes[0].data = originalValue;*/
			console.log(originalValue);
		}
	}
	
	$scope.showEditCell = function(cell,id,originalValue){
		console.log(cell[0]);
			cell[0].style.setProperty('position','fixed','important');
			 
			var textLength = (cell[0].childNodes[0].data).trim().length;
			var startVaue = cell[0].childNodes[0].data.trim();
			var textFontSize = cell[0].style.fontSize;
			console.log(textFontSize);
			var cellWidth = 250 + 12*textLength;
			cell[0].style.setProperty('z-index','500');
			cell.css('width',cellWidth);
			cell.css('transform','translatey(-14px)');//transform: translatey(-7px);
			
				$mdDialog
			.show({
				scope : $scope,
				parent: cell,
				preserveScope : true,
				controller : function DialogController($scope,$mdDialog){
					$scope.closeDialog = function(e){
						if(e.keyCode===13){
						$mdDialog.hide();
							
						$scope.writeBackCell( id, $scope.value,  startVaue, originalValue);
		}
						
					}
					
					
				},
				template : "<md-dialog style='min-height: 30px;position: absolute;left: 0;top:0'><input ng-model='value' type='text' style='width: 190px;transform:translateX(50px);' ng-keypress='closeDialog($event)'><input type='button'  ng-click='showFormulaDialog()' style='position:absolute;left:0px;top:0px' value='f(x)'></md-dialog>",
					onRemoving: function(){
						cell.css('width','inherit');
						cell[0].style.setProperty('position','relative','important');
						cell[0].style.setProperty('z-index','1');
						cell.css('transform','translatey(0px)');
						console.log($scope.value);
						
						
					},
				clickOutsideToClose : true,
					autoWrap:false
			});
		
		
	}
	$scope.showFormulaDialog = function(){
		
	
		$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				controllerAs : 'olapCtrl',
				templateUrl : '/knowagewhatifengine/html/template/main/toolbar/writeBackCell.html',
				//targetEvent : ev,
				clickOutsideToClose : false,
				hasBackdrop:false
			});
	}
	
	$scope.sendWriteBackCellService = function(ordinal,expression){
		
		 var path = '/model/setValue/'+ordinal+'?SBI_EXECUTION_ID='+JSsbiExecutionID; 
		  
		  var st = {'expression':expression};
		 
		 sbiModule_restServices.promisePost("1.0/model/setValue/"+ordinal+"?SBI_EXECUTION_ID="+JSsbiExecutionID,"",st)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("error", 'Error');
				
			});	
	}
	/****************************************************************************/
	
	$scope.dimensionShift = function(direction){
		if(direction == 'left' && $scope.columns.length-1-$scope.topStart >= $scope.maxCols){
		      $scope.topStart++;      
	    }
	    if(direction == 'right' && $scope.topStart>0){
	      $scope.topStart--;
	    }
	    if(direction == 'up' && $scope.rows.length-1-$scope.leftStart >= $scope.maxRows){
	    	$scope.leftStart++;
	    }
	    if(direction == 'down' && $scope.leftStart){
	    	$scope.leftStart--;
	    }
	    
	}
	

	$scope.openDtDialog = function(ev) {
		$scope.dtAssociatedLevels= []; 
		$mdDialog
		.show({
			scope : $scope,
			preserveScope : true,
			controllerAs : 'olapCtrl',
			templateUrl : '/knowagewhatifengine/html/template/main/toolbar/drillThrough.html',
			targetEvent : ev,
			clickOutsideToClose : true
			
		});
	  };
	  $scope.closeDialog = function(ev) {
		  $mdDialog.hide();
      }; 
      
      
      $scope.showCCWizard = function(){
  		
    		
  		$mdDialog
  			.show({
  				scope : $scope,
  				preserveScope : true,
  				controllerAs : 'olapCtrl',
  				templateUrl : '/knowagewhatifengine/html/template/main/calculatedfields/calculatedFields.html',
  				//targetEvent : ev,
  				clickOutsideToClose : false,
  				hasBackdrop:false
  			});
  	}

	
};

