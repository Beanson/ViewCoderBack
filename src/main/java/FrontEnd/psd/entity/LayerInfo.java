package FrontEnd.psd.entity;

import FrontEnd.psd.layer.PsdLayer;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/6/27.
 */
public class LayerInfo {

    /*图层基本信息*/
    public static void InitLayerBaseInfo(HashMap<String,Object> layer_info,PsdLayer layer,int num){
        /*基本设置*/
        layer_info.put("layer_rate",num);
        layer_info.put("layer_id",layer.getLayerId());
        layer_info.put("top",layer.getTop());
        layer_info.put("left",layer.getLeft());
        layer_info.put("width",layer.getWidth());
        layer_info.put("height",layer.getHeight());
        layer_info.put("show",setVisible(layer.isVisible(),layer.getParent()));
        layer_info.put("opacity",(float) (layer.getOpacity()+256)/255);

    }

    /*文字图层信息*/
    public static void InitLayerTextInfo(HashMap<String,Object> layer_info,PsdLayer layer,String [] fontData){

        layer_info.put("type","Common_Text");
        layer_info.put("name","text_" + layer.getLayerId());
        layer_info.put("text-editable",false); //为true时可以通过对文字内容进行编辑处理

        layer_info.put("text",layer.getFontText());
        layer_info.put("temp_text",layer.getFontText());
        layer_info.put("font-size",(int) (Float.parseFloat(fontData[4])));
        layer_info.put("font-weight",400);
        layer_info.put("line-height",150);
        layer_info.put("text-align","left");
        layer_info.put("font-family","Microsoft YaHei");
        layer_info.put("font-style","normal");
        layer_info.put("text-decoration","none");
        layer_info.put("font-color","rgba(" + (int) (Float.parseFloat(fontData[1]) * 255) + "," + (int) (Float.parseFloat(fontData[2]) * 255) + "," + (int) (Float.parseFloat(fontData[3]) * 255) + "," +  (Float.parseFloat(fontData[0]) * 255/255)+")");

    }

    /*背景图层信息*/
    public static void InitLayerBackGroundInfo(HashMap<String,Object> layer_info,PsdLayer layer,String bg_color){
        layer_info.put("type","Common_Background");
        layer_info.put("name","bg_" + layer.getLayerId());
        layer_info.put("bg-color",bg_color);

        /*border边框设置*/
        layer_info.put("border-top-width",0);
        layer_info.put("border-right-width",0);
        layer_info.put("border-bottom-width",0);
        layer_info.put("border-left-width",0);
        layer_info.put("border-color","rgba(100,100,100,0.4)");

        /*圆角设置*/
        layer_info.put("border-top-left-radius",0);
        layer_info.put("border-top-right-radius",0);
        layer_info.put("border-bottom-left-radius",0);
        layer_info.put("border-bottom-right-radius",0);
    }

    /*图像图层信息*/
    public static void InitLayerImageInfo(HashMap<String,Object> layer_info,String name,String src){
        layer_info.put("type","Common_Image");
        layer_info.put("name",name);
        layer_info.put("image_reposition",false); //为true时可以通过鼠标移动图片到在框内不同位置，滚轮可以扩大缩小图片等

        /*图片设置*/
        layer_info.put("src",src);
        layer_info.put("bg-position-left",0);
        layer_info.put("bg-position-top",0);
        layer_info.put("bg-repeat","no-repeat");
        layer_info.put("bg-size",101);
        layer_info.put("bg-color","rgba(238,155,94,0)");

        /*border边框设置*/
        layer_info.put("border-top-width",0);
        layer_info.put("border-right-width",0);
        layer_info.put("border-bottom-width",0);
        layer_info.put("border-left-width",0);
        layer_info.put("border-color","rgba(100,100,100,0.4)");

        /*圆角设置*/
        layer_info.put("border-top-left-radius",0);
        layer_info.put("border-top-right-radius",0);
        layer_info.put("border-bottom-left-radius",0);
        layer_info.put("border-bottom-right-radius",0);
    }


    /**
     *  迭代查看父容layer中visible变量，出现false，则子layer也为false
     * @param visible 子layer本身的
     * @param parent_layer 父组件
     * @return Boolean类型，返回true或false
     */
    public static Boolean setVisible(boolean visible,PsdLayer parent_layer)
    {
        PsdLayer temp=parent_layer;
        //如果有父组件，则父组件有一个不可见则返回false
        while(temp!=null){
            if(!temp.isVisible()){
                return false;
            }
            temp=temp.getParent();
        }
        //否则根据本组件自身visible的true或false
        return visible;
    }
}
