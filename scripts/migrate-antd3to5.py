"""v2.4 前端迁移批量脚本：antd3->antd5 机械变换（一次性工具，不进构建）"""
import io, os, re, sys

ROOT = os.path.normpath(os.path.join(os.path.dirname(__file__), '..', 'frontend', 'src'))

SKIP = {  # 已人工迁移/无需处理
    os.path.normpath('index.jsx'),
    os.path.normpath('main.less'),
    os.path.normpath('component/Index.jsx'),
    os.path.normpath('component/Login.jsx'),
    os.path.normpath('component/register.jsx'),
    os.path.normpath('component/utils/fetchUtil.jsx'),
    os.path.normpath('component/utils/routerUtil.jsx'),
    os.path.normpath('component/common/Collapse.jsx'),
    os.path.normpath('component/model/ModelList.jsx'),
    os.path.normpath('component/model/modal/AddModel.jsx'),
    os.path.normpath('component/model/modal/CopyModel.jsx'),
    os.path.normpath('component/model/modal/EditModel.jsx'),
    os.path.normpath('component/model/modal/StaticField.jsx'),
}

ICON_MAP = {
    'plus': 'PlusOutlined', 'upload': 'UploadOutlined', 'file-text': 'FileTextOutlined',
    'setting': 'SettingOutlined', 'solution': 'SolutionOutlined', 'pushpin-o': 'PushpinOutlined',
    'picture': 'PictureOutlined', 'logout': 'LogoutOutlined', 'lock': 'LockOutlined',
    'line-chart': 'LineChartOutlined', 'left-circle': 'LeftCircleOutlined', 'home': 'HomeOutlined',
    'delete': 'DeleteOutlined', 'copy': 'CopyOutlined', 'calendar': 'CalendarOutlined',
    'appstore': 'AppstoreOutlined', 'question-circle-o': 'QuestionCircleOutlined',
    'search': 'SearchOutlined', 'close': 'CloseOutlined', 'save': 'SaveOutlined',
    'export': 'ExportOutlined', 'import': 'ImportOutlined', 'download': 'DownloadOutlined',
    'cloud-download': 'CloudDownloadOutlined', 'cloud-upload': 'CloudUploadOutlined',
    'right-circle': 'RightCircleOutlined', 'down': 'DownOutlined', 'up': 'UpOutlined',
    'loading': 'LoadingOutlined', 'user': 'UserOutlined', 'eye-o': 'EyeOutlined',
    'link': 'LinkOutlined', 'edit': 'EditOutlined', 'reload': 'ReloadOutlined',
    'question-circle': 'QuestionCircleOutlined',
}

PARAM_FILES = {
    os.path.normpath(p) for p in [
        'component/abstraction/AbstractionList.jsx', 'component/activation/RuleList.jsx',
        'component/activation/HistoryRecordList.jsx', 'component/activation/Activation.jsx',
        'component/datalist/DatalistRecord.jsx', 'component/datalist/Datalist.jsx',
        'component/field/Field.jsx', 'component/model/Model.jsx',
        'component/modelconfig/ModelConfig.jsx', 'component/modelconfig/modal/EditModelConfParam.jsx',
        'component/preItem/PreItem.jsx', 'component/report/ListRule.jsx',
        'component/report/ListEvent.jsx',
    ]
}

report = []

def prune_icon_from_antd_import(s):
    m = re.search(r"import\s*\{([^}]*)\}\s*from\s*'antd';", s)
    if not m:
        return s
    names = [n.strip() for n in m.group(1).split(',') if n.strip()]
    if 'Icon' in names:
        names = [n for n in names if n != 'Icon']
        s = s[:m.start()] + "import {" + ", ".join(names) + "} from 'antd';" + s[m.end():]
    return s

def transform_icons(s):
    used = []

    def repl(m):
        attrs = m.group(1).strip()
        tm = re.search(r'type="([a-z-]+)"', attrs)
        if not tm:
            return m.group(0)
        t = tm.group(1)
        comp = ICON_MAP.get(t)
        if comp is None:
            report.append('  [icon-unknown] type="%s"' % t)
            return m.group(0)
        rest = attrs[:tm.start()] + attrs[tm.end():]
        rest = re.sub(r'^\s+|\s+$', '', rest)
        rest = re.sub(r'\s{2,}', ' ', rest)
        if comp not in used:
            used.append(comp)
        return '<%s %s/>' % (comp, rest) if rest else '<%s />' % comp

    s = re.sub(r'<Icon\b([^>]*)/>', repl, s)
    return s, used

