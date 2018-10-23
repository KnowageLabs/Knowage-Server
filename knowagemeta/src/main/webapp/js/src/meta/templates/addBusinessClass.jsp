<md-dialog aria-label="add Business model" layout="column" ng-cloak style="min-width:90%; min-height:90%;height:90%;">
	<form name="newBMForm" layout="column" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>{{translate.load("sbi.meta.new.businessclass")}}</h2>
			</div>
		</md-toolbar>
		<md-dialog-content flex layout="column"> 
		 <div class="md-dialog-content" layout="column" flex>
				<md-input-container   class="md-block" >
					<label>{{translate.load("sbi.meta.table.physical")}}</label>
					<md-select ng-model="tmpBnssModel.physicalModel" ng-model-options="{trackBy:'$value.name'}" ng-change="changePhYModel()">
						<md-option ng-repeat="phTable in physicalModel" ng-value="phTable">
							{{phTable.name}}
						</md-option>
					</md-select>
				</md-input-container>
				<md-input-container    class="md-block"  >
					<label>{{translate.load("sbi.generic.name")}}</label>
					<input ng-model="tmpBnssModel.name" required>
				</md-input-container>				
			
			 <md-input-container class="md-block">
				<label>Description**</label>
				<textarea ng-model="tmpBnssModel.description" ></textarea>
			</md-input-container>
			
			
			<angular-table flex id='newBmodelTableColumn' ng-model="tmpBnssModel.physicalModel.columns"
					columns='bmTableColumns'
				 	show-search-bar=true 
				 	no-pagination="true"
				 	multi-select="true"
				 	selected-item="tmpBnssModel.selectedColumns"
				 	>
		 	 </angular-table>
			</div>
		</md-dialog-content>
		<md-dialog-actions layout="row">
			<span flex></span>
			<md-button ng-click="cancel()">
				{{translate.load("sbi.general.cancel")}}
			</md-button>
			<md-button   ng-click="create()" ng-disabled="!newBMForm.$valid || tmpBnssModel.selectedColumns.length==0">
				{{translate.load("sbi.generic.update")}}
			</md-button>
		</md-dialog-actions>
	</form>
</md-dialog>