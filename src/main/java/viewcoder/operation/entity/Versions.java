package viewcoder.operation.entity;

/**
 * Created by Administrator on 2018/5/27.
 */
public class Versions {

    private String v; //v: version的意思，表示在oss文件系统中版本编号
    private int t;    //t: type的意思，0表示电脑版，1表示手机版
    private String n; //n: name的意思

    public Versions() {
    }

    public Versions(String v, int t, String n) {
        this.v = v;
        this.t = t;
        this.n = n;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }


    @Override
    public String toString() {
        return "Versions{" +
                "v='" + v + '\'' +
                ", t=" + t +
                ", n='" + n + '\'' +
                '}';
    }
}
