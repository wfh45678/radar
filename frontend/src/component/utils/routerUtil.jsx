import React from 'react';
import { useParams, useLocation, useOutletContext } from 'react-router';

// react-router 6/7 移除了 withRouter；为 class 组件注入路由能力，
// 对齐 react-router 3 时代的 this.props.params / this.props.location 用法
export function withParams(Component) {
  return function (props) {
    const params = useParams();
    return <Component {...props} params={params} />;
  };
}

export function withLocation(Component) {
  return function (props) {
    const location = useLocation();
    return <Component {...props} location={location} />;
  };
}

// 原 rr3 父组件经 React.cloneElement 注入子页的 props，
// 在 router 6/7 下走 Outlet context（配合布局组件的 <Outlet context={...}/>）
export function withOutletCtx(Component) {
  return function (props) {
    const ctx = useOutletContext();
    return <Component {...props} {...ctx} />;
  };
}
