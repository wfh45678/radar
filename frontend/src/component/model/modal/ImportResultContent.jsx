import React from 'react';

/**
 * 导入/模板创建结果展示（v3.4 抽公共）：新模型 id + 告警列表。
 * 供 ImportModel 与 TemplateLibrary 两弹窗共用。
 */
export default function ImportResultContent({ id, warnings }) {
  return (
    <div>
      <p>新模型 id：{id}，请构建模型后生效。</p>
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
