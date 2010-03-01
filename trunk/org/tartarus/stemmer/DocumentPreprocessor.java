package org.tartarus.stemmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DocumentPreprocessor {
	
	/* Taken from http://www.lextek.com/manuals/onix/stopwords1.html on 02/22/2010 */
	private static String[] STOP_WORDS = {"a","about","above","across","after","again","against","all","almost","alone","along","already","also","although","always","among","an","and","another","any","anybody","anyone","anything","anywhere","are","area","areas","around","as","ask","asked","asking","asks","at","away","b","back","backed","backing","backs","be","became","because","become","becomes","been","before","began","behind","being","beings","best","better","between","big","both","but","by","c","came","can","cannot","case","cases","certain","certainly","clear","clearly","come","could","d","did","differ","different","differently","do","does","done","down","down","downed","downing","downs","during","e","each","early","either","end","ended","ending","ends","enough","even","evenly","ever","every","everybody","everyone","everything","everywhere","f","face","faces","fact","facts","far","felt","few","find","finds","first","for","four","from","full","fully","further","furthered","furthering","furthers","g","gave","general","generally","get","gets","give","given","gives","go","going","good","goods","got","great","greater","greatest","group","grouped","grouping","groups","h","had","has","have","having","he","her","here","herself","high","high","high","higher","highest","him","himself","his","how","however","i","if","important","in","interest","interested","interesting","interests","into","is","it","its","itself","j","just","k","keep","keeps","kind","knew","know","known","knows","l","large","largely","last","later","latest","least","less","let","lets","like","likely","long","longer","longest","m","made","make","making","man","many","may","me","member","members","men","might","more","most","mostly","mr","mrs","much","must","my","myself","n","necessary","need","needed","needing","needs","never","new","new","newer","newest","next","no","nobody","non","noone","not","nothing","now","nowhere","number","numbers","o","of","off","often","old","older","oldest","on","once","one","only","open","opened","opening","opens","or","order","ordered","ordering","orders","other","others","our","out","over","p","part","parted","parting","parts","per","perhaps","place","places","point","pointed","pointing","points","possible","present","presented","presenting","presents","problem","problems","put","puts","q","quite","r","rather","really","right","right","room","rooms","s","said","same","saw","say","says","second","seconds","see","seem","seemed","seeming","seems","sees","several","shall","she","should","show","showed","showing","shows","side","sides","since","small","smaller","smallest","so","some","somebody","someone","something","somewhere","state","states","still","still","such","sure","t","take","taken","than","that","the","their","them","then","there","therefore","these","they","thing","things","think","thinks","this","those","though","thought","thoughts","three","through","thus","to","today","together","too","took","toward","turn","turned","turning","turns","two","u","under","until","up","upon","us","use","used","uses","v","very","w","want","wanted","wanting","wants","was","way","ways","we","well","wells","went","were","what","when","where","whether","which","while","who","whole","whose","why","will","with","within","without","work","worked","working","works","would","x","y","year","years","yet","you","young","younger","youngest","your","yours","z"};

	private String TOKEN_DELIMS = "\t \r\n";
	
	private String content;
	private HashMap<String,Integer> words;
	
	public DocumentPreprocessor(String mystr) throws IOException {
		content = mystr;
		words = new HashMap<String,Integer>();
		
		HashSet<String> stopwords = new HashSet<String>();
		int len = STOP_WORDS.length;
		for (int i=0; i<len; i++)
			stopwords.add(STOP_WORDS[i]);
		
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(content));
		tokenizer.resetSyntax();
		tokenizer.whitespaceChars((int)' ',(int)' ');
		tokenizer.whitespaceChars((int)'\t',(int)'\t');
		tokenizer.whitespaceChars((int)'\r',(int)'\r');
		tokenizer.whitespaceChars((int)'\n',(int)'\n');
		tokenizer.wordChars((int)'a',(int)'z');
		tokenizer.wordChars((int)'A',(int)'Z');
		tokenizer.lowerCaseMode(true);
		
		Stemmer s = new Stemmer();
		
		while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
			tokenizer.nextToken();
			while (tokenizer.sval == null && tokenizer.ttype != StreamTokenizer.TT_EOF)
				tokenizer.nextToken();
			if (tokenizer.ttype != StreamTokenizer.TT_EOF && !stopwords.contains(tokenizer.sval)) {
				s.add(tokenizer.sval);
				s.stem();
				if (words.containsKey(s.toString()))
					words.put(s.toString(), words.get(s.toString()) + 1);
				else
					words.put(s.toString(), 1);
			}
		}
	}
	
	public static String getContent(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String curline = br.readLine();
		
		StringWriter sw = new StringWriter();

		while (!curline.startsWith("UNCLASSIFIED")) {
			curline = br.readLine();
		}

		while (!curline.startsWith("copyright.")) {
			curline = br.readLine();
		}

		curline = br.readLine();
		while (!curline.equals("To access this product and its attachment(s), please visit OpenSource.gov and") &&
			   !curline.equals("As a broker of open source information, the OSC hosts material from other")) {
			sw.append(curline + "\n");
			curline = br.readLine();
		}

		return sw.toString();
	}
	
	public String tokenInfo() {
		ArrayList<String> keys = new ArrayList<String>(words.keySet());
		Collections.sort(keys);
		StringWriter sw = new StringWriter();
		int len = keys.size();
		for (int i=0; i<len; i++) {
			String s = keys.get(i) + "\t" + Integer.toString(words.get(keys.get(i))) + "\n";
			sw.append(s);
		}
		return sw.toString();
	}
	
	public void writeFile(String dir, int hashcode) throws IOException {
		ArrayList<String> keys = new ArrayList<String>(words.keySet());
		Collections.sort(keys);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir + Integer.toString(hashcode) + ".txt")));
		int len = keys.size();
		for (int i=0; i<len; i++) {
			int num = words.get(keys.get(i));
			for (int j=0; j<num; j++)
				bw.write(keys.get(i) + "\n");
		}
		bw.close();
	}
	
	public static HashMap<String,Integer> buildIndex(List<String> L) {
		int len = L.size();
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for (int i=0; i<len; i++) {
			map.put(L.get(i), i);
		}
		return map;
	}
	
	public static File[] filterFiles(File[] files) {
		int len = files.length;
		int count = 0;
		for (int i=0; i<len; i++) {
			String name = files[i].getName();
			String extension = name.substring(name.length()-3);
			if (extension.equals("txt"))
				count++;
		}
		File[] new_files = new File[count];
		count = 0;
		for (int i=0; i<len; i++) {
			String name = files[i].getName();
			String extension = name.substring(name.length()-3);
			if (extension.equals("txt"))
				new_files[count++] = files[i];
		}
		return new_files;
	}
	
	public static void createARFF(String dir) throws IOException {
		File f = new File(dir);
		File[] files = filterFiles(f.listFiles());
		int len = files.length;
		/* Create all words - each token is on its own line */
		HashSet<String> words = new HashSet<String>();
		BufferedReader br;
		String curline;
		for (int i=0; i<len; i++) {
			br = new BufferedReader(new FileReader(files[i]));
			curline = br.readLine();
			while (curline != null) {
				if (curline.length() > 0)
					words.add(curline);
				curline = br.readLine();
			}
			br.close();
		}
		Iterator<String> it_words = words.iterator();
		ArrayList<String> al_words = new ArrayList<String>();
		while (it_words.hasNext())
			al_words.add(it_words.next());
		Collections.sort(al_words);
		it_words = al_words.iterator();
		HashMap<String,Integer> index = buildIndex(al_words);
		/* We now have a sorted list of our words. We will need to do one more pass through all files */
		/* We first start by starting the ARFF file */
		FileWriter fw = new FileWriter(dir + "data.arff");
		fw.write("% ARFF file automatically generated by Java on " + (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")).format(new Date()));
		fw.write("\n");
		fw.write("@RELATION OSC\n");
		fw.write("\n");
		fw.write("@ATTRIBUTE id" + "\tNUMERIC\n");
		while (it_words.hasNext()) 
			fw.write("@ATTRIBUTE " + it_words.next() + "\tNUMERIC\n");
		fw.write("\n");
		fw.write("@DATA\n");
		String curword;
		int curword_count;
		String newword;
		String arff_string;
		for (int i=0; i<len; i++) {
			br = new BufferedReader(new FileReader(files[i]));
			arff_string = "{0 " + files[i].getName().substring(0,files[i].getName().length()-4);
			curword = br.readLine();
			curword_count = 1;
			while (curword != null) {
				newword = br.readLine();
				/* If we see the same word, just increment */
				if (newword == null)
					curword = null;
				else if (curword.equals(newword))
					curword_count++;
				else {
					arff_string += "," + Integer.toString(index.get(curword)) + " " + Integer.toString(curword_count);
					curword = newword;
					curword_count = 1;
				}
			}
			arff_string += "}";
			System.out.println(arff_string);
			fw.write(arff_string + "\n");
			br.close();
		}
		fw.close();
		
	}
	
	public static void main (String[] args) throws IOException {
		System.out.println(Integer.toString(STOP_WORDS.length));
		String dir = "C:\\Documents and Settings\\praff\\Desktop\\2009-2010\\OSC\\processed\\";
		createARFF(dir);
	} 

}
