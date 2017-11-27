

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class Comment {
	
	// 定义接口CLibrary，继承自com.sun.jna.Library
			public interface CLibrary extends Library {
				// 定义并初始化接口的静态变量
				CLibrary Instance = (CLibrary) Native.loadLibrary("路径....",
						CLibrary.class);

				public int NLPIR_Init(String sDataPath, int encoding, String sLicenceCode);

				public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

				public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit, boolean bWeightOut);

				public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit, boolean bWeightOut);

				public int NLPIR_AddUserWord(String sWord);// add by qp 2008.11.10

				public int NLPIR_DelUsrWord(String sWord);// add by qp 2008.11.10

				public String NLPIR_GetLastErrorMsg();

				public void NLPIR_Exit();
			}
	
	/**
	 * 从数据库中获取评论(分好词的)
	 * 
	 * @param result
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getComment(int num) throws Exception {
		// 数据库配置
		String url = "....";
		String user = "root";
		String password = ".....";
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(url, user, password);
		Statement stmt = conn.createStatement();

		ArrayList<String> splitWords = new ArrayList<String>(); // 存放正文的分词结果

		// 获取正文(分好词的)
		String sql = "";
		ResultSet rs = stmt.executeQuery(sql);
		int i = 0;
		while (i < num) {
			rs.next();
			splitWords.add((String) rs.getObject(""));
			i++;
		}
		// 关闭数据库
		stmt.close();
		conn.close();

		return splitWords;
	}
		
	/**
	 * 词性标注
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public String wordsAnnotated(String content) throws Exception {
		String argu = "路径.....";
		// String system_charset = "GBK";//GBK----0
//		String system_charset = "UTF-8";
		int charset_type = 1;

		int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");
		String nativeBytes = null;

		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is " + nativeBytes);
			return null;
		}
		nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(content, 1);
		CLibrary.Instance.NLPIR_Exit();
		return nativeBytes;
	}
	
	/**
	 * 将句子按标点符号分开
	 * 
	 * @param sentence
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> splitSentence(String sentence) throws Exception {
		ArrayList<String> list = new ArrayList<String>();	
		String[] splitSentence = null;
		String wordsAnnotated = wordsAnnotated(sentence);  	//进行词性标注
		wordsAnnotated = wordsAnnotated.trim();
		if (wordsAnnotated.contains("/w")) {          	//句子中含有标点符号的情况
			splitSentence = wordsAnnotated.split("/w.{0,2}");        //将句子按标点符号进行分割
		} else {               //句子中不含标点符号，直接存入list中                 
			list.add(sentence);                         
			return list;
		}		
		for (String s : splitSentence) {     //去掉分句中的词性标注记号和标点符号以及空格
			s = s.replaceAll("/.*?  ", "");
			s = s.substring(0, s.length() - 2);
			s = s.trim();
			if(!s.equals(""))				//若去掉以上这些字符串为空，则不加到list中
				list.add(s);
		}
		return list;
	}

}
