const fs = require('fs');
const path = require('path');
const {
  readSourceLocale,
  rootDir
} = require('./lib.ts');

const sourceExtensions = new Set(['.java']);
const maxOutput = 120;

const keyPatterns = [
  /\bMessageUtils\.message\s*\(\s*"([A-Za-z0-9_.-]+)"/g,
  /\bServiceException\.ofMessageKey\s*\(\s*"([A-Za-z0-9_.-]+)"/g,
  /\.setMessageKey\s*\(\s*"([A-Za-z0-9_.-]+)"/g,
  /\bmessage\s*=\s*"\{([A-Za-z0-9_.-]+)\}"/g,
  /\bdefault\s*"\{([A-Za-z0-9_.-]+)\}"/g,
  /\bsuper\s*\(\s*"([A-Za-z0-9_.-]+)"\s*,\s*new Object\[\]/g
];

function sourceRoots() {
  return fs.readdirSync(rootDir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && entry.name.startsWith('bocoo-'))
    .map((entry) => path.join(rootDir, entry.name));
}

function listSourceFiles(dir) {
  if (!fs.existsSync(dir)) return [];
  const files = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === 'target' || entry.name === 'node_modules') continue;
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

function relativePath(file) {
  return path.relative(rootDir, file).split(path.sep).join('/');
}

function findKeys(file, content) {
  const references = [];
  for (const pattern of keyPatterns) {
    pattern.lastIndex = 0;
    let match;
    while ((match = pattern.exec(content)) !== null) {
      references.push({
        file: relativePath(file),
        line: lineNumber(content, match.index),
        key: match[1]
      });
    }
  }
  return references;
}

function main() {
  const { messages } = readSourceLocale('en_US');
  const knownKeys = new Set(Object.keys(messages));
  const missing = [];
  let referenceCount = 0;
  let fileCount = 0;

  for (const sourceRoot of sourceRoots()) {
    for (const file of listSourceFiles(sourceRoot)) {
      fileCount += 1;
      const content = fs.readFileSync(file, 'utf8');
      for (const reference of findKeys(file, content)) {
        referenceCount += 1;
        if (!knownKeys.has(reference.key)) {
          missing.push(reference);
        }
      }
    }
  }

  if (missing.length) {
    console.error(`[i18n:java-keys] failed with ${missing.length} missing key reference(s).`);
    for (const item of missing.slice(0, maxOutput)) {
      console.error(`- missing key ${item.key} at ${item.file}:${item.line}`);
    }
    if (missing.length > maxOutput) {
      console.error(`... ${missing.length - maxOutput} more missing key reference(s) omitted.`);
    }
    process.exit(1);
  }

  console.log(`[i18n:java-keys] ${referenceCount} Java key reference(s) checked across ${fileCount} file(s).`);
}

main();
