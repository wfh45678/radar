import { defineConfig } from 'vitepress';
import { withMermaid } from 'vitepress-plugin-mermaid';

// 文档唯一源 = 仓库 docs/（npm 工程根即 docs/，页面在工程内，vue/mermaid 依赖正常解析）。
// ignoreDeadLinks：docs/ 的方案档案里存在指向仓库根/CHANGELOG 的相对链接，首版放宽，死链治理列后续。
export default withMermaid(
  defineConfig({
    lang: 'zh-CN',
    title: 'Radar 风控引擎',
    description: '开源实时风控决策引擎：Groovy 规则 + 插件生态 + 模型策略包',
    ignoreDeadLinks: true,
    themeConfig: {
      siteTitle: 'Radar 风控引擎',
      outline: { level: [2, 3], label: '本页导航' },
      docFooter: { prev: '上一篇', next: '下一篇' },
      socialLinks: [
        { icon: 'github', link: 'https://github.com/freshday/radar' },
        { icon: 'gitee', link: 'https://gitee.com/freshday/radar' },
      ],
      nav: [
        { text: '首页', link: '/' },
        { text: '快速开始', link: '/operations' },
        { text: '插件生态', link: '/plugin-dev-guide' },
        { text: '模型策略包', link: '/model-package' },
        { text: 'Roadmap', link: '/roadmap' },
      ],
      sidebar: [
        {
          text: '指南',
          items: [
            { text: '文档导航', link: '/index' },
            { text: '架构总览', link: '/architecture' },
            { text: '运维手册', link: '/operations' },
            { text: '压测与监控', link: '/benchmark' },
          ],
        },
        {
          text: '插件生态',
          items: [
            { text: '插件开发指南', link: '/plugin-dev-guide' },
            { text: '插件注册表', link: '/PLUGIN_REGISTRY' },
          ],
        },
        {
          text: '模型策略包',
          items: [{ text: '格式与操作', link: '/model-package' }],
        },
        {
          text: '演进档案',
          items: [
            { text: 'Roadmap', link: '/roadmap' },
            { text: 'v2.1 工程基础', link: '/plan-v2.1' },
            { text: 'v2.2 专业度', link: '/plan-v2.2' },
            { text: 'v2.4 前端现代化', link: '/plan-v2.4' },
            { text: 'v3.0 插件重构', link: '/plan-v3.0' },
            { text: 'v3.1 插件运营', link: '/plan-v3.1' },
            { text: 'v3.2 策略包', link: '/plan-v3.2' },
            { text: 'v3.3 开源基建', link: '/plan-v3.3' },
            { text: 'Java17/Boot3 升级', link: '/upgrade-plan-java17-boot3' },
            { text: '监控栈', link: '/monitoring' },
          ],
        },
      ],
    },
    mermaid: {},
  })
);
