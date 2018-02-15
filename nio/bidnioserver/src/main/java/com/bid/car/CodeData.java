package com.bid.car;

public class CodeData {
    private String sessionId;
    private String code;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "CodeData{" +
                "sessionId='" + sessionId + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
