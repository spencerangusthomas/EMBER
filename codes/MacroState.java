import java.util.Arrays;
/*
	Statistics on a the Macroscopic state 
	Variables have methods to :
		set to initialise them to an externally calucalted value
		calculate them in this class
		get to return value (one of set or calcualted values)
	array variables have get methods for specific indeicies or the entire array
	
	
	TO DO : boubds check on setters
*/
public class MacroState{

	// varaibles
	private int size;				// F.length (number of orders/states)
	private int ensemble;   // F[0].length number of realisations
	private double[][] F; 	// fixed point -> F(u,mu) = Phi(u,mu)-u
	private double[] mean ;
	private double[] standardDeviation;
	private double[] standardError;
	private double[] variance;
	private double[] median;
	private double[] liftAverage;
	private Double[] solution;
	private double[] predictedX;
	private double[] X;
	private double norm;
	private double predictedMu;
	private double parameter;
	private double[][] Jacobian;		// Jacobian Info
	private double[][] EigenVector;
	private double[][] EigenValueMatrix;
	private double[] EigenValueReal;
	private double[] EigenValueImag;
	private Double determinantOfJ = null; // Double to distinguish null from 0
	
	// Constructor
	public MacroState(){
	}
	
	public MacroState(double[][] FF)
	{
		this.F = FF;
		this.size = F.length;
		this.ensemble = F[0].length;
		this.Jacobian = new double[size][size];
	}
	
	public MacroState deepCopy( MacroState M )
	{
		MacroState copyM = new MacroState();
		
		copyM.size = M.size;
		copyM.ensemble = M.ensemble;
		copyM.F = M.F;
		copyM.mean = M.mean;
		copyM.standardDeviation = M.standardDeviation;
		copyM.standardError = M.standardError;
		copyM.variance = M.variance;
		copyM.median = M.median;
		copyM.liftAverage = M.liftAverage;
		copyM.solution = M.solution;
		copyM.predictedX = M.predictedX;
		copyM.X = M.X;
		copyM.norm = M.norm;
		copyM.predictedMu = M.predictedMu;
		copyM.parameter = M.parameter;
		copyM.Jacobian = M.Jacobian;
		copyM.EigenVector = M.EigenVector;
		copyM.EigenValueMatrix = M.EigenValueMatrix;
		copyM.EigenValueReal = M.EigenValueReal;
		copyM.EigenValueImag = M.EigenValueImag;
		copyM.determinantOfJ = M.determinantOfJ;
		
		return copyM;
	}
	
	// Getter, Setter and Calculationc Methods
	public void setSize(int size)
	{
		this.size = size;
		this.mean = new double[size];
		this.standardDeviation = new double[size];
		this.standardError = new double[size];
		this.variance = new double[size];
		this.median = new double[size];
		this.liftAverage = new double[size];
		this.solution = new Double[size];
		this.predictedX = new double[size];
		this.X = new double[size];
	}
	
	public int getSize()
	{
		if (size != 0){
			return size; 
		} else {
			System.out.println("Array not defined, therefore cant define size");
			return 0;
		}
	}	
	
	public int getNumberOfStates()
	{
			return getSize(); 
	}	

	public int getEnsemble()
	{
		if (ensemble != 0){
			return ensemble; 
		} else {
			System.out.println("Array not defined, therefore cant define size");
			return 0;
		}
	}
	
	public int getNumberOfRealisations()
	{
			return getEnsemble(); 
	}	
	
	public int getNumberOfEnsemble()
	{
			return getEnsemble(); 
	}	

	///////////////////////////////
	// Ensemble
	///////////////////////////////
	public void setF(double[][] FF)
	{
		this.F = FF;
		this.size = F.length;  // number of orders
		this.ensemble = F[0].length; // number of realisations
	}
	
	public double[][] getF()
	{
		return F;
	}

	///////////////////////////////
	// Jacobian
	///////////////////////////////
	public void setJacobian(double[][] JJ)
	{
		this.Jacobian = JJ;
	}
	
	public double[][] getJacobian()
	{
		return Jacobian;
	}
	
	public double getJacobian(int i, int j)
	{
		return Jacobian[i][j];
	}
	
	public void setDeterminant(Double D)
	{
		this.determinantOfJ = D;
	}
	
	public double getDeterminant()
	{
		return determinantOfJ;
	}
	
	public void setEigenVector(double[][] EV)
	{
		this.EigenVector = EV;
	}
	
	public double[][] getEigenVector()
	{
		return EigenVector;
	}
	
