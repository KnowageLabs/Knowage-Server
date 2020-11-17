/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v1.0.2
 * 
 */ 

angular.module('angular_table', ['ngMaterial', 'angularUtils.directives.dirPagination','sbiModule'])
        .directive('angularTable',
                function ($compile,sbiModule_config) {
                    return {
                        templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/commons/angular-table/template/angular-table.html',
                        transclude: true,
                        controller: TableControllerFunction,
                        priority: 1000,
                        scope: {
                            ngModel: '=',
                            id: "@",
                            columns: "=?", //items to display. if not defined list all element ordering by name
                            columnsSearch: "=?", // columns where search
                            showSearchBar: '=', //default false
                            searchFunction: '&',
                            pageChangedFunction: "&",
                            totalItemCount: "=?", //if not present, create a non sync pagination and page change function is not necessary
                            currentPageNumber: "=?",
                            noPagination: "=?", //not create pagination and  totalItemCount and pageChangedFunction are not necessary
                            clickFunction: "&", //function to call when click into element list
                            menuOption: "=?", //menu to open with right click
                            speedMenuOption: "=?", //speed menu to open with  button at the end of item
                            selectedItem: "=?", //optional to get the selected  item value
                            highlightsSelectedItem: "=?",
                            multiSelect: "=?",
                            scopeFunctions: "=?",
                            dragEnabled: "=?",
                            dragDropOptions: "=?",
                            enableClone: "=?",
                            showEmptyPlaceholder: "=?",
                            noDropEnabled: "=?",
                            allowEdit: "=?",
                            editFunction: "&?",
                            allowEditFunction: "&?",
                            hideTableHead: "=?",
                            fullWidth:"=?",
                            comparisonColumn:"=?",
                            initialSorting:"=?",
                            initialSortingAsc:"=?",
                            visibleRowFunction:"&",
                            sortableColumn:"=?",
                            changeDetector:"@?",
                            fixedItemPerPage:"=?",
                            itemsPerPage:"=?",
                            showExpanderRowCondition:"&?",
                            disableAutoLoadOnInit:"@?",
                            relativeHeader:"=?" 
                        },
                        compile: function (tElement, tAttrs, transclude) {
                        	
                            return {
                                pre: function preLink(scope, element, attrs, ctrl, transclud) {
                                	scope.tableItem=element;
                                	
                                    if (attrs.dragEnabled && (attrs.dragEnabled == true || attrs.dragEnabled == "true")) {
                                        var table = angular.element(element[0].querySelector("table.principalTable"))
                                        table.attr('ui-tree', "dragDropOptions");
                                        table.attr('drag-delay', "600");
                                        table.attr('data-clone-enabled', "enableClone");
                                        table.attr('data-empty-placeholder-enabled', "showEmptyPlaceholder");
                                        table.attr('data-nodrop-enabled', "noDropEnabled");


                                        $compile(table)(scope)
                                        var body = angular.element(table[0].querySelector("tbody"));
                                        body.attr('ui-tree-nodes', "");
                                        body.attr('ng-model', "ngModel");
                                        
                                        scope.DandDEnabled=true;
                                    }else{
                                    	scope.DandDEnabled=false;
                                    }
                                   
                                },
                                post: function postLink(scope, element, attrs, ctrl, transclud) {

                                    //TODO ngModel obbligatorio
                                	scope.internalTableConfiguration={};

                                    var id = "";
                                    if (attrs.id) {
                                        id = attrs.id;
                                    }else{
                                    	id = "at"+(new Date()).getTime();
                                    	scope.id=id;
                                    }


                                    scope.tableItem.addClass(id + "ItemBox");
                                    scope.tableItem.addClass("layout-column");


                                    var table = angular.element(scope.tableItem[0].querySelector("table.principalTable"));
                                    var thead = angular.element(table[0].querySelector("thead"));
                                    var tbody = angular.element(table[0].querySelector("tbody"));
                                    var footerBox = angular.element(scope.tableItem[0].querySelector("angular-table-footer"));

                                    //create the head
                                    thead.attr('angular-table-head', "");
                                    //create tbody
                                    tbody.attr('angular-table-body', "");
                                    
                                    if(scope.DandDEnabled){
                                    	tbody.attr('no-drag-and-drop', true);
                                    }

                                    scope.initializeColumns = function (noCompile) {
                                        //create the column of the table. If attrs.column is present load only this column, else load all the columns
                                        scope.tableColumns = [];
 
                                        if ((attrs.multiSelect && (attrs.multiSelect == true || attrs.multiSelect == "true")) || scope.multiSelect == true){
                                            scope.tableColumns.push({label: "--MULTISELECT--", name: "--MULTISELECT--", size: "30px"});
                                            thead.attr('multi-select', true);
                                            tbody.attr('multi-select', true);
                                            if (!scope.selectedItem) {
                                                scope.selectedItem = [];
                                            }
                                        }

                                        if (scope.DandDEnabled) {
                                            scope.tableColumns.push({label: "", name: "--DRAG_AND_DROP--", size: "1px"});
                                        }  
                                        
                                        if(scope.internalTableConfiguration.rowDetail==true){
                                        	scope.tableColumns.push({label: "", name: "--ROW_DETAIL--", size: "30px"});
                                        }
                                        
                                        
                                        if (attrs.columnsSearch) {
                                            var colSearch = scope.columnsSearch;
                                            if (Object.prototype.toString.call(colSearch) == "[object String]") {
                                                //if is a string, convert it to object
                                                scope.columnsSearch = JSON.parse(colSearch);
                                            }
                                        }

                                        if (attrs.columns) {
                                            var col = scope.columns;
                                            if (Object.prototype.toString.call(col) == "[object String]") {
                                                //if is a string, convert it to object
                                                col = JSON.parse(col);
                                            }

                                            for (var i in col) {
                                                var tmpColData = {};
                                                if (Object.prototype.toString.call(col[i]) == "[object Object]") {
                                                    //json of parameter like name, label,size
                                                    tmpColData.name = col[i].name || "---";
                                                    tmpColData.tooltip = col[i].tooltip;
                                                    tmpColData.label = col[i].label || tmpColData.name;
                                                    tmpColData.size = col[i].size || "";
                                                    tmpColData.editable = (col[i].editable && scope.allowEdit) || false;
                                                    tmpColData.comparatorFunction=col[i].comparatorFunction;
                                                    tmpColData.comparatorColumn=col[i].comparatorColumn;
                                                    tmpColData.transformer=col[i].transformer;
                                                    tmpColData.customClass=col[i].customClass;
                                                    tmpColData.customRecordsClass=col[i].customRecordsClass;
                                                    if(col[i].inputType)tmpColData.inputType=col[i].inputType;
                                                    tmpColData.hideTooltip=col[i].hideTooltip;
                                                    tmpColData.style=col[i].style;
                                                    tmpColData.static=col[i].static;
                                                    tmpColData.template=col[i].template;
                                                } else {
                                                    //only the col name
                                                    tmpColData.label = col[i];
                                                    tmpColData.name = col[i];
                                                    tmpColData.size = "";
                                                    tmpColData.editable = scope.allowEdit || false;
//                                                    tmpColData.comparatorFunction=col[i].comparatorFunction;
//                                                    tmpColData.transformer=col[i].transformer;
                                                }

                                                scope.tableColumns.push(tmpColData);
                                            }
                                        } else {
                                            //load all
                                            var firstValue = scope.ngModel[0];
                                            if (firstValue != undefined) {
                                                for (var key in firstValue) {
                                                    var tmpColData = {};
                                                    tmpColData.label = key;
                                                    tmpColData.name = key;
                                                    tmpColData.size = "";
                                                    tmpColData.editable = key;
                                                    scope.tableColumns.push(tmpColData);
                                                }
                                            }
                                        }
                                        //add speed menu column at the end of the table
                                        if (attrs.speedMenuOption) {
                                            var speedSize = scope.speedMenuOption.length > 3 ? 30 : (scope.speedMenuOption.length * 30 || 30);
                                            scope.tableColumns.push({label: "", name: "--SPEEDMENU--", size: speedSize + "px"});
                                            thead.attr('speed-menu-option', true);
                                            tbody.attr('speed-menu-option', true);
                                        }
                                        //if contextMenu is enabled 
                                        tbody.attr('menu-option', attrs.menuOption!=undefined);
                                        

                                        if (noCompile != true) {
                                            $compile(thead)(scope);
//								$compile(tbody)(scope);
                                        }
                                    };

                                    scope.initializeColumns(true);
                                    
                                    //check for pagination
                                    var paginBox = angular.element(footerBox[0].querySelector("dir-pagination-controls"));
                                    if(attrs.noPagination && (attrs.noPagination == true || attrs.noPagination == "true")){
                                    	tbody.attr('pagination-type', "none");
                                    	paginBox.remove();
                                    }else{
                                    	 if (!attrs.totalItemCount) {
                                    		 tbody.attr('pagination-type', "local");
                                    		 tbody.attr('current-page-number', scope.pagination.currentPageNumber);
                                    		 scope.totalItemCount = undefined;
                                             paginBox.removeAttr("on-page-change");
                                    	 }else{
                                    		 tbody.attr('pagination-type', "backend");
//                                    		 scope.pagination.totalItemCount=scope.totalItemCount;
//                                    		 scope.$watch(function(){return scope.totalItemCount;},function(newVal,oldVal){
//                                    			 if(!angular.equals(newVal,oldVal)){
//                                    			 scope.pagination.totalItemCount=newVal;
//                                    			 }
//                                    		 })
                                    	 }
                                    }
                                    
                                    //check for change-detector
                                    scope.enableChangeDetector=attrs.changeDetector!=undefined;
                                    

                                    $compile(thead)(scope);
                                    $compile(tbody)(scope);

                                    //add search tab
                                    if (!attrs.showSearchBar || attrs.showSearchBar == false || attrs.showSearchBar == "false") {
//                                        angular.element(scope.tableItem[0].querySelector(".tableSearchBar")).css("display", "none");
                                    	  angular.element(scope.tableItem[0].querySelector("angular-table-actions")).css("display", "none");
                                    }

                                    
                                    if (!attrs.searchFunction) {
                                        scope.localSearch = true;
                                    }
									
                                    if(attrs.hasOwnProperty("fullWidth") || (attrs.hasOwnProperty("noPagination") && attrs.noPagination==true)){
                                    	  
										scope.$watch(function(){return scope.getPrincipalTableHeadWidth()}
                                    		  , function (newValue, oldValue) {
	                                        	if(newValue!=oldValue){
	                                        		scope.loadTheadColumn(newValue);
	                                        	}
											});
                                    }

                                    transclude(scope, function (clone, scope) {
                                        angular.element(element[0]).append(clone);
                                    });

                                    
                                    
                                }
                            };
                        }
                    };
                })
        .directive('angularTableHead',
                function ($compile) {
                    return {
                        templateUrl: 'headThTemplate.html',
                        transclude: true,
                        controller: TableHeaderControllerFunction,
                        link: function (scope, element, attrs, ctrl, transclude) {
                        }
                    };
                })
        .directive('angularTableBody',
                function ($compile) {
                    return {
                        templateUrl: 'bodyTemplate.html',
                        transclude: true,
                        controller: TableBodyControllerFunction,
                        priority: 100,
                        compile: function (tElement, tAttrs, transclude) {
                        	var tr=tElement.find("tr");
                        	var td=tElement.find("td");
                        	if(!angular.equals(tAttrs.menuOption,"true")){
                        		//remove the contextMenu
                        		td.removeAttr("data-target");
                        		td.removeAttr("context-menu");
                        		angular.element(tElement[0].querySelector(".dropdown_menu_list")).remove()
                        	}
                        	
                            if (tAttrs.noDragAndDrop != "true") {
                               tr.removeAttr('ui-tree-node');
                               angular.element(td[0].querySelector("i[ui-tree-handle]")).remove();
                            }
                            
                            //check for pagination
                            if(angular.equals(tAttrs.paginationType,"none")){
                            	var repeatAttr=tr.attr("dir-paginate").replace("| itemsPerPage:itemsPerPage","");
                            	tr.attr("ng-repeat",repeatAttr);
                                tr.removeAttr("dir-paginate")
                                tr.removeAttr("total-items")
                                tr.removeAttr("pagination-id")
                                tr.removeAttr("current-page")
                            }else if(angular.equals(tAttrs.paginationType,"local")){
                            	tr.removeAttr('total-items');
                            	if (tAttrs.currentPageNumber == null) {
                            		tr.removeAttr('current-page');
                            	}
                            }else if(angular.equals(tAttrs.paginationType,"backend")){
                            }
                            
                            return {
                                pre: function preLink(scope, element, attrs, ctrl, transclud) {
                                	if(scope.rowDetailTemplate!=undefined){
                                		tr.attr("angular-table-row-detail","");
                                	}
                                }
                                    
                            }
                        }
                            
                    };
                })
        .directive('angularTableFooter',
                function ($compile) {
                    return {
                        template: "<div ng-transclude></div>",
                        controller: TableFooterControllerFunction,
                        transclude: true,
                        link: function (scope, element, attrs, ctrl, transclude) {

                        }
                    };
                })
        .directive('actionBackgroundColor',
                function ($compile) {
                    return {
                        link: function (scope, element, attrs, ctrl, transclude) {
                            element.css("background-color", attrs.actionBackgroundColor);
                        }
                    };
                })
        .directive('actionColor',
                function ($compile) {
                    return {
                        link: function (scope, element, attrs, ctrl, transclude) {
                            element.css("color", attrs.actionColor);
                        }
                    };
                })
        .directive('dynamichtml', function ($compile) {
            return {
                restrict: 'A',
                replace: true,
                link: function (scope, ele, attrs) {
                    scope.$watch(attrs.dynamichtml, function (html) { 
                        ele.html(html);
                        $compile(ele.contents())(scope);
                    });
                }
            };
        })
        .directive('buildTemplate', function ($compile) {
        	return {
        		restrict: 'A',
        		replace: true,
        		link: function (scope, ele, attrs) {
        			ele.html(scope.$eval(attrs.buildTemplate));
    				$compile(ele.contents())(scope);
        		}
        	};
        })
        .directive('queueTable',function ($compile) {
                    return {
                        template: '',
                        replace: true,
                        transclude: true,
                        link: function (scope, element, attrs, ctrl, transclude) {
                            transclude(scope, function (clone, scope) {
                                var contElem = angular.element(element.parent())[0].querySelector("#queueTableContent");
                                angular.element(contElem).append(clone);
                            });
                            transclude(scope, function (clone, scope) {
                                var fidexContElem = angular.element(element.parent())[0].querySelector("#fixedAngularTableContentBox");
                                angular.element(fidexContElem).append(clone);
                            });

                            //enable watch to block on the bottom the foot row if scroll is enabled
                            scope.$watch(function () {
                                var elem = angular.element(scope.tableItem[0].querySelector('#angularTableContentBox'))[0];
                                return elem == undefined ? undefined : {scroll: elem.scrollHeight, heigth: elem.offsetHeight};
                            }, function (newValue, oldValue) {
                                if (newValue != oldValue) { 
                                    if (newValue.scroll > newValue.heigth) {
                                    	scope.tableItem.addClass("absoluteTfoot");
                                    } else {
                                    	scope.tableItem.removeClass("absoluteTfoot")

                                    }
                                }

                            }, true);
                        }
                    };
                })
          .directive('rowDetail',function ($compile) {
            return {
                template: '',
                replace: true,
//                transclude: true,
                compile:function (tElement, tAttrs, transclude) {
                	var notCompiledContent=tElement.html();
                	tElement.html("");
                    return {
                        pre: function preLink(scope, element, attrs, ctrl, transclud) {
//                        	debugger
                        	scope.internalTableConfiguration.rowDetail=true;
                        	scope.rowDetailTemplate=notCompiledContent;
                        	scope.tableItem[0].querySelector("#angularTableContentBox").style.overflow="auto";
                        	scope.initializeColumns(false)
                        	},
                        	post: function preLink(scope, element, attrs, ctrl, transclud) {
                        	}
                        }
                    },
            };
        })
        .directive('angularTableRowDetail',function ($compile) {
        	return {
        		template: '',
        		restrict: 'A',
        		replace: false,
        		link: function (scope, element, attrs, ctrl, transclude) {
        			var td=document.createElement("td");
        			td.innerHTML=scope.rowDetailTemplate;
        			td.setAttribute("colspan",element.scope().tableColumns.length);
        			element.after("<tr/>").next().append(td)
        			element.next().addClass("expanderRowClass");
        			element.next().attr("ng-if","visible");
        			$compile(element.next())(element.scope());
        			
        		}
        	};
        })
        
        .filter('filterBySpecificColumns', function () {
            return function (items, columnsSearch, searchTerm, localSearch) {
                if (searchTerm == undefined || searchTerm == "") {
                    return items;
                }
                var filtered = [];
                for (var item in items) {
                    if (columnsSearch != undefined && columnsSearch.length != 0) {
                        for (var cols in columnsSearch) {
                        	var tmpSearchItem=""
                        	if(angular.isObject(columnsSearch[cols])){
                        		if(columnsSearch[cols].transformer!=undefined){
                        			tmpSearchItem=columnsSearch[cols].transformer(items[item],columnsSearch[cols].name);
                        		}else{
                        			tmpSearchItem=items[item][columnsSearch[cols].name];
                        		}
                        	}else{
                        		tmpSearchItem=items[item][columnsSearch[cols]]
                        	}
                        	
                        	
                            if (tmpSearchItem!= undefined) {
                                if (tmpSearchItem.toString().toUpperCase().indexOf(searchTerm.toUpperCase()) !== -1) {
                                    filtered.push(items[item]);
                                    break;
                                }
                            }
                        };
                    } else {
                        if (JSON.stringify(items[item]) != undefined) {
                            if (JSON.stringify(items[item]).toString().toUpperCase().indexOf(searchTerm.toUpperCase()) !== -1) {
                                filtered.push(items[item]);
                            }
                        }
                    }
                }
                ;
                return filtered;
            };
        })
        .filter('customOrdering', function ($filter) {
        
	        return function(items, column , reverse, tableColumns ,initialSorting,initialSortingAsc) {
	        	function getfiltered(){
	        		var tmp=[];
	        		angular.forEach(items, function(item) {
			            tmp.push(item);
			          });
	        		return tmp;
	        	}
	        	
	        	if(column==undefined && initialSorting!=undefined){
	        		for(var i=0;i<tableColumns.length;i++){
	        			if(tableColumns[i].name== initialSorting){
	        				column=tableColumns[i];
	        				if(initialSortingAsc!=undefined){
	        					if(initialSortingAsc==true){
	        						reverse=true;
	        					}
	        				}
	        				break;
	        			}
	        		}
	        	}
		          if(column!=undefined && column.comparatorFunction!=undefined ){
		        	  var filtered = getfiltered();
			          filtered.sort(function (a, b) {   
			        	  return column.comparatorFunction(a,b);
			          });
			          if(reverse){
			        	  filtered.reverse();
			          }
			          return filtered;
		        }else if(column!=undefined && column.comparatorColumn!=undefined ){
		        	return	$filter('orderBy')(items, column.comparatorColumn , reverse)
		        }else{ 
		        	if(column!=undefined){
		        		var str = JSON.stringify(column.name);
		        		return	$filter('orderBy')(items, str , reverse)
		        	}else{
		        		 var filtered = getfiltered();
		        		 if(reverse){
				        	  filtered.reverse();
				          }
				          return filtered;
		        	}
		        	
		        }
	        };
        });


