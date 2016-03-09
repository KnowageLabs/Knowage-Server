<md-content layout-fill layout="column" ng-controller="kpiDefinitionThresholdController"> 
  <div layout="row">
  <md-input-container flex class="md-block">
            <label>name</label>
            <input ng-model="kpi.threshold.name" >
  </md-input-container>
  
   <md-input-container flex class="md-block">
          <label>description</label>
          <textarea ng-model="kpi.threshold.description" md-maxlength="150" ></textarea>
        </md-input-container>
        
        
      <md-input-container flex class="md-block" >
	        <label>Type</label>
	        <md-select ng-model="kpi.threshold.typeId" >
	          <md-option ng-repeat="thresh in thresholdTypeList" value="{{thresh.valueId}}">
	            {{thresh.translatedValueName}}
	          </md-option>
	        </md-select>
      </md-input-container>
        
       <md-button class="md-icon-button md-primary" aria-label="load" ng-click="openThresholdSidenav()">
        <md-icon md-font-icon="fa fa-external-link fa-2x"></md-icon>
      </md-button>
  </div> 


 <md-button class="md-raised" ng-click="addNewThreshold()">Add new Threshold</md-button>
<angular-table ng-show="kpi.threshold.thresholdValues.length>0" flex class="overflow relative" full-width
		id='kpiListTableThreshold' ng-model=kpi.threshold.thresholdValues
		columns='thresholdColumn'
		click-function="" 
		scope-functions="thresholdFunction"
		no-pagination=true
		speed-menu-option= thresholdTableActionButton>
		 </angular-table>
		
		
		<md-sidenav class="md-sidenav-right md-whiteframe-z2" layout="column" md-component-id="thresholdTab" >
	      <md-toolbar>
	        <h1 class="md-toolbar-tools">{{translate.load("sbi.kpi.placeholder")}}</h1>
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