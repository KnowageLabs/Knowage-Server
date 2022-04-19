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
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.SingletonConfig"%>
<%@page import="it.eng.spago.base.*"%>

<%
	RequestContainer aRequestContainer = null;
	IUrlBuilder urlBuilder = null;	
	String sbiMode = null;
		
	// case of portlet mode
	aRequestContainer = RequestContainerPortletAccess.getRequestContainer(request);
	if (aRequestContainer == null) {
		// case of web mode
		aRequestContainer = RequestContainer.getRequestContainer();
		if(aRequestContainer == null){
			//case of REST 
			aRequestContainer = RequestContainerAccess.getRequestContainer(request);
		}
	}

	String channelType = aRequestContainer.getChannelType();
	if ("PORTLET".equalsIgnoreCase(channelType)) sbiMode = "PORTLET";
	else sbiMode = "WEB";

	// create url builder 
	urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);
%>
<!DOCTYPE html>
    <head>
    	<link rel="icon" href="data:;base64,iVBORw0KGgo=">
    	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/themes/commons/css/reset_2018.css")%>">
    	<link rel="stylesheet" href="<%= KnowageSystemConfiguration.getKnowageContext() %>/node_modules/ag-grid-community/dist/styles/ag-grid.css">
    	<link rel="stylesheet" href="<%= KnowageSystemConfiguration.getKnowageContext() %>/node_modules/ag-grid-community/dist/styles/ag-theme-balham.css">
    	<link rel="stylesheet" type="text/css"  href="<%= KnowageSystemConfiguration.getKnowageContext() %>/node_modules/toastify-js/src/toastify.css">
    	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/themes/commons/css/customStyle.css")%>">
    	<script src="<%= KnowageSystemConfiguration.getKnowageContext() %>/node_modules/ag-grid-community/dist/ag-grid-community.min.noStyle.js"></script>
    	<!-- POLYFILLS -->
    	<script>if (!Array.isArray) {
		  Array.isArray = function(arg) {
		    return Object.prototype.toString.call(arg) === '[object Array]';
		  };
		}</script>
    	<script src="<%= KnowageSystemConfiguration.getKnowageContext() %>/polyfills/promise-polyfill/promise-polyfill.js"></script>
    	<script src="<%= KnowageSystemConfiguration.getKnowageContext() %>/polyfills/fetch-polyfill/fetch-polyfill.js"></script>
    	<script src="<%= KnowageSystemConfiguration.getKnowageContext() %>/polyfills/url-polyfill/url-polyfill.min.js"></script>
    	<style>
    		html, body {height: 100%;}
    	</style>
    </head>
    
    <body>
    	<div id="utility-bar" class="utility-bar hidden"></div>
    	
        <div id="myGrid" class="ag-theme-balham kn-preview-table-theme"></div>
        
		<script type="text/javascript" charset="utf-8">
			//GLOBAL VARIABLES 
			const 	MAX_ROWS_CLIENT_PAGINATION = <%= SingletonConfig.getInstance().getConfigValue("dataset.preview.clientpagination.maxrows") %>;
			const 	SEARCH_WAIT_TIMEOUT = 500;
			const 	DEFAULT_MAX_ITEMS_PER_PAGE = 15;
			const 	KNOWAGE_BASEURL = '<%= KnowageSystemConfiguration.getKnowageContext() %>';
			const 	KNOWAGE_SERVICESURL = '/restful-services';
			var 	FIELDS;
			var		DATASET;
			var 	backEndPagination = {page: 1, itemsPerPage: DEFAULT_MAX_ITEMS_PER_PAGE};
			var 	filters = [];
			//Getting the url parameters
	  		var url = new URL(window.location.href);
	  		var datasetLabel = url.searchParams.get("datasetLabel");
	  		var parameters = url.searchParams.getAll("parameters");	  		
	  		var drivers = url.searchParams.get("drivers");
	  		var options = JSON.parse(url.searchParams.get("options")) || {};
	  		
	  		var exporterBarShown = false;	  		
	  		function showExportersBar(){
	  			if(options && options['exports'] && !exporterBarShown) {
		  			document.getElementById('utility-bar').classList.remove("hidden");
		  			document.getElementById('myGrid').classList.add("has-utility-bar");
		  			for(var e in options['exports']){
		  				document.getElementById('utility-bar').innerHTML += '<button class="kn-button" id="export-'+options['exports'][e].toUpperCase()+'" onclick="exportDataset(\''+options['exports'][e].toUpperCase()+'\')">Export '+options['exports'][e].toUpperCase()+'</button>'
		  			}
		  			exporterBarShown = true;
		  		}
	  		}
	  		
	  		//Utility methods
	  		function isEmpty(obj) {
	  		    for(var key in obj) {
	  		        if(obj.hasOwnProperty(key)) return false;
	  		    }return true;
	  		}
	  		
	  		//Function to create the colDefs for ag-grid
		  	function getColumns(fields) {
	  			if(!FIELDS) FIELDS = fields;
				var columns = [];
				for(var f in fields){
					if(typeof fields[f] != 'object') continue;
					var tempCol = {"headerName":fields[f].header,"field":fields[f].name, "tooltipField":fields[f].name};
					tempCol.headerComponentParams = {template: headerTemplate(fields[f].type)};
					columns.push(tempCol);
				}
				return columns;
			}
		
		  	//Defining ag-grid options
		  	var gridOptions = {
			    enableSorting: true,
			    enableFilter: false,
			    pagination: options && typeof options.pagination != 'undefined' ? options.pagination : true,
			    suppressDragLeaveHidesColumns : true,
			    enableColResize: true,
	            //paginationAutoPageSize: true,
	            headerHeight: 48,
	            onSortChanged: changeSorting,
	            noRowsOverlayComponent: CustomErrorOverlay,
	            defaultColDef: {
	                filter: customFilter
	            },
	        };
		  	
			options.backEndPagination = true;
		  	
		  	//Defining custom column filter
		  	function customFilter(){}

		  	customFilter.prototype.init = function (params) {
		  	    this.valueGetter = params.valueGetter;
		  	    this.filterText = null;
		  	    this.setupGui(params);
		  	};

		  	customFilter.prototype.setupGui = function (params) {
		  	    this.gui = document.createElement('div');
		  	    this.gui.innerHTML =
		  	        '<div style="padding: 4px; width: 200px;">' +
		  	        '	<div><input style="margin: 4px 0px 4px 0px;width:192px" type="text" id="filterText" /></div>' +
		  	        '</div>';

		  	    this.eFilterText = this.gui.querySelector('#filterText');
		  	    this.eFilterText.addEventListener("changed", listener);
		  	    this.eFilterText.addEventListener("paste", listener);
		  	    this.eFilterText.addEventListener("input", listener);
		  	    this.eFilterText.addEventListener("keydown", listener);
		  	    this.eFilterText.addEventListener("keyup", listener);
				this.col = params.colDef.headerName;
				
		  	    var that = this;
		  	  	var timeout = null;
		  	    function listener(event) {
		  	        that.filterText = event.target.value;
		  	        if(options.backEndPagination){
		  	        	clearTimeout(timeout);
			  	     	
				  	    timeout = setTimeout(function () {
				  	    	var currentIndex;
				  	    	for(var k in filters){
				  	    		if(filters[k].column == that.col){
				  	    			currentIndex = k;
				  	    			break;
				  	    		}
				  	    	}
				  	    	if(currentIndex){
				  	    		if(that.filterText == '') filters.splice(currentIndex,1);
				  	    		else filters[currentIndex].value = that.filterText;
				  	    	}else filters.push({'column': that.col, 'value':that.filterText})
				  	        getData();
				  	    }, SEARCH_WAIT_TIMEOUT);
		  	        }else{
		  	        	params.filterChangedCallback();
		  	        }
		  	    }
		  	};

		  	customFilter.prototype.getGui = function () {
		  	    return this.gui;
		  	};

		  	customFilter.prototype.isFilterActive = function () {
		  		return this.filterText !== null && this.filterText !== undefined && this.filterText !== '';
		  	};
		  	
		  	customFilter.prototype.getModel = function() {
		  	    var model = {value: this.filterText.value};
		  	    return model;
		  	};

		  	customFilter.prototype.doesFilterPass = function (params) {
		  	    // make sure each word passes separately, ie search for firstname, lastname
		  	    var passed = true;
		  	    var valueGetter = this.valueGetter;
		  	    this.filterText.toLowerCase().split(" ").forEach(function(filterWord) {
		  	        var value = valueGetter(params);
		  	        if (value.toString().toLowerCase().indexOf(filterWord)<0) {
		  	            passed = false;
		  	        }
		  	    });

		  	    return passed;
		  	};
		  	
		  	customFilter.prototype.setModel = function(model) {
		  	    this.eFilterText.value = model.value;
		  	};
		
		  	//Defining custom sorting for backend 
			function changeSorting(){
				if(options.backEndPagination){
					var sorting = gridOptions.api.getSortModel();
					backEndPagination.sorting = {
							'column' : sorting.length>0 ? getColumnName(sorting[0].colId) : '',
							'order' : sorting.length>0 ? sorting[0]['sort'] : ''};
					getData();
				}
			}
			
			function getColumnName(colNum){
				for(var k in FIELDS){
					if(FIELDS[k].dataIndex && FIELDS[k].dataIndex == colNum) return FIELDS[k].header;
				}
			}
		  
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
			            
			            '	<div ref="btFirst" class="ag-paging-button-wrapper '+disableFirstClass()+'"><span class="ag-icon ag-icon-first" unselectable="on"></span>'+
		       			'		<button type="button" class="ag-paging-button" ref="btFirst" '+disableFirst()+' onclick="first()">First</button>'+
		      			'	</div>'+
		      			'	<div ref="btPrevious" class="ag-paging-button-wrapper '+disableFirstClass()+'"><span class="ag-icon ag-icon-previous" unselectable="on"></span>'+
		       			'		<button type="button" class="ag-paging-button" ref="btPrevious" '+disableFirst()+' onclick="prev()">Previous</button>'+
		       			'	</div>'+
		       			'page <span ref="lbCurrent">'+backEndPagination.page+'</span> of <span ref="lbTotal">'+backEndPagination.totalPages+'</span>'+
		       			'	<div ref="btNext" class="ag-paging-button-wrapper '+disableLastClass()+'" ><span class="ag-icon ag-icon-next" unselectable="on"></span>'+
		       			'   	<button type="button" class="ag-paging-button" ref="btNext" onclick="next()" '+disableLast()+'">Next</button>'+
		       			'	</div>'+
		       			'	<div ref="btLast" class="ag-paging-button-wrapper '+disableLastClass()+'" ><span class="ag-icon ag-icon-last" unselectable="on"></span>'+
		       			'   	<button type="button" class="ag-paging-button" ref="btLast" onclick="last()" '+disableLast()+'">Last</button>'+
		       			'	</div>'+
						'</span>';
		  	}
		  	
		  	function CustomErrorOverlay () {}

		  	var errors;
		  	CustomErrorOverlay.prototype.init = function(params) {
		  	    this.eGui = document.createElement('div');
		  	    var errorMessage = '<i class="far fa-frown"> No Rows Available </i>'
		  	    if(errors && errors.length > 0) errorMessage = '<i class="far fa-frown"> WARNING: '+ errors +' </i>';
		  	    this.eGui.innerHTML = '<div class="ag-overlay-loading-center">' + errorMessage + '</div>';
		  	};

		  	CustomErrorOverlay.prototype.getGui = function() {
		  	    return this.eGui;
		  	};
		  	
		  	function maxPageNumber(){
				if(backEndPagination.page*backEndPagination.itemsPerPage < backEndPagination.totalRows) return backEndPagination.page*backEndPagination.itemsPerPage;
				else return backEndPagination.totalRows;
		  	}
		  	
		  	function disableFirst(){
		  		return backEndPagination.page == 1 ? "disabled=\"disabled\"" : "";
		  	}
		  	
		  	function disableFirstClass(){
		  		return backEndPagination.page == 1 ? "ag-disabled" : "";
		  	}
		  	
		  	function disableLast(){
		  		return backEndPagination.page == backEndPagination.totalPages ? "disabled=\"disabled\"" : "";
		  	}
		  	
		  	function disableLastClass(){
		  		return backEndPagination.page == backEndPagination.totalPages ? "ag-disabled" : "";
		  	}
		  	
		  	function first(){
		  		backEndPagination.page = 1;
		  		getData();
			}
			
		  	function prev(){
		  		if(backEndPagination.page == 1) return;
		  		backEndPagination.page = backEndPagination.page - 1;
		  		getData();
			}
			
		  	function next(){
		  		backEndPagination.page = backEndPagination.page + 1;
		  		getData();
			}
			
		  	function last(){
		  		backEndPagination.page = backEndPagination.totalPages;
		  		getData();
			}
			
		  	//Defining the service call to datastore
		  	function getData(init){
				var fetchParams = {method:"POST",body:{}};
				if(init){
					if(!options.backEndPagination) {
						fetchParams.body.start = 0;
						fetchParams.body.limit = -1;
					}
				}
				if(!init) gridOptions.api.showLoadingOverlay();
				if(options.backEndPagination){
					fetchParams.body.start = (backEndPagination.page-1) * backEndPagination.itemsPerPage;
					fetchParams.body.limit = backEndPagination.itemsPerPage;
					if(filters.length > 0) fetchParams.body.filters = filters;
					if(backEndPagination.sorting) fetchParams.body.sorting = backEndPagination.sorting;
				}
				if(!isEmpty(parameters)){
					for (var i = 0; i < parameters.length; i++) {
						if(typeof parameters[i] == 'string') parameters[i] = JSON.parse(parameters[i]);
					}
					fetchParams.body.pars = parameters;
				}				
				if(!isEmpty(drivers)){
					var parsedDrivers = JSON.parse(drivers);
					fetchParams.body.drivers = parsedDrivers;
				}
				fetchParams.body = JSON.stringify(fetchParams.body);
				window.fetch(KNOWAGE_BASEURL +  KNOWAGE_SERVICESURL + '/2.0/datasets/' + datasetLabel + '/preview', fetchParams)
				.then(function(response) {return response.json()})
				.then(function(data){
					showExportersBar();
					if(data.errors){
						errors = data.errors[0].message;
						gridOptions.api.showNoRowsOverlay();
					}else{
						backEndPagination.totalRows = data.results;
						gridOptions.api.setColumnDefs(getColumns(data.metaData.fields));
						if(options.backEndPagination){
							gridOptions.api.setRowData(data.rows);
							//backEndPagination.itemsPerPage = gridOptions.api.getLastDisplayedRow()+1;
							backEndPagination.totalPages = Math.ceil(backEndPagination.totalRows/backEndPagination.itemsPerPage) || 0;
							document.getElementsByClassName('ag-paging-panel')[0].innerHTML = paginationTemplate();
						}else{
							
							gridOptions.api.setRowData(data.rows);
							if(data.results > (MAX_ROWS_CLIENT_PAGINATION || 500)) {
								gridOptions.pagination = false;
								options.backEndPagination = true;
								backEndPagination.itemsPerPage = gridOptions.api.getLastDisplayedRow()+1;
								backEndPagination.totalPages = Math.ceil(backEndPagination.totalRows/backEndPagination.itemsPerPage) || 0;
								document.getElementsByClassName('ag-paging-panel')[0].innerHTML = paginationTemplate();
							}
						}
						gridOptions.api.hideOverlay();
					}
					
				})
			}

	  		getData(true);
	  		
	  		if(options.exports){
				window.fetch(KNOWAGE_BASEURL +  KNOWAGE_SERVICESURL + '/2.0/datasets?asPagedList=true&seeTechnical=true&label=' + datasetLabel)
				.then(function(response) {return response.json()})
				.then(function(data){
					DATASET = data.item[0];
				})
	  		}
			
			function exportDataset(format){
				var body = {};
				Toastify({
					text: "The download has started in background. You will find the result file in your download page.",
					duration: 10000,
					close: true,
					className: 'kn-infoToast',
					stopOnFocus: true
				}).showToast();
				
				if(parameters && parameters.length > 0) body.parameters = parameters;
				
				if(!isEmpty(drivers)){
					var parsedDrivers = JSON.parse(drivers);
					body.drivers = parsedDrivers;
				}			
		
				var exportFormat = null;
				if (format == 'CSV') {
					exportFormat = '/csv';
				} else if (format == 'XLSX') {
					exportFormat = '/xls';
				}
				
				window.fetch(KNOWAGE_BASEURL +  KNOWAGE_SERVICESURL + '/2.0/export/dataset/' + DATASET.id.dsId + exportFormat, {
					method: "POST",
					headers: {
						"Content-Type": "application/json"
					},
					body: JSON.stringify(body)
				}).then(function(result) {
						if(result.errors){
							Toastify({
								text: result.errors[0].message,
								duration: 10000,
								close: true,
								className: 'kn-warningToast',
								stopOnFocus: true
							}).showToast();
						}
					})
			}

			var eGridDiv = document.querySelector('#myGrid');
			new agGrid.Grid(eGridDiv, gridOptions);
	
	  </script>
	  <script type="text/javascript" src="<%= KnowageSystemConfiguration.getKnowageContext() %>/node_modules/toastify-js/src/toastify.js"></script>
    </body>
</html>
