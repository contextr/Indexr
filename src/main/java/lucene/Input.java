package lucene;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.StringJoiner;

//@Indexed
//@Entity(name="input")
public class Input implements Serializable {
	private static final long serialVersionUID = 5517056608258504818L;
	
	//	@Id @GeneratedValue
	private LinkedList<String> prefix;
	private String next;
	private String profile;
	private Integer uid;
	private Integer frequency;
	
	public final String getPrefix() {
		StringJoiner joiner = new StringJoiner("_");
		for (String string : prefix) {
			joiner.add(string);
		}
		return joiner.toString();
	}
	public final void setPrefix(String prefix) {
		String[] split = prefix.split(" ");
		this.prefix = new LinkedList<>();
		for (String string : split) {
			this.prefix.add(string.intern());
		}
	}
	public final String getNext() {
		return next;
	}
	public final void setNext(String next) {
		this.next = next.intern();
	}
	public final String getProfile() {
		return profile;
	}
	public final void setProfile(String profile) {
		this.profile = profile;
	}
	public final void setPrefix(LinkedList<String> prefix) {
		this.prefix = prefix;
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
