package com.feida.ams.api.pc.process.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class ProcessCcList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer definitionType;

    private Integer relatedId;

    private Integer accountId;

    public Integer getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(Integer definitionType) {
        this.definitionType = definitionType;
    }

    public Integer getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Integer relatedId) {
        this.relatedId = relatedId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}