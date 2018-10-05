<md-card   class="flexCard fullScreenPanel" >
	<md-card-content flex layout="column" class="noPadding">
	    <md-toolbar>
	      <div class="md-toolbar-tools">
	        <h2>{{translate.load('sbi.meta.manage.temporal.hierarchy')}}</h2>
	        <span flex></span>
	      </div>
	    </md-toolbar>
    <md-content flex layout="column">
		<angular-table ng-show="hierarchyList!=undefined && hierarchyList.length>0" id="hierarchyTable" class="md-whiteframe-1dp" flex 
				ng-model="hierarchyList"
				columns="hierarchyColumns"
				speed-menu-option="hierarchyActions"
				scope-functions="hierarchyTableScope"
				>
				<queue-table>
					<div layout="row">
						<span flex></span>
						 <md-button ng-click="scopeFunctions.manageHierarchy()">{{scopeFunctions.translate.load("sbi.generic.add")}}</md-button>
					</div>
				</queue-table>
		</angular-table>
		
		<div flex layout="column"  layout-align="center center" ng-if="hierarchyList==undefined || hierarchyList.length==0">
		
		<h1>{{translate.load("sbi.meta.hierarchy.emptyList")}}</h1>
		 <md-button class="md-raised" ng-click="manageHierarchy()">{{translate.load("sbi.generic.add")}}</md-button>
		</div>
		
    </md-content>
</md-card-content>
    
    <md-card-actions layout="row" layout-align="end center">
      <md-button ng-click="cancelConfiguration()" >
        {{translate.load('sbi.generic.cancel')}}
      </md-button>
      <md-button ng-click="saveConfiguration()"  >
        {{translate.load('sbi.generic.save')}}
      </md-button>

	</md-card-actions>
</md-card>