import React from 'react';

export default class CollapseGroup extends React.Component{

	constructor(props){
		super(props);

		this.state={
			activeKey:'',

		}

	}

	getItems=()=>{
		return this.props.children.map((child,index)=>{
			const key=child.key;
			const props={
				slide:child.key==this.state.activeKey?true:false,
				index:index,

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

    render(){
        return (
            <div>
                {this.getItems()}
            </div>
        );
    }

}

