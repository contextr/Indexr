package lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@ComponentScan(basePackages = "lucene")
@EnableAsync
public class Lucene implements CommandLineRunner {

	private Map<Input, Integer> inputFrequencyMap;
	private List<String> words;
	
	@Autowired LuceneIndexr indexr;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(Lucene.class, args);
	}

	@Bean
	public ScheduledExecutorFactoryBean executorService() {
		ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
		bean.setPoolSize(50);
		return bean;
	}

	public void run(String... args) throws Exception {
		
		inputFrequencyMap = new HashMap<>();
		words = new LinkedList<>();
		
		System.out.println();
		readAndExtract("output", "Physics");
		readAndExtract("history", "History");
		addDictionary("words");
		
/*		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writerWithDefaultPrettyPrinter = mapper.writerWithDefaultPrettyPrinter();
		writerWithDefaultPrettyPrinter.writeValue(new File("json"),inputFrequencyMap);
*/		
		Future<Void> future = indexr.createIndex(inputFrequencyMap, words);
		String s = ".";
		while(!future.isDone()){
			if(s.length()==4)
				s = ".";
			System.out.print("\rWriting Index"+s+"     ");
			s+=".";
			Thread.sleep(750);
		}
		System.out.println("\rIndex Successfully Written");
	}

	private void addDictionary(String string) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(string));
		while(scanner.hasNext()){
			String word = scanner.next();
			if(word.length()<3)
				continue;
			this.words.add(word);
		}
		scanner.close();
	}

	@SuppressWarnings("unchecked")
	private void readAndExtract(String fileName, String profile) throws IOException, JsonParseException, JsonMappingException, InterruptedException {
		System.out.println("Processing Profile: "+profile);
		List<String> strings;

		{
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("rawtypes")
			List readValue = mapper.readValue(new File(fileName), List.class);
			StringJoiner builder = new StringJoiner("\n");
			readValue.forEach(o -> {
				Map<String, String> map = ((Map<String, String>) o);
				builder.add(map.get("text"));
			});

			String data = builder.toString();

			strings = extractSentences(data);
		}

		int size = strings.size();
		int curr = 0;
		for (String sentence : strings) {
			curr++;
			System.out.printf("\r%.2f %%           ", (float) (curr*100) / size);
			String[] words = sentence.split(" ");
			for (int i = 0; i < words.length; i++) {

				List<String> prefix = getPrefix(words, i);
				
				for (String string : prefix) {
					Input input = new Input();
					input.setProfile(profile);
					input.setUid(1);
					input.setFrequency(1);
					input.setPrefix(string.toLowerCase());
					input.setNext(words[i]);
					incrementInIndex(input);
				}
			}
		}
		System.out.println();
	}

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

			strings.add(string);
		}
		return strings;
	}

	private List<String> getPrefix(String[] words, int i) {
		List<String> list = new ArrayList<String>(4);

		if (i == 0)
			return Arrays.asList("");
		for (int j = 1; j <= 4 && i >= j; j++) {
			StringJoiner joiner = new StringJoiner("_");
			for (int k = i - j; k < i; k++) {
				joiner.add(words[k]);
			}
			list.add(joiner.toString());
		}

		return list;
	}

	private void incrementInIndex(Input input) throws IOException {
		Integer integer = inputFrequencyMap.get(input);
		if(integer == null){
			integer = 0;
		}
		inputFrequencyMap.put(input, integer+1);
	}

}
