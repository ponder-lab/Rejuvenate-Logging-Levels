package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
public class IRCTProcessParameter implements Serializable {
	
	private static final long serialVersionUID = 4141141792005064323L;
	
	@Id
	private long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private IRCTProcessParameterType type;
	private String value;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public IRCTProcessParameterType getType() {
		return type;
	}
	public void setType(IRCTProcessParameterType type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
