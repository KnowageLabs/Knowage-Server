<md-content ng-controller="measureRoleMetadataController"  layout-fill>

<div layout="row" layout-wrap>

<md-whiteframe class="md-whiteframe-3dp metadataTabs" layout-margin ng-repeat=" mtd in currentMeasure.metadata">
  <md-toolbar>
      <div class="md-toolbar-tools">
		<span>{{mtd}}</span>
		</div>
</md-toolbar>
<md-content layout-margin>
 <md-input-container >
        <label>Tipologia</label>
        <md-select ng-model="cuserState">
          <md-option ng-repeat="state in states" value="{{state.abbrev}}">
            {{state.abbrev}}
          </md-option>
        </md-select>
      </md-input-container>
      
        <md-input-container>
        <label>Categoria</label>
        <md-select ng-model="userState">
          <md-option ng-repeat="state in states" value="{{state.abbrev}}">
            {{state.abbrev}}
          </md-option>
        </md-select>
      </md-input-container>
      </md-content>
</md-whiteframe>

 </div>  
  </md-content> 