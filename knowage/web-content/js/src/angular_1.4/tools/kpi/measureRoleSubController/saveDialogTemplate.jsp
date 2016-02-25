<md-dialog aria-label="Save measure" style="height:80%;width:80%" ng-cloak>
 
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h2>Salvataggio</h2>
        <span flex></span>
	      <md-button ng-click="cancel()">
	      Annulla
	      </md-button>
	      <md-button ng-click="save()"  >
	      Salva
	      </md-button>
       </div>
    </md-toolbar>
    <md-dialog-content flex layout="column"  >
    
        <md-input-container class="md-block">
            <label>Nome</label>
            <input ng-model="currentRole.name">
          </md-input-container>
      
      <div layout="row" flex layout-wrap >
       <div flex>
       	 <md-toolbar md-scroll-shrink ng-if="true"  >
		    <div class="md-toolbar-tools">
		      <h3>
		        <span>Alias that will be insert after saving</span>
		      </h3>
		    </div>
		  </md-toolbar>
		  <md-content flex>
			<md-list>
  		    	<md-list-item   ng-repeat="item in newAlias">
  		    	{{item}}
  		    	</md-list-item>
  		    </md-list>
		  </md-content>
       </div>
       
        <div flex>
       	 <md-toolbar md-scroll-shrink ng-if="true"  >
		    <div class="md-toolbar-tools">
		      <h3>
		        <span>Alias that will be reused</span>
		      </h3>
		    </div>
		  </md-toolbar>
		  <md-content flex>
			<md-list>
  		    	<md-list-item   ng-repeat="item in presentAlias">
  		    	{{item}}
  		    	</md-list-item>
  		    </md-list>
		  </md-content>
       </div>
       
       
      </div>
    
           
    </md-dialog-content> 
 
</md-dialog>