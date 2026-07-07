const fs = require('fs');
const path = require('path');
const { rootDir, supportedLocales, localePath } = require('./lib.ts');
const { buildLocales } = require('./build.ts');

const targets = [
  path.join(rootDir, 'admin-ui', 'public', 'i18n'),
  path.join(rootDir, 'bocoo-admin', 'src', 'main', 'resources', 'i18n')
];

function main() {
  buildLocales();
  for (const targetDir of targets) {
    fs.mkdirSync(targetDir, { recursive: true });
    for (const locale of supportedLocales) {
      fs.copyFileSync(localePath(locale), path.join(targetDir, `${locale}.json`));
    }
  }
  console.log(`[i18n:sync] synced ${supportedLocales.length} locale files to ${targets.length} runtime target(s).`);
}

main();
