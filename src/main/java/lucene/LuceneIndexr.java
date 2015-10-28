package lucene;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Future;

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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class LuceneIndexr {
	
	@Async
	public Future<Void> createIndex(Map<Input, Integer> inputFrequencyMap, List<String> words) throws IOException {
		
		Directory directory = new NIOFSDirectory(new File("lucene").toPath());
		
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
		return new AsyncResult<Void>(null);
	}

	private String pieces(String next) {
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = 1; i <= next.length(); i++) {
			joiner.add(next.substring(0, i).toLowerCase());
		}
		return joiner.toString();
	}


	
}
