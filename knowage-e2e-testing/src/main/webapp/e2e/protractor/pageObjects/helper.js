
var Helper = function(){
	
	var elWaitTime = 10000;
	
	this.waitForElement = function(element){
		browser.wait(function(){return element.isPresent()},elWaitTime,"waiting for " + element.getWebElement());
	}
	this.waitForElementNotPresent = function(element){
		browser.wait(function(){return !element.isDisplayed()()},elWaitTime,"waiting for element" + element.getWebElement());
	}
	
	this.waitForElementToBeClickable = function(element){
		return browser.wait(protractor.ExpectedConditions.elementToBeClickable(element), elWaitTime);
	}
	this.waitForElementToBeNotClickable = function(element){
		return browser.wait(function(){
			return !protractor.ExpectedConditions.elementToBeClickable(element)
		}
				, elWaitTime);
	}
	
	this.useAngular = function(condition){
		browser.ignoreSynchronization = !condition;
	}
	
	this.getMainContent = function(){
		browser.switchTo().defaultContent();
	}
	
	this.getIframeDoc = function(){
		this.getMainContent();
		browser.switchTo().frame(0);
	}
	
	this.getDocumentViewerIframe = function(){
		this.getIframeDoc();
		browser.switchTo().frame(0);
	}
	
	
}

module.exports = new Helper();