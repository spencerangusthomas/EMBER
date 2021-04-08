import java.util.Arrays;
import java.util.Arrays;
import java.io.*;
// Gaussian Elimination using partial or full pivoting
// full pivoting does not work properly yet.
class GaussianElimination {
    private static final double EPSILON = 1e-10;
		
		/* 
			Gaussian elimination with with full pivot
			Chapter 2 pg 40 
			http://courses.cms.caltech.edu/cs171/c2-1.pdf 
		*/ 
		double[] FullPivot(double[][] A, double[][] b){
			int size = A.length;
			int sizeb= b[0].length;
			int i, icol=0, irow=0, j, k, l, ll;
			double big, dum, pivinv, temp;
			// the integer arrays ipiv, indix, and indxc 
			// are used for bookkeeping on the pivoting;
			int[] indxc = new int[size], 
						indxr = new int[size], 
						ipiv  = new int[size];
	
			for (j=0;j<size;j++) ipiv[j]=0;
			// This is the main loop over the columns to be reduced
			for (i=0;i<size;i++) { 
				big=0.0;
				// This is the outer loop of the search for a pivot element
				for (j=0;j<size;j++)
					if (ipiv[i] != 1)
						for (k=0;k<size;k++){
							if (ipiv[k] == 0){
								if (Math.abs(A[j][k]) >= big){
									big = Math.abs(A[j][k]);
									irow = j;
									icol = k;
								}
							}
						}
					
				ipiv[icol]++;
				/*
				We now have the pivot element, so we interchange rows, 
				if needed, to put the pivot element on the diagonal. 
				The columns are not physically interchanged, only relabeled:
				indxc[i], the column of the ith pivot element, is the ith 
				column that is reduced, while indxr[i] is the row in which 
				that pivot element was originally located. If indxr[i] =
				indxc[i] there is an implied column interchange. With this 
				form of bookkeeping, the solution b’s will end up in the 	
				correct order, and the inverse matrix will be scrambled
				by columns.
				*/
		
				if (irow != icol){
					for (l=0;l<size;l++){ 
						double t = A[irow][l];
					 	A[irow][l] = A[icol][l];
					 	A[icol][l] = t;
					}
					for (l=0;l<sizeb;l++){
						double t = b[irow][l];
					 	b[irow][l] = b[icol][l];
					 	b[icol][l] = t;
					}
				}
				
				indxr[i] = irow;	// we are now ready to divide the pivot row by
				indxc[i] = icol;	// the pivot element, located at irow and icol
			
				if (A[icol][icol] == 0.0){
					for (int ii=0; ii<size; ii++){
						for (int jj=0; jj<size; jj++)
							System.out.print(A[ii][jj]+ " ");
						System.out.println(" " );
					}  
					System.out.println(irow+" " +icol);
					throw new RuntimeException("Matrix is singular or nearly singular");
				}
				pivinv = 1.0 / A[icol][icol];
				A[icol][icol] = 1.0; 
				for (l=0;l<size;l++)  A[icol][l] *= pivinv;	
				for (l=0;l<sizeb;l++) b[icol][l] *= pivinv;
			
				for (ll=0;ll<size;ll++)		// next we reduce the rows ...
					if (ll != icol) {				// except for the pivot element
						dum = A[ll][icol];
						A[ll][icol] = 0.0;
						for (l=0;l<size;l++)  A[ll][l] -= A[icol][l] * dum;
						for (l=0;l<sizeb;l++) b[ll][l] -= b[icol][l] * dum;
					}
			}
			/*
			This is the end of the main loop over columns of the reduction. 
			It only remains to unscramble the solution in view of the column 
			interchanges We do this by interchanging pairs of columns in the
			reverse order that the permutation was built up.
			*/
			for (l=size-1;l>=0;l--) {
				if (indxr[l] != indxc[l])
					for (k=0;k<size;k++){
						double t = A[k][indxr[l]];
						A[k][indxr[l]] = A[k][indxc[l]];
						A[k][indxc[l]] = t;
					}
			} 

			// Backward subsititution
			double[] x = new double[size];
			for (i=size-1;i>=0;i--){
				double sum = 0.0;
				for (j=i+1;j<size;j++)
					sum += A[i][j] * x[j];
				x[i] = (b[i][0]-sum) / A[i][i];
			}
			return x;
		}

