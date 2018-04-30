package viewcoder.exception.render;

/**
 * Created by Administrator on 2018/2/16.
 */
public class RenderException extends Exception{

    public RenderException() {
        super();
    }

    public RenderException(String message) {
        super(message);
    }

    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RenderException(Throwable cause) {
        super(cause);
    }

    protected RenderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
