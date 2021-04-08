/*
	A Class contining methods for predicting the next fixed point along the continuation curve
	Currently includes:
	1) Basic Secant based predictor, i.e projected gradient of previous two solutions 
	2) Least Squares (straight line) predictor, projected least squares fit of previous three points to obtain y=mx+c
	3) Least Squares (quadratic) predictor, as 2) but fits previous three data points to y = ax^2+bx+c, Currently this method produces poor results as uses ill-conditioned Matrix
*/

public class Predictor extends ContinuationParameters
{


	// constructor
	public Predictor(){};
	
	// variables 
	private boolean flag = true;				 // is third point ni LS method obtained using Secant method?
	private double  projectionStep = continuationStep; // form ContinuationParameters
	private double Norm;
	private double[] alpha;
	private double[] v0;
	private double[] v1;
	private double[] v;
	private double[] dv;	// difference between two vectors
	private int numOfOrders;

	// methods 
	
	////////////////////////////////////////////////////////
	// Poormans continuation for first few points
	////////////////////////////////////////////////////////
	public void poormans(MacroState b, String files[])
	{
		// takes 
		NewtonRaphson newtRaph = new NewtonRaphson();
   	FunctionType F  = new FixedPoint();
   	
		double tau = timeHorizon;
		double h = hFD;
		double[] xGuess = b.getPredictedState();
		double[] pGuess = param;
		pGuess[0] = b.getPredictedParameter();
		
		// use damped Newton to correct the predicted value
		//double[] x = 
		newtRaph.newton1D(F, xGuess, pGuess, h, maxIter, stepNum, newtonTol, b, files, timeHorizon); 
		// Newton1D calls FixedPoint (F) which sets converged values in b so do not need to set them here
    
	}
	
	////////////////////////////////////////////////////////
	// Secant
	////////////////////////////////////////////////////////
	public double[] Secant(MacroState[] b, int index, String[] files)
	{
	
		setVectors(b, index);	
		for (int i=0; i<numOfOrders; i++)
			v[i] = v1[i] + dv[i] / Norm * projectionStep;

		// update predictions 
		for (int i=0; i<numOfOrders-1; i++){
			b[index].setPredictedState( v[i], i ) ;
		}
		b[index].setPredictedParameter( v[numOfOrders-1] );
		
		/*	
		// Check direction of prediction
		double[][] yTemp = new double[numOfOrders][3*realisations];
		double[]   xTemp = new double[3*realisations];
		double[][][] yT2 = new double[3][numOfOrders][realisations];
		double[]     xT2 = new double[3];
		
		for (int j=0; j<2; j++){
			int indexTemp = index - 2 + j;
			yT2[j] = b[indexTemp].getF();
			xT2[j] = b[indexTemp].getConvergedParameter();
		}
	
		for (int k=0; k<numOfOrders; k++)		
			for (int i=0; i<realisations; i++)
				yT2[2][k][i] = v[k];
		xT2[2] = v[numOfOrders];
		
		for (int k=0; k<numOfOrders; k++){
			int counter=0;
			for (int j=0; j<3; j++){	
				for (int i=0; i<realisations; i++){
					yTemp[k][counter] = yT2[j][k][i];
					xTemp[counter] = xT2[j];
					counter++;
				}
			}
		}
		// returns {gradient , intercept, gradErrGrad, confInt};
		double [][] gradPredict = new double [numOfOrders][];
		for (int k=0; k<numOfOrders; k++){
			gradPredict[k] = newtonCheck(xTemp, yTemp[k]);
		}
		*/	

		return getPredictor();
	}
	
