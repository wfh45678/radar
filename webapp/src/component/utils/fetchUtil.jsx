import {Modal,message} from 'antd';
import 'whatwg-fetch';
// import 'es6-promise/dist/es6-promise.min.js';
import 'fetch-ie8/fetch.js';

export var fetchVersion='/services/v1';

export var FetchUtil=function(url,method,param,callback,done=()=>{}){
	let config={
		credentials: 'include'
	};
	let hide=null;
	console.log(param,'=====')
	if(method!='GET'){
		config.method=method;
		config.headers={"Content-Type": "application/json"};
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
				if(!data.success&&data.code==600){
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