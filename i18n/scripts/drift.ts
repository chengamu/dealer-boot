const fs = require('fs');
const path = require('path');
const {
  localePath,
  readSourceLocale,
  rootDir,
  sortObject,
  supportedLocales
} = require('./lib.ts');

const runtimeTargets = [
  path.join(rootDir, 'admin-ui', 'public', 'i18n'),
  path.join(rootDir, 'bocoo-admin', 'src', 'main', 'resources', 'i18n')
];

function readFile(file) {
  return fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : undefined;
}

function main() {
  const errors = [];

  for (const locale of supportedLocales) {
    const { messages, duplicates } = readSourceLocale(locale);
    if (duplicates.length) {
      for (const item of duplicates) {
        errors.push(`${locale} duplicate key ${item.key} in ${item.firstModule} and ${item.duplicateModule}`);
      }
    }

    const expectedContent = `${JSON.stringify(sortObject(messages), null, 2)}\n`;
    const generatedTargets = [
      localePath(locale),
      ...runtimeTargets.map((targetDir) => path.join(targetDir, `${locale}.json`))
    ];

    for (const targetFile of generatedTargets) {
      const targetContent = readFile(targetFile);
      if (targetContent === undefined) {
        errors.push(`${targetFile} is missing`);
      } else if (targetContent !== expectedContent) {
        errors.push(`${targetFile} differs from i18n/source expected ${locale}`);
      }
    }
  }

  if (errors.length) {
    console.error(`[i18n:drift] failed with ${errors.length} issue(s).`);
    for (const error of errors) console.error(`- ${error}`);
    process.exit(1);
  }

  console.log(`[i18n:drift] source, generated locales, and ${runtimeTargets.length} runtime target(s) are in sync for ${supportedLocales.length} locale(s).`);
}

main();
