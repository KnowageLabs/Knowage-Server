/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Functions
  * 
  *  [list]
  * 
  * 
  * Authors
  * 
  * - Andrea Gioia (adrea.gioia@eng.it), 
  * - Marco Battaglia (marco.battaglia@quantyca.it)
  */

Ext.ns("Sbi.geo.utils");

Sbi.geo.utils.ContextFactory = function(){
 
	return {
		
		createContext : function(  ){
			var context;
			
			context = {
					
					msg: 'pippo'
					
	                , getSize: function(feature) {
	                    return 100;
	                },
	                getColor: function(feature){
					
							return 'white';
						
					},
					// getChartURL:this.targetLayerConf.myFunc;
					getWidth:function(feature){
						
						  function isIN(array, value){
							 for(current in array){
								 if (array[current] == value){
									 return true;
								 }
							 }
							 return false;
						 }
							if(!feature.layer.map.pieDimensions){
								var mapObj = feature.layer.map;
								mapObj.pieDimensions = new Object();
								//alert(feature.layer.name + ' = ' + feature.layer.features.length);
								//alert(mapObj.features + ' |-| ' + mapObj.xfeatures.length);
								
								var data =  mapObj.xfeatures;//mapObj.features;
								for(row in data){
									if(typeof(data[row])!="function"){
									var rowTotal=0;
										if(mapObj.totalField){
											rowTotal = data[row].data[mapObj.totalField];
										}
										else{
										for(field in data[row].data){
											  var currentValue = data[row].data[field];
													if(! isNaN(currentValue) && (isIN(mapObj.fieldsToShow, field))){
														rowTotal+=currentValue;
													}
												}
										}
									feature.layer.map.pieDimensions[data[row].fid] = rowTotal;
								}
							}
								var min;
								var max;
								for(value in feature.layer.map.pieDimensions){
									if(value != 'undefined'){
										var current = feature.layer.map.pieDimensions[value];
										if(!min){min=current;}
										if( current<min){
											min=current;
										}
										if(!max){max=current;}
										if(current>max){
											max=current;
										}
									}
								}
								
								var delta = max-min;
								for(fid in feature.layer.map.pieDimensions){
									var factorK = (feature.layer.map.pieDimensions[fid] - min) / delta;
									size = (30 + Math.round(70 * factorK));
		                            feature.layer.map.pieDimensions[fid+'_size'] =size;
		                          //  console.log("size: " +size );
								}
							}
							//if(feature.layer.map.zoom>6){
							//return 600;
					//	}
						//else{
							//console.log("size:" +  feature.layer.map.pieDimensions[feature.fid+'_size'] );
							return feature.layer.map.pieDimensions[feature.fid+'_size'] ;
						//}
					},
					getHeight:function(feature){
						
//						if(feature.layer.map.zoom>6){
//							return 200;
//						}
//						else{
						
							return feature.layer.map.pieDimensions[feature.fid+'_size'] ;
						//}
					},
					//getChartURL:this.targetLayerConf.myFunc,
	                getChartURLOriginal: function(feature) {
	                	 function isIN(array, value){
							 for(current in array){
								 if (array[current] == value){
									 return true;
								 }
							 }
							 return false;
						 }
	                	var mapObj = feature.layer.map;
	                	var values="";
	                	var count  = 0;
	                	var length = mapObj.fieldsToShow.length;
//	                	var total = 0;
//	                	for(var p in feature.data){
//	                		length++;
//	                		if(! isNaN(feature.data[p])){
//	                			if(p!="AREA" && p!="descriptionid"){
//	                			total+=feature.data[p];
//	                			}
//	                		}
//	                	}
	                	
	                	var label ='';
	                
	                	for(var p in feature.data){                		
	                		       		
	                		if(! isNaN(feature.data[p]) && (isIN(mapObj.fieldsToShow, p))){
	                			                count=count+1;         
		                						values += feature.data[p];
						                		label += p;                		
						                		if(count<length){
											                	   label +='|';
											                	   values+=",";
						                		}
	                		
	                		}
	                	}
	                	 var esize = 50 + (10*feature.layer.map.zoom);
	                   if(esize>540){
	                	   esize=540;
	                   }
	                   var charturl='';
	                   var color = "&chco=" + feature.layer.map.colors;
	                   var chartType = feature.layer.map.chartType;
	                    if(feature.layer.map.zoom>6){
//	                    	var valuesArray = values.split(",");
//	                    	var labelsArray = label.split("|");
//	                    	for(m=0; m<valuesArray.length;m++){
//	                    		var perc = (valuesArray[m]/total) * 100;
//	                    		labelsArray[m] = labelsArray[m] +" " + perc.toFixed(2) + "%";
//	                    	}
//	                    	label="";
//	                    	for(m=0;m<labelsArray.length;m++){
//	                    		label += labelsArray[m];
//	                    		if(m<labelsArray.length-1){
//	                    			label+="|";
//	                    		}
//	                    	}
//	                    	var chdl = '&chdl='+label;
//	                    	charturl = 'http://chart.apis.google.com/chart?cht='+chartType+'&chd=t:' + values + '&chs=' +810 + 'x' +300 + '&chf=bg,s,ffffff00'+chdl+color+'&chdls=000000,20';
//	                    
	                    	var charturl = 'http://chart.apis.google.com/chart?cht=p&chd=t:' + values + '&chds=a&chs=' + esize + 'x' + esize + '&chf=bg,s,ffffff00'+color;
	                    	}
	                    else{
	                    var charturl = 'http://chart.apis.google.com/chart?cht=p&chd=t:' + values + '&chds=a&chs=' + esize + 'x' + esize + '&chf=bg,s,ffffff00'+color;
	                    }
	                    return charturl;
	                }
	            };
			
			return context;
		}
	};
	
}();















	