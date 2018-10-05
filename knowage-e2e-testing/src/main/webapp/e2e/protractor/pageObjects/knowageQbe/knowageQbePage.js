var helper = require('pageObjects/helper');
var KnowageQbePage = function(){
	var mainToolbar =  require('./mainToolbar/mainToolbar');
	var qbeEditor =  require('./editor/editor');
	
	this.getMainToolbar = function(){
		helper.getIframeDoc();
		return mainToolbar;
	}
	
	this.getEditor = function(){
		helper.getDocumentViewerIframe();
		helper.useAngular(true);
//		browser.waitForAngular();
		
		return qbeEditor;
	}

}

module.exports = new KnowageQbePage();