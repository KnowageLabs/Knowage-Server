var helper = require('pageObjects/helper');
var MainToolbar = function(){
	
	var saveButton = element(by.css('button[ng-click="saveQbeDocument()"]'));
	var closeButton = element(by.css('button[ng-click="closeDocument()"]'));
	
	
	this.getSaveButton = function(){
		helper.waitForElement(saveButton);
		return saveButton;
	}
	
	this.getCloseButton = function(){
		helper.waitForElement(closeButton);
		return closeButton;
	}


}

module.exports = new MainToolbar();