/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 


 
  
// authentication function
function authenticate(spagobiContext,userId,password){

	try
  	{// Firefox, Opera 8.0+, Safari, IE7
  		xmlHttp=new XMLHttpRequest();
 		 }
		catch(e)
  		{// Old IE
 	 try
    {
    xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
  catch(e)
    {
    alert ("Your browser does not support XMLHTTP!");
    return 'KO';  
    }
  }
  
 try{ 
  var authenticationUrl=spagobiContext+'/servlet/AdapterHTTP?ACTION_NAME=LOGIN_ACTION_WEB&NEW_SESSION=TRUE&user='+userId+'&password='+password;
	xmlHttp.open('POST',authenticationUrl,false);										
	xmlHttp.send(null);
  }
  catch(e)
    {
    alert ("Could not do the authentication "+e);
    return 'KO';  
    }


	if(xmlHttp.responseText=='KO') return 'KO';
	else return 'OK';
}


function executeDoc(spagobiContext
					,documentId
					,documentLabel
					,executionRole
					,parametersStr
					,parametersMap
					,displayToolbar
					,displaySliders
					,iframeStyle
					,theme) {
	var url=spagobiContext+'/servlet/AdapterHTTP?PAGE=ExecuteBIObjectPage&NEW_SESSION=true&MODALITY=SINGLE_OBJECT_EXECUTION_MODALITY&IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS=true';

	if (documentId==null && documentLabel==null) {
  		alert('documentId e documentLabel nulli');
  		return;
	}

	if (documentId != null) {
			url+='&OBJECT_ID=' + documentId;
	} 
	else {
			url+='&OBJECT_LABEL=' + documentLabel;
	}

	if (parametersStr != null) {
			url+='&PARAMETERS=' + parametersStr;
	}

	// Get the parameterMap
	if (parametersMap != null) {
			for(prop in parametersMap) {
			 if(typeof parametersMap[prop]=='string'){
				url+='&'+prop+'='+parametersMap[prop];
			 }
			 if(typeof parametersMap[prop]=='number'){
					url+='&'+prop+'='+parametersMap[prop];			 
			}			 
		}

	}
	

	if (executionRole != null) url+='&ROLE='+executionRole;
	if (displayToolbar != null) url+='&TOOLBAR_VISIBLE='+displayToolbar;
	if (displaySliders != null) url+='&SLIDERS_VISIBLE='+displaySliders;
	if (theme != null)	url+='&theme='+theme;

	// once finished the url build the HTML
	
	if(iframeStyle==null){
		iframeStyle="";
	}
	alert(url);
	
	var htmlCode='<iframe id="frame" src="'+url+'" style="'+iframeStyle+'" width="500" height="500"></iframe>'

	return htmlCode;
}