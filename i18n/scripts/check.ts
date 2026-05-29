const { readLocale, supportedLocales, localePath } = require('./lib.ts');

function main() {
  const [sourceLocale, ...targetLocales] = supportedLocales;
  const source = readLocale(sourceLocale);
  const sourceKeys = Object.keys(source).sort();
  let failed = false;

  for (const locale of targetLocales) {
    const target = readLocale(locale);
    const targetKeys = Object.keys(target).sort();
    const missing = sourceKeys.filter((key) => !(key in target));
    const extra = targetKeys.filter((key) => !(key in source));
    const empty = targetKeys.filter((key) => String(target[key] ?? '').trim() === '');

    if (missing.length || extra.length || empty.length) {
      failed = true;
      console.error(`[i18n:check] ${localePath(locale)}`);
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
