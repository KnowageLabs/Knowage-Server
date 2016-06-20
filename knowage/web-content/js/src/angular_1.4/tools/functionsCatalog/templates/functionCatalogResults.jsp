<md-dialog aria-label='{{"sbi.functionscatalog.executionresult.executionresult"}}' ng-cloack>

	<form>
    	<md-toolbar>
	      <div class="md-toolbar-tools">
	        	<h2 ng-if="!isDemo">{{translate.load("sbi.functionscatalog.executionresult.demoexecutionresult")}}</h2>
	        	<h2 ng-if="isDemo">{{translate.load("sbi.functionscatalog.executionresult.executionresult")}}</h2> 
	        <span flex></span>
	        <md-button class="md-icon-button" ng-click="cancel()">
	        </md-button>
	      </div>
	    </md-toolbar>
		
		<md-dialog-content>
		    <md-tabs md-selected="selectedIndex" md-autoselect md-dynamic-height>
      			<md-tab ng-repeat="res in results" label="{{res.resultName}}">
        			 <div class="md-padding"
        			 ng-if="res.resultType.toLowerCase()=='text'">
          				{{res.result}}
        			 </div>
        			 <div class="md-padding"
        			 ng-if="res.resultType.toLowerCase()=='dataset'">
          				{{translate.load("sbi.functionscatalog.executionresult.spagobidatasetsaved")}}&nbsp;{{res.result}}
          				
      				
       					<div ng-if="truncate">
						{{translate.load("sbi.functionscatalog.executionresult.firstrowspreview")}}
						</div>
						<div ng-if = "dataset.rows != undefined && dataset.rows.length > 0">
							<angular-table 
										id="tablePreview"
										flex
										columns="headers"
										ng-show=true
										ng-model="dataset.rows" 
										highlights-selected-item=true				
								>						
							</angular-table>		
						</div>

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