	////////////////////////////////////////////////////////
	// Least Squares
	////////////////////////////////////////////////////////
	public double[] LeastSquares(MacroState[] b, int index, String[] files, String type)
	{
	
		setVectors(b, index);	// reset vectors so dv, alpha based on new vector
		
		// start with oldest datum point
		// for each orderparameter
		double[][] ydata = new double[numOfOrders-1][numOfPreviousPoints];
		for (int i=0; i<numOfOrders-1; i++){
			for (int j=0; j<numOfPreviousPoints; j++){
				ydata[i][j] = b[index - (numOfPreviousPoints) + j ].getConvergedState(i); 
			}
		}
		
		// Least Sqaures fit predictor based on previous N points
		double[] xdata = new double [numOfPreviousPoints];
		for (int i=0; i<numOfPreviousPoints; i++){
			xdata[i] = b[ index-numOfPreviousPoints+i].getConvergedParameter();
		} 
			
		// x_i+i = x_i + ( x_i - x_i-2 ) to account for change in direction at the fold.
		// thismore robust than ( x_i - x_i-1 ) which can casuse kinks
		
		
		double projectedValue = b[index-1].getConvergedParameter() 
													 +  dv[dv.length-1] / Norm * projectionStep;
				 		   						 		   
		/*
		// 4/11/15 not sure how to generalise this to N points and get around the fold
		double[] w = new double [numOfPreviousPoints-1];
		double wSum = 0.0;
		for (int i=0; i<w.length; i++){
			w[i] = Math.exp((double) i / w.length);
			wSum += w[i];
		}
		double projectedValue = 0.0;
		for (int i=0; i<numOfPreviousPoints-1; i++)
			projectedValue += ( xdata[i+1] - xdata[i] ) * w[i]/wSum ;
		projectedValue /= (double) (numOfPreviousPoints-1);
		*/
		
		v = new double[numOfOrders];
		LeastSquaresFit ls = new LeastSquaresFit();
		
		for (int i=0; i<numOfOrders-1; i++){
		
			// linear
			if (type == "Straight" || type == "straight" || type == "S" || type == "s"){
				ls.setup(xdata, ydata[i]);
				ls.fitStraightLine();
													   
				v[i] = ls.gradient * projectedValue + ls.intercept;
											
			// quadratic														
			} else if (type == "Quadratic" || type == "quadratic" || type == "Q" || type == "q"){

				ls.setup(ydata[i], xdata); // parabola on lambda-axis (x-axis)
				ls.fitQuadraticLine();
				double aa = ls.a;
				double bb = ls.b;
				double cc = ls.c - projectedValue;
				double projectedX = ydata[i][2] + ( ydata[i][2] - ydata[i][1] );
				
				double temp = Math.pow(bb,2) - 4.0*aa*cc;
				
				if (temp > 0) 
					temp = Math.sqrt(temp);
			
				temp -= bb;
				double temp2 = -1.0*bb - temp; 
				temp /= 2.0*aa;
				temp2 /= 2.0*aa;
				double minTemp = Math.abs(b[index-1].getConvergedState(i) - temp); 
				
				if ( Math.abs(b[index-1].getConvergedState(i) - temp2) < minTemp )
					v[i] = temp2;
				else
					v[i] = temp;
											 
				v[i] = projectedX;
				v[numOfOrders-1] = aa * Math.pow(projectedX, 2) + bb * (projectedX) + cc;
		
	
			} else {
				System.out.println("Error: specify a valid type of linear least squares fitting");
				for (int k=0; k<numOfOrders; k++)
					v[k] = 0.0;
			}
		
		}
		v[numOfOrders-1] = projectedValue; // required for the secant method 
		
		
		// update predictions 
		for (int i=0; i<numOfOrders-1; i++){
			b[index].setPredictedState( v[i], i ) ;
		}
		b[index].setPredictedParameter( v[numOfOrders-1] );
		
		return getPredictor();
	}

	// common methods for secant and least squares predictors to set up vectors
	private void setVectors(MacroState[] b, int index)
	{	

		numOfOrders = b[index-1].getSize()+1;
		v  = new double [numOfOrders];
		v0 = new double [numOfOrders];
		v1 = new double [numOfOrders];
		dv = new double [numOfOrders];
	
		for (int i=0; i<numOfOrders-1; i++){
			v0[i] = b[index-2].getConvergedState(i);
			v1[i] = b[index-1].getConvergedState(i);
		}
		v0[numOfOrders-1] = b[index-2].getConvergedParameter();
		v1[numOfOrders-1] = b[index-1].getConvergedParameter();
		
		dv = getDifference(v1, v0);	
		Norm = Norm(dv, normNum);
		alpha 	= new double [numOfOrders];
	
		for (int i=0; i<numOfOrders; i++){	
			//v[i] = v1[i] + dv[i] / Norm * projectionStep;
			alpha[i] = dv[i] / Norm;
		}

	}

