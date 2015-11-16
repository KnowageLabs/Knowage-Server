var geoM=angular.module('geo_module');

geoM.service('thematizer',function(geo_template,geo_dataset,dataset_join_columns_item){
	var tmtz=this;
	
	
	this.getStyle = function(feature, resolution) {
		//if no indicator has been selected
		if(geo_template.selectedIndicator==undefined){
			return null;
		}
		
		var dsValue;
		var layerCol=feature.getProperties()[geo_template.layer_join_columns];
		for(var i=0;i<geo_dataset.rows.length;i++){
			if(geo_dataset.rows[i][dataset_join_columns_item.name]==layerCol){
				dsValue=geo_dataset.rows[i][geo_template.selectedIndicator.name];
				//search if there is a filter enabled
				for(var key in geo_template.selectedFilters){
					if(geo_template.selectedFilters[key]!="-1" && geo_template.selectedFilters[key]!= geo_dataset.rows[i][key]){
						console.log("filtrato");
						return null;
					}
				}
				break;
			}
		}
	
		if(geo_template.analysisType=="choropleth"){
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
		var radius={"minRadiusSize":2,"maxRadiusSize":50,color:"red"};
		
		var rad= dsValue%(radius.maxRadiusSize+1);
		if(rad<radius.minRadiusSize){
			rad=radius.minRadiusSize;
		}
		
		return  [new ol.style.Style({
		    stroke: new ol.style.Stroke({
			      color: "#000000",
			      width: 1
			    }),
			   
			  }),
			  new ol.style.Style({
			    image: new ol.style.Circle({
			        radius: rad,
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