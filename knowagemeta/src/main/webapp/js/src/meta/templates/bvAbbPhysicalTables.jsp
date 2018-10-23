<md-dialog aria-label="business view add physical tables"   ng-cloak style="min-width:90%; min-height:90%;">
  	<form name="newBMForm" layout="column" flex>
  	<md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load('sbi.meta.businessview.add.physical.tables')}}</h2>
       </div>
    </md-toolbar>
    <md-dialog-content flex>
    	<div class="md-dialog-content"> 
   			<angular-table ng-model="physicalTables" columns="ptColumns" selected-item="selectedPhysicalTables" multi-select=true show-search-bar="true" no-pagination="true"></angular-table>
		</div>
	</md-dialog-content>
	
	<md-dialog-actions layout="row">
	     <span flex></span>
	     <md-button ng-click="cancel()">
	      {{translate.load('sbi.general.cancel')}}
	     </md-button>
	     <md-button ng-click="save()"  >
	       {{translate.load('sbi.general.add')}}
	     </md-button>
	</md-dialog-actions>
   		</form>
</md-dialog>