function TableControllerFunction($scope, $timeout) {
	$scope.pagination = {currentPageNumber : 1};
	if($scope.currentPageNumber!=undefined){
		$scope.pagination.currentPageNumber=$scope.currentPageNumber;
		$scope.$watch('pagination.currentPageNumber',function(nv,ov){if(nv!=ov){$scope.currentPageNumber=nv;}})
		$scope.$watch('currentPageNumber',function(nv,ov){if(nv!=ov){$scope.pagination.currentPageNumber=nv;}})
	}
	if($scope.itemsPerPage==undefined){
		$scope.itemsPerPage=3;
	}
    $scope.tmpWordSearch = "";
    $scope.prevSearch = "";
    $scope.localSearch = false;
    $scope.searchFastVal = "";
    $scope.column_ordering;
    $scope.reverse_col_ord = false;
    $scope.internal_column_ordering;
    $scope.internal_reverse_col_ord = false;
 
    $scope.getDynamicValue=function(item,row,column,index,nature){ 
    	if(item==null || item==undefined) return ;
    	
    	return angular.isFunction(item) ? item(row,column,index) : item;
    }
    
    $scope.searchItem = function (searchVal) {
        if ($scope.localSearch) {
            $scope.searchFastVal = searchVal;
        } else {
            $scope.tmpWordSearch = searchVal;
            $timeout(function () {
                if ($scope.tmpWordSearch != searchVal || $scope.prevSearch == searchVal) {
                    return;
                }
                $scope.prevSearch = searchVal;
                $scope.searchFunction({
                    searchValue: searchVal,
                    itemsPerPage: $scope.itemsPerPage,
                    currentPageNumber: $scope.pagination.currentPageNumber,
                    columnsSearch: $scope.columnsSearch,
                    columnOrdering: $scope.column_ordering,
                    reverseOrdering: $scope.reverse_col_ord
                });

            }, 1000);
        }
    };

    // pagination item
    $scope.changeWordItemPP = function () {

    	if($scope.box==undefined || $scope.box.$$NG_REMOVED==true){
    		var box = $scope.tableItem[0];
//    		var box = angular.element(document.querySelector('angular-table.' + $scope.id + 'ItemBox'))[0]
    		if (box == undefined) {
    			return;
    		}
    		$scope.box=box;
    	}
    	
    	if($scope.tableContainer==undefined || $scope.tableContainer.$$NG_REMOVED==true){
    		var tableContainer = angular.element($scope.tableItem[0].querySelector('#angularFullTableContentBox'))[0];
//    		var tableContainer = angular.element(document.querySelector('angular-table.' + $scope.id + 'ItemBox #angularFullTableContentBox'))[0];
    			$scope.tableContainer=tableContainer;
    	}
    	
    	if($scope.headButton==undefined || $scope.headButton.$$NG_REMOVED==true){
    		var headButton = angular.element($scope.tableItem[0].querySelector('table.fakeTable thead'))[0];
    		var headButton = angular.element(document.querySelector('angular-table.' + $scope.id + 'ItemBox table.fakeTable thead'))[0];
    			$scope.headButton=headButton;
    	}
    	
    	if($scope.listItemTemplBox==undefined || $scope.listItemTemplBox.$$NG_REMOVED==true){
    		var listItemTemplBox = angular.element($scope.tableItem[0].querySelector(' table.principalTable tbody tr'))[0];
    			$scope.listItemTemplBox=listItemTemplBox;
    	}

    	if($scope.queueTable==undefined || $scope.queueTable.$$NG_REMOVED==true){
    		var queueTable = angular.element($scope.tableItem[0].querySelector(' #queueTableContent'))[0];
    			$scope.queueTable=queueTable;
    	}
    	
    	if($scope.tableHeader==undefined || $scope.tableHeader.$$NG_REMOVED==true){
    		var tableHeader = angular.element($scope.tableItem[0].querySelector('.angularTableHeader'))[0];
    			$scope.tableHeader = tableHeader;
    	}
    		 
    	var tableContainerHeight = $scope.tableContainer == undefined ? 33 : $scope.tableContainer.offsetHeight - $scope.tableHeader.offsetHeight;
        var headButtonHeight = $scope.headButton == undefined ? 0 : $scope.headButton.offsetHeight;
        var listItemTemplBoxHeight = $scope.listItemTemplBox == undefined ? 33 : $scope.listItemTemplBox.offsetHeight;
        var heightQueueTable = $scope.queueTable.offsetHeight>0? $scope.queueTable.offsetHeight:0;;
        
        var scrollHeight=($scope.tableContainer.scrollWidth>$scope.tableContainer.offsetWidth) ? 20:0;
        
        var avaiableHeight=tableContainerHeight - headButtonHeight - heightQueueTable-scrollHeight;
        if ($scope.firstLoad && $scope.noPagination != true) {
        	avaiableHeight-=30;
        }

        var nit = parseInt(avaiableHeight / listItemTemplBoxHeight);

        if(!$scope.firstLoad && angular.equals( $scope.itemsPerPage , nit)){
        	return;
        }
        $scope.itemsPerPage = (nit <= 0 || isNaN(nit)) ? 0 : nit;
        if ($scope.firstLoad) {
            $scope.pageChangedFunction({
                searchValue: "",
                itemsPerPage: $scope.itemsPerPage,
                currentPageNumber: $scope.pagination.currentPageNumber,
                columnsSearch: $scope.columnsSearch,
                columnOrdering: $scope.column_ordering,
                reverseOrdering: $scope.reverse_col_ord
            });
            $scope.firstLoad = false;
        }
    };

    $scope.firstLoad = $scope.disableAutoLoadOnInit!=undefined ? false : true;
    $timeout(function () {
        if ($scope.noPagination != true) {
        	if($scope.fixedItemPerPage!=true){
                $scope.changeWordItemPP();
        	}else{
        		 $scope.pageChangedFunction({
                     searchValue: "",
                     itemsPerPage: $scope.itemsPerPage,
                     currentPageNumber: $scope.pagination.currentPageNumber,
                     columnsSearch: $scope.columnsSearch,
                     columnOrdering: $scope.column_ordering,
                     reverseOrdering: $scope.reverse_col_ord
                 });
        	}
        }
    }, 0);

  
    $scope.$watch(function(){
         return $scope.enableChangeDetector ?  {items:($scope.ngModel==undefined ? 0 : $scope.ngModel.length),height:($scope.tableItem[0] == undefined ? null : $scope.tableItem[0].offsetHeight),ngModel:$scope.ngModel} : {items:($scope.ngModel==undefined ? 0 : $scope.ngModel.length),height:($scope.tableItem[0] == undefined ? null : $scope.tableItem[0].offsetHeight)};
    }, function(newValue,oldValue){
    	if ($scope.noPagination != true &&( newValue.items != 0 ||  newValue.height != 0) && $scope.fixedItemPerPage!=true) {
	    	if(newValue!=oldValue){
	    		$timeout(function(){
	    			$scope.changeWordItemPP();
	    			},0) 
	    	}
    	}
    	
    	if($scope.enableChangeDetector && !angular.equals(oldValue.ngModel,newValue.ngModel)){
    		$scope.changeDetectorFunction(newValue.ngModel,oldValue.ngModel);
   		 	$timeout(function(){
   		 		$scope.clearChangeItemStyle();
   		 	},7000)
   	}
    },true);
    
    $scope.$watchCollection('columns', function (newValue, oldValue) {
        if (!angular.equals(newValue, oldValue)) {
            $scope.initializeColumns();
        }
    });
    

    
    var ptThead;
	$scope.getPrincipalTableHeadWidth=function(){
		if(ptThead==undefined){
			ptThead = angular.element($scope.tableItem[0].querySelector('#angularTableContentBox .principalTable thead'))[0];
		}
	 	  return ptThead == undefined ? null : ptThead.offsetWidth;
	}
	
	var lastTheadVal=0; 
	$scope.loadTheadColumn=function(width){
		lastTheadVal=width;
	    $timeout(function(){
	    	if(angular.equals(lastTheadVal,width)){
	    		var tableContentBox=angular.element($scope.tableItem[0].querySelector('#angularTableContentBox'));
	    		var fakeDiv = angular.element($scope.tableItem[0].querySelectorAll('.faketable th>div'));
	    		var principalThDiv = angular.element($scope.tableItem[0].querySelectorAll('.principalTable th>div'));
	    	}
	    },0)
	    
	
	}
 
	$scope.isVisibleRowFunction=function(row){
		
		if(row==undefined){
			return true;
		}
		var isVisible =$scope.visibleRowFunction({item:row}) ;
		
		return isVisible == undefined ? true : isVisible;
	}
	
	 $scope.clearChangeItemStyle=function(){
		 $scope.getChangedValueStyle=undefined;
		 $scope.detectorMap=angular.copy(emptyDetectorMap); 
    }
	 var emptyDetectorMap={edit:{},add:[]}
	 $scope.detectorMap=angular.copy(emptyDetectorMap); 
	 $scope.changeDetectorFunction=function(newVal,oldVal){
		 if(oldVal==undefined || oldVal.length==0){
			 return;
		 }
		 
		 var table1 = new daff.TableView(jsonToTable(oldVal));
		 var table2 = new daff.TableView(jsonToTable(newVal));
		 var data_diff = [];
		 var table_diff = new daff.TableView(data_diff);
		 var alignment = daff.compareTables(table1,table2).align();
		 var flags = new daff.CompareFlags();
		 flags.show_unchanged=true
		 var highlighter = new daff.TableDiff(alignment,flags);
		 highlighter.hilite(table_diff);
		 
		 var decIndex=0;
		 var columnArray=[];
		 angular.forEach(data_diff, function(value, index) {
			 if(!angular.equals(value[0],"@@") && !angular.equals(value[0],"!")){
				 var currRow=newVal[index-decIndex];
				 if(angular.equals(value[0],"->")){
					 //edited row
//					 $scope.detectorMap.edit.push(currRow);
					 
					 for(var i=1;i<value.length;i++){
						 
						 if(angular.isString(value[i]) && value[i].split("->").length>1){
							 $scope.columns[i];
							 if($scope.detectorMap.edit[columnArray[i]]==undefined){
								 $scope.detectorMap.edit[columnArray[i]]=[];
							 }
							 $scope.detectorMap.edit[columnArray[i]].push(currRow);
						 }
					 }
					 
					 
					 $scope.detectorMap.edit.hasOwnProperty()
				 }else if(angular.equals(value[0],"---")) {
					 //removed row
					 decIndex++;
				 }else if(angular.equals(value[0],"+++")){
					 //added row
					 $scope.detectorMap.add.push(currRow);
				 }
			 }else{

				 if(angular.equals(value[0],"@@")){
					 //create column array
					 for(var i=0;i<value.length;i++){
						 columnArray.push(value[i]);
					 }
				 }
				 
				 decIndex++;
			 }
		 });
		 if(!angular.equals($scope.detectorMap,emptyDetectorMap)){
			 $scope.getChangedValueStyle=function(row,col){
				 var rind= $scope.detectorMap.edit[col]==undefined ? -1:  $scope.detectorMap.edit[col].indexOf(row);
				 if(rind!=-1){
					 return "editCell";
				 }
				 var rind= $scope.detectorMap.add.indexOf(row);
				 if(rind!=-1){
					 return "addCell";
				 }
			 }
		 }else{
			 $scope.getChangedValueStyle=undefined;
		 }
	 }
	 
	 $scope.getChangedValueStyle=undefined;
	 
	 function jsonToTable(jsarr){
		 var toRet=[]
		 angular.forEach(jsarr, function(arrValue, arrKey) {
			 var tmpToRet=[];
			 if(arrKey==0){
			 //create the first item with column name
			 var colname=[];
			 angular.forEach(arrValue, function(colVal, colName) {
					 this.push(colName)
				},colname);
			 this.push(colname);
			 }
			 
			 //create all row
			 angular.forEach(arrValue, function(objValue, objKey) {
					 this.push(objValue)
				},tmpToRet);
			 this.push(tmpToRet);
			},toRet);
		 return toRet;
		 
	 }
}

