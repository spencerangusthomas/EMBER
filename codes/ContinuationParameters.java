// parameters used in continuation
class ContinuationParameters {
	// ================================
	//continuation parameters 
	// ================================
	static int realisations 			= 100, 	// number of microscopic simualtions to run
		maxIter = 10,		// Max Newton Iterations 
		stepNum = 10, 	// Steps in Euler ODE solver 
		NumContSteps = 50,	// number of continaution steps 
		normNum = 2;	// type of norm (1=abs, 2=Euclidean) 

	static double timeHorizon = 0.1,	// time evolution (tau)
		dt = 0.1 , 	// step size in the Euler solver
		secantTol = 1.0e-3 , 	// tolerance for Newton Corrector 
		newtonTol = 1.0e-3 , 	// tolerance for initial fixed points 
		lambda = 1.0 , 	// initial value in damped Newton corrector	x += lambda*h 
		continuationStep = 0.1,	// Step size for continuation (ds) 
		hFD = continuationStep/2.0 , 	// stepsize for finite differencing 
		contStepInitial = -continuationStep ; 	// Initial ds, sign determines direction of continuation 
	 
		static boolean DAMP = false ; 	// Use damped Newton Method in the Predictor-Corrector?
		static boolean BranchSwitching = false ; 	// search for other branhces at bifurcation points after continuation? 
		static int RANGE = 5 ; 	// multiple of ds or stdErr for corrector convergence window	
		static int NUMOFRESTARTS = 0 ; 	// number of restarts to use in the corrector method if outside of convergence window 
		static int STATISTIC = 1 ; 	// 0=Gaussian Approximation, 1=bootstrap estimate
		static int numOfPreviousPoints = 3;			// number of points used in predition method (Secant only needs 2 but can leave as 3)
		static String predictorMethod = "DLS"; // type of predictor, Secant or LeastSquares (default=secant)

 
	// ================================
	//system exploration phase parameters 
	// ================================
		static boolean testing = false ; 	// run algorithm in testing phase (true) 
		static int MaxIter = 1000 ; 	// Number of iterations 
		static int Nmax = 100000 ; 	// maximum number of realisations 
		static int EulerSteps = 1 ; 	// number of Euler steps 
		static int NumOfRuns = 10 ; 	// number of runs to take averages 
		static int IntReals = 1000 ; 	// initial number of reailisations	 
		static int realsInc = 2 ; 	// multiplier of N  
		static double tauIncrement = 10.0 ; 	// multiple of tau 
		static double desiredTol = 0.01 ; 	// desired tolerance of fixed point solution (F-x<=tol) 
		static double tauSpredThreshold = 100000.0 ; 	// confInterval on variance	 

	// ================================
	//Problem Sepcific parameters 
	// ================================
	
	// Double Well
	// gamma = 0.05; tau = 1; N = 160; ds = 0.24;
	static String[] systemParameters = {"set barrier-height ", 										// system settings
																			"set asymmetric-term ", "set eta "};						// continued parameter first
	static double[] param 					 = {3.0, 0.3, 0.0};	// values of systemParameters, continued parameter first	
	static String NetlogoFile				 = "netlogo/StocahsticDoubleWell.nlogo";			// Netologo code name and location relative to current directory
	static String[] RestrictOperator		 = {"mean [energy] of turtles "};			// i.e. RestrictOperator of the system															 
	static String[] LiftOperator  = {"set InitialX "}; // set conditions (x[])
	static double[]xInitial					= {1.75};				// Initial guess for the fixed point for the system // 1.75 for stable branch
	static boolean isSystemInitialised = false;	
	
/*
	static String[] systemParameters = {"set virus-spread-chance ",
																			"set average-node-degree ",
																			"set virus-check-frequency ",
																			"set recovery-chance ",
																			"set gain-resistance-chance " 
																			};		// set conditions (x[])				
	static double[] param 					 = {20.0, 5.0, 1.0, 1.0, 0.0};	// values of systemParameters, continued parameter first
	static String NetlogoFile				 = "netlogo/Virus on a Network.nlogo";			// Netologo code name and location relative to current directory
	static String[] RestrictOperator		     = {
																		"count turtles ",
																	  "count turtles with [infected?] / (count turtles) * 100 "
																	  };	// RestrictOperator of macroscopic state									 
	static String[] LiftOperator   = {"set number-of-nodes ", "set initial-outbreak-size "}; 
	static double[]xInitial				 = {100.0, 95.0};				// Initial guess for the fixed point for the system
	static boolean isSystemInitialised = true;
	*/
/*

	static String[] systemParameters = {"set ROs-incentive "};		// set conditions (x[])				
	static double[] param 					 = {0.0};	// values of systemParameters, continued parameter first
	static String NetlogoFile				 = "netlogo/EFNChumber.nlogo";			// Netologo code name and location relative to current directory
	static String[] RestrictOperator = {"get-composter-number ",            "get-AD-number ",
																	  "get-num-of-contract-composter ",   "get-num-of-contract-AD ",
																	  "get-length-of-contract-Composter", "get-length-of-contract-AD",
																	  "get-solid-price "};	// RestrictOperator of macroscopic state									 
	static String[] LiftOperator		 = {"set composter-number-of-companies " , "set AD-number-of-companies ", 
																	  "set mean-composter-contract-number ", "set mean-AD-contract-number ", 
																  	"set mean-composter-contract-length ", "set mean-AD-contract-length ", 
																	  "set initial-price-solid " }; 
	static double[]xInitial				 	= {20,	0, 2.0, 0.0, 2.0, 0.0, -41.0};				// Initial guess for the fixed point for the system
	static boolean isSystemInitialised = true;

		// gamma = 0.005; tau = 8; N = 20; ds = 0.04;
	static String[] systemParameters = {"set disease ", "set cost-of-altruism ", 										// system settings
																			"set benefit-from-altruism ", "set harshness "};						// continued parameter first
	static double[] param 					 = {0.0, 0.13, 0.48, 0.85};	// values of systemParameters, continued parameter first
	static String NetlogoFile				 = "netlogo/Altruism.nlogo";			// Netologo code name and location relative to current directory
	static String[] RestrictOperator		 = {"count patches with [pcolor = pink] / count patches ", 			// Definition of the Lift operator
																		 "count patches with [pcolor = green] / count patches "};			// i.e. RestrictOperator of the system																 
	static String[] LiftOperator  = {"set altruistic-probability ", "set selfish-probability "}; // set conditions (x[])
	static double[]xInitial					= {1.0,	0.0};				// Initial guess for the fixed point for the system
	static boolean isSystemInitialised = true; 
	*/
}
