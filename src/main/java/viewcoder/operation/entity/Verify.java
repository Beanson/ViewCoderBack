package viewcoder.operation.entity;

/**
 * Created by Administrator on 2018/6/28.
 * 支付校验的实体类
 */
public class Verify {

    private String sign;
    private String content;

    public Verify() {
    }

    public Verify(String sign, String content) {
        this.sign = sign;
        this.content = content;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Verify{" +
                "sign='" + sign + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
