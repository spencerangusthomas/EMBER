// to run
// javac -cp : codes/GMREStest.java codes/MatrixOperations.java codes/CustomMatrix.java 
// java -Xmx2G -cp .:codes/ -Djava.library.path=lib GMREStest 
public class GMREStest{

	public static void main (String[] args) {
		
		MatrixOperations gmres = new MatrixOperations();
		
		int size = 2;
		CustomMatrix A  = new CustomMatrix(size,size);
		CustomMatrix b  = new CustomMatrix(size,1);
		CustomMatrix x0 = new CustomMatrix(size,1);
		
		double tol = 1.0e-6; 
		/*
		// set up A
		A.elements[0][0] = 0.878; A.elements[0][1] = 0.832; A.elements[0][2] = 0.266; A.elements[0][3] = 0.979; A.elements[0][4] = 0.024;     
		A.elements[1][0] = 0.116; A.elements[1][1] = 0.293; A.elements[1][2] = 0.263; A.elements[1][3] = 0.791; A.elements[1][4] = 0.209;    
		A.elements[2][0] = 0.986; A.elements[2][1] = 0.511; A.elements[2][2] = 0.583; A.elements[2][3] = 0.212; A.elements[2][4] = 0.294; 
		A.elements[3][0] = 0.858; A.elements[3][1] = 0.751; A.elements[3][2] = 0.443; A.elements[3][3] = 0.949; A.elements[3][4] = 0.366; 
		A.elements[4][0] = 0.442; A.elements[4][1] = 0.380; A.elements[4][2] = 0.447; A.elements[4][3] = 0.059; A.elements[4][4] = 0.850; 
		
		// set up b
		b.ones();
		*/
		A.elements[0][0] = 0.003;
		A.elements[0][1] = 59.14;
		A.elements[1][0] = 5.291;
		A.elements[1][1] = -6.13;
		
		b.elements[0][0] = 46.78;
		b.elements[1][0] = 59.17;
		
		
		
		// set up x0
		x0.zero();
		
		double[] x = gmres.solve(A, b, x0, tol, 10); 
		
		
		for (int i=0; i<size; i++)
			System.out.println ( x[i] );
		System.out.println(" ");
	/*	
		CustomMatrix solution1 = gmres.divide(b,A);
		//CustomMatrix solution2 = gmres.divide(A,b);
		
System.out.println(" ");		
System.out.println(" gmres.divide(b,A) = " );
for (int ii=0; ii<solution1.rows; ii++){
for (int jj=0; jj<solution1.columns; jj++){
System.out.print(solution1.elements[ii][jj] + " " );
}
System.out.println(" ");
}
System.out.println(" ");

System.out.println(" ");		
System.out.println(" gmres.divide(A,b) = " );
for (int ii=0; ii<solution2.rows; ii++){
for (int jj=0; jj<solution2.columns; jj++){
System.out.print(solution2.elements[ii][jj] + " " );
}
System.out.println(" ");
}
System.out.println(" ");
	*/
	
	
	CustomMatrix A2  = new CustomMatrix(3,3);
	CustomMatrix b2  = new CustomMatrix(3,1);
	CustomMatrix x02 = new CustomMatrix(3,1);
	x02.ones();
		
	A2.elements[0][0] = 1;
	A2.elements[0][1] = 2;
	A2.elements[0][2] = 3;
	A2.elements[1][0] = 5;
	A2.elements[1][1] = 8;
	A2.elements[1][2] = 21;
	A2.elements[2][0] = 96;
	A2.elements[2][1] = 42;
	A2.elements[2][2] = 1;
	
	b2.elements[0][0] = 2;
	b2.elements[1][0] = 5;
	b2.elements[2][0] = 3;
	
	CustomMatrix testing = gmres.divide(b2,A2);
	System.out.println(" ");		
System.out.println(" testing = " );
for (int ii=0; ii<testing.rows; ii++){
for (int jj=0; jj<testing.columns; jj++){
System.out.print(testing.elements[ii][jj] + " " );
}
System.out.println(" ");
}
System.out.println(" ");
	
	}

}
