<angular-list-detail ng-controller="metaModelCreationPhysicalController">
	<list label="translate.load('sbi.glossary.tables')" layout="column"> 
		<md-content class="noMargin" >
		 	<!-- PHYSICAL CLASSES -->
			<div class="metaModelBusinessList" ng-if="meta.physicalModels.length>0">
		   		<div class="md-dense" ng-repeat="pm in meta.physicalModels" >
			   		<div class="selectable" ng-click="selectPhysicalModel(pm)" layout="row" layout-align="start center" ng-class="{'selected':pm == selectedPhysicalModel}">
			   			<span class="businessListName"><md-icon md-font-icon="{{::physicalModel_getlevelIcon(pm)}}"></md-icon> {{pm.name}}</span>
				   		<span flex></span>
				     	<span class="businessListProperties">{{pm.columns.length}} properties</span>
				     	<md-button class="md-icon-button md-icon-button-32" ng-click="openBusinessModel(pm,$event)">
				     		<md-icon md-font-icon="fa fa-chevron-down"></md-icon>
				     	</md-button>
			   		</div>
			     	<md-divider ng-if="!$last"></md-divider>
			     	
					<div ng-if="openedItems.indexOf(pm.name) !== -1">
						<md-card>
							<md-card-content class="noPadding">
								<ul>
					  		<li ng-repeat-start="col in pm.columns" class="selectable" ng-click="selectPhysicalModel(col)" ng-class="{'selected':col == selectedPhysicalModel}">
					  			<md-icon md-font-icon="{{::physicalModel_getlevelIcon(col)}}"></md-icon>
					  			<span>{{col.name}}</span>
					  		</li>
					  		<md-divider ng-repeat-end ng-if="!$last"></md-divider>
					  	</ul>
							</md-card-content>
						</md-card>
						<md-divider></md-divider>
					</div>
			   	</div>
		   	</div>
	</list>
	
	<extra-list-button>
		<md-button ng-click="refreshPhysicalModel()">
   	 		{{translate.load('sbi.meta.update.physicalModel')}}
	    </md-button>
	</extra-list-button>
	
	<detail label="selectedPhysicalModel.name==undefined ? '' : selectedPhysicalModel.name "  ng-if="selectedPhysicalModel.name!=undefined" >
		<md-tabs flex>	
			<md-tab id="propertiestab" label="{{translate.load('sbi.udp.udpList')}}">
				<md-content layout="column"  >
				
					<expander-box layout="column" layout-margin expanded="true" label="'Misc'" background-color="transparent" color="black" >
						<md-input-container ng-repeat="prop in physicalModelMiscInfo "  >
							<label>{{prop.label}}</label>
							 <input ng-model="selectedPhysicalModel[prop.name]" disabled>
						</md-input-container>
					
					</expander-box>									
				
					<expander-box layout="column" layout-margin expanded="true" label="catProp" background-color="transparent" color="black" ng-repeat="catProp in currentPhysicalModelParameterCategories">
						<md-input-container ng-repeat="prop in selectedPhysicalModel.properties | filterByCategory:catProp"
						ng-init="getPropertyAttributes(prop).value= (getPropertyAttributes(prop).value==undefined || getPropertyAttributes(prop).value==null) ? getPropertyAttributes(prop).propertyType.defaultValue : getPropertyAttributes(prop).value">
							<label>{{getPropertyAttributes(prop).propertyType.name}}</label>
							<input ng-model="getPropertyAttributes(prop).value"  disabled>
						</md-input-container>
					</expander-box>
				</md-content>
			</md-tab>
			
			<md-tab ng-if="selectedPhysicalModel.columns!=undefined" id="fkTab" label="{{translate.load('sbi.meta.model.business.fk')}}">
				<angular-table  id="fktable" ng-model="selectedPhysicalModel.foreignKeys" columns="fkTableColumns"  no-pagination=true flex>
				</angular-table>
			
			</md-tab>
		</md-tabs>
	</detail>
</angular-list-detail>