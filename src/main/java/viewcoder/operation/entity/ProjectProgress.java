package viewcoder.operation.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/5/14.
 * 对于PSD和URL项目，需要一定时间等待后台去渲染的，需要设置其进度条
 */
public class ProjectProgress implements Serializable{

    private int userId;
    private String type;
    private String name;
    private String timeStamp;
    private int progress;

    public ProjectProgress() {
    }

    public ProjectProgress(int userId, String type, String name, String timeStamp, int progress) {
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.timeStamp = timeStamp;
        this.progress = progress;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProjectProgress{" +
                "userId=" + userId +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", progress=" + progress +
                '}';
    }
}
