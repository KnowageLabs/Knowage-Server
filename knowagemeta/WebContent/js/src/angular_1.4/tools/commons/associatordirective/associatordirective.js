var associatorDirective = angular.module('associator-directive', ['ngMaterial']);
associatorDirective.directive("associatorDirective",function(){
		return {
//			restrict: 'E',
			templateUrl:'/knowagemeta/js/src/angular_1.4/tools/commons/associatordirective/template/associatorDirectiveTemplate.jsp',
			controller: associatorDirectiveController,
			scope: {
				sourceColumnLabel:"=",
				targetColumnLabel:"=",
				sourceModel:"=",
				targetModel:"=",
				sourceName:"@",
				targetName:"@",
				associatedItem:"@?",
				dragOptions:"=?",
				beforeDeleteAssociation:"&?",
				afterDeleteAssociation:"&?",
				multivalue:"=?"
			},
			 link: function (scope, element, attrs, ctrl, transclude) {

				 if(!attrs.associatedItem){
					 scope.associatedItem="links";
					 scope.$watch('targetModel', function() {
						 if (scope.targetModel != undefined || scope.targetModel != null)
							 scope.targetModel.forEach(function(entry) {
								 entry.links = [];
							 });
					 });
				 }
             }
		};
	});

function associatorDirectiveController($scope){
	$scope.deleteRelationship=function(item){
		var execute=true;
		if($scope.beforeDeleteAssociation!=undefined){
			execute= $scope.beforeDeleteAssociation({item:item})
		}
		if(execute!=false){
			item[$scope.associatedItem]=[];
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
	    	    $scope.$apply();
	    	}
		}

		if($scope.afterDeleteAssociation!=undefined){
			execute= $scope.afterDeleteAssociation({item:item})
		}
	}
}
associatorDirective.directive('draggable', function() {
    return {
        scope: {
        	item: '='
        },
        link: function(scope, element) {
	        // this gives us the native JS object
	        var el = element[0];

	        el.draggable = true;

	        el.addEventListener(
	            'dragstart',
	            function(e) {
	                e.dataTransfer.effectAllowed = 'move';
	                e.dataTransfer.setData('itemIndex', this.id.split("-")[1]);
	                this.classList.add('drag');
	                return false;
	            },
	            false
	        );

	        el.addEventListener(
	            'dragend',
	            function(e) {
	                this.classList.remove('drag');
	                return false;
	            },
	            false
	        );
	    }
    }
});

associatorDirective.directive('droppable', function($timeout) {
    return {
        scope: {
        	drop: '&', // parent
        	item: '='
        },
        link: function(scope, element) {
            // again we need the native object
            var el = element[0];

            el.addEventListener(
            	    'dragover',
            	    function(ev) {
            	    	var data = ev.dataTransfer.getData("itemIndex");

            	    	var accept=true;
        			    if(scope.$parent.dragOptions && scope.$parent.dragOptions.hasOwnProperty("accept")){
        			    	accept=scope.$parent.dragOptions.accept(ev,scope.item) ;
                	    }

        			    if (scope.$parent.multivalue!=true &&  scope.item[scope.$parent.associatedItem]!=undefined && scope.item[scope.$parent.associatedItem].length > 0){
        			    	accept=false;
        			    }

        			    if(accept!=false){
        			    	ev.dataTransfer.dropEffect = 'move';
        			    	// allows us to drop
        			    	if (ev.preventDefault) ev.preventDefault();
        			    	this.classList.add('over');

        			    }

            	        return false;
            	    },
            	    false
            	);

            el.addEventListener(
            	    'dragenter',
            	    function(e) {
            	        this.classList.add('over');
            	        return false;
            	    },
            	    false
            	);

        	el.addEventListener(
        	    'dragleave',
        	    function(e) {
        	        this.classList.remove('over');
        	        return false;
        	    },
        	    false
        	);

        	el.addEventListener(
        		    'drop',
        		    function(ev) {
        		        // Stops some browsers from redirecting.
        		        if (ev.stopPropagation) ev.stopPropagation();

        		        this.classList.remove('over');
        		        var data = ev.dataTransfer.getData("itemIndex");

        			    var executeDrop=true;
        			    if(scope.$parent.dragOptions && scope.$parent.dragOptions.hasOwnProperty("beforeDrop")){
        			    	executeDrop=scope.$parent.dragOptions.beforeDrop(ev,scope.$parent.sourceModel[data],scope.item) ;
                	    }
        			    if(executeDrop!=false){

        			    	if(scope.item[scope.$parent.associatedItem]==undefined){
        			    		scope.item[scope.$parent.associatedItem]=[];
        			    	}
        			    	if(scope.$parent.multivalue==true && scope.item[scope.$parent.associatedItem].indexOf(scope.$parent.sourceModel[data])!=-1){
        			    		var classList=this.classList;
        			    		 classList.add('errorClass');
        			    		 $timeout(function(){
        			    			 classList.remove('errorClass');
        			    		 },500)

        			    		return
        			    	}
        			    	scope.item[scope.$parent.associatedItem].push(scope.$parent.sourceModel[data]);

        			    	if (scope.$root.$$phase != '$apply' && scope.$root.$$phase != '$digest') {
        			    	    scope.$apply();
        			    	}

        			    	if(scope.$parent.dragOptions && scope.$parent.dragOptions.hasOwnProperty("dropEnd")){
        			    		scope.$parent.dragOptions.dropEnd(ev,scope.$parent.sourceModel[data],scope.item);
        			    	}
        			    }

        		        return false;
        		    },
        		    false
        		);
        }
    }
});