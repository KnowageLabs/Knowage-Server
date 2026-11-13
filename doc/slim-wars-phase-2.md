# Slim WARs, phase 2: first-session evidence

Status: independently tested frontend/tooling changes, **not JVM/runtime sign-off**.
The later request to work on `master` supersedes the plan's dedicated-branch
instruction. Changes are committed separately on CE `master`, without deployment.
The existing local core POM edit and EE overlays are preserved.

Implementation commits: `e2761b2206` (frontend) and `f7c37600b9` (archive tools).
No push was performed in this phase.

## Baselines and scope

| Snapshot | Source |
| --- | --- |
| A | CE `6232083f13a4b9412a84cb4130eab80ac539581e` |
| B | CE `fed91500187db5452582fcec33392ea2c93c104b` |
| C | Phase-two implementation following B |
| EE reference | `62a5aa59e5061beaa3c9f5561c35397578ae8b8b` |

Separate A/B/C worktrees and local Maven repository directories were prepared.
No Maven or Java command was run, respecting the existing user restriction.
There are **no rebuilt A/B/C WARs**, resolved dependency trees, effective POMs,
verified Maven profiles, JUnit counts or measured backend deltas.
CE remote refresh failed twice with a github.com DNS error; the recorded cached
base was used. EE refresh succeeded and its base was unchanged.

Validation used Windows, Node **22.22.1**, npm **11.21.0** and PowerShell.
The lock uses format 2. Compatibility with the actual corporate runner is not
certified: Jenkins configuration is unavailable and the versioned GitHub Actions
JDK 8 configuration is not evidence of the current Java 17 product toolchain.
No runner/settings/credentials were changed or copied.

## Completed changes

- Cockpit now uses a committed npm lock, `npm ci --include=dev` and project-local
  Grunt through the existing Maven lifecycle. Maven passes its version to Grunt
  without modifying package.json or the lock.
- Generated bundles live under `target/frontend`; stale source-tree `dist` is
  excluded. Existing WAR names, bundle URLs, runtime npm asset whitelist and
  self-contained deployment remain unchanged in the packaging configuration.
- Each transformed JS/CSS bundle has a hidden source map with embedded original
  files. Concatenation order and script parsing are retained. The existing debug
  ZIP also receives those maps and an asset/version/revision/SHA-256 manifest.
  Map existence/usability checks run automatically in the normal Grunt build.
- Tests use isolated temporary output directories. They check AST parity, CSS
  URLs, legal banners, asset/map hashes and matching generated/original tokens.
  `KNOWAGE_REQUIRE_BROWSER=1` makes missing browser prerequisites a test failure.
- The packaging validator searches all application JARs and `WEB-INF/classes`
  for duplicate/stale SPARQL classes, including repeated ZIP entries.
  Solr streaming/ZooKeeper are no longer hard-coded as mandatory. **This does not
  remove them from the product or prove that they can be removed.**
- The new archive inventory records hashes, nested JAR metadata, class duplicates
  with identical/differing bytes, coordinate version conflicts and static content.
  Fixtures exercise main/Cockpit/debug policies and the new inventory.

CE Cockpit assets reach EE through its existing CE WAR overlay dependency.
There is no new EE-specific code change in this tranche; the actual EE overlay
build remains unverified. No system libraries, external JAR pool, new service,
mandatory environment variable or administrator installation step was introduced.
The pre-existing ignored local lock was backed up before replacing it with the
valid versioned lock; no unrelated source edit was discarded.

## Measurements, not projected savings

[Bundle measurements and hashes](slim-wars-phase-2-bundles.json) use the same locked
npm installation for A/B/C and asset version `9.0.0-SNAPSHOT`. A/B use their
original Grunt tasks with developer-only dependency junctions; C uses the new
output-directory/version options. The comparison covers nine matching bundles.
Baseline banners still contain their build date; C deliberately removes it.
DEFLATE level 9 is a normalized content comparison, **not a WAR measurement**.

| Bundles | Raw bytes | DEFLATE bytes |
| --- | ---: | ---: |
| A | 8,689,575 | 1,989,121 |
| B | 7,545,059 | 1,790,869 |
| C | 7,614,120 | 1,805,052 |

A to B saves 198,252 normalized compressed bytes. B to C **adds 14,183 bytes**:
original legal comments/banners are no longer stripped before mapping. This is
a reproducibility/debug correction, not an additional size optimization.
All eight normalized B/C JS bundles have equal parsed/printed ASTs.

[Historical inventory summary](slim-wars-phase-2-inventory.json) covers **21 local
CE/EE WARs**, totaling 2,873,918,948 file bytes and 3,553,025,893 uncompressed entry
bytes. These archives predate the current sources and are not baseline A/B/C.
Vue and SDK archives were unavailable. The JSON includes each WAR hash, bytes,
entry/JAR counts, duplicate-class counts and coordinate conflicts.
The full inventory, including individual class hashes/owners, was retained in
the session artifact `phase2-historical-war-inventory.json` and is reproducible
with `inventory-wars.ps1`.

The inventory observes 61 duplicate class paths in historical Talend, 316 in
CE Cockpit, 1,099 in CE main and 2,817 in EE Data Preparation. Some have different
bytes. QBE CE/EE each contains byte-identical Bouncy Castle JAR pairs under
`1.38`/`138` filenames. These are investigation candidates, not a blanket failure
gate or permission to delete dependencies. Embedded Maven metadata can describe
shaded content and does not prove resolved dependency provenance.

## Dependency priorities

