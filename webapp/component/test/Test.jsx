import React from 'react';
import {Icon,Popover} from 'antd';

import CollapseGroup from './CollapseGroup';
import Collapse from './Collapse';

import './Test.less';

import {FetchUtil} from '../utils/fetchUtil';

export default class Test extends React.Component{
	constructor(props){
		super(props);

		this.state={
			slide:false,
			height:0 
		}

	}

	slideDown=()=>{
		if(this.state.height<this.refs.pChild.offsetHeight){
			this.setState({
				height:this.state.height+=5
			})
			setTimeout(this.slideDown,10);
		}
		else{
			this.setState({
				height:"auto",
				slide:true
			})
		}		
	}

	slideUp=()=>{
		if(this.state.height>0){
			this.setState({
				height:this.state.height-=5
			})
			setTimeout(this.slideUp,10);
		}
		else{
			this.setState({
				height:0,
				slide:false
			})
		}
	}

	handleClick=()=>{
		// if(this.state.slide==false){
		// 	this.slideDown();
		// }
		// else{
		// 	let height=this.refs.pContent.clientHeight;
		// 	this.setState({
		// 		height:height
		// 	},this.slideUp)
		// }

		// this.setState({
		// 	slide:!this.state.slide
		// })
	}

	render(){
		var content=<span>hello world</span>;
		return (
			<div className="ant-layout-content">
				<CollapseGroup>
					<Collapse key="a" title="a" ><div>ad<br/>asfdasdflasdf</div></Collapse>
					<Collapse key="b" title="b"><div>bd</div></Collapse>
					<Collapse key="c" title="c"><div>cd</div></Collapse>
				</CollapseGroup>
			</div>
			);
	}
}