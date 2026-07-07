const {
  localePath,
  readSourceLocale,
  supportedLocales,
  writeJson
} = require('./lib.ts');

function buildLocales() {
  for (const locale of supportedLocales) {
    const { messages, duplicates } = readSourceLocale(locale);
    if (duplicates.length) {
      const sample = duplicates
        .slice(0, 20)
        .map((item) => `${item.key} (${item.firstModule}, ${item.duplicateModule})`)
        .join(', ');
      throw new Error(`[i18n:build] duplicate ${locale} key(s): ${sample}`);
    }
    writeJson(localePath(locale), messages);
    console.log(`[i18n:build] built ${Object.keys(messages).length} ${locale} key(s).`);
  }
}

if (require.main === module) {
  try {
    buildLocales();
  } catch (error) {
    console.error(error.message || error);
    process.exit(1);
  }
}

module.exports = {
  buildLocales
};
