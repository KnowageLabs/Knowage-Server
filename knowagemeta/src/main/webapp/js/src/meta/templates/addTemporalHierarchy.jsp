<md-card   class="flexCard fullScreenPanel" >
	<md-card-content flex layout="column" class="noPadding">
		<md-toolbar>
		   <div class="md-toolbar-tools">
		     <h2>{{translate.load('sbi.meta.manage.temporal.hierarchy')}}</h2>
		     <span flex></span>
		   </div>
		 </md-toolbar>
		<md-content flex layout="column" layout-padding>
			<div layout="column" class="noPadding" layout-gt-sm="row">
				<div layout=row flex  layout-align="center center" >
					<md-input-container class="md-block" flex  >
						<label>{{translate.load("sbi.generic.name")}}</label>
						<input ng-model="currentHierarchy.name">
					</md-input-container>
<!-- 					<div flex layout-align="center center" layout=row> -->
<!-- 						<md-checkbox   ng-model="currentHierarchy.properties.defaultHierarchy" aria-label="hasAll" class="noMargin"> -->
<!-- 							{{translate.load("sbi.meta.isDefaultHierarchy")}} -->
<!-- 						</md-checkbox> -->
						 
<!-- 					</div> -->
				</div>
				<div layout=row flex >
					<div flex layout-align="center center" layout=row>
						<md-checkbox  ng-model="currentHierarchy.properties.hasall" aria-label="hasAll" class="noMargin">
							{{translate.load("sbi.meta.hasAll")}}
						</md-checkbox>
					</div>
					<md-input-container class="md-block  " flex  >
						<label>{{translate.load("sbi.meta.model.all.member.name")}}</label>
						<input ng-model="currentHierarchy.properties.allmembername" ng-disabled="currentHierarchy.properties.hasall!=true">
					</md-input-container>
				</div>
			</div>
			
			<div  layout="row" flex layout-margin>
				<md-whiteframe class="md-whiteframe-1dp" layout="column" style="min-width:200px">
					<md-toolbar>
				      <div class="md-toolbar-tools">
				        <h2>{{translate.load('sbi.meta.columns')}}</h2>
				       </div>
				    </md-toolbar>
					<div flex  style="overflow: auto;" >
						<md-list flex>
				   		   	<md-list-item ng-repeat="col in columns" ng-click="addCol(col)" ng-if="col.used!=true">
				   		   		{{col.name}}
				   		   		<md-divider></md-divider>
				   		   	</md-list-item>
			   		   </md-list>
					</div>
				</md-whiteframe>
				
				<angular-table id="attrTable" class="md-whiteframe-1dp" flex 
				ng-model="currentHierarchy.levels"
				columns="levelsColumns"
				speed-menu-option="levelsActions"
				scope-functions="levelsTableScope"
				>
				</angular-table>
			</div>
		
		
		
		</md-content>
	</md-card-content>
    
    <md-card-actions layout="row" layout-align="end center">
      <md-button ng-click="cancelConfiguration()" >
        {{translate.load('sbi.generic.cancel')}}
      </md-button>
      <md-button ng-disabled="!isValidHierarchy()" ng-click="saveConfiguration()"  >
        {{translate.load('sbi.generic.save')}}
      </md-button>

	</md-card-actions>
</md-card>