/*package lucene;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DynamicBoost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Indexed
@Entity(name="input")
@DynamicBoost(impl = FrequencyBoostStrategy.class)
public class Input2 {
	
	@Id @GeneratedValue
	private Integer id;
	@Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
	private String prefix;
	@Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	private String next;
	@Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
	private Integer pid;
	@Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
	private Integer uid;
	@Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
	private Integer frequency;
	
	public final Integer getId() {
		return id;
	}
	public final void setId(Integer id) {
		this.id = id;
	}
	public final String getPrefix() {
		return prefix;
	}
	public final void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public final String getNext() {
		return next;
	}
	public final void setNext(String next) {
		this.next = next;
	}
	public final Integer getPid() {
		return pid;
	}
	public final void setPid(Integer pid) {
		this.pid = pid;
	}
	public final Integer getUid() {
		return uid;
	}
	public final void setUid(Integer uid) {
		this.uid = uid;
	}
	public final Integer getFrequency() {
		return frequency;
	}
	public final void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	
}
*/