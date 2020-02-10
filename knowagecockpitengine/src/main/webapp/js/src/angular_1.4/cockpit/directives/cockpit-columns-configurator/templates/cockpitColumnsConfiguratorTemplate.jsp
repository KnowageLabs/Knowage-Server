<md-content layout-fill>
    <md-card>
     	<md-card-content layout="row" layout-align="space-around center">
     		<dataset-selector flex ng-model=model.dataset.dsId on-change="resetValue(dsId);" dataset-type-exclusion="[{type:'SbiSolrDataSet'}]"></dataset-selector>
		   	<md-input-container flex class="md-block"> 
				<md-switch ng-model="model.settings.pagination.enabled" aria-label="Fixed rows per page" layout-align="center center">
					 {{translate.load('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.fixedrowsperpage')}}
				</md-switch>
		     </md-input-container>
			<md-input-container flex class="md-block"> 
				<label>{{translate.load('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.maxrowsnumber')}}</label>
					<input class="input_class" ng-model="model.settings.pagination.itemsNumber" type="number" min="1" ng-disabled="!model.settings.pagination.enabled">
			</md-input-container>
			<md-input-container flex class="md-block"> 
				<md-switch ng-model="model.settings.pagination.frontEnd" aria-label="Frontend pagination" layout-align="center center"
						ng-disabled="!model.settings.pagination.enabled || (local && local.isRealtime)">
					 {{translate.load('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.frontendpagination')}}
				</md-switch>
		     </md-input-container>
		</md-card-content>
	</md-card>
	<md-card>
		<md-card-content class="noPadding" layout="column" layout-align="center none" style="height:auto">
		
			<md-subheader class="noPadding" style="margin-bottom:8px;">
				<div layout="row" layout-align="start center" style="height:48px; padding-left:8px">
					<span>{{translate.load('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.tablecolumns')}}</span>
					<span flex></span>
					<md-button ng-click="openListColumn()">{{translate.load('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.addcolumn')}} </md-button>
	                <md-button ng-click="addNewCalculatedField()">{{translate.load('sbi.cockpit.widgets.table.calculatedFields.add')}}</md-button>
                </div>
			</md-subheader>
			<div layout="row" layout-margin ng-show="!showCircularcolumns.value">
		        <md-input-container flex="40" class="md-block" ng-show="model.content.columnSelectedOfDataset.length>0">
	      			<label>{{translate.load("sbi.cockpit.widgets.table.modalselectioncolumn");}}</label>
	         		<md-select md-option-empty ng-model="model.settings.modalSelectionColumn">
	         			<md-option md-option-empty ng-value=""></md-option>
	          			<md-option ng-repeat="modalcolumn in model.content.columnSelectedOfDataset" ng-value="modalcolumn.aliasToShow">
	               			{{modalcolumn.aliasToShow}}
	          			</md-option>
	      			</md-select>
			    </md-input-container>
			    <md-input-container flex="40" class="md-block">
	       			<label>{{translate.load("sbi.cockpit.widgets.table.sorting.column");}}</label>
	          		<md-select ng-model="model.settings.sortingColumn" multiple="false">
	          			<md-option></md-option>
	           			<md-option ng-repeat="sortingColumn in model.content.columnSelectedOfDataset" ng-value="sortingColumn.name">
	                			{{sortingColumn.name}}
	           			</md-option>
	       			</md-select>
			    </md-input-container>
			    <md-input-container flex="20" class="md-block">
	       			<label>{{translate.load("sbi.cockpit.widgets.table.sorting.order");}}</label>
	          		<md-select ng-model="model.settings.sortingOrder" ng-disabled="!model.settings.sortingColumn || model.settings.sortingColumn==''">
	           			<md-option value="ASC">{{translate.load("sbi.cockpit.widgets.table.sorting.asc");}}</md-option>
	          			<md-option value="DESC">{{translate.load("sbi.cockpit.widgets.table.sorting.desc");}}</md-option>
	       			</md-select>
			    </md-input-container>
			</div>
     		<div layout="row" ng-if="showCircularcolumns.value" layout-sm="column" layout-align="space-around">
      			<md-progress-circular md-mode="indeterminate"></md-progress-circular>
   			 </div>
   			 <div ng-show="model.content.columnSelectedOfDataset.length>0" ag-grid="columnsGrid" class="ag-theme-balham ag-theme-knowage ag-theme-knowage-secondary" style="padding:8px"></div>
     	</md-card-content>
    </md-card>
	
</md-content>