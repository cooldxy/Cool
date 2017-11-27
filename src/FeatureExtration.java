

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.international.pennchinese.ChineseTreebankLanguagePack;

public class FeatureExtration {
	
	static String grammar = "edu/stanford/nlp/models/lexparser/chinesePCFG.ser.gz";
	static String[] options = {"-MAX_ITEMS","200000000" };
	static LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);

	
	/**
	 * 提取句子中的属性词（名词和名词短语）
	 * 
	 * @param sentence
	 * @return
	 */
	public ArrayList<String> extraFeature(String sentence) {
		//Stanford Parser
		TreebankLanguagePack tlp = new ChineseTreebankLanguagePack();
		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
		List<? extends HasWord> sentList = toke.tokenize();
		Tree parse = lp.apply(sentList);            //整棵句法树
		List<Tree> leaves = parse.getLeaves();		//句法树的叶节点（所有的分词）

		Iterator<Tree> it = leaves.iterator();		
		
		ArrayList<String> feature = new ArrayList<String>();	//存储提取的属性
		
		while (it.hasNext()) {							  //遍历叶节点
			Tree leaf = it.next();
			Tree start = leaf;
			start = start.parent(parse);                  //叶节点的父节点    
			
			String tag = start.value().toString().trim();
		
			if (tag.equals("NN")) {                       //是名词的情况
				start = start.parent(parse);
				tag = start.value().toString().trim();
				if (tag.equals("NP")) {					//倒数第三层节点是NP（名词或名词短语）
					List<Tree> children =  start.getChildrenAsList();         //该节点所有孩子节点
					if(children.size() > 1) {            			//孩子数量大于1的情况，即有可能是名词短语的情况
						String s = "";
						String s1 = "";
						for (Tree t : children) {
							s1 = t.getChild(0).value().toString().trim();
							s = s + s1;								//名词短语
							if(!feature.contains(s1))
								feature.add(s1);					//组成名词短语的单个名词存入feature中
						}
						if(!feature.contains(s))
							feature.add(s);							//名词短语存入feature中
					} else {
						feature.add(children.get(0).getChild(0).value().toString().trim());   //单个名词存入feature中
					}					
				}

			}
		}
		return feature;
	}
	

	public static void main(String[] args) throws Exception {
		Comment c = new Comment();
		FeatureExtration fe = new FeatureExtration();
		ArrayList<String> comment = new ArrayList<String>();
		comment = c.getComment(100);	
		
		ArrayList<String> featureSum = new ArrayList<String>();
		ArrayList<ArrayList<String>> featureList = new ArrayList<ArrayList<String>>();
		int num = 0;
		for (String sentence : comment) {
			ArrayList<String> subSen = c.splitSentence(sentence);
			ArrayList<String> feature = new ArrayList<String>();
			for (String sen : subSen) {
				feature.addAll(fe.extraFeature(sen));
			}
			featureSum.addAll(feature);
			featureList.add(feature);
			System.out.println(++num + ":" +feature);
		}
		System.out.println(featureSum);
		System.out.println(featureList);
	}
	
}