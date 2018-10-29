package viewcoder.operation.impl.transfer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.purchase.wechat.HttpWechatPayUtil;
import viewcoder.operation.impl.purchase.wechat.WechatPay;
import viewcoder.tool.common.Common;
import viewcoder.tool.config.GlobalConfig;

import java.util.SortedMap;
import java.util.TreeMap;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by Administrator on 2018/10/29.
 */
public class WePayWithdraw {

    private static Logger logger = LoggerFactory.getLogger(WePayWithdraw.class.getName());

    // 微信的参数
    private WeixinConfigUtils config = new WeixinConfigUtils();

    /**
     * 微信提现（企业付款）
     */
    public static String weixinWithdraw(){
        // 构造签名的map
        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        Transfers transfers = new Transfers();

        XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
        String openId = "oaCnbs6EiIYbXgc8aYlRRSlJvqGk";
        String ip = "119.23.40.181";
        String money = "10";
        if (StringUtils.isNotBlank(money) && StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(openId)) {
            // 参数组
            String appid = "wx16c7efa55a7f976b";
            String mch_id = "1503031011";
            String nonce_str = "5K8264ILTKCH16CQ2502SI8ZNMTM67VS";
            //是否校验用户姓名 NO_CHECK：不校验真实姓名  FORCE_CHECK：强校验真实姓名
            String checkName ="NO_CHECK";
            //等待确认转账金额,ip,openid的来源
            Integer amount = Integer.valueOf(money);
            String spbill_create_ip = ip;
            String partner_trade_no = CommonService.getUnionId();
            //描述
            String desc = "红包金额"+amount/100+"元";
            // 参数：开始生成第一次签名
            parameters.put("mch_appid", appid);
            parameters.put("mchid", mch_id);
            parameters.put("nonce_str", nonce_str);
            parameters.put("partner_trade_no", partner_trade_no);
            parameters.put("openid", openId);
            parameters.put("check_name", checkName);
            parameters.put("amount", amount);
            parameters.put("desc", desc);
            parameters.put("spbill_create_ip", spbill_create_ip);
            String sign = WXSignUtils.createSign("UTF-8", parameters);

            transfers.setMch_appid(appid);
            transfers.setMchid(mch_id);
            transfers.setNonce_str(nonce_str);
            transfers.setPartner_trade_no(partner_trade_no);
            transfers.setOpenid(openId);
            transfers.setCheck_name(checkName);
            transfers.setAmount(amount);
            transfers.setDesc(desc);
            transfers.setSpbill_create_ip(spbill_create_ip);
            transfers.setSign(sign);

            //xStream.autodetectAnnotations(true);
            xStream.alias("xml", Transfers.class);
            String xmlInfo = xStream.toXML(transfers);

            logger.debug("prepare to send red package: "+ xmlInfo );
            try {
                String resXml = WePayHttpsPost.postOpt("https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers", xmlInfo);
                logger.debug("Response XML Data: " + resXml);

//                CloseableHttpResponse response =  HttpUtil.Post(weixinConstant.WITHDRAW_URL, xmlInfo, true);
//                String transfersXml = EntityUtils.toString(response.getEntity(), "utf-8");
//                Map<String, String> transferMap = HttpXmlUtils.parseRefundXml(transfersXml);
//                if (transferMap.size()>0) {
//                    if (transferMap.get("result_code").equals("SUCCESS") && transferMap.get("return_code").equals("SUCCESS")) {
//                        //成功需要进行的逻辑操作，
//
//                    }
//                }
//                System.out.println("成功");
            } catch (Exception e) {
                logger.debug("system error: "+ e);
                //log.error(e.getMessage());
                //throw new Exception(this, "企业付款异常" + e.getMessage());
            }
        }else {
            logger.debug("失败");
        }
        return "";
    }

}
