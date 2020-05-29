package com.feida.ams.api.pc.process.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "process_node_t")
public class ProcessNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer processDefinitionId;

    private String nodeName;

    private Integer nodeNo;

    private Integer nodeType;

    private Integer passCodition;

    private Integer orderFlag;

    private Integer mandatoryFlag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(Integer processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getNodeType() {
        return nodeType;
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }

    public Integer getPassCodition() {
        return passCodition;
    }

    public void setPassCodition(Integer passCodition) {
        this.passCodition = passCodition;
    }

    public Integer getOrderFlag() {
        return orderFlag;
    }

    public void setOrderFlag(Integer orderFlag) {
        this.orderFlag = orderFlag;
    }

    public Integer getMandatoryFlag() {
        return mandatoryFlag;
    }

    public void setMandatoryFlag(Integer mandatoryFlag) {
        this.mandatoryFlag = mandatoryFlag;
    }
}