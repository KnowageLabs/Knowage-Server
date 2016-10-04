
'use strict';
 
/* https://github.com/angular/protractor/blob/master/docs/getting-started.md */
 
describe('my app', function() {
 
  beforeEach(function () {
    
  });
 
  it('should try to login', function() {
	browser.ignoreSynchronization=true; 
   browser.get('/knowage');
   browser.driver.findElement(by.id('userID')).sendKeys('biadmin');
   browser.driver.findElement(by.id('password')).sendKeys('biadmin');
   browser.driver.findElement(by.css('.btn-signin')).click();
  });
  
 it('should click on hamburger', function() {

   element(by.css('.menuKnowage')).click();
   browser.sleep(3000);
  });
  
 it('should click on mydata', function() {
	 

	element(by.css('a[title="Documents development"]')).click();
	browser.waitForAngular();
  });

  it('should click on generic', function() {

	browser.driver.switchTo().frame(driver.findElement(By.id("iframeDoc")));
	browser.driver.actions().click(driver.findElement(By.id('newDocument'))).perform();
	browser.driver.findElement(By.xpath("//li[@id='STANDARD']/a")).click();
	browser.sleep(3000);
  });
});

