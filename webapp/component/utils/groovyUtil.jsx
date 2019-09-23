function getIndent(level) {
    var result = "";
    for (var i = 0; i < level; i++) {
        result = result + "    ";
    }
    return result;
}

function getExpression(expreObject) {
    if(!expreObject){
        return null;
    }
    var className = expreObject["class"];
    var type = expreObject["type"];
    var column = expreObject["column"];
    var value = expreObject["value"];
    //console.log(expreObject);
    //console.log(value);
    if (className == "ENTATTR") // 选择的数据类型
        return "data." + column;
    else if (className == "CONST") {
        if (type == "STRING") {
            return "'" + value + "'";
        } else if (type == "DOUBLE") {
            return "" + value;
        } else {
            return "" + value;
        }
    }
}

function processRule(ruleObject, level) {
    var className = ruleObject["class"];
    var enabled = ruleObject["enabled"];
    var operator = ruleObject["operator"];
    var expressions = ruleObject["expressions"];
    if (className == "PDCT") {
        return processRules(ruleObject, level + 1);
    }
    // if (operator=="Equal") {
    //     return getExpression(expressions[0]) + "==" + getExpression(expressions[1]);
    // } else if (operator=="InList") {
    //     return getExpression(expressions[0]) + " in blackList["+getExpression(expressions[1])+"]";
    // } else if (operator=="IsNull") {
    //     return getExpression(expressions[0]) + " is null";
    // } else if (operator=="Field_Equal") {
    //     return getExpression(expressions[0]) + "==" + getExpression(expressions[1]);
    // } else if (operator=="StartsWith") {
    //     return getExpression(expressions[0]) + ".startsWith("+getExpression(expressions[1])+")";
    // }
    //console.log(ruleObject);
    switch (operator) {
        case "StartsWith":
            return getExpression(expressions[0]) + ".startsWith(" + getExpression(expressions[1]) + ")";
        case "NotStartsWith":
            return "!" + getExpression(expressions[0]) + ".startsWith(" + getExpression(expressions[1]) + ")";
        case "Contains":
            return getExpression(expressions[0]) + ".contains(" + getExpression(expressions[1]) + ")";
        case "NotContains":
            return "!" + getExpression(expressions[0]) + ".contains(" + getExpression(expressions[1]) + ")";
        case "Equal":
        case "Field_Equal":
            return getExpression(expressions[0]) + "==" + getExpression(expressions[1]);
        case "NotEqual":
        case "Field_Not_Equal":
            return getExpression(expressions[0]) + "!=" + getExpression(expressions[1]);
        case "Less":
        case "Field_Less":
            return getExpression(expressions[0]) + "<" + getExpression(expressions[1]);
        case "Less_Equal":
        case "Field_Less_Equal":
            return getExpression(expressions[0]) + "<=" + getExpression(expressions[1]);
        case "Greater":
        case "Field_Greater":
            return getExpression(expressions[0]) + ">" + getExpression(expressions[1]);
        case "Greater_Equal":
        case "Field_Greater_Equal":
            return getExpression(expressions[0]) + ">=" + getExpression(expressions[1]);
        case "InList":
            return "lists." + getExpression(expressions[1]) + ".containsKey(" + getExpression(expressions[0]) + ")";
        case "NotInList":
            return "!lists." + getExpression(expressions[1]) + ".containsKey(" + getExpression(expressions[0]) + ")";
        case "IsNull":
            return "!" + getExpression(expressions[0]);
        case "IsNotNull":
            return getExpression(expressions[0]);
    }
}

function processRules(jsonObject, level) {
    var className = jsonObject["class"];
    var enabled = jsonObject["enabled"];
    var linking = jsonObject["linking"];
    var conditions = jsonObject["conditions"];
    var length = conditions.length;
    var result="";
    if (linking == "NotAll" || linking == "None") {
        result += "!";
    }
    result += "(";
    for (var i in conditions) {
        result = result + processRule(conditions[i], level);
        if (i != length - 1) {
            // if (linking=="All") {
            //     result = result + "\n"+getIndent(level)+"and ";
            // } else if (linking=="Any") {
            //     result = result + "\n"+getIndent(level)+"or ";
            // }
            switch (linking) {
                case "All":
                case "NotAll":
                    result += "&&";
                    break;
                case "Any":
                case "None":
                    result += "||";
                    break;
            }
        }
    }
    return result + ")";
}

export var generateScript = function(jsonObject, className) {
        if(jsonObject==null){
            return '';
        }
        var script = "class " + className + "CheckScript {" + "\n";
        script += "  public boolean check(def data, def lists) {";
        script += "    if (" + processRules(jsonObject, 1) + ")\n";
        script += "        return true;" + "\n";
        script += "    else" + "\n";
        script += "        return false;" + "\n";
        script += "}";
        script += "}";
        return script;
}

export let validateRules = function(jsonObject) {
    if(jsonObject==null){
        return true;
    }

    let conditions = jsonObject["conditions"];
    for (let i in conditions) {
        if(!validateRule(conditions[i])){
            return false;
        }
    }
    return true;
}

function validateRule(ruleObject){
    var className = ruleObject["class"];
    var operator = ruleObject["operator"];
    var expressions = ruleObject["expressions"];
    if (className == "PDCT") {
        return validateRules(ruleObject);
    }

    if(!operator){
        return false;
    }

    switch (operator) {
        case "StartsWith":
        case "NotStartsWith":
        case "Contains":
        case "NotContains":
        case "Equal":
        case "Field_Equal":
        case "NotEqual":
        case "Field_Not_Equal":
        case "Less":
        case "Field_Less":
        case "Less_Equal":
        case "Field_Less_Equal":
        case "Greater":
        case "Field_Greater":
        case "Greater_Equal":
        case "Field_Greater_Equal":
        case "InList":
        case "NotInList":
            return getExpression(expressions[1])&&getExpression(expressions[0]);
        case "IsNull":
        case "IsNotNull":
            return getExpression(expressions[0])&&true;
    }

    return true;
}