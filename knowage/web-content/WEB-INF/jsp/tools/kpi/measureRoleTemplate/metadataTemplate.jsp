<md-content ng-controller="measureRoleMetadataController"  layout-fill>

<div layout="row" layout-wrap> 
<md-whiteframe class="md-whiteframe-3dp metadataTabs" layout-margin ng-repeat=" (mtd,mtdValue) in currentRule.metadata ">
  <md-toolbar>
      <div class="md-toolbar-tools"  class="alertIconMissingAlias" > 
         <span ng-if="::!aliasExtist(mtd)">
	  		 <md-tooltip md-direction="top">
		          L'alias non esiste e verrà aggiunto
		 	</md-tooltip>  
        	<md-icon  class="alertIconMissingAlias"  md-font-icon="fa fa-exclamation-triangle" > 
       		</md-icon> 
         </span>
		<span flex>{{mtd}}</span>
		</div>
      
		 
</md-toolbar> 
<md-content layout-margin>
 <md-input-container >
        <label>Tipologia</label>
        <md-select ng-model="mtdValue.type" ng-model-options="{trackBy: '$value.valueCd'}"  >
          <md-option ng-repeat="tipolo in tipologiesType" ng-value={{tipolo}}>
            {{translate.load(tipolo.translatedValueName)}}
          </md-option>
        </md-select>
      </md-input-container> 
        <md-input-container ng-if="mtdValue.type.valueCd=='TEMPORAL_ATTRIBUTE'">
        <label>Livello gerarchico</label>
        <md-select ng-model="mtdValue.hierarchicalLevel">
          <md-option ng-repeat="hlevel in hierarchicalLevelList" ng-value="{{hlevel}}">
            {{hlevel.valueName}}
          </md-option>
        </md-select>
      </md-input-container>
      
	<md-autocomplete ng-if="mtdValue.type.valueCd!='TEMPORAL_ATTRIBUTE'"
          ng-disabled="false" 
          md-selected-item="mtdValue.category" 
          md-search-text="searchText" 
          md-items="item in querySearchCategory(searchText)"
          md-item-text="item.valueCd" 
          md-floating-label="Categoria"
          md-autoselect	="true"
         >
        <md-item-template>
          <span md-highlight-text="searchText">{{item.valueCd}}</span>
        </md-item-template> 
      </md-autocomplete>      
      
      
      </md-content>
</md-whiteframe>

 </div>  
  </md-content> 