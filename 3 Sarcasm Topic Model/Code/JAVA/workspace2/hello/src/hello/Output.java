package hello;

import hello.mymodel.ArrayIndexComparator;
import hello.mymodel.ArrayIndexComparator2;
import hello.mymodel.ArrayIndexComparator3;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Output {
	public int iteration ;
	public static int Z; // number of topics
	public int W; // vocabulary
	public static int D; // number of documents
	private static int L ; // labels
	private static int S ; // number of sentiment

	private String[] map_id_word ; //mapping bw word and id
	public double nprecision ; 
	public double nrecall ;
	
	public int[][] w_di ;
	public int[] label ;
	public int[][] sentiment ;
	
	
	public double[][] P_w_z ; // Probabilities ex P(w/z)
	public double[][][] P_w_zs ; // P(w/zs)
	
	public double[][] P_z_l ; //P(z/l)
	
	public double[] P_z , P_l ; 
	public double[][] P_s_l ; //P(s/l)
	public double[][][] P_s_zl ; //P(s/zl)
	
	String path_result_prefix = "/home/development/prayas/topic-model-data/results/result-" ; 
	

	
	public double precision,recall, fscore ;
	
	//constructor that loads the data and the iteration
	//This method is used to store the data of each iteration, separately
	public Output(int[][] w_di,int[] label, int[][] sentiment,int iteration,int W,int D,int L, int S, int Z,String[] map_id_word,double[][] P_w_z,
			double[][][] P_w_zs,double[][] P_z_l,double[] P_z,double[] P_l,double[][] P_s_l , double[][][] P_s_zl,
					double precision,double recall,double nprecision, double nrecall,int suffix){
		this.iteration=iteration ;
		this.D=D ;
		this.L = L;
		this.S=S ;
		this.W=W ;
		this.Z=Z ;
		this.P_z = P_z ;
		this.P_l=P_l ;
		this.map_id_word = map_id_word ;
		this.P_w_z = P_w_z ;
		//System.out.println(P_w_z.length) ;
		this.P_w_zs=P_w_zs ;
		
		this.P_z_l = P_z_l ;
		this.P_s_l = P_s_l  ;
		this.P_s_zl = P_s_zl ;
		this.label= label ;
		this.w_di = w_di;
		this.sentiment = sentiment ;
		
		this.precision=precision ;
		this.recall=recall;
		this.nprecision = nprecision ;
		this.nrecall = nrecall ;
		fscore = (2*precision*recall)/(precision+recall) ;
		get_words(10, Integer.parseInt(String.valueOf(suffix)+String.valueOf(iteration)));
		getSentimentWords(5, Integer.parseInt(String.valueOf(suffix)+String.valueOf(iteration)));
		getPz_l(Integer.parseInt(String.valueOf(suffix)+String.valueOf(iteration)));
		printP_s_l(Integer.parseInt(String.valueOf(suffix)+String.valueOf(iteration))) ;
		printP_s_zl(Integer.parseInt(String.valueOf(suffix)+String.valueOf(iteration))) ;
		print_scatter(Integer.parseInt(String.valueOf(suffix)+String.valueOf(iteration)));
	}
	
	//prints the count of sentiment words for histogram
	public void print_scatter(int fsuffix){
		WriteFile scatter = new WriteFile("/home/development/prayas/topic-model-data/results/scatter/scatter-"+String.valueOf(fsuffix)) ;
		for(int d=0;d<w_di.length;d++){
			String res=String.valueOf(label[d]) ;
			int s0=0,s1=0;
			for(int i=0;i<w_di[d].length;i++){
				if(sentiment[d][i]==1)
					s1++;
				else if(sentiment[d][i]==0)
					s0++ ;
			}
			res=res+" "+String.valueOf(s0)+" "+String.valueOf(s1)+" "+String.valueOf(w_di[d].length) ;
			//System.out.println(res);
			try {
				scatter.write(res);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//gets top topic words and writes to the file
	public void get_words(int count,int fsuffix){
		String wordpath="/home/development/prayas/topic-model-data/results/words/words-"+String.valueOf(fsuffix) ;
		WriteFile wordwrite = new WriteFile(wordpath) ;
		for(int z=0;z<Z;z++){
			ArrayIndexComparator compare = new ArrayIndexComparator(P_w_z,z,W) ;
			Integer[] indexes = compare.createIndexArray() ;
			//System.out.println(Arrays.toString(indexes));
			Arrays.sort(indexes,compare) ;
			//System.out.println(Arrays.toString(indexes));
			String w =String.valueOf(z+1)+" " ;
			for(int c=0;c<count;c++){
				 w+= map_id_word[indexes[c]] ;
				 w+=" " ;
			}
			try {
				wordwrite.write(w) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			wordwrite.write("\n---------\n") ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wordwrite.close() ;
	}	
		//gets top sentiment words and writes to the file
	public void getSentimentWords(int count,int fsuffix){
		
		//P_w_zs
		
		String wordpath="/home/development/prayas/topic-model-data/results/words/words-"+String.valueOf(fsuffix) ;
		WriteFile wordwrite = new WriteFile(wordpath) ;
		
		
		for(int z=0;z<Z;z++){
			for(int s=0;s<S;s++){
				ArrayIndexComparator2 compare = new ArrayIndexComparator2(P_w_zs,z,s,W) ;
				Integer[] indexes = compare.createIndexArray() ;
				Arrays.sort(indexes,compare) ;
				String w =String.valueOf(z+1)+" " ;
				String pw = String.valueOf(z+1)+ " " ;
				for(int c=0;c<count;c++){
					 w+= map_id_word[indexes[c]] ;
					 w+=" " ;
					 pw+= String.valueOf(P_w_zs[indexes[c]][z][s])+" " ;
				}
				try {
					wordwrite.write(w) ;
			//		wordwrite.write(pw) ;
					//wordwrite.write("\n") ;
				} catch (IOException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			
			try {
					
					wordwrite.write("\n") ;
				} catch (IOException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
		}
		
		try {
			wordwrite.write("\n---Sentiment words for each topic----\n") ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wordwrite.close() ;

	}
	
	//computes and prints the probab P(z/l)
	public void getPz_l(int fsuffix){
		
		String wordpath="/home/development/prayas/topic-model-data/results/words/words-"+String.valueOf(fsuffix) ;
		WriteFile wordwrite = new WriteFile(wordpath) ;
		for(int z=0;z<Z;z++){
				String w=" " ;	
				w= String.valueOf(z+1)+ " Happy= " ;
				w+= String.valueOf(P_z_l[z][0]) ;
				w+= " Sad= " ;
				w+= String.valueOf(P_z_l[z][1]) ;
				w+= " Sarcasm= " ;
				w+= String.valueOf(P_z_l[z][2]) ;
				try {
					wordwrite.write(w) ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
		}
		
		try {
			wordwrite.write("\n") ;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(int l= 0 ;l<L;l++){
			double[] prob = new double[Z] ;
			for(int z=0;z<Z;z++){
				prob[z]=P_z_l[z][l]; 
			}
			
			ArrayIndexComparator3 compare = new ArrayIndexComparator3(prob) ;
			Integer[] indexes = compare.createIndexArray() ;
			Arrays.sort(indexes,compare) ;
			for(int i=0;i<indexes.length;i++){
				indexes[i]=indexes[i]+1 ;
			}
			
			try {
			
				
			if(l==0)
				
					wordwrite.write("Happy: "+Arrays.toString(indexes)) ;
				
			if(l==1)
				wordwrite.write("Sad: "+Arrays.toString(indexes)) ;
			if(l==2)
				wordwrite.write("Sarcasm: "+Arrays.toString(indexes)) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		
		}
		
		//P_l_z
		double[][] p_l_z = new double[L][Z];
		
		
		for(int z=0;z<Z;z++){
			for(int l=0;l<L;l++){
				p_l_z[l][z] = (P_z_l[z][l]*P_l[l])/P_z[z] ;
			
			}
			try {
				wordwrite.write(String.valueOf(z+1)+ " " + "Happy "+ String.valueOf(p_l_z[0][z])+" "+"Sad "
								+ String.valueOf(p_l_z[1][z])+" Sarcasm "+String.valueOf(p_l_z[2][z])) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		for(int l= 0 ;l<L;l++){
			double[] prob = new double[Z] ;
			for(int z=0;z<Z;z++){
				prob[z]=p_l_z[l][z]; 
			}
			
			ArrayIndexComparator3 compare = new ArrayIndexComparator3(prob) ;
			Integer[] indexes = compare.createIndexArray() ;
			Arrays.sort(indexes,compare) ;
			for(int i=0;i<indexes.length;i++){
				indexes[i]=indexes[i]+1 ;
			}
			
			try {
			
				
			if(l==0)
				
					wordwrite.write("Happy: "+Arrays.toString(indexes)) ;
				
			if(l==1)
				wordwrite.write("Sad: "+Arrays.toString(indexes)) ;
			if(l==2)
				wordwrite.write("Sarcasm: "+Arrays.toString(indexes)) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		
		}
		
		
		
		
		
		
		try {
			wordwrite.write("\n---Probabilities topic----\n") ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wordwrite.close() ;
	
	
	}
	//prints p(s/l)
	public void printP_s_l(int fsuffix){
		
		WriteFile writer = new WriteFile("/home/development/prayas/topic-model-data/results/words/words-"+String.valueOf(fsuffix)) ;
		try {
			writer.write("P_s_l") ;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(int l=0;l<L;l++){
			String w = "" ;
			for(int s=0;s<S;s++)
				w+=String.valueOf(P_s_l[s][l])+" ";
			try {
				writer.write(w) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		writer.close();
	}
	//prints p(s/zl)
	public void printP_s_zl(int fsuffix){
		WriteFile writer = new WriteFile("/home/development/prayas/topic-model-data/results/words/words-"+String.valueOf(fsuffix)) ;
		try {
			writer.write("P_s_zl") ;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		for(int l=0;l<L;l++){
			for(int s=0;s<S;s++){
				String w = "" ;
				for(int z=0;z<Z;z++)
					w+=String.valueOf(P_s_zl[s][z][l])+" ";
				try {
					writer.write(w) ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		writer.close();
	}
	//Helper classes
	
	public class ArrayIndexComparator3 implements Comparator<Integer>
	{	
    		private final double[] array;
		
    		public ArrayIndexComparator3(double[] array)
    		{
    			
        		this.array = array;
    		}		

    		public Integer[] createIndexArray()
    		{
        		Integer[] indexes = new Integer[array.length];
        		for (int i = 0; i < array.length; i++)
        		{
            			indexes[i] = i; // Autoboxing
        		}
        		return indexes;
    		}

    		@Override
    		public int compare(Integer index1, Integer index2)
    		{
         // Autounbox from Integer to int to use as array indexes
        //reversed them -prayas to get reverse sorted
    			return Double.compare(array[index2], array[index1]) ;
        		
    		}
	}
	public class ArrayIndexComparator implements Comparator<Integer>
	{	
    		private final double[][] array;
		private final int z  ;
		private final int W ;
    		public ArrayIndexComparator(double[][] array,int z,int W)
    		{
    			this.z = z; 
        		this.array = array;
        		this.W=W ;
    		}		

    		public Integer[] createIndexArray()
    		{
        		Integer[] indexes = new Integer[W];
        		for (int i = 0; i < W; i++)
        		{
            			indexes[i] = i; // Autoboxing
        		}
        		return indexes;
    		}

    		@Override
    		public int compare(Integer index1, Integer index2)
    		{
         // Autounbox from Integer to int to use as array indexes
        //reversed them -prayas to get reverse sorted
    			return Double.compare(array[index2][z], array[index1][z]) ;
        		
    		}
    		
    		public void getSentimentWords(int count,int fsuffix){
    			
    			//P_w_zs
    			
    			String wordpath="/home/development/prayas/topic-model-data/results/words/words-"+String.valueOf(fsuffix) ;
    			WriteFile wordwrite = new WriteFile(wordpath) ;
    			
    			
    			for(int z=0;z<Z;z++){
    				for(int s=0;s<S;s++){
    					ArrayIndexComparator2 compare = new ArrayIndexComparator2(P_w_zs,z,s,W) ;
    					Integer[] indexes = compare.createIndexArray() ;
    					Arrays.sort(indexes,compare) ;
    					String w =String.valueOf(z+1)+" " ;
    					String pw = String.valueOf(z+1)+ " " ;
    					for(int c=0;c<count;c++){
    						 w+= map_id_word[indexes[c]] ;
    						 w+=" " ;
    						 pw+= String.valueOf(P_w_zs[indexes[c]][z][s])+" " ;
    					}
    					try {
    						wordwrite.write(w) ;
    				//		wordwrite.write(pw) ;
    						//wordwrite.write("\n") ;
    					} catch (IOException e) {
    					// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				
    				}
    				
    				try {
    						
    						wordwrite.write("\n") ;
    					} catch (IOException e) {
    					// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				
    				
    			}
    			
    			try {
    				wordwrite.write("\n---Sentiment words for each topic----\n") ;
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
    			
    			wordwrite.close() ;

    		}

	}	
	public class ArrayIndexComparator2 implements Comparator<Integer>
	{	
    		private final double[][][] array;
		private final int z,s  ;
    		public ArrayIndexComparator2(double[][][] array,int z,int s,int W)
    		{
    			this.z = z; 
    			this.s= s ;
        		this.array = array;
    		}		

    		public Integer[] createIndexArray()
    		{
        		Integer[] indexes = new Integer[W];
        		for (int i = 0; i < W; i++)
        		{
            			indexes[i] = i; // Autoboxing
        		}
        		return indexes;
    		}

    		@Override
    		public int compare(Integer index1, Integer index2)
    		{
         // Autounbox from Integer to int to use as array indexes
        //reversed them -prayas to get reverse sorted
    			return Double.compare(array[index2][z][s], array[index1][z][s]) ;
        		
    		}
	}
}
