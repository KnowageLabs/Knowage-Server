
<div flex layout="column">

	<script type="text/ng-template" id="dialog1.tmpl.html">
<md-dialog aria-label="Snapshot"  style="height:90%; width:90%; max-width: 100%;  max-height: 100%;" ng-cloak>
<md-toolbar>
      <div class="md-toolbar-tools">
        <h1>Snapshot</h1>
        <span flex></span>
        <md-button class="md-icon-button" ng-click="closeFilter()">
          <md-icon md-font-icon="fa fa-times closeIcon" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
    </md-toolbar>	
<md-dialog-content flex layout="column" class="dialogFrameContent" >
			<iframe flex class=" noBorder" ng-src="{{snapshotUrl}}" name="angularIframe"></iframe> 
	</md-dialog-content> 
 </md-dialog>
	</script>

	<div ng-if=!mergePdfsInto1 flex layout="column">

	
	<md-toolbar>
	    <div class="md-toolbar-tools" >
	      	<h2 class="md-flex" >{{translate.load("sbi.execution.snapshots.title")}}</h2>
	     	<span flex></span>
	      	<md-button title="Close" aria-label="Close" class="toolbar-button-custom" 
					ng-click="returnToDocument()">
			<md-icon md-font-icon="fa fa-times"></md-icon>
		 </md-button>
		</div>
	</md-toolbar>
	<md-tabs md-dynamic-height flex=95 class="tabOverflow">
	 <md-tab ng-repeat="d in scheduler.documents" label="{{d.nameTitle}}" md-on-select="getDocumentsSnapshots(scheduler.jobName, d.name)"  >
	  	<div layout-fill layout="column" flex>
	  	
	  	<!-- Xstyle="min-height:600px" -->
	  	<angular-table 
	  		flex=70
			id="tableSchedulers_{{d.name}}" ng-model="schedulers" 
			columns='schedulatinColumns'
			columns-search='["name","description","time"]'
			initial-sorting="'time'"
			initial-sorting-asc="true"
			highlights-selected-item = "true"
			show-search-bar="true"
			speed-menu-option=downloadSnapshotSpeedMenuOption
				>
		</angular-table>
		
		</div>
	  </md-tab>
	 </md-tabs>
	 </div>
	 <div ng-if=mergePdfsInto1  flex layout="column">
	 
		 <md-toolbar flex>
		    <div class="md-toolbar-tools" flex layout-align="center center">
		      	<h2 class="md-flex" >{{translate.load("sbi.execution.snapshots.title")}}</h2>
		     	<span flex></span>
		      	<md-button title="Close" aria-label="Close" class="toolbar-button-custom" 
						ng-click="returnToDocument()">
				<md-icon md-font-icon="fa fa-times"></md-icon>
			 </md-button>
			</div>
		</md-toolbar>

		<div ng-if=mergeAndNotPDF flex layout="column">

			<angular-table flex=70 id="tableMergeSchedulersNoButton"
				ng-model=schedulationListForMerge columns='schedulatinMergeColumns'
				highlights-selected-item="true" initial-sorting="'time'"
				initial-sorting-asc="true" show-search-bar="true"
				>
			</angular-table>
		</div>

		<div ng-if=!mergeAndNotPDF flex layout="column">
			<angular-table flex=70 id="tableMergeSchedulersButton"
				ng-model=schedulationListForMerge columns='schedulatinMergeColumns'
				highlights-selected-item="true" initial-sorting="'time'"
				initial-sorting-asc="true" show-search-bar="true"
				speed-menu-option=downloadSnapshotSpeedMenuOption> </angular-table>
		</div>




	</div>
 </div>