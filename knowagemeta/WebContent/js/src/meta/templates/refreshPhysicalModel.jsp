<md-dialog aria-label="refresh physical model" layout="column" ng-cloak style="min-width:90%; min-height:90%;height:90%;">
	<form name="refreshPMForm" layout="column" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>{{translate.load("sbi.meta.update.physicalModel")}}</h2>
			</div>
		</md-toolbar>
		<md-dialog-content flex layout="column"> 
			<div class="md-dialog-content" flex layout="column">
			
				<div layout="column" ng-if="steps.current==0">
					<angular-table  flex id='missingColumnsTable' ng-model="changedItem.missingColumns"
							columns='[{label:changedItem.missingColumns.length>0 ? translate.load("sbi.meta.column.add") : translate.load("sbi.meta.column.add.none"),name:"name"}]'
						 	show-search-bar=false 
						 	no-pagination="true"  
						 	sortable-column="[]"
						 	>
				 	 </angular-table>
				 	 <md-divider ></md-divider>
				 	  
					<angular-table  flex id='removingItemsTable' ng-model="changedItem.removingItems"
							columns='[{label:changedItem.removingItems.length>0 ? translate.load("sbi.meta.element.deleted") : translate.load("sbi.meta.element.deleted.none"),name:"name"}]'
						 	show-search-bar=false 
						 	no-pagination="true" 
						 	sortable-column="[]"
						 	>
				 	 </angular-table>
				</div>
			 	 
			 	<div layout="column" ng-if="steps.current==1">  
					<angular-table flex id='alteredTable' ng-model="changedItem.missingTables"
							columns='[{label:changedItem.missingTables.length>0 ? translate.load("sbi.meta.table.new") : translate.load("sbi.meta.table.new.none"),name:"name"}]'
						 	show-search-bar=true 
						 	no-pagination="true"
						 	multi-select="true"
						 	selected-item="updateObj.selectedtable"
						 	>
				 	 </angular-table>
			 	 </div>
			 	 
			 	
			</div>
		</md-dialog-content>
		<md-dialog-actions layout="row">
			<span flex></span>
			<md-button ng-click="cancel()">
				{{translate.load("sbi.general.cancel")}}
			</md-button>
			<md-button  ng-if="steps.current==0" ng-click="steps.current=1" >
				{{translate.load("sbi.generic.next")}}
			</md-button>
			<md-button  ng-if="steps.current==1" ng-click="saveChange()" >
				{{translate.load("sbi.generic.update")}}
			</md-button>
		</md-dialog-actions>
	</form>
</md-dialog>