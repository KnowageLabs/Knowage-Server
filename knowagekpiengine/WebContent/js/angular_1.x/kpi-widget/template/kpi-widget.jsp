<md-whiteframe  class="md-whiteframe-4dp " layout="column" layout layout-margin  > 
	 <md-toolbar	class="miniheadimportexport ternaryToolbar" layout="row">
		<div class="md-toolbar-tools">
			<h1  style="font-size: {{fontConf.size}}em;">{{label}}</h1>
		</div>
		<span flex></span>
		
		<md-button ng-show="canSee" class="md-icon-button"  ng-click="openEdit()">
          <md-icon md-font-icon="fa fa-pencil" aria-label="Edit Value"></md-icon>
        </md-button>
       
	</md-toolbar>
 	<div layout-padding layout="column"> 
		<div layout="row">
			<div flex>
				<h3 style="font-size: {{fontConf.size}}em;" class="grey">{{translate.load("sbi.kpi.widget.kpi")}}</h3>
				<h1 style="color:#3B678C">{{getValueToShow()}}</h1>
			</div>
			<div flex>
				<h3 style="font-size: {{fontConf.size}}em;" class="grey">{{translate.load("sbi.kpi.widget.target")}}</h3>
				<h1 style="color:#C4DCF3">{{getTargetToShow()}}</h1>
			</div>
			
		</div>
		<div layout="row" layout-align="start center" ng-if="showTargetPercentage" >
			<h1  style="color:#C4DCF3">{{getPercentage()}}</h1> 
			<h3 style="font-size: {{fontConf.size}}em;" layout-margin class="grey">
				{{translate.load("sbi.kpi.widget.percentage.oftarget")}}
			</h3>
		</div>
		
		<kpi-linear-gauge
			gauge-id="widgetId"
			label="label"
			size="gaugeSize"
			min-value="minValue"
			max-value="maxValue"
			value="value"
			threshold-stops="thresholdStops"
			show-value="false"
			show-thresholds="showThresholds"
			value-precision="precision"
			font-conf="fontConf"
			target-value="targetValue"
		></kpi-linear-gauge>
		
<!-- 		<nvd3 data="data" options="options" ></nvd3> -->
	</div>
</md-whiteframe>