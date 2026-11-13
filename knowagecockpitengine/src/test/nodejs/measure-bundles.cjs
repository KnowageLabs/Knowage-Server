const assert = require("node:assert/strict");
const crypto = require("node:crypto");
const fs = require("node:fs");
const path = require("node:path");
const zlib = require("node:zlib");

const directories = process.argv.slice(2);
assert.equal(directories.length, 3, "Usage: node measure-bundles.cjs <A-dist> <B-dist> <C-dist>");
const snapshots = directories.map((directory, index) => {
	const files = fs.readdirSync(directory).filter(name => /\.(js|css)$/.test(name)).sort().map(name => {
		const content = fs.readFileSync(path.join(directory, name));
		return {
			name,
			bytes: content.length,
			deflatedBytes: zlib.deflateRawSync(content, { level: 9 }).length,
			sha256: crypto.createHash("sha256").update(content).digest("hex"),
		};
	});
	assert(files.length > 0, "No bundles found: " + directory);
	return {
		label: ["A", "B", "C"][index],
		files,
		bytes: files.reduce((total, file) => total + file.bytes, 0),
		deflatedBytes: files.reduce((total, file) => total + file.deflatedBytes, 0),
	};
});
const names = snapshots[0].files.map(file => file.name);
for (const snapshot of snapshots.slice(1)) {
	assert.deepEqual(snapshot.files.map(file => file.name), names,
		"Bundle names differ: use the same asset version and remove stale outputs");
}
console.log(JSON.stringify({
	limitation: "Bundle content only; DEFLATE level 9 is a normalized proxy, not a measured WAR size.",
	snapshots,
	deltas: snapshots.slice(1).map((snapshot, index) => ({
		from: snapshots[index].label,
		to: snapshot.label,
		bytesSaved: snapshots[index].bytes - snapshot.bytes,
		deflatedBytesSaved: snapshots[index].deflatedBytes - snapshot.deflatedBytes,
	})),
}, null, 2));
