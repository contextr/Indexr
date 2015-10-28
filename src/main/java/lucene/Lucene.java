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

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
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
import com.fasterxml.jackson.databind.ObjectWriter;

@SpringBootApplication
@ComponentScan(basePackages = "lucene")
@EnableAsync
public class Lucene implements CommandLineRunner {

	Map<Input, Integer> inputFrequencyMap;
	List<String> words;
	
	private Directory directory;

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
		directory = new NIOFSDirectory(new File("lucene").toPath());
		inputFrequencyMap = new HashMap<>();
		words = new LinkedList<>();
		
		readAndExtract("physics", "Physics");
		readAndExtract("history", "History");
		addDictionary("words");
		
/*		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writerWithDefaultPrettyPrinter = mapper.writerWithDefaultPrettyPrinter();
		writerWithDefaultPrettyPrinter.writeValue(new File("json"),inputFrequencyMap);
*/		
		
		createIndex();
		
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
	private void readAndExtract(String fileName, String profile) throws IOException, JsonParseException, JsonMappingException {
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
			System.out.println((float) curr / size);
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
	}

	private void createIndex() throws IOException {
		IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
		conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(directory, conf);
		inputFrequencyMap.forEach((input, freq) -> {
			
			TextField profile = new TextField("profile", input.getProfile(), Store.NO);
			TextField prefix = new TextField("prefix", input.getPrefix(), Store.NO);
			TextField next = new TextField("next", input.getNext(), Store.YES);
			TextField nextPieces = new TextField("nextPieces", pieces(input.getNext()), Store.NO);
			TextField user = new TextField("user", String.valueOf(input.getUid()), Store.NO);
			
//			NumericDocValuesField frequencyDoc = new NumericDocValuesField("freq", freq);
			IntField frequency = new IntField("frequency", freq, Store.YES);
			
			Document document = new Document();
			document.add(profile);
			document.add(prefix);
			document.add(next);
			document.add(nextPieces);
			document.add(user);
			document.add(frequency);
//			document.add(frequencyDoc);
			prefix.setBoost(input.getPrefix().split("_").length*freq);
			
			try {
				writer.addDocument(document);
			} catch (Exception e) {
				try {
					writer.close();
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
				throw new RuntimeException(e);
			}
		});
		
		words.stream()
		.sorted((o1,o2) -> o1.length()-o2.length())
		.forEach(w -> {
			TextField profile = new TextField("profile", "Dictionary", Store.NO);
			TextField prefix = new TextField("prefix", "", Store.NO);
			TextField next = new TextField("next", w, Store.YES);
			TextField nextPieces = new TextField("nextPieces", pieces(w), Store.NO);
			TextField user = new TextField("user", String.valueOf(1), Store.NO);
			
//			NumericDocValuesField frequencyDoc = new NumericDocValuesField("freq", freq);
			IntField frequency = new IntField("frequency", 1, Store.YES);
			
			Document document = new Document();
			document.add(profile);
			document.add(prefix);
			document.add(next);
			document.add(nextPieces);
			document.add(user);
			document.add(frequency);
//			document.add(frequencyDoc);
			
			try {
				writer.addDocument(document);
			} catch (Exception e) {
				try {
					writer.close();
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
				throw new RuntimeException(e);
			}
		});
		writer.commit();
		writer.close();
	}

	private String pieces(String next) {
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = 1; i <= next.length(); i++) {
			joiner.add(next.substring(0, i).toLowerCase());
		}
		return joiner.toString();
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
		System.out.println("Done with extraction");
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
