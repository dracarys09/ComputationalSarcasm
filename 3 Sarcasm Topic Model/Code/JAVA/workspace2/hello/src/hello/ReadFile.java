package hello;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
	
	private String path  ;
	
	
	public ReadFile(String file_path){
		path=file_path ;
	}
	//count lines
	int countlines() throws IOException {
		FileReader fr = new FileReader(path)  ;
		BufferedReader bf = new BufferedReader(fr) ;
		String line ;
		int cline=0 ;
		while((line=bf.readLine())!=null){
			cline++ ;
		}
		bf.close();
		return cline ;
		
	}
	//open and read file
	public String[] OpenFile() throws IOException{
		FileReader fr = new FileReader(path)  ;
		BufferedReader textReader = new BufferedReader(fr) ;
		
		int nlines =countlines() ;
		String[] textData = new String[nlines] ;
		int i ;
		for(i=0;i<nlines;i++){
			textData[i] =textReader.readLine();
		}
		textReader.close(); 
		return textData ;
	}
	
}
