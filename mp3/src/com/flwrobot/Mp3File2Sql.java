package com.flwrobot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Mp3File2Sql {
	private static Logger logger = Logger.getLogger(Mp3TestMp3agic.class); 
	
	//如果当前数据库为/e/x/s0089.mp3 则上面为 dir2= 'y'-1 tempName=0089 start =89 
	private int total = 0;
	private char dir1='f';//一级目录
	private char dir2='n'-1;//二级目录
	private String tempName="0085";
	private int start =85;

	private String dstFile;
	private StringBuffer sb = new StringBuffer(); //sql串
	
	/**
	 * 
	 * @param currUrl 格式/f/m/s0086.mp3
	 * @param mp3Path 格式 /home/music_data/heliang1/BaiduMusic
	 * @param dstFile
	 * @return
	 */
	public int convert(String currUrl,String mp3Path,String dstFile){
		logger.info("准备开始生成文件,目录: " + mp3Path);
		if(StringUtils.isBlank(currUrl)||StringUtils.isBlank(mp3Path)||StringUtils.isBlank(dstFile)){
			return total;
		}
		
		dir1 = currUrl.charAt(1);
		dir2 = currUrl.charAt(3);
		tempName = currUrl.split("/s")[1].split(".mp3")[0];
		start = Integer.parseInt(tempName);
		this.dstFile = dstFile;
		File f = new File(dstFile);
		if(null != f && f.exists() && f.isFile()){
			logger.info("当前文件已经存在，先删除 dstFile:" + dstFile);
			f.delete();
		}
		
		File dir = new File(mp3Path);
		convertAllFiles(dir);
		logger.info(total + " 生存完成,共生成记录 ： " + total  + " 条。");
		return total;
	}

	private void convertAllFiles(File dir) {
		if(null == dir){
			logger.info("文件或目录错误, dir: " + dir);
			return;
		}
		
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].isDirectory()) {
				try {
					sb.append("-- 新目录  " + fs[i].getAbsolutePath()+"\n");
					logger.info("-- 新目录  " + fs[i].getAbsolutePath() + " 添加数量 ： " +total);
					convertAllFiles(fs[i]);
				} catch (Exception e) {
				}
			}else{
				Map<String, Object> map = null;
				try {
					if(null != fs[i] &&fs[i].getName().endsWith(".mp3")){
						map = getMp3Attr2Map(fs[i]);
					}else{
						logger.info("非MP3格式文件 ： " + fs[i]);
						continue;
					}
				} catch (Exception e) {
					logger.info("歌曲文件异常 ： " + fs[i]);
					e.printStackTrace();
					continue;
				}
				if(total == 1){
					logger.info("开始生成  。。。 \n 当前生成数量 : " + total + "\n 当前：" +map.toString());
				}

				sb.append("INSERT INTO music (title, artist_name, url, source_file_path,duration) "
						+ "VALUES ("
						+ "\""
						+ map.get("title")
						+ "\", \""
						+ map.get("artist_name")
						+ "\", \""
						+ ((String) map.get("url")).replace("\\", "/")
						+ "\",\""
						+ ((String) map.get("source_file_path")).replace("\\", "\\\\")
						+ "\","
						+ map.get("duration") + ");\n");
			   
				if(total%500==0){
					writeFile(sb.toString());
					sb =  new StringBuffer();
					logger.info("当前生成数量 : " + total + "\n 末首 ：" +map.toString());
				}
				
				
			}
		}
		writeFile(sb.toString());
		sb =  new StringBuffer();
	}
	
	private Map<String, Object> getMp3Attr2Map(File f) throws UnsupportedTagException, InvalidDataException, IOException { //获取mp3文件的属性
		Mp3File mp3file = new Mp3File(f);

			Map<String, Object> map = new HashMap<String, Object>();
			total++;
			map.put("size", mp3file.getLength() / 1024.0 / 1024.0 + "Mb"); // 大小
			map.put("duration",
					mp3file.getLengthInMilliseconds()); // 时长
			map.put("source_file_path",f.getAbsolutePath());
			map.put("url", genUrl(total));
		
			if (mp3file.hasId3v1Tag()) {
				map.put("tagType", "ID3v1");
				process(map, mp3file.getId3v1Tag());
			} else if (mp3file.hasId3v2Tag()) {
				map.put("tagType", "ID3v2");
				process(map, mp3file.getId3v2Tag());
			}

			if (mp3file.hasId3v2Tag()) {
				process(map, mp3file.getId3v2Tag());
			}
			return map;
	}
	
	 private String genUrl(int total){
		 if((total + start -1)%200==0){
			 if(dir2!='z'){
				 dir2 ++;
			 }else{
				 dir2 = 'a';
				 dir1 ++;
			 }
			 tempName="0000";
		 }
		 return File.separator+ dir1+File.separator + dir2+ File.separator + "s"+ genName(tempName) + ".mp3";
	 }
	 
	 private String genName(String value){
		 int index=1;  
		 int n=Integer.parseInt(value.substring(index))+1;  
		 String newValue=String.valueOf(n);  
		 int len=value.length()-newValue.length()-index;  
		   
		 for(int i=0;i<len;i++){  
		 newValue="0"+newValue;  
		 }  
		 tempName = value.substring(0,index)+newValue;
		 return tempName;  
	 }
	 
		// 有v1则处理v1返回
		private Map<String, Object> process(Map<String, Object> map, ID3v1 v1) {
			if (v1 == null) {
				return null;
			}
			map.put("title", this.changeCharset(v1.getTitle()));
			map.put("artist_name", this.changeCharset(v1.getArtist()));
			map.put("album", this.changeCharset(v1.getAlbum()));// 唱片集
			map.put("year", this.changeCharset(v1.getYear()));// 唱片集
			map.put("genre", v1.getGenre());// 唱片集

			return map;
		}
		
		private void writeFile(String sql) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(new File(dstFile), true));
				writer.write(sql + "\n");

				writer.close();

			} catch (Exception e) {
				logger.error("写文件异常.",e);
			}finally{
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * 编码转换 去掉 ” \等特殊符号
		 * @param str
		 * @return
		 */
		public String changeCharset(String str) {
			if (null == str) {
				return "";
			}  
//			 try {
////			 logger.info(str + " 源码" + " 转码 ： " +new String(str.getBytes("iso8859-1"), "gbk") );
//			 return new String(str.getBytes("iso8859-1"), "gbk");
//			 } catch (UnsupportedEncodingException e) {
//			 e.printStackTrace();
//			 }
			if(str.length()>50){
				return str.replace("\"", "").replace("\\", "").substring(0, 50);
			}
			 return str.replace("\"", "").replace("\\", "");
		}
}
