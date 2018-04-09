package FrontEnd.psd.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Administrator on 2017/6/26.
 */
public class PsdInfo {

    private int id;
    private HashMap<String,Object> overall=new HashMap<>();
    private HashMap<String,HashMap<String,Object>> all_tools=new HashMap<>();

    public PsdInfo() {
        initTools();
    }

    public PsdInfo(int id, HashMap<String,Object> overall) {
        this.id=id;
        this.overall = overall;
        initTools();
    }

    public PsdInfo(int id, HashMap<String, Object> overall, HashMap<String, HashMap<String, Object>> all_tools) {
        this.id = id;
        this.overall = overall;
        this.all_tools = all_tools;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<String, Object> getOverall() {
        return overall;
    }

    public void setOverall(HashMap<String, Object> overall) {
        this.overall = overall;
    }

    public HashMap<String, HashMap<String,Object>> getAll_tools() {
        return all_tools;
    }

    public void setAll_tools(HashMap<String, HashMap<String,Object>> all_tools) {
        this.all_tools = all_tools;
    }

    /**
     * 初始化all_tools为空信息
     */
    public void initTools(){
        all_tools=new InitTools().getInitTool();
    }

    @Override
    public String toString() {
        return "PsdInfo{" +
                "id=" + id +
                ", overall=" + overall +
                ", all_tools='" + all_tools + '\'' +
                '}';
    }
}

