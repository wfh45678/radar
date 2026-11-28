import {Modal,message} from 'antd';
// fetch 为浏览器原生 API，IE polyfill（whatwg-fetch/fetch-ie8）已随现代化移除

export var fetchVersion='/services/v1';

export var FetchUtil=function(url,method,param,callback,done=()=>{}){
	// Boot3(Spring6) 取消尾斜杠路径匹配：2022 老代码 CRUD URL（如 /scenario/、/field/）带尾斜杠
	// 会被落到静态资源处理器返回 400（v2.2.0 及之前已存在，v2.4 冒烟发现），此处统一剥离
	url=url.replace(/\/+$/,'');
	let config={
		credentials: 'include',
		headers:{
			"x-auth-token": localStorage.getItem('x-auth-token')
		}
	};
	let hide=null;
	console.log(param,'=====')
	if(method!='GET'){
		config.method=method;
		config.headers['Content-Type']="application/json"
		config.body=param;
		hide = message.loading('正在执行中...', 0);
	}
	return fetch(fetchVersion+url,config)
	        .then((res) => {
	        	if(method!='GET'){hide();}
	        	if(res.ok){ 
		            return res.json();
	            }
	            else{
					if(window.modal==undefined){
						window.modal=Modal.error({
							title: '系统错误',
							content: '请检查是否有参数配置错误',
							onOk:()=>{
									window.modal=undefined;
								}
						});
					}
	            } 
	        })
	        .then((data)=>{
				if(!data.success&&data.code.indexOf('60')!==-1){
					if(window.modal==undefined){
						window.modal=Modal.error({
							title: '您尚未登录',
							content: '请返回登录页面重新登录',
							onOk:()=>{
									window.modal=undefined;
									window.location.href="#/login";
								}
						});
					}
				}
				else if(!data.success){
					if(window.modal==undefined){
						window.modal=Modal.error({
							title: '系统错误',
							content: data.msg,
						});
					}
				}
				else{
					callback(data);
				}
				done();
			})
	        .catch((e) => { 
	            console.log(e.message);
	        });
}