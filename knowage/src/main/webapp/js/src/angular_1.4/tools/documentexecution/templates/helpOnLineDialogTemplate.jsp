<md-dialog md-theme="{{::lookupParamCtrl.theme }}" style="height:90% ;width:90%;" layout="column">
	<md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load("sbi.generic.helpOnLine")}}</h2>
        <span flex></span>
        <md-button class="" ng-click="close()">
         {{translate.load("sbi.general.close")}}
        </md-button>
      </div>
        
        
    </md-toolbar>
    <md-dialog-content flex  style=" position:relative ; "> 
     <iframe style="position:absolute;    border: 0px;" width="100%" height="100%" name="helpOnline" ng-src="{{url}}"></iframe>
    </md-dialog-content>
</md-dialog>