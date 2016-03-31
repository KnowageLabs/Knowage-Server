<div layout-fill ng-controller="filterController" class="overflow" layout="column">
	<md-whiteframe class="md-whiteframe-4dp filterWhiteFrame " layout-margin
		ng-repeat="kpi in selectedScheduler.kpis"> 
		<md-toolbar	class="miniheadimportexport">
			<div class="md-toolbar-tools">
				<h2 class="md-flex">{{kpi.name}}</h2>
			</div>
		</md-toolbar>

	<div layout="row" >
	<div flex=50 ng-repeat="filter in selectedScheduler.filters" ng-if="filter.kpiName == kpi.name">
		<md-whiteframe class="md-whiteframe-4dp filterWhiteFrame" layout-margin > 
			<md-card > 
			<div >
				<h3>{{filter.placeholderName}}</h3>
			</div>
			<di layout=rowv>
			<div flex=50>
				<md-select aria-label="aria-label" ng-model="filter.type.valueCd">
						<md-option ng-repeat="type in listType" value="{{type.VALUE_CD}}">{{type.VALUE_CD}}</md-option>
				</md-select> 
			</div>
			<div flex=50>
				<md-input-container class="small counter" class="small counter" ng-if="filter.type.valueCd=='FIXED_VALUE'">
					<label>{{translate.load("sbi.behavioural.lov.details.label")}}</label>
					<input class="input_class" ng-model="filter.value" required> 
				</md-input-container>
				<md-select aria-label="aria-label" ng-model="filter.value"  ng-if="filter.type.valueCd=='TEMPORAL_FUNCTIONS'">
						<md-option ng-repeat="type in funcTemporal" value="{{type.VALUE_CD}}">{{type.VALUE_CD}}</md-option>
				</md-select> 
				<md-select aria-label="aria-label" ng-model="filter.value" ng-if="filter.type.valueCd=='LOV'">
						<md-option ng-repeat="type in lov" value="{{type.name}}">{{type.name}}</md-option>
				</md-select> 
			</div>
			</md-card>
		</md-whiteframe>
	</div>
	</div>
</div>