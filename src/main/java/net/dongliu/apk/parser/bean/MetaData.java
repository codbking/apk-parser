package net.dongliu.apk.parser.bean;

/**
 * @author wulang
 * @version 1.0.0
 * @ClassName MetaData.java
 * @Description TODO
 * @createTime 2019年04月24日 17:24:00
 */
public class MetaData {
    private String name;
    private String value;
    private String resource;

    public MetaData(String name, String value, String resource) {
        this.name = name;
        this.value = value;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
