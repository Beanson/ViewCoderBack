package viewcoder.tool.common;

import org.apache.log4j.Logger;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.encrypt.AESEncryptor;

/**
 * Created by Administrator on 2018/2/12.
 */
public class Common {

    private static Logger logger = Logger.getLogger(Common.class.getName());

    //Genreal信息
    public static String EMPTY = "";

    //不同环境共享的配置
    public static String TARGET_ENVIRONMENT = "com.viewcoder.target.environement"; //设定目标环境
    public static String SETTING_FILES = "com.viewcoder.setting.files"; //设定目标环境下的文件
    public static String DEV_ENVIRONMENT = "dev"; //开发环境
    public static String PROD_ENVIRONMENT = "prod"; //生产环境

    //异地重新登录设置
    public static Integer RELOGIN_ALERT = -1;
    public static String RETURN_TEXT_HTML = "text/html;charset=UTF-8";
    public static String RETURN_JSON = "application/json";

    //后台返回status_code
    public final static int STATUS_CODE_OK = 200;
    public final static String STATUS_OK = "OK";
    public final static String SUCCESS = "SUCCESS";
    public final static String FAIL = "FAIL";

    //PSD文件解析出来后的组件类型
    public final static String COMMON_IMAGE = "Common_Image";
    public final static String COMMON_TEXT = "Common_Text";
    public final static String COMMON_BACKGROUND = "Common_Background";

    //user_upload_file的文件类型 file_type
    public final static int FILE_TYPE_IMAGE = 1;
    public final static int FILE_TYPE_VIDEO = 2;
    public final static int FILE_TYPE_SOUND = 3;
    public final static int FILE_TYPE_DOWNLOAD = 4;

    //各种类型套餐
    public final static int SERVICE_TRY = 1;
    public final static int SERVICE_TRY_NUM = 3;
    public final static int SERVICE_MONTH = 2;
    public final static int SERVICE_YEAR = 3;
    public final static int SERVICE_BUSINESS = 4;

    //各种套餐可使用空间
    public final static int SERVICE_TRY_RESOURCE = 20; //免费使用三天20M的使用空间
    public final static int SERVICE_MONTH_RESOURCE = 100; //一个月使用100M空间
    public final static int SERVICE_YEAR_RESOURCE = 1024;//一年使用1024M空间
    public final static int SERVICE_BUSINESS_RESOURCE = 1024;//一年使用1024M空间


    //user_upload_file的文件类型 is_folder
    public final static int FOLDER_FILE = 1;
    public final static int NOT_FOLDER_FILE = 0;

    //timeout配置
    public final static String TIMEOUT_10000 = "10000";

    //XML的配置文件类型
    public final static String XML_MSG_TYPE = "msg";

    //用户数据
    public final static String PHONE = "phone";
    public final static String CODE = "code";

    //短信数据
    public final static String MESG_REGISTER_VERIFY_CODE = "SMS_142010203";
    public final static String MESG_ALERT_EXPIRY = "SMS_142000294";
    public final static String MESG_PURCHASE_SUCCESS = "SMS_142020247";


    //各种配置文件的key值
    //阿里云key
    public static final String ACCESS_KEY_ID = "com.viewcoder.aliyun.access.key";
    public static final String ACCESS_KEY_SECRET = "com.viewcoder.aliyun.access.secret";

    //文件存储配置信息
    public final static String PORTRAIT_IMG = "com.viewcoder.file.portrait_img";
    public final static String MULTI_EXPORT = "com.viewcoder.file.multi_export";
    public final static String SINGLE_EXPORT = "com.viewcoder.file.single_export";
    public final static String PROJECT_DATA = "com.viewcoder.file.project_data";
    public final static String UPLOAD_FILES = "com.viewcoder.file.upload_files";
    public final static String PSD_PARSE_ERROR = "com.viewcoder.file.psd_parse_error";
    public final static String FEEDBACK = "com.viewcoder.file.feedback";

    //OSS配置信息
    public static String FILE_SYS_BASE_URL_KEY = "com.viewcoder.file.system.base.url";
    public static String FILE_OSS_BASE_URL_KEY = "com.viewcoder.file.oss.base.url";
    public static String FILE_HTTP_BASE_URL_KEY = "com.viewcoder.file.http.base.url";

    //配置查看资源空间的URL
    public static String SERVICE_SPACE_URL = "com.viewcoder.service.space.url";

