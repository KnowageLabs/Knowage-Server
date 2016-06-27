<md-dialog aria-label="Outboung Manager"   ng-cloak style="min-width:90%; min-height:90%;">
  <md-toolbar>
      <div class="md-toolbar-tools">
        <h2>OutBound</h2>
       </div>
    </md-toolbar>
    <md-dialog-content flex>
      <div class="md-dialog-content">
   		<md-input-container class="md-block"> 
					<label>{{translate.load("sbi.meta.business.relationship.insert.name")}}</label>
					<input type="text" ng-model="businessName">
				</md-input-container>
					
				<md-input-container flex> 
						<label>{{translate.load("sbi.meta.business.relationship.cardinality")}}</label>
						<md-select ng-model="cardinalityValue"> 
							<md-option	ng-repeat="card in cardinality" value="{{card.value}}">
								{{card.name}} 
							</md-option> 
						</md-select> 
				</md-input-container>

				<div layout="row">
						<md-input-container flex id="left_ch"> 
							<label>{{translate.load("sbi.meta.business.relationship.source.business.class.name")}}</label>
							<input ng-model="selectedBusinessModel.name" disabled></input>
 						</md-input-container>
				
						<md-input-container flex id="right_ch" > 
							<label>{{translate.load("sbi.meta.business.relationship.target.business.class.name")}}</label>
							<md-select  ng-model="rightElement" ng-model-options="{trackBy: '$value.uniqueName'}"> 
								<md-option	ng-repeat="element in businessModel" ng-value="element" ng-click="alterTableToSimpleBound(element)">
									{{element.name}} 
								</md-option> 
							</md-select> 
						</md-input-container>

				</div>
			
			<associator-directive flex source-model="simpleLeft" target-model="simpleRight"  source-name="name" target-name="name" associated-item="links" translate="translate">
			</associator-directive>
			
		
			</div>
			</md-dialog-content>
			
			 <md-dialog-actions layout="row">
	             <span flex></span>
			      <md-button ng-click="cancel()">
			       Cancel
			      </md-button>
			      <md-button ng-click="createOutbound()">
			        Create
			      </md-button>
   			 </md-dialog-actions>
			
</md-dialog>