<md-dialog aria-label="Edit document"  style="height:100%; width:100%; max-width: 100%;  max-height: 100%;" ng-cloak>

	<md-dialog-content flex layout="column" class="dialogFrameContent" >
			
			<!-- 
				Adding the toolbar that will provide user the ability to close the iframe that wraps the Federation model
				editor (interface) whenever user would like to do that. The "Close" button is available now in its own toolbar.
				@author Ana Tomic (atomic, ana.tomic@mht.net)
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)				
			 -->
			<md-toolbar class="primaryToolbar" ng-cloak ng-if="cancelDialog!=undefined">
				<div class="md-toolbar-tools " layout="row">
					
					<span flex></span>
		       	 	
		       	 	<md-button ng-click="cancelDialog()">
		          		{{translate.load('sbi.general.close')}}
		       	 	</md-button>
		       	 	
	        	</div>
        	
			</md-toolbar>	
			
			<iframe flex class=" noBorder" ng-src="{{iframeUrl}}" name="angularIframe"></iframe> 
			
	</md-dialog-content> 
	
 </md-dialog>