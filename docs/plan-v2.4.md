# Radar v2.4 实施方案：前端现代化（radar-web 复活：React 18 + antd 5 + Vite）

> 版本：v1.0（2026-11-14）
> 范围：radar-web 管理端前端源码现代化迁移，合入主仓；**后端零改动**
> 状态：方案评审稿
> 前置：v2.2.0 已发布；v2.3（存储演进）暂缓，本次顺延为 v2.4.0；roadmap 见 [roadmap.md](roadmap.md)
> 前端源码仓：gitee.com/freshday/radar-web（本地 `../radar-web`）

---

## 1. 背景与目标

主工程 jar 内嵌的管理端前端（`src/main/resources/static/`，3.4M）是 2022-08 构建的产物，其源码
radar-web 此前一直缺席本仓。现已取回源码并完成盘点：技术栈停留在 React 16.8 / antd 3 / webpack 4，
现代 Node 环境下依赖无法安装（node-sass 4.9 与 webpack3 时代插件），前端处于"有产物、无源码链路"的死档状态。

v2.4 主打**前端工程现代化**：让前端源码在 2026 年的工具链下可安装、可构建、可维护——**技术栈现代化、
业务行为零变化、UI 不改版**。

| # | 目标 | 说明 |
|---|------|------|
| T1 | 前端源码合入主仓 `frontend/`（git subtree 保留历史） | 消除双仓，单仓单流水线 |
| T2 | 构建链现代化：webpack 4 → **Vite**，React 16.8 → **18.3**，antd 3 → **5**，router 3 → **7**，echarts 3 → **5** | 可安装、可构建 |
| T3 | 构建产物继续内嵌 jar `static/`，部署拓扑零变化 | compose 一键体验、"开箱即用"不回退 |
| T4 | 清除死重：node-sass、IE8/9 兼容件（fetch-ie8/es5-shim/es3ify/console-polyfill/babel-runtime）、webpack 全家桶 | 依赖树瘦身 |
| T5 | CI 增加前端构建步骤；文档（README 前端开发说明）同步 | 可持续构建 |

**版本与分支**：v2.4.0（semver minor），分支 `feature/v2.4-frontend-modernization`。
（v2.3 版本号保留给暂缓的存储演进，semver 允许跳号。）

## 2. 现状盘点（2026-11-14 实测）

| 项 | 现状 | 对 v2.4 的意义 |
|----|------|----------------|
| 仓库 | 独立仓 freshday/radar-web，master 最后提交 2022-08-13"fix a bug"，README 为 Gitee 默认模板 | T1 整合对象；模板 README 废弃 |
| 构建 | webpack 4 + babel 7 + yarn.lock；`yarn install` 在 Node 18+ 不可安装（node-sass 4.9、copy-webpack-plugin 2.x、extract-text-webpack-plugin 3.x） | T2 全量替换为 Vite |
| 框架 | React 16.8（**49 个 class 组件、0 个函数组件、无 hooks**）；react-router 3（hashHistory，约 20 条嵌套路由）；echarts 3（仅 RuleGraph.jsx 一处） | React 18 完全兼容 class 组件 → 不重写 |
| antd | 3.16，47 个文件 import；表单模式 = **受控组件 + this.state 手写校验**为主，断代 API 影响面极小：`getFieldDecorator` 仅 register.jsx 一处（6 个字段）、`Form.create` 仅 2 处 | T2 最大风险点实测很小，机械适配即可 |
| 请求层 | 单文件 `fetchUtil.jsx`：FetchUtil 封装（`/services/v1` 前缀、x-auth-token header、60x 未登录全局跳转、错误 Modal、非 GET loading） | 全项目唯一请求出口，原生 fetch 无需换库，改造收敛一个文件 |
| 认证 | localStorage `x-auth-token`；登录 `/user/login`，登出清空 | 后端契约 v2.0 起未动，零适配 |
| 样式 | 9 个 less 文件（无任何 scss）；覆盖 antd 类名的自定义布局 less | Vite 原生支持 less；与 antd 5 cssinjs 共存需核对 |
| 内嵌产物 | `static/main.ba8e3789.js` = 本源码 2022-08 构建产物；`static/images/`、`static/res/`（dataRecord_temp.xlsx 导入模板）为拷贝资源 | T3 产物替换时 images/res 必须保留 |
| dev 代理 | webpack devServer 3000 → localhost:6580，与当前后端端口一致 | Vite server.proxy 照搬 |
| 本机环境 | Node v22.13.1 / npm 10.9.2 | Vite 就绪；CI 用同版本 |
| 后端回归基线 | L1/L2/L3 全绿（v2.2.0），前端改造后必须保持全绿 | 后端零改动验收线 |

## 3. 技术决策（定案，不再讨论）

