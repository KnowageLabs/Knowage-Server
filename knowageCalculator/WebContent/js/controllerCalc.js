var app = angular.module('calcManager', ['ngMaterial','ngMessages']);


app.controller('calculatorRuntimeCtrl', ["$scope","$log","$mdDialog","$http",calcRuntimeManagerFunction]);

function calcRuntimeManagerFunction($scope,$log,$mdDialog,$http)
{ 
	
//-------------------------Utility variables definition--------------------------	
	var self=this;
	self.items=["BD","SI","ER","LI","PM","PA","OD","EI"];
	self.selected=[];

    self.cores=['4','8','12','16','20','24'];
    self.selectedNumCores="";
    self.showResults=false;
    

//-------------------------Utility functions definition--------------------------	
	
	
	self.isUndefined = function(thing)
	{
	    return (typeof thing === "undefined" || thing.length===0);
	}
	
    self.toggle = function (item, list) {
        var idx = list.indexOf(item);
        if (idx > -1) list.splice(idx, 1);
        else list.push(item);
    };
    
    self.exists = function (item, list) {
        return list.indexOf(item) > -1;
    };
    
//---------------------------Block Calculate Button logic------------------------

    self.getCost=function()
    {
    	self.body={};
    	self.body.selectedNumCores=self.selectedNumCores;
    	self.body.selected=self.selected;
    	self.body.modality="ISV"//"SUBSCRIPTION"; //Temporary fake
    	
		$log.info("Sending ",self.body);
    	
		
		if(self.body.selected.length<=0)
		{	
		    $mdDialog.show(
		    	      $mdDialog.alert()
		    	        .parent(angular.element(document.querySelector('#popupContainer')))
		    	        .clickOutsideToClose(true)
		    	        .title('Select at least one product!')
		    	        .ariaLabel('Alert Dialog')
		    	        .ok('OK')
		    	       )
		}
		else
		{
			
	    	/*
	        var successHandler=function(data)
	        {
	        	$log.info("Successful POST");
	        }
	        var errorHandler=function(data)
	        {
	        	$log.info("Failed POST");
	        }
	    	
	    	$http.post('rest/costInformation/calculateCost')
	    		 .then(successHandler,errorHandler)
	    	*/
	        
	    	//$http.put('rest/costInformation/calculateCost', self.body); //Funzionante con errore sulla risposta
	    	
		    var requestParams = {
		            method: 'POST',
		            url: 'http://localhost:8080/knowageCalculator/rest/costInformation/calculateCost',
		            headers: { 'Content-Type': 'application/json' },
		            data:	self.body,
		          };
	
		    $http(requestParams).then(function(evt)
		    	{
					$log.info("REST COMUNICATION OK, RECEIVED: ",evt);
			    	self.goldCost=evt.data.goldPrice.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
			    	self.silverCost=evt.data.silverPrice.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');

					self.showResults=true;				
		    	});
		
		}
    }
    
    
}



	