Compressed byte figures below describe historical JAR entries, **not demonstrated
removable payload**. Rows overlap transitively and must not be summed as savings.

| Dependency/capability | Consumers and reason | Next change | Required validation | Observed removal in C |
| --- | --- | --- | --- | --- |
| Hazelcast: 14,917,580 bytes in historical Talend | Utils startup, locking and exposed contracts; main/Cockpit also carry it | Lightweight engine support first; separate lock contracts/provider only with startup analysis | Talend boot/callbacks, listeners, supported multinode paths | None |
| POI trio: 9,803,163 bytes in historical Talend/API | Dataset readers, upload validation and exports, not just Excel datasets | File/export contracts with explicit consumers | Upload validation, Excel datasets and exports | None |
| Groovy: 7,204,203 bytes in historical Talend | Script execution and sandbox in utils | Provider split preserving indirect engine loading | Sandbox, initialization and supported script result formats | None |
| Mongo driver: 1,646,145 bytes in historical Talend | Dataset factory directly references MongoDataSet | Break factory/provider reverse dependency before extraction | Persisted factory mapping and real Mongo execution | None |
| PDFBox: 2,647,147 bytes in historical Talend | PDF creation and report/export paths | Check direction against existing knowage-export before moving | Fonts/images/PDF per affected engine | None |
| Jena/Solr | Dataset factory, core associativity and indirect consumers | Resolve graph, SPI/JDBC/resources and actual transport capabilities | Isolated WAR SPARQL/Solr, facet/pivot/HTTPS | None |
| Frontend dev tooling/maps | Cockpit build/debug, CE assets overlaid into EE | Locked local build, generated outputs and real debug maps, completed here | Node suite, build repeatability; real WAR/browser still pending | B to C adds 14,183 normalized compressed bytes |

Talend engine support is the first structural target: avoid extending its
exclusion list. Order the remaining work using resolved graphs and rebuilt WARs,
not the largest historical JAR alone. Main/Cockpit legitimately still need many
specialist capabilities; extracting a provider is not itself a saving for them.

## Reproducible checks and results

From `knowagecockpitengine\src\main\webapp`:

```powershell
npm.cmd ci --include=dev --no-audit --no-fund
npm.cmd run build -- --asset-version=9.2.0-SNAPSHOT
npm.cmd run test:bundles
```

For browser validation in the developer environment, set
`KNOWAGE_TEST_CHROMIUM` to an existing browser executable and
`KNOWAGE_TEST_PUPPETEER` to an existing compatible Puppeteer module, then set
`KNOWAGE_REQUIRE_BROWSER=1`. These are test-only, not operational prerequisites.
The browser used here came from the existing offline-export installation archive;
no system browser or service was installed. Its temporary extraction was removed
after validation.

From the repository root:

```powershell
& .\knowage-ce-parent\src\test\powershell\test-war-packaging.ps1
$wars = @('C:\artifacts\knowage.war', 'C:\artifacts\knowagecockpitengine.war')
& .\knowage-ce-parent\src\test\powershell\inventory-wars.ps1 `
  -War $wars -Label C -OutputFile .\war-inventory-C.json
& .\knowage-ce-parent\src\test\powershell\verify-war-packaging.ps1 `
  -KnowageWar $wars[0] -CockpitWar $wars[1]
node .\knowagecockpitengine\src\test\nodejs\measure-bundles.cjs `
  C:\comparison\A\dist C:\comparison\B\dist C:\comparison\C\dist
```

Use the real artifact locations for archive checks. Inventory/PowerShell/browser
checks remain developer validation; no PowerShell/browser prerequisite was added
to the operational Maven command.

| Check | Result |
| --- | --- |
| Clean locked npm installation | Passed, including a second cached installation with unchanged lock hash |
| Production-version build | Passed; package.json and lock unchanged |
| Repeated production build | All nine asset and map hashes identical at the same revision/output path |
| Node suite with browser required | 4 tests passed, 0 failed, 0 skipped |
| AST/CSS/banner/map checks | Passed; actual token positions checked for all nine bundles |
| Offline browser smoke | Angular binding, WKT coordinates, CodeMirror/ag-grid presence, PDF generation and dates passed; no observed page/console/request errors |
| Archive-policy/inventory fixtures | Passed, including missing providers/assets and duplicates in arbitrary JARs/direct classes |
| XML, Node syntax, diff whitespace | Passed |
| Historical archive inventory | 21 WARs processed |
| A/B/C JVM builds, JUnit, Checkstyle, WAR classpath closure | Blocked: Maven/Java execution prohibited |
| Full Cockpit on WAR, application bundles/CSS, save/reopen, real maps/widgets/exports | Not executed; vendor smoke is not application regression coverage |
| Talend Java/Perl contexts/jobs, Solr/SPARQL integration, login/scheduler/multinode | Not executed |

Clean installations reported legacy dependency deprecations. CSS minification
warns about the missing vendor `ol.css.map` beside the concatenated input; the
generated bundle map is independently remapped to embedded original CSS and
passes the position check. Neither warning is presented as runtime sign-off.
During implementation, map-path/Windows URL test failures were corrected; the
final suite above passes. Logs and full machine-readable evidence remain with
the session artifacts.

Before any structural-removal sign-off, the intended product validation remains
`mvn -f knowage-ce-parent/pom.xml -Pproduction -DskipTests=false clean verify`,
separately for A/B/C with isolated repositories and a common dependency snapshot,
plus relevant development-profile and EE builds. **This command was not run.**
The first-session work does not claim that the full phase-two program is done.
