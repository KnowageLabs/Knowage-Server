const url = require("url");
const path = require('path');
const puppeteer = require('puppeteer');
const isValid = require('is-valid-path');
const shajs = require('sha.js')

// Parse parameters
console.log("Start Chromium Export with following parameters:");
for (let j = 0; j < process.argv.length; j++) {
	console.log("\t" + j + " -> " + (process.argv[j]));
}

// Arg[0]: url
var requestUrl = null;
try {
	requestUrl = url.parse(process.argv[2]);
	console.log("Request URL is " + requestUrl.href);
} catch(e) {
	console.log("Invalid URL at argumet 1: " + process.argv[2], e);
	process.exit(2);
}

// Arg[1]: encoded user id for authentication
var encodedUserId = null;
try {
	encodedUserId = process.argv[3];
} catch(e) {
	console.log("Invalid user id at argumet 2", e);
	process.exit(2);
}

// Arg[3]: output path
var output = null;
//if (!isValid(process.argv[4])) {
//	console.log("Invalid path at argument 3");
//	process.exit(4);
//}
output = process.argv[4];

var sheetCount = process.argv[5];

var sheetWidth = process.argv[6];
if (sheetWidth == undefined) {
	console.log("Invalid sheet width at argument 6", e);
	process.exit(2);
} else {
	sheetWidth = new Number(sheetWidth);
}

var sheetHeight = process.argv[7];
if (sheetWidth == undefined) {
	console.log("Invalid sheet height at argument 7", e);
	process.exit(2);
} else {
	sheetHeight = new Number(sheetHeight);
}

// Parameter ok, start browsing
async function exportSheets() {
	let browser;
	try {
		const browser = await puppeteer.launch({
			ignoreHTTPSErrors: true,
			// Really, really important!
			defaultViewport: { width: sheetWidth, height: sheetHeight, deviceScaleFactor: 0.80 },
			headless: true,
			args: [
					'--window-size=' + sheetWidth + ',' + sheetHeight + '',
					'--no-sandbox'
		]});

		// Get a page
		const page = (await browser.pages())[0];

		await page.setRequestInterception(true);

		// Clear cookies
		const client = await page.target().createCDPSession();
		await client.send('Network.clearBrowserCookies');
		await client.send('Network.clearBrowserCache');

		var d = new Date();
		var uniqueToken = d.getTime().toString();
		console.log("Unique token " + uniqueToken);

		var authHeader = "Direct " + encodedUserId;
		console.log("Authorization header is: " + authHeader);
		var hmacString = requestUrl.href + "" + uniqueToken + "Authorization" + "Direct " + encodedUserId + Number.NaN;
		console.log("HMAC String to be signed is: " + hmacString);
		var hmacSignature = shajs("sha256").update(hmacString).digest("hex");
		console.log("Signature " + hmacSignature);

		page.setExtraHTTPHeaders({
			"Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3",
			"Accept-Language": "en-US,en;q=0.5",
			"Accept-Encoding": "gzip, deflate",
			"Authorization": authHeader,
			"HMAC_Signature": hmacSignature,
			"HMAC_Token": uniqueToken
		});

		page.on('request', request => {
			// Override headers
			const headers = Object.assign({}, request.headers(), {
				"Upgrade-Insecure-Requests": undefined,
				"upgrade-insecure-requests": undefined,
				"Sec-Fetch-Mode": undefined,
				"Sec-Fetch-Site": undefined,
				"sec-fetch-user": undefined,
				"sec-fetch-mode": undefined,
				"Pragma": undefined,
				"Cache-Control": undefined
			});

			request.continue({headers});
		});

		for (var currSheet=0; currSheet<sheetCount; currSheet++) {

			var currRequestStr = requestUrl.href;
			if (sheetCount > 0) {
				currRequestStr + "&sheet=" + currSheet;
			}

			console.log("Go to sheet " + currSheet + " at: " + currRequestStr);
			await page.goto(currRequestStr, { timeout: 120000, waitUntil: "networkidle0" });

			console.log("Generate image for sheet " + currSheet);
			await page.emulateMedia('print');
			
			await page.pdf({
				path: path.join(output, "sheet_" + currSheet + ".pdf" ),
				displayHeaderFooter: false,
				printBackground: true,
				landscape: true,
				format: "A4",
				width: sheetWidth,
				height: sheetHeight,
				margin: {
					top: 0, right: 0, bottom: 0, left: 0
				}
			});

			console.log("Image generated for sheet " + currSheet);
		}

		console.log("Number of images generated: " + sheetCount);

	} catch (err) {
		console.error(err);
				process.exit(1)
	} finally {
		if (browser) {
			await browser.disconnect();
		}
		process.exit(0)
	}
}

exportSheets();
