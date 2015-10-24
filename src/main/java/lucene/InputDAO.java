package lucene;

import java.util.List;
import java.util.Optional;

public interface InputDAO {
	Optional<Boolean> addInput(Input input);
	List<String> nextWords(Input input);
}
