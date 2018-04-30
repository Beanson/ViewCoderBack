package viewcoder.psd.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/5/6.
 */
public class PSDParseDev {

    /**
     * 解析文字的设置样式
     *
     * @param data             获取的文字样式流字符串
     * @param colorRGBAAndSize 用于装载文字样式信息的string数组 [0]-[3]装载argb,，[4]装载字体大小，[5]装载文字内容
     */
    public void ParseFont(String data, String[] colorRGBAAndSize) {
        String regex = "RunArray\\=\\[\\{StyleSheet[\\s\\S]*\\}\\}\\}\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(data);
        while (m.find()) {

            //获取字符串的rgba颜色设置
            String regex2 = "([\\d\\.]*,\\s*){3}[\\d\\.]*";
            Pattern p2 = Pattern.compile(regex2);
            String filterStr = m.group();
            Matcher m2 = p2.matcher(filterStr);
            while (m2.find()) {
                String[] temp = m2.group().replace(" ", "").split(",");
                colorRGBAAndSize[0] = temp[0]; //a
                colorRGBAAndSize[1] = temp[1]; //r
                colorRGBAAndSize[2] = temp[2]; //g
                colorRGBAAndSize[3] = temp[3]; //b
                break;
            }

            //获取字体大小
            String regex3 = "FontSize=[\\d\\.]*";
            Pattern p3 = Pattern.compile(regex3);
            Matcher m3 = p3.matcher(filterStr);
            while (m3.find()) {
                colorRGBAAndSize[4] = m3.group().substring(9);
                //System.out.println("the font size is:"+colorRGBAAndSize[4]);
                break;
            }

            //设置文字内容,不需要设置，已经在外面可以获取了
            //colorRGBAAndSize[5]=filterStr.substring(filterStr.indexOf("Text=")+5);
            break;
        }
    }

}
