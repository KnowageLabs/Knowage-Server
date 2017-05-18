var system = require('system');

// this function writes the arguments to stdout
var log = function () {
  Array.prototype.slice.call(arguments).map(system.stdout.writeLine);
};

// this function writes the arguments to the stderr
var err = function () {
  Array.prototype.slice.call(arguments).map(system.stderr.writeLine);
};

var renderId = system.args[ 1 ];
log("renderId = " + renderId);

var baseUrl = system.args[ 2 ];
log("baseUrl = " + baseUrl);

var	customHeaders = system.args[ 3 ];
log("customHeaders = " + customHeaders);
customHeaders = JSON.parse(system.args[ 3 ]);

var	sheets = +system.args[ 4 ];
log("sheets = " + sheets);

var	viewportWidth = system.args[ 5 ];
log("viewportWidth = " + viewportWidth);

var	viewportHeight = system.args[ 6 ];
log("viewportHeight = " + viewportHeight);

var	operatingSystem = system.args[ 7 ];
log("operatingSystem = " + operatingSystem);

var	targetPath = system.args[ 8 ];
log("targetPath = " + targetPath);

var	jsWait = +system.args[ 9 ];
log("jsWait = " + jsWait);

var	jsInterval = +system.args[ 10 ];
log("jsInterval = " + jsInterval);

// this sets a zoom on the page because of the dpi differences between windows and unix
var setZoom = function (page) {
	log("[SETZOOM] IN");
  if (operatingSystem !== "WINDOWS") {
    try {
      log('Setting zoom on HTML to 0.75');
      page.evaluate(function () {
        document.body.style.zoom = 0.75;
      });
    } catch (error) {
      err('Failed to set zoom on HTML file: ', error);
      slimer.exit(1);
    }
  }
};

// this function renders the page
var renderPage = function (page) {
	log("[RENDERPAGE] IN");
  setZoom(page);

  // render the page
  try {
    log('Rendering PNG to target folder: ' + targetPath);
    var targetFile = targetPath + "_" + page.sheet + ".png";
    log("Rendering PNG as target file: " + targetFile);
    page.render(targetFile);
  } catch (error) {
    err('Failed to render PNG: ' + error);
    slimer.exit(3);
  }

  slimer.exit(0); // OK!
};

var applySettingOnPage = function applySettingOnPage(page, sheet) {
	log("[APPLYSETTINGONPAGE] IN");
	page.sheet = sheet;
	page.viewportSize = { width: viewportWidth, height: viewportHeight };
	page.settings.userName = 'biadmin';
	page.settings.password = 'biadmin';
	page.customHeaders = customHeaders;
}

var urls = new Map();

for(var i=0; i<sheets; i++) {
	log("Looping over sheets, if any");
	var currentUrl = decodeURIComponent(baseUrl);
	if(i > 0) {
		currentUrl = currentUrl + "&sheet=" + i;
	}
	log("URL to be processed = " + currentUrl);
	urls.set(i, currentUrl);
}

var queue = [];
urls.forEach(function(url, sheet) {
    var p = new Promise(function(resolve, reject) {
        var page = require('webpage').create();
        applySettingOnPage(page, sheet);
        log("Processing URL " + url + " during rendering with id " + renderId);
        page.open(url, function() {
        	setTimeout(renderPage, jsInterval, page);
        });
    });
    queue.push(p);
}, urls);

Promise.all(queue).then(function(values) {
    log(values);
    slimer.exit();
});