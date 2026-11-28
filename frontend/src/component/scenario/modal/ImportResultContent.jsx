import React from 'react';

/**
 * 导入/模板创建结果展示（v3.4 抽公共）：新场景 id + 告警列表。
 * 供 ImportScenario 与 TemplateLibrary 两弹窗共用。
 */
export default function ImportResultContent({ id, warnings }) {
  return (
    <div>
      <p>新场景 id：{id}，请构建场景后生效。</p>
      {warnings && warnings.length > 0 ? (
        <>
          <p style={{ color: '#fa8c16' }}>存在 {warnings.length} 条告警：</p>
          <ul style={{ textAlign: 'left', paddingLeft: 20 }}>
            {warnings.map((w, i) => (
              <li key={i}>{w}</li>
            ))}
          </ul>
        </>
      ) : null}
    </div>
  );
}
