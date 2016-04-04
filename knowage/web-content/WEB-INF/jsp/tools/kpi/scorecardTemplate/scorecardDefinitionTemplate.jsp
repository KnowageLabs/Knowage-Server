	<md-input-container> 
		<label>{{translate.load('sbi.kpi.scorecard.scorecard.name')}}</label> <input ng-model="currentScorecard.name"> 
	</md-input-container> 

	<md-content layout="column" class=" md-whiteframe-3dp" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>
					<span>{{translate.load('sbi.kpi.scorecard.perspective.list')}}</span>
				</h2>
				<span flex></span>
				<md-button class="md-raised" ng-click="addPerspective()" aria-label="Aggiungi Prospettiva">
					{{translate.load('sbi.kpi.scorecard.perspective.add')}}
				</md-button>
			</div>
		</md-toolbar>
		<div layout="row" layout-padding layout-wrap  ng-cloak >
			
			<md-whiteframe class="md-whiteframe-2dp scorecardPrespectiveCard" layout-margin layout="column"    ng-repeat="perspective in currentScorecard.perspectives">
				<md-toolbar>
					<div class="md-toolbar-tools" layout-fill layout="column">
						<kpi-semaphore-indicator indicator-color="perspective.status"></kpi-semaphore-indicator>
			       		<label>{{perspective.name}}</label>
			       		<span flex></span>
			       		 <md-button class="md-icon-button"  ng-click="addPerspective(perspective, $index)">
				      	    <md-icon md-font-icon="fa fa-pencil-square-o fa-2x" aria-hidden="true"></md-icon>
				        </md-button>
			    	</div>	
		    	</md-toolbar>
		    	<md-content layout-padding layout="row" >
		    		
		    			<b layout-padding class="lh30">KPI</b>
		    			<kpi-semaphore-indicator flex ng-repeat="groupedKpi in perspective.groupedKpis" indicator-color="groupedKpi.status" indicator-value="groupedKpi.count"></kpi-semaphore-indicator>

		    	</md-content>
			</md-whiteframe> 
			
		</div>
		</md-content>