def inject_icons_import(s, used):
    if not used:
        return s
    imp = "import {%s} from '@ant-design/icons';" % ", ".join(sorted(used))
    if '@ant-design/icons' in s:
        # 合并进已有 import
        m = re.search(r"import\s*\{([^}]*)\}\s*from\s*'@ant-design/icons';", s)
        names = [n.strip() for n in m.group(1).split(',') if n.strip()]
        allnames = sorted(set(names + used))
        s = s[:m.start()] + "import {%s} from '@ant-design/icons';" % ", ".join(allnames) + s[m.end():]
        return s
    m = re.search(r"import\s*\{[^}]*\}\s*from\s*'antd';", s)
    if m:
        return s[:m.end()] + "\n" + imp + s[m.end():]
    return imp + "\n" + s

def transform_visible(s):
    s = s.replace('this.state.visible', 'this.state.open')
    s = s.replace('visible:false', 'open:false')
    s = s.replace('visible:true', 'open:true')
    s = s.replace('const { visible} = this.state;', 'const { open} = this.state;')
    s = s.replace('const { visible } = this.state;', 'const { open } = this.state;')
    s = s.replace('const {visible} = this.state;', 'const {open} = this.state;')
    s = s.replace('const {visible } = this.state;', 'const {open } = this.state;')
    return s

def transform_form(s):
    s = s.replace('<Form horizontal form={this.props.form}>', '<Form>')
    s = re.sub(r'<Form horizontal([^>]*)>', r'<Form\1>', s)
    return s

def transform_breadcrumb(s):
    def repl(m):
        inner = m.group(1)
        items = re.findall(r'<Breadcrumb\.Item>(.*?)</Breadcrumb\.Item>', inner, re.S)
        if not items:
            return m.group(0)
        arr = []
        for it in items:
            it = it.strip()
            em = re.fullmatch(r"\{(.+?)\}", it, re.S)
            if em:
                expr = em.group(1).strip()
                if re.fullmatch(r"'[^']*'", expr) or re.fullmatch(r'"[^"]*"', expr):
                    arr.append(expr)
                else:
                    arr.append(expr)
            else:
                arr.append("'" + it.replace("'", "\\'") + "'")
        return '<Breadcrumb items={[%s]}/>' % ', '.join(arr)
    return re.sub(r'<Breadcrumb>(.*?)</Breadcrumb>', repl, s, flags=re.S)

def add_withparams(s, rel):
    m = re.search(r'export default class (\w+) extends React\.Component\s*\{', s)
    if not m:
        report.append('  [params] 未找到 export default class 模式')
        return s
    name = m.group(1)
    s = s[:m.start()] + 'class ' + name + ' extends React.Component{' + s[m.end():]
    depth = rel.count(os.sep)
    up = '../' * depth + 'utils/routerUtil'
    imp = "import {withParams} from '%s';" % up
    m2 = re.search(r"import\s*\{[^}]*\}\s*from\s*'antd';", s)
    if m2:
        s = s[:m2.end()] + "\n" + imp + s[m2.end():]
    else:
        s = imp + "\n" + s
    s = s.rstrip() + "\n\nexport default withParams(%s);\n" % name
    return s

for dirpath, dirs, files in os.walk(ROOT):
    dirs[:] = [d for d in dirs if d not in ('node_modules', 'dist')]
    for fn in files:
        if not fn.endswith('.jsx'):
            continue
        full = os.path.join(dirpath, fn)
        rel = os.path.normpath(os.path.relpath(full, ROOT))
        if rel in SKIP:
            continue
        s0 = io.open(full, encoding='utf-8').read()
        s = s0
        s = prune_icon_from_antd_import(s)
        s, used = transform_icons(s)
        s = inject_icons_import(s, used)
        s = transform_visible(s)
        s = transform_form(s)
        s = transform_breadcrumb(s)
        if rel in PARAM_FILES:
            s = add_withparams(s, os.path.dirname(rel))
        if s != s0:
            io.open(full, 'w', encoding='utf-8', newline='').write(s)
            print('MIGRATED', rel)

print('---- report ----')
for r in report:
    print(r)
