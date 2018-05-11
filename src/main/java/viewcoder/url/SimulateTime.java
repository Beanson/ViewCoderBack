package viewcoder.url;

/**
 * Created by Administrator on 2018/5/10.
 */
public class SimulateTime {

    private long startTotalMillTime;
    private String totalBeginTime;
    private String totalEndTime;
    private double totalTimeLength;

    private long startBrowserMillTime;
    private String browserBeginTime;
    private String browserEndTime;
    private double browserTimeLength;

    private long startDivMillTime;
    private String divBeginTime;
    private String divEndTime;
    private double divTimeLength;

    private long startSpanMillTime;
    private String spanBeginTime;
    private String spanEndTime;
    private double spanTimeLength;

    private long startImgMillTime;
    private String imgBeginTime;
    private String imgEndTime;
    private double imgTimeLength;

    public SimulateTime() {
    }

    public String getTotalBeginTime() {
        return totalBeginTime;
    }

    public void setTotalBeginTime(String totalBeginTime) {
        this.totalBeginTime = totalBeginTime;
        this.startTotalMillTime = System.currentTimeMillis();
    }

    public String getTotalEndTime() {
        return totalEndTime;
    }

    public void setTotalEndTime(String totalEndTime) {
        this.totalEndTime = totalEndTime;
        this.totalTimeLength = (System.currentTimeMillis() - this.startTotalMillTime) / 1000;
    }

    public double getTotalTimeLength() {
        return totalTimeLength;
    }


    public String getBrowserBeginTime() {
        return browserBeginTime;
    }

    public void setBrowserBeginTime(String browserBeginTime) {
        this.browserBeginTime = browserBeginTime;
        this.startBrowserMillTime = System.currentTimeMillis();
    }

    public String getBrowserEndTime() {
        return browserEndTime;
    }

    public void setBrowserEndTime(String browserEndTime) {
        this.browserEndTime = browserEndTime;
        this.browserTimeLength = (System.currentTimeMillis()-this.startBrowserMillTime)/1000;
    }

    public double getBrowserTimeLength() {
        return browserTimeLength;
    }

    public String getDivBeginTime() {
        return divBeginTime;
    }

    public void setDivBeginTime(String divBeginTime) {
        this.divBeginTime = divBeginTime;
        this.startDivMillTime = System.currentTimeMillis();
    }

    public String getDivEndTime() {
        return divEndTime;
    }

    public void setDivEndTime(String divEndTime) {
        this.divEndTime = divEndTime;
        this.divTimeLength = (System.currentTimeMillis() - this.startDivMillTime) / 1000;
    }

    public double getDivTimeLength() {
        return divTimeLength;
    }

    public String getSpanBeginTime() {
        return spanBeginTime;
    }

    public void setSpanBeginTime(String spanBeginTime) {
        this.spanBeginTime = spanBeginTime;
        this.startSpanMillTime = System.currentTimeMillis();
    }

    public String getSpanEndTime() {
        return spanEndTime;
    }

    public void setSpanEndTime(String spanEndTime) {
        this.spanEndTime = spanEndTime;
        this.spanTimeLength = (System.currentTimeMillis() - this.startSpanMillTime) / 1000;
    }

    public double getSpanTimeLength() {
        return spanTimeLength;
    }

    public String getImgBeginTime() {
        return imgBeginTime;
    }

    public void setImgBeginTime(String imgBeginTime) {
        this.imgBeginTime = imgBeginTime;
        this.startImgMillTime = System.currentTimeMillis();
    }

    public String getImgEndTime() {
        return imgEndTime;
    }

    public void setImgEndTime(String imgEndTime) {
        this.imgEndTime = imgEndTime;
        this.imgTimeLength = (System.currentTimeMillis() - this.startImgMillTime) / 1000;
    }

    public double getImgTimeLength() {
        return imgTimeLength;
    }


    @Override
    public String toString() {
        return "SimulateTime{" +
                "totalBeginTime='" + totalBeginTime + '\'' +
                ", totalEndTime='" + totalEndTime + '\'' +
                ", totalTimeLength='" + totalTimeLength + '\'' +
                ", divBeginTime='" + divBeginTime + '\'' +
                ", divEndTime='" + divEndTime + '\'' +
                ", divTimeLength='" + divTimeLength + '\'' +
                ", spanBeginTime='" + spanBeginTime + '\'' +
                ", spanEndTime='" + spanEndTime + '\'' +
                ", spanTimeLength='" + spanTimeLength + '\'' +
                ", imgBeginTime='" + imgBeginTime + '\'' +
                ", imgEndTime='" + imgEndTime + '\'' +
                ", imgTimeLength='" + imgTimeLength + '\'' +
                '}';
    }
}
