<md-dialog aria-label="add Business model" ng-cloak style="min-width:90%; min-height:90%;">
	<form name="newBVForm" layout="column" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>{{translate.load("sbi.meta.new.businessview")}}</h2>
			</div>
		</md-toolbar>
		<md-dialog-content flex > 
			<div class="md-dialog-content" layout="column">
				
				<expander-box title="translate.load('sbi.meta.model.physical')" expanded="true">
					<angular-table flex id='newBViewTableColumn' ng-model="physicalModel"
					columns='bvTableColumns'
				 	show-search-bar=true 
				 	no-pagination="true"
				 	multi-select="true"
				 	selected-item="tmpBnssView.physicalModels"
				 	></angular-table>
				</expander-box>
				
				<md-content layout="column" layout-padding>
					<div layout="row" >
						<md-input-container flex>
							<label>{{translate.load("sbi.meta.business.relationship.source.table")}}</label>
							<md-select ng-model="sourceTable" ng-model-options="{trackBy: '$value.name'}" >
								<md-option ng-repeat="colu in tmpBnssView.physicalModels"  ng-value="colu"  >
								{{colu.name}}
								</md-option>
							</md-select>
						</md-input-container>						

						<md-input-container flex>
							<label>{{translate.load("sbi.meta.business.relationship.target.table")}}</label>
							<md-select ng-model="targetTable" ng-model-options="{trackBy: '$value.name'}" >
								<md-option ng-repeat="colu in tmpBnssView.physicalModels"  ng-value="colu"  >
								{{colu.name}}
								</md-option>
							</md-select>
						</md-input-container>						
					</div>
					<associator-directive flex 
						source-model="sourceTable.columns"
						target-model="targetTable.columns"  
						source-name="name" 
						target-name="name" 
						associated-item="links" 
						source-column-label="translate.load('sbi.meta.business.relationship.source.attributes')"
						target-column-label="translate.load('sbi.meta.business.relationship.target.attributes')"
						drag-options="dragOptionsFunct"
						after-delete-association=afterClearItem(item)
						>
					</associator-directive>
				</md-content>
				
				
				
				
				<expander-box title="Riepilogo" expanded="true">
				 
					<md-list class="md-dense" flex >
        				<md-list-item     ng-repeat="item in summary"  ng-click="null" layout="row">
        					<span flex=40>{{item.source}}</span>
	        				<span flex  ><i class="fa fa-link" aria-hidden="true"></i></span>
	        				<span flex=40>{{item.target}}</span>
	        				 <md-button   class="md-secondary md-icon-button " ng-click="deleteRelationship(item)">
	        				 <md-icon md-font-icon="fa fa-trash"></md-icon>
        				 </md-button>
        				 <md-divider ng-if="!$last"></md-divider>
			    		</md-list-item>
			    	</md-list>
				
				</expander-box>
				
				
				
				
			</div>
		</md-dialog-content>
		<md-dialog-actions layout="row">
			<span flex></span>
			<md-button ng-click="cancel()">
				{{translate.load("sbi.general.cancel")}}
			</md-button>
			<md-button   ng-click="create()" ng-disabled="!newBVForm.$valid ">
				{{translate.load("sbi.generic.update")}}
			</md-button>
		</md-dialog-actions>
	</form>
</md-dialog>