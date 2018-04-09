package FrontEnd.myBatis.entity.response;

/**
 * Created by Administrator on 2018/2/2.
 */
public enum StatusCode {

    OK(200), ERROR(400);

    private int value;

    StatusCode(int value) {
        this.value=value;
    }

    public int getValue(){
        return value;
    }
}
