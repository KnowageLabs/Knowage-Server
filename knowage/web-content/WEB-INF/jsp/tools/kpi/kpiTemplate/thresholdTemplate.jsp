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
					<md-button   ng-click="scopeFunctions.addNewThreshold()">Add new Threshold item</md-button>
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

<script type="text/template" id="dialogReusedThreshold.html">
<md-dialog aria-label="add thresh "  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h2>Mango (Fruit)</h2>
        <span flex></span>
        <md-button class="md-icon-button" ng-click="cancel()">
          <md-icon md-svg-src="img/icons/ic_close_24px.svg" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
    </md-toolbar>
    <md-dialog-content>
      <div class="md-dialog-content">
        <h2>Using .md-dialog-content class that sets the padding as the spec</h2>
        <p>
          The mango is a juicy stone fruit belonging to the genus Mangifera, consisting of numerous tropical fruiting trees, cultivated mostly for edible fruit. The majority of these species are found in nature as wild mangoes. They all belong to the flowering plant family Anacardiaceae. The mango is native to South and Southeast Asia, from where it has been distributed worldwide to become one of the most cultivated fruits in the tropics.
        </p>
        <img style="margin: auto; max-width: 100%;" alt="Lush mango tree" src="img/mangues.jpg">
        <p>
          The highest concentration of Mangifera genus is in the western part of Malesia (Sumatra, Java and Borneo) and in Burma and India. While other Mangifera species (e.g. horse mango, M. foetida) are also grown on a more localized basis, Mangifera indica&mdash;the "common mango" or "Indian mango"&mdash;is the only mango tree commonly cultivated in many tropical and subtropical regions.
        </p>
        <p>
          It originated in Indian subcontinent (present day India and Pakistan) and Burma. It is the national fruit of India, Pakistan, and the Philippines, and the national tree of Bangladesh. In several cultures, its fruit and leaves are ritually used as floral decorations at weddings, public celebrations, and religious ceremonies.
        </p>
      </div>
    </md-dialog-content>
    <md-dialog-actions layout="row">
      <md-button href="http://en.wikipedia.org/wiki/Mango" target="_blank" md-autofocus>
        More on Wikipedia
      </md-button>
      <span flex></span>
      <md-button ng-click="answer('not useful')">
       Not Useful
      </md-button>
      <md-button ng-click="answer('useful')" style="margin-right:20px;">
        Useful
      </md-button>
    </md-dialog-actions>
  </form>
</md-dialog>
</script>