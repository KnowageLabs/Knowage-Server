

function getMultiValueProfileAttribute(attrName, prefix, newSplit, suffix) {
	var splitter = attrName.substring(1,2);
	var valuesList = attrName.substring(3, attrName.length - 2);
	var newListOfValues="";
	var valuesArray = valuesList.split(splitter);
	var i=0;
	for (x in valuesArray){
 		if(i==0){
		  newListOfValues=newListOfValues+valuesArray[x];
 		  i=1;
 		}
 		else{
 		  newListOfValues=newListOfValues+newSplit+valuesArray[x];
 		}
	 }
	var finalResult=prefix+newListOfValues+suffix;
	return finalResult;
}


function  returnValue(valueIn){
var valueToRet="";
  valueToRet+='<ROWS>';
  valueToRet+='<ROW ';
  var newValueIn=valueIn;
  while(newValueIn.indexOf('\'')!=-1){
  	newValueIn=newValueIn.replace('\'','');
  }    
  valueToRet+='value=\''+newValueIn+'\' >';
  valueToRet+='</ROW>';
  valueToRet+='</ROWS>';
return valueToRet;
}


function  getListFromMultiValueProfileAttribute(attrValue){
  var splitter = attrValue.substring(1,2);
	var valuesList = attrValue.substring(3, attrValue.length - 2);
	var newListOfValues="";
	var valuesArray = valuesList.split(splitter);
 	 var valueToRet="";
  	valueToRet+='<ROWS>';
  	var newValue="";
  	for (x in valuesArray){
  	  newValue=valuesArray[x];
  	  while(newValue.indexOf('\'')!=-1){
  		 newValue=newValue.replace('\'','');
 		 } 
		valueToRet+="<ROW VALUE=\"" + newValue +  "\" />";
	};  
	valueToRet+='</ROWS>';
  return valueToRet;
}

function  getParameterValuesAsInOperandClause(parName, isString){
	 var toReturn = "";
	 var quote = (isString !== undefined && isString == true) ? "'" : "";
	 if (parameters.get(parName)){
		 var arValues = parameters.get(parName).split(";"); 
		 if (arValues.length > 0 ){
		      for (var i=0, l = arValues.length; i<l; i++){		    	
		    	  toReturn += quote + arValues[i] + quote;		    	  
		          if (i < l-1) toReturn += ", ";
		      }
		 }else{
			   toReturn = quote + parameters.get(parName) + quote;
		 }
	 }
	  return toReturn;
}