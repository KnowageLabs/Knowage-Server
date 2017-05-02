<md-dialog aria-label="Outboung Manager"  layout="column" ng-cloak style="width:90%; height:90%;">
  	<md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load('sbi.meta.model.business.inbound')}}</h2>
       </div>
    </md-toolbar>
  	<form name="newBMForm" layout="column" flex>
    <md-dialog-content flex> 
          <div class="md-dialog-content"> 
   			<md-input-container class="md-block"> 
					<label>{{translate.load("sbi.meta.business.relationship.insert.name")}}</label>
					<input type="text" ng-model="dataSend.name" required>
				</md-input-container>
					
				<md-input-container flex> 
						<label>{{translate.load("sbi.meta.business.relationship.cardinality")}}</label>
						<md-select ng-model="dataSend.cardinality" required> 
							<md-option	ng-repeat="card in cardinality" value="{{card.value}}">
								{{card.name}} 
							</md-option> 
						</md-select> 
				</md-input-container>

				<div layout="row">
						
						<md-input-container flex id="left_ch"> 
							<label>{{translate.load("sbi.meta.business.relationship.source.business.class.name")}}</label>						
							<md-select ng-model="rightElement" ng-model-options="{trackBy: '$value.uniqueName'}"> 
								<md-option	ng-repeat="element in businessModel" ng-value="element" ng-click="alterTableToSimpleBound(element)">
									{{element.name}} 
								</md-option> 
								<md-option	ng-repeat="element in businessViews" ng-value="element" ng-click="alterTableToSimpleBound(element)">
									{{element.name}} 
								</md-option> 
							</md-select> 
	 					</md-input-container>
	 					
						<md-input-container flex id="right_ch" > 
							<label>{{translate.load("sbi.meta.business.relationship.target.business.class.name")}}</label>							
							<input ng-model="selectedBusinessModel.name" disabled>	 </input>
						</md-input-container>
				</div>
			
			<associator-directive flex 
			source-model="simpleRight" 
			target-model="simpleLeft" 
			source-name="name" 
			target-name="name" 
			associated-item="links" 
			source-column-label="translate.load('sbi.meta.business.relationship.source.attributes')"
			target-column-label="translate.load('sbi.meta.business.relationship.target.attributes')">
			</associator-directive>
		
			</div>
			</md-dialog-content>
	
			 <md-dialog-actions layout="row">
	             <span flex></span>
			      <md-button ng-click="cancel()">
			       {{translate.load('sbi.general.cancel')}}
			      </md-button>
			      <md-button ng-click="createInbound()" ng-disabled="!newBMForm.$valid || checkData()">
			        {{translate.load('sbi.generic.create')}}
			      </md-button>
   			 </md-dialog-actions>
   		</form>
</md-dialog>