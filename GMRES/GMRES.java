public class GMRES extends MatrixOperations {

	public GMRES(){};
	
		// Generalised Minimal Residual algorithm
		// based on the Saad Schulz 1986 scheme
		// using Arnoldi method to generate an orthornormal basis using a Gramm-Schmidt iterator


		// the method
		
		public double[] solve(CustomMatrix A, CustomMatrix b, CustomMatrix x0, double tol){
		
			// determine the initial residual 
			CustomMatrix Ax0 = multiply(A, x0);
			double[] r0 = mat2vec( substract(b, Ax0) );
		
			// length of solution
			int size = r0.length;
		
			// need this in the least squares part later
			double norm_r0 = norm(r0, 2);
			double residual = 1.0;
			double[] v = new double[size];
			for (int i=0; i<size; i++)
				v[i] = r0[i] / norm_r0;
			
			// decalre and initialise
			int k=0;
			CustomMatrix J = new CustomMatrix();
			CustomMatrix Jtotal = new CustomMatrix(2,2);
			Jtotal.ones();
			CustomMatrix Htemp = new CustomMatrix();
			CustomMatrix HH = new CustomMatrix();
			CustomMatrix H = new CustomMatrix(1,1);
			CustomMatrix bb = new CustomMatrix(1,1);
			CustomMatrix c = new CustomMatrix();
			CustomMatrix cc = new CustomMatrix();
			CustomMatrix tempMat = new CustomMatrix();
			CustomMatrix Vmat = new CustomMatrix(size, size);
			CustomMatrix VmatOld = new CustomMatrix();
			CustomMatrix hNewCol = new CustomMatrix();
			
			CustomMatrix w;
			CustomMatrix vj = new CustomMatrix(size,1); 
			
			bb.elements[0][0] = norm_r0;
			
			// initialise matrix Vmat (matrix of orthogonal basis vectors)
			// first column only. 
			for (int i=0; i<size; i++)
				Vmat.elements[0][i] = v.elements[i][0]; 
			
			while(residual > tol){
				H.resize(H, k+1, 1); 
				
				// Arnoldi steps (using Gram-Schmidt process)
				CustomMatrix w0 = new CustomMatrix();
				w0 = multiply(A, v);
				w = mat2vec( w0 ); 
				
				for (int j=0; j<k; j++){
					for (int i=0; i<Vmat.rows; i++){
						// set the vector vj to be the jth column of Vmat
						vj.elements[i][0] = Vmat.elements[i][j]; 
					}
					// the next two lines calculate the inner product
					CustomMatrix vjw = multiply(vj, w);
					tempMat = transpose(vjw);
					
					// these two lines calculate the inner product
					H.elements[j][k] = tempMat.elements[0][0]; 
					
					w = subtract( w, multiply(H.elements[j][k], vj) ); // w = w - H[j][k]*vj
					
				}
				
				H.elements[k+1][k] = norm(w); 
				
				v = divide(w, H.elements[k+1][k]); 
				
				// add one more column to Vmat
				Vmat.resize(Vmat, Vmat.rows, k+1);
				
				for (int i=0; i<Vmat.rows; i++){
					// copy the entries of v to new column of Vmat
					Vmat.elements[i][k+1] = v.elements[i][0]; 
				}
				
				// Least squares step
				
				if (k==1)
					Htemp = copy(H);
				else {
					// for subsequent passes, Htemp = Jtotal*H
					Jtotal = resize(Jtotal, k+1, k+1);
					Jtotal.elements[k+1][k+1] = 1.0;
					Htemp = multiply(Jtotal, H);
				} 
				
				// form  next Givens rotation matrix
				J.identity(k-1);
				J = resize(J, k+1, k+1);
				
				double denominator = Math.pow( Math.pow( Htemp.elements[k][k],2) 
																		 + Math.pow( Htemp.elements[k+1][k], 2), 
														 0.5 );
														 
				J.elements[k][k]   = Htemp.elements[k][k] / denominator;
				J.elements[k][k+1] = Htemp.elements[k+1][k] / denominator;
				J.elements[k+1][k] = -1.0 * Htemp.elements[k+1][k] / denominator;
				J.elements[k+1][k+1] = Htemp.elements[k][k] / denominator; 
			
				// combine together with previous Givens rotations
				Jtotal = multiply(J, Jtotal); 
				
				HH = multiply(J, Jtotal); 
				
				for (int i=0; i<k+1; i++){
					for (int j=0; j<k; j++){
						// set all small values to zero
						if ( Math.abs( HH.elemnets[i][j] ) < 1.0e-10 )
							 HH.elemnets[i][j] = 0.0;
					}
				}
				
				bb = resize(bb, k+1, 1);
				
				c = multiply(Jtotal, bb);
				
				residual = Math.abs( c.elements[k+1][0] );
				
				k++;
			
			}
			
			System.out.println("GMRES converged in " + (k-1) + " iterations ");
			
			// extract upper triangle square matrix
			HH = resize(HH, HH.rows, 1);
			
			cc = resize(c, HH.rows, 1);
			
			CustomMatrix yy = divide(cc, HH);
			
			CustomMatrix y = mat2vec(yy); 
			
			// chop off the newest column of Vmat
			Vmat = resize(Vmat, Vmat.rows, Vmat.columns-1);
			 
			// x = mat2vec( x0 + Vmat*y )
			CustomMatrix xTemp = mat2vec( add( x0, multiply(Vmat,y) ) ); 
			
			double[] x = new double [ xTemp.rows ];
			for (int i=0; i<xTemp.rows; i++)
				x[i] = xTemp.elements[i][0];
			
			return x;
		}


}
