const {
  readLocale,
  supportedLocales,
  placeholderSet,
  parseDictCoverageFromSql
} = require('./lib.ts');

function diff(a, b) {
  return a.filter((item) => !b.includes(item));
}

function main() {
  const [sourceLocale, ...targetLocales] = supportedLocales;
  const source = readLocale(sourceLocale);
  const sourceKeys = Object.keys(source).sort();
  const errors = [];

  for (const [key, value] of Object.entries(source)) {
    if (String(value ?? '').trim() === '') {
      errors.push(`${sourceLocale}:${key} is empty`);
    }
  }

  for (const locale of targetLocales) {
    const target = readLocale(locale);
    const targetKeys = Object.keys(target).sort();
    for (const key of sourceKeys) {
      if (!(key in target)) {
        errors.push(`${locale}:${key} is missing`);
        continue;
      }
      if (String(target[key] ?? '').trim() === '') {
        errors.push(`${locale}:${key} is empty`);
      }
      const sourcePlaceholders = placeholderSet(source[key]);
      const targetPlaceholders = placeholderSet(target[key]);
      const missingPlaceholders = diff(sourcePlaceholders, targetPlaceholders);
      const extraPlaceholders = diff(targetPlaceholders, sourcePlaceholders);
      if (missingPlaceholders.length || extraPlaceholders.length) {
        errors.push(`${locale}:${key} placeholder mismatch source=[${sourcePlaceholders.join(',')}] target=[${targetPlaceholders.join(',')}]`);
      }
    }
    for (const key of targetKeys) {
      if (!(key in source)) {
        errors.push(`${locale}:${key} is extra`);
      }
    }
  }

  const dictRows = parseDictCoverageFromSql();
  for (const row of dictRows) {
    for (const locale of supportedLocales) {
      const messages = readLocale(locale);
      if (!(row.key in messages)) {
        errors.push(`${locale}:${row.key} missing for enabled dict ${row.dictType}/${row.value}`);
      }
    }
  }

  if (errors.length) {
    console.error(`[i18n:validate] failed with ${errors.length} issue(s).`);
    for (const error of errors.slice(0, 100)) {
      console.error(`- ${error}`);
    }
    if (errors.length > 100) {
      console.error(`... ${errors.length - 100} more`);
    }
    process.exit(1);
  }

  console.log(`[i18n:validate] ${sourceKeys.length} keys valid across ${supportedLocales.length} locales; ${dictRows.length} dict keys covered.`);
}

main();
