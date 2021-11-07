package com.oneplatform.modules.system.dto.param;

import com.jeesuite.springweb.model.BaseQueryParam;


public class FunctionResourceQueryParam extends BaseQueryParam {

    /**
     * 父ID
     */
    private Integer parentId;

    /**
     * 资源名称（可模糊）
     */
    private String name;
    
    private String code;
    
    private String type;
    
    private Boolean enabled;

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
