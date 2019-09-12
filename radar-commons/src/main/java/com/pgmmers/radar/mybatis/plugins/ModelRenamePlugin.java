package com.pgmmers.radar.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

import java.util.List;

public class ModelRenamePlugin extends PluginAdapter {

    private  String postFix;



    @Override
    public boolean validate(List<String> list) {
        postFix = properties.getProperty("postFix");
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        introspectedTable.setBaseRecordType(introspectedTable.getBaseRecordType() + this.getPostFix());
    }

    public String getPostFix() {
        return postFix;
    }

    public void setPostFix(String postFix) {
        this.postFix = postFix;
    }
}
