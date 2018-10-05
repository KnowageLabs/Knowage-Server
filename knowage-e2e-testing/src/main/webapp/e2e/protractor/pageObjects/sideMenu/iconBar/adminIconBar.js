var helper = require('pageObjects/helper');
var workspace = require('pageObjects/workspace/workspace');
var AdminIconBar = function(){
	
	var workspaceIcon = element(by.linkText('work'));
	var loadingMask = element(by.css('.loadingMask'));
	
	
	this.getWorkSpace = function(){
		helper.waitForElementToBeClickable(workspaceIcon);
		workspaceIcon.click().then(function(){
			helper.getIframeDoc();
			helper.useAngular(false);
			//helper.waitForElement(loadingMask);
			//helper.waitForElementNotPresent(loadingMask);
		});
		
		return workspace;
	}
	
	
}

module.exports = new AdminIconBar();