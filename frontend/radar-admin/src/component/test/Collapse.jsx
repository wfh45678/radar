import React from 'react';
import {Switch} from 'antd';

import './Collapse.less';

export default class Collapse extends React.Component{

    constructor(props){
        super(props);

        this.state={
            height:0 
        }

        //this.props.handleClick(this);

    }

    slideDown=()=>{
        if(this.state.height=="auto"){
            return;
        }
        if(this.state.height<this.refs.pChild.offsetHeight){
            this.setState({
                height:this.state.height+15
            },()=>{
                setTimeout(this.slideDown,1);
            })            
        }
        else{
            this.setState({
                height:"auto"
            })
        }       
    }

    slideUp=()=>{
        if(this.state.height=="auto"){
            this.state.height=this.refs.pChild.offsetHeight;
        }
        if(this.state.height>0){
            this.setState({
                height:this.state.height-15
            },()=>{
                setTimeout(this.slideUp,1);
            })           
        }
        else{
            this.setState({
                height:0
            })
        }
    }

    handleClick=()=>{
        this.props.handleClick();
    }

    componentWillReceiveProps(nextProps){
        if(nextProps.slide){
            this.slideDown();
        }
        else{
            this.slideUp();
        }
    }

    handleDragStart=()=>{
        console.log(2);
    }

    switchClick=(e)=>{
        e.stopPropagation();
    }

    render(){
        return (
            <div className="p-block" draggable="true" onDragStart={this.handleDragStart}>
                <div className={'p-block-titles'+(this.props.slide?' p-block-title-select':'')} onClick={this.handleClick}>
                    <div className='p-block-title-left'>{this.props.title}</div>
                    {this.props.switcher!=undefined?
                    <div className='p-block-title-right' onClick={this.switchClick}><Switch /></div>
                    :''}
                </div>
                <div className={'p-block-contents'+(this.props.slide?' p-block-content-select':'')} style={{height:this.state.height}} ref="pContent">
                    <div ref="pChild" className="p-block-main">
                        {this.props.children}
                    </div>
                </div>
            </div>
        );
    }
}