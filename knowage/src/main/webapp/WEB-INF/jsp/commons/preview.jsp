<%--
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
--%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>

<script type="text/javascript" src="<%= GeneralUtilities.getSpagoBiContext() %>/js/lib/angular/angular_1.4/angular.min.js"></script>
<!DOCTYPE html>
    <head>
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/reset_2018.css">
        <script src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/ag-grid.min.noStyle.js"></script>
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/styles/ag-grid.css">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/styles/ag-theme-balham.css">
    	<style>
    		html, body {height: 100%;}

			.ag-cell-label-container.data-type-string {
				box-shadow: inset 5px 10px 0 -5px #AD1457;
			}
			.ag-cell-label-container.data-type-float {
				box-shadow: inset 5px 10px 0 -5px #1565C0;
			}
			.ag-cell-label-container.data-type-int {
				box-shadow: inset 5px 10px 0 -5px #EF6C00;
			}
			.ag-cell-label-container.data-type-date {
				box-shadow: inset 5px 10px 0 -5px #2E7D32;
			}
			.ag-cell-label-container.data-type-boolean {
				box-shadow: inset 5px 10px 0 -5px #00695C;
			}
			.ag-theme-balham .ag-header-cell-label .ag-header-cell-text {
				color: #232323;
			}
			.kn-preview-table-theme .ag-header-cell {
				padding-left: 8px;
				padding-right: 8px;
			}
			.kn-preview-table-theme .ag-header-cell-menu-button .ag-icon-menu {
				height:48px;
			}
			.kn-preview-table-theme .ag-cell-type{
				position: absolute;
				height: 20px !important;
				top:24px;
				line-height: 20px;
				font-size: 10px;
				clear:both;
			}
			
			.kn-preview-table-theme .ag-header-cell::after, .kn-preview-table-theme .ag-header-group-cell::after,.kn-preview-table-theme .ag-header-cell-resize::after {
				height: 24px;
				margin-top: 16px;
			}
    	</style>
    </head>
    <body>
        <div id="myGrid" style="height: 100%;width:100%;" class="ag-theme-balham kn-preview-table-theme"></div>
		<script type="text/javascript" charset="utf-8">
	  
	  		var url = new URL(window.location.href);
	  		var datasetLabel = url.searchParams.get("dataset");
	  		var options = JSON.parse(url.searchParams.get("options"));
	  		debugger;

	    		
	  		function filterType(type){
	  			if(type == 'date') return 'agDateColumnFilter';
	  			if(type == 'float' || type == 'int') return 'agNumberColumnFilter';
	  			return 'agTextColumnFilter';
	  		}
	  		
	  		function compareDates(filterLocalDateAtMidnight, cellValue){
	            var dateAsString = cellValue;
	            var dateParts  = dateAsString.split("/");
	            var cellDate = new Date(Number(dateParts[2]), Number(dateParts[1]) - 1, Number(dateParts[0]));
	            if (filterLocalDateAtMidnight.getTime() == cellDate.getTime()) return 0
	            if (cellDate < filterLocalDateAtMidnight) return -1;
	            if (cellDate > filterLocalDateAtMidnight) return 1;
	        }
		
		  	function getColumns(fields) {
				var columns = [];
				for(var f in fields){
					if(typeof fields[f] != 'object') continue;
					var tempCol = {"headerName":fields[f].header,"field":fields[f].name, "tooltipField":fields[f].name};
					tempCol.headerComponentParams = {template: headerTemplate(fields[f].type)};
					tempCol.filter = filterType(fields[f].type);
					if(fields[f].type == 'date'){
						tempCol.filterParams = {
							comparator: function (filterLocalDateAtMidnight, cellValue) {

					            //dd/mm/yyyy
					            var dateParts  = cellValue.split("/");
					            var day = Number(dateParts[2]);
					            var month = Number(dateParts[1]) - 1;
					            var year = Number(dateParts[0]);
					            var cellDate = new Date(day, month, year);

					            if (cellDate < filterLocalDateAtMidnight) return -1;
					            else if (cellDate > filterLocalDateAtMidnight) return 1;
					            else return 0;
					        }
					    }
					}
					columns.push(tempCol);
				}
				return columns
			}
		
		  // let the grid know which columns and what data to use
		  var gridOptions = {
		    enableSorting: true,
		    enableFilter: true,
		    pagination: options && options.pagination ? true : false,
		    suppressDragLeaveHidesColumns : true,
		    enableColResize: true,
            paginationAutoPageSize: true,
            headerHeight: 48,
		  };
		  
		  function headerTemplate(type) { 
				return 	'<div class="ag-cell-label-container data-type-'+type+'" role="presentation">'+
						'	 <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span>'+
						'    <div ref="eLabel" class="ag-header-cell-label" role="presentation">'+
						'       <span ref="eText" class="ag-header-cell-text" role="columnheader"></span>'+
						'       <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span>'+
						'       <span ref="eSortOrder" class="ag-header-icon ag-sort-order" ></span>'+
						'    	<span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon" ></span>'+
						'   	<span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon" ></span>'+
						'  		<span ref="eSortNone" class="ag-header-icon ag-sort-none-icon" ></span>'+
						'		<span class="ag-cell-type">'+type+'</span>'+
						'	</div>'+
						'</div>';
			}

		    fetch('http://localhost:8080/knowage/restful-services/2.0/datasets/'+datasetLabel+'/data',{
				  method: "POST",
				  //body: {}
				}).then(function(response) {return response.json()})
				.then(function(data){
					if(data.errors){
						gridOptions.api.showNoRowsOverlay();
					}else{
						gridOptions.api.setColumnDefs(getColumns(data.metaData.fields));
				        gridOptions.api.setRowData(data.rows);
					}
		      })
		    
		
		  // lookup the container we want the Grid to use
		  var eGridDiv = document.querySelector('#myGrid');
		
		  // create the grid passing in the div to use together with the columns & data we want to use
		  new agGrid.Grid(eGridDiv, gridOptions);
	
	  </script>
    </body>
</html>