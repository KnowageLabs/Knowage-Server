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

<!DOCTYPE html>
    <head>
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/reset_2018.css">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/styles/ag-grid.css">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/styles/ag-theme-balham.css">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/customStyle.css">
    	<script src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/ag-grid.min.noStyle.js"></script>
    	<!-- POLYFILLS -->
    	<script src="<%= GeneralUtilities.getSpagoBiContext() %>/polyfills/fetch-polyfill/fetch.js"></script>
    	<script src="<%= GeneralUtilities.getSpagoBiContext() %>/polyfills/url-polyfill/url-polyfill.min.js"></script>
    	<style>
    		html, body {height: 100%;}
    	</style>
    </head>
    <body>
        <div id="myGrid" class="ag-theme-balham kn-preview-table-theme"></div>
		<script type="text/javascript" charset="utf-8">
	  
			//Getting the url parameters
	  		var url = new URL(window.location.href);
	  		var datasetLabel = url.searchParams.get("dataset");
	  		var options = JSON.parse(url.searchParams.get("options"));
	    	
	  		//Defining filter type depending on the column data type
	  		function filterType(type){
	  			if(type == 'date') return 'agDateColumnFilter';
	  			if(type == 'float' || type == 'int') return 'agNumberColumnFilter';
	  			return 'agTextColumnFilter';
	  		}
		
	  		//Function to create the colDefs for ag-grid
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
		  	
		  	function changeSorting(){
				var sorting = gridOptions.api.getSortModel();
			}
		
		  	//Defining ag-grid options
		  	var gridOptions = {
			    enableSorting: options && typeof options.sorting != 'undefined' ? options.sorting : true,
			    enableFilter: options && typeof options.filter != 'undefined' ? options.filter : true,
			    pagination: options && typeof options.pagination != 'undefined' ? options.pagination : true,
			    suppressDragLeaveHidesColumns : true,
			    enableColResize: true,
	            paginationAutoPageSize: true,
	            headerHeight: 48,
	            onSortChanged: changeSorting,
			};
		  
		  	//Defining the custom template for the table header
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
			  
		  	//Function to get the columns metadata and data
			function refreshRows() {
				fetch('http://localhost:8080/knowage/restful-services/2.0/datasets/'+datasetLabel+'/data',{
			  		method: "POST",
			  		//body: {}
				})
				.then(function(response) {return response.json()})
				.then(function(data){
					if(data.errors){
						gridOptions.api.showNoRowsOverlay();
					}else{
						if(!gridOptions.columnDefs) gridOptions.api.setColumnDefs(getColumns(data.metaData.fields));
				        gridOptions.api.setRowData(data.rows);
					}
			    })
			};
			refreshRows();
			  
			var eGridDiv = document.querySelector('#myGrid');
			new agGrid.Grid(eGridDiv, gridOptions);
	
	  </script>
    </body>
</html>