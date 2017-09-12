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
			<iframe flex class=" noBorder" ng-src="{{urlViewPointService.snapshotUrl}}" name="angularIframe"></iframe> 
	</md-dialog-content> 
 </md-dialog>
</script>

<md-toolbar layout="row">
    <div class="md-toolbar-tools" flex layout-align="center center">
      	<h2 class="md-flex" >{{translate.load("sbi.execution.snapshots.title")}}</h2>
     	<span flex></span>
      	<md-button title="Close" aria-label="Close" class="toolbar-button-custom" 
				ng-click="paramRolePanelService.returnToDocument()">
		{{translate.load("sbi.general.close")}} 
	 </md-button>
	</div>
</md-toolbar>
<angular-table    flex
	id="tableSchedulers" ng-model="urlViewPointService.gvpCtrlSchedulers" 
	columns='column'
	columns-search='["name","description","dateCreation"]'
	highlights-selected-item = "true"
	show-search-bar="true"
	speed-menu-option=gvpCtrlSchedulerMenuOpt	>
</angular-table>