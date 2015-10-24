package lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringJoiner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = "lucene")
//@Transactional
@EnableAsync
public class Lucene implements CommandLineRunner {

	@Autowired
	private InputDAO dao;

	@PersistenceContext
	private EntityManager entityManager;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(Lucene.class, args);
	}

	@SuppressWarnings("unchecked")
	public void run(String... args) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		List readValue = mapper.readValue(new File("output"), List.class);
		
		StringJoiner builder = new StringJoiner("\n");
		readValue.forEach(o -> {
			Map<String, String> map = ((Map<String, String>) o);
			builder.add(map.get("text"));
		});

		String data = builder.toString();

		List<String> strings = extractSentences(data);
		int size = strings.size();
		int curr = 0;
		for (String sentence : strings) {
			curr++;
			System.out.println((float) curr / size);
			String[] words = sentence.split(" ");
			for (int i = 0; i < words.length; i++) {

				List<String> prefix = getPrefix(words, i);

				for (String string : prefix) {
					Input input = new Input();
					input.setPid(1);
					input.setUid(1);
					input.setFrequency(1);
					input.setPrefix(string);
					input.setNext(words[i]);
					dao.addInput(input);
				}
			}
		}
	}

	/*
	 * FullTextEntityManager fullTextEntityManager =
	 * org.hibernate.search.jpa.Search .getFullTextEntityManager(entityManager);
	 * try { fullTextEntityManager.createIndexer().startAndWait(); } catch
	 * (InterruptedException e) { System.out.println(
	 * "An error occurred trying to build the serach index: " + e.toString()); }
	 * 
	 * Scanner scanner = new Scanner(System.in);
	 * 
	 * if (scanner.hasNextLine()) { String input = scanner.nextLine();
	 * 
	 * long currentTimeMillis = System.currentTimeMillis(); QueryBuilder
	 * queryBuilder =
	 * fullTextEntityManager.getSearchFactory().buildQueryBuilder().
	 * forEntity(Input.class).get(); Query query = queryBuilder.phrase()
	 * .onField("prefix") .sentence(input) .createQuery(); FullTextQuery
	 * jpaQuery = fullTextEntityManager.createFullTextQuery(query, Input.class);
	 * jpaQuery.setMaxResults(15); List resultList = jpaQuery.getResultList();
	 * System.out.println(System.currentTimeMillis()-currentTimeMillis);
	 * System.out.println(resultList);
	 * 
	 * Iterable<Input> findByPrefixAndNext = jpa.findByPrefixAndNext("The",
	 * "war"); for (Input inp : findByPrefixAndNext) { System.out.println(inp);
	 * inp.setFrequency(5); jpa.save(inp); System.out.println(inp);
	 * System.out.println(inp.getId()); entityManager.refresh(inp); } }
	 * scanner.close();
	 */

	private List<String> extractSentences(String data) {
		String[] lines = data.split("([.?!:;,](?=\\s)|\\R)");

		List<String> strings = new LinkedList<String>();
		Stack<Character> characters = new Stack<Character>();

		for (int i = 0; i < lines.length; i++) {
			String string = lines[i];
			string = string.trim();

			string.replaceAll("(\".*?\")|('.*?'(?=\\s|\\R))", "");

			StringBuilder str = new StringBuilder();

			for (int j = 0; j < string.length(); j++) {
				char charAt = string.charAt(j);

				if ("[{()}]".contains(String.valueOf(charAt)) && !characters.isEmpty()) {
					Character peek = characters.peek();

					switch (charAt) {
					case '(':
					case '[':
					case '{':
						characters.push(charAt);
						break;
					case '}':
						if (peek == '{')
							characters.pop();
						break;
					case ']':
						if (peek == '[')
							characters.pop();
						break;
					case ')':
						if (peek == '(')
							characters.pop();
						break;
					}

					continue;
				} else if ("[{(".contains(String.valueOf(charAt))) {
					characters.push(charAt);
					continue;
				}
				if (characters.isEmpty())
					str.append(charAt);

			}

			if (!characters.isEmpty())
				continue;

			string = str.toString().replaceAll("\\s+", " ").trim();

			if (string.isEmpty())
				continue;
			
			string = StringEscapeUtils.escapeSql(string);
			
			strings.add(string);
			System.out.println(string);
		}
		return strings;
	}

	private List<String> getPrefix(String[] words, int i) {
		List<String> list = new ArrayList<String>(4);

		if (i == 0)
			return Arrays.asList("");
		for (int j = 1; j <= 4 && i >= j; j++) {
			StringJoiner joiner = new StringJoiner(" ");
			for (int k = i - j; k < i; k++) {
				joiner.add(words[k]);
			}
			list.add(joiner.toString());
		}

		return list;
	}

}
