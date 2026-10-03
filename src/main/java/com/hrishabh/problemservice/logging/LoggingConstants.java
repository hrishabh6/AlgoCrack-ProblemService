package com.hrishabh.problemservice.logging;

public final class LoggingConstants {

    public static final String REQUEST_ID = "request_id";
    public static final String USER_ID = "user_id";
    public static final String HTTP_METHOD = "http_method";
    public static final String HTTP_PATH = "http_path";
    public static final String HTTP_STATUS = "http_status";
    public static final String DURATION_MS = "duration_ms";
    public static final String REMOTE_IP = "remote_ip";
    public static final String USER_AGENT = "user_agent";
    public static final String SUBMISSION_ID = "submission_id";
    public static final String QUESTION_ID = "question_id";
    public static final String EXECUTION_ID = "execution_id";
    public static final String LANGUAGE = "language";
    public static final String STATUS = "status";
    public static final String VERDICT = "verdict";
    public static final String EVENT_TYPE = "event_type";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String ERROR_CODE = "error_code";
    public static final String EXCEPTION_CLASS = "exception_class";
    public static final String TYPE = "type";
    public static final String COMPONENT = "component";
    public static final String OPERATION = "operation";

    private LoggingConstants() {
    }

    public static final class EventType {
        public static final String REQUEST = "REQUEST";
        public static final String RESPONSE = "RESPONSE";
        public static final String ERROR = "ERROR";
        public static final String AUTH = "AUTH";
        public static final String SUBMISSION = "SUBMISSION";
        public static final String EXECUTION = "EXECUTION";
        public static final String DATABASE = "DATABASE";
        public static final String CACHE = "CACHE";
        public static final String EXTERNAL_CALL = "EXTERNAL_CALL";
        public static final String LIFECYCLE = "LIFECYCLE";

        private EventType() {
        }
    }

    public static String getHttpStatusType(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return "SUCCESS";
        }
        if (statusCode >= 300 && statusCode < 400) {
            return "REDIRECT";
        }
        if (statusCode >= 400 && statusCode < 500) {
            return "CLIENT_ERROR";
        }
        if (statusCode >= 500) {
            return "SERVER_ERROR";
        }
        return "UNKNOWN";
    }
}
