 <md-whiteframe  class="md-whiteframe-4dp " layout="column" layout layout-margin  > 
 	
 	<md-toolbar	class="miniheadimportexport ternaryToolbar">
		<div class="md-toolbar-tools">
			<h1  style="font-size: {{documentData.template.chart.style.font.size}}em;">{{documentData.kpiValue.name}}</h1>
		</div>
	</md-toolbar>
 	<div layout-padding layout="column"> 
		<div layout="row">
			<div flex>
				<h3 style="font-size: {{documentData.template.chart.style.font.size}}em;" class="grey">KPI Value</h3>
				<h1 ng-show="gaugeValue<1000" style="color:#3B678C">{{gaugeValue}}</h1>
				<h1 ng-show="gaugeValue>=1000" style="color:#3B678C">{{gaugeValue/1000}}K</h1>
			</div>
			<div flex>
				<h3 style="font-size: {{documentData.template.chart.style.font.size}}em;" class="grey">Target Value</h3>
				<h1 ng-show="gaugeTargetValue<1000" style="color:#C4DCF3">{{gaugeTargetValue}}</h1>
				<h1 ng-show="gaugeTargetValue>=1000" style="color:#C4DCF3">{{gaugeTargetValue/1000}}K</h1>
			</div>
			
		</div>
		<div layout="row" layout-align="start center" ng-if="documentData.template.chart.options.showtargetpercentage" >
			<h1  style="color:#C4DCF3">{{percentage}}%</h1> <h3 style="font-size: {{documentData.template.chart.style.font.size}}em;" layout-margin class="grey">of Target</h3>
			
			
		</div>
		
		<kpi-linear-gauge
			gauge-id="documentData.docId"
			label="documentData.docLabel"
			size="gaugeSize"
			min-value="gaugeMinValue"
			max-value="gaugeMaxValue"
			value="gaugeValue"
			threshold-stops="thresholdStops"
			show-value="false"
			show-thresholds="documentData.template.chart.options.showthreshold"
			value-precision="documentData.template.chart.options.history.size"
			font-conf="documentData.template.chart.style.font"
			target-value="gaugeTargetValue"
		></kpi-linear-gauge>
		
		<nvd3 data="data" options="options" ></nvd3>
	</div>
</md-whiteframe>