package com.yang.sourcecounter.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2018/10/10.
 */
public class ProjectSourceAmount  implements Serializable {

    private String PorjectName;

    private List<SourceAmountEntity> sourceAmountEntityList;

    public String getPorjectName() {
        return PorjectName;
    }

    public void setPorjectName(String porjectName) {
        PorjectName = porjectName;
    }

    public List<SourceAmountEntity> getSourceAmountEntityList() {
        return sourceAmountEntityList;
    }

    public void setSourceAmountEntityList(List<SourceAmountEntity> sourceAmountEntityList) {
        this.sourceAmountEntityList = sourceAmountEntityList;
    }
}
