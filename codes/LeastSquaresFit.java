public class LeastSquaresFit
{
	// variables
	// generic
	public double[] x;
	public double[] y;
	int numX; // number of elements in x
	int numY; // number of elements in y
	
	// striaght line m*x + c 
	public double gradient;
	public double intercept;
	public double gradientError;
	public double interceptError;
	public double confInterval95;
	double sumX=0.0; 			// sum (x) 
	double sumXsquar=0.0; // ( sum (x) )^2
	double sumSquarX=0.0; // sum (x^2)
	double sumY=0.0;			// sum (y)
	double sumXY=0.0; 		// sum (xy)
	double meanX;
	double meanY;
	double varY;
	double varX;
	
	// quadratic a*x^2 + b*x + c
	// also uses sumXY, sumX, sumSquarX, sumY
	double a;
	double b;
	double c;
	
	// constructor
	public LeastSquaresFit(){};
	public LeastSquaresFit(double[] x, double[] y)
	{
		this.x = x;
		this.y = y;
	}
	
	// methods
	public void setup (double[] x, double[] y)
	{
		this.x = x;
		this.y = y;
	}
	
	// fit y = m*x + c
	public void fitStraightLine(){
		numX = x.length;
		numY = y.length;
		
		if (numX != numY)
		{
			System.out.println("ERROR: There is a different number of x ("+numX+") and y("+numY+") points!!");
		}
		
		for (int i=0; i<numX; i++)
		{
			sumX += x[i];
			sumSquarX += x[i]*x[i];
			sumY += y[i];
			sumXY += x[i] * y[i];
			//System.out.println("$ y[i] = " + y[i]);
		}
		sumXsquar = sumX * sumX;
		
		// y = mx + c 
		gradient 	= 0.0;		
		intercept = 0.0;
		if ( numX*sumSquarX - sumXsquar != 0)
		{
			gradient 		= (numX*sumXY - sumX*sumY) / (numX*sumSquarX - sumXsquar);
			intercept 	= (sumY*sumSquarX - sumX*sumXY) / (numX*sumSquarX - sumXsquar);
			//System.out.println( "% " + sumX + " " + sumSquarX + " " + sumY + " " + sumXY + " " + sumXsquar);
		}
		meanX = sumX / (double) numX;
		meanY = sumY / (double) numY;
		varY = 0.0;
		varX = 0.0;
		
		for (int i=0; i<numX; i++)
		{
			varY = Math.pow(y[i] - meanY, 2);
			varX = Math.pow(x[i] - meanX, 2);
		}

		gradientError 	= Math.sqrt( varY/(numY-2.0) ) / Math.sqrt(varX);
		confInterval95	= 1.96 * gradientError;

	}
	
	// fit a*x^2 + b*x + c
	public void fitQuadraticLine(){
		numX = x.length;
		numY = y.length;

		// set up linear equations to be solved
		// M v = k 
		double[][] M = new double[numX][numX]; 
		double[] v = new double[numX]; // coefficients of quadratic
		double[] k = new double[numX];
	
		// temporary values linear equations
		double sumQuarX=0.0; 		// sum (x^4)
		double sumCubeX=0.0; 		// sum (x^3)
		double sumYSquarX=0.0; 	// sum (y*x^2)
		
		for (int i=0; i<numX; i++)
		{
	//	System.out.println ( y[i] + " " + x[i] );
			sumX += x[i];
			sumSquarX += Math.pow(x[i], 2.0);;
			sumY += y[i];
			sumXY += x[i] * y[i];
			
			sumCubeX += Math.pow(x[i], 3.0);
			sumQuarX += Math.pow(x[i], 4.0);
			sumYSquarX += y[i] * Math.pow(x[i], 2.0);
		}
		//System.out.println("\n");
		
	
		// set up matrix M
		M[0][0] = sumQuarX;
		M[0][1] = sumCubeX;
		M[1][0] = sumCubeX;
		M[2][0] = sumSquarX;
		M[1][1] = sumSquarX;
		M[0][2] = sumSquarX;
		M[1][2] = sumX;
		M[2][1] = sumX;
		M[2][2] = (double) numX;
		
		// set up vector k
		k[0] = sumYSquarX;
		k[1] = sumXY;
		k[2] = sumY;
	
		// set up coefficient values
		v[0] = 0.0;
		v[1] = 0.0;
		v[2] = 0.0;
		
		// solve linear system with Gaussian Elimination
		GaussianElimination gauss = new GaussianElimination();


		System.out.println(" Matrix = ");
		for (int i=0; i<numX; i++){
		for (int j=0; j<numX; j++)
		{
			System.out.print(M[i][j] + " " );
		}
		System.out.println(" ");
		}
		
		System.out.println(" vector = ");
		for (int i=0; i<numX; i++)
		{
			System.out.println(k[i] );
		}
		System.out.println(" ");
	
		
		v = gauss.lsolve(M, k);


		System.out.println(" solution = ");
		for (int i=0; i<numX; i++)
		{
			System.out.println( v[i] );
		}
		System.out.println("\n");

		// assign to coefficients
		a = v[0];
		b = v[1];
		c = v[2];
		
		// could also change this to GMRES when it is working 15/10/15
	}
	
}