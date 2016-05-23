package com.flwrobot.test;

import com.flwrobot.Mp3File2Sql;

public class Mp3File2SqlTest {
public static void main(String[] args) {
	Mp3File2Sql m = new Mp3File2Sql();
	String currUrl = "/f/m/s0086.mp3";
	char dir1 = currUrl.charAt(1);
	char dir2 = (char) (currUrl.charAt(3)-1);
	String tempName = currUrl.split("/s")[1].split(".mp3")[0];
	int start = Integer.parseInt(tempName);
	System.out.println(dir1);
	System.out.println(dir2);
	System.out.println(tempName);
	System.out.println(start);
	
	
	System.out.println("res " + m.changeCharset("1242462"));

}
}
