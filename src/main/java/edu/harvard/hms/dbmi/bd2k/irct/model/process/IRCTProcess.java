package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class IRCTProcess implements Serializable {
	private static final long serialVersionUID = 30045608286165958L;

	@Id
	private long id;
	
	private String name;
	private String description;
	
	@OneToMany
	private List<IRCTProcessParameter> parameter;
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<IRCTProcessParameter> getParameter() {
		return parameter;
	}

	public void setParameter(List<IRCTProcessParameter> parameter) {
		this.parameter = parameter;
	}
	
	

}
