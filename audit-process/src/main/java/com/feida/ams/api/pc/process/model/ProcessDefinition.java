package com.feida.ams.api.pc.process.model;

import com.feida.common.domain.Dto;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "process_definition_t")
public class ProcessDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer formTypeId;

    private Integer parentId;

    private Integer copyCondition;

    private String relationRegex;

    private String relationContent;

    private String relationJson;

    private Integer processDefinitionVersion;

    private Integer creator;

    private Date createTime;

    private Integer modifier;

    private Date modifiedTime;

    private Integer deleteFlag;

    private Integer roadId;
    @Transient
    private List<Dto> processNodeList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getCopyCondition() {
        return copyCondition;
    }

    public void setCopyCondition(Integer copyCondition) {
        this.copyCondition = copyCondition;
    }

    public String getRelationRegex() {
        return relationRegex;
    }

    public void setRelationRegex(String relationRegex) {
        this.relationRegex = relationRegex;
    }

    public String getRelationContent() {
        return relationContent;
    }

    public void setRelationContent(String relationContent) {
        this.relationContent = relationContent;
    }

    public String getRelationJson() {
        return relationJson;
    }

    public void setRelationJson(String relationJson) {
        this.relationJson = relationJson;
    }

    public Integer getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getModifier() {
        return modifier;
    }

    public void setModifier(Integer modifier) {
        this.modifier = modifier;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Integer getRoadId() {
        return roadId;
    }

    public void setRoadId(Integer roadId) {
        this.roadId = roadId;
    }

    public List<Dto> getProcessNodeList() {
        return processNodeList;
    }

    public void setProcessNodeList(List<Dto> processNodeList) {
        this.processNodeList = processNodeList;
    }
}