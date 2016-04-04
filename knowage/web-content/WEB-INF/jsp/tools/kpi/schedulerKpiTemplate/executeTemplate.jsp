<div layout-fill class="overflow" layout="column">
	<md-whiteframe class="md-whiteframe-4dp  " layout-margin>
		<md-toolbar class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<h2 class="md-flex">{{translate.load("sbi.kpiScheduler.executionType")}}</h2>
			</div>
		</md-toolbar>
		
			<md-radio-group ng-model="selectedScheduler.delta">
				<md-radio-button ng-value="true">{{translate.load("sbi.kpiScheduler.insertAndUpdate")}}</md-radio-button>
				<md-radio-button ng-value="false">{{translate.load("sbi.kpiScheduler.deleteAndInsert")}}</md-radio-button>
			</md-radio-group>
		
		<div layout="row"><span flex></span><md-button>{{translate.load("sbi.kpiScheduler.execute")}}</md-button></div>
	</md-whiteframe>
</div>