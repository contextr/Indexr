package lucene;

import org.hibernate.search.engine.BoostStrategy;

public class FrequencyBoostStrategy implements BoostStrategy {

	public float defineBoost(Object value) {
		if(value instanceof Input){
			Input input = (Input) value;
			return (float) Math.log(input.getFrequency());
		}
		return 1;
	}

}
