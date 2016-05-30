<md-content layout="column" class=" md-whiteframe-3dp" flex>

<div layout="column" layout-padding layout-wrap ng-cloak>

	<expander-box id="Info" color="white" expanded="true" toolbar-class="ternaryToolbar" title="perspective.name" locals="localsScope" layout-margin layout="column" 
	class="md-whiteframe-2dp scorecardPrespectiveCard" ng-repeat="perspective in scorecardTarget.scorecard.perspectives" >
	<custom-toolbar layout="row">
		<kpi-semaphore-indicator flex indicator-color="perspective.status"></kpi-semaphore-indicator>
	</custom-toolbar>
	<custom-toolbar-action layout="row">
		<md-button style="position:absolute;right:75px;top:0px;"  aria-label="KPI list Perspective" class="md-icon-button" layout-padding ng-click="localsScope.listKPer(perspective.id,$event)">
		<md-icon md-menu-origin md-font-icon="fa fa-search"></md-icon></md-button>
		<md-button style="position:absolute;right:40px;top:0px;"  aria-label="Criterion Perspective" class="md-icon-button" layout-padding ng-click="localsScope.critPers(perspective.id,$event)">
		<md-icon md-font-icon="fa fa-info-circle"></md-icon></md-button>
	</custom-toolbar-action>
	<md-whiteframe class="md-whiteframe-2dp scorecardPrespectiveCard" layout-margin layout="column">

		<md-content layout-padding layout="column">
		<div layout="row">
		 <b	layout-padding class="lh30">{{translate.load('sbi.kpi.kpi')}}</b> 
		 <kpi-semaphore-indicator flex ng-repeat="groupedKpi in perspective.groupedKpis"
			indicator-color="groupedKpi.status" indicator-value="groupedKpi.count"></kpi-semaphore-indicator>
		</div>
		<div layout="row" layout-wrap>
			<md-whiteframe class="md-whiteframe-2dp scorecardPrespectiveCard"
				layout-margin layout-wrap layout="column" ng-repeat="goal in perspective.targets">
				<md-toolbar class="ternaryToolbar">
					<div class="md-toolbar-tools" layout-fill layout="column">
					<kpi-semaphore-indicator indicator-color="goal.status">
						</kpi-semaphore-indicator>
						<label>{{goal.name}}</label> 
						<span flex></span>
						   <md-button style="position:absolute;right:28px;top:0px;" aria-label="KPI List" class="md-icon-button" ng-click="localsScope.listKGoal(goal.id, perspective.id, $event)">
				       		 <md-icon md-menu-origin md-font-icon="fa fa-search"></md-icon>
				     	   </md-button>
				     	   	<md-button style="position:absolute;right:8px;top:0px;"  aria-label="Criterion Goal" class="md-icon-button" layout-padding ng-click="localsScope.critGoal(goal.id,perspective.id, $event)">
							<md-icon md-font-icon="fa fa-info-circle"></md-icon></md-button>
					</div>
				</md-toolbar> 
				<div>
				<md-content layout-padding layout="row"> 
					<b	layout-padding class="lh30">{{translate.load('sbi.kpi.kpi')}}</b> 
					<kpi-semaphore-indicator flex	ng-repeat="groupedKpi in goal.groupedKpis"
						indicator-color="groupedKpi.status"	indicator-value="groupedKpi.count">
					</kpi-semaphore-indicator> 
				</md-content>
				</div>
			</md-whiteframe> 
		</div>
		</md-content> 
	</md-whiteframe>
</expander-box>

</div>
</md-content>




<script type="text/ng-template" id="templateKPI.html">
<md-dialog aria-label="Select Function" style="height:80%; width:70%" ng-cloak layout="column" >
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>{{translate.load('sbi.kpi.scorecard.list.kpi.toolbar')}} {{nameList}}</h1>
        <span flex class="flex2"></span>
		<md-button class="md-primary" ng-click="closeDialog()">
			Close    
        </md-button>
      </div>
    </md-toolbar>

       <md-dialog-content layout style="height:90%;">
		<angular-table
		id='targetListTable' ng-model=arrayToShow
		columns='[{"label":"Status","name":"kpiSemaphore","size":"60px"}, {"label":"KPI Name","name":"name"},{"label":"Category","name":"category.translatedValueName"},{"label":"Date","name":"dateCreation"},{"label":"Author","name":"author"}]'
		comparison-column="'id'"
		scope-functions = tableFunction 
		style="height:100%;"
		> </angular-table>
	  </md-dialog-content>

</md-dialog>
</script>


<script type="text/ng-template" id="templateCriterion.html">
<md-dialog aria-label="show criterion" style="min-width:30%; min-height:60%;" class="flex2" ng-cloak layout="column">

    <md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load('sbi.kpi.scorecard.info.toolbar')}} {{nameList}}</h2>
        <span flex></span>
        <md-button class="md-icon-button" ng-click="closeDialog()">
          	Close  
        </md-button>
      </div>
    </md-toolbar>
    <md-dialog-content layout-padding>
  	<div layout="column"> 
		<label layout = "row" style="padding-left:10px"><b>{{translate.load('sbi.kpi.scorecard.perspective.criterion')}} : </b></label>
		<label layout = "row" style="padding-left:20px">{{translate.load(criterion.translatedValueName)}} </label>
		<br>
		<br>
		<div  ng-if="criterion.valueCd != 'MAJORITY'">
		<div>
			<label layout="row" style="padding-left:10px"><b>{{translate.load('sbi.kpi.scorecard.info.list')}}</b></label>
		</div>
		<span flex></span>
		<div>
				<div layout="column" style="padding-left:20px">
					<label layout="row" ng-repeat="targ in criterionOption.criterionPriority"><li>{{targ}}</label>
				</div>
		</div>
		</div>	
	</div>
    </md-dialog-content> 
</md-dialog>
</script>
