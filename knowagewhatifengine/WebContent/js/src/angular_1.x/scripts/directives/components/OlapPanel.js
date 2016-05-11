
angular.module('olap_panel',[])
.directive('olapPanel',function(){
	return{
		restrict: "E",
		replace: 'true',
		templateUrl: '/knowagewhatifengine/html/template/main/olap/olapPanel.html',
		controller: olapPanelController
	}
});

function olapPanelController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate,toastr,$cookies,sbiModule_docInfo,sbiModule_config) {
	
	
	
	$scope.drillDown = function(axis, position, member, uniqueName,positionUniqueName) {
		var encoded = encodeURI('/member/drilldown/'+ axis+ '/'+ position+ '/'+ member+ '/'+ positionUniqueName+ '/'+ uniqueName+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promiseGet
		("1.0",encoded)
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured by drill down functionality", 'Error');
			
		});		
	}
	
	$scope.drillUp = function(axis, position, member, uniqueName,positionUniqueName) {
		var encoded = encodeURI('/member/drillup/'+ axis+ '/'+ position+ '/'+ member+ '/'+ positionUniqueName+ '/'+ uniqueName+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promiseGet
		("1.0",encoded)
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured by drill up functionality", 'Error');
			
		});		
	}

	$scope.swapAxis = function() {
		var encoded = encodeURI("1.0/axis/swap?SBI_EXECUTION_ID="+JSsbiExecutionID);
		sbiModule_restServices.promisePost(encoded,"")
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured during swap axis functionality", 'Error');
			
		});	
	}
	
	$scope.enableDisableDrillThrough = function() {
		
		
		
		if($scope.dtAssociatedLevels.length == 0 && $scope.dtMaxRows == 0){
			var encoded = encodeURI('/member/drilltrough/'+ $scope.selectedCell.ordinal + '?SBI_EXECUTION_ID=' + JSsbiExecutionID);
			sbiModule_restServices.promiseGet
			("1.0",encoded)
			.then(function(response,ev) {
				$scope.dtLoadingShow = true;
				$scope.dtData = [];
				setTimeout(function(){
					
					$scope.dtData = response.data;
					$scope.dtColumns = Object.keys(response.data[0]);
					$scope.formateddtColumns =$scope.formatColumns($scope.dtColumns);
					$scope.getCollections();
					$scope.openDtDialog();
					
					
					
				
					$scope.dtLoadingShow = false;
					$scope.$apply();
				},1000)
				
			    }, function(response) {
				sbiModule_messaging.showErrorMessage("Please select cell for DrillThrough", 'Error');
				$scope.dtLoadingShow = false;
					});	
			
		}else {
			console.log("IZ DIALOGAA");
			var c = JSON.stringify($scope.dtAssociatedLevels);
			var encoded = encodeURI('/member/drilltrough/'+ $scope.selectedCell.ordinal+ '/'+c+ '/'+ $scope.dtMaxRows+ '/' + '?SBI_EXECUTION_ID=' + JSsbiExecutionID);
			sbiModule_restServices.promiseGet
			("1.0",encoded)
			.then(function(response,ev) {
				$scope.dt = response.data;
				$scope.dtData = response.data;
				$scope.dtColumns = Object.keys(response.data[0]);
				$scope.formateddtColumns =$scope.formatColumns($scope.dtColumns);
			    }, function(response) {
				sbiModule_messaging.showErrorMessage("error", 'Error');
				
					});
			
			
		}
		}
	
	 $scope.exportDrill = function(JSONData, ReportTitle, ShowLabel) {
		 
		 JSONData = angular.toJson( JSONData );
			
		 //If JSONData is not an object then JSON.parse will parse the JSON string in an Object
		    var arrData = typeof JSONData != 'object' ? JSON.parse(JSONData) : JSONData;
		    
		    var CSV = '';    
		    //Set Report title in first row or line
		    
		    CSV += ReportTitle + '\r\n\n';

		    //This condition will generate the Label/Header
		    if (ShowLabel) {
		        var row = "";
		        
		        //This loop will extract the label from 1st index of on array
		        for (var index in arrData[0]) {
		            
		            //Now convert each value to string and comma-seprated
		            row += index + ',';
		        }

		        row = row.slice(0, -1);
		        
		        //append Label row with line break
		        CSV += row + '\r\n';
		    }
		    
		    //1st loop is to extract each row
		    for (var i = 0; i < arrData.length; i++) {
		        var row = "";
		        
		        //2nd loop will extract each column and convert it in string comma-seprated
		        for (var index in arrData[i]) {
		            row += '"' + arrData[i][index] + '",';
		        }

		        row.slice(0, row.length - 1);
		        
		        //add a line break after each row
		        CSV += row + '\r\n';
		    }

		    if (CSV == '') {        
		        alert("Invalid data");
		        return;
		    }   
		    
		    //Generate a file name
		    var fileName = "MyReport_";
		    //this will remove the blank-spaces from the title and replace it with an underscore
		    fileName += ReportTitle.replace(/ /g,"_");   
		    
		    //Initialize file format you want csv or xls
		    var uri = 'data:text/csv;charset=utf-8,' + escape(CSV);
		    
		    // Now the little tricky part.
		    // you can use either>> window.open(uri);
		    // but this will not work in some browsers
		    // or you will not get the correct file extension    
		    
		    //this trick will generate a temp <a /> tag
		    var link = document.createElement("a");    
		    link.href = uri;
		    
		    //set the visibility hidden so it will not effect on your web-layout
		    link.style = "visibility:hidden";
		    link.download = fileName + ".csv";
		    
		    //this part will append the anchor tag and remove it after automatic click
		    document.body.appendChild(link);
		    link.click();
		    document.body.removeChild(link);
	}
	
	$scope.getProps = function(memberUniqueName) {
	var encoded = encodeURI('/member/properties/'+memberUniqueName+'?SBI_EXECUTION_ID='+JSsbiExecutionID);
		sbiModule_restServices.promiseGet
		("1.0",encoded)
		.then(function(response) {
			console.log(response.data);
			$scope.propertiesArray = response.data;
			$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent: angular.element(document.body),
				controllerAs : 'olapCtrl',
				templateUrl : '/knowagewhatifengine/html/template/main/toolbar/properties.html',
				//targetEvent : ev,
				clickOutsideToClose : false,
				hasBackdrop:false
			});
			
			
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while getting properties for selected member", 'Error');
			
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
		var encoded = encodeURI('/member/drilltrough/levels/?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promiseGet
		("1.0", encoded)
		.then(function(response) {
			console.log(response);
			$scope.dtTree = response.data;
		    }, function(response) {
			sbiModule_messaging.showErrorMessage("error", 'Error');
			
				});
		}
	
	$scope.checkCheckboxes = function (item, list) {
		if(item.hasOwnProperty("name")){
			var index = $scope.indexInList(item, list);

			if(index != -1){
				$scope.dtAssociatedLevels.splice(index,1);
			}else{
				$scope.dtAssociatedLevels.push(item);
			}
		} 
	};
	
	$scope.indexInList=function(item, list) {
		if(item.hasOwnProperty("name")){
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				return i;
			}
		}
		}
		return -1;
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
		  
		  $scope.dtData = [];
		  $scope.dtAssociatedLevels = [];
		  $scope.selectedMDXFunction = {};
		  $scope.selectedMDXFunctionName ="";
		  $scope.selectedCrossNavigation = null;
		  $scope.selectedTab= 0;
		  $mdDialog.cancel();
      }; 
     
      $scope.showCCWizard = function(){
    	  
    	console.log($scope.olapDocName);
    		$mdDialog
    			.show({
    				scope : $scope,
    				preserveScope : true,
    				controllerAs : 'olapCtrl',
    				templateUrl : '/knowagewhatifengine/html/template/main/calculatedfields/calculatedFields.html',
    				clickOutsideToClose : false,
    				hasBackdrop:false
    			});
    		
    		$scope.getFromCookies();
    	}
      
    $scope.checkValidity = function(){
    	
    	if($scope.selectedMDXFunction.type == null || $scope.selectedMDXFunctionName == ""){
    		return true;
    	}else{
    
    		return false;
    	}
    }
    
    
    
    $scope.cellClickCreateCrossNavigationMenu = function(ordinal){
    	var encoded = encodeURI('/crossnavigation/getCrossNavigationUrl/'+ordinal+'?SBI_EXECUTION_ID=' + JSsbiExecutionID);
    	sbiModule_restServices.promisePost
		("1.0",encoded)
		.then(function(response) {
			$scope.handleResponse(response);
			try{
				eval(response.data);
			}catch ( e){
				sbiModule_messaging.showErrorMessage("error", 'Error');
			}
			
		 }, function(response) {
			sbiModule_messaging.showErrorMessage("error", 'Error');
		});
    }
    
    $scope.checkValidityCrossNav = function(){
    	
    	if($scope.selectedCrossNavigationDocument == null){
    		return true;
    	}else{
    		if($scope.selectedCrossNavigationDocument.title == ""){
    			return true;
    		}else{
    			return false;
    		}
    	}
    }

	$scope.selectMDXFunction = function(obj) {
		$scope.selectedMDXFunction = obj;
		console.log($scope.selectedMDXFunction);
	}
		
	$scope.openArgumentsdialog = function(){
		
		$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent: angular.element(document.body),
				controllerAs : 'olapCtrl',
				templateUrl : '/knowagewhatifengine/html/template/main/calculatedfields/argumentsDialog.html',
				//targetEvent : ev,
				clickOutsideToClose : false,
				hasBackdrop:false
			});
		
	}
	