    //邮件配置
    public static String MAIL_BASE_URL = "com.viewcoder.mail.base.url";
    public static String MAIL_HOST = "com.viewcoder.mail.host";
    public static String MAIL_USERNAME = "com.viewcoder.mail.username";
    public static String MAIL_FROM = "com.viewcoder.mail.from";
    public static String MAIL_PASS = "com.viewcoder.mail.pass";
    public static String MAIL_PROTOCOL = "com.viewcoder.mail.protocol";
    public static String MAIL_PORT = "com.viewcoder.mail.port";
    public static String MAIL_AUTH = "com.viewcoder.mail.auth";
    public static String MAIL_TIMEOUT = "com.viewcoder.mail.timeout";
    public static String MAIL_SERVICE_EXPIRE_INFORM = "ViewCoder实例即将到期提醒";

    //阿里云短信服务配置
    public static final String MSG_PRODUCT = "com.viewcoder.msg.product";
    public static final String MSG_DOMAIN = "com.viewcoder.msg.domain";
    public static final String MSG_ENDPOINT = "com.viewcoder.msg.endPointName";
    public static final String MSG_REGIONID = "com.viewcoder.msg.regionId";

    //阿里云短信直接调用配置
    public static final String MSG_SIGNNAME_LIPHIN = "莱芬科技"; //短信签名
    public static final String MSG_TEMPLEATE_EXPIRE1 = "SMS_142954544"; //套餐服务到期提醒
    public static final String MSG_TEMPLEATE_RELEASE = "instance_release"; //instance释放提醒, 暂时不提醒
    public static final String MSG_TEMPLEATE_EXPIRE2 = "instance_expire2"; //instance过期且无剩余空间
    public static final String MSG_TEMPLEATE_PURCHASE = "instance_purchase";//成功购买instance提醒
    public static final String MSG_TEMPLEATE_VERIFY = "verification_code"; //验证码提醒

    //mail发送的HTML
    public static final String MAIL_SERVICE_EXPIRE = "service_expire.html"; //套餐服务过期HTML


    //Test配置信息
    public static String PROJECT_TEST_INTEGRATION = "com.viewcoder.project.test.integration";

    //AliPay配置信息
    public static String PAY_ALI_GW_URL = "com.pay.ali.gw_url";
    public static String PAY_ALI_APPID = "com.pay.ali.appid";
    public static String PAY_ALI_PRIVATE_KEY = "com.pay.ali.app_private_key";
    public static String PAY_ALI_FORMAT = "com.pay.ali.format";
    public static String PAY_ALI_CHARSET = "com.pay.ali.charset";
    public static String PAY_ALI_PUBLIC_KEY = "com.pay.ali.alipay_public_key";
    public static String PAY_ALI_SIGN_TYPE = "com.pay.ali.sign_type";
    public static String PAY_ALI_RETURN_URL = "com.pay.ali.return_url";
    public static String PAY_ALI_NOTIFY_URL = "com.pay.ali.notify_url";
    public static String PAY_ALI_PRODUCT_CODE = "com.pay.ali.product_code";
    //Alipay一些发送请求参数
    public static final String PAY_ALI_KEY_TRADE_NO = "out_trade_no"; //交易单号，唯一
    public static final String PAY_ALI_KEY_PRODUCT_CODE = "product_code"; //现仅支持 FAST_INSTANT_TRADE_PAY 模式
    public static final String PAY_ALI_KEY_TOTAL_AMOUNT = "total_amount"; //总金额，字符串类型，支持小数点后两位
    public static final String PAY_ALI_KEY_SUBJECT = "subject"; //商品描述
    public static final String PAY_ALI_KEY_PASSBACK_PARAMS = "passback_params"; //设置进去的参数，回调后原样返回

