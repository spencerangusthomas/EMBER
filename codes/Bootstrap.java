/*
 The class calcualtes the bootstrap estimator 
 for the mean and standard error of a i.i.d data
 sets with any arbitrary undelying distribution
*/
import java.util.Random;
import java.util.Arrays;
import java.io.*;

public class Bootstrap
{
	/*
	 The standard deviation of the bootstrap distribution 
	 of means (from the boot strap samples)
	 is the estimator for the standard error in the mean. 
	*/
	public Bootstrap(){}
	
	double mean;
	double standardError;
	double standardDeviation;
	double variance;
	double median;
	int sampleSize;
		
	public void getBootstrap(int n, double[] data){
		
		int bootstrapNumber = n;
		double[] bootstrap = new double [bootstrapNumber];
		double bootstrapMean = 0.0;
		
			for (int z=0; z<bootstrapNumber; z++){
				double bootstrapTemp = 0.0;

				for (int k=0; k<data.length; k++){
					Random rand = new Random();
  	  	 	int randomNum = rand.nextInt( data.length );
  	  	 	bootstrapTemp += data[randomNum];
				}
				bootstrapTemp /= (double) data.length; // mean from each bootstrap sample
				bootstrap[z] = bootstrapTemp;
				bootstrapMean += bootstrapTemp;
		
			}	
		
			bootstrapMean /= (double) bootstrapNumber; // mean of all the bootstrap sample means
			
			double bootstrapStdErr = 0.0;
			for (double a : bootstrap){
				bootstrapStdErr += Math.pow( (a - bootstrapMean), 2 ); 
			}
			
			bootstrapStdErr = Math.sqrt( bootstrapStdErr ) / Math.sqrt( (double) (bootstrapNumber - 1)) ;
		
			double bootstrapSigma = bootstrapStdErr * Math.sqrt( (double) data.length );
			double bootstrapVariance = bootstrapSigma * bootstrapSigma;
			
			mean = bootstrapMean;
			standardError = bootstrapStdErr;
			standardDeviation = bootstrapSigma;
			variance = bootstrapVariance;
			sampleSize = data.length;
			
			Arrays.sort(data);
  		int mid = data.length/2;
  		if (data.length%2 == 1){
  			median = data[mid];
  		} else {
  			median = (data[mid-1]+data[mid])/2.;
  		}
	
			//return BootStatistic;
			
	} 	

	public void getBootstrap(int n, double[] data, boolean writeFile){
		
		//statistics Estimator = new statistics();
		if (writeFile==false)
			getBootstrap(n, data);
		else
			getBootstrapWithOutput(n, data);
	
		//return Estimator;
	}
	
	public void getBootstrapWithOutput(int n, double[] data){	
	
		int bootstrapNumber = n;
		double[] bootstrap = new double [bootstrapNumber];
		double bootstrapMean = 0.0;
				
  	try {
			File file = new File("bootstrap.txt");
			FileWriter fileWriter = new FileWriter(file,true);
			BufferedWriter bufferFileWriter  = new BufferedWriter(fileWriter);
	
			File file2 = new File("bootstrap2.txt");
			FileWriter fileWriter2 = new FileWriter(file2,true);
			BufferedWriter bufferFileWriter2  = new BufferedWriter(fileWriter2);
			for (int k=0; k<data.length; k++)
				fileWriter2.append(data[k] + "\n");
				fileWriter2.append("\n\n");
			bufferFileWriter2.close();
			
			for (int z=0; z<bootstrapNumber; z++){
				double bootstrapTemp = 0.0;

				for (int k=0; k<data.length; k++){
					Random rand = new Random();
  	  	 	int randomNum = rand.nextInt( data.length );
  	  	 	bootstrapTemp += data[randomNum];
				}
				bootstrapTemp /= (double) data.length; 
				bootstrap[z] = bootstrapTemp;
				bootstrapMean += bootstrapTemp;
			
				fileWriter.append(bootstrap[z] + "\n");
			}	
		
			bootstrapMean /= (double) bootstrapNumber;
			
			double bootstrapStdErr = 0.0;
			for (double a : bootstrap){
				bootstrapStdErr += Math.pow( (a - bootstrapMean), 2 ); 
			}
			
			bootstrapStdErr = Math.sqrt( bootstrapStdErr ) / Math.sqrt( (double) (bootstrapNumber - 1)) ;
		
			double bootstrapSigma = bootstrapStdErr * Math.sqrt( (double) data.length );
			double bootstrapVariance = bootstrapSigma * bootstrapSigma;
		

			mean = bootstrapMean;
			standardError = bootstrapStdErr;
			standardDeviation = bootstrapSigma;
			variance = bootstrapVariance;
			sampleSize = data.length;
	
					
			// write out all orders in columns and realisations as rows
				fileWriter.append("# number =\t");
				fileWriter.append(bootstrapNumber + "\n");	
				fileWriter.append("# mean =\t");
				fileWriter.append(bootstrapMean + "\n");	
				fileWriter.append("# stderr =\t");
				fileWriter.append(bootstrapStdErr + "\n");
			
				// two blank lines between data sets for gunplot indexing
				fileWriter.append("\n\n");				
				bufferFileWriter.close();

			//return BootStatistic;
		} catch(Exception ex) {
			System.out.println(ex);
			
		}
			
	} 
	
