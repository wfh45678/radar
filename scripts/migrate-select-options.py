"""v2.4 迁移：antd3 Select 子组件 Option 写法 -> antd5 options 属性（一次性工具）"""
import io, os, re

ROOT = os.path.normpath(os.path.join(os.path.dirname(__file__), '..', 'frontend', 'src'))

report = []

def comment_spans(s):
    spans = []
    for m in re.finditer(r'\{/\*.*?\*/\}', s, re.S):
        spans.append((m.start(), m.end()))
    return spans

def in_spans(pos, spans):
    return any(a <= pos < b for a, b in spans)

def parse_expr(tok):
    """'{expr}' -> expr；纯文本 -> 带引号字符串"""
    t = tok.strip()
    m = re.fullmatch(r"\{(.+?)\}", t, re.S)
    if m:
        return m.group(1).strip()
    return "'" + t.replace("'", "\\'") + "'"

def convert_map(seg):
    """LIST.map((x,index)=> <Option key={} value={}>{L}</Option>) -> LIST.map((x,index)=>({label:L, value:V}))"""
    inner = re.match(r'\{(.+)\}$', seg.strip(), re.S)
    expr = inner.group(1).strip()
    # 条件守卫 {COND?null:MAP}
    guard = ''
    gm = re.match(r'(.+\?\s*null\s*:\s*)(.+)$', expr, re.S)
    if gm:
        guard = gm.group(1)
        expr = gm.group(2).strip()
    mm = re.match(r'(.+?)\.map\(\s*(\(?[\w, ]+\)?)\s*=>\s*<Option\b([^>]*)>(.*?)</Option>\s*\)\s*$', expr, re.S)
    if not mm:
        return None
    lst, params, attrs, label = mm.group(1).strip(), mm.group(2), mm.group(3), mm.group(4)
    vm = re.search(r'value=\{(.+?)\}', attrs, re.S)
    if not vm:
        return None
    val = vm.group(1).strip()
    # params: (x,index) 或 x —— 仅当 index 未被 label/value 使用才可去掉
    p = params.strip()
    if p.startswith('(') and p.endswith(')'):
        p = p[1:-1].strip()
    pl = [x.strip() for x in p.split(',')]
    uses_index = re.search(r'\b%s\b' % pl[-1], label + ' ' + val) if len(pl) > 1 else False
    new_params = p if uses_index else (pl[0])
    return '%s%s.map(%s=>({label:%s, value:%s}))' % (guard, lst, new_params, parse_expr(label), val)

def convert_block(attrs, content, rel):
    segs = []
    pos = 0
    for m in re.finditer(r'<Option\b[^>]*>.*?</Option>|\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}', content, re.S):
        if m.start() != pos and content[pos:m.start()].strip():
            return None
        segs.append(m.group(0))
        pos = m.end()
    if content[pos:].strip():
        return None
    opts = []
    for seg in segs:
        if seg.startswith('<Option'):
            vm = re.search(r'value=(?:"([^"]*)"|\{(.+?)\})', seg, re.S)
            lm = re.search(r'>(.*)</Option>', seg, re.S)
            if not vm or lm is None:
                return None
            val = "'" + vm.group(1) + "'" if vm.group(1) is not None else vm.group(2).strip()
            opts.append('{label:%s, value:%s}' % (parse_expr(lm.group(1)), val))
        else:
            conv = convert_map(seg)
            if conv is None:
                return None
            opts.append('...' + conv)
    if not opts:
        return None
    return '<Select%s options={[%s]}/>' % (attrs, ', '.join(opts))

def process(rel):
    full = os.path.join(ROOT, rel)
    s = io.open(full, encoding='utf-8').read()
    spans = comment_spans(s)
    changed = 0
    out = []
    last = 0
    for m in re.finditer(r'<Select\b([^>]*)>(.*?)</Select>', s, re.S):
        if in_spans(m.start(), spans):
            continue
        new = convert_block(m.group(1), m.group(2), rel)
        if new is None:
            report.append('  [manual] %s' % rel)
            continue
        out.append(s[last:m.start()])
        out.append(new)
        last = m.end()
        changed += 1
    if changed:
        out.append(s[last:])
        s = ''.join(out)
        # Option 不再使用则清理
        if '<Option' not in s and 'Option' in s:
            s = re.sub(r'\n?const Option = Select\.Option;\n?', '\n', s)
            m = re.search(r"import\s*\{([^}]*)\}\s*from\s*'antd';", s)
            if m and 'Option' in [n.strip() for n in m.group(1).split(',')]:
                names = [n.strip() for n in m.group(1).split(',') if n.strip() and n.strip() != 'Option']
                s = s[:m.start()] + "import {" + ", ".join(names) + "} from 'antd';" + s[m.end():]
        io.open(full, 'w', encoding='utf-8', newline='').write(s)
    print('%-55s %d 处' % (rel, changed))

for dirpath, dirs, files in os.walk(ROOT):
    dirs[:] = [d for d in dirs if d not in ('node_modules', 'dist')]
    for fn in files:
        if not fn.endswith('.jsx'):
            continue
        full = os.path.join(dirpath, fn)
        s = io.open(full, encoding='utf-8').read()
        if '<Option' in s:
            process(os.path.relpath(full, ROOT))

print('---- 需人工处理 ----')
for r in sorted(set(report)):
    print(r)
