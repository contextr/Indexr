/*package lucene;

import java.io.IOException;
import java.util.Scanner;
import java.util.StringJoiner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@SpringBootApplication
@ComponentScan(basePackages = "lucene")
public class Lucene4 implements CommandLineRunner {

	@Autowired
	private InputJPA jpa;

	@PersistenceContext
	private EntityManager entityManager;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(Lucene4.class, args);
	}

	public void run(String... args) throws Exception {

		
		 * FileReader fr = new FileReader("text"); StringBuilder builder = new
		 * StringBuilder();
		 * 
		 * int c; while ((c = fr.read()) != -1) { builder.append((char) c); }
		 * fr.close();
		 * 
		 * String data = builder.toString();
		 * 
		 * String[] lines = data.split("([.?!:;,](?=\\s)|\\R)");
		 * 
		 * List<String> strings = new LinkedList<String>(); Stack<Character>
		 * characters = new Stack<Character>();
		 * 
		 * for (int i = 0; i < lines.length; i++) { String string = lines[i];
		 * string = string.trim();
		 * 
		 * string.replaceAll("(\".*?\")|('.*?'(?=\\s|\\R))", "");
		 * 
		 * StringBuilder str = new StringBuilder();
		 * 
		 * for (int j = 0; j < string.length(); j++) { char charAt =
		 * string.charAt(j);
		 * 
		 * if ("[{()}]".contains(String.valueOf(charAt)) &&
		 * !characters.isEmpty()) { Character peek = characters.peek();
		 * 
		 * switch (charAt) { case '(': case '[': case '{':
		 * characters.push(charAt); break; case '}': if (peek == '{')
		 * characters.pop(); break; case ']': if (peek == '[') characters.pop();
		 * break; case ')': if (peek == '(') characters.pop(); break; }
		 * 
		 * continue; } else if ("[{(".contains(String.valueOf(charAt))) {
		 * characters.push(charAt); continue; } if (characters.isEmpty())
		 * str.append(charAt);
		 * 
		 * }
		 * 
		 * if (!characters.isEmpty()) continue;
		 * 
		 * string = str.toString().replaceAll("\\s+", " ").trim();
		 * 
		 * if (string.isEmpty()) continue;
		 * 
		 * strings.add(string); System.out.println(string); }
		 * 
		 * for (String sentence : strings) { String[] words = sentence.split(" "
		 * ); for (int i = 0; i < words.length; i++) {
		 * 
		 * boolean updated = false; Iterable<Input> findByPrefixAndNext =
		 * jpa.findByPrefixAndNext(getPrefix(words, i), words[i]); for (Input
		 * input : findByPrefixAndNext) {
		 * input.setFrequency(input.getFrequency() + 1); jpa.save(input);
		 * updated = true; }
		 * 
		 * if (!updated) {
		 * 
		 * Input input = new Input(); input.setPid(1); input.setUid(1);
		 * input.setFrequency(1); input.setPrefix(getPrefix(words, i));
		 * input.setNext(words[i]); jpa.save(input);
		 * 
		 * } } }
		 

		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search
				.getFullTextEntityManager(entityManager);

		// create the query using Hibernate Search query DSL
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(User.class)
				.get();

		// a very basic query by keywords
		Query query = queryBuilder.keyword().onFields("name", "city", "email").matching(text).createQuery();

		// wrap Lucene query in an Hibernate Query object
		FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, User.class);

		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {
			String input = scanner.nextLine();
			long currentTimeMillis = System.currentTimeMillis();
			Iterable<Input> findByPrefix = jpa.findByPrefix(input,
					new PageRequest(0, 10, new Sort(Direction.DESC, "frequency")));
			System.out.println(System.currentTimeMillis() - currentTimeMillis);
			StringJoiner joiner = new StringJoiner(", ");
			for (Input input2 : findByPrefix) {
				joiner.add(input2.getNext());
			}
			System.out.println(joiner.toString());
			System.out.println();
		}
	}

	private String getPrefix(String[] words, int i) {
		if (i == 0)
			return "";
		return words[i - 1];
	}

}
*/