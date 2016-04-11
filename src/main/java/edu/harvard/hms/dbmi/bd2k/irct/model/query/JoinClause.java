package edu.harvard.hms.dbmi.bd2k.irct.model.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;

@Entity
public class JoinClause extends ClauseAbstract implements Serializable {

	private static final long serialVersionUID = -8136813552982367867L;
	
	@ElementCollection
	@CollectionTable(name="join_values", joinColumns=@JoinColumn(name="JOIN_VALUE"))
	@MapKeyColumn(name="join_id")
	@Column(name="join_value")
	private Map<String, String> values;
	
	@ManyToOne
	private JoinType joinType;
	
	public JoinClause() {
		this.values = new HashMap<String, String>();
	}
	

}
