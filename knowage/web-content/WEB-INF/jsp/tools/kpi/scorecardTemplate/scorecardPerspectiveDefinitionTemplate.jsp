
		<md-input-container>
			 <label>Nome Prospettiva</label> 
			 <input ng-model="currentPerspective.name"> 
		 </md-input-container>

		<md-input-container class="md-block"> 
			<label>Criterio	di Valutazione</label> 
				<md-select ng-model="currentPerspective.criterion"> 
				<md-option	ng-repeat="crit in criterionTypeList" value="{{sublist.abb}}">	{{sublist.abb}} </md-option> 
			</md-select> 
		</md-input-container>

	<md-content layout="column" class=" md-whiteframe-3dp" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>
					<span>Elenco Obiettivi</span>
				</h2>
				<span flex></span>
				<md-button class="md-raised" ng-click="addTarget()" aria-label="Aggiungi Obiettivo">
					Aggiungi Obiettivo 
				</md-button>
			</div>
		</md-toolbar>
		<div layout="row" layout-padding layout-wrap flex ng-cloak >
			
			<md-whiteframe class="md-whiteframe-2dp" layout-margin layout="column"  layout-align="top center" ng-repeat="prespective in currentScorecard.perspectives">
				<md-toolbar>
					<div class="md-toolbar-tools" layout-fill layout="column">
						<i class="fa fa-circle-thin"></i>
			       		<label>{{prespective.name}}</label>
			    	</div>
		    	</md-toolbar>
		    	<div layout="row">
		    		<div>
		    			<span>KPI</span>
		    			<span ng-repeat="groupedKpi in prespective.groupedKpis">
		    				<i class="fa fa-circle-thin" style="color : {{groupedKpi.status}}"></i>
		    				<span>{{groupedKpi.count}}</span>
		    			</span>
		    		</div>
		    	</div>
			</md-whiteframe> 
			
		</div>
		</md-content>
