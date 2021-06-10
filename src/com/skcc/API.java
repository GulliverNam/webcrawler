package com.skcc;

public class API {
	
	private String apiId;
	private String headerBody;
	private String contentId;
	private String contentName;
	private String typeLength;
	private boolean required;
	private String description;
	
	public API(String apiId, String headerBody, String contentId, 
			String contentName, String typeLength,
			boolean required, String description) {
		super();
		this.apiId = apiId;
		this.headerBody = headerBody;
		this.contentId = contentId;
		this.contentName = contentName;
		this.typeLength = typeLength;
		this.required = required;
		this.description = description;
	}
	public String getApiId() {
		return apiId;
	}
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	public String getHeaderBody() {
		return headerBody;
	}
	public void setHeaderBody(String headerBody) {
		this.headerBody = headerBody;
	}
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String getContentName() {
		return contentName;
	}
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}
	public String getTypeLength() {
		return typeLength;
	}
	public void setTypeLength(String typeLength) {
		this.typeLength = typeLength;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "API [apiId=" + apiId + ", headerBody=" + headerBody + ", contentId=" + contentId + ", contentName="
				+ contentName + ", typeLength=" + typeLength + ", required=" + required + ", description=" + description
				+ "]";
	}
	
	

}
