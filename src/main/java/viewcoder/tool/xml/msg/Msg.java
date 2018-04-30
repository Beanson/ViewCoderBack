package viewcoder.tool.xml.msg;

/**
 * Created by Administrator on 2018/4/28.
 */
public class Msg {

    private String type;
    private String description;

    public Msg() {
    }

    public Msg(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
