// JSX 引用静态扫描（v5.0 起 CI 门禁）：检查所有 .jsx/.js 中大写 JSX 标签是否有可解析的绑定
// （import 绑定 / 本地定义 / 解构 / 函数参数）。已知局限：不追踪跨文件 re-export、动态引用。
const fs = require('fs'), path = require('path');
function walk(d, acc = []) {
  for (const f of fs.readdirSync(d)) {
    const p = path.join(d, f);
    fs.statSync(p).isDirectory() ? walk(p, acc) : (p.endsWith('.jsx') || p.endsWith('.js')) && acc.push(p);
  }
  return acc;
}
function stripComments(src) {
  return src.replace(/\/\*[\s\S]*?\*\//g, '').replace(/(^|[^:])\/\/[^\n]*/g, '$1');
}
const problems = [];
for (const f of walk(path.join(__dirname, '..', 'frontend', 'src'))) {
  let src = stripComments(fs.readFileSync(f, 'utf8'));
  const names = new Set();
  for (const m of src.matchAll(/import\s+(?:([\w$]+)\s*,?\s*)?(?:{([^}]*)})?\s*(?:from\s*['"][^'"]+['"])?/g)) {
    if (m[1]) names.add(m[1]);
    if (m[2]) for (const part of m[2].split(',')) {
      const local = part.split(/\s+as\s+/).pop().trim();
      if (local) names.add(local);
    }
  }
  for (const m of src.matchAll(/(?:function|class|const|let|var)\s+([\w$]+)/g)) names.add(m[1]);
  for (const m of src.matchAll(/(?:const|let|var)\s*{([^}]+)}\s*=/g)) {
    for (const part of m[1].split(',')) {
      const local = part.split(/\s*:\s*/).pop().trim();
      if (local) names.add(local);
    }
  }
  // 函数参数（HOC/回调）：function X ( A ) / ( A ) => / ( A, B ) =>
  for (const m of src.matchAll(/function\s+[\w$]+\s*\(([^)]*)\)/g)) {
    for (const part of m[1].split(',')) {
      const p = part.trim().split(/[\s=]/)[0];
      if (p) names.add(p);
    }
  }
  for (const m of src.matchAll(/[(,]\s*([\w$]+)\s*\)\s*=>/g)) names.add(m[1]);
  for (const m of src.matchAll(/\(\s*([\w$]+)\s*\)\s*=>/g)) names.add(m[1]);
  const used = new Set();
  for (const m of src.matchAll(/<([A-Z][\w$]*)[\s/>]/g)) used.add(m[1]);
  const missing = [...used].filter(u => !names.has(u));
  if (missing.length) problems.push(path.relative(process.cwd(), f) + ' → ' + missing.join(', '));
}
if (problems.length) {
  console.log(problems.join('\n'));
  console.log('---- 问题文件数:', problems.length);
  process.exit(1);
}
console.log('JSX 引用扫描：全部干净');
