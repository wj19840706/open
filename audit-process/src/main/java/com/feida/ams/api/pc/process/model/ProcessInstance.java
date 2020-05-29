package com.feida.ams.api.pc.process.model;

import com.feida.common.util.WebUtils;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
@Table(name = "process_instance_t")
public class ProcessInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer processDefinitionId;

    private Integer applicant;

    private Integer applicantOrganization;

    private Integer currentNodeId;

    private Integer currentNodeNumber;

    private Integer status;

    private String auditCode;

    private Integer deleteFlag;

    private Integer creator;

    private Date createTime;

    private Integer modifier;

    private Date modifiedTime;

    private String formContent;

    private Integer commentFlag;

    private Integer formId;

    private Integer formTypeId;
    private String title;
    public ProcessInstance() {
        this.deleteFlag = 1;
        this.creator = WebUtils.getSessionAccountId();
        this.createTime = new Date();
        this.modifier = WebUtils.getSessionAccountId();
        this.modifiedTime = new Date();
    }

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

    public Integer getApplicant() {
        return applicant;
    }

    public void setApplicant(Integer applicant) {
        this.applicant = applicant;
    }

    public Integer getApplicantOrganization() {
        return applicantOrganization;
    }

    public void setApplicantOrganization(Integer applicantOrganization) {
        this.applicantOrganization = applicantOrganization;
    }

    public Integer getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(Integer currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public Integer getCurrentNodeNumber() {
        return currentNodeNumber;
    }

    public void setCurrentNodeNumber(Integer currentNodeNumber) {
        this.currentNodeNumber = currentNodeNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuditCode() {
        return auditCode;
    }

    public void setAuditCode(String auditCode) {
        this.auditCode = auditCode;
    }

    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
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

    public String getFormContent() {
        return formContent;
    }

    public void setFormContent(String formContent) {
        this.formContent = formContent;
    }

    public Integer getCommentFlag() {
        return commentFlag;
    }

    public void setCommentFlag(Integer commentFlag) {
        this.commentFlag = commentFlag;
    }

    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}