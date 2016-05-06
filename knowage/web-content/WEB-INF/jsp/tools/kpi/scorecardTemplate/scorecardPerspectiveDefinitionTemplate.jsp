
		<md-input-container>
			 <label>{{translate.load('sbi.kpi.scorecard.perspective.name')}}</label> 
			 <input ng-model="currentPerspective.name" > 
		 </md-input-container>

		<div layout="row"> 
			<md-input-container class="md-block" flex> 
				<label>{{translate.load('sbi.kpi.scorecard.perspective.criterion')}}</label> 
				<md-select ng-model="currentPerspective.criterion" ng-model-options="{trackBy: '$value.valueId'}"> 
					<md-option	ng-repeat="crit in criterionTypeList" ng-value="{{crit}}">	{{crit.translatedValueName}} </md-option> 
				</md-select> 
			</md-input-container> 
			<md-input-container flex='50' class="md-block" ng-if="currentPerspective.criterion.valueCd!='MAJORITY'"> 
				<label>{{translate.load('sbi.kpi.scorecard.priority.target')}}</label> 
				<md-select ng-model="currentPerspective.options.criterionPriority" ng-model-options="{trackBy: '$value.name'}" multiple=true> 
					<md-option	ng-repeat="targ in currentPerspective.targets" ng-value="{{targ}}">	{{targ.name}} </md-option> 
				</md-select> 
			</md-input-container>
		</div>
		
		 
	<md-content layout="column" class=" md-whiteframe-3dp" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>
					<span>{{translate.load('sbi.kpi.scorecard.perspective.goal.list')}}</span>
				</h2>
				<span flex></span>
				<md-button class="ng-scope" ng-click="addTarget()" aria-label="Aggiungi Obiettivo">
					{{translate.load('sbi.kpi.scorecard.perspective.goal.add')}}
				</md-button>
			</div>
		</md-toolbar>
		<div layout="row" layout-padding layout-wrap  ng-cloak >
			
			
			<md-whiteframe class="md-whiteframe-2dp scorecardPrespectiveCard" layout-margin layout="column"    ng-repeat="target in currentPerspective.targets">
				<md-toolbar>
					<div class="md-toolbar-tools" layout-fill layout="column">
						<kpi-semaphore-indicator indicator-color="target.status"  ></kpi-semaphore-indicator>
			       		<label>{{target.name}}</label>
			       		<span flex></span>
			       		
					<md-menu>
				      <md-button aria-label="Menu List" class="md-icon-button" ng-click="$mdOpenMenu($event)">
				        <md-icon md-menu-origin md-font-icon="fa fa-ellipsis-v"></md-icon>
				      </md-button>
				      <md-menu-content width="4">
				        <md-menu-item>
				          <md-button ng-click="addTarget(target, $index)">
				            <md-icon md-font-icon="fa fa-pencil-square-o" md-menu-align-target></md-icon>
				            {{translate.load('sbi.generic.modify')}}
				          </md-button>
				        </md-menu-item>
				        <md-menu-item>
				          <md-button ng-click="deleteTarget(target, $index)">
				            <md-icon md-font-icon="fa fa-trash"></md-icon>
				            {{translate.load('sbi.generic.delete')}}
				          </md-button>
				        </md-menu-item>
				   </md-menu-content>
				    </md-menu>
			       		
			    	</div>
		    	</md-toolbar>
		    	<md-content layout-padding layout="row" >
		    		
		    			<b layout-padding class="lh30">{{translate.load('sbi.generic.kpi')}}</b>
		    			<kpi-semaphore-indicator flex ng-repeat="groupedKpi in target.groupedKpis" indicator-color="groupedKpi.status" indicator-value="groupedKpi.count"></kpi-semaphore-indicator>

		    	</md-content>
			</md-whiteframe> 
			 
		</div>
		</md-content>
