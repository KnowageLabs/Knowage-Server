<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/sbi_default/css/FunctionsCatalog/functionsCatalog.css">


<md-dialog aria-label='{{"sbi.functionscatalog.executionresult.executionresult"}}' ng-cloack>

	<form>
		<div ng-if='error==""'>
	    	<md-toolbar>
		      <div class="md-toolbar-tools">
		        	<h2 ng-if="isDemo">{{translate.load("sbi.functionscatalog.executionresult.demoexecutionresult")}}</h2>
		        	<h2 ng-if="!isDemo">{{translate.load("sbi.functionscatalog.executionresult.executionresult")}}</h2> 
		        <span flex></span>
		        <md-button class="md-icon-button" ng-click="cancel()">
		        </md-button>
		      </div>
		    </md-toolbar>
		</div>    
		
		<div ng-if='error!=""'>
	    	<md-toolbar id="error-message">
		      <div class="md-toolbar-tools">
		        	<h2 ng-if="isDemo">{{translate.load("sbi.functionscatalog.executionresult.demoexecutionresult")}}</h2>
		        	<h2 ng-if="!isDemo">{{translate.load("sbi.functionscatalog.executionresult.executionresult")}}</h2> 
		        <span flex></span>
		        <md-button class="md-icon-button" ng-click="cancel()">
		        </md-button>
		      </div>
		    </md-toolbar>
		</div>    
		
		<md-dialog-content>
		    <md-tabs md-selected="selectedIndex" md-autoselect md-dynamic-height ng-if="results.length>0"> 
      			<md-tab ng-repeat="res in results" label="{{res.resultName}}">
        			 <div class="md-padding"
        			 ng-if="res.resultType.toLowerCase()=='text'">
          				{{res.result}}
        			 </div>
        			 <div class="md-padding"
        			 ng-if="res.resultType.toLowerCase()=='dataset'">
          				{{translate.load("sbi.functionscatalog.executionresult.spagobidatasetsaved")}}&nbsp;{{res.result}}
          				<br> 
      				 
       					<div ng-if="dataset.metaData.fields.length<7">
						{{translate.load("sbi.functionscatalog.executionresult.firstrowspreview")}}
						</div>
						<div ng-if = "dataset.rows != undefined && dataset.rows.length > 0 && dataset.metaData.fields.length<=25">
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
						
						<div ng-if = "dataset.rows != undefined && dataset.rows.length > 0 && dataset.metaData.fields.length>26">
							{{translate.load("sbi.workspace.dataset.datasetpreview.toomanycols")}}
						</div>

        			 </div>
        			 <div ng-if="res.resultType.toLowerCase()=='image'">
        				<img alt="{{res.resultName}}" ng-src="{{res.imageString}}"/>
        				<!--  <img src="data:image/jpeg;base64,a4$da46dgfdjtar8fevjt2..." alt="">-->        				
        			</div>
        			
        			<div ng-if="res.resultType.toLowerCase()=='file'">
        				
          					Received file: {{res.result.filename}}
          					<md-button ng-click="download(res.result.filename,res.result.base64)"> 
          						Download
		        			</md-button>
        				
        			</div>
        			
        			
        			
      			</md-tab>
   	 		</md-tabs>    	
			<div ng-if='error!=""'>
				{{error}}
				{{translate.load("sbi.functionscatalog.executionresult.seelogfileforotherinformation")}}
			</div>    	
			    	
		</md-dialog-content>

  	</form>
</md-dialog>