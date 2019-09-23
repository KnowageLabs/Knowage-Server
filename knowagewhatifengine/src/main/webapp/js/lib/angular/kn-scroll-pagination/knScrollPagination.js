angular.module('knScrollPagination',[]);

(function(){
	var scripts = document.getElementsByTagName('script')
	var componentTreePath = scripts[scripts.length-1].src;
	componentTreePath = componentTreePath.substring(0, componentTreePath.lastIndexOf('/') + 1);



	var app = angular.module('knScrollPagination',[]);

		app.directive('pivotTable',function(){

			var link = function(){

			}

		var controller = function($scope, $element){

			var pivotTable1 = new pivotTable($element[0]);

			var getRowCountInParent = function(){
				if($element.parent()[0]){
					var SCROLL_WIDTH = 15;
					var tableBodyDataColumnElements = pivotTable1.getHtmlTableBodyDataColumnElements(0);
					var bounds = new Bounds($element.parent()[0],new Margin(0,0,SCROLL_WIDTH,SCROLL_WIDTH));

					for (var i = 0; i < tableBodyDataColumnElements.length; i++) {

	        			if(bounds.isOutOfBounds(tableBodyDataColumnElements[i])){
	        				return i;
	        			}
					}
					return tableBodyDataColumnElements.length;
				}else{
					return 0;
				}

			}

			var getColumnCountInParent = function(){
				if($element.parent()[0]){
					var SCROLL_WIDTH = 15;
					var tableBodyDataRowElements = pivotTable1.getHtmlTableBodyDataRowElements(0);
					var bounds = new Bounds($element.parent()[0],new Margin(0,0,SCROLL_WIDTH,SCROLL_WIDTH));

					for (var i = 0; i < tableBodyDataRowElements.length; i++) {

	        			if(tableBodyDataRowElements.length>0&&bounds.isOutOfBounds(tableBodyDataRowElements[i])){
	        				return i;
	        			}
					}
					return tableBodyDataRowElements.length;
				}else{
					return 0;
				}

			}

			var getTableHeight = function(){
				if($element.parent()[0]){
					return $element.parent()[0].offsetHeight;
				}

			}

			var getTableWidth = function(){
				if(pivotTable1.getHtmlTable())
				return pivotTable1.getHtmlTable().offsetWidth;
			}

			var getHeadersHeight = function(){
				if(pivotTable1.getHtmlTableHeader())
				return pivotTable1.getHtmlTableHeader().offsetHeight;
			}

			$scope.$watch(getHeadersHeight,function(newValue,oldValue){
				if(newValue!=undefined&&!isNaN(newValue)){
					$scope.tableHeaderHeight = newValue;
				}
			})

			$scope.$watch(getTableHeight,function(newValue ,oldValue){
					var SCROLL_WIDTH = 15;
					var ROW_HEIGHT = 50;
					if($element.parent()[0]){
						var resizeRowsBounds = new Bounds($element.parent()[0],new Margin(0,0,SCROLL_WIDTH+ROW_HEIGHT,-getTableWidth()+10));
						var bounds = new Bounds($element.parent()[0],new Margin(0,0,SCROLL_WIDTH,-getTableWidth()+10));
						var rowCountInParent = getRowCountInParent();
						if(!resizeRowsBounds.isOutOfBounds(pivotTable1.getHtmlTable())&&$scope.modelConfig.rowCount>$scope.modelConfig.rowsSet){

							$scope.modelConfig.rowsSet = 50;

		        		}

						if(bounds.isOutOfBounds(pivotTable1.getHtmlTable())){

							$scope.modelConfig.rowsSet = rowCountInParent;


		        		}
					}

			},true)

			$scope.$watch(getTableWidth,function(newValue ,oldValue){
					if(getColumnCountInParent()>0){
						$scope.columnSet = getColumnCountInParent();
					}
			},true)

		}
		return{

			restrict:'C',
			controller:controller,
			link:link

		}
	});

	app.directive('resize',function(){

		function link(scope,element,attrs){
			scope.$watchGroup(['height','width'],function(){
				 element.css('min-height', (scope.height + 10) +'px');
				 element.css('width', scope.width + 'px');
			})
		}
		return{
			restrict:'A',
			link:link,
			scope:{
				height:'=',
				width:'='
			}
		}
	});

	app.directive('scroll',function($window,$anchorScroll){

		function link(scope,element,attrs){
			$anchorScroll.yOffset = 50;

			scope.$watchGroup(['scrollTop','scrollLeft'],function(newValues,oldValues,scope){
				element[0].scrollTop = newValues[0];
				element[0].scrollLeft = newValues[1];
			})
			element.on('scroll',function(){

				scope.scrollTop = Math.round(element[0].scrollTop);
				scope.scrollLeft = Math.round(element[0].scrollLeft);
				scope.$apply();
			})

			scope.$on('tableScroll',function(event,delta){

			scope.scrollTop = Math.round(element[0].scrollTop);
			scope.scrollLeft = Math.round(element[0].scrollLeft);
			scope.$apply();

			})
		}



		return{

			restrict:'A',
			link:link,

		}
	});

	app.directive('tableWheelScroll',function($window,$anchorScroll,$rootScope){


		function link(scope,element,attrs){
		element.bind("DOMMouseScroll mousewheel onmousewheel",function(event){

			var event = window.event || event; // old IE support
            var delta = Math.max(-1, Math.min(1, (event.wheelDelta || -event.detail)));
            $rootScope.$broadcast('tableScroll',delta);

		 })
		}

		return{

			restrict:'A',
			link:link,

		}
	})

	app.directive('scroller',function(){

		return{

			restrict:'E',
			templateUrl:componentTreePath+"scroller.html",
			scope:{
				height:'=',
				width:'=',
				scrollTop:'=',
				scrollLeft:'='
			}
		}
	});

	app.directive('tableScroller',function(){

		function link(scope,element,attrs){

				scope.$watch('rowNo',function(){
					scope.scrollTop = scope.cellHeight * scope.rowNo;
					scope.scrollLeft = scope.cellWidth * scope.columnNo;
				},true)

				scope.$watchGroup(['scrollTop','scrollLeft'],function(){
					scope.rowNo =	Math.round(scope.scrollTop / scope.cellHeight)  ;
					scope.columnNo =	Math.round(scope.scrollLeft / scope.cellWidth)  ;
				},true)

				scope.$on('tableScroll',function(event,delta){

				var newRowNo = scope.rowNo - delta;
				if(newRowNo>=0&&newRowNo<=scope.rowCount){
					scope.rowNo = newRowNo;
					scope.$apply();
				}

			})


		}

		var controller = ['$scope',function($scope){



			$scope.getTableHeight = function(){
				return $scope.cellHeight * $scope.rowCount;
			}

			$scope.getTableWidth = function(){
				return $scope.cellWidth * $scope.columnCount;
			}

			$scope.getscrollTop = function(){
				return $scope.cellHeight * $scope.rowNo;
			}


		}]

		return{

			restrict:'E',
			templateUrl:componentTreePath+"tableScroller.html",
			scope:{
				cellHeight:'=',
				cellWidth:'=',

				columnCount:'=',
				rowCount:'=',

				rowNo:'=',

			}
		,controller:controller,
		link:link


		}
	});

function pivotTable(table){

    	this.table = table;

		this.getHtmlTable = function(){

			return this.table;

		};

		this.getHtmlTableRows = function(){

			return this.getHtmlTable().getElementsByTagName("tr");
		};

		this.getHtmlTableRow = function(rowNumber){

			return this.getHtmlTableRows()[rowNumber];
		};

		this.getHtmlTableHeaderRow = function(rowNumber){

			return this.getHtmlTableHeader().getElementsByTagName("tr")[rowNumber];
		};

		this.getHtmlTableHeaderRows = function (){

			return this.getHtmlTableHeader().getElementsByTagName("tr");
		}

		this.getHtmlTableBodyRow = function(rowNumber){

			return this.getHtmlTableBody().getElementsByTagName("tr")[rowNumber];
		};

		this.getHtmlTableBodyRows = function(){

			return this.getHtmlTableBody().getElementsByTagName("tr");
		};

		this.getHtmlTableColumns = function (htmlRow){

			return htmlRow.children;

		};

		this.getHtmlTableBodyDataRowElements = function (rowNumber){

			if(this.getHtmlTableBodyRow(rowNumber)){
				return this.getHtmlTableBodyRow(rowNumber).getElementsByTagName("td");
			}else{
				return [];
			}


		};

		this.getHtmlTableBodyDataElements = function (){

			var bodyDataElements;

			if(this.getHtmlTableBody()){
				var bodyDataElements = this.getHtmlTableBody().getElementsByTagName("td");
			}

			return bodyDataElements;
		};

		this.getHtmlTableBodyDataColumnElements = function(columnNumber){

			var bodyDataColumnElements = [];

			var bodyRows = this.getHtmlTableBodyRows();

			for (var i = 0; i < bodyRows.length; i++) {
				var bodyColumnElement = this.getHtmlTableBodyDataRowElements(i)[columnNumber];
				bodyDataColumnElements.push(bodyColumnElement);
			}
			return bodyDataColumnElements;
		}

		this.getHtmlTableHeader = function(){

			return this.getHtmlTable().getElementsByTagName("thead")[0];
		};


		this.getHtmlTableBody =function(){

			var htmlTable;

			if(this.getHtmlTable()){

				htmlTable = this.getHtmlTable().getElementsByTagName("tbody")[0];
			}

			return htmlTable;
		};

		this.getHtmlTableBodyHeaders = function(rowNumber){
			var bodyRowHeaders = [];
			var bodyRow;
			bodyRowColumns = this.getHtmlTableBodyRow(rowNumber).children;

			for (var i = 0; i < bodyRowColumns.length; i++) {

				if(bodyRowColumns[i].nodeName==='TH'){
					bodyRowHeaders.push(bodyRowColumns[i]);
				}
			}

			return bodyRowHeaders;
		}

	}

function Bounds(htmlElement,marginObj){
	this.rectObject = htmlElement.getBoundingClientRect();
	this.margin = marginObj;


	this.isOutOfBounds = function(htmlElement){

		var compareRectObject = htmlElement.getBoundingClientRect();
		if(this.rectObject.top+this.margin.top>compareRectObject.top||
				this.rectObject.left+this.margin.left>compareRectObject.left||
				this.rectObject.bottom-this.margin.bottom<compareRectObject.bottom||
				this.rectObject.right-this.margin.right<compareRectObject.right
				){
			return true;
		}
		return false;
	}


}

function Margin(top,left,bottom,right){
	this.top = top;
	this.left = left;
	this.bottom = bottom;
	this.right = right;

}
})();