function TableBodyControllerFunction($scope) {
	
    $scope.clickItem = function (row,columnName, cell, evt,index) {
        if ($scope.multiSelect) {
            $scope.toggleMultiSelect(row, evt);
        } else {
            $scope.selectedItem = row;
        }

        $scope.clickFunction({
            item: row,
            cell: cell,
            listId: $scope.id,
            row: row,
            column: cell,
            index:index,
            evt:evt,
            columnName:columnName
        });
    };

    $scope.toggleMultiSelect = function (row, evt) {
        evt.stopPropagation();
        var index = $scope.indexInList($scope.selectedItem, row);
        if (index > -1) {
            $scope.selectedItem.splice(index, 1);
        } else {
            $scope.selectedItem.push(row);
        }
    };

    $scope.indexInList = function (list, item) {
        for (var i = 0; i < list.length; i++) {
        	if($scope.comparisonColumn==undefined){
	            if (angular.equals(list[i], item)) {
	                return i;
	            }
        	}else{
        		 if (angular.equals(list[i][$scope.comparisonColumn], item[$scope.comparisonColumn])) {
 	                return i;
 	            }
        	}
        }
        return -1;
    };

    $scope.isSelected = function (item) {
        if ($scope.multiSelect) {
            return $scope.indexInList($scope.selectedItem, item) > -1;

        } else {
            return angular.equals($scope.selectedItem, item);
        }

    };

    var oldObject = [];
    $scope.editingMap = {};

    $scope.checkIfEditable = function (row, column, cell, evt) {
        var allowEdit = $scope.allowEdit && column.editable;
        if ($scope.allowEditFunction) {
            allowEdit = allowEdit && $scope.allowEditFunction({
                item: row,
                cell: cell,
                listId: $scope.id,
                row: row,
                column: column
            });
        }
        return allowEdit;
    };

    $scope.startEdit = function (row, column, cell, evt) {
        var allowEdit = $scope.checkIfEditable(row, column, cell, evt);
        if (allowEdit && oldObject.length < 1) {
            oldObject.push(angular.copy(row));
            row.editing = true;
        }
    };

    $scope.doneEdit = function (row, column, cell, evt) {
        if (row.editing) {
            row.editing = undefined;
            var oldObj = oldObject.pop();
            if (!angular.equals(row, oldObj)) {
                if ($scope.editFunction) {
                    var response = $scope.editFunction({
                        item: row,
                        itemOld: oldObj,
                        cell: cell,
                        listId: $scope.id,
                        row: row,
                        column: cell
                    });
                    //check which type is the response. Could be either boolean or promise
                    if (typeof response == "boolean" && response == false) {
                        for (var k in row) {
                            row[k] = angular.copy(oldObject.row[k]);
                        }
                    } else if (typeof response == "object" && typeof response.then == "function") {
                        response.then(
                                function () {
                                },
                                function () {
                                    for (var k in row) {
                                        row[k] = angular.copy(oldObj[k]);
                                    }
                                }
                        );
                    }
                }
            }
        }
    };
    $scope.showRowDetail=function(ev){
    	var vis= angular.element(ev.currentTarget.parentElement.parentElement).scope().visible;
    	angular.element(ev.currentTarget.parentElement.parentElement).scope().visible = (vis!=true);
    }
}

