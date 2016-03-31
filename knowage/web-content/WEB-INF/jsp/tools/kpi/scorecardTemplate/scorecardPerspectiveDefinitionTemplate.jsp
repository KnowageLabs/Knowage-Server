
		<md-input-container>
			 <label>{{translate.load('sbi.kpi.scorecard.perspective.name')}}</label> 
			 <input ng-model="currentPerspective.name"> 
		 </md-input-container>

		<md-input-container class="md-block"> 
			<label>{{translate.load('sbi.kpi.scorecard.perspective.criterion')}}</label> 
				<md-select ng-model="currentPerspective.criterion" ng-model-options="{trackBy: '$value.valueId'}" > 
				<md-option	ng-repeat="crit in criterionTypeList" ng-value="{{crit}}">	{{crit.translatedValueName}} </md-option> 
			</md-select> 
		</md-input-container>

	<md-content layout="column" class=" md-whiteframe-3dp" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>
					<span>{{translate.load('sbi.kpi.scorecard.perspective.goal.list')}}</span>
				</h2>
				<span flex></span>
				<md-button class="md-raised" ng-click="addTarget()" aria-label="Aggiungi Obiettivo">
					{{translate.load('sbi.kpi.scorecard.perspective.goal.add')}}
				</md-button>
			</div>
		</md-toolbar>
		<div layout="row" layout-padding layout-wrap  ng-cloak >
			
			
			<md-whiteframe class="md-whiteframe-2dp scorecardPrespectiveCard" layout-margin layout="column"    ng-repeat="prespective in currentScorecard.perspectives">
				<md-toolbar>
					<div class="md-toolbar-tools" layout-fill layout="column">
						<kpi-semaphore-indicator indicator-color="'RED'"></kpi-semaphore-indicator>
			       		<label>{{prespective.name}}</label>
			    	</div>
		    	</md-toolbar>
		    	<md-content layout-padding layout="row" >
		    		
		    			<b layout-padding class="lh30">KPI</b>
		    			<kpi-semaphore-indicator flex ng-repeat="groupedKpi in prespective.groupedKpis" indicator-color="groupedKpi.status" indicator-value="groupedKpi.count"></kpi-semaphore-indicator>

		    	</md-content>
			</md-whiteframe> 
			 
		</div>
		</md-content>
