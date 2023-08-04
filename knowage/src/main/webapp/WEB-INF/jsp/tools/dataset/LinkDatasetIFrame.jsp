<md-dialog aria-label="link dataset"  style="height:100%; width:100%; max-width: 100%;  max-height: 100%;" ng-cloak>

	<md-dialog-content flex layout="column" class="dialogFrameContent" >
			
			<md-toolbar class="miniheadfederation" ng-cloak>
			
				<div layout="row" class="md-toolbar-tools">
					<h2 class="md-flex" >Table Link for {{selectedDataSet.label}} </h2>
					<span flex></span>
		       	 	
		       	 	<md-button ng-click="cancelDialog()" class="md-icon-button">
		          		<md-icon md-font-icon="fa fa-times-circle-o"></md-icon>
		       	 	</md-button>
		       	 	
	        	</div>
        	
			</md-toolbar>
			
			<iframe flex class=" noBorder" ng-src="{{iframeUrl}}" name="angularIframe"></iframe> 
			
	</md-dialog-content> 
	
 </md-dialog>