	public double getEigenVector(int i, int j)
	{
		return EigenVector[i][j];
	}
	
	public void setEigenValueMatrix(double[][] EVM)
	{
		this.EigenValueMatrix = EVM;
	}
	
	public double[][] getEigenValueMatrix()
	{
		return EigenValueMatrix;
	}
	
	public double getEigenValueMatrix(int i, int j)
	{
		return EigenValueMatrix[i][j];
	}
	
	public void setEigenValueReal(double[] realEV)
	{
		this.EigenValueReal = realEV;
	}
	
	public double[] getEigenValueReal()
	{
		return EigenValueReal;
	}
	
	public double getEigenValueReal(int i)
	{
		if (i < size)
			return EigenValueReal[i];
		else {
			System.out.println("Index out of bounds! EigenValueReal");
			return 0;
		}
	}
	
	public void setEigenValueImag(double[] imagEV)
	{
		this.EigenValueImag = imagEV;
	}
	
	public double[] getEigenValueImag()
	{
		return EigenValueImag;
	}
	
	public double getEigenValueImag(int i)
	{
		if (i < size)
			return EigenValueImag[i];
		else {
			System.out.println("Index out of bounds! EigenValueImag");
			return 0;
		}
	}
	
	///////////////////////////////
	// Mean
	///////////////////////////////
	public void setMean(double m, int i)
	{
		this.mean[i] = m;
	}	
	
	public void setMean(double[] m)
	{
		this.mean = m;
	}
	
	public void calcMean(int i)
	{
		if (i < size){
			double sum = 0.0;
			int numOfValues = getSize();
			//for (double a : F[i]){
			for (int k=0; k<numOfValues; k++){
				sum += F[i][k];
			}
			mean[i] = sum/(double)numOfValues; 
		} else {
			System.out.println("Index out of bounds! calcMean");
		}
	}		
	
	public void calcMean()
	{
		for (int i=0; i<size; i++){
			double sum = 0.0;
			int numOfValues = getSize();
			//for (double a : F[i]){
			for (int k=0; k<numOfValues; k++){
				sum += F[i][k];
			}
			mean[i] = sum/(double)numOfValues; 
		}
	}		
	
	public double getMean(int i)
	{
		if (i < size)
			return mean[i]; 
		else {
			System.out.println("Index out of bounds! getMean");
			return 0;
		}
	}	
	
	public double[] getMean()
	{
			return mean; 
	}
	
	///////////////////////////////
	// Variance
	///////////////////////////////
	public void setVariance(double var, int i)
	{
		this.variance[i] = var;
	}	
	
	public void setVariance(double[] var)
	{
		this.variance = var;
	}
		
	public void calcVariance(int i){
		if (i < size){
			calcStandardDeviation(i);
			variance[i] = Math.pow( getStandardDeviation(i), 2 );	
		} else {
			System.out.println("Index out of bounds! calcVariance");
		}
	}
	
	public void calcVariance()
	{
		for (int i=0; i<size; i++){
			calcVariance(i);
		}
	}
	
	public double getVariance(int i)
	{
			if (i < size)
				return variance[i]; 
			else {
				System.out.println("Index out of bounds! getVariance");
				return 0;
			}
	}	
	
	public double[] getVariance()
	{
			return variance;
	}
	
	///////////////////////////////
	// Standard Deviation
	///////////////////////////////
	public void setStandardDeviation(double SD, int i)
	{
		this.standardDeviation[i] = SD;
	}	
	
	public void setStandardDeviation(double[] SD)
	{
		this.standardDeviation = SD;
	}
	
	public void calcStandardDeviation(int i){
		if (i < size){
			double meanValue = getMean(i);
			double temp = 0;
			for (double a : F[i])
				temp += Math.pow((meanValue-a),2);
			
			standardDeviation[i] = Math.sqrt( temp/(double)F[i].length );
		} else {
			System.out.println("Index out of bounds! calcStandardDeviation");
		}
	}	
	
	public void calcStandardDeviation()
	{
		for (int i=0; i<size; i++){
			calcStandardDeviation(i);	
		}
	}
	
	public double getStandardDeviation(int i)
	{
			if (i < size)
				return standardDeviation[i]; 
			else {
				System.out.println("Index out of bounds! getStandardDeviation");
				return 0;
			}
	}	
	
	public double[] getStandardDeviation()
	{
			return standardDeviation; 
	}
	
