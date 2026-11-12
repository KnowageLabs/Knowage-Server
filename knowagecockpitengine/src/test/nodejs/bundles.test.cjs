const assert = require("node:assert/strict");
const { execFileSync } = require("node:child_process");
const fs = require("node:fs");
const http = require("node:http");
const path = require("node:path");
const test = require("node:test");
const zlib = require("node:zlib");

const webapp = path.resolve(__dirname, "..", "..", "main", "webapp");
const grunt = require(require.resolve("grunt", { paths: [webapp] }));
const uglify = require(require.resolve("uglify-js", { paths: [webapp] }));
const CleanCSS = require(require.resolve("clean-css", { paths: [webapp] }));
grunt.file.setBase(webapp);
require(path.join(webapp, "Gruntfile.js"))(grunt);

function runGrunt(task) {
	try {
		execFileSync(process.execPath, ["-e", `require("grunt").tasks([${JSON.stringify(task)}]);`], {
			cwd: webapp,
			stdio: "pipe",
		});
	} catch (error) {
		throw new Error(`${error.message}\n${error.stdout || ""}\n${error.stderr || ""}`, { cause: error });
	}
}

function urls(css) {
	return [...css.matchAll(/url\(([^)]+)\)/g)].map(match => match[1].trim().replace(/^['"]|['"]$/g, ""));
}

test("duplicated OpenLayers assets are byte-identical to the canonical tree", () => {
	const retained = path.join(webapp, "js", "lib", "openlayers");
	const excluded = path.join(retained, "openlayers");
	let count = 0;
	function check(directory) {
		for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
			const filename = path.join(directory, entry.name);
			if (entry.isDirectory()) {
				check(filename);
			} else {
				const counterpart = path.join(retained, path.relative(excluded, filename));
				assert.deepEqual(fs.readFileSync(filename), fs.readFileSync(counterpart), counterpart);
				count++;
			}
		}
	}
	check(excluded);
	assert(count > 0, "No duplicate assets were verified");
});

test("runtime sources do not reference the excluded legacy OpenLayers paths", () => {
	function check(directory) {
		for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
			const filename = path.join(directory, entry.name);
			const relative = path.relative(webapp, filename).split(path.sep).join("/");
			if (["node_modules", "dist", "js/lib"].includes(relative)) continue;
			if (entry.isDirectory()) {
				check(filename);
			} else if (/\.(js|jsp|jspf|html|css|json)$/.test(filename)) {
				assert(!/openlayers\/(?:openlayers|3\.x\.x|4\.6\.4)\//.test(fs.readFileSync(filename, "utf8")),
					`Runtime source references an excluded library: ${relative}`);
			}
		}
	}
	check(webapp);
});

