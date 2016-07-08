<md-dialog aria-label="Calculated field Manager"   ng-cloak style="min-width:90%; min-height:90%;">
	<form name="newCFForm" layout="column" flex>
  	<md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load('sbi.meta.model.calculatedField')}}</h2>
       </div>
    </md-toolbar>
    <md-dialog-content flex>
		<div class="md-dialog-content">
	   		
		</div>
	</md-dialog-content>
			
	 <md-dialog-actions layout="row">
            <span flex></span>
	      <md-button ng-click="cancel()">
	       {{translate.load('sbi.general.cancel')}}
	      </md-button>
	      <md-button ng-click="createCalculatedField()" >
	        {{translate.load('sbi.generic.create')}}
	      </md-button>
	 </md-dialog-actions>
		</form>		
</md-dialog>