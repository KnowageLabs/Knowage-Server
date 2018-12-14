<!DOCTYPE html>
    <head>
    	<link rel="stylesheet" href="http://localhost:8080/knowage/themes/commons/css/reset_2018.css">
        <script src="http://localhost:8080/knowage/node_modules/ag-grid/dist/ag-grid.min.noStyle.js"></script>
    	<link rel="stylesheet" href="http://localhost:8080/knowage/node_modules/ag-grid/dist/styles/ag-grid.css">
    	<link rel="stylesheet" href="http://localhost:8080/knowage/node_modules/ag-grid/dist/styles/ag-theme-balham.css">
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
			.kn-preview-table-theme .ag-header-cell::after, .kn-preview-table-theme .ag-header-group-cell::after {
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
	  
	  	var body = {aggregations:{"measures":[{"id":"gross_weight","alias":"gross_weight","columnName":"gross_weight","orderType":"","funct":"NONE","orderColumn":"gross_weight"},{"id":"units_per_case","alias":"units_per_case","columnName":"units_per_case","orderType":"","funct":"NONE","orderColumn":"units_per_case"},{"id":"SRP","alias":"SRP","columnName":"SRP","orderType":"","funct":"NONE","orderColumn":"SRP"}],"categories":[{"id":"product_name","alias":"product_name","columnName":"product_name","orderType":"","funct":"NONE"},{"id":"low_fat","alias":"low_fat","columnName":"low_fat","orderType":"","funct":"NONE"},{"id":"the_date","alias":"the_date","columnName":"the_date","orderType":"","funct":"NONE"},{"id":"DATE(the_date)","alias":"DATE(the_date)","columnName":"DATE(the_date)","orderType":"","funct":"NONE"}],"dataset":"multiTypeDataset"},parameters:{},selections:{}};
	 
		
		  function getColumns(fields) {
				var columns = [];
				for(var f in fields){
					if(typeof fields[f] != 'object') continue;
					var tempCol = {"headerName":fields[f].header,"field":fields[f].name, "tooltipField":fields[f].header};
					tempCol.headerComponentParams = {template: headerTemplate(fields[f].type)};
					columns.push(tempCol);
				}
				return columns
			}
		
		  // let the grid know which columns and what data to use
		  var gridOptions = {
		    enableSorting: true,
		    enableFilter: true,
		    pagination: true,
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
		    // specify the data
		    fetch('http://localhost:8080/knowage/restful-services/2.0/datasets/multiTypeDataset/data',{
				  method: "POST",
				  body: JSON.stringify(body)
				}).then(function(response) {return response.json()})
				.then(function(data){
					gridOptions.api.setColumnDefs(getColumns(data.metaData.fields));
			        gridOptions.api.setRowData(data.rows);
		      })
		    
		
		  // lookup the container we want the Grid to use
		  var eGridDiv = document.querySelector('#myGrid');
		
		  // create the grid passing in the div to use together with the columns & data we want to use
		  new agGrid.Grid(eGridDiv, gridOptions);
	
	  </script>
    </body>
</html>