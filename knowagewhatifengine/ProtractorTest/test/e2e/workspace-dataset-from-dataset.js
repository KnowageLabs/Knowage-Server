describe('my app', function() {
 
  beforeEach(function () {
    
  });
  
  var name;

  it('should try to login', function() {
	browser.ignoreSynchronization=true; 
   browser.get('/knowage');
   browser.driver.findElement(by.id('userID')).sendKeys('biadmin');
   browser.driver.findElement(by.id('password')).sendKeys('biadmin');
   browser.driver.findElement(by.css('.btn-signin')).click();
  });
  
 it('should click on hamburger menu', function() {

   element(by.css('.menuKnowage')).click();
   browser.sleep(1000);
  });
 
 it('should open Workspace',function(){
    //browser.driver.findElement(protractor.By.linkText('Business Models catalog')).click();
    element(by.css('[title="Workspace"]')).click();
    browser.sleep(5000);
 });

  it('should open data',function(){
    //browser.driver.findElement(protractor.By.linkText('Business Models catalog')).click();
    var driver = browser.driver;
    var loc = by.tagName('iframe');
    var el = driver.findElement(loc);
    browser.switchTo().frame(el);

    element(by.css('[class="md-clickable notSelectedLeftMenuItem"]')).click();
    browser.sleep(2000);
    element.all(by.repeater('suboption in option.submenuOptions')).get(0).click();//suboption in option.submenuOptions
    browser.sleep(6000);
    element(by.css('[aria-label="showDatasetQbe"]')).click();
 });
  //aria-label="showDatasetQbe"
  it('should select data',function(){
        var driver = browser.driver;
    var loc = by.id('documentViewerIframe');
    var el = driver.findElement(loc);
    browser.switchTo().frame(el);
    
    browser.sleep(10000);
    element(by.css('[src="../js/lib/ext-3.2.1/resources/images/default/s.gif"]')).click();
    browser.sleep(1000)
    element.all(by.css('[class="x-tree-node-anchor"]')).then(function(items){
      
      items[items.length/2].click();
      browser.sleep(1000)
      items[(items.length/2) + 1].click();
      browser.sleep(1000);
    });

  });

  it('should open preview', function(){
      var index;
      element.all(by.css('[class=" x-btn-text"]')).getText().then(function(items){
          console.log("ITEMS-LENGTH"+items.length);
          for(var i=0; i<items.length;i++){
              console.log(items[i]);
              if(items[i].indexOf("Preview") > -1){
                index = i;
                break;  
              }
          }
      });

      element.all(by.css('[class=" x-btn-text"]')).then(function(items){
          items[index].click();
      });

      browser.sleep(1000);

  });

  it('should open save dialog', function(){
    

    browser.driver.switchTo().defaultContent();
    browser.switchTo().frame(0);
      element(by.css('[md-font-icon="fa fa-floppy-o"]')).click();
      browser.sleep(2000);

          
  });

  it('fill input fields and save',function(){
      var d = new Date();
      var driver = browser.driver;
      var loc = by.id('documentViewerIframe');
      var el = driver.findElement(loc);
      browser.switchTo().frame(el);

      element(by.id("label")).sendKeys("lable-example-test"+d.getTime());
      browser.sleep(1000);
      name = "name-example-test"+d.getTime();
      element(by.id("name")).sendKeys(name);
      browser.sleep(1000); 
      element(by.css('[class=" x-btn-text icon-save"]')).click();
      browser.sleep(4000);
  });

  it('should exit dialog',function(){
    var index;
      element.all(by.css('[class=" x-btn-text"]')).getText().then(function(items){
          console.log("ITEMS-LENGTH"+items.length);
          for(var i=0; i<items.length;i++){
              console.log(items[i]);
              if(items[i] == "OK"){
                index = i;
                break;  
              }
          }
      });

      element.all(by.css('[class=" x-btn-text"]')).then(function(items){
          items[index].click();
      });

      browser.sleep(1000);
  });

  it("should exit document",function(){
    browser.driver.switchTo().defaultContent();
    browser.switchTo().frame(0);
      element(by.css('[ng-click="closeDocument()"]')).click();
      browser.sleep(3000);
  });

  it("should find last saved",function(){
      element.all(by.css('[class="ellipsis ng-binding"]')).getText().then(function(names){
          var limit = names.length;
          for(var i=0; i<names.length;i++){
              console.log(names[i]);
              if(i==limit-1)
                expect(names[i]).toBe(name.toUpperCase());
          }
          //console.log(names[names.length - 1]+"name");
          //expect(names[limit - 1]).toBe(name.toUpperCase());
      });
  });
}); 