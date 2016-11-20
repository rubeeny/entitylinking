package com.entitylinking.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 文件索引操作
 * @author HP
 *
 */
public class IndexFile {

	public static void main(String args[]){
		String indexDir1 = "./index/synonymsIndex";
		String indexDir2 = "./index/ambiguationIndex";
		String indexDir3 = "./index/entityRelationIndex";
		
		String filePath1 = "./dict/synonymsDict.txt";
		String filePath2 = "./dict/ambiguationDict.txt";
		String filePath3 = "./dict/entityRelation.txt";
		
		String[] fields1 = new String[]{"synonymsDictKey","synonymsDictValue"}; 
		String[] fields2 = new String[]{"ambiguationDictKey","ambiguationDictValue"}; 
		String[] fields3 = new String[]{"entityRelationKey","entityCount","entityRelationValue"}; 
		
		creatIndex(filePath1, indexDir1, fields1);
		creatIndex(filePath2, indexDir2, fields2);
		creatIndex(filePath3, indexDir3, fields3);
	}
	
	/**
	 * 为指定文件创建索引
	 * @param filePath
	 * @param indexDir
	 * @param fields
	 */
	public static void creatIndex(String filePath, String indexDir, String[] fields){
		IndexWriter indexWriter = null;
		try {
			//1. 创建目录
			Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(indexDir));
			//2. 创建IndexWriter
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
			indexWriter = new IndexWriter(directory, indexWriterConfig);
			//3. 创建文件对象
			File file  = new File(filePath);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line ;
			while((line = bReader.readLine())!= null){
				//4. 创建Document对象
				Document document = new Document();
				String[] lineArray = line.split("\t\\|\\|\t");
				if(lineArray.length == fields.length){
					for(int i=0;i<lineArray.length;i++){
						document.add(new Field(fields[i], lineArray[i], 
								TextField.TYPE_STORED));
					}
					//5. 将文档添加到indexWriter对象中
					indexWriter.addDocument(document);
				}
			}
			bReader.close();
			indexWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 通过索引查询关键词所对应的document
	 * @param queryString
	 * @param queryField
	 */
	public static Document queryDocument(String queryString, String queryField, String indexDir){
		try {
			//1. 获取索引文件目录
			Directory directory = FSDirectory.open(Paths.get(indexDir));
			//2. 创建IndexReader对象，读取索引文件
			IndexReader indexReader = DirectoryReader.open(directory);
			//3. 创建索引查询器，查询索引文件
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			//4. 实例化分析器
			Analyzer analyzer = new StandardAnalyzer();
			//5. 创建查询解析器，解析query
			QueryParser queryParser = new QueryParser(queryField, analyzer);
			//6. 解析查询字符串获取查询对象
			Query query = queryParser.parse(queryString);
			TopDocs topDocs = indexSearcher.search(query,1);
			//7. 处理查询结果
			if(topDocs != null){
				return indexSearcher.doc(topDocs.scoreDocs[0].doc);
			}
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}