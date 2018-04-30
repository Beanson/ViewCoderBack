package viewcoder.exception.personal;

/**
 * Created by Administrator on 2018/2/16.
 */
public class PersonalException extends Exception {

    public PersonalException() {
        super();
    }

    public PersonalException(String message) {
        super(message);
    }

    public PersonalException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersonalException(Throwable cause) {
        super(cause);
    }

    protected PersonalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
