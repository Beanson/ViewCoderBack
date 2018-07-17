package viewcoder.operation.entity;

/**
 * Created by Administrator on 2018/7/17.
 * 用户建议、反馈信息的实体类
 */
public class Feedback {

    private int id;
    private int user_id;
    private String subject; //摘要信息：文本的255个字符
    private String message;
    private String phone;
    private String email;
    private int status;
    private String text; //反馈的文本信息

    public Feedback() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", text='" + text + '\'' +
                '}';
    }
}
