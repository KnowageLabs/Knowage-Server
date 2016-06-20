<md-content layout="row" flex layout-fill ng-controller="metaModelCreationPhysicalController">
	<md-content layout="row" flex="30" class="md-whiteframe-9dp"  layout-margin >
		<component-tree id="pmTree" layout-fill style="position:absolute"
			ng-model="physicalModel"
			highlights-selected-item="true"   
			subnode-key="columns" 
			click-function="selectPhysicalModel(node)"
			hide-progress=true
			not-hide-on-load = true
			folder-icon-fn="physicalModel_getlevelIcon(node)"
			open-folder-icon-fn="getOpenFolderIcons(node)"
			is-folder-fn="physicalModel_isFolder(node)"
		></component-tree>
		
	</md-content>
	
	<md-content layout="column" flex class="md-whiteframe-9dp"   ng-if="selectedPhysicalModel.name!=undefined" >
		<md-toolbar class="md-theme-indigo">
			<h1 class="md-toolbar-tools">{{selectedPhysicalModel.name}}</h1>
		</md-toolbar>

		<md-tabs flex>
			<md-tab id="propertiestab" label="{{translate.load('sbi.udp.udpList')}}">
				<md-content layout="column"  >
				
					<expander-box layout="column" layout-margin expanded="true" title="'Misc'" background-color="transparent" color="black" >
						<md-input-container ng-repeat="prop in physicalModelMiscInfo "  >
							<label>{{prop.label}}</label>
							 <input ng-model="selectedPhysicalModel[prop.name]" disabled>
						</md-input-container>
					
					</expander-box>									
				
					<expander-box layout="column" layout-margin expanded="true" title="catProp" background-color="transparent" color="black" ng-repeat="catProp in currentPhysicalModelParameterCategories">
						<md-input-container ng-repeat="prop in selectedPhysicalModel.properties | filterByCategory:catProp"
						ng-init="prop.value.value= (prop.value.value==undefined || prop.value.value==null) ? prop.value.propertyType.defaultValue : prop.value.value">
							<label>{{prop.value.propertyType.name}}</label>
							<input ng-model="prop.value.value"  disabled>
						</md-input-container>
					</expander-box>
				</md-content>
			</md-tab>
			
			<md-tab id="fkTab" label="{{translate.load('sbi.meta.model.business.fk')}}">
			</md-tab>
		</md-tabs>
	</md-content>
</md-content>