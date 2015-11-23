var geoM=angular.module('geoModule');

geoM.service('geoModule_thematizer',function(geoModule_template,geoModule_dataset,geModule_datasetJoinColumnsItem){
	var tmtz=this;
	var cacheProportionalSymbolMinMax={};
	
	
	this.getStyle = function(feature, resolution) {
		//if no indicator has been selected
		if(geoModule_template.selectedIndicator==undefined){
			return null;
		}
		
		var dsValue;
		var layerCol=feature.getProperties()[geoModule_template.layerJoinColumns];
		for(var i=0;i<geoModule_dataset.rows.length;i++){
			if(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name]==layerCol){
				dsValue=geoModule_dataset.rows[i][geoModule_template.selectedIndicator.name];
				//search if there is a filter enabled
				for(var key in geoModule_template.selectedFilters){
					if(geoModule_template.selectedFilters[key]!="-1" &&  geoModule_template.selectedFilters[key].length!=0 && geoModule_template.selectedFilters[key].indexOf(geoModule_dataset.rows[i][key])==-1){
						console.log("filtrato");
						return null;
					}
				}
				break;
			}
		}
	
		if(geoModule_template.analysisType=="choropleth"){
			return tmtz.choropleth(dsValue);
		}else{
			return tmtz.proportionalSymbol(dsValue);
		}
	}
	
	this.choropleth=function(dsValue){
		var legend=[{start:0,end:100,color:"#8CF703"},{start:100,end:1000,color:"#F1F703"},{start:1000,end:10000,color:"#F7AE03"},{start:10000,end:100000,color:"#F70303"}];
		var color;
		for(var i=0;i<legend.length;i++){
			if(dsValue>=legend[i].start && dsValue<legend[i].end){
				color=legend[i].color;
				break;
			}
		}
		
		if(color==undefined){color=legend[legend-1].color;}
		
		return  [new ol.style.Style({
		    stroke: new ol.style.Stroke({
		      color: "#000000",
		      width: 1
		    }),
		    fill: new ol.style.Fill({
		      color: color
		    })
		  })];
		
	}
	
	this.proportionalSymbol=function(dsValue){
		//calc  max and min value if they arent' present in cacheProportionalSymbolMinMax  
		if(!cacheProportionalSymbolMinMax.hasOwnProperty(geoModule_template.selectedIndicator.name)){
			var minV;
			var maxV;
			for(var i=0;i<geoModule_dataset.rows.length;i++){
					var tmpV= parseInt(geoModule_dataset.rows[i][geoModule_template.selectedIndicator.name]);
					if(minV==undefined || tmpV<minV){
						minV=tmpV;
					}
					if(maxV==undefined || tmpV>maxV){
						maxV=tmpV;
					
				}
			}
			cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name]={minValue:minV, maxValue:maxV};
		}
		
		var radius={"minRadiusSize":2,"maxRadiusSize":50,color:"red"};
		 var minValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].minValue;
         var maxValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].maxValue;
         var size;
		
		if(minValue == maxValue) { // we have only one point in the distribution
       	 size = (radius.maxRadiusSize + radius.minRadiusSize)/2;
        } else {
       	 size = ( parseInt(dsValue) - minValue) / ( maxValue - minValue) *
            (radius.maxRadiusSize - radius.minRadiusSize) + radius.minRadiusSize;
        }
		
		
		
		return  [new ol.style.Style({
		    stroke: new ol.style.Stroke({
			      color: "#000000",
			      width: 1
			    }),
			   
			  }),
			  new ol.style.Style({
			    image: new ol.style.Circle({
			        radius: size,
			        stroke: new ol.style.Stroke({
					      color: "#000000",
					      width: 1
					    }),
					    
			        fill: new ol.style.Fill({
			          color: radius.color
			        })
			      }),
			      geometry: function(feature) {
			        // return the coordinates of the first ring of the polygon
			        var coordinates = feature.getGeometry().getInteriorPoints().getCoordinates()[0];
			        return new ol.geom.Point(coordinates);
			      }
			    })];
	}


});