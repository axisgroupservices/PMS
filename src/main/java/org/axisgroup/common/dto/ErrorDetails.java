package org.axisgroup.common.dto;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ErrorDetails {
	private String field;
	private String issue;
	private List<Link> link;
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public List<Link> getLink() {
		return link;
	}
	public void setLink(List<Link> link) {
		this.link = link;
	}
	

}
