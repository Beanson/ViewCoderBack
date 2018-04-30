package viewcoder.tool.xml;

import org.apache.log4j.Logger;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.xml.msg.Msg;
import viewcoder.tool.xml.msg.MsgXml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2018/4/28.
 */
public class XmlUtil {

    private static Map<String, Object> xmlMap = new HashMap<String, Object>();
    private static Logger logger= Logger.getLogger(XmlUtil.class);

    /**
     * 从配置文件中读取xml文件数据
     */
    static {
        InputStream is = null;
        JAXBContext jaxbContext = null;
        Unmarshaller jaxbUnmarshaller = null;
        MsgXml msgXml = null;

        try {
            //遍历所有xml配置文件并装载到xmlMap中
            for (Map.Entry<String , String> entry : CommonObject.getXmlMap().entrySet()) {
                //从文件系统中load相应的xml文件
                is = XmlUtil.class.getResourceAsStream(entry.getValue());
                jaxbContext = JAXBContext.newInstance(MsgXml.class);
                jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                //解析为MsgXml对象，并装载到map中
                msgXml = (MsgXml) jaxbUnmarshaller.unmarshal(is);
                xmlMap.put(entry.getKey(), msgXml);
            }
        } catch (Exception e) {
            XmlUtil.logger.warn("XmlUtil parse xml occurs error", e);

        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                //释放资源
                is = null;
                jaxbContext = null;
                jaxbUnmarshaller = null;
                msgXml = null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 遍历每个message短信，并返回对应类型的message对象
     * @param type
     * @return
     */
    public static String getMsgInfo(String type){
        List<Msg> list = (List<Msg>) xmlMap.get(Common.XML_MSG_TYPE);
        for (Msg msg:
                list) {
            if(msg.getType()!=null && type!=null && msg.getType().equals(type)){
                return msg.getDescription();
            }
        }
        return null;
    }

}
