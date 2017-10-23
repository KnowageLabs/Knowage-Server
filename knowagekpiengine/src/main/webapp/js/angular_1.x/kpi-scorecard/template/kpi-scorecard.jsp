
<div layout-padding class="kn-scorecard-visualization" ng-cloak>
	<kpi-color-indicator perspectives="scorecardTarget.scorecard.perspectives" definition="false" ></kpi-color-indicator>
</div>




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

       <md-dialog-content class="kpiSelectionDialog">
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
