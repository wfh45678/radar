import React from 'react';
import {Switch,Icon} from 'antd';
import { Link } from 'react-router';

import './Collapse.less';

export default class Collapse extends React.Component{

    constructor(props){
        super(props);

        this.state={
            height:0,

            index:-1,
            pos:-1
        }

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

    handleDragEnd=(e)=>{
        this.setState({
            index:-1,
            pos:-1
        })
        this.props.handleReOrder();
    }

    handleDrag=(e)=>{
        if(!this.props.draggable){return;}
        if(e.pageY==0){return;}
        let pos=Math.floor((e.pageY-300)/48);
        let index=this.props.ruleOrder.indexOf(this.props.ruleId+'');
        if(index==pos){
            return;
        }
        if(index==this.state.index&&pos==this.state.pos){
            return;
        }
        this.state.index=index;
        this.state.pos=pos;
        this.props.handleDrag(this.props.ruleId,pos);
    }

    switchClick=(e)=>{
        e.stopPropagation();
    }

    render(){
        return (
            <div style={this.state.index!=-1?{"visibility":"hidden"}:{}} className="p-block" draggable={this.props.draggable} onDragEnd={this.handleDragEnd} onDrag={this.handleDrag}>
                <div className={'p-block-titles'+(this.props.slide?' p-block-title-select':'')} onClick={this.handleClick}>
                    <div className='p-block-title-left'>{this.props.title}</div>
                    {this.props.switcher!=undefined?
                        <div className='p-block-title-right' onClick={this.switchClick}><Switch checked={this.props.switcher} onChange={this.props.onSwitch}/></div>
                        :''}
                    {
                        this.props.type!=undefined?
                            <div className='p-block-title-right'><Link to={"/historyRecordList/"+this.props.modelId+"/" + this.props.activationId+"/"+this.props.ruleId}><Icon type="calendar" style={{fontSize:'24px',lineHeight:1.5}}/></Link></div>
                            :''
                    }

                </div>
                <div className={'p-block-contents'+(this.props.slide?' p-block-content-select':'')} style={{height:this.state.height}} ref="pContent">
                    <div ref="pChild" className="p-block-main">
                        {this.props?this.props.children:''}
                    </div>
                </div>
            </div>
        );
    }
}