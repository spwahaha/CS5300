package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class TokenizerTest {
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("input"));
		String line = br.readLine();
		StringTokenizer st = new StringTokenizer(line, " ,");
		while(st.hasMoreTokens()){
			System.out.println(st.nextToken());
		}
		br.close();
	}
}
