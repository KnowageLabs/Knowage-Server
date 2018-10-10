<angular-list-detail ng-controller="metaModelCreationPhysicalController">
	<list label="translate.load('sbi.glossary.tables')" layout="column"> 
		<span ng-if="meta.physicalModels.length>0">
			<component-tree id="pmTree"  style="margin:0px"
				ng-model="meta.physicalModels"
				highlights-selected-item="true"   
				subnode-key="columns" 
				click-function="selectPhysicalModel(node)"
				hide-progress=true
				not-hide-on-load = true
				folder-icon-fn="physicalModel_getlevelIcon(node)"
				open-folder-icon-fn="physicalModel_getlevelIcon(node)"
				is-folder-fn="physicalModel_isFolder(node)"
				expand-on-click=false
				interceptor="physicalModelTreeInterceptor"
			></component-tree>
		</span>
	</list>
	
	<extra-list-button>
		<md-menu>
			<md-button aria-label="Create" class="md-fab" ng-click="$mdOpenMenu($event)">
			  <md-icon md-menu-origin  md-font-icon="fa fa-bars" class="md-primary"></md-icon>
			</md-button>
			<md-menu-content width="4">
			  <md-menu-item>
			    <md-button ng-click="refreshPhysicalModel()">
			      <md-icon md-font-icon="fa fa-download" md-menu-align-target></md-icon>
			    	 {{translate.load('sbi.meta.update.physicalModel')}}
			    </md-button>
			  </md-menu-item>
			</md-menu-content>
		</md-menu>
		
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