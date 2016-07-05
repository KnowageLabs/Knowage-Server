<md-dialog aria-label="refresh physical model" ng-cloak style="min-width:90%; min-height:90%;">
	<form name="refreshPMForm" layout="column" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>{{translate.load("sbi.meta.update.physicalModel")}}</h2>
			</div>
		</md-toolbar>
		<md-dialog-content flex   > 
			<div class="md-dialog-content" flex layout="column">
				<p ng-hide="changedItem.missingColumns.length>0">No new column will be added </p>
				<p ng-show="changedItem.missingColumns.length>0">new columns that will be added </p>
				<angular-table ng-show="changedItem.missingColumns.length>0" flex id='missingColumnsTable' ng-model="changedItem.missingColumns"
						columns='[{label:"name",name:"name"}]'
					 	show-search-bar=false 
					 	no-pagination="true"  
					 	>
			 	 </angular-table>
			 	 <md-divider ></md-divider>
			 	 
			 	 <p ng-hide="changedItem.missingTables.length>0">No new table to add </p>
				<p ng-show="changedItem.missingTables.length>0">New Physical tables to import </p>
				<angular-table ng-show="changedItem.missingTables.length>0" flex id='alteredTable' ng-model="changedItem.missingTables"
						columns='[{label:"name",name:"name"}]'
					 	show-search-bar=true 
					 	no-pagination="true"
					 	multi-select="true"
					 	selected-item="updateObj.selectedtable"
					 	>
			 	 </angular-table>
			 	 <md-divider ></md-divider>
			 	 
			 	<p ng-hide="changedItem.removingItem.length>0">No elements deleted </p>
				<p ng-show="changedItem.removingItem.length>0">Elements that will be marked as deleted </p>
				<angular-table ng-show="changedItem.removingItems.length>0" flex id='removingItemsTable' ng-model="changedItem.removingItems"
						columns='[{label:"name",name:"name"}]'
					 	show-search-bar=false 
					 	no-pagination="true" 
					 	>
			 	 </angular-table>
			</div>
		</md-dialog-content>
		<md-dialog-actions layout="row">
			<span flex></span>
			<md-button ng-click="cancel()">
				{{translate.load("sbi.general.cancel")}}
			</md-button>
			<md-button   ng-click="saveChange()" ng-if="changedItem.missingColumns.length>0 || changedItem.missingTables.length>0 || changedItem.removingItem.length>0">
				{{translate.load("sbi.generic.update")}}
			</md-button>
		</md-dialog-actions>
	</form>
</md-dialog>