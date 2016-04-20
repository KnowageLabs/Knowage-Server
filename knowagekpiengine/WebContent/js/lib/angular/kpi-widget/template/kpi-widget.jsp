 <md-whiteframe class="md-whiteframe-4dp layout-padding " layout="column" layout layout-margin  > 
 	
 	<md-toolbar	class="miniheadimportexport ternaryToolbar">
		<div class="md-toolbar-tools">
			<h1  style="font-size: {{documentData.template.chart.style.font.size}}em;">{{documentData.kpiValue.name}}</h1>
		</div>
	</md-toolbar>
 	
	<div layout="row">
		<div flex>
		<h3 style="font-size: {{documentData.template.chart.style.font.size}}em;">KPI Value</h3>
		<h3 style="color: #a9c3db;font-size: {{documentData.template.chart.style.font.size}}em;">{{gaugeValue}}</h3>
		</div>
		<div flex>
		<h3 style="font-size: {{documentData.template.chart.style.font.size}}em;">Target Value</h3>
		<h3 style="font-size: {{documentData.template.chart.style.font.size}}em;">{{gaugeTargetValue}}</h3>
		</div>
	</div>
	<div ng-if="documentData.template.chart.options.showtargetpercentage">
		<h3  style="font-size: {{documentData.template.chart.style.font.size}}em;" >{{percentage}}% of Target</h3>
	</div>
	
	<kpi-linear-gauge style="height: 150px;"
		gauge-id="documentData.docId"
		label="documentData.docLabel"
		size="gaugeSize"
		min-value="gaugeMinValue"
		max-value="gaugeMaxValue"
		value="gaugeValue"
		threshold-stops="thresholdStops"
		show-value="documentData.template.chart.options.showvalue"
		show-thresholds="documentData.template.chart.options.showthreshold"
		value-precision="documentData.template.chart.options.history.size"
		font-conf="documentData.template.chart.style.font"
	></kpi-linear-gauge>
	<nvd3 data="data" options="options" ></nvd3>
	
</md-whiteframe>