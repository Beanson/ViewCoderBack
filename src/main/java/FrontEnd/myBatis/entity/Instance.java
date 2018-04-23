package FrontEnd.myBatis.entity;

/**
 * Created by Administrator on 2018/4/23.
 * 我的实例的实体类
 */
public class Instance {

    private int id;
    private int user_id;
    private int service_id;
    private int space;  //单位是KByte
    private String from_date;
    private String end_date;

    public Instance() {
    }

    public Instance(int id, int user_id, int service_id, int space, String from_date, String end_date) {
        this.id = id;
        this.user_id = user_id;
        this.service_id = service_id;
        this.space = space;
        this.from_date = from_date;
        this.end_date = end_date;
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

    public int getService_id() {
        return service_id;
    }

    public void setService_id(int service_id) {
        this.service_id = service_id;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public String getFrom_date() {
        return from_date;
    }

    public void setFrom_date(String from_date) {
        this.from_date = from_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", service_id=" + service_id +
                ", space=" + space +
                ", from_date='" + from_date + '\'' +
                ", end_date='" + end_date + '\'' +
                '}';
    }
}
