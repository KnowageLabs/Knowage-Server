/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
angular.module('member_directive',['sbiModule'])
	.directive('member', function (sbiModule_restServices,sbiModule_messaging,$mdDialog,sbiModule_config) {
	    return {
	        restrict: 'A',
	        
	        link: function (scope, $element, attrs) {
	           
	        	
	        	
	            for(var i =0;i< scope.members.length;i++){
	            	if(scope.members[i].position===attrs.position&&scope.members[i].uniqueName===attrs.uniquename){
	            		
	            		mark();
	            		break;
	            	}
	            	
	            }
	            
	            $element.bind('$destroy', function () {

		        	 $element.detach();
 
		            });
	            
	            var selectMember = function(){
	            	
	            	if(isSameHierarchy()){
	            		copyAttributesToSelectedMember();
			            addMember(scope.selectedMember);
			            mark();
	            	}
	            	
	            }
	            
	            var deselectMember = function(){
	            	removeMember();
	            	clearMark();
	            }
	            
	            var toggleMemberSelection = function(){
	            	if(!isMemberSelected()){
	            		selectMember();
	            	}else{
	            		deselectMember();
	            	}
	            }
	            
	            
	            
	            var isMeasure = function(){
	            	
	            	return attrs.hierarchyUniqueName==='[Measures]';
	            }
	            
	            var isSameHierarchy = function(){
	            	if(!isMembersEmpty()){
	            		return scope.members[0].hierarchyUniqueName == attrs.hierarchyuniquename;
	            	}
	            		return true;
	            	
	            }
	            var copyAttributesToSelectedMember = function(){
	            	scope.selectedMember.uniqueName = attrs.uniquename;
		            scope.selectedMember.level = attrs.level;
		            scope.selectedMember.dimension = attrs.dimensionuniquename;
		            scope.selectedMember.parentMember = attrs.parentmember;
		            scope.selectedMember.axisOrdinal = attrs.axisordinal;
		            scope.selectedMember.hierarchyUniqueName = attrs.hierarchyuniquename;
		            scope.selectedMember.position = attrs.position;
	            }
	            
	            var addMember = function(selectedMember){
	            	scope.members.push(angular.copy(selectedMember))
	            }
	            
	            var removeMember = function(){
	            	for(var i =0;i< scope.members.length;i++){
		            	if(scope.members[i].position===attrs.position){
		            		
		            		scope.members.splice(i,1);
		            		
		            	}
		            	
		            }
	            }
	            
	            var isMemberSelected = function(){
	            	for(var i =0;i< scope.members.length;i++){
		            	if(scope.members[i].position===attrs.position){
		            		return true;
		            	}	
		            }
	            	return false;
	            }
	            
	            var isMembersEmpty = function(){
	            	return scope.members.length===0;
	            }
	            
	           
	            
	            
	            
	            
	            
	            var onClick = function($event){
	            	
	            
	            	$event.preventDefault()
            		$event.stopPropagation();
	            	
	            	if(scope.selectedAgument){

			            if(scope.selectedAgument.expected_value==='Measure_Expression'){
			            		
				            if(isMeasure()){
				            	toggleMemberSelection();
				            }
				            
			            }else{
			            	toggleMemberSelection();
			            }
			                 
	            }else if(scope.olapMode){
	            	toggleMemberSelection();
	            }    
			         
	        		
	        if(scope.modelConfig.showCompactProperties == true){
	        	
	        	sbiModule_restServices.promiseGet
	    		("1.0",'/member/properties/'+scope.selectedMember.uniqueName+'?SBI_EXECUTION_ID='+JSsbiExecutionID)
	    		.then(function(response) {
	    			console.log(response.data);
	    			scope.propertiesArray = response.data;
	    			$mdDialog
	    			.show({
	    				scope : scope,
	    				preserveScope : true,
	    				parent: angular.element(document.body),
	    				controllerAs : 'olapCtrl',
//	    				templateUrl : '/knowagewhatifengine/html/template/main/toolbar/properties.html',
	    				templateUrl : function() {
							return sbiModule_config.contextName + '/html/template/main/toolbar/properties.html'
						},
	    				clickOutsideToClose : false,
	    				hasBackdrop:false
	    			});
	    			
	    			
	    		}, function(response) {
	    			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.propertiesGet.error'), 'Error');
	    			
	    		});
	        	
	        }
	            
	            }      
	           
	         $element.bind('click', onClick);
	        
	         var mark = function(){
	            	$element[0].className = 'pivot-table-selected';
	            }
	            
	         var clearMark = function(){
	            	$element[0].className = 'pivot-table th';
	            }
	         
	        }
	    };
	});
