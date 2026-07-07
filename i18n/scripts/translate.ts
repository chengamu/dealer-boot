const {
  loadEnv,
  readSourceLocale,
  readSourceModule,
  writeSourceModule
} = require('./lib.ts');

const sourceLocale = 'en_US';
const targetLocale = 'zh_CN';

function extractJsonObject(text) {
  const start = text.indexOf('{');
  const end = text.lastIndexOf('}');
  if (start < 0 || end <= start) {
    throw new Error('DeepSeek response did not contain a JSON object.');
  }
  return JSON.parse(text.slice(start, end + 1));
}

async function main() {
  loadEnv();
  const apiKey = process.env.DEEPSEEK_API_KEY;
  const model = process.env.DEEPSEEK_MODEL || 'deepseek-v4-pro';
  const baseUrl = process.env.DEEPSEEK_BASE_URL || 'https://api.deepseek.com/chat/completions';

  if (!apiKey) {
    console.error('[i18n:translate] DEEPSEEK_API_KEY is missing. Fill .env before running translation.');
    process.exit(1);
  }

  const { messages: source, keyModules } = readSourceLocale(sourceLocale);
  const { messages: target } = readSourceLocale(targetLocale);
  const missing = Object.fromEntries(Object.entries(source).filter(([key]) => !target[key] || String(target[key]).trim() === ''));

  if (!Object.keys(missing).length) {
    console.log('[i18n:translate] no missing zh_CN keys.');
    return;
  }

  const prompt = [
    'Translate the following i18n JSON values from English to Simplified Chinese for a B2B cross-border order management system.',
    'Rules: return JSON only; keep keys unchanged; preserve placeholders like {{orderNo}}, {0}, {min}; preserve HTML tags; do not translate codes, units, order numbers, or variable names.',
    JSON.stringify(missing, null, 2)
  ].join('\n\n');

  const response = await fetch(baseUrl, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${apiKey}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      model,
      messages: [
        { role: 'system', content: 'You are a precise software localization assistant. Return valid JSON only.' },
        { role: 'user', content: prompt }
      ],
      temperature: 0.2
    })
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`DeepSeek request failed: ${response.status} ${body}`);
  }

  const data = await response.json();
  const content = data?.choices?.[0]?.message?.content;
  if (!content) {
    throw new Error('DeepSeek response did not include choices[0].message.content.');
  }
  const translated = extractJsonObject(content);
  const translatedByModule = {};
  for (const [key, value] of Object.entries(translated)) {
    const moduleName = keyModules[key];
    if (!moduleName) continue;
    if (!translatedByModule[moduleName]) translatedByModule[moduleName] = {};
    translatedByModule[moduleName][key] = value;
  }
  for (const [moduleName, moduleMessages] of Object.entries(translatedByModule)) {
    writeSourceModule(moduleName, targetLocale, {
      ...readSourceModule(moduleName, targetLocale),
      ...moduleMessages
    });
  }
  console.log(`[i18n:translate] translated ${Object.keys(translated).length} key(s) into ${targetLocale}.`);
}

main().catch((error) => {
  console.error(error.message || error);
  process.exit(1);
});
