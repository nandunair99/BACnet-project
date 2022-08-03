package covchecker.schedulerexception;

public class COVSchedulerException extends Exception {
    public COVSchedulerException(String message) {
        super(message);
    }

    public COVSchedulerException(String message, Throwable e) {
        super(message, e);
    }

    public COVSchedulerException(Throwable e) {
        super(e);
    }
}
