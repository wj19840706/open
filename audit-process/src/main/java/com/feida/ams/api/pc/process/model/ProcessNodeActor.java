package com.feida.ams.api.pc.process.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "process_node_actor_t")
public class ProcessNodeActor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer processNodeId;

    private String executiveIds;

    private Integer executiveType;

    private Integer executiveLevel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProcessNodeId() {
        return processNodeId;
    }

    public void setProcessNodeId(Integer processNodeId) {
        this.processNodeId = processNodeId;
    }

    public String getExecutiveIds() {
        return executiveIds;
    }

    public void setExecutiveIds(String executiveIds) {
        this.executiveIds = executiveIds;
    }

    public Integer getExecutiveType() {
        return executiveType;
    }

    public void setExecutiveType(Integer executiveType) {
        this.executiveType = executiveType;
    }

    public Integer getExecutiveLevel() {
        return executiveLevel;
    }

    public void setExecutiveLevel(Integer executiveLevel) {
        this.executiveLevel = executiveLevel;
    }
}