| 决策 | 选择 | 理由 |
|------|------|------|
| 仓库形态 | `git subtree add --prefix=frontend` 合入（保留提交历史），随后一个整理 commit 将 radar-admin 内容上移至 `frontend/` 根、删除模板 README/.gitee；原仓封存只读 | 单仓单流水线（呼应 roadmap P2-4 双仓成本）；历史可追溯。若 subtree 受阻可退化为普通拷贝（历史放弃），验收不受影响 |
| 构建工具 | **Vite**（`@vitejs/plugin-react`），配置文件用 `.js` 不引 TS | 开箱 ESM、antd 5 无需按需编译插件；与"不引 TS"决策配套 |
| React | **18.3，49 个 class 组件原样保留，不 hooks 化** | 本次目标是栈现代化+行为零变化；React 18 官方支持 class 组件无废弃计划；hooks 化列入 roadmap P2 渐进项，防止范围膨胀 |
| TypeScript | **不引入**（保持 .jsx） | 64 个文件机械迁移最小化；类型化列后续可选 |
| antd | **3 → 5**：Icon 换 `@ant-design/icons` 具名图标；`Menu.SubMenu`/`Breadcrumb` children → `items`；`Select`/`Cascader` children → `options`；`<Form horizontal>` → `layout` 属性；message/Modal 静态方法沿用（无 Context 依赖） | 官方迁移路径；实测断代影响面小（§2） |
| 路由 | **react-router 7（library mode）+ `createHashRouter`，保持 hash 路由** | 旧链接/收藏夹兼容；后端无需加 history fallback 配置；v7 = v6 API 超集 |
| 路由参数获取 | class 组件统一包函数组件 wrapper（`useParams`/`useNavigate` 注入 props），不逐个改写为函数组件 | class 组件在 router 6/7 无 withRouter；wrapper 改造最机械、可测 |
| echarts | **3 → 5**，仅 RuleGraph.jsx 一处 graph 图适配 | API 兼容度高 |
| 日期库 | **moment 删除，换 dayjs**（6 个文件 import moment） | antd 5 内部即 dayjs；少维护一个 173KB 的历史包袱 |
| UI 视觉 | **不改版**：布局结构/页面样式保持 2022 版观感，只修 antd 5 cssinjs 引起的错位 | "前端现代化"范围 = 工程链路，UI 翻新列 roadmap 后续项 |
| 浏览器基线 | 现代常青浏览器（Chrome/Edge/Firefox/Safari 近两年版本），IE 全放弃 | IE 兼容件全删的依据 |
| 包管理 | npm（CI 与本机一致，package-lock.json），yarn.lock 废弃 | 减少一个工具链 |
| 部署形态 | **产物继续内嵌 jar**：`scripts/build-frontend.sh`（npm ci → vite build → 同步 `static/`，保留 images/res）→ `mvn package`；Dockerfile / compose **零改动**；CI 在 maven 步骤前执行前端构建 | 一键体验不回退；无 GHCR/无 node 环境用户不受影响 |
| dev 模式 | `npm run dev`：Vite 3000 端口，`/services` 代理 → localhost:6580（对齐原代理） | 前后端分离开发体验保持 |

## 4. 实施内容

### 4.1 T1：仓库整合（S0）

- `git subtree add --prefix=frontend https://gitee.com/freshday/radar-web.git master --squash`
  （或带全历史，实施时定；squash 更干净，原仓本身就是封存态）
- 整理 commit：`frontend/radar-admin/*` 上移至 `frontend/`；删除 Gitee 模板 README/.gitee
- 主仓 `.gitignore` 增加 `frontend/node_modules/`、`frontend/dist/`

### 4.2 T2：构建链替换（S0~S1）

- 重写 `frontend/package.json`：dependencies = react 18.3 / react-dom 18.3 / antd 5 / react-router 7 /
  echarts 5 / dayjs / @ant-design/icons / less；devDependencies = vite / @vitejs/plugin-react；删除 node-sass、
  全部 webpack/babel 生态、IE 兼容件、babel-runtime
- `vite.config.js`：root/alias `@` → `./src`、build.outDir、`server.port=3000`、`server.proxy['/services'] → http://localhost:6580`
- 入口 `index.html` 重写为 Vite 形态（module script 引 src/index.jsx；删 IE 条件注释与 polyfill CDN）

### 4.3 代码迁移（S1~S4，机械改造为主）

| 块 | 文件面 | 内容 |
|----|--------|------|
| 请求层 | fetchUtil.jsx（1 文件） | 删 whatwg-fetch/fetch-ie8 import，其余逻辑（token/60x 跳转/错误 Modal）原样保留 |
| 路由 | index.jsx + Index.jsx | AppRoutes → createHashRouter 路由表（约 20 条照搬）+ RouterProvider；布局组件 `this.props.children` → `<Outlet/>` |
| 登录注册 | Login.jsx / register.jsx | register 的 `Form.create + getFieldDecorator`（6 字段）→ antd 5 Form 组件 + form 实例；Login 原受控写法基本不动 |
| antd 图标 | 全局（grep `Icon type=`） | `<Icon type="home"/>` → `<HomeOutlined/>` 具名导入 |
| 菜单/面包屑 | Index.jsx / 各页面 Breadcrumb | children 写法 → `items` |
| Select/Cascader | 各 modal | children/Option 写法 → `options` 数据驱动 |
| Form 杂项 | 47 个 import 文件 | `Form.Item` 保留（API 兼容）；`<Form horizontal>` → 去属性；size="large" 等微调 |
| moment | 6 文件 | → dayjs（DatePicker 场景为主，逐一核对格式化串） |
| echarts | RuleGraph.jsx（1 文件） | echarts 5 init/setOption，graph series 配置核对 |

