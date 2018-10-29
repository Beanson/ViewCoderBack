package viewcoder.operation.impl.transfer;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/10/29.
 * 企业转账到零钱的实体类
 */
public class Transfers implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 商户账号appid
     */
    public String mch_appid;
    /**
     * 微信支付商户号
     */
    public String mchid;
    /**
     * 随机串
     */
    public String nonce_str;
    /**
     * 商户订单号
     */
    public String partner_trade_no;
    /**
     * 用户id
     */
    public String openid;
    /**
     * 是否校验用户姓名 NO_CHECK：不校验真实姓名  FORCE_CHECK：强校验真实姓名
     */
    public String check_name;

    /**
     * 金额 单位：分
     */
    public Integer amount;
    /**
     * 企业付款描述信息
     */
    public String desc;
    /**
     * ip地址
     */
    public String spbill_create_ip;

    /**
     * 签名
     */
    public String sign;


    public String getMch_appid() {
        return mch_appid;
    }

    public void setMch_appid(String mch_appid) {
        this.mch_appid = mch_appid;
    }

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String mchid) {
        this.mchid = mchid;
    }

    public String getNonce_str() {
        return nonce_str;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getPartner_trade_no() {
        return partner_trade_no;
    }

    public void setPartner_trade_no(String partner_trade_no) {
        this.partner_trade_no = partner_trade_no;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getCheck_name() {
        return check_name;
    }

    public void setCheck_name(String check_name) {
        this.check_name = check_name;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSpbill_create_ip() {
        return spbill_create_ip;
    }

    public void setSpbill_create_ip(String spbill_create_ip) {
        this.spbill_create_ip = spbill_create_ip;
    }

}
