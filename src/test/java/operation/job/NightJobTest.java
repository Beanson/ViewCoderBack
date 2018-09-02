package operation.job;

import org.apache.commons.text.StrSubstitutor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.tool.mail.MailEntity;
import viewcoder.tool.mail.MailHelper;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.config.GlobalConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/1.
 */
public class NightJobTest {

    private static Logger logger = LoggerFactory.getLogger(NightJobTest.class);

    @Test
    public void testMail(){
        MailEntity mailEntity = new MailEntity("2920248385@qq.com", Common.MAIL_SERVICE_EXPIRE, Common.MAIL_HTML_TYPE);
        String mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + "expire_no_space.html"; //本地网页数据
        Map<String, String> replaceData = new HashMap<String, String>();
        replaceData.put("name", "张斌");
        replaceData.put("service_name", CommonObject.getServiceName(1));
        replaceData.put("expire_date", "2018-01-01 00:00:00");
        replaceData.put("url", GlobalConfig.getProperties(Common.SERVICE_SPACE_URL));
        replaceData.put("days", "0");
        replaceData.put("space_remain", "-123M");
        String str = StrSubstitutor.replace(MailHelper.getHtmlData(mailUrl, true), replaceData);
        mailEntity.setTextAndContent(str);
        new MailHelper(mailEntity).send();
    }
}
