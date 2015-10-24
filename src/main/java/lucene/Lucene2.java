/*package lucene;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

public class Lucene2 {

	public static void main(String[] args) throws IOException {

		RAMDirectory directory = new RAMDirectory();

		FileReader fr = new FileReader("text");
		StringBuilder builder = new StringBuilder();

		int c;
		while ((c = fr.read()) != -1) {
			builder.append((char) c);
		}
		fr.close();

		String data = builder.toString();

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
			System.out.println(string);
		}

		IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
		conf.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(directory, conf);

		for (String sentence : lines) {
			String[] words = sentence.split(" ");

			for (int i = 0; i < words.length; i++) {

				Document document = new Document();
				TextField profile = new TextField("profile", "GENERAL", Store.NO);
				TextField prefix = new TextField("prefix", getPrefix(words, i), Store.NO);
				TextField next = new TextField("next", words[i], Store.YES);
				TextField dataField = new TextField("data", getPrefix(words, i) + " " + words[i], Store.YES);
				SortedDocValuesField dataFieldDoc = new SortedDocValuesField("data",
						new BytesRef(getPrefix(words, i) + " " + words[i]));
				TextField user = new TextField("user", "ALL", Store.NO);

				document.add(profile);
				document.add(prefix);
				document.add(next);
				document.add(dataField);
				document.add(dataFieldDoc);
				document.add(user);

				System.out.println(dataField);

				writer.addDocument(document);

			}

		}

		writer.forceMerge(1);
		writer.close();

		DirectoryReader directoryReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
		// indexSearcher.setSimilarity(new SearchSimilarity());

		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {

			String input = scanner.nextLine();

			Builder queryBuilder = new Builder();

			queryBuilder.add(new TermQuery(new Term("profile", "GENERAL")), Occur.FILTER);
			queryBuilder.add(new TermQuery(new Term("user", "ALL")), Occur.FILTER);
			queryBuilder.add(new TermQuery(new Term("prefix", input)), Occur.MUST);

			BooleanQuery query = queryBuilder.build();

			System.out.println(query);

			GroupingSearch groupingSearch = new GroupingSearch("data");
			groupingSearch.setGroupSort(Sort.RELEVANCE);
			groupingSearch.setFillSortFields(true);
			groupingSearch.setAllGroups(true);

			groupingSearch.setCachingInMB(4.0, true);

			TopGroups<BytesRef> result = groupingSearch.search(indexSearcher, query, 0, 1000);
			System.out.println(result.totalGroupedHitCount);
			System.out.println(result.totalHitCount);
			System.out.println(result.totalGroupCount);
			System.out.println(result.groups);
			System.out.println(result.groups.length);
			GroupDocs<BytesRef>[] groups = result.groups;
			
			for (GroupDocs<BytesRef> group : groups) {
				System.out.println(group.totalHits);
				for (ScoreDoc scoredoc : group.scoreDocs) {
					Document doc = indexSearcher.doc(scoredoc.doc);
					System.out.println(doc);
				}
			}

			// Render groupsResult...
			// if (requiredTotalGroupCount) {
			// int totalGroupCount = result.totalGroupCount;
			// }

			// GroupingSearch groupingSearch = new GroupingSearch("data");
			// Sort groupSort = new Sort(new SortField("data", Type.STRING));
			// groupingSearch.setGroupSort(groupSort);
			// groupingSearch.setSortWithinGroup(groupSort);
			// TopGroups<Object> search2 = groupingSearch.search(indexSearcher,
			// query, 0, 15);
			//
			// GroupDocs<Object>[] groups = search2.groups;
			// for (GroupDocs<Object> groupDocs : groups) {
			// float score = groupDocs.score;
			// Object groupValue = groupDocs.groupValue;
			//
			// System.out.println(groupValue+ " : "+score);
			//
			// }
			//

			//
			// TopDocs search = indexSearcher.search(query, 15);
			//
			// ScoreDoc[] docs = search.scoreDocs;
			// for (ScoreDoc scoreDoc : docs) {
			// System.out.println("Next Word:
			// "+indexSearcher.doc(scoreDoc.doc).get("next"));
			// System.out.println("Explain:");
			// System.out.println(indexSearcher.explain(query, scoreDoc.doc));;
			// System.out.println();
			// }
			System.out.println("--------------------------------------------------------------------------");

		}

		// while (stream.incrementToken()) {
		// String termString = stream.getAttribute(CharTermAttribute.class)
		// .toString();
		//
		// BooleanQuery bq = new BooleanQuery(true);
		// TermQuery q1 = new TermQuery(new org.apache.lucene.index.Term(
		// "description", termString));
		// TermQuery q2 = new TermQuery(new org.apache.lucene.index.Term(
		// "nemodDescription", termString));
		//
		// bq.add(q1, BooleanClause.Occur.SHOULD);
		// bq.add(q2, BooleanClause.Occur.MUST);
		// query.add(bq, BooleanClause.Occur.MUST);
		//
		// }
		// stream.close();
		//
		//
		// Set<Object> children = new HashSet<Object>();
		// for (Object term : terms) {
		// children.addAll(((Term)term).getChildren());
		// }
		// terms.removeAll(children);
		// analyzer.close();

	}

	private static String getPrefix(String[] words, int i) {
		if (i == 0)
			return "";
		return words[i - 1];
	}
}
*/