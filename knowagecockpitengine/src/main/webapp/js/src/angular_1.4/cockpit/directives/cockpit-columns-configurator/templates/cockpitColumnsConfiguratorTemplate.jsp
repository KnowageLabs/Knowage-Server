<md-content layout-fill>
    <md-card>
     	<md-card-content layout="row" layout-align="space-around center">
     		<dataset-selector flex ng-model=model.dataset.dsId on-change="resetValue(dsId);"></dataset-selector>  	
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
		<md-card-title>
              <md-card-title-text layout="row">
              		
                    <span flex class="md-headline">{{translate.load('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.tablecolumns')}}</span>
                    <span flex></span>

                    <md-button flex="10" class="md-icon-button" ng-click="openListColumn()">{{translate.load('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.addcolumn')}} </md-button>
                    <md-button flex="20" class="md-icon-button" ng-click="addNewCalculatedField()">{{translate.load('sbi.cockpit.widgets.table.calculatedFields.add')}}</md-button>
                     
              </md-card-title-text>
        </md-card-title>
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
           			<md-option ng-repeat="sortingColumn in model.content.columnSelectedOfDataset" ng-value="sortingColumn.aliasToShow">
                			{{sortingColumn.aliasToShow}}
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
		<md-card-content layout="column" layout-align="center none" style="padding-top: 0px; padding-bottom: 0px;">
     		<div layout="row" ng-if="showCircularcolumns.value" layout-sm="column" layout-align="space-around">
      			<md-progress-circular md-mode="indeterminate"></md-progress-circular>
   			 </div>
   			 
   			 <table class="kn-table">
   			 	<thead>
   			 		<tr>
   			 			<th></th>
   			 			<th ng-repeat="col in metadataTableColumns">{{col.label}}</th>
   			 			<th></th>
   			 		</tr>
   			 	</thead>
   			 	<tbody>
   			 		<tr ng-repeat="row in model.content.columnSelectedOfDataset track by $index">
   			 			<td class="multiTableAction">
							<div layout="row" layout-align="center"> 
			                	<md-button ng-click="functionsCockpitColumn.moveUp($event,$index)" class="md-icon-button" aria-label="up" ng-show="$index!=0"> 
		               				<md-icon md-font-icon="fa fa-arrow-up"></md-icon>
		          				</md-button>
		          				<md-button ng-click="functionsCockpitColumn.moveDown($event,$index)" class="md-icon-button" aria-label="down" ng-show="!$last">
		          					<md-icon md-font-icon="fa fa-arrow-down"></md-icon>
		          				</md-button>
	          				</div>
       					</td>
   			 			<td ng-repeat="col in metadataTableColumns" ng-style="{'width':col.width}">
							<md-input-container ng-if="col.type == 'inputtext'" class="noMdError">
						        <input type="text" ng-model="row[col.name]" aria-label="text-input">
						    </md-input-container>

						    <md-select ng-if="col.type == 'select'" ng-model="row[col.name]" ng-if="col.if" aria-label="select-input">
		           				<md-option value=""></md-option>
		           				<md-option ng-repeat="opt in col.values track by $index" value="{{opt.value}}">
		           					{{opt.label}}
		           				</md-option> 
	           				</md-select>
	           				
	           				<md-button class="md-icon-button" ng-click="functionsCockpitColumn.draw(row,col,index)" ng-style="{'background-color':row.style['background-color']}" ng-if="col.type == 'style'">
								<md-tooltip>Column style</md-tooltip>
								<md-icon md-font-icon="fa fa-paint-brush" ng-style="{'color':row.style.color}">
							</md-button>
						</td>
						<td class="tableAction">
							<div layout="row" layout-align="center">
								<md-button ng-repeat="action in actionsOfCockpitColumns" class="md-icon-button" ng-click="action.action(row,$event)">
									<md-tooltip>{{action.label}}</md-tooltip>
									<md-icon md-font-icon="{{action.icon}}">
								</md-button>
							</div>
						</td>
   			 		</tr>
   			 	</tbody>
   			 </table>
   			 
	  		<!--  angular-table flex
		  		ng-show="model.content.columnSelectedOfDataset.length>0 && !showCircularcolumns.value"
				id='metadataTable' ng-model="model.content.columnSelectedOfDataset"
				columns='metadataTableColumns'
				columns-search='["name"]' show-search-bar=true
				no-pagination=true  scope-functions="functionsCockpitColumn"
				speed-menu-option= actionsOfCockpitColumns>
			</angular-table--> 
			 
			
	    	
     	</md-card-content>
    </md-card>
	
</md-content>