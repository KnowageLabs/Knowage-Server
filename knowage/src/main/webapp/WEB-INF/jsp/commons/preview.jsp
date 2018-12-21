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
    	<link rel="icon" href="data:;base64,iVBORw0KGgo=">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/reset_2018.css">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/styles/ag-grid.css">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/styles/ag-theme-balham.css">
    	<link rel="stylesheet" href="<%= GeneralUtilities.getSpagoBiContext() %>/themes/commons/css/customStyle.css">
    	<script src="<%= GeneralUtilities.getSpagoBiContext() %>/node_modules/ag-grid/dist/ag-grid.min.noStyle.js"></script>
    	<!-- POLYFILLS -->
    	<script src="<%= GeneralUtilities.getSpagoBiContext() %>/polyfills/url-polyfill/url-polyfill.min.js"></script>
    	<style>
    		html, body {height: 100%;}
    	</style>
    </head>
    
    <body>
    	<div id="utility-bar" class="utility-bar hidden"></div>
    	
        <div id="myGrid" class="ag-theme-balham kn-preview-table-theme"></div>
        
		<script type="text/javascript" charset="utf-8">
			//GLOBAL VARIABLES 
			const MAX_ROWS_CLIENT_PAGINATION = 5000;
			const KNOWAGE_BASEURL = '<%= GeneralUtilities.getSpagoBiContext() %>';
			const KNOWAGE_SERVICESURL = '/restful-services';
			var backEndPagination = {page: 1};
	  
			//Getting the url parameters
	  		var url = new URL(window.location.href);
	  		var datasetLabel = url.searchParams.get("dataset");
	  		var parameters = JSON.parse(url.searchParams.get("parameters")) || {};
	  		var options = JSON.parse(url.searchParams.get("options")) || {};
	  		if(options && options['exports']) {
	  			document.getElementById('utility-bar').classList.remove("hidden");
	  			document.getElementById('myGrid').classList.add("has-utility-bar");
	  			for(var e in options['exports']){
	  				document.getElementById('utility-bar').innerHTML += '<button class="kn-button" onclick="exportDataset(\''+options['exports'][e].toUpperCase()+'\')">Export '+options['exports'][e].toUpperCase()+'</button>'
	  			}
	  		}
	  		var datasetId;
	    	
	  		//Function to create the colDefs for ag-grid
		  	function getColumns(fields) {
				var columns = [];
				for(var f in fields){
					if(typeof fields[f] != 'object') continue;
					var tempCol = {"headerName":fields[f].header,"field":fields[f].name, "tooltipField":fields[f].name};
					tempCol.headerComponentParams = {template: headerTemplate(fields[f].type)};
					columns.push(tempCol);
				}
				return columns
			}
		  	
		  	function changeSorting(){
		  		if(options.backEndPagination){
		  			var sorting = gridOptions.api.getSortModel();
		  		}
			}
		
		  	//Defining ag-grid options
		  	var gridOptions = {
			    enableSorting: options && typeof options.sorting != 'undefined' ? options.sorting : true,
			    enableFilter: false,
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
		  	
		  	//Pagination template and utility methods
		  	function paginationTemplate(){
		  		return 	'<span ref="eSummaryPanel" class="ag-paging-row-summary-panel">'+
			            '	<span ref="lbFirstRowOnPage">'+((backEndPagination.page-1)*backEndPagination.itemsPerPage+1)+'</span> to <span ref="lbLastRowOnPage">'+maxPageNumber()+'</span> of <span ref="lbRecordCount">'+backEndPagination.totalRows+'</span>'+
			            '</span>'+
			            '<span class="ag-paging-page-summary-panel">'+
			            '	<button type="button" class="ag-paging-button" ref="btFirst" '+disableFirst()+' onclick="first()">First</button>'+
			            '   <button type="button" class="ag-paging-button" ref="btPrevious" '+disableFirst()+' onclick="prev()">Previous</button>'+
			            '   page <span ref="lbCurrent">'+backEndPagination.page+'</span> of <span ref="lbTotal">'+backEndPagination.totalPages+'</span>'+
			            '   <button type="button" class="ag-paging-button" ref="btNext" onclick="next()" '+disableLast()+'">Next</button>'+
			            '   <button type="button" class="ag-paging-button" ref="btLast" '+disableLast()+' onclick="last()">Last</button>'+
			            '</span>';
		  	}
		  	
		  	function maxPageNumber(){
				if(backEndPagination.page*backEndPagination.itemsPerPage < backEndPagination.totalRows) return backEndPagination.page*backEndPagination.itemsPerPage;
				else return backEndPagination.totalRows;
		  	}
		  	
		  	function disableFirst(){
		  		return backEndPagination.page == 1 ? "disabled=\"disabled\"" : "";
		  	}
		  	
		  	function disableLast(){
		  		return backEndPagination.page == backEndPagination.totalPages ? "disabled=\"disabled\"" : "";
		  	}
		  	
		  	function first(){
		  		backEndPagination.page = 1;
		  		refreshRows();
			}
			
		  	function prev(){
		  		if(backEndPagination.page == 1) return;
		  		backEndPagination.page = backEndPagination.page - 1;
		  		refreshRows();
			}
			
		  	function next(){
		  		backEndPagination.page = backEndPagination.page + 1;
		  		refreshRows();
			}
			
		  	function last(){
		  		backEndPagination.page = backEndPagination.totalPages;
		  		refreshRows();
			}
		  	
		  	/*function getParameters(){
		  		var tempParams = {};
		  		for(var i in dataset.parameters){
		  			if(parameters[dataset.parameters[i].name]) tempParams[dataset.parameters[i].name] = parameters[dataset.parameters[i].name];
		  			else tempParams[dataset.parameters[i].name] = dataset.parameters[i].defaultValue;
		  		}
		  		return {parameters : tempParams};
		  	}*/
		  	
		  	/*function getDatasetMetadata(){
				fetch(KNOWAGE_BASEURL +  KNOWAGE_SERVICESURL + '/2.0/datasets/?seeTechnical=true&label='+datasetLabel)
				.then(function(response) {return response.json()})
				.then(function(data){
					dataset = data[4];
					refreshRows(true);
				})
			}
			getDatasetMetadata();*/
		  	
			function refreshRows(init) {
				if(!init) gridOptions.api.showLoadingOverlay();
				var fetchParams = {method:"POST"}
				var body = parameters;
				body.limit = -1;
				if(options.backEndPagination) {
					body.start = (backEndPagination.page-1)*backEndPagination.itemsPerPage;
					body.limit = backEndPagination.itemsPerPage;
				}
				//if(dataset.parameters.length > 0) fetchParams.body = JSON.stringify(getParameters());
				fetchParams.body = JSON.stringify(body);
	  			fetch(KNOWAGE_BASEURL + KNOWAGE_SERVICESURL + '/1.0/datasets/preview',fetchParams)
				.then(function(response) {return response.json()})
				.then(function(data){
					if(data.errors){
						gridOptions.api.showNoRowsOverlay();
					}else{
						if(options.backEndPagination){
							document.getElementsByClassName('ag-paging-panel')[0].innerHTML = paginationTemplate();
							gridOptions.api.setRowData(data.rows);
						}else{
							if(!gridOptions.columnDefs) gridOptions.api.setColumnDefs(getColumns(data.metaData.fields));
							gridOptions.api.setRowData(data.rows);
							if(data.results > MAX_ROWS_CLIENT_PAGINATION) {
								gridOptions.pagination = false;
								options.backEndPagination = true;
								backEndPagination.totalRows = data.results;
								backEndPagination.itemsPerPage = gridOptions.api.getLastDisplayedRow()+1;
								backEndPagination.totalPages = Math.ceil(backEndPagination.totalRows/backEndPagination.itemsPerPage);
								document.getElementsByClassName('ag-paging-panel')[0].innerHTML = paginationTemplate();
							}
						}
						gridOptions.api.hideOverlay();
					}
			    })
			};
			refreshRows(true);
			
			function exportDataset(format){
		       	if(format == 'CSV') {
		       		var url = KNOWAGE_BASEURL +  KNOWAGE_SERVICESURL + '/1.0/datasets/'+dataset.id+'/export';
		       	} else if (format == 'XLSX') {
		       		var url= KNOWAGE_BASEURL + '/servlet/AdapterHTTP?ACTION_NAME=EXPORT_EXCEL_DATASET_ACTION&SBI_EXECUTION_ID=-1&LIGHT_NAVIGATOR_DISABLED=TRUE&id='+dataset.id;
		       	}
		       	window.location.href = url;
		    }
			  
			var eGridDiv = document.querySelector('#myGrid');
			new agGrid.Grid(eGridDiv, gridOptions);
	
	  </script>
    </body>
</html>