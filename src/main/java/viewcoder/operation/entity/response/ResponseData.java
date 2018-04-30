package viewcoder.operation.entity.response;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/2/2.
 */
public class ResponseData implements Serializable{

    private static final long serialVersionUID = 7526472295622776147L;

    private int status_code;
    private int exception_code;
    private String exception;
    private Object data;
    private int mark; //辅助类型数据

    public ResponseData() {
    }

    public ResponseData(int status_code) {
        this.status_code = status_code;
    }

    public ResponseData(int status_code, int exception_code, String exception, Object data) {
        this.status_code = status_code;
        this.exception_code = exception_code;
        this.exception = exception;
        this.data = data;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getException_code() {
        return exception_code;
    }

    public void setException_code(int exception_code) {
        this.exception_code = exception_code;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "status_code=" + status_code +
                ", exception_code=" + exception_code +
                ", exception='" + exception + '\'' +
                ", data=" + data +
                ", mark=" + mark +
                '}';
    }
}
