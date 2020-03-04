package org.comroid.test.model;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType
public final class NGinXFSNode {
    private final String name;
    private final String type;
    private final String mtime;

    @JSONCreator
    public NGinXFSNode(
            @JSONField(name = "name") String name,
            @JSONField(name = "type") String type,
            @JSONField(name = "mtime") String mtime
    ) {
        this.name = name;
        this.type = type;
        this.mtime = mtime;
    }
  
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getMtime() {
        return mtime;
    }
}
