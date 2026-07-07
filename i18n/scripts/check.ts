const { readSourceLocale, supportedLocales } = require('./lib.ts');

function main() {
  const [sourceLocale, ...targetLocales] = supportedLocales;
  const { messages: source, duplicates: sourceDuplicates } = readSourceLocale(sourceLocale);
  const sourceKeys = Object.keys(source).sort();
  let failed = false;

  if (sourceDuplicates.length) {
    failed = true;
    console.error(`[i18n:check] duplicate ${sourceLocale} keys`);
    for (const item of sourceDuplicates.slice(0, 50)) {
      console.error(`  ${item.key}: ${item.firstModule}, ${item.duplicateModule}`);
    }
  }

  for (const locale of targetLocales) {
    const { messages: target, duplicates } = readSourceLocale(locale);
    const targetKeys = Object.keys(target).sort();
    const missing = sourceKeys.filter((key) => !(key in target));
    const extra = targetKeys.filter((key) => !(key in source));
    const empty = targetKeys.filter((key) => String(target[key] ?? '').trim() === '');

    if (duplicates.length || missing.length || extra.length || empty.length) {
      failed = true;
      console.error(`[i18n:check] i18n/source/*/${locale}.json`);
      if (duplicates.length) console.error(`  duplicate keys: ${duplicates.slice(0, 50).map((item) => `${item.key}(${item.firstModule},${item.duplicateModule})`).join(', ')}`);
      if (missing.length) console.error(`  missing keys: ${missing.slice(0, 50).join(', ')}`);
      if (extra.length) console.error(`  extra keys: ${extra.slice(0, 50).join(', ')}`);
      if (empty.length) console.error(`  empty values: ${empty.slice(0, 50).join(', ')}`);
    }
  }

  if (failed) {
    process.exit(1);
  }
  console.log(`[i18n:check] ${sourceKeys.length} keys checked across ${supportedLocales.length} locales.`);
}

main();
