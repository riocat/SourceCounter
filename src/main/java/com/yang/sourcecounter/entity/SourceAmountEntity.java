package com.yang.sourcecounter.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/10/10.
 */
public class SourceAmountEntity implements Serializable {

    private String language;
    private int files;
    private int blank;
    private int comment;
    private int code;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getFiles() {
        return files;
    }

    public void setFiles(int files) {
        this.files = files;
    }

    public int getBlank() {
        return blank;
    }

    public void setBlank(int blank) {
        this.blank = blank;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
