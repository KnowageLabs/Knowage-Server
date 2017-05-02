<md-dialog aria-label="Warning">
  <form ng-cloak>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load('sbi.generic.warning')}}</h2>
      </div>
    </md-toolbar>

    <md-dialog-content>
      <div class="md-dialog-content">
        <h3>{{translate.load('sbi.bm.check.title')}}</h3>
        <p>
		{{translate.load('sbi.bm.check.warning.message.one')}}</br>
		{{translate.load('sbi.bm.check.warning.message.two')}}</br>
		{{translate.load('sbi.bm.check.warning.message.three')}}
        </p>
         <div flex="100" layout="column">
         	
	           
	        <md-card ng-repeat="relation in incorrectRelationships">   
	             <md-card-title>
		          <md-card-title-text>
		            <b>{{relation.businessRelationshipName}}</b>
		          </md-card-title-text>
        		</md-card-title>
        		 <md-card-content layout="column" layout-align="space-between">
        		 		<div>
        		 			{{relation.sourceTableName}}  <i class="fa fa-arrow-right" aria-hidden="true"></i>  {{relation.destinationTableName}}  
        		 		</div>
						 <md-chips>
				      		<md-chip>{{translate.load('sbi.bm.check.required.columns')}}: {{relation.requiredNumberOfColumns}}</md-chip>
				    	 </md-chips>
        		 </md-card-content>

	    	</md-card>    	
	   		
   		 </div>

      </div>
    </md-dialog-content>

    <md-dialog-actions layout="row">

	  <md-button ng-click="cancel()">
	       {{translate.load('sbi.general.cancel')}}
	  </md-button>
      <md-button ng-click="saveModel()">
      	   {{translate.load('sbi.generic.save')}}
      </md-button>
    </md-dialog-actions>
  </form>
</md-dialog>