$scope.openSavedSets = function(){
		
		$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent: angular.element(document.body),
				controllerAs : 'olapCtrl',
				templateUrl : '/knowagewhatifengine/html/template/main/calculatedfields/savedSets.html',
				clickOutsideToClose : true,
				hasBackdrop:false
			});
		
	}

	
		
	$scope.formatValues = function(index,obj) {
		var value = null;
		if(obj.expected_value == "Set_Expression"){
		value="{";
		if ($scope.members.length>=1) {
			value += $scope.members[0].uniqueName;
		}
		for(var i =1;i< $scope.members.length;i++){
        value +=  ","+ $scope.members[i].uniqueName ;	
        }
		value +="}"
		$scope.selectedMDXFunction.argument[index].default_value = value;
		
		}else if(obj.expected_value == "Level_Expression"){
			for(var i =0;i< $scope.members.length;i++){
		        value = $scope.members[i].level ;	
		        }
			$scope.selectedMDXFunction.argument[index].default_value = value;
			
		}
		else{
			for(var i =0;i< $scope.members.length;i++){
		        value = $scope.members[i].uniqueName ;	
		        }
			$scope.selectedMDXFunction.argument[index].default_value = value;
		}
		$scope.members = [];
		$scope.sendModelConfig($scope.modelConfig);
		return value;
			
	}
	
	$scope.enterSelectMode = function(index,obj) {
		
		
		$mdDialog.hide();
		toastr.info('Click ok to finish selection<br /><br /><md-button class="md-raised">OK</md-button>' , { 
			  allowHtml: true,
			  timeOut: 0,
			  extendedTimeOut: 0,
			  
			  onTap: function() {
				  $scope.valuesArray.push($scope.formatValues(index,obj)); 
				  
					  
				  $scope.openArgumentsdialog();
				  toastr.clear();
				  
			  }
			  
			});
		
	}
	

	var formatFormulaforSending = function(){
		
		var value = $scope.selectedMDXFunction.name + "(";
		
		if ($scope.selectedMDXFunction.argument.length>=1) {
			value+=$scope.selectedMDXFunction.argument[0].default_value;
		}
		for (var i = 1; i < $scope.selectedMDXFunction.argument.length; i++) {
			if($scope.selectedMDXFunction.argument[i].default_value != undefined){
				value+=","+$scope.selectedMDXFunction.argument[i].default_value;
			}
		}
		value +=")";
		$scope.finalFormula = value;
		
		
	}
	
	var cleanCC = function() {
		
		$scope.selectedMDXFunction = {}; 
		$scope.selectedMDXFunctionName ="";
		$scope.selectedTab= 0;
		
	}
	