    //WechatPay配置信息
    public static final String PAY_WECHAT_APPID = "com.pay.wechat.appid";
    public static final String PAY_WECHAT_APP_SECRET = "com.pay.wechat.app_secret";
    public static final String PAY_WECHAT_MCH_ID = "com.pay.wechat.mch_id";
    public static final String PAY_WECHAT_API_KEY = "com.pay.wechat.api_key";
    public static final String PAY_WECHAT_UNIFIED_URL = "com.pay.wechat.unifiedoder_url";
    public static final String PAY_WECHAT_NOTIFY_URL = "com.pay.wechat.notify_url";
    public static final String PAY_WECHAT_CREATE_IP = "com.pay.wechat.create_ip";
    public static final String PAY_WECHAT_FEE_TYPE = "com.pay.wechat.fee_type";
    //WechatPay配置一些参数
    public static final String PAY_WECHAT_KEY_OPENID = "openid"; //微信支付客户的openid值
    public static final String PAY_WECHAT_KEY_APPID = "appid"; //微信支付分配的公众账号ID
    public static final String PAY_WECHAT_KEY_MCH_ID = "mch_id"; //微信支付分配的商户号
    public static final String PAY_WECHAT_KEY_NONCE_STR = "nonce_str"; //随机字符串，长度要求在32位以内。推荐
    public static final String PAY_WECHAT_KEY_BODY = "body"; //商品简单描述
    public static final String PAY_WECHAT_KEY_OUT_TRADE_NO = "out_trade_no"; //商户系统内部订单号
    public static final String PAY_WECHAT_KEY_FEE_TYPE = "fee_type"; //符合ISO 4217标准的三位字母代码，默认人民币：CNY
    public static final String PAY_WECHAT_KEY_TOTAL_FEE = "total_fee"; //订单总金额，单位为分
    public static final String PAY_WECHAT_KEY_CREATE_IP = "spbill_create_ip"; //APP和网页支付提交用户端ip
    public static final String PAY_WECHAT_KEY_NOTIFY_URL = "notify_url"; //异步接收微信支付结果通知的回调地址
    public static final String PAY_WECHAT_KEY_TRADE_TYPE = "trade_type"; //JSAPI 公众号支付, NATIVE 扫码支付, APP APP支付
    public static final String PAY_WECHAT_KEY_ATTACH = "attach"; //附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
    public static final String PAY_WECHAT_KEY_SIGN = "sign"; //通过签名算法计算得出的签名值
    public static final String PAY_WECHAT_RES_CODE_URL = "code_url"; //通过请求wechatPay接口获取的返回字符串提取变量
    public static final String PAY_WECHAT_NOTIFY_RESULT_CODE = "result_code"; //支付结果变量
    public static final String PAY_WECHAT_NOTIFY_RESULT_CODE_SUCCESS = "SUCCESS"; //支付结果变量--SUCCESS
    public static final String PAY_WECHAT_NOTIFY_ERROR_CODE = "err_code"; //支付错误代码
    public static final String PAY_WECHAT_NOTIFY_ERROR_DEPICT = "err_code_des"; //支付错误描述
    public static final String PAY_WECHAT_TRANSACTION_ID = "transaction_id"; //相当于支付宝的trade_no
    public static final String PAY_WECHAT_PRODUCT_ID = "product_id"; //trade_type为native时要传


    //Web driver pool消息获取
    public static final String DRIVER_LOCATION = "com.viewcoder.webdriver.location";
    public static final String PAGELOAD_TIMEOUT = "com.viewcoder.webdriver.timeout.pageload";
    public static final String DRIVER_MAX_ACTIVE = "com.viewcoder.webdriver.max.active";
    public static final String DRIVER_MAX_WAIT = "com.viewcoder.webdriver.max.wait";
    public static final String DRIVER_MIN_IDLE = "com.viewcoder.webdriver.min.idle";
    public static final String DRIVER_CHECK_TIME = "com.viewcoder.webdriver.check.time";
    public static final String DRIVER_CHECK_NUM = "com.viewcoder.webdriver.check.num";


