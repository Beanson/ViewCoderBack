package viewcoder.tool.parser.form;

/**
 * Created by Administrator on 2017/6/16.
 */
public class DataParser {

    public static Object dataParse(String type,String data){

        Object resultData=new Object();
        switch (type){
            case "java.lang.String":resultData=data;break;
            case "int":resultData=Integer.parseInt(data);break;
            case "byte":resultData=Byte.parseByte(data);break;
            case "short":resultData=Short.parseShort(data);break;
            case "long":resultData=Long.parseLong(data);break;
            case "char":resultData=data.toCharArray()[0];break;
            case "float":resultData=Float.parseFloat(data);break;
            case "double":resultData=Double.parseDouble(data);break;
            case "boolean":resultData=Boolean.parseBoolean(data);break;
        }
        return resultData;
    }
}