var checkForDuplicates = function(type) {
		
	for (var i = 0; i < $scope.cookieArray.length; i++) {
		if(type.name === $scope.cookieArray[i].name){
			console.log("same item replacing");
			$scope.cookieArray.splice(i,1);
		
		}
	}	
		
	}
	
	var doMemberSaving = function() {
		
		var encoded = encodeURI('/calculatedmembers/execute/'+$scope.selectedMDXFunctionName+'/'+$scope.finalFormula+'/'+$scope.selectedMember.parentMember+'/'+$scope.selectedMember.axisOrdinal+'?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promisePost
		("1.0",encoded)
		.then(function(response) {
			$scope.handleResponse(response);
			
			var namedMember = {
					'docName': sbiModule_docInfo.label,
					'name':$scope.selectedMDXFunctionName,
				    'value': $scope.finalFormula,
				    'type': 'Member',
				    'formula': 	$scope.selectedMDXFunction,
				    'img' : sbiModule_config.contextName + "/img/m.png"
				}
			
			checkForDuplicates(namedMember);
			
			$scope.cookieArray.push(namedMember);
			$cookies.putObject('data',$scope.cookieArray);
			cleanCC();
			sbiModule_messaging.showSuccessMessage("Member is saved", 'Success');
			
			
			
		    }, function(response) {
			sbiModule_messaging.showErrorMessage("Error adding Calculated Field", 'Error');
			cleanCC()
				});
	
	}
	
	var doSetSaving = function() {
		
		var namedSet = {
				'docName': sbiModule_docInfo.label,	
				'name':$scope.selectedMDXFunctionName,
			    'value': $scope.finalFormula,
			    'type': 'Set',
			    'formula': 	$scope.selectedMDXFunction,
			    'img' : sbiModule_config.contextName + "/img/s.png"
			    
			}
			
			checkForDuplicates(namedSet);
		
			$scope.cookieArray.push(namedSet);
			$cookies.putObject('data',$scope.cookieArray);
			cleanCC();
			sbiModule_messaging.showSuccessMessage("Set is saved", 'Success');
			
	
	}
	
$scope.sendCC = function() {
	
	formatFormulaforSending();
	
	if($scope.selectedMDXFunction.output != "Set"){
	
		doMemberSaving();
		
	}
	
	if($scope.selectedMDXFunction.output == "Set"){
		
		doSetSaving();	
	}

	$mdDialog.hide();
	}
	
	$scope.getFromCookies = function() {
		
	if($cookies.getObject('data') != undefined){
		
		$scope.cookieArray = $cookies.getObject('data');
		console.log($scope.cookieArray);
	}else{
		console.log("no cookies");
	}
	
	}
	
	$scope.setIt = function(index,set) {
		
		$scope.selectedMDXFunction.argument[0].default_value=set.value;
		$scope.openArgumentsdialog();
		
		}
	
	
	/*function to call pop-up for editing saved members/sets */
	$scope.edit = function(item){
		console.log("editing...");
		
		for (var i = 0; i < $scope.cookieArray.length; i++) {
			if(item.name === $scope.cookieArray[i].name){
				console.log("same one in editing")
				$scope.selectedMDXFunction = item.formula;
				$scope.selectedMDXFunctionName = item.name;
				console.log($scope.selectedMDXFunction);
			}
		}

		$scope.openArgumentsdialog();
	}
	
	$scope.deleteFromCookie = function(index,item) {
		
		$scope.cookieArray = $cookies.getObject('data');
		$scope.cookieArray.splice(index,1);
		$cookies.putObject('data',$scope.cookieArray);
		$scope.cookieArray = $cookies.getObject('data');
		if (item.name != null) {
			$scope.deleteCC(item.name);
			$scope.selectedMDXFunction = {};
			sbiModule_messaging.showSuccessMessage("Item is deleted", 'Success');
		} else {
			console.log("cant delete name is null");
		}
		
		
		}

$scope.deleteCC = function(calculateMemberName) {
	sbiModule_restServices.promiseDelete
	("1.0",'/calculatedmembers/'+calculateMemberName+'?SBI_EXECUTION_ID=' + JSsbiExecutionID)
	.then(function(response) {
		
		$scope.handleResponse(response);
	    }, function(response) {
		sbiModule_messaging.showErrorMessage("Error deleting Calculated Field", 'Error');
		
			});
	};
	
	$scope.hideSwitchIcon = function(position,axis){
		var max = axis == 1 ? $scope.maxRows : $scope.maxCols;
		var last = axis == 1 ?  $scope.maxRows + $scope.leftStart -1 : $scope.maxCols + $scope.topStart -1
		var length = axis == 1 ? $scope.rows.length : $scope.columns.length;

		if(position == length-1)
			return true;
		if(length > max && last == position)
			return true;

		return false;
		
	}
	
};

