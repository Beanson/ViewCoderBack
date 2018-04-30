package viewcoder.helper.msg.entity;

/**
 * Created by Administrator on 2018/4/28.
 */
public class VerifyCodeMsg {

    private String to;
    private String name;
    private String code;

    public VerifyCodeMsg() {
    }

    public VerifyCodeMsg(String to, String name, String code) {
        this.to = to;
        this.name = name;
        this.code = code;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "VerifyCodeMsg{" +
                "to='" + to + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
