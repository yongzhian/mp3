<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="com.flwrobot.Mp3File2Sql"%>
<%@ page import="java.io.File"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
	
	String curr_url = request.getParameter("curr_url");
	String mp3_path = request.getParameter("mp3_path");
  	String sqlFilePath = request.getServletContext().getRealPath("/sqlfile") ;
  	
	String mp3Path = "/home/music_data/heliang1/BaiduMusic";
	Mp3File2Sql mp3File2Sql = new Mp3File2Sql();
	File f = null;
	if(mp3File2Sql.convert(curr_url,mp3_path,sqlFilePath+File.separator+"songs.sql") != 0){
		f=  new File(sqlFilePath+File.separator+"songs.sql");
	}
	
%>
<!DOCTYPE html>
<html>
<head lang="en">
<meta charset="UTF-8">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta content="IE=edge" http-equiv="X-UA-Compatible">
<title>flw MP3生成sql</title>
<script charset="utf-8" type="text/javascript" src="js/jquery-1.12.1.js"></script>
<script type="text/javascript">
	function genSqlFile(){
		window.location.href = "index.jsp?curr_url="+$("#curr_url").val()+"&mp3_path="+$("#mp3_path").val();
	}
</script>

<body>
	MP3生成sql文件<br/>
	当前DB最后URL<input id="curr_url" size="20"> 实例：/f/m/s0086.mp3<br/>
	音乐文件目录<input id="mp3_path" size="20"> 注意文件名必须utf-8，且格式必须mp3<br/>
	<input type="button" onclick="genSqlFile()" value="生成"  ><br/>
	<%if(null != f && f.exists() && f.isFile()){	%>
	生存成功!
	生成结果 ：<%= sqlFilePath+File.separator+"songs.sql"%><br/>
	文件:<a href="sqlfile/songs.sql">songs.sql</a>
	<%}else{ %>
		生成失败或未执行生成操作!
	<%}%>
</body>
</html>