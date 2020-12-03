package php.date;

public class TimeException extends RuntimeException {

    static ThreadLocal<String> lastTimeStr = new ThreadLocal<>();

    public TimeException(String msg) {
        super("解析出错 " + getLastTimeStr() + ", " + msg);
    }

    public TimeException(Throwable cause) {
        super("解析出错 " + getLastTimeStr() + ", " + cause.getMessage(), cause);
    }

    static String getLastTimeStr() {
        String timeStr = lastTimeStr.get();
        return timeStr == null ? "" : timeStr;
    }

    static void setLastTimeStr(String timeStr) {
        lastTimeStr.set(timeStr);
    }

}
