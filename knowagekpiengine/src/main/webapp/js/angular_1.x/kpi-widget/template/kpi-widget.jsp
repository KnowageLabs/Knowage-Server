<md-card  class="" layout="column" layout-margin layout-padding > 
	<md-card-title>
    	<md-card-title-text layout="row" >
        	<span class="md-headline">{{label}}</span>
        	<span flex></span>
			<md-button ng-show="canSee" class="md-icon-button"  ng-click="openEdit()">
          		<md-icon md-font-icon="fa fa-pencil" aria-label="Edit Value"></md-icon>
       		</md-button>
        </md-card-title-text>
    </md-card-title>
 	
 	<div layout-padding layout="column"> 
		<div layout="row">
			<div flex class="kpiValue">
				<h3 class="">{{translate.load("sbi.kpi.widget.kpi")}}</h3>
				<div layout="row" layout-align="start center">
					<span ng-if="checkLabelIsPrefix()">{{valuePrefixSuffix}}&nbsp;</span>
					<h1>{{getValueToShow()}}</h1>
					<span ng-if="checkLabelIsSuffix()">&nbsp;{{valuePrefixSuffix}}</span>
				</div>
			</div>
			<div flex class="kpiValue" ng-show="getTargetToShow()!='-'">
				<h3 class="">{{translate.load("sbi.kpi.widget.target")}}</h3>
				<h1>{{getTargetToShow()}}</h1>
			</div>
			
		</div>
		<div layout="row" layout-align="center center" ng-if="showTargetPercentage && getPercentage()!='-'" class="kpiValue">
			<h1 >{{getPercentage()}}</h1>&nbsp;
			<h3 class="">
				{{translate.load("sbi.kpi.widget.percentage.oftarget")}}
			</h3>
		</div>
		
		<r-linear-gauge thresholds="thresholdStops" ng-model="value" tick-number="20" target="targetValue"></r-linear-gauge>
		
		<!--  kpi-linear-gauge style="overflow-x : hidden;"
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
		></kpi-linear-gauge*/-->
		
		<nvd3 data="data" options="options" ></nvd3>
	</div>
</md-card>