<div layout-fill ng-controller="filterController" class="overflow" layout="column">
	<md-whiteframe class="md-whiteframe-4dp filterWhiteFrame " layout-margin
		ng-repeat="kpi in selectedScheduler.kpis"> 
		<md-toolbar	class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<h2 class="md-flex">{{kpi.name}}</h2>
			</div>
		</md-toolbar>

	<div>
		<p>{{kpi}}</p>
		<p>TODOOOOOO</p>
	</div>
	</md-whiteframe>
</div>