	// compute the difference between vectors
	private double[] getDifference(double[] a, double[] b)
	{	
		if (a.length != b.length)
			System.out.println("Warning: Vectors not the same length!");
			
		int min = Math.min(a.length, b.length);

		double[] dv = new double[min];
		for (int i=0; i<min; i++){
			dv[i] = a[i] - b[i];
		}
		
		return dv;
	}

	// method to return alpha for pseudo arclength equation
	public double[] getAlpha()
	{
		return alpha;
	}
	
	// method to return the size of vectors
	public int getSize()
	{
		return numOfOrders;
	}
	
	// method to return the predicted vector
	public double[] getPredictor()
	{
		return v;
	}
	
	// method to return the previous vector
	public double[] getPrevious()
	{
		return v1;
	}
	
	// method to return the predicted vector
	public double getPredictor(int index)
	{
		if (index > numOfOrders){
			System.out.println("Predictor index out of bounds!");
			return 0;
		} else {	
			return v[index];
		}
	}
	
	// method to return the previous vector
	public double getPrevious(int index)
	{
		if (index > numOfOrders-1){
			System.out.println("Predictor index out of bounds!");
			return 0;
		} else {	
			return v1[index];
		}
	}

	// computes norm of vector x
	private double Norm(double[] x, int normVal){
		double norm = 0.0;
		for (int i=0; i<x.length; i++){
			norm += Math.pow( x[i], normVal );
		}
		norm = Math.pow( norm, 1.0/normVal );
		return norm;
	}
	
	// computes norm of scalar x
	private double Norm(double x, int normVal){
		double norm = 0.0;
		norm += Math.pow( x, normVal );
		norm = Math.pow( norm, 1.0/normVal );
		return norm;
	}

	private double[] newtonCheck(double[] x, double[] y){
	// returns the gradient, intercept, gradient err, intercaept err and the 95% confidence interval on grad
	
		double sumX=0.0, sumXsquar=0.0, sumSquarX=0.0, sumY=0.0, sumXY=0.0;
		int numX = x.length, numY = y.length;
		if (numX != numY)
		{
			System.out.println("There is a different number of x ("+numX+") and y("+numY+") points!!");
			double[] output = {0.0, 0.0, 0.0, 0.0};
			return output;
		}
		
		for (int i=0; i<numX; i++)
		{
			sumX += x[i];
			sumSquarX += x[i]*x[i];
			sumY += y[i];
			sumXY += x[i] * y[i];
		}
		sumXsquar = sumX * sumX;
		
		// y = mx + c 
		double gradient 	= 0.0;		
		double intercept 	= 0.0;
		if ( numX*sumSquarX - sumXsquar != 0)
		{
			gradient 		= (numX*sumXY - sumX*sumY) / (numX*sumSquarX - sumXsquar);
			intercept 	= (sumY*sumSquarX - sumX*sumXY) / (numX*sumSquarX - sumXsquar);
		}
		double meanX = sumX / (double) numX;
		double meanY = sumY / (double) numY;
		double varY = 0.0;
		double varX = 0.0;
		
		for (int i=0; i<numX; i++)
		{
			varY = Math.pow(y[i] - meanY, 2);
			varX = Math.pow(x[i] - meanX, 2);
		}

		double gradErrGrad = Math.sqrt( varY/(numY-2.0) ) / Math.sqrt(varX);
		double confInt	 = 1.96 * gradErrGrad;

		double[] output = {	gradient , intercept, gradErrGrad, confInt};
	
		return output;
	}


}