package com.feida.ams.api.pc.process.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
@Table(name = "process_log_t")
public class ProcessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer formTypeId;

    private Integer formId;

    private Integer processInstanceId;

    private Integer processNodeId;

    private Integer actorId;

    private Integer actorType;

    private String actorOpinion;

    private Date actorTime;

    private Date taskCreateTime;

    private Integer taskCreator;

    private Integer transferFlag;

    public ProcessLog() {
        this.actorTime = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    public Integer getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Integer processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Integer getProcessNodeId() {
        return processNodeId;
    }

    public void setProcessNodeId(Integer processNodeId) {
        this.processNodeId = processNodeId;
    }

    public Integer getActorId() {
        return actorId;
    }

    public void setActorId(Integer actorId) {
        this.actorId = actorId;
    }

    public Integer getActorType() {
        return actorType;
    }

    public void setActorType(Integer actorType) {
        this.actorType = actorType;
    }

    public String getActorOpinion() {
        return actorOpinion;
    }

    public void setActorOpinion(String actorOpinion) {
        this.actorOpinion = actorOpinion;
    }

    public Date getActorTime() {
        return actorTime;
    }

    public void setActorTime(Date actorTime) {
        this.actorTime = actorTime;
    }

    public Date getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Date taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public Integer getTaskCreator() {
        return taskCreator;
    }

    public void setTaskCreator(Integer taskCreator) {
        this.taskCreator = taskCreator;
    }

    public Integer getTransferFlag() {
        return transferFlag;
    }

    public void setTransferFlag(Integer transferFlag) {
        this.transferFlag = transferFlag;
    }
}