		double[] FullPivot(double[][] A, double[] b){
			int size = A.length;
			int i, icol=0, irow=0, j, k, l, ll;
			double big, dum, pivinv, temp;
			// the integer arrays ipiv, indix, and indxc 
			// are used for bookkeeping on the pivoting;
			int[] indxc = new int[size], 
						indxr = new int[size], 
						ipiv  = new int[size];
	
			for (j=0;j<size;j++) ipiv[j]=0;
			// This is the main loop over the columns to be reduced
			for (i=0;i<size;i++) { 
				big=0.0;
				// This is the outer loop of the search for a pivot element
				for (j=0;j<size;j++)
					if (ipiv[i] != 1)
						for (k=0;k<size;k++){
							if (ipiv[k] == 0){
								if (Math.abs(A[j][k]) >= big){
									big = Math.abs(A[j][k]);
									irow = j;
									icol = k;
								}
							}
						}
					
				ipiv[icol]++;
				/*
				We now have the pivot element, so we interchange rows, 
				if needed, to put the pivot element on the diagonal. 
				The columns are not physically interchanged, only relabeled:
				indxc[i], the column of the ith pivot element, is the ith 
				column that is reduced, while indxr[i] is the row in which 
				that pivot element was originally located. If indxr[i] =
				indxc[i] there is an implied column interchange. With this 
				form of bookkeeping, the solution b’s will end up in the 	
				correct order, and the inverse matrix will be scrambled
				by columns.
				*/
		
				if (irow != icol){
					for (l=0;l<size;l++){ 
						double t = A[irow][l];
					 	A[irow][l] = A[icol][l];
					 	A[icol][l] = t;
					}
					
					double t = b[irow];
					b[irow] = b[icol];
					b[icol] = t;
				}
				
				indxr[i] = irow;	// we are now ready to divide the pivot row by
				indxc[i] = icol;	// the pivot element, located at irow and icol
			
				if (A[icol][icol] == 0.0){
					for (int ii=0; ii<size; ii++){
						for (int jj=0; jj<size; jj++)
							System.out.print(A[ii][jj]+ " ");
						System.out.println(" " );
					}  
					System.out.println(irow+" " +icol);
					throw new RuntimeException("Matrix is singular or nearly singular");
				}
				pivinv = 1.0 / A[icol][icol];
				A[icol][icol] = 1.0; 
				for (l=0;l<size;l++)  A[icol][l] *= pivinv;	
				b[icol] *= pivinv;
			
				for (ll=0;ll<size;ll++)		// next we reduce the rows ...
					if (ll != icol) {				// except for the pivot element
						dum = A[ll][icol];
						A[ll][icol] = 0.0;
						for (l=0;l<size;l++)  A[ll][l] -= A[icol][l] * dum;
						b[ll] -= b[icol] * dum;
					}
			}
			/*
			This is the end of the main loop over columns of the reduction. 
			It only remains to unscramble the solution in view of the column 
			interchanges We do this by interchanging pairs of columns in the
			reverse order that the permutation was built up.
			*/
			for (l=size-1;l>=0;l--) {
				if (indxr[l] != indxc[l])
					for (k=0;k<size;k++){
						double t = A[k][indxr[l]];
						A[k][indxr[l]] = A[k][indxc[l]];
						A[k][indxc[l]] = t;
					}
			} 

			// Backward subsititution
			double[] x = new double[size];
			for (i=size-1;i>=0;i--){
				double sum = 0.0;
				for (j=i+1;j<size;j++)
					sum += A[i][j] * x[j];
				x[i] = (b[i]-sum) / A[i][i];
			}
			return x;
		}


    // Gaussian elimination with partial pivoting
    public static double[] lsolve(double[][] A, double[] b) {
        int N  = b.length;
        for (int p = 0; p < N; p++) {
            // find pivot row and swap
            int max = p;
            for (int i = p + 1; i < N; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }
            double[] temp = A[p]; A[p] = A[max]; A[max] = temp;
            double   t    = b[p]; b[p] = b[max]; b[max] = t;

            // singular or nearly singular
            if (Math.abs(A[p][p]) <= EPSILON) {
                throw new RuntimeException("Matrix is singular or nearly singular");
            }
            // pivot within A and b
            for (int i = p + 1; i < N; i++) {
                double alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < N; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }
        // Condition of the Jacobian
        // System.out.print(" \tcondition ");
        // back substitution
        double[] x = new double[N];
        for (int i = N - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < N; j++) {
                sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
            // System.out.print(A[i][i] + " " );
        }
        // System.out.println(" ");
        return x;
    }
}