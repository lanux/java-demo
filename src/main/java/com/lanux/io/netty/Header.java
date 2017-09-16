package com.lanux.io.netty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lanux on 2017/9/16.
 */
public class Header {
    private int crcCode = 0xabef0101;

    private int length;

    private long sessionID;

    private byte type;

    private byte priority;

    private Map<String, Object> attachment = new HashMap<String, Object>();

    public int getCrcCode() {
        return crcCode;
    }

    public void setCrcCode(int crcCode) {
        this.crcCode = crcCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getSessionID() {
        return sessionID;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    public String toString() {
        return "Header [crcCode=" + crcCode + ", length=" + length + ", sessionID=" + sessionID
                + ", type=" + type + ", priority=" + priority + ", attachment=" + attachment + "]";
    }
}
