<md-content layout-fill layout="column" ng-controller="kpiDefinitionThresholdController"> 
 	
 	<md-whiteframe class="md-whiteframe-2dp cloneWarningTab" layout="row"  ng-if="isUsedByAnotherKpi.value==true">
	   <p flex>{{translate.load("sbi.kpi.threshold.load.reused.title")}} {{ translate.load("sbi.kpi.threshold.load.reused.message")}}</p>
	   <md-button    ng-click="cloneThreshold()"  >  {{translate.load("sbi.generic.clone")}} </md-button>
 	 </md-whiteframe>
  <div layout="row">
	  <md-input-container flex class="md-block">
	            <label>{{translate.load("sbi.generic.name")}}</label>
	            <input ng-model="kpi.threshold.name" >
	  </md-input-container>
  
   		<md-input-container flex class="md-block">
          <label>{{translate.load("sbi.generic.descr")}}</label>
          <input ng-model="kpi.threshold.description" md-maxlength="1000">
        </md-input-container>
    </div>    
      <div layout="row">   
      <md-input-container flex="50" class="md-block" >
	        <label>{{translate.load("sbi.generic.type")}}</label>
	        <md-select ng-model="kpi.threshold.typeId" >
	          <md-option ng-repeat="thresh in thresholdTypeList" value="{{thresh.valueId}}">
	            {{thresh.translatedValueName}}
	          </md-option>
	        </md-select>
      </md-input-container>
        <span flex></span>
       <md-button class="md-raised loadThreshold" aria-label="load" ng-click="openThresholdSidenav()">{{translate.load("sbi.kpi.threshold.load.from.list")}}  </md-button>
       
    </div>
    
 
	<angular-table  flex class="overflow relative thresholdTable" full-width
		id='kpiListTableThreshold' ng-model=kpi.threshold.thresholdValues
		columns='thresholdColumn'
		click-function="" 
		scope-functions="thresholdFunction"
		no-pagination=true
		speed-menu-option= thresholdTableActionButton
		>
		
			<queue-table >
				<div layout="row"> 
					<span flex></span>
					<md-button   ng-click="scopeFunctions.addNewThreshold()">{{translate.load("sbi.kpi.threshold.add")}}</md-button>
				</div>
			</queue-table> 
		
		</angular-table>
		
		 
		<md-sidenav class="md-sidenav-right md-whiteframe-z2" layout="column" md-component-id="thresholdTab" >
	      <md-toolbar>
	        <h1 class="md-toolbar-tools">{{translate.load("sbi.thresholds.listTitle")}}</h1>
	      </md-toolbar>
	      <md-content layout-margin flex class="relative"> 
	        <angular-list layout-fill class="absolute" id="thresholdListANGL"
                		ng-model=thresholdList
                		item-name='name' 
                		show-search-bar=true
                		click-function="loadSelectedThreshold(item,listId)" 
                		>
                		</angular-list>


	      </md-content>
	    </md-sidenav>
</md-content>
