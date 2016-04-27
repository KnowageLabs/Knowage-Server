<md-content layout="column" class=" md-whiteframe-3dp" flex>

<div layout="column" layout-padding layout-wrap ng-cloak>

	<expander-box id="Info" color="white" expanded="true" toolbar-class="ternaryToolbar" title="perspective.name" layout-margin layout="column" 
	class="md-whiteframe-2dp scorecardPrespectiveCard" ng-repeat="perspective in scorecardTarget.scorecard.perspectives" >
	<custom-toolbar>
	<kpi-semaphore-indicator flex	indicator-color="perspective.status">
	</kpi-semaphore-indicator></custom-toolbar>
	
	<md-whiteframe class="md-whiteframe-2dp scorecardPrespectiveCard" layout-margin layout="column">

		<md-content layout-padding layout="column">
		<div layout="row">
		 <b	layout-padding class="lh30">KPI</b> 
		 <kpi-semaphore-indicator flex ng-repeat="groupedKpi in perspective.groupedKpis"
			indicator-color="groupedKpi.status" indicator-value="groupedKpi.count"></kpi-semaphore-indicator>
		</div>
		<div layout="row" layout-wrap>
			<md-whiteframe class="md-whiteframe-2dp scorecardPrespectiveCard"
				layout-margin layout-wrap layout="column" ng-repeat="goal in perspective.targets">
				<md-toolbar class="ternaryToolbar">
					<div class="md-toolbar-tools" layout-fill layout="column">
					<kpi-semaphore-indicator indicator-color="goal.status">
						</kpi-semaphore-indicator>
						<label>{{goal.name}}</label> 
						<span flex></span>
					</div>
				</md-toolbar> 
				<div>
				<md-content layout-padding layout="row"> 
					<b	layout-padding class="lh30">KPI</b> 
					<kpi-semaphore-indicator flex	ng-repeat="groupedKpi in goal.groupedKpis"
						indicator-color="groupedKpi.status"	indicator-value="groupedKpi.count">
					</kpi-semaphore-indicator> 
				</md-content>
				</div>
			</md-whiteframe> 
		</div>
		</md-content> 
	</md-whiteframe>
</expander-box>

</div>
</md-content>

