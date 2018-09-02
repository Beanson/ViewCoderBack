package viewcoder.tool.parser.text;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2018/3/18.
 */
public class TextData {
    private static Logger logger = LoggerFactory.getLogger(TextData.class.getName());

    /**
     * 获取http请求响应的text/plain类型数据传输
     *
     * @param msg
     * @return
     */
    public static String getText(Object msg) {
        ByteBuf in = ((FullHttpRequest) msg).content();
        String text = in.toString(CharsetUtil.UTF_8);
        TextData.logger.debug("Receive text from http: " + text);
        return text;
    }
}
