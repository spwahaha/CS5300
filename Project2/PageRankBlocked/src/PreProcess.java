
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PreProcess {
	static String path = "input";
	public static void main(String[] args) throws IOException{
//		filter();
//		preProcess();
//		construct();
		processBlock();
	}
	
	
	private static void processBlock() throws IOException {
		// TODO Auto-generated method stub
		String fileName = "/blocks.txt";
		BufferedReader br = new BufferedReader(new FileReader(path + fileName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/processedBlocks.txt"));
		String line = br.readLine();
		String res = "";
		while(line != null){
			line = line.trim();
			res += "," + line;
			line = br.readLine();
		}
		bw.write(res.substring(1));
		br.close();
		bw.close();
	}


	private static void construct() throws IOException{
		// TODO Auto-generated method stub
		String fileName = "/preProcessed.txt";
		int nodeNum = 685230;
		int cnt = 0;
		double weight = ((double)1) / nodeNum; 
		BufferedReader br = new BufferedReader(new FileReader(path + fileName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/input.txt"));
		String line = br.readLine();
		while(line != null){
			int src = Integer.parseInt(line.split(" ")[0]);
			while(src != cnt){
				bw.write("" + cnt + " " + weight+ "\n");
				bw.flush();
				cnt++;
			}
			bw.write(src + " " + weight + " " + line.split(" ")[1] +"\n");
			bw.flush();
			line = br.readLine();
			cnt++;
		}
		while(cnt < nodeNum){
			bw.write("" + cnt + " " + weight+ "\n");
			bw.flush();
			cnt++;
		}
		br.close();
		bw.close();

	}


	private static double id = 0.733;
	private static double rejectMin = 0.9 * id;
	private static double rejectLimit = rejectMin + 0.01;
	
	public static void filter() throws IOException{
		String fileName = "/edges.txt";
		BufferedReader br = new BufferedReader(new FileReader(path + fileName));
		String line = br.readLine();
		BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/filterd.txt"));
		while(line != null){
			double num = Double.parseDouble(line.substring(15));
			if(selectInputLine(num)){
				bw.write(line + "\n");
				bw.flush();
			}
			line = br.readLine();
		}
		br.close();
		bw.close();
	}
	
	private static boolean selectInputLine(double x){
		return ( ((x >= rejectMin) && (x < rejectLimit)) ? false : true );
	}
	
	private static void preProcess() throws IOException{
		String fileName = "/filterd.txt";
		int source = 0;
		System.out.println("preprocess");
		String neighbor = " ";
		BufferedReader br = new BufferedReader(new FileReader(path + fileName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/preProcessed.txt"));
		String line = br.readLine();
		while(line != null){
			int src = Integer.parseInt(line.substring(0, 6).trim());
			int des = Integer.parseInt(line.substring(6, 13).trim());
//			System.out.println(src +"       " + des);
			if(src == source){
				neighbor += des + ",";
			}else{
				bw.write(source + neighbor.substring(0, neighbor.length() - 1) + "\n");
				bw.flush();
				source = src;
				neighbor = " " + des + ",";
			}
			line = br.readLine();
		}
		br.close();
		bw.close();
	}
}
