<md-dialog aria-label="Demo Execution Result">

	<form>
    	<md-toolbar>
	      <div class="md-toolbar-tools">
	        <h2>Demo Execution Result</h2>
	        <span flex></span>
	        <md-button class="md-icon-button" ng-click="cancel()">
	        </md-button>
	      </div>
	    </md-toolbar>
		
		<md-dialog-content  style="max-width:800px;max-height:810px ">
		    <md-tabs md-selected="selectedIndex" md-autoselect md-dynamic-height md-dynamic-width>
      			<md-tab ng-repeat="res in results" label="{{res.resultName}}">
        			 <div class="md-padding"
        			 ng-if="res.resultType.toLowerCase()!='image'">
          				{{res.result}}
        			 </div>
        			 <div ng-if="res.resultType.toLowerCase()=='image'">
        				<img alt="{{res.resultName}}" ng-src="{{res.imageString}}"/>
        				<!--  <img src="data:image/jpeg;base64,a4$da46dgfdjtar8fevjt2..." alt="">-->
        			</div>
      			</md-tab>
   	 		</md-tabs>	    	
			    	
			    	
		</md-dialog-content>

  	</form>
</md-dialog>