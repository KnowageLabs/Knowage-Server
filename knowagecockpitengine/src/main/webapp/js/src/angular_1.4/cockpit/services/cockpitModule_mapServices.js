angular.module("cockpitModule").service("cockpitModule_mapServices",
		function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q, $mdPanel,cockpitModule_widgetSelection,cockpitModule_properties,cockpitModule_utilstServices, $rootScope){
	
	var ms = this; //mapServices
	
	ms.getFeaturesDetails = function(geoColumn, values){
		if (values != undefined){
			var geoFieldName;
			var geoFieldValue;
			var featuresSource = new ol.source.Vector();

			
			for(var k=0; k < values.metaData.fields.length; k++){
				var field = values.metaData.fields[k];
				if (field.header === geoColumn){
					geoFieldName = field.name;
					break;
				}
			}
			if (geoFieldName){
				for(var r=0; r < values.rows.length; r++){
					var lonlat;
					var row = values.rows[r];
					geoFieldValue = row[geoFieldName].trim();
					if (geoFieldValue.indexOf(" ")){
						lonlat = geoFieldValue.split(" ");
					}else if (geoFieldValue.indexOf(",")){
						lonlat = geoFieldValue.split(",");
					}else{
						console.log("Error getting longitude and latitude from column value ["+ geoFieldValue +"]");
						return null;
					}
					var transform = ol.proj.getTransform('EPSG:4326', 'EPSG:3857');
					var feature = new ol.Feature();  //add columns value like properties
			        var coordinate = transform([parseFloat(lonlat[0].trim()), parseFloat(lonlat[1].trim())]);
			        var geometry = new ol.geom.Point(coordinate);
			        feature.setGeometry(geometry);
//			        feature.setProperties({'name':'abcd', 'description':'xyz'})
			        feature.set('name', 'abcd');
			        featuresSource.addFeature(feature);
				}
//				 var featuresStyle = new ol.style.Style({
//		              fill: new ol.style.Fill({
//		                color :  "#"+((1<<24)*Math.random()|0).toString(16) //random color
//		              }),
//		              stroke: new ol.style.Stroke({
//		                color: '#ffcc33',
//		                width: 2
//		              }),
//		              image: new ol.style.Circle({
//		                radius: 7,
//		                fill: new ol.style.Fill({
//		                 color :  "#"+((1<<24)*Math.random()|0).toString(16) //random color
//		                })
//		              }),
//		              text: new ol.style.Text({
//		                  font: '12px Calibri,sans-serif',
//		                  fill: new ol.style.Fill({ color: '#000' }),
//		                  stroke: new ol.style.Stroke({
//		                    color: '#fff', width: 2
//		                  }),
//		                  // get the text from the feature - `this` is ol.Feature
//		                 // text: this.get('description')
//		                  text: "BELLA LI'"
//		                })
//		            });
//				return new ol.layer.Vector({
//				      source: featuresSource,
//					  style: featuresStyle
//				    });
				 var clusterSource = new ol.source.Cluster({
				        distance: 10,
				        source: featuresSource
				      });

			    var styleCache = {};
				return new ol.layer.Vector({
				        source: clusterSource,
				        style: function(feature) {
				          var size = feature.get('features').length;
				          var textValue = "";
				          for (var f=0; f<size; f++){
				        	  var tmpFeature = feature.get('features')[f];
				        	  var props  = tmpFeature.getProperties();
				        	  textValue =  props["name"];
				          }	         
				          var style = styleCache[size];
				          if (!style) {
				            style = new ol.style.Style({
				              image: new ol.style.Circle({
				                radius: 10,
				                stroke: new ol.style.Stroke({
				                  color: '#fff'
				                }),
				                fill: new ol.style.Fill({
				                  color: '#3399CC'
				                })
				              }),
				              text: new ol.style.Text({
//				                text: size.toString(),
				                text: textValue.toString(),
				                fill: new ol.style.Fill({
				                  color: '#fff'
				                })
				              })
				            });
				            styleCache[size] = style;
				          }
				          return style;
				        }
				      });
			}
		}
		return new ol.layer.Vector();
	} 
	
	ms.getBaseLayer = function (conf){
		
		var toReturn;
		
		//check input configuration
		if (!conf || !conf.type){
			conf = {type:""}; //default case
		}
	
		switch(conf.type) {
		    case "OSM": case "OAM":
		    	toReturn = new ol.layer.Tile({
		   	    	visible: true,
		   	    	source: new ol.source.OSM({
			   	    	url:conf.url,
			   	    	attributions: conf.attributions || "",
		   	    		})
					});
		        break;
		    case "Stamen":
		    	//toner-hybrid, toner, toner-background, toner-hybrid, toner-labels, toner-lines, toner-lite,terrain, terrain-background, terrain-labels, terrain-lines
		    	toReturn = new ol.layer.Tile({
		   	    	visible: true,
		   	    	source: new ol.source.Stamen({
			   	    	layer: conf.layer,
		   	    		})
					});
		        break;
		    case "XYZ": //generic tiles (ex. for carto)
		    	toReturn = new ol.layer.Tile({
		   	    	visible: true,
		   	    	source: new ol.source.XYZ({
			   	    	url:conf.url,
			   	    	attributions: conf.attributions || "",
		   	    		})
					});
		    	break;
		    default: //OSM
		    		toReturn = new ol.layer.Tile({
			    		visible: true,
			      	    source: new ol.source.OSM()
			      	});
		}
      
		return toReturn;
		
	}

	
});