### 4.4 T3：构建集成（S5）

- `scripts/build-frontend.sh`：`cd frontend && npm ci && npm run build`，dist 产物同步
  `src/main/resources/static/`（**先清 index.*.* / main.*.* 旧产物，保留 images/、res/**）
- `ci.yml`：加 `setup-node 22` job 步骤（frontend npm ci + build），再走既有 maven 流程；`release.yml` 同步
- Dockerfile / docker-compose.yml 零改动（产物已内嵌 jar）
- README 快速开始补两种模式：一键（compose / 内嵌 jar）与前端开发模式（`npm run dev` + 代理）

### 4.5 回归与验收（S6）

- 后端：L1/L2 本地全绿 + L3 `mvn verify` 全绿（零后端改动证明）
- 前端手测清单（对着 2022 版基线逐页比对行为）：
  登录/注册 → 模型列表/新建/copy/编辑/删除 → 字段 → 抽象 → 激活/阈值 → 规则列表 → 名单/记录 →
  preItem → 报表（事件/规则命中/关系图/看板）→ 配置中心 → 退出登录
- 关键链路：登录 → enable 模型 → 事件上传命中 → getScore（复用 L3 十步链路的手工对应版）
- 测试服务器 124.221.202.39 部署 v2.4.0-SNAPSHOT 验证（verify-chain.sh 十步 + 页面抽查）

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S0 | subtree 合入 + Vite 脚手架（依赖替换/入口/代理） | `npm run dev` 白屏起得来 | 1 人日 |
| S1 | 请求层 + 路由表 + 登录/注册 + 主框架（图标/菜单） | 登录后进入模型列表页 | 1.5 人日 |
| S2 | 模型域迁移：model/field/modelconfig/abstraction | 页面行为对齐基线 | 2 人日 |
| S3 | 规则与数据域：activation/RuleList/datalist/preItem | 页面行为对齐基线 | 2 人日 |
| S4 | 报表域 + echarts 5 + 配置中心 + dayjs 替换收尾 | 全页面可走通 | 1.5 人日 |
| S5 | 构建集成：脚本 + CI + jar 内嵌验证 | `build-frontend.sh` + `mvn package` 出可用 jar | 1 人日 |
| S6 | 手测清单回归 + 测试服务器部署验证 | 全绿记录 | 1 人日 |
| S7 | 文档 + 发布准备 v2.4.0（CHANGELOG/tag，按发布规范） | 发布 | 0.5 人日 |

**合计约 10.5 人日**。S0→S1→S2→S3→S4 串行（路由/主框架先立，各业务域逐个搬），S5 可在 S4 后并行准备，
S6→S7 收尾。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| antd 5 cssinjs 与旧 less 覆盖冲突（自定义 .ant-layout-* 类名布局） | antd 5 仍输出同名类名，先直跑看错位面；错位处以局部 less 修正为准，不改全局主题 |
| react-router 3 → 7 语义差异（children→Outlet、props.params→useParams） | wrapper 注入统一解决；路由表迁移后先手测全 20 条路由跳转再进业务域 |
| Select/Cascader value 类型行为差异（受控值 object/string） | 逐 modal 对照 2022 版行为；手测清单覆盖新增/编辑双路径 |
| Vite 产物 hash 文件名与 Spring static 相对引用 | index.html 产物为相对路径引用，实测 jar 内 404 即改 base:'./' |
| images/res 资源误清导致 favicon/模板下载断链 | build 脚本只清 `index.*.*` / `main.*.*` 模式文件；verify-chain.sh 加资源 200 检查 |
| subtree 操作不可逆担忧 | 原仓保留不动（封存只读），主仓操作全部可 revert；必要时退化为普通拷贝 |
| Node 22 与 CI 环境漂移 | CI setup-node 22 与本机同版本；lockfile 提交 |
| 范围膨胀（UI 改版/hook 化/TS 化的顺手诱惑） | 本方案定案明确排除；一律记 roadmap 后续项 |

## 7. 验收标准

1. `frontend/` 单仓内 `npm ci && npm run dev` 可起前端并代理后端；`npm run build` 出 dist
2. `scripts/build-frontend.sh` + `mvn package` 出 jar，内嵌前端全页面可用；Dockerfile/compose 未改动
3. 手测清单（§4.5）全过；后端 L1/L2/L3 全绿（证明零后端改动）
4. CI 绿：前端构建 + maven 全量两段流水线；测试服务器部署验证通过
5. 依赖树：node-sass / webpack / babel / IE 兼容件 / moment 清零；React 18.3 / antd 5 / router 7 / Vite / dayjs 就位
6. v2.4.0 按发布规范：tag + CHANGELOG + GitHub Release + GHCR 镜像
