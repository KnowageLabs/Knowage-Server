<div layout-fill class="overflow" layout="column">
	<md-whiteframe class="md-whiteframe-4dp  " layout-margin>
		<md-toolbar class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<h2 class="md-flex">Tipologia Esecuzione</h2>
			</div>
		</md-toolbar>
		<md-card>
			<md-radio-group ng-model="selectedScheduler.delta">
				<md-radio-button ng-value="true">Insert and Update</md-radio-button>
				<md-radio-button ng-value="false">Delete and Insert</md-radio-button>
			</md-radio-group>
		</md-card>
		<div layout="row"><span flex></span><md-button>Execute</md-button></div>
	</md-whiteframe>
</div>