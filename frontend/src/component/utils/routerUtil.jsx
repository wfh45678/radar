import React from 'react';
import { useParams } from 'react-router';

// react-router 6/7 移除了 withRouter；class 组件经此 HOC 注入路由参数，
// 组件内部 this.props.params.xxx 的用法与 react-router 3 保持一致
export function withParams(Component) {
  return function (props) {
    const params = useParams();
    return <Component {...props} params={params} />;
  };
}
