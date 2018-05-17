package viewcoder.psd.entity;

import java.util.HashMap;

/**
 * 用于返回新生成 "空项目"和"PSD项目" 时组件overall信息
 */
public class InitTools {

    private HashMap<String,HashMap<String,Object>> all_tools=new HashMap<>();

    public InitTools() {
        initData();
    }

    /**
     * 对外公开方法获取实例的组件信息
     */
    public HashMap<String,HashMap<String,Object>> getInitTool(){
        return all_tools;
    }

    private void initData(){
        //数据流组件
        all_tools.put("Table",new HashMap<String,Object>(0));
        all_tools.put("List",new HashMap<String,Object>(0));
        //通用组件
        all_tools.put("Common_Image",new HashMap<String,Object>());
        all_tools.put("Common_Text",new HashMap<String,Object>());
        all_tools.put("Common_Background",new HashMap<String,Object>());
        all_tools.put("Common_Button",new HashMap<String,Object>(0));
        //表单组件
        all_tools.put("TextInput",new HashMap<String,Object>(0));
        all_tools.put("Password",new HashMap<String,Object>(0));
        all_tools.put("TextArea",new HashMap<String,Object>(0));
        all_tools.put("CheckBox",new HashMap<String,Object>(0));
        all_tools.put("RadioBox",new HashMap<String,Object>(0));
        all_tools.put("SelectOptions",new HashMap<String,Object>(0));
        all_tools.put("DatePicker",new HashMap<String,Object>(0));
        all_tools.put("File",new HashMap<String,Object>(0));
        all_tools.put("Submit",new HashMap<String,Object>(0));
        //多媒体组件
        all_tools.put("Video",new HashMap<String,Object>(0));
        all_tools.put("Sound",new HashMap<String,Object>(0));
        all_tools.put("Carousel",new HashMap<String,Object>(0));
        //其他组件
        all_tools.put("DownLoad",new HashMap<String,Object>(0));
        all_tools.put("List_Navigation",new HashMap<String,Object>(0));
        all_tools.put("CusCode",new HashMap<String,Object>(0));
    }

}
