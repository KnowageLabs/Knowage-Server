

<md-dialog aria-label="unused columns"  layout="column" ng-cloak >
  	<md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load('sbi.meta.model.business.add.unused.fields')}}</h2>
       </div>
    </md-toolbar>
  	
    <md-dialog-content > 
    <div layout="row" layout-align="center center" ng-if="!hasUnUsedColumns()">
				<div class="kn-noItems" flex="60" flex-xs="100">
					<p class="ng-binding">{{translate.load('sbi.no.columns.to.add')}}</p>
				</div>
	</div>
    <md-card ng-if="hasUnUsedColumns()">
    
    	<md-card-content>
    	
   			<div ng-repeat="column in unUsedColumns" >
        	
   				<div layout="row">
   					<p>{{column.name}}</p>
   					<span flex></span>
  				 	<md-checkbox ng-model="column.selected"></md-checkbox>
   				</div>
  				 
 			
        </div>
 		</md-card-content>
    
    </md-card>
    
    
        
	</md-dialog-content>
	
			 <md-dialog-actions layout="row">
	             <span flex></span>
			      <md-button ng-click="cancel()">
			       {{translate.load('sbi.generic.cancel')}}
			      </md-button>
			      <md-button ng-click="save()" ng-if="hasUnUsedColumns()">
			       {{translate.load('sbi.generic.save')}}
			      </md-button>
   			 </md-dialog-actions>
   		
</md-dialog>