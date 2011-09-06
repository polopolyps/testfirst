package com.polopoly.ps.test;

public enum HttpContentType {
    HTML("text/html"), PLAINTEXT("text/plain");

    public static final String REQUEST_PARAMETER_NAME = "contenttype";
    private String mimeType;

    private HttpContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public static boolean isValidContentTypeString(String typeString) {
        try {
            HttpContentType.valueOf(typeString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getMimeType() {
        return mimeType;
    }
}
