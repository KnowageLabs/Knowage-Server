<md-dialog aria-label="add Business model" ng-cloak  layout="column" md-dynamic-height style="min-width:90%">
	<form name="newBVForm">
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>{{translate.load("sbi.meta.new.businessview")}}</h2>
			</div>
		</md-toolbar>
		<md-dialog-content style="min-width:500px"> 
				<div ng-if="steps.current==0" layout="column" flex layout-padding>
					 <md-input-container class="md-block">
						<label>{{translate.load("sbi.generic.name")}}</label>
						<input ng-model="tmpBnssView.name" required>
					</md-input-container>
					
					<md-input-container class="md-block">
						<label>{{translate.load("sbi.generic.descr")}}</label>
						<textarea  ng-model="tmpBnssView.description" ></textarea>
					</md-input-container>

					<angular-table  flex id='newBViewTableColumn'
					ng-model="physicalModel"
					columns='bvTableColumns'
				 	show-search-bar=true 
				 	no-pagination="true"
				 	multi-select="true"
				 	selected-item="tmpBnssView.physicalModels"
				 	></angular-table>
				</div>
				<div ng-if="steps.current==1" layout-padding>
					<div layout="row" >
						<md-input-container class="md-block margin-right10" flex>
							<label>{{translate.load("sbi.meta.business.relationship.source.table")}}</label>
							<md-select ng-model="sourceTable" ng-model-options="{trackBy: '$value.name'}" >
								<md-option ng-repeat="colu in tmpBnssView.physicalModels"  ng-value="colu"  >
								{{colu.name}}
								</md-option>
							</md-select>
						</md-input-container>						

						<md-input-container class="md-block" flex>
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
						multivalue=true						
						>
					</associator-directive>
				
				
				
				
					<expander-box title="translate.load('sbi.meta.new.businessview.summary')" expanded="true" layout="column">
					 
						<md-list class="md-dense noPadding" flex ng-repeat="item in summary">
	        				<md-list-item ng-repeat="rel in item.links"  ng-click="null" layout="row">
		        				<span flex=40>{{rel.$parent.name}}.{{rel.name}}</span>
		        				<span flex=10  ><i class="fa fa-link" aria-hidden="true"></i></span>
	        					<span flex=40>{{item.$parent.name}}.{{item.name}}</span>
		        				<span flex=10  > <md-button   class="md-secondary md-icon-button "  aria-label="delete relationship" ng-click="deleteRelationship(item,rel)">
		        				 <md-icon md-font-icon="fa fa-trash"></md-icon>
	        				 	</md-button></span>
	        				 <md-divider></md-divider>
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
			
			<md-button  ng-if="steps.current==0" ng-click="next()" ng-disabled="!newBVForm.$valid || tmpBnssView.physicalModels.length<2">
				{{translate.load("sbi.generic.next")}}
			</md-button>
			<md-button ng-if="steps.current==1 && editMode!=true" ng-disabled="summary.length>0"  ng-click="back()"  >
				{{translate.load("sbi.generic.back")}}
			</md-button>
			<md-button  ng-if="steps.current==1" ng-click="create()" ng-disabled="!newBVForm.$valid ">
				{{translate.load("sbi.generic.update")}}
			</md-button>
		</md-dialog-actions>
	</form>
</md-dialog>