package org.comroid.common.upd8r.nginx.model;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;

public class JsonFileInfo {
    private final String fileName;
    private final String myTime;
    private final int    size;

    @JSONCreator
    public JsonFileInfo(
            @JSONField(name = "name") String fileName,
            @JSONField(name = "mtime", serialize = false) String myTime,
            @JSONField(name = "size") int size
    ) {
        this.fileName = fileName;
        this.myTime = myTime;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMyTime() {
        return myTime;
    }

    public int getSize() {
        return size;
    }
}
