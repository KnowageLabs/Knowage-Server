	<md-input-container> 
		<label>Nome	Scorecard</label> <input ng-model="currentScorecard.name"> 
	</md-input-container> 

	<md-content layout="column" class=" md-whiteframe-3dp" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>
					<span>Elenco Prospettive</span>
				</h2>
				<span flex></span>
				<md-button class="md-raised" ng-click="addPerspective()" aria-label="Aggiungi Prospettiva">
					Aggiungi Prospettiva 
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
<!-- 		    			<span flex ng-repeat="groupedKpi in prespective.groupedKpis"> -->
<!-- 		    				<span  ng-if="groupedKpi.status=='RED'" class="fa-stack"> -->
<!-- 		    					<i class="fa fa-square-o fa-stack-1x scorecardSemaphoreBackIcon"></i> -->
<!-- 		    					<i class="fa fa-square fa-stack-1x scorecardSemaphoreFrontIcon" style="color : {{groupedKpi.status}}"></i> -->
<!-- 		    				</span> -->
		    				
<!-- 		    				<span  ng-if="groupedKpi.status=='YELLOW'" class="fa-stack fa-rotate-45"> -->
<!-- 		    					<i class="fa fa-square-o fa-stack-1x  scorecardSemaphoreBackIcon"></i> -->
<!-- 		    					<i class="fa fa-square fa-stack-1x  scorecardSemaphoreFrontIcon" style="color : {{groupedKpi.status}}"></i> -->
<!-- 		    				</span> -->
		    				
<!-- 		    				<span  ng-if="groupedKpi.status=='GREEN'" class="fa-stack"> -->
<!-- 		    					<i class="fa fa-circle-o fa-stack-1x scorecardSemaphoreBackIcon"></i> -->
<!-- 		    					<i class="fa fa-circle fa-stack-1x scorecardSemaphoreFrontIcon" style="color : {{groupedKpi.status}}"></i> -->
<!-- 		    				</span> -->
<!-- 		    				<span>{{groupedKpi.count}}</span> -->
<!-- 		    			</span> -->
		    		
		    	</md-content>
			</md-whiteframe> 
			
		</div>
		</md-content>
