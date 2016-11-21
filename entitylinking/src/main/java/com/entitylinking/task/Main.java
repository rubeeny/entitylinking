package com.entitylinking.task;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.entitylinking.linking.GenerateEntityGraph;
import com.entitylinking.linking.LinkingKB;
import com.entitylinking.linking.bean.PathBean;
import com.entitylinking.linking.bean.RELRWParameterBean;
import com.entitylinking.linking.bean.Text;
import com.entitylinking.utils.FileUtils;
import com.entitylinking.utils.Parameters;

/**
 * 实体链接主程序
 * @author HP
 *
 */
public class Main {

	static Logger logger = Logger.getLogger(Main.class);
	static{
		PropertyConfigurator.configure("./log4j.properties");
	}
	private GenerateEntityGraph generateEntityGraph;
	public static void main(String args[]) {
		/**
		 *1.加载文件
		 *2.获得候选实体
		 *3.构造密度子图并初始化 
		 *4.使用随机游走计算语义签名 
		 *5.计算全局score并获得目标实体 
		 */
		Main main = new Main();
		main.init();
		main.linkingMainProcess();
		
	}
	
	/**
	 * entity-linking主流程
	 * @return
	 */
	public Map<String, String> linkingMainProcess(){
		File fileDir = new File(RELRWParameterBean.getSourceFileDirPath());
		if(fileDir.isDirectory()){
			File[] fileList = fileDir.listFiles();
			String textContent;
			for(File file:fileList){
				String filePath = file.getAbsolutePath();
				Text text = new Text();
				text.setTextName(file.getName());
				textContent = FileUtils.readFileContent(filePath);
				text.setContent(textContent);
				//生成所有实体的密度子图
				generateEntityGraph.generateDensityGraph(text);
				//链接知识库过程
				LinkingKB linkingKB = new LinkingKB();
				Map<String, String> mentionEntityPairs = linkingKB.obtainmentionEntityPairs(text);
				StringBuilder sBuilder = new StringBuilder();
				for(Entry<String, String> entry :mentionEntityPairs.entrySet()){
					sBuilder.delete(0, sBuilder.length());
					sBuilder.append(entry.getKey()).append("\t").append(entry.getValue());
					logger.info(sBuilder.toString());
				}
			}
		}
		return null;
	}
	
	/**
	 * 初始化工作
	 */
	public void init(){
		generateEntityGraph = new GenerateEntityGraph();
		Parameters parameters = new Parameters();
		parameters.loadPath("./xml/path.xml");
		parameters.loadRELParameters(PathBean.getRelParameterPath());
		parameters.loadDictFromXML();
	}
}