	///////////////////////////////////////////
	// same methods as above using Doubles to 
	// boxin/unboxing which can slow performance
	// require Doubles for generality eg Lists
	///////////////////////////////////////////
	
	public void getBootstrap(int n, Double[] data){
		
		int bootstrapNumber = n;
		double[] bootstrap = new double [bootstrapNumber];
		double bootstrapMean = 0.0;
		
			for (int z=0; z<bootstrapNumber; z++){
				double bootstrapTemp = 0.0;

				for (int k=0; k<data.length; k++){
					Random rand = new Random();
  	  	 	int randomNum = rand.nextInt( data.length );
  	  	 	bootstrapTemp += (double) data[randomNum]; // cast Double to double
				}
				bootstrapTemp /= (double) data.length; 
				bootstrap[z] = bootstrapTemp;
				bootstrapMean += bootstrapTemp;
		
			}	
		
			bootstrapMean /= (double) bootstrapNumber;
			
			double bootstrapStdErr = 0.0;
			for (double a : bootstrap){
				bootstrapStdErr += Math.pow( (a - bootstrapMean), 2 ); 
			}
			
			bootstrapStdErr = Math.sqrt( bootstrapStdErr ) / Math.sqrt( (double) (bootstrapNumber - 1)) ;
		
			double bootstrapSigma = bootstrapStdErr * Math.sqrt( (double) data.length );
			double bootstrapVariance = bootstrapSigma * bootstrapSigma;
			
	
			mean = bootstrapMean;
			standardError = bootstrapStdErr;
			standardDeviation = bootstrapSigma;
			variance = bootstrapVariance;
			sampleSize = data.length;
	
			//return BootStatistic;
			
	} 	

	public void getBootstrap(int n, Double[] data, boolean writeFile){
		
		//statistics Estimator = new statistics();
		if (writeFile==false)
			getBootstrap(n, data);
		else
			getBootstrapWithOutput(n, data);
	
		//return Estimator;
	}
	
	public void getBootstrapWithOutput(int n, Double[] data){	
	
		int bootstrapNumber = n;
		double[] bootstrap = new double [bootstrapNumber];
		double bootstrapMean = 0.0;
				
  	try {
			File file = new File("bootstrap.txt");
			FileWriter fileWriter = new FileWriter(file,true);
			BufferedWriter bufferFileWriter  = new BufferedWriter(fileWriter);
	
			
			for (int z=0; z<bootstrapNumber; z++){
				double bootstrapTemp = 0.0;

				for (int k=0; k<data.length; k++){
					Random rand = new Random();
  	  	 	int randomNum = rand.nextInt( data.length );
  	  	 	bootstrapTemp += (double) data[randomNum]; // cast Double to double
				}
				bootstrapTemp /= (double) data.length; 
				bootstrap[z] = bootstrapTemp;
				bootstrapMean += bootstrapTemp;
			
				fileWriter.append(bootstrap[z] + "\n");
			}	
		
			bootstrapMean /= (double) bootstrapNumber;
			
			double bootstrapStdErr = 0.0;
			for (double a : bootstrap){
				bootstrapStdErr += Math.pow( (a - bootstrapMean), 2 ); 
			}
			
			bootstrapStdErr = Math.sqrt( bootstrapStdErr ) / Math.sqrt( (double) (bootstrapNumber - 1)) ;
		
			double bootstrapSigma = bootstrapStdErr * Math.sqrt( (double) data.length );
			double bootstrapVariance = bootstrapSigma * bootstrapSigma;
	

			mean = bootstrapMean;
			standardError = bootstrapStdErr;
			standardDeviation = bootstrapSigma;
			variance = bootstrapVariance;
			sampleSize = data.length;
					
			// write out all orders in columns and realisations as rows
				fileWriter.append("# number =\t");
				fileWriter.append(bootstrapNumber + "\n");	
				fileWriter.append("# mean =\t");
				fileWriter.append(bootstrapMean + "\n");	
				fileWriter.append("# stderr =\t");
				fileWriter.append(bootstrapStdErr + "\n");
			
				// two blank lines between data sets for gunplot indexing
				fileWriter.append("\n\n");				
				bufferFileWriter.close();

			
		} catch(Exception ex) {
			System.out.println(ex);
		}
			
	} 
	
}