package hello ;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class CallMyModel {
	
	private static String[] map_id_word ; //Vocubulary of words
	private static int W ; // length of vocabulary 
	private static double[] is_w ;// probability that the word is an issue word is=0
	private static HashMap<String, Integer> hm ; //hashmap for words and indexes
	private static int[][] positive,negative,sarcastic,w_di,tpositive,tnegative,tsarcastic,tw_di ; //Data
	private static int[] label ; //training label
	private static int Z =30,L=3,S=2,burnIn=50,samples=5,step=2 ; //constants
	
	
	private static int result_number=2118;
	
	private static int count = 17 ; 
	private static long count_w[] ; // count of each word
	private static double alpha_num , //priors
		alpha_den  ,
		beta_2_num ,
		beta_2_den  ,
		gamma_num ,
		gamma_den  ,
		delta_1_num  ,
		delta_1_den  ,
		delta_2_num , 
		delta_2_den ; 
	private static double[][] beta_1_num = {{20.0,1.0,10.5},{1.0,20.0,10.5}} ; //s l
	private static double[] beta_1_den = {21.0,21.0,21.0} ; ; //priors
	
	private static void test_fun(){  //method to test if the data is being loaded properly
		//System.out.println(W);
		//for(int i=0;i<10;i++){
			//System.out.println(map_id_word[i]);
			//System.out.println(is_w[i]);
		//}
		//System.out.println(map_id_word.length);
		//System.out.println(is_w.length);
		System.out.println(w_di.length);
		//System.out.println(label.length);
		
		
	}
	//main method, the magic happens here :)
	public static void main(String[] args){
		try{
			
			Create_vocab_switch() ; // Will fill values of vocab_size, map_id_word, is_w
		
			//Loading tweets and mapping each word to ID
			
			
			alpha_num = 1 ;
			alpha_den = Z ;
					
					beta_2_num = 100;
					beta_2_den = 100 ;
					gamma_num = 10.0d/(W*10.0d) ;
					gamma_den = 1.0d ;
					delta_1_num =10.0d/(W*10.0d) ;
					delta_1_den = 1 ;
					delta_2_num = 10; 
					delta_2_den = 10; 
				
			
			
			
			
			
			
			
			
			
			hm= new HashMap<String, Integer>() ;
			for (int i=0;i<map_id_word.length;i++){
				hm.put(map_id_word[i],i) ;
			}
			
			
			//System.out.println("hello");
			String fpath = "/home/development/prayas/topic-model-data/clean_data_old/positive.txt" ;
			create_count_words(fpath) ;
			
			String fpath2 = "/home/development/prayas/topic-model-data/clean_data_old/negative.txt" ; 
			create_count_words(fpath2) ;
			
			String fpath3 = "/home/development/prayas/topic-model-data/clean_data/sarcastic.txt" ; 
			create_count_words(fpath3) ;
			
		/*	WriteFile writer = new WriteFile("/home/development/prayas/topic-model-data/results/debug") ;
			
			for(int w=0;w<W;w++){
				
				if(  count_w[w]>0){
					String we = map_id_word[w] ;
					we = we+ " "+ String.valueOf(is_w[w]) + " " + String.valueOf(count_w[w]) ;
					writer.write(we)  ;
					
				}
				
			}
			writer.close() ;
			*/	
			
			//Take words that only occur 3 or more times
			// Remove tweets with less than 3 words
			
			
			positive = loadTweets(fpath) ;
			//positive = new int[0][0]; 
			//System.out.println("hello");
			
 			negative = loadTweets(fpath2) ;
			//negative = new int[0][0] ;
			
 			sarcastic = loadTweets(fpath3) ;
			
			Integer[] index = new Integer[positive.length + negative.length + sarcastic.length] ;
			for(int i=0;i<positive.length + negative.length + sarcastic.length;i++)
				index[i]=i ;
			Collections.shuffle(Arrays.asList(index)) ;
			//System.out.println("hello12");
			
			//System.out.println(positive.length);
		//	System.out.println(negative.length);
		//	System.out.println(sarcastic.length);
			
			w_di = new int[positive.length + negative.length + sarcastic.length][] ;
			
			label = new int[positive.length + negative.length + sarcastic.length] ;
			
			for(int i=0;i<index.length;i++){
				if(index[i]<positive.length){
					label[i] = 0 ;
					w_di[i] = new int[positive[index[i]].length] ;
					for(int j=0;j<w_di[i].length;j++){
						w_di[i][j]= positive[index[i]][j] ;
					}

				}else if(index[i]<(positive.length+negative.length)){
					label[i] =1 ;
					w_di[i] = new int[negative[index[i]-positive.length].length] ;
					for(int j=0;j<w_di[i].length;j++){
						w_di[i][j]= negative[index[i]-positive.length][j] ;
					}

				}else {
					label[i]=2 ;
					w_di[i] = new int[sarcastic[index[i]-positive.length-negative.length].length] ;
					for(int j=0;j<w_di[i].length;j++){
						w_di[i][j]= sarcastic[index[i] - positive.length - negative.length][j] ;
					}

				}
			}

			
			String tpath1="/home/development/prayas/topic-model-data/test_data2/" ;
			tpositive = loadTweets(tpath1+"positive.test.final");
			tnegative= loadTweets(tpath1+"negative.test.final") ;
			tsarcastic = loadTweets(tpath1+"sarcastic.test.final");
			//int length = tsarcastic.length/2  ;
			int length = 1000000 ;
			//tw_di = new int[tsarcastic.length+ Math.min(length, tpositive.length)+Math.min(length, tnegative.length)][] ;
			tw_di = new int[tsarcastic.length + tpositive.length+tnegative.length][];
			
			int[] tlabel= new int[tw_di.length] ;
			int[] y = new int[tw_di.length] ;
			for(int i=0;i<tw_di.length;i++){
				if(i<tsarcastic.length){
					tlabel[i]=2 ;
					tw_di[i] = new int[tsarcastic[i].length] ;
					
					for(int j=0;j<tsarcastic[i].length;j++){
						tw_di[i][j] = tsarcastic[i][j] ;
					}
				}else if(i<tsarcastic.length+Math.min(length, tpositive.length)){
					tlabel[i]=0 ;
					tw_di[i] = new int[tpositive[i-tsarcastic.length].length] ;
					
					for(int j=0;j<tpositive[i-tsarcastic.length].length;j++){
						tw_di[i][j] = tpositive[i-tsarcastic.length][j] ;
					}
				}else {
					tlabel[i]=1 ;
					tw_di[i] = new int[tnegative[i-tsarcastic.length-Math.min(length, tpositive.length)].length] ;
					
					for(int j=0;j<tnegative[i-tsarcastic.length-Math.min(length, tpositive.length)].length;j++){
						tw_di[i][j] = tnegative[i-tsarcastic.length-Math.min(length, tpositive.length)][j] ;
					}
				}
			}
			
//			WriteFile fulldata = new WriteFile("/home/development/prayas/topic-model-data/fullcorpus") ;
//			
//			for(int d=0;d<w_di.length;d++){
//				String w = "" ;
//				for(int i=0;i<w_di[d].length;i++){
//					w=w+map_id_word[w_di[d][i]]+" " ;
//				}
//				w=w+"\t" ;
//				w=w+String.valueOf(label[d]) ;
//				fulldata.write(w) ;
//				
//			}
			
			mymodel model = new mymodel(hm,tw_di,tlabel) ;
			
			System.out.println(String.valueOf(tpositive.length)+" "+ String.valueOf(tnegative.length)+" "+String.valueOf(tsarcastic.length)) ; 
			
			model.estimate(label,w_di,is_w,map_id_word,W,w_di.length,Z,L,S,burnIn,samples,step,result_number,count,alpha_num
					,alpha_den,beta_1_num, beta_1_den , beta_2_num , beta_2_den , gamma_num, gamma_den,
				delta_1_num, delta_1_den , delta_2_num ,delta_2_den) ;
			
			/*WriteFile writetrain = new WriteFile("/home/development/prayas/topic-model-data/test_data2/testjava")  ;
			for(int i=0;i<tpositive.length;i++){
				String w="" ;
				w="0" ;
				w=w+"\t" ;
				for(int j=0;j<tpositive[i].length;j++){
					w=w+map_id_word[tpositive[i][j]]+" " ; 
				}
				writetrain.write(w) ;
			}
			for(int i=0;i<tnegative.length;i++){
				String w="" ;
				w="1" ;
				w=w+"\t" ;
				for(int j=0;j<tnegative[i].length;j++){
					w=w+map_id_word[tnegative[i][j]]+" " ; 
				}
				writetrain.write(w) ;
			}
			for(int i=0;i<tsarcastic.length;i++){
				String w="" ;
				w="2" ;
				w=w+"\t" ;
				for(int j=0;j<tsarcastic[i].length;j++){
					w=w+map_id_word[tsarcastic[i][j]]+" " ; 
				}
				writetrain.write(w) ;
			}
			writetrain.close() ;
			*/
			System.out.println("All done\n");
			
			
			
			
			int tp=0,fp=0,tn=0,fn=0;
			for(int i=0;i<tw_di.length;i++){
				y[i]= model.predict(w_di[i]) ;
				if(y[i]==tlabel[i] && tlabel[i]==2)
					tp++ ;
				else if(y[i]==tlabel[i] && tlabel[i]!=2){
					tn++ ;
				}else if(y[i]!=tlabel[i] && y[i]==2){
					fp++ ;
				}else{
					fn++ ;
				}
			}  
			
			//model.predict(positive[0]) ;
			System.out.println("Precision\n") ;
			System.out.println((double)tp/(tp+fp)) ;
			
			System.out.println("Recall\n") ;
			System.out.println((double)tp/(tp+fn)) ;
			
			
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	
	
	
	
	
	
	
	//loads the count  of each word in the vocabulary
	private static void create_count_words(String fpath1 ){
		try{
		
			ReadFile f1 ;
			f1= new ReadFile(fpath1) ;
			String[] fdata = f1.OpenFile() ;
			
			for(int i=0;i<fdata.length;i++){
				String[] words = fdata[i].split(" ") ;
				for(int j=0;j<words.length;j++){
					String w = words[j] ;
					if(hm.containsKey(w)){
						count_w[hm.get(w)]++   ;
					}
				
				}
			}
			
			
			
			
			
		}catch( IOException e){
			System.out.println("Load Tweets");
			System.out.println(e.getMessage());
			
			
		}
		
	}
	//loads the tweets
	private static int[][] loadTweets(String fpath) {
		try{
			ReadFile file = new ReadFile(fpath) ;
			String[] fdata = file.OpenFile() ;
			int[][] tweets = new int[fdata.length][] ;
			int[][] tweets_mapped = new int[fdata.length][] ;
			int ci=0 ;
			int counts=0, count =0 ;
			
			for(int i=0;i<fdata.length;i++){
				String[] words = fdata[i].split(" ") ;
				tweets[i]= new int[words.length] ;
				count=0;
				counts=0 ;
				
				for(int j=0;j<words.length;j++){
					String w = words[j] ;
					if(hm.containsKey(w)){
						int gw = hm.get(w) ; // tweet is the mapping from word to int
						if(count_w[gw]>=3)
							tweets[i][count++] = gw ;
						if(is_w[gw]>=0.98)
							counts++ ;
					}
				}
				if( count>2 ){  // making sure the no. of words (mapped) are greater than 3,reducing dimension of tweets
					tweets_mapped[ci]= new int[count] ;
					//System.out.println(count);
					for(int j=0;j<count;j++){
						//System.out.println(String.valueOf(i)+" "+String.valueOf(j)+" "+String.valueOf(ci));
						tweets_mapped[ci][j]= tweets[i][j] ;
					}
					ci++ ;
				}
				}
			int[][] tweets_mapped2 = new int[ci][] ; // reducing no of tweets dimension
			for(int i=0;i<ci;i++){
				tweets_mapped2[i] = new int[tweets_mapped[i].length] ;
				for(int j=0;j<tweets_mapped[i].length;j++){
					tweets_mapped2[i][j] =tweets_mapped[i][j] ;
				}
			}
				
				
			
			return tweets_mapped2 ;
		}catch( IOException e){
			System.out.println("Load Tweets");
			System.out.println(e.getMessage());
			
		}
		return null ;

	}

	//loads the distribution for is variable
	private static  void Create_vocab_switch() throws IOException{
		String fpath = "/home/development/prayas/topic-model-data/support/switch-probabilities.txt" ; //path to switch probab
		ReadFile file =new ReadFile(fpath) ;
		String[] fdata = file.OpenFile()  ;
		W=fdata.length-1 ;
		map_id_word = new String[W] ;
		is_w = new double[W] ;
		for(int i=1;i<W+1;i++){
			String[] splitted= fdata[i].split("\t") ;
			map_id_word[i-1]=splitted[0] ;
			is_w[i-1] = Double.parseDouble(splitted[1]) ;
		}
		count_w=new long[W]  ;
		
	}
	
	
	
	
	
	
}
