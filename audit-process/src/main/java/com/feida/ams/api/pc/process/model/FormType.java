package com.feida.ams.api.pc.process.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "form_type_t")
public class FormType {
    @Id
    private Integer id;

    private String formTypeName;
    private Integer processDefinitionVersion;
    private String remarks;
    private String taskTitleTemplate;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFormTypeName() {
        return formTypeName;
    }

    public void setFormTypeName(String formTypeName) {
        this.formTypeName = formTypeName;
    }

    public Integer getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTaskTitleTemplate() {
        return taskTitleTemplate;
    }

    public void setTaskTitleTemplate(String taskTitleTemplate) {
        this.taskTitleTemplate = taskTitleTemplate;
    }
}