package lucene;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.Indexed;

@Indexed
@Entity(name="input")
public class Input {
	
	@Id @GeneratedValue
	private Integer id;
	private String prefix;
	private String next;
	private Integer pid;
	private Integer uid;
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
	@Override
	public String toString() {
		return "Input [prefix=" + prefix + ", next=" + next + ", frequency=" + frequency + "]";
	}
	
}
