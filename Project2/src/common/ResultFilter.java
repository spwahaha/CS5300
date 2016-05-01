package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.StringTokenizer;

public class ResultFilter {
	public static void main(String[] args) throws IOException{
		System.out.println("x");
		int[] blocks = {10328,10045,10256,10016,9817,10379,9750,9527,10379,10004,10066,10378,10054,9575,10379,10379,9822,10360,10111,10379,10379,10379,9831,10285,10060,10211,10061,10263,9782,9788,10327,10152,10361,9780,9982,10284,10307,10318,10375,9783,9905,10130,9960,9782,9796,10113,9798,9854,9918,9784,10379,10379,10199,10379,10379,10379,10379,10379,9981,9782,9781,10300,9792,9782,9782,9862,9782,9782};
		HashSet<Integer> set = new HashSet<Integer>();
		int sum = 0;
		for(int blk : blocks){
			set.add(sum);
			set.add(sum + 1);
			sum += blk;
		}
		
		String[] inputs = {"/home/zhenchuan/Downloads/blked", "/home/zhenchuan/Downloads/RandomBlk",
					"/home/zhenchuan/Downloads/Gauss", "/home/zhenchuan/workspace/CS5300/Project2/output/stage21"};
		
		for(int i = 0; i < inputs.length; i++){
			String input = inputs[i];
			System.out.println(input);
			File[] files = new File(input + "/").listFiles();
			BufferedWriter bw = new BufferedWriter(new FileWriter(input + "/result"));
			ArrayList<OutNode> res = new ArrayList<OutNode>();
			for(File file : files){
				if(file.getName().contains("part")){
					System.out.println("use this file:  " + file.getName());
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line = br.readLine();
					while(line != null){
						String out = "";
						StringTokenizer st = new StringTokenizer(line, " ,\t");
						int nodeId = Integer.parseInt(st.nextToken());
						String PR = st.nextToken();
						if(set.contains(nodeId)){
							res.add(new OutNode(nodeId, PR));
						}
						line = br.readLine();
					}
				}
				
			}
//			Arrays.sort(res, new Comparator<OutNode>(){
//				public int compare(OutNode n1, OutNode n2){
//					return n1.nodeId.compareTo(n2.nodeId);
//				}
//			});
			Collections.sort(res, new Comparator<OutNode>(){
				public int compare(OutNode n1, OutNode n2){
					return n1.nodeId.compareTo(n2.nodeId);
				}
			});
			for(OutNode node : res){
				bw.write(node.toString());
				bw.flush();
			}
			bw.close();
		}
	}
}
