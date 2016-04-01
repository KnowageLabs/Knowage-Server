		<md-input-container>
			 <label>{{translate.load('sbi.kpi.scorecard.perspective.goal.name')}}</label> 
			 <input ng-model="currentTarget.name"> 
		 </md-input-container>
		
		<div layout="row">
			<md-input-container flex class="md-block"> 
				<label>{{translate.load('sbi.kpi.scorecard.perspective.criterion')}}</label> 
					<md-select ng-model="currentTarget.criterion" ng-model-options="{trackBy: '$value.valueId'}" > 
					<md-option	ng-repeat="crit in criterionTypeList" ng-value="{{crit}}">	{{crit.translatedValueName}} </md-option> 
				</md-select> 
			</md-input-container>
			<md-input-container flex='50' class="md-block" ng-if="currentTarget.criterion.valueId==229"> 
				<label>{{translate.load('sbi.kpi.scorecard.priority.kpi')}}</label> 
				<md-select ng-model="currentTarget.criterionPriority" ng-model-options="{trackBy: '$value.name'}" multiple=true> 
					<md-option	ng-repeat="kpi in currentTarget.kpis" ng-value="{{kpi}}">	{{kpi.name}} </md-option> 
				</md-select> 
			</md-input-container>
		</div>
		
		<md-content layout="column" class=" md-whiteframe-3dp" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>
					<span>{{translate.load('sbi.kpi.scorecard.perspective.kpi.list')}}</span>
				</h2>
				<span flex></span>
				<md-button class="md-raised" ng-click="addKpiToTarget();" aria-label="load kpi">
					{{translate.load('sbi.kpi.scorecard.goal.load.kpi')}}
				</md-button>
			</div>
		</md-toolbar>
		
		 	<angular-table flex
		 		id='targetList' 
		 		ng-model=currentTarget.kpis
				columns='[  {"label":"Name","name":"name"}, {"label":"category","name":"category.translatedValueName"}]'
			 	show-search-bar=true
			 	speed-menu-option = targetListAction
		> 
			</angular-table>




		</md-content>
		
<script type="text/ng-template" id="templatesaveKPI.html">
<md-dialog aria-label="Select Function" style="height: 90%;" ng-cloak layout="column" >
  
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>Select KPI</h1>
        <span flex></span>
		<md-button class="md-primary" ng-click="saveKpiToTarget()">
          	Save
        </md-button>
		<md-button class="md-primary" ng-click="close()">
			Close    
        </md-button>
      </div>
	
    </md-toolbar>
    <md-dialog-content flex layout >
		<angular-table flex
		id='targetListTable' ng-model=kpiAllList
		columns='[{"label":"KPI Name","name":"name"},{"label":"Category","name":"category.translatedValueName"},{"label":"Date","name":"datacreation"},{"label":"Author","name":"author"}]'
		columns-search='["name"]' show-search-bar=true
		multi-select = true selected-item=kpiSelected comparison-column="'id'"
		scope-functions = tableFunction 
		> </angular-table>
   	 </md-dialog-content>

</md-dialog>
</script>