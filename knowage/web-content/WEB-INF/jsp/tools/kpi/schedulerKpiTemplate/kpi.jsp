<div layout-fill ng-controller = "kpiController" layout="row">
 <md-whiteframe class="md-whiteframe-4dp layout-padding" layout-margin >
 		<angular-table layout-fill class="absolute"
		id='targetListTable' ng-model=selectedScheduler.kpi
		columns='[{"label":"Name","name":"name"},{"label":"Category","name":"valueCd"}]'
		columns-search='["name"]' show-search-bar=true
		scope-functions=tableFunction  speed-menu-option=measureMenuOption 
		> 
		
		<queue-table >
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.loadListKPI()">Add KPI associations</md-button>
			</div>
		</queue-table> 
		</angular-table>
		

 	
   </md-whiteframe>       


</div>