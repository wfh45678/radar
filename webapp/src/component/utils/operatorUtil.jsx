export var Operator = {
    StartsWith:{label:'以...开始',value:'StartsWith',nextType:'input'},
    Contains:{label:'包含',value:'Contains',nextType:'input'},
    Equal : {label:'等于',value:'Equal',nextType:'input'},
    Less : {label:'小于',value:'Less',nextType:'input'},
    Less_Equal : {label:'小于等于',value:'Less_Equal',nextType:'input'},
    Greater : {label:'大于',value:'Greater',nextType:'input'},
    Greater_Equal : {label:'大于等于',value:'Greater_Equal',nextType:'input'},
    InList : {label:'在列表...中',value:'InList',nextType:'list'},
    NotStartsWith : {label:'不以...开始',value:'NotStartsWith',nextType:'input'},
    NotContains : {label:'不包含',value:'NotContains',nextType:'input'},
    NotEqual : {label:'不等于',value:'NotEqual',nextType:'input'},
    NotInList : {label:'不在列表...中',value:'NotInList',nextType:'list'},
    IsNull : {label:'为空',value:'IsNull',nextType:'empty'},
    IsNotNull : {label:'不为空',value:'IsNotNull',nextType:'empty'},
    Field_Greater : {label:'大于（字段）',value:'Field_Greater',nextType:'field'},
    Field_Less : {label:'小于（字段）',value:'Field_Less',nextType:'field'},
    Field_Greater_Equal : {label:'大于等于（字段）',value:'Field_Greater_Equal',nextType:'field'},
    Field_Less_Equal : {label:'小于等于（字段）',value:'Field_Less_Equal',nextType:'field'},
    Field_Equal : {label:'等于（字段）',value:'Field_Equal',nextType:'field'},
    Field_Not_Equal : {label:'不等于（字段）',value:'Field_Not_Equal',nextType:'field'}
};

/*nextType:{
	input,list,empty,field	
}
*/

export var operatorMap={
	'STRING':[
		Operator.StartsWith,
		Operator.NotStartsWith,
		Operator.Contains,
		Operator.NotContains,
		Operator.Equal,
		Operator.NotEqual,
		Operator.InList,		
		Operator.NotInList,
		Operator.IsNull,
		Operator.IsNotNull,
		Operator.Field_Equal,
		Operator.Field_Not_Equal
	],
	'INTEGER':[
		Operator.Equal,
		Operator.NotEqual,
		Operator.InList,		
		Operator.NotInList,
		Operator.Less,
		Operator.Less_Equal,
		Operator.Greater,
		Operator.Greater_Equal,
		Operator.IsNull,
		Operator.IsNotNull,
		Operator.Field_Greater,
		Operator.Field_Less,
		Operator.Field_Greater_Equal,
		Operator.Field_Less_Equal,
		Operator.Field_Equal,
		Operator.Field_Not_Equal
	],
	'DOUBLE':[
		Operator.Equal,
		Operator.NotEqual,
		Operator.InList,		
		Operator.NotInList,
		Operator.Less,
		Operator.Less_Equal,
		Operator.Greater,
		Operator.Greater_Equal,
		Operator.IsNull,
		Operator.IsNotNull,
		Operator.Field_Greater,
		Operator.Field_Less,
		Operator.Field_Greater_Equal,
		Operator.Field_Less_Equal,
		Operator.Field_Equal,
		Operator.Field_Not_Equal
	],
	'LONG':[
		Operator.Equal,
		Operator.NotEqual,
		Operator.InList,		
		Operator.NotInList,
		Operator.Less,
		Operator.Less_Equal,
		Operator.Greater,
		Operator.Greater_Equal,
		Operator.IsNull,
		Operator.IsNotNull,
		Operator.Field_Greater,
		Operator.Field_Less,
		Operator.Field_Greater_Equal,
		Operator.Field_Less_Equal,
		Operator.Field_Equal,
		Operator.Field_Not_Equal
	],
	'':[]
};
