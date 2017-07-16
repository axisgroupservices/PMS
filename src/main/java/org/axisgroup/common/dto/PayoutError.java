package org.axisgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_DEFAULT)
public class PayoutError {
	private String name;
	private String debug_id;
	private String message;
	private String information_link;
	private ErrorDetails details;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDebug_id() {
		return debug_id;
	}
	public void setDebug_id(String debug_id) {
		this.debug_id = debug_id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getInformation_link() {
		return information_link;
	}
	public void setInformation_link(String information_link) {
		this.information_link = information_link;
	}
	public ErrorDetails getDetails() {
		return details;
	}
	public void setDetails(ErrorDetails details) {
		this.details = details;
	}

}