async function browserSmoke(original) {
	const puppeteer = require(process.env.KNOWAGE_TEST_PUPPETEER);
	const scripts = ["angular-bundle.js", "knowagecockpit-lib-bundle.js", "knowagecockpit-modules-bundle.js"];
	const server = http.createServer((request, response) => {
		const parts = new URL(request.url, "http://localhost").pathname.split("/").filter(Boolean);
		const variant = parts.shift();
		if (parts.length === 0 && ["before", "after"].includes(variant)) {
			response.setHeader("Content-Type", "text/html");
			response.end(`<!doctype html><html><body ng-app="bundleSmoke">
				<span id="bound">{{value}}</span>
				${scripts.map(name => `<script src="/${variant}/dist/${name}"></script>`).join("")}
				<script>angular.module("bundleSmoke", ["ngMaterial"]).run(function($rootScope) {
					$rootScope.value = "smoke-ok";
				});</script></body></html>`);
		} else {
			const filename = parts.join("/");
			if (["before", "after"].includes(variant) && original.has(filename)) {
				response.setHeader("Content-Type", "application/javascript");
				response.end(variant === "before" ? original.get(filename)
					: fs.readFileSync(path.join(webapp, filename)));
			} else {
				response.writeHead(204).end();
			}
		}
	});
	await new Promise(resolve => server.listen(0, "127.0.0.1", resolve));
	let browser;
	try {
		browser = await puppeteer.launch({
			executablePath: process.env.KNOWAGE_TEST_CHROMIUM,
			headless: true,
			args: ["--disable-background-networking", "--disable-component-update", "--disable-sync", "--no-first-run"],
		});
		const results = [];
		for (const variant of ["before", "after"]) {
			const page = await browser.newPage();
			const errors = [];
			page.on("pageerror", error => errors.push(error.message));
			await page.setRequestInterception(true);
			page.on("request", request => {
				const url = new URL(request.url());
				if (url.hostname === "127.0.0.1" || url.protocol === "data:") request.continue();
				else request.abort();
			});
			await page.goto(`http://127.0.0.1:${server.address().port}/${variant}/`);
			assert.deepEqual(errors, [], `${variant} bundle raised browser errors`);
			await page.waitForFunction(() => document.getElementById("bound").textContent === "smoke-ok");
			results.push(await page.evaluate(() => {
				const pdf = new jspdf.jsPDF();
				pdf.text("Feature parity", 10, 10);
				return {
					angularBinding: document.getElementById("bound").textContent,
					mapCoordinates: new ol.format.WKT().readGeometry("POINT(1 2)").getCoordinates(),
					pdfBytes: pdf.output("arraybuffer").byteLength,
					date: luxon.DateTime.fromISO("2026-11-12").toISODate(),
					editor: typeof CodeMirror,
					grid: typeof agGrid.Grid,
				};
			}));
			assert.deepEqual(errors, [], `${variant} bundle raised browser errors`);
			await page.close();
		}
		assert(results[0].pdfBytes > 1000, "PDF generation failed");
		assert.deepEqual(results[0].mapCoordinates, [1, 2]);
		assert.deepEqual(results[1], results[0], "Browser behavior changed after minification");
		console.log("Offline browser smoke passed: Angular binding, map parsing, editor, grid and PDF generation.");
	} finally {
		try {
			if (browser) await browser.close();
		} finally {
			await new Promise(resolve => server.close(resolve));
		}
	}
}

test("production bundles preserve JavaScript semantics, CSS URLs and license banners", async t => {
	runGrunt("concat");
	const files = Object.keys(grunt.config.get("concat.basic_and_extras.files"))
		.map(filename => grunt.template.process(filename));
	const original = new Map(files.map(filename => [
		filename, fs.readFileSync(path.join(webapp, filename), "utf8"),
	]));

	runGrunt("knowagecockpit-concat");

	let beforeBytes = 0;
	let afterBytes = 0;
	let beforeDeflated = 0;
	let afterDeflated = 0;
	for (const [filename, before] of original) {
		const after = fs.readFileSync(path.join(webapp, filename), "utf8");
		beforeBytes += Buffer.byteLength(before);
		afterBytes += Buffer.byteLength(after);
		beforeDeflated += zlib.deflateRawSync(before, { level: 9 }).length;
		afterDeflated += zlib.deflateRawSync(after, { level: 9 }).length;
		if (filename.endsWith(".js")) {
			assert.equal(uglify.parse(after).print_to_string(), uglify.parse(before).print_to_string(),
				`JavaScript changed beyond formatting: ${filename}`);
		} else {
			const expected = new CleanCSS(grunt.config.get("cssmin.options")).minify(before);
			assert.deepEqual(expected.errors, []);
			assert.equal(after, expected.styles, filename);
			assert.deepEqual(urls(after), urls(before), `CSS asset URLs changed: ${filename}`);
		}
		for (const banner of before.match(/\/\*![\s\S]*?\*\//g) || []) {
			assert(after.replace(/\s+/g, "").includes(banner.replace(/\s+/g, "")),
				`License banner removed: ${filename}`);
		}
	}
	assert(afterBytes < beforeBytes, "Production bundles did not shrink");
	assert(afterDeflated < beforeDeflated, "Compressed production bundles did not shrink");
	console.log(JSON.stringify({ files: files.length, beforeBytes, afterBytes, beforeDeflated, afterDeflated }));
	await t.test("browser behavior matches before and after minification", {
		skip: !process.env.KNOWAGE_TEST_CHROMIUM || !process.env.KNOWAGE_TEST_PUPPETEER,
	}, () => browserSmoke(original));
});
