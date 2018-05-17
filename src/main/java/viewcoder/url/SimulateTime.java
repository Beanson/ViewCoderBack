package viewcoder.url;

/**
 * Created by Administrator on 2018/5/10.
 */
public class SimulateTime {

    //记录总时间
    private long startTotalMillTime;
    private String totalBeginTime;
    private String totalEndTime;
    private double totalTimeLength;

    //记录启动浏览器时间
    private long startBrowserMillTime;
    private String browserBeginTime;
    private String browserEndTime;
    private double browserTimeLength;

    //记录打开指定网站url的时间
    private long startGetUrlMillTime;
    private String getUrlBeginTime;
    private String getUrlEndTime;
    private double getUrlTimeLength;
    private int getUrlTimes = 0; //获取连接指定网站url的次数

    //记录渲染div组件时间
    @Deprecated
    private long startDivMillTime;
    @Deprecated
    private String divBeginTime;
    @Deprecated
    private String divEndTime;
    @Deprecated
    private double divTimeLength;


    //记录渲染span组件时间
    @Deprecated
    private long startSpanMillTime;
    @Deprecated
    private String spanBeginTime;
    @Deprecated
    private String spanEndTime;
    @Deprecated
    private double spanTimeLength;

    //记录渲染img组件时间
    @Deprecated
    private long startImgMillTime;
    @Deprecated
    private String imgBeginTime;
    @Deprecated
    private String imgEndTime;
    @Deprecated
    private double imgTimeLength;

    public SimulateTime() {
    }

    //****************************************************************
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

    //****************************************************************
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
        this.browserTimeLength = (System.currentTimeMillis() - this.startBrowserMillTime) / 1000;
    }

    public double getBrowserTimeLength() {
        return browserTimeLength;
    }

    //****************************************************************
    public String getGetUrlBeginTime() {
        return getUrlBeginTime;
    }

    public void setGetUrlBeginTime(String getUrlBeginTime) {
        this.getUrlBeginTime = getUrlBeginTime;
        this.startGetUrlMillTime = System.currentTimeMillis();
    }

    public String getGetUrlEndTime() {
        return getUrlEndTime;
    }

    public void setGetUrlEndTime(String getUrlEndTime) {
        this.getUrlEndTime = getUrlEndTime;
        this.getUrlTimeLength = (System.currentTimeMillis() - this.startGetUrlMillTime) / 1000;
    }

    public double getGetUrlTimeLength() {
        return getUrlTimeLength;
    }

    public int getGetUrlTimes() {
        return getUrlTimes;
    }

    public void setGetUrlTimes(int getUrlTimes) {
        this.getUrlTimes = getUrlTimes;
    }

    //****************************************************************
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

    //****************************************************************
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

    //****************************************************************
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
