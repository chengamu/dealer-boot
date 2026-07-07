const fs = require('fs');
const path = require('path');
const {
  rootDir
} = require('./lib.ts');

const sourceRoots = [
  path.join(rootDir, 'admin-ui', 'src')
];

const sourceExtensions = new Set(['.js', '.jsx', '.ts', '.tsx', '.vue']);
const maxOutput = 120;
const baselinePath = path.join(rootDir, 'i18n', 'hardcoded-baseline.json');

const ignoredPathPatterns = [
  /^admin-ui\/src\/agent\//
];

const ignoredLinePatterns = [
  /\bdata-agent-/,
  /\bi18n-key\b/,
  /\bi18nKey\b/,
  /message\.includes\('验证码'\)/
];

const formulaDslLinePatterns = [
  /['"]?(?:订单宽|订单长|订单面积|宽cm|长cm|面积m²|面积m2|宽|长|面积|店铺|面料|产品类型|配置项值)(?:\(in\)|\(cm\)|\(m²\)|in|cm|m²|m2)?['"]?\s*:/,
  /\b(?:四舍五入|向上取整|向下取整):\s*'/,
  /\blabel:\s*'(?:订单宽|订单长|订单面积|面料|产品类型|配置项值)/,
  /\.replace\(.+[×÷（），并且或者]/,
  /joiner:\s*'并且'/,
  /\bjoiner.+(?:并且|或者)/,
  /value="(?:并且|或者)"/,
  /\binsert:\s*.*(?:物料类型|物料编码|物料名称|属性分组|四舍五入|向上取整|向下取整)/,
  /\bconst operators\b.*(?:四舍五入|向上取整|向下取整|并且|或者)/,
  /\bpushAlias\(aliases,.*(?:四舍五入|向上取整|向下取整|厚度|类型|编码|名称|名字|物料类型|物料编码|物料名称|属性分组)/,
  /includes\('面料'\)/
];

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

function relativePath(file) {
  return path.relative(rootDir, file).split(path.sep).join('/');
}

function isIgnoredPath(file) {
  const normalized = relativePath(file);
  return ignoredPathPatterns.some((pattern) => pattern.test(normalized));
}

function isFormulaDslLine(file, line) {
  const normalized = relativePath(file);
  if (!normalized.startsWith('admin-ui/src/pages/product-formula/')) return false;
  return formulaDslLinePatterns.some((pattern) => pattern.test(line));
}

function shouldIgnoreLine(file, line) {
  const trimmed = line.trim();
  if (!trimmed) return true;
  if (trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*')) return true;
  if (trimmed.startsWith('<!--') || trimmed.startsWith('-->')) return true;
  if (isFormulaDslLine(file, line)) return true;
  return ignoredLinePatterns.some((pattern) => pattern.test(line));
}

function stripBlockComments(line, state) {
  let output = '';
  let index = 0;

  while (index < line.length) {
    if (state.inBlockComment) {
      const end = line.indexOf('*/', index);
      if (end < 0) return output;
      state.inBlockComment = false;
      index = end + 2;
      continue;
    }

    const start = line.indexOf('/*', index);
    if (start < 0) {
      output += line.slice(index);
      break;
    }

    output += line.slice(index, start);
    state.inBlockComment = true;
    index = start + 2;
  }

  return output;
}

function excerpt(text) {
  return text.trim().replace(/\s+/g, ' ').slice(0, 180);
}

function findStringLiterals(line, valuePattern) {
  const matches = [];
  const stringPattern = /(^|[^A-Za-z0-9_$])(['"`])((?:\\.|(?!\2).)*)\2/g;
  let match;

  while ((match = stringPattern.exec(line)) !== null) {
    if (valuePattern.test(match[3])) {
      matches.push(match[3]);
    }
  }

  return matches;
}

function findCjkStringLiterals(line) {
  return findStringLiterals(line, /[\u3400-\u9FFF]/);
}

function findCjkTextNodes(line) {
  const matches = [];
  const textPattern = />\s*([^<{]*[\u3400-\u9FFF][^<{]*)\s*</g;
  let match;

  while ((match = textPattern.exec(line)) !== null) {
    matches.push(match[1]);
  }

  return matches;
}

function findElementMessageLiterals(line) {
  if (!/\b(?:ElMessage|ElNotification)(?:\.[A-Za-z]+)?\s*\(|\bElMessageBox\.(?:confirm|alert|prompt)\s*\(/.test(line)) return [];
  if (/\b(?:getMessage|t)\s*\(/.test(line) || /\$t\s*\(/.test(line)) return [];
  return findStringLiterals(line, /[A-Za-z]/)
    .filter((value) => !/[\u3400-\u9FFF]/.test(value))
    .filter((value) => !/^(success|warning|error|info)$/i.test(value));
}

function addCandidate(candidates, file, lineNumber, type, value, sourceLine) {
  candidates.push({
    file: relativePath(file),
    line: lineNumber,
    type,
    value: excerpt(value),
    source: excerpt(sourceLine)
  });
}

function candidateKey(item) {
  return [item.file, item.type, item.value, item.source].join('\u0000');
}

function sortCandidates(candidates) {
  return [...candidates].sort((left, right) => {
    const fileCompare = left.file.localeCompare(right.file);
    if (fileCompare !== 0) return fileCompare;
    const typeCompare = left.type.localeCompare(right.type);
    if (typeCompare !== 0) return typeCompare;
    const valueCompare = left.value.localeCompare(right.value);
    if (valueCompare !== 0) return valueCompare;
    return left.source.localeCompare(right.source);
  });
}

function readBaseline() {
  if (!fs.existsSync(baselinePath)) {
    return {
      entries: []
    };
  }
  return JSON.parse(fs.readFileSync(baselinePath, 'utf8'));
}

function writeBaseline(candidates) {
  const entries = sortCandidates(candidates).map((item) => ({
    file: item.file,
    type: item.type,
    value: item.value,
    source: item.source
  }));
  const lines = [
    '{',
    '  "version": 1,',
    '  "description": "Historical frontend hardcoded i18n candidates. New candidates must be fixed or deliberately added with pnpm i18n:hardcoded:update.",',
    '  "entries": ['
  ];
  entries.forEach((entry, index) => {
    const suffix = index === entries.length - 1 ? '' : ',';
    lines.push(`    ${JSON.stringify(entry)}${suffix}`);
  });
  lines.push('  ]', '}');
  fs.writeFileSync(baselinePath, `${lines.join('\n')}\n`, 'utf8');
}

function scanFile(file) {
  const candidates = [];
  const lines = fs.readFileSync(file, 'utf8').split(/\r?\n/);
  const state = {
    inBlockComment: false
  };

  lines.forEach((rawLine, index) => {
    const lineNumber = index + 1;
    const line = stripBlockComments(rawLine, state);
    if (shouldIgnoreLine(file, line)) return;

    for (const value of findCjkStringLiterals(line)) {
      addCandidate(candidates, file, lineNumber, 'cjk-string', value, rawLine);
    }
    for (const value of findCjkTextNodes(line)) {
      addCandidate(candidates, file, lineNumber, 'cjk-text', value, rawLine);
    }
    for (const value of findElementMessageLiterals(line)) {
      addCandidate(candidates, file, lineNumber, 'element-message', value, rawLine);
    }
  });

  return candidates;
}

function main() {
  const updateBaseline = process.argv.includes('--update-baseline');
  const candidates = [];
  let scannedFiles = 0;
  let ignoredFiles = 0;

  for (const sourceRoot of sourceRoots) {
    for (const file of listSourceFiles(sourceRoot)) {
      if (isIgnoredPath(file)) {
        ignoredFiles += 1;
        continue;
      }
      scannedFiles += 1;
      candidates.push(...scanFile(file));
    }
  }

  if (updateBaseline) {
    writeBaseline(candidates);
    console.log(`[i18n:hardcoded] baseline updated with ${candidates.length} candidate(s).`);
    return;
  }

  if (!candidates.length) {
    console.log(`[i18n:hardcoded] ${scannedFiles} file(s) scanned; no hardcoded UI text candidates found.`);
    return;
  }

  const baseline = readBaseline();
  const baselineKeys = new Set((baseline.entries || []).map(candidateKey));
  const currentKeys = new Set(candidates.map(candidateKey));
  const newCandidates = candidates.filter((item) => !baselineKeys.has(candidateKey(item)));
  const staleEntries = (baseline.entries || []).filter((item) => !currentKeys.has(candidateKey(item)));

  if (newCandidates.length) {
    console.error(`[i18n:hardcoded] failed with ${newCandidates.length} new hardcoded candidate(s) in ${scannedFiles} file(s); ${ignoredFiles} file(s) ignored.`);
    for (const item of sortCandidates(newCandidates).slice(0, maxOutput)) {
      console.error(`- ${item.type} at ${item.file}:${item.line} -> ${item.value}`);
      console.error(`  ${item.source}`);
    }
    if (newCandidates.length > maxOutput) {
      console.error(`... ${newCandidates.length - maxOutput} more new candidate(s) omitted.`);
    }
    console.error('- Fix visible text with i18n keys, or update the historical baseline only after review with pnpm i18n:hardcoded:update.');
    process.exit(1);
  }

  if (staleEntries.length) {
    console.warn(`[i18n:hardcoded] ${staleEntries.length} stale baseline entrie(s) can be removed with pnpm i18n:hardcoded:update.`);
  }
  console.log(`[i18n:hardcoded] ${candidates.length} candidate(s) match baseline; no new hardcoded UI text found in ${scannedFiles} file(s).`);
}

main();
