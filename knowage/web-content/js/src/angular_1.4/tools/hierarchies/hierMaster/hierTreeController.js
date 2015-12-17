var app = angular.module('hierManager');

app.controller('hierTreeController', ["sbiModule_translate","$scope","$mdDialog","$mdToast","sbiModule_restServices","glDimension", hierarchyTreeFunction ]);

function hierarchyTreeFunction(sbiModule_translate, $scope, $mdDialog, $mdToast, sbiModule_restServices, glDimension){
	
 sbiModule_translate.addMessageFile("messages");
 $scope.translate = sbiModule_translate;
 
 $scope.hierTypes={"root":[{"idx":1,"type":"AUTO"},{"idx":2,"type":"TECHNICAL"}]};
 
 $scope.structureNode = {};
 
 $scope.getHierarchies = function(hierType) {
//	 showPreloader('preloaderHier');
//		SpagoBI/restful-services/hierarchies/getCustomHierarchies?SBI_EXECUTION_ID=-1&dimension=ACCOUNTS&_dc=1449743008200&page=1&start=0&limit=25
	  if (hierType == 'TECHNICAL'){
			sbiModule_restServices
					.get(
							"hierarchies",
							"getCustomHierarchies",
							"dimension="+glDimension.DIMENSION_NM)
					.success(
							function(data, status, headers, config) {
								$scope.hierarchies = data;
					});
	  }
	  if (hierType == 'AUTO'){
			sbiModule_restServices
					.get(
							"hierarchies",
							"hierarchiesOfDimension",
							"dimension="+glDimension.DIMENSION_NM)
					.success(
							function(data, status, headers, config) {
								$scope.hierarchies = data;
					});
	  }
//	  hidePreloader('preloaderHier');
 }
 
 $scope.initTree = function(scope){
	 scope.collapsed = true;
 }
 
 $scope.toggleNode = function(scope) {
	 scope.toggle();
  };
  
  $scope.addNode = function(item){

	  $scope.metadataNode = {"fields":[{"id":"id","name":"Codice","type":"String", "required":"true","visible":"true"},	//required
	     							  {"id":"name","name":"Name","type":"String", "required":"true","visible":"true"},		//required
	    							  {"id":"leafParentCode","name":"Leaf parent code","type":"String", "required":"true","visible":"false"},
	    							  {"id":"originalLeafParentCode","name":"Original Leaf Parent Code","type":"String", "required":"false","visible":"false"},
	    							  {"id":"leafParentName","name":"Parent Name","type":"String", "required":"false","visible":"false"}
	    							]};
	  $mdDialog.show({
      	locals:{parent: item, 
      			fields: $scope.metadataNode.fields
      		  },
      	 	 controllerAs : 'infCtrl',
	         preserveScope : true,
	         clickOutsideToClose : false,
	         template:
	           '<md-dialog style="overflow-y: visible;"  class="infoBox" aria-label="Nuovo nodo">' +
	           '  <md-dialog-content class="md-padding">'+
	           '     <md-toolbar class="minihead">'+
	           '  	   <div class="md-toolbar-tools">'+
	           '		   <h4 class="md-flex" >Nuovo Nodo</h4>'+
	           ' 	  </div>'+
	           '   </md-toolbar>'+
	           ' 	<div layout="row" layout-wrap ng-repeat="f in infCtrl.fields">'+
	           ' 		<div flex="100">'	+
	           ' 			<md-input-container > <!-- Use floating label instead of placeholder -->'+
	           ' 				<label ng-if="f.visible==\'true\'">{{f.name}}</label> '+
	           ' 				<input ng-required="f.required" ng-if="f.visible==\'true\' && f.type==\'String\'" '+
	           '				       ng-model="infCtrl.fields[$index].value" type="text" maxlength="100" > </md-input-container>'+
	           ' 				<input ng-required="f.required" ng-if="f.visible==\'true\' && f.type==\'Number\'" '+
	           '				       ng-model="infCtrl.fields[$index].value" type="number" maxlength="100" > </md-input-container>'+
	           '				<md-datepicker ng-required="f.required" ng-if="f.visible==\'true\' && f.type==\'Date\'" '+
	           '					   ng-model="infCtrl.fields[$index].value" md-placeholder="" ></md-datepicker>'+
	           ' 		</div>'+
	           ' 	</div>'+
	           '  </md-dialog-content>' +
	           '  <div class="md-actions" layout="row">'+    	          
	           '  	<md-button ng-click="infCtrl.insertNode()" class="md-raised">'+
	           '		Conferma ' +	
	           ' 	</md-button>'+	
	           '  	<md-button ng-click="infCtrl.closeDialog()" class="md-raised">'+
	           '		Chiudi ' +	
	           ' 	</md-button>'+	
	           '  </div>'+
	           '</md-dialog>',

	         controller: function(locals, $mdDialog, $mdToast) {
	        	 this.parent = locals.parent;
	        	 this.fields = locals.fields;
	        	 
	        	 this.closeDialog = function() {	        		 
			          $mdDialog.hide();
			      }
	        	 
	        	 this.insertNode = function(){
					  //chiamare servizio REST per salvare il nuovo nodo!!!
					  
					  //aggiornamento struttura json al volo 
//	        		 {"name":"C - Current Assets","id":"G1.3","leafId":"","leafParentCode":"","originalLeafParentCode":"","leafParentName":"",
//                 		 "children":[{"name":"Cash and cash eqivalents","id":"G1.3.5","leafId":"","leafParentCode":"","originalLeafParentCode":"","leafParentName":""}
	        		//clone a brother && substitutes new values inserted
	        		 var newLogicalNode = {};
	        		 if (this.parent.children){
	        			 newLogicalNode = angular.copy(this.parent.children[0]) ;	     
	        		 }else{	        			 
	        			 this.parent.children = []; //prepare for the first child
	        			 newLogicalNode = $scope.structureNode;  			 
	        		 }
	        		 
	        		 for (el in newLogicalNode){
	        			 newLogicalNode[el] = this.getPropertyValue(el);
	        		 }
	        		 
	        		 //add new node to the root
//	        		  $scope.data.root.push(newLogicalNode);
	        		 //add new  node to the parent
	        		 this.parent.children.push(newLogicalNode);
	        		  $mdDialog.hide();
	        	  }
	        	 
	        	 this.getPropertyValue = function(prop){
	        		 for (f in this.fields){
	        			 if (this.fields[f].id == prop){
	        				 if (this.fields[f].value) return this.fields[f].value || '';
	        			 }
	        		 }
	        		 return '';
	        	 }
			  }//controller
	      })
  }

  $scope.modifyNode = function(item){
	  alert(item.name);
  }
  
  $scope.deleteNode = function(item){
	  //update json structure without the element deleted
//	  delete $scope.data.root[item.id];
	  
	  
//	  var c, found=false;
//	    for(c in $scope.data.root) {
//	        if($scope.data.root[c]['id'] == item.id) {
//	            found=true;
//	            break;
//	        }
//	    }
//	    if(found){
//	        delete $scope.data.root[c];
//	    }
  }

  $scope.copyLeaf = function(item){
	  alert(item);
  }
  
  $scope.loadTree = function(hierName){
	  $scope.data={"root":[
	                     	{"name":"C - Current Assets","id":"G1.3","leafId":"","leafParentCode":"","originalLeafParentCode":"","leafParentName":"",
	                 		 "children":[{"name":"Cash and cash eqivalents","id":"G1.3.5","leafId":"","leafParentCode":"","originalLeafParentCode":"","leafParentName":""}]},
	                 		{"name":"A - Non-current Assets","id":"G1.1","leafId":"","leafParentCode":"","originalLeafParentCode":"","leafParentName":"",
	                 		"children":[{"name":"Intangible assets","id":"G1.1.2","leafId":"","leafParentCode":"","originalLeafParentCode":"","leafParentName":""}]},
	                 		{"name":"B - Non-current Assets held for sale","id":"G1.2","leafId":"z","leafParentCode":"","originalLeafParentCode":"","leafParentName":""}
	                     	],
	                     	"hierName":hierName};
	  //update structureNode with the first element
	  for(d in $scope.data.root[0]){
		  if (typeof $scope.data.root[0][d] !== 'object'){
			  $scope.structureNode[d] = "";
		  }
	  }
	  delete $scope.structureNode.children; //clean from children because the structure is the same!
  }
  

  /*
 $scope.toggleNode = function(scope, item, gloss){
 	alert(selectedNode  + " - " + id);
 	if($scope.prevSWSG!="" && $scope.prevSWSG!=undefined ){
		scope.toggle();
		return;	
	}
 
	item.preloader = true;
	if (scope.collapsed) {
		 $scope.getGlossaryNode(gloss, item, scope)
	} else {
		scope.toggle();
		item.preloader = false;
	}
 };
 */
  
    $scope.saveHierMaster = function(){
    	alert('save hierarchy' + $scope.data.root);
    }
  

	$scope.showPreloader = function(pre) {
		alert("show");
		$scope[pre] = true;

	}
	$scope.hidePreloader = function(pre) {
		alert("hide");
		$scope[pre] = false;
	}

};
