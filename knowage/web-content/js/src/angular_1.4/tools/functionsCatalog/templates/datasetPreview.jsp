<md-dialog aria-label="Dataset Preview">
	
	<md-toolbar>
	      <div class="md-toolbar-tools">
	        <h2>{{datasetLabel}} {{translate.load("sbi.functionscatalog.datasetpreview.datasetpreview")}}</h2>
	        <span flex></span>
	      </div>
	</md-toolbar>
	
	<md-dialog-content layout-padding>
	
	
		<div ng-if="truncate">
			{{translate.load("sbi.functionscatalog.datasetpreview.firstrowspreview")}}
		</div>
	
	
		<angular-table 
					id="tablePreview"
					flex
					columns="headers"
					ng-show=true
					ng-model="dataset.rows" 
					highlights-selected-item=true				
			>						
		</angular-table>		
		

	
	</md-dialog-content>
	


</md-dialog>