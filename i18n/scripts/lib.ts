const fs = require('fs');
const path = require('path');

const rootDir = path.resolve(__dirname, '..', '..');
const localeDir = path.join(rootDir, 'i18n', 'locales');
const supportedLocales = ['en_US', 'zh_CN'];

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function writeJson(file, data) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, `${JSON.stringify(sortObject(data), null, 2)}\n`, 'utf8');
}

function sortObject(data) {
  return Object.fromEntries(Object.entries(data).sort(([a], [b]) => a.localeCompare(b)));
}

function readLocale(locale) {
  return readJson(path.join(localeDir, `${locale}.json`));
}

function localePath(locale) {
  return path.join(localeDir, `${locale}.json`);
}

function placeholderSet(value) {
  const matches = String(value).match(/\{\{\s*[\w.]+\s*\}\}|\{[A-Za-z0-9_.]+\}/g) || [];
  return [...new Set(matches.map((item) => item.replace(/\s+/g, '')))].sort();
}

function loadEnv() {
  const envPath = path.join(rootDir, '.env');
  if (!fs.existsSync(envPath)) {
    return;
  }
  for (const rawLine of fs.readFileSync(envPath, 'utf8').split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) continue;
    const index = line.indexOf('=');
    if (index < 0) continue;
    const key = line.slice(0, index).trim();
    const value = line.slice(index + 1).trim();
    if (key && process.env[key] === undefined) {
      process.env[key] = value;
    }
  }
}

function parseDictCoverageFromSql() {
  const sqlPath = path.join(rootDir, 'sql', 'postgresql', 'base.sql');
  if (!fs.existsSync(sqlPath)) return [];
  const sql = fs.readFileSync(sqlPath, 'utf8');
  const rows = [];
  const blockPattern = /INSERT INTO sys_dict_data \(([^)]+)\)\s*VALUES\s*([\s\S]*?)(?:ON CONFLICT|;)/g;
  let blockMatch;
  while ((blockMatch = blockPattern.exec(sql)) !== null) {
    const columns = blockMatch[1].split(',').map((column) => column.trim());
    const values = blockMatch[2];
    const labelIndex = columns.indexOf('dict_label');
    const valueIndex = columns.indexOf('dict_value');
    const typeIndex = columns.indexOf('dict_type');
    if (labelIndex < 0 || valueIndex < 0 || typeIndex < 0) continue;
    const rowPattern = /\(([^;]*?)\)\s*,?/g;
    let rowMatch;
    while ((rowMatch = rowPattern.exec(values)) !== null) {
      const cells = splitSqlRow(rowMatch[1]);
      const label = unquoteSql(cells[labelIndex]);
      const value = unquoteSql(cells[valueIndex]);
      const dictType = unquoteSql(cells[typeIndex]);
      if (!dictType || dictType === 'sys_country') continue;
      if (!/^[A-Za-z0-9_]+$/.test(dictType)) continue;
      rows.push({
        key: `dict.${dictType}.${value}`,
        dictType,
        value,
        label
      });
    }
  }
  const seen = new Set();
  return rows.filter((row) => {
    const id = row.key;
    if (seen.has(id)) return false;
    seen.add(id);
    return true;
  });
}

function splitSqlRow(row) {
  const cells = [];
  let current = '';
  let inString = false;
  for (let index = 0; index < row.length; index += 1) {
    const char = row[index];
    if (char === "'") {
      current += char;
      if (row[index + 1] === "'") {
        current += row[index + 1];
        index += 1;
      } else {
        inString = !inString;
      }
      continue;
    }
    if (char === ',' && !inString) {
      cells.push(current.trim());
      current = '';
      continue;
    }
    current += char;
  }
  cells.push(current.trim());
  return cells;
}

function unquoteSql(value) {
  const text = String(value || '').trim();
  if (text.startsWith("'") && text.endsWith("'")) {
    return text.slice(1, -1).replace(/''/g, "'");
  }
  return text;
}

module.exports = {
  rootDir,
  localeDir,
  supportedLocales,
  readJson,
  writeJson,
  readLocale,
  localePath,
  placeholderSet,
  loadEnv,
  parseDictCoverageFromSql
};
