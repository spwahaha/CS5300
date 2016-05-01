package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;

public class ResultFormat {
	public static void main(String[] args) throws IOException{
		File[] files = new File("result/").listFiles();
		String templet = "The PageRank value for the two lowest-numbered Nodes in Block ";
		for(File file : files){
			System.out.println(file.getName());
			BufferedWriter bw = new BufferedWriter(new FileWriter("result/" + file.getName() + "formatted"));
			BufferedReader br = new BufferedReader(new FileReader(file));
			String l1 = br.readLine();
			String l2 = br.readLine();
			int cnt = 0;
			System.out.println(l1);
			System.out.println(l2);
			/**
			 * 
		The PageRank value for the two lowest-numbered Nodes in Block 67: 
		2.1890460137472094E-7 (Node ID: 675448), 4.8970941807479925E-6 (Node ID: 675449)
			 */
			while(l1 != null && l2 != null){
				StringTokenizer st = new StringTokenizer(l1, " ,\t");
				String nid1 = st.nextToken();
				String PR1 = st.nextToken();
				st = new StringTokenizer(l2, " ,\t");
				String nid2 = st.nextToken();
				String PR2 = st.nextToken();
				bw.write(templet + cnt + ":" + "\n");
				System.out.println("write");
				bw.flush();
				bw.write(PR1 + " " + "(Node ID: " + nid1 + "), " + PR2 + " " + "(Node ID: " + nid2 + ")\n");
				l1 = br.readLine();
				l2 = br.readLine();
				cnt++;
			}
			
			br.close();
			bw.close();
		}
		
	}
}
