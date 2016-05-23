 function exportKpi(format){ 
	 if(format=="PDF"){
		 angular.element(document.body).scope().exportPDF(); 
	 }
}
(function() {
	 
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
	var kpiViewerModule = angular.module('kpiViewerModule');

	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData', 'sbiModule_restServices','sbiModule_translate','sbiModule_config', 'kpiViewerServices','$q','$mdDialog','$timeout','$mdToast', kpiViewerControllerFn]);

	function kpiViewerControllerFn($scope, documentData, sbiModule_restServices,sbiModule_translate, sbiModule_config, kpiViewerServices,$q,$mdDialog,$timeout,$mdToast) {
		$scope.documentData = documentData;
		$scope.kpiOptions = documentData.template.chart.options;
		$scope.kpiItems = [];
		$scope.showPreloader=false;
		$scope.displayScorecard=true;
		$scope.displayKpiWidget=true;
		$scope.GAUGE_DEFAULT_SIZE = 250;
		$scope.LINEAR_GAUGE_DEFAULT_SIZE= 400;
		$scope.gaugeMinValue = 0;
		$scope.gaugeMaxValue = 150;
		$scope.gaugeValue = 0;
		$scope.gaugeTargetValue = 0;
		$scope.thresholdStops = documentData.kpiValue.threshold;
		$scope.percentage=0;
		$scope.translate = sbiModule_translate;
		$scope.loadKpiValues = [];
		$scope.scorecardExpanderStatus={};
	
		$scope.loadKpiValue = function(){
			if($scope.documentData.template.chart.data.kpi != undefined){

					var array =JSON.parse($scope.loadKpiValues);
					
					for(var j = 0; j < $scope.kpiItems.length; j++){
						var kpiItem = $scope.kpiItems[j];
							
						for(var i = 0; i < array.length; i++){
							var kpiArray = JSON.parse(array[i])
							
							if(kpiArray.length > 0 && kpiArray[kpiArray.length-1].kpiId == kpiItem.id ){
								if(kpiArray[kpiArray.length-1].manualValue!=undefined)
									kpiItem.value = kpiArray[kpiArray.length-1].manualValue;
								else
									kpiItem.value = kpiArray[kpiArray.length-1].computedValue;
						
								for(var k = 0; k < kpiArray.length; k++) {
									kpiItem.valueSeries.push(kpiArray[k]);
								}
							}										
						}
					}

			
			}
		};
		

		$scope.init = function(){
			sbiModule_restServices.promisePost("1.0/jsonKpiTemplate", "readKpiTemplate", $scope.documentData.template)
			.then(function(response){ 
				var chart = $scope.documentData.template.chart;

				$scope.gaugeValue = null;
				$scope.gaugeTargetValue = null;
				$scope.loadKpiValues = response.data.loadKpiValue;
				if(chart.type == "kpi") {
					if(response.data.info!=undefined){
					if(Array.isArray(JSON.parse(response.data.info))) {
						var templateKpi = $scope.documentData.template.chart.data.kpi;
						if(!Array.isArray(templateKpi)) {
							var array = [templateKpi];
							templateKpi = array;
						}

						var templateOptions = $scope.documentData.template.chart.options;
						var templateStyle = $scope.documentData.template.chart.style;

						for(var i = 0; i < JSON.parse(response.data.info).length; i++) {
							var responseItem = JSON.parse(response.data.info)[i];

							var responseItemKpi = responseItem.kpi;

							$scope.documentData.kpiValue.push(responseItemKpi);

							for(var j = 0; j < templateKpi.length; j++) {
								var templateKpiItem = templateKpi[j];
								responseItemKpi.targetValue = responseItem.target;

								if(templateKpiItem.id == responseItemKpi.id) {
									var conf = kpiViewerServices.createWidgetConfiguration(
											templateKpiItem, responseItemKpi, chart);

									$scope.kpiItems.push(conf);

									break;
								}
							}
						}
					}
					}

					
					$scope.loadKpiValue();
				} else { //scorecard
					$scope.documentData.scorecard = JSON.parse(response.data.info)[0].scorecard;
				}
			});
		};
		
		$scope.openEdit = function(kpiItem){
			
			if(kpiItem.value!=undefined){
				var deferred = $q.defer();
				
				$mdDialog.show({
					controller: dialogController,
					templateUrl: currentScriptPath + '../kpi-widget/template/kpi-widget-editValue.jsp',
					clickOutsideToClose:true,
					preserveScope:true,
					locals: {
						items: deferred,
						label:kpiItem.name,
						value:kpiItem.value,
						targetValue:kpiItem.targetValue,
						valueSeries:kpiItem.valueSeries[kpiItem.valueSeries.length-1]
					}
				})
				.then(function(answer) {
					
					return deferred.resolve($scope.selectedFunctionalities);
				}, function() {
					//$scope.loadKpiValue();
					if(deferred.promise.$$state.value!=undefined){
						kpiItem.value = deferred.promise.$$state.value.value;
					}
					if(deferred.promise.$$state.comment!=undefined){
						kpiItem.valueSeries[$scope.valueSeries.length-1].manualNote = deferred.promise.$$state.value.comment;
					}
					$scope.status = 'You cancelled the dialog.';
				});
				
				return deferred.promise;
			
			}else{
				$scope.showAction($scope.translate.load('sbi.kpi.widget.missingvalue'));
			}
		}
		
		  
		//export pdf
		
		function getPage() {
	        var div = document.createElement('div')
	        div.offsetWidth = 2048;
	        div.classList.add("layout-padding"); 
	        div.style.backgroundColor = 'white';
	        return div;
	    }
		
		$scope.createExportToolbar=function(text){
			 var testata = document.createElement('div');  
		     testata.classList.add("layout-row");
		     testata.classList.add("layout-padding");
		     var testataP = document.createElement('span');
		     testataP.textContent =text;
		     testataP.classList.add("flex");
		     testataP.classList.add("layout-align-center-center");
		     testataP.classList.add("layout-row");
		     testataP.style.fontSize = "20";
		     testata.appendChild(testataP); 
		     var testataIMG = document.createElement('img');
		     testataIMG.src=sbiModule_config.contextLogo;
		     testataIMG.style.padding="10"
		     testata.appendChild(testataIMG);
		     return testata;
		}
		
		$scope.createTmpContainer=function(pageArray){
			 var printTmpContainer = document.createElement('div')
		     printTmpContainer.id="printTmpContainer";
		     printTmpContainer.style.position="absolute";
		     printTmpContainer.style.width="1024";
		     printTmpContainer.classList.add("layout-fill"); 
		     document.body.appendChild(printTmpContainer);
			 for(var i=0;i<pageArray.length;i++){
		    	 printTmpContainer.appendChild(pageArray[i]);
		     }
			 
			 return printTmpContainer;
		}
		
		$scope.exportPDF=function(){
			$scope.showPreloader=true;
			if($scope.documentData.template.chart.type=="scorecard"){
				$scope.exportScorecardPDF();
			}else if($scope.documentData.template.chart.type=="kpi"){
				 if($scope.documentData.template.chart.model=='widget'){
					$scope.exportKpiWidgetPDF();
				}else{
					$scope.exportKpiListPDF();
					
				}
			} else{
				$scope.showPreloader=false;
				alert("NO INFO")
			}
		}
		
		
		$scope.exportKpiListPDF=function(){
			$scope.showPreloader=false;
		};
		
		$scope.exportKpiWidgetPDF=function(){
			 var pdf = new jsPDF('p', 'pt', 'a4');
			
			 var heigth = 0;
			 var tmpMaxHeigth = 0;
			 var tmpMaxHeigthPREV = 0;
			 var width = 0;
		     var pageHeigth = 900; //pt
		     var pageWidth = 1024; //px
		     var pageArray = [];
		     var page = 0;
		     var fpage=getPage();
		     fpage.classList.add("layout-column"); 
		     var testata=$scope.createExportToolbar($scope.documentData.docLabel);
		     heigth+= 70 * 0.75;
		     fpage.appendChild(testata);
		     var fContentePage=getPage();
		     fContentePage.classList.add("layout-row"); 
		     fContentePage.classList.add("layout-wrap"); 
		     fpage.appendChild(fContentePage);
		     pageArray.push(fpage);
		     var kpiList = document.querySelectorAll("#kpiWidgetTemplate md-whiteframe");
		     for (var i = 0; i < kpiList.length; i++) {
		    	 var offH= kpiList[i].offsetHeight * 0.75;
		    	 var offW= kpiList[i].offsetWidth;
		    	 var nextRow=false;
		    	 if(offH>tmpMaxHeigth){
		    		 tmpMaxHeigthPREV=tmpMaxHeigth;
		    		  tmpMaxHeigth=offH;
		    	 }
		    	 
		    	 if(width + offW >pageWidth) {
		    		 width=0;
		            if (heigth +tmpMaxHeigth > pageHeigth) {
		                page++;
		                heigth = 0; 
		                var tmppage=getPage();
		                tmppage.classList.add("layout-row"); 
		                tmppage.classList.add("layout-wrap"); 
		                pageArray.push(tmppage);
		            }else{
		            	heigth += tmpMaxHeigthPREV; 
		            	tmpMaxHeigth=0;
		            	tmpMaxHeigthPREV=0;
		            	nextRow=true;
		            }
		    	 }
		    	 
		    	 if(offH>tmpMaxHeigth && !nextRow){
		    		 heigth += offH-tmpMaxHeigth; 
		    	 }
		    	 
		    	 
		    	 
		            var tmpNode = kpiList[i].cloneNode(true); 
		            //the page 0 have a toolbar
		            if(page==0){
		            	pageArray[page].children[1].appendChild(tmpNode);
		            }else{
		            	pageArray[page].appendChild(tmpNode);
		            }
		            width += offW; 
		        }
		     var printTmpContainer= $scope.createTmpContainer(pageArray);
		     generatePdf(pageArray, 0, pdf,printTmpContainer).then(
		    		 function(){

		    			 $scope.displayKpiWidget=false;
		                    if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
		                        $scope.$apply();
		                    }
						    $timeout(function(){
								$scope.displayKpiWidget=true; 
								$scope.showPreloader=false;
							},0); 
		    		 
		    		 },
		    		 function(){
		    			 alert("ERROR");
		    		 }
		    		 );
		     
		};
		
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
		$scope.exportScorecardPDF=function(){ 
			
			var oldExpanderStatus={};
			angular.copy($scope.scorecardExpanderStatus,oldExpanderStatus);
			for(var key in $scope.scorecardExpanderStatus){
				$scope.scorecardExpanderStatus[key]=true;
			}
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
			    $scope.$apply();
			}
			
			$timeout(function(){  
				 var pdf = new jsPDF('p', 'pt', 'a4');
				 var heigth = 0;
			     var pageHeigth = 1000;
			     var pageArray = [];
			     var page = 0;
			     pageArray.push(getPage());
			     var testata=$scope.createExportToolbar($scope.documentData.docLabel);
			     heigth+= testata.offsetHeight * 0.75;
			     pageArray[page].appendChild(testata);
			     
			     var perspectiveList = document.querySelectorAll("kpi-scorecard>md-content>div>expander-box");
				 
			     for (var i = 0; i < perspectiveList.length; i++) {
			    	 var offH= perspectiveList[i].offsetHeight * 0.75;
			            if (heigth +offH > pageHeigth) {
			                page++;
			                heigth = 0;
			                pageArray.push(getPage());
			            }
			            var tmpNode = perspectiveList[i].cloneNode(true); 
			            pageArray[page].appendChild(tmpNode);
			            heigth += offH; 
			        }
			     var printTmpContainer= $scope.createTmpContainer(pageArray);
			     generatePdf(pageArray, 0, pdf,printTmpContainer).then(
			    		 function(){
			    			 $scope.displayScorecard=false;
			                    if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
			                        $scope.$apply();
			                    }
							    $timeout(function(){
									$scope.displayScorecard=true;
									angular.copy(oldExpanderStatus,$scope.scorecardExpanderStatus);
									$scope.showPreloader=false;
								},0); 
			    		 },
			    		 function(){
			    			 alert("ERROR");
			    		 }
			    		 );
				 
			},0);
		}
		 
		function generatePdf(pageArray, index, pdf,tmpContainer,deferred) {
			if(deferred==undefined){
				deferred=$q.defer();
			}
			 pdf.addHTML(pageArray[index], function () {
	            if (index == pageArray.length - 1) { 
	                    pdf.save($scope.documentData.docLabel+'.pdf');
	                    deferred.resolve();
	                    document.body.removeChild(tmpContainer) 
	            } else {
	                pdf.addPage();
	                generatePdf(pageArray, index + 1, pdf,tmpContainer,deferred);
	            }
	        });
			
			return deferred.promise;
	    }
		
	};
	
	

	
	function dialogController($scope,$mdDialog,sbiModule_restServices,$mdToast,sbiModule_config,sbiModule_translate,items,label,value,targetValue,valueSeries){
		$scope.label = label;
		$scope.value = value;
		$scope.targetValue =targetValue;
		$scope.valueSeries = valueSeries;
		$scope.oldValue=valueSeries.computedValue;
		$scope.array = [];
		$scope.translate = sbiModule_translate;
		
		$scope.parseLogicalKey = function(){
			var string  = $scope.valueSeries.logicalKey;
			var char = string.split(",");
			$scope.array = [];
			for(var i=0;i<char.length;i++){
				var values = char[i].split("=")
				var obj = {};
				obj["label"] = values[0];
				obj["value"] = values[1];
				$scope.array.push(obj);
			}
		}
		$scope.parseLogicalKey();
		$scope.close = function(){
			$mdDialog.cancel();

		}

		
		$scope.apply = function(){
			if($scope.valueSeries.manualNote==null || $scope.valueSeries.manualNote.trim()==""){
				$scope.showAction($scope.translate.load("sbi.kpi.widget.missingcomment"));
			}else{
				if($scope.value==undefined){
					$scope.value = null;
				}
				$mdDialog.cancel();
				$scope.kpiValueToSave = {};
				$scope.kpiValueToSave["manualValue"] = $scope.value;
				$scope.kpiValueToSave["manualNote"] = $scope.valueSeries.manualNote;
				$scope.kpiValueToSave["valueSeries"] = $scope.valueSeries;
				sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
				sbiModule_restServices.promisePost("1.0/kpi", 'editKpiValue',$scope.kpiValueToSave)
	
				.then(function(response){ 
					var obj = {};
					if($scope.value==null){
						$scope.value =$scope.valueSeries.computedValue;
					}
					obj["value"] = $scope.value;
					obj["comment"] = $scope.kpiValueToSave["manualNote"];
					items.resolve(obj);
				},function(response){
					$scope.errorHandler(response.data,"");
				});
			}
		};

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

	};
})();