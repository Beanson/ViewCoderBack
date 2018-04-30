package viewcoder.tool.xml.msg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by 44077339 on 2018/4/28.
 */
@XmlRootElement(name = "MsgXml")
@XmlAccessorType(XmlAccessType.FIELD)
public class MsgXml {

    @XmlElement(name="msg")
    private List<Msg> msg;

    public List<Msg> getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "MsgXml{" +
                "msg=" + msg +
                '}';
    }
}