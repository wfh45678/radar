import React from 'react';

export default class CollapseGroup extends React.Component{

	constructor(props){
		super(props);

		this.state={
			activeKey:'',


		}
	}

	getItems=()=>{
		if(!this.props){
			return '';
		}

		return this.props.children.map((child,index)=>{
			const key=child.key;
			const props={
				slide:child.key==this.state.activeKey?true:false,
				index:index,
				draggable:this.props.draggable&&this.state.activeKey=='',

				handleDrag:(ruleId,pos)=>{
					this.props.handleDrag(ruleId,pos);
				},
				handleReOrder:()=>{
					this.props.handleReOrder();
				},
				handleClick:()=>{
					if(this.state.activeKey==key){
						this.setState({
							activeKey:''
						})
					}
					else{
						this.setState({
							activeKey:key
						});
					}
				}
			}
			return React.cloneElement(child,props);
		})
	}

	allowDrop=(e)=>{
		e.preventDefault();
	}

    render(){
        return (
            <div style={{position:"relative"}} onDragOver={(this.props.draggable&&this.state.activeKey=='')?this.allowDrop:null}>
                {this.getItems()}
            </div>
        );
    }

}

