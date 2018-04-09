package FrontEnd.helper.parser.form;

import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * Created by Administrator on 2017/6/16.
 */
public interface CallbackTools {
    public void parseFile(FileUpload fileUpload);
}
