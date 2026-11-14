# Radar 管理端前端（radar-admin）

风控引擎管理平台 SPA，React 18 + antd 5 + Vite + react-router 7 + echarts 5 + dayjs。
v2.4.0 起源码入仓（由独立仓 radar-web 经 git subtree 合入，2022-08 封存版现代化迁移而来，
URL 结构与旧版完全兼容），构建产物提交于 `src/main/resources/static/` 内嵌 jar。

## 开发

```bash
npm ci
npm run dev        # http://localhost:3000，/services 代理到 localhost:6580
# 联调其它后端：
RADAR_API_TARGET=http://测试服务器:6580 npm run dev
```

## 构建

```bash
npm run build                      # 产物出 dist/
# 仓库根执行，同步产物到 jar 内嵌静态目录（保留 images/ res/）：
../scripts/build-frontend.sh
```

改了 `frontend/` 源码后必须重跑 `scripts/build-frontend.sh` 并提交
`src/main/resources/static/`（CI 校验两者不漂移）。`mvn package` 无需 node。

## 结构

```
src/index.jsx                 入口：createHashRouter 路由表（URL 与 2022 版兼容）+ ConfigProvider zhCN
src/component/Index.jsx       顶部导航布局（pathless layout route）
src/component/model/Model.jsx 模型工作区布局（字段/预处理/名单/抽象/配置/策略 六个 Tab）
src/component/report/Report.jsx 报表工作区布局（调用查询/规则命中/数据看板，经 Outlet context 向子页注入数据）
src/component/utils/fetchUtil.jsx  全站唯一请求封装（x-auth-token、60x 未登录跳转、尾斜杠剥离）
src/component/utils/routerUtil.jsx withParams/withLocation/withOutletCtx（class 组件的路由桥接）
```

## 迁移约定（保持 2022 版行为）

- 49 个 class 组件原样保留（React 18 兼容），不 hooks 化、不引 TypeScript、UI 不改版
- antd 5：`visible→open`、Icon 具名图标、Select 用 `options`、Menu/Breadcrumb 用 `items`、
  `Form.create→Form.useForm`；locale 需显式 `ConfigProvider locale=zhCN`
- 旧生命周期已迁移（React 17+ 移除 `componentWillReceiveProps/WillMount`）
- fetchUtil 统一剥离 URL 尾斜杠（Spring 6 取消尾斜杠匹配，修复旧版 CRUD 400）
