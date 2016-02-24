<md-content ng-controller="measureRoleMetadataController"  layout-fill>

<div layout="row" layout-wrap>

<md-whiteframe class="md-whiteframe-3dp metadataTabs" layout-margin ng-repeat=" (mtd,mtdValue) in currentMeasure.metadata ">
  <md-toolbar>
      <div class="md-toolbar-tools"  class="alertIconMissingAlias" > 
         <span ng-if="!aliasExtist(mtd)">
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
        <md-select ng-model="mtdValue.tipology" selected="{{mtdValue.tipology=mtdValue.tipology || 'Attribute'}}"  >
          <md-option ng-repeat="tipolo in tipologiesType" value={{tipolo.value}}>
            {{tipolo.label}}
          </md-option>
        </md-select>
      </md-input-container>
      
        <md-input-container ng-if="mtdValue.tipology=='TemporalAttribute'">
        <label>Livello gerarchico</label>
        <md-select ng-model="mtdValue.hierarchicalLevel">
          <md-option ng-repeat="hlevel in hierarchicalLevelList" value="{{hlevel.valueId}}">
            {{hlevel.valueName}}
          </md-option>
        </md-select>
      </md-input-container>
      
	<md-autocomplete ng-if="mtdValue.tipology=='Attribute'"
          ng-disabled="false" 
          md-selected-item="mtdValue.category" 
          md-search-text="searchText" 
          md-items="item in querySearchCategory(searchText)"
          md-item-text="item.valueCd" 
          md-floating-label="Categoria">
        <md-item-template>
          <span md-highlight-text="searchText">{{item.valueCd}}</span>
        </md-item-template> 
      </md-autocomplete>      
      
      
      </md-content>
</md-whiteframe>

 </div>  
  </md-content> 