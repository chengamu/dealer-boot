const fs = require('fs');
const path = require('path');
const {
  readSourceLocale,
  rootDir
} = require('./lib.ts');

const sourceRoots = [
  path.join(rootDir, 'admin-ui', 'src')
];

const sourceExtensions = new Set(['.js', '.jsx', '.ts', '.tsx', '.vue']);

const dynamicKeyAllowList = [
  'apply.legalPlaceholder.*',
  'dashboard.relativeTime.*',
  'dict.*',
  'errorCode.*',
  'productCenter.formula.status.*'
];

function globToRegExp(pattern) {
  const escaped = pattern.replace(/[.+?^${}()|[\]\\]/g, '\\$&');
  return new RegExp(`^${escaped.replace(/\*/g, '.*')}$`);
}

const dynamicKeyAllowMatchers = dynamicKeyAllowList.map(globToRegExp);

function listSourceFiles(dir) {
  if (!fs.existsSync(dir)) return [];
  const files = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === 'node_modules' || entry.name === 'dist') continue;
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...listSourceFiles(fullPath));
    } else if (sourceExtensions.has(path.extname(entry.name))) {
      files.push(fullPath);
    }
  }
  return files;
}

function lineNumber(text, index) {
  return text.slice(0, index).split(/\r?\n/).length;
}

function isAllowedDynamic(pattern) {
  return dynamicKeyAllowMatchers.some((matcher) => matcher.test(pattern));
}

function isTranslationLikeCall(text, matchIndex, callee) {
  if (callee === '$t') return true;
  if (callee === 't' || callee === 'te') {
    const prefix = text.slice(Math.max(0, matchIndex - 20), matchIndex);
    return !/[A-Za-z0-9_$]$/.test(prefix);
  }
  return true;
}

function findCalls(file, content) {
  const calls = [];
  const callPattern = /(?:\b(getMessage|t|te)|(\$t))\s*\(\s*(['"`])((?:\\.|(?!\3)[\s\S])*?)\3/g;
  let match;

  while ((match = callPattern.exec(content)) !== null) {
    const callee = match[1] || match[2];
    if (!isTranslationLikeCall(content, match.index, callee)) continue;

    const quote = match[3];
    const value = match[4];
    const location = {
      file: path.relative(rootDir, file),
      line: lineNumber(content, match.index),
      key: value
    };

    if (quote === '`' && value.includes('${')) {
      calls.push({
        ...location,
        dynamic: true,
        pattern: value.replace(/\$\{[^}]+\}/g, '*')
      });
    } else {
      calls.push({
        ...location,
        dynamic: false
      });
    }
  }

  return calls;
}

function main() {
  const { messages } = readSourceLocale('en_US');
  const knownKeys = new Set(Object.keys(messages));
  const errors = [];
  const dynamicErrors = [];
  let staticCount = 0;
  let dynamicCount = 0;

  for (const sourceRoot of sourceRoots) {
    for (const file of listSourceFiles(sourceRoot)) {
      const content = fs.readFileSync(file, 'utf8');
      for (const call of findCalls(file, content)) {
        if (call.dynamic) {
          dynamicCount += 1;
          if (!isAllowedDynamic(call.pattern)) {
            dynamicErrors.push(call);
          }
          continue;
        }

        staticCount += 1;
        if (!knownKeys.has(call.key)) {
          errors.push(call);
        }
      }
    }
  }

  if (errors.length || dynamicErrors.length) {
    console.error(`[i18n:keys] failed with ${errors.length + dynamicErrors.length} issue(s).`);
    for (const item of errors.slice(0, 80)) {
      console.error(`- missing key ${item.key} at ${item.file}:${item.line}`);
    }
    for (const item of dynamicErrors.slice(0, 40)) {
      console.error(`- dynamic key pattern ${item.pattern} is not allowed at ${item.file}:${item.line}`);
    }
    if (errors.length > 80 || dynamicErrors.length > 40) {
      console.error(`... ${Math.max(0, errors.length - 80) + Math.max(0, dynamicErrors.length - 40)} more`);
    }
    process.exit(1);
  }

  console.log(`[i18n:keys] ${staticCount} static key reference(s) checked; ${dynamicCount} dynamic pattern(s) allowed.`);
}

main();