    //一些数据库定义的字段
    public static final String ID = "id";
    public static final String PROJECT = "project";
    public static final String PROJECT_ID = "project_id";
    public static final String USER_ID = "user_id";
    public static final String SESSION_ID = "session_id";
    public static final String FILE_TYPE = "file_type";
    public static final String NEW_FILE_NAME = "new_file_name";
    public static final String TIME_STAMP = "timestamp";
    public static final String VERSION = "version";
    public static final String PC_VERSION = "pc_version";
    public static final String MO_VERSION = "mo_version";
    public static final String PROJECT_NAME = "project_name";
    public static final String OPT_TYPE = "opt_type";
    public static final String PROJECT_FILE_NAME = "project_file_name";
    public static final String LAST_MODIFY_TIME = "last_modify_time";
    public static final String VIDEO_ID = "video_id";
    public static final String VIDEO_IMAGE = "video_image"; //video的截图文件
    public static final String VIDEO_IMAGE_NAME = "video_image_name"; //video截图文件名, timestamp.png
    public static final String ORDER_FROM_DATE = "order_from_date"; //订单开始时间
    public static final String ORDER_END_DATE = "order_end_date"; //订单结束时间
    public static final String PAY_STATUS = "pay_status"; //支付状态
    public static final String PAY_WAY = "pay_way"; //支付方式
    public static final String PRICE = "price"; //价格
    public static final String SERVICE_ID = "service_id"; //服务类型
    public static final String SERVICE_NUM = "service_num"; //服务类型
    public static final String IS_PUBLIC = "is_public"; //该项目是否为公开项目
    public static final String INDUSTRY_CODE = "industry_code"; //行业一级代号
    public static final String INDUSTRY_SUB_CODE = "industry_sub_code"; //行业一级代号
    public static final String EXTEND_UNIT = "extend_unit"; //扩容或续期的单位
    public static final String EXTEND_SIZE = "extend_size"; //扩容或续期的容量
    public static final String EXTEND_TYPE = "extend_type"; //是扩容还是续期的类型
    public static final String SPACE_INFO = "space_info"; //资源空间资料
    public static final String SPACE_EXPIRE = "space_expire"; //过期的资源
    public static final String INSTANCE_INFO = "instance_info"; //实例资料
    public static final String REF_ID = "ref_id"; //引用参照的project_id
    public static final String PARENT = "parent"; //parent数据
    public static final String NEW_PARENT = "new_parent"; //设置更元素新的parent
    public static final String WEB_URL = "web_url"; //通过URL生成网页的操作
    public static final String TARGET_WIDTH = "target_width"; //浏览器的宽度
    public static final String PROJECT_SIMULATE = "simulate"; //项目类型是simulate类型
    public static final String PAGE_PARENT = "parent"; //页面的父页面
    public static final String OPT = "opt"; //具体操作类型
    public static final String COMPANY_CREDIT = "company_credit"; //公司credit, company_credit
    public static final String OPENID = "openid"; //获取用户扫码登录的openid
    public static final String ACCOUNT = "account"; //获取用户登录的账号信息
    public static final String RESOURCE_REMAIN = "resource_remain"; //资源剩余空间
    public static final String RESOURCE_DELETE_SIZE = "resource_delete_size"; //删除该资源可释放的空间
    public static final String RESOURCE_DELETE_DB_NUM = "resource_delete_db_num"; //资源在数据库中删除数目
    public static final String RESOURCE_DELETE_OSS_NUM = "resource_delete_oss_num"; //资源在oss中删除数目


    //数字证书
    public static final String SIGN_FILE = "sign_file"; //数字签名文件
    public static final String SIGN_STR = "sign_str"; //数字签名文件内容
    public static final String OUT_TRADE_NO = "out_trade_no"; //商户订单号
    public static final String TRADE_NO = "trade_no"; //支付宝交易号
    public static final String SIGN_PRIVATE_KEY = "com.pay.sign.private_key";
    public static final String SIGN_PUBLIC_KEY = "com.pay.sign.public_key";
    public static final String STATUS = "status";//支付凭证验证情况

    //的AES加解密的key
    public static final String AES_KEY = "@Admin123*Go";

    //编码信息
    public static final String GBK = "GBK";
    public static final String UTF8 = "UTF-8";

    //其他配置信息
    public static final String IMG_PNG = ".png";
    public static final String IMG_PSG = ".psd";
    public static final String TIME_FORMAT_1 = "yyyy-MM-dd HH:mm:ss";
    public static final String RELATIVE_PATH_SUFFIX = "/";
    public static final String PC_V = "pc";
    public static final String MOBILE_V = "mo";
    public static final String STORE_TYPE = "store";
    public static final Integer EXAMPLE_REF_ID = 140;
    public static final String EXAMPLE_CASE_1 = "样例1";
    public static final String DEFAULT_PORTRAIT = "default_portrait.png";


    //mail的其他配置
    public static final String MAIL_HTML_TYPE = "html";
    public static final String MAIL_TEXT_TYPE = "text";

    //测试辅助数据
    public static final String BASE_HTTP_URL = "http://127.0.0.1:8080/";


    //文件后缀的相关数据
    public static String PROJECT_FILE_SUFFIX = "-index.html"; //项目文件的后缀名
    public static String PROJECT_DATA_SUFFIX = ".txt"; //项目数据文件的后缀名
    public static String TEXT_FILE_SUFFIX = ".txt"; //txt文件的后缀名
    public static String DOT_SUFFIX = "."; //中间分隔符
}
