package FrontEnd.exceptions.project;

/**
 * Created by Administrator on 2018/2/6.
 */
public class ProjectListException extends Exception{

    public ProjectListException() {
        super();
    }

    public ProjectListException(String message) {
        super(message);
    }

    public ProjectListException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectListException(Throwable cause) {
        super(cause);
    }

    protected ProjectListException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
