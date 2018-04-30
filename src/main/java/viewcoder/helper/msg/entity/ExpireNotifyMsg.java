package viewcoder.helper.msg.entity;

/**
 * Created by Administrator on 2018/4/28.
 */
public class ExpireNotifyMsg {

    private String to;
    private String name;
    private String renewUrl;

    public ExpireNotifyMsg() {
    }

    public ExpireNotifyMsg(String to, String name, String renewUrl) {
        this.to = to;
        this.name = name;
        this.renewUrl = renewUrl;
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

    public String getRenewUrl() {
        return renewUrl;
    }

    public void setRenewUrl(String renewUrl) {
        this.renewUrl = renewUrl;
    }

    @Override
    public String toString() {
        return "ExpireNotifyMsg{" +
                "to='" + to + '\'' +
                ", name='" + name + '\'' +
                ", renewUrl='" + renewUrl + '\'' +
                '}';
    }
}
