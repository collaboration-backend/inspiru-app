package com.stc.inspireu.beans;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MailMetadata {

	private String from;
	private String to;
	private Set<String> tos;
	private String subject;
	private String templateFile;
	private Map<String, Object> props;
	private String templateString;

    private List<String> attachments;

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Set<String> getTos() {
		return tos;
	}

	public void setTos(Set<String> tos) {
		this.tos = tos;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public String getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(String templateFile) {
		this.templateFile = templateFile;
	}

	public String getTemplateString() {
		return templateString;
	}

	public void setTemplateString(String templateString) {
		this.templateString = templateString;
	}

}