function TableHeaderControllerFunction($scope, $timeout) {
    $scope.multiSelectVal = false;
    $scope.selectedAllPageNumbers = {};

    $scope.selectAll = function () {
        $scope.multiSelectVal = !$scope.multiSelectVal;
        // keep track if in current page has been sleected all value
        $scope.selectedAllPageNumbers[$scope.pagination.currentPageNumber] =  $scope.multiSelectVal;
        
        
//        var template = angular.element(document.querySelector("angular-table." + $scope.id + "ItemBox"));
        var table = angular.element($scope.tableItem[0].querySelector("table.principalTable"));
        var tbody = angular.element(table[0].querySelector("tbody"));
        var rows = tbody[0].children;

        var j = 0;
        for (var i = 0; i < rows.length; i++) {
            $timeout(function () {
                var checkCol = angular.element(rows[j].querySelector('td'));
                j++;

                if (checkCol[0].querySelector('md-checkbox').checked != $scope.multiSelectVal) {
                    checkCol.triggerHandler('click');
                }
            }, 0);

        }
    };

    $scope.orderBy = function (column) {
    	
    	if($scope.sortableColumn!=undefined && $scope.sortableColumn.indexOf(column.name)==-1){
    		return
    	}
    	
        if ($scope.column_ordering!=undefined && $scope.column_ordering.name == column.name) {
            $scope.reverse_col_ord = !$scope.reverse_col_ord;
        } else {
            $scope.column_ordering = column;
            $scope.reverse_col_ord = false;
        }

        if ($scope.localSearch) {
            $scope.internal_column_ordering = $scope.column_ordering;
            $scope.internal_reverse_col_ord = $scope.reverse_col_ord;
        } else {
        	$scope.pagination.currentPageNumber =1;
            $scope.searchFunction({
                searchValue: $scope.prevSearch,
                itemsPerPage: $scope.itemsPerPage,
                currentPageNumber: $scope.pagination.currentPageNumber,
                columnsSearch: $scope.columnsSearch,
                columnOrdering: $scope.column_ordering,
                reverseOrdering: $scope.reverse_col_ord
            });
        }
        
        if($scope.loadTheadColumn!=undefined){
        	$scope.loadTheadColumn($scope.getPrincipalTableHeadWidth());
        }
    }; 
    $scope.getColumnValue=function(row,columnName,columnTransformationText){ 
    	if(typeof columnName == 'string'){
    		var splname=columnName.split(".");
        	var toReturn="";
        	if(splname.length>1){
        		var tmpVal=row[splname[0]];
        		for(var i=1;i<splname.length;i++){
        			if(tmpVal!=null && tmpVal!=undefined){
        				 tmpVal=tmpVal[splname[i]];
        			}else{
        				break;
    				}
        		}
        		if(tmpVal!=null && tmpVal!=undefined){
        			toReturn= tmpVal;
        		}else{
        			toReturn= row[columnName];
        		}
        	}else{
        		toReturn= row[columnName];
        	}
        	
        	if(columnTransformationText==undefined){
        		return toReturn;
        	}else{
        		//apply filter
        		return columnTransformationText(toReturn,row,columnName);
        	}
    	}
    	
    }
}
function TableFooterControllerFunction($scope, $timeout) {
	$scope.pageChangedHandler=function(searchValue,itemsPerPage,currentPageNumber,columnsSearch,columnOrdering,reverseOrdering,changeFromInput,paginationItem,directivePageChangeFunction){
		if(changeFromInput!=undefined){
			return $scope.setCurrentPageFromInput(changeFromInput,paginationItem,directivePageChangeFunction);
		}else{
			
			// when changing page set to blank the select all unless it was previously checked
			
			if($scope.selectedAllPageNumbers[currentPageNumber] != undefined &&
					$scope.selectedAllPageNumbers[currentPageNumber]==true){
				$scope.multiSelectVal = true;
			}
			else{
				$scope.multiSelectVal = false;	
			}
			
			$scope.selectedAllPageNumbers[$scope.pagination.currentPageNumber]
			
			
			$scope.pageChangedFunction({
                 searchValue: searchValue,
                 itemsPerPage: itemsPerPage,
                 currentPageNumber:currentPageNumber,
                 columnsSearch: columnsSearch,
                 columnOrdering: columnOrdering,
                 reverseOrdering: reverseOrdering
             });
		}
	}
	
	
	$scope.tmpSearchCurrPage;
	$scope.setCurrentPageFromInput=function(num,paginationItem,functChangePage){
		
    	if(angular.equals(num.trim(),"")){
    		return num
    	}
    	
    	if(isNaN(parseInt(num)) || parseInt(num)>paginationItem.last ){
    		 return paginationItem.current;
    	}
    	$scope.tmpSearchCurrPage=num;
    	$timeout(function(){
    		
    		if($scope.tmpSearchCurrPage==num){
    			functChangePage(num);
    		}
    	},500) 
    	 return num;
    }
}