package com.pgmmers.radar.service.engine;

import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.Map;
import java.util.StringJoiner;

public interface PluginServiceV2 {

    default String pluginName() {
        return this.getClass().getSimpleName();
    }

    Integer key();

    String desc();

    String getType();

    default String getMeta() {
        return null;
    }

    Object handle(PreItemVO item, Map<String, Object> jsonInfo, String[] sourceField);

    default String info() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("key='" + key() + "'")
                .add("desc='" + desc() + "'")
                .add("type='" + getType() + "'")
                .add("meta='" + getMeta() + "'")
                .toString();
    }
}
