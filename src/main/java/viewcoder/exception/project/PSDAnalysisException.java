package viewcoder.exception.project;

/**
 * Created by Administrator on 2018/2/14.
 */
public class PSDAnalysisException extends Exception {

    public PSDAnalysisException() {
        super();
    }

    public PSDAnalysisException(String message) {
        super(message);
    }

    public PSDAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public PSDAnalysisException(Throwable cause) {
        super(cause);
    }

    protected PSDAnalysisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
