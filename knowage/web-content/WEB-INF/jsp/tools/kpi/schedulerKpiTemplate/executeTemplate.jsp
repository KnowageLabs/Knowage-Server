<div layout-fill ng-controller="schedulerKpiController" class="overflow"
	layout="column">
	<md-whiteframe class="md-whiteframe-4dp  "
		layout-margin> 
		<md-toolbar	class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<h2 class="md-flex">Tipologia Esecuzione</h2>
			</div>
		</md-toolbar>

	<div class="md-toolbar-tools">
		<md-radio-group ng-model="selectedScheduler.delta"> 
		<md-radio-button
			value="true">Insert and Update</md-radio-button> <md-radio-button
			value="false"> Delete and Insert </md-radio-button> </md-radio-group>
	</div>
	</md-whiteframe>
</div>