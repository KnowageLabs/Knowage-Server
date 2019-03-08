/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

(function(){

angular.module('cockpitModule').directive('mdTabFixedAddSheetButton',function ($compile) {
    return {
        link: function (scope, element, attrs, ctrl, transclude) {
    		var mdTabsWrapper= angular.element(document.querySelector("md-tabs.cockpitSheetTabs"));
            angular.element(mdTabsWrapper).append(element);
        }
    };
})

angular.module('cockpitModule').directive('cockpitSheet',function($compile){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-sheet/templates/cockpitSheet.html',
		   transclude: true,
		   controller: cockpitSheetControllerFunction,
		   priority: 1000,
		   compile: function (tElement, tAttrs, transclude) {
			   tElement[0].classList.add("layout");
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    }
                };
		   	}
	   }
});

angular.module('cockpitModule').filter('orderObjectBy', function() {
	return function(items, field, reverse) {
		var filtered = [];
		angular.forEach(items, function(item) {
			filtered.push(item);
		});
		filtered.sort(function (a, b) {
			return (a[field] > b[field] ? 1 : -1);
		});
		if(reverse) filtered.reverse();
		return filtered;
	};
});

function cockpitSheetControllerFunction($scope,cockpitModule_template,cockpitModule_properties,sbiModule_translate,$mdPanel,$mdDialog,$timeout,$mdMenu){
	$scope.translate = sbiModule_translate;
	$scope.cockpitModule_template=cockpitModule_template;
	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.maxSheetsNumber = 7;

	for(var sh in cockpitModule_template.sheets){
		cockpitModule_template.sheets[sh].index = cockpitModule_template.sheets[sh].index!=undefined?parseInt(cockpitModule_template.sheets[sh].index):sh;
	}

	$scope.addSheet=function(){
		cockpitModule_template.sheets.push({index:cockpitModule_template.sheets.length,label:sbiModule_translate.load("sbi.cockpit.new.sheet"),widgets:[]});
	};

	$scope.cloneSheet = function(sheet) {
		var newSheet = {
			index: cockpitModule_template.sheets.length,
			label: sbiModule_translate.load("sbi.cockpit.new.sheet"),
			widgets: angular.copy(sheet.widgets)
		};

		var nextId = new Date().getTime();
		for(var i in newSheet.widgets){
			newSheet.widgets[i].id = nextId++;
		}

		cockpitModule_template.sheets.push(newSheet);
	}

	$scope.deleteSheet=function(sheet,ev){
		var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.cockpit.sheet.delete.title"))
	        .textContent(sbiModule_translate.load("sbi.cockpit.sheet.delete.messages"))
	        .ariaLabel('delete sheet')
	        .targetEvent(ev)
	        .ok(sbiModule_translate.load("sbi.ds.wizard.confirm"))
	        .cancel(sbiModule_translate.load("sbi.ds.wizard.cancel"));

		$mdDialog.show(confirm).then(function() {
		  for(var sh in cockpitModule_template.sheets){
			  if(cockpitModule_template.sheets[sh].index>sheet.index){
				  cockpitModule_template.sheets[sh].index --;
			  }
		  }
		  cockpitModule_template.sheets.splice(cockpitModule_template.sheets.indexOf(sheet),1);
	  });
	}

	$scope.moveSheet = function(sheet,direction){
		var cur, prev, next;
		for(var sh in cockpitModule_template.sheets){
			if(sheet.index==cockpitModule_template.sheets[sh].index){cur=sh}
			if(sheet.index-cockpitModule_template.sheets[sh].index==1){prev=sh}
			if(sheet.index-cockpitModule_template.sheets[sh].index==-1){next=sh}
		}
		if(direction == 'prev'){
			cockpitModule_template.sheets[cur].index --;
			cockpitModule_template.sheets[prev].index ++;
		}else {
			cockpitModule_template.sheets[cur].index ++;
			cockpitModule_template.sheets[next].index --;
		}
		if($scope.initializedSheets.indexOf(cur) != -1) $scope.initializedSheets.splice($scope.initializedSheets.indexOf(cur),1);
		
	};

	$scope.renameSheet=function(sheet,ev){
		var confirm = $mdDialog.prompt()
	      .title(sbiModule_translate.load("sbi.cockpit.sheet.rename.title"))
	      .placeholder(sbiModule_translate.load("sbi.cockpit.sheet.name"))
	      .ariaLabel('rename')
	      .initialValue(sheet.label)
	      .targetEvent(ev)
	      .ok(sbiModule_translate.load("sbi.ds.wizard.confirm"))
	      .cancel(sbiModule_translate.load("sbi.ds.wizard.cancel"));
	    $mdDialog.show(confirm)
	    .then(function(result) {
	    	 sheet.label=result;
	    }, function() {});
	}
};

})();