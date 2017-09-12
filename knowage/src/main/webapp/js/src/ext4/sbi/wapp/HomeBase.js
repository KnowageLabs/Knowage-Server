
var	 firstUrlTocallvar;
var	 win_info_1;	

function execDirectUrl (url, path){
		this.mainframe.load(url);
		this.titlePath.setTitle(path);
		return;
}

function execDirectDoc(btn){
	var url = "";
	var idMenu = btn.id;
	var path=btn.path;
	
	if (idMenu != null && idMenu != 'null'){
		url =   "'<%=contextName%>/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID="+idMenu+"'" ;
		this.mainframe.load(url);
	}
	return;
 }

function getFunctionality (btn){
	var url = btn.url;
	var path=btn.path;
	execDirectUrl(url, path);
	return;
 }
 
function readHtmlFile(btn){
	var url = "";
 	var idMenu = btn.id;
 	var path = btn.path;

 	 if (idMenu != null && idMenu != 'null'){
		url =  "'<%=contextName%>/servlet/AdapterHTTP?ACTION_NAME=READ_HTML_FILE&MENU_ID="+idMenu+"'";
		this.mainframe.load(url);

	}
	return;
}

function callExternalApp(url, path){
	url = getExternalAppUrl(url);
	this.mainframe.load(url);
	this.titlePath.setTitle(path);
	return;
}

function getExternalAppUrl(url){
	if (!Sbi.config.isSSOEnabled) {
		if (url.indexOf("?") == -1) {
			url += '?<%= SsoServiceInterface.USER_ID %>=' + Sbi.user.userUniqueIdentifier;
		} else {
			url += '&<%= SsoServiceInterface.USER_ID %>=' + Sbi.user.userUniqueIdentifier;
		}
	}
	return url;
}


function execUrl(url){
	document.location.href=url;
	return;
}

//returns the language url to be called in the language menu
function getLanguageUrl(config){
  var languageUrl = "javascript:execUrl('"+Sbi.config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=CHANGE_LANGUAGE&LANGUAGE_ID="+config.language+"&COUNTRY_ID="+config.country+"&THEME_NAME="+config.currTheme+"')";
  return languageUrl;
}

function roleSelection(){
	if(Sbi.user.roles && Sbi.user.roles.length > 1){
		this.win_roles = new Sbi.home.DefaultRoleWindow({'SBI_EXECUTION_ID': ''});
		this.win_roles.show();
	}

}
function info(){

	if(!win_info_1){

		win_info_1= new Ext.Window({
			frame: false,
			style:"background-color: white",
			id:'win_info_1',
			autoLoad: {url: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/infos.jsp'},             				
			layout:'fit',
			width:210,
			height:180,
			closeAction:'hide',
			//closeAction:'close',
			buttonAlign : 'left',
			plain: true,
			title: LN('sbi.home.Info')
		});
	}		
	win_info_1.show();
  }
  
function goHome(html, path){
	var url;
	if(!html){
		url = firstUrlTocallvar;
	}else{
		url = Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+html;
	}
	if(url){
		execDirectUrl(url, path);
	}
	
}