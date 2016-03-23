<md-dialog aria-label="Edit document"  style="height:95%; width:95%; max-width: 100%;  max-height: 100%;" ng-cloak>
 
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load("sbi.general.save.in.progress")}}</h2>
        <span flex></span>
	      <md-button ng-click="cancel()">
	      {{translate.load("sbi.general.cancel")}}
	      </md-button>
	      <md-button ng-click="save()"  >
	     {{translate.load("sbi.generic.update")}}
	      </md-button>
       </div>
    </md-toolbar>
    <md-dialog-content flex layout="column"  >
 		<iframe flex class=" noBorder" ng-src="{{editDocumentUrl}}"> </iframe>
    </md-dialog-content> 
 
</md-dialog>