package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class IRCTProcess implements Serializable {
	private static final long serialVersionUID = 30045608286165958L;

	@Id
	private long id;
	
	private String name;
	private String description;
	
	@OneToMany
	private List<IRCTProcessParameter> parameter;
	
	/**
	 * Returns the id
	 * 
	 * @return ID
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id ID
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the process
	 * 
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the process
	 * 
	 * @param name Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a description of the process
	 * 
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the process
	 * 
	 * @param description Description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a list parameters
	 * 
	 * @return List of parameters
	 */
	public List<IRCTProcessParameter> getParameter() {
		return parameter;
	}

	/**
	 * Sets a list of the parameters
	 * 
	 * @param parameter List of the parameters
	 */
	public void setParameter(List<IRCTProcessParameter> parameter) {
		this.parameter = parameter;
	}
	
	

}