	///////////////////////////////
	// Standard Error
	///////////////////////////////	
	public void setStandardError(double SE, int i)
	{
		this.standardError[i] = SE;
	}	
	
	public void setStandardError(double[] SE)
	{
		this.standardError = SE;
	}
	 
	public void calcStandardError(int i){
		if (i < size){
			standardError[i] = getStandardDeviation(i) / Math.sqrt((double)F[i].length);
		} else {
			System.out.println("Index out of bounds! calcStandardError");
		}
	}
	
	public void calcStandardError()
	{
		for (int i=0; i<size; i++){
			calcStandardError(i);
		}
	}
	
	public double getStandardError(int i)
	{
			if (i < size)
				return standardError[i]; 
			else {
				System.out.println("Index out of bounds! getStandardError");
				return 0;
			}
	}	
	
	public double[] getStandardError()
	{
			return standardError; 
	}
	
	///////////////////////////////
	// Median
	///////////////////////////////
	public void setMedian(double med, int i)
	{
		this.median[i] = med;
	}	
	
	public void setMedian(double[] med)
	{
		this.median = med;
	}
			
	public void calcMedian(int i){
		if (i < size){
			Arrays.sort(F[i]); 
  		int mid = F[i].length/2;
  		if (F[i].length%2 == 1){
  			median[i] = F[i][mid];
  		} else {
  			median[i] =  ( F[i][mid-1]+F[i][mid] ) / 2;
  		}
		} else {
			System.out.println("Index out of bounds! calcMedian");
		}
	}
	
	public void calcMedian()
	{
		for (int i=0; i<size; i++){
			calcMedian(i);
		}
	}
		
	public double getMedian(int i)
	{
			if (i < size)
				return median[i]; 
			else {
				System.out.println("Index out of bounds! getMedian");
				return 0;
			}
	}	
	
	public double[] getMedian()
	{
			return median; 
	}
	
	///////////////////////////////
	// Predicted Solution
	///////////////////////////////
	public void setPredictedState(double x, int i)
	{
		this.predictedX[i] = x;
	}	
	
	public void setPredictedState(double[] x)
	{
		this.predictedX = x;
	}
	
	public double getPredictedState(int i)
	{
			if (i < size)
				return predictedX[i]; 
			else {
				System.out.println("Index out of bounds! getPredictedState");
				return 0;
			}
	}	
	
	public double[] getPredictedState()
	{
			return predictedX; 
	}

	public void setPredictedParameter(double mu)
	{
		this.predictedMu = mu;
	}	
	
	public double getPredictedParameter()
	{
			return predictedMu; 
	}	
	
	
	
	///////////////////////////////
	// Converged Solution
	///////////////////////////////
	public void setConvergedState(double x, int i)
	{
		this.X[i] = x;
	}	
	
	public void setConvergedState(double[] x)
	{
		this.X = x;
	}
	
	public double getConvergedState(int i)
	{
			if (i < size)
				return X[i]; 
			else {
				System.out.println("Index out of bounds! getConvergedState");
				return 0;
			}
	}	
	
	public double[] getConvergedState()
	{
			return X; 
	}

	public void setConvergedParameter(double mu)
	{
		this.parameter = mu;
	}	
	
	public double getConvergedParameter()
	{
			return parameter; 
	}	
	
	///////////////////////////////
	// Fixed Point Solution Values
	///////////////////////////////
	public void setSolution(Double sol, int i)
	{
		this.solution[i] = sol;
	}	
	
	public void setSolution(Double[] sol)
	{
		this.solution = sol;
	}
	
	public Double getSolution(int i)
	{
			if (i < size)
				return solution[i]; 
			else {
				System.out.println("Index out of bounds! getSolution");
				return null;
			}
	}	
	
	public Double[] getSolution()
	{
			return solution; 
	}
	
	///////////////////////////////
	// Lifted state from ensemble
	///////////////////////////////
	public void setLift(double lift, int i)
	{
		this.liftAverage[i] = lift;
	}	
	
	public void setLift(double[] lift)
	{
		this.liftAverage = lift;
	}
	
	public double getLift(int i)
	{
			if (i < size)
				return liftAverage[i]; 
			else {
				System.out.println("Index out of bounds! getLift");
				return 0;
			}
	}	
	
	public double[] getLift()
	{
			return liftAverage; 
	}	
	
	
	///////////////////////////////
	// Norm of State X
	///////////////////////////////
	public void setNorm(double n)
	{
		this.norm = n;
	}
	
	public double getNorm()
	{
			return norm; 
	}	
	
	
}