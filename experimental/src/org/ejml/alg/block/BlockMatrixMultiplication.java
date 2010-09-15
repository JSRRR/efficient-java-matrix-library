/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.block;

import org.ejml.data.D1Submatrix64F;

/**
 * Contains matrix multiplication operations on  {@link org.ejml.data.BlockMatrix64F}. To
 * reduce code complexity all operations take a submatrix as input.
 *
 * @author Peter Abeles
 */
// TODO optimize the code.  Don't forget to simply comment out the current readable code
public class BlockMatrixMultiplication {

    /**
     * <p>
     * Performs a matrix multiplication on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void mult( int blockLength ,
                             D1Submatrix64F A , D1Submatrix64F B ,
                             D1Submatrix64F C )
    {
        for( int i = A.row0; i < A.row1; i += blockLength ) {
            int heightA = Math.min( blockLength , A.row1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.row0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*heightA;

                for( int k = A.col0; k < A.col1; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.col1 - k );

                    int indexA = i*A.original.numCols + k*heightA;
                    int indexB = (k-A.col0+B.row0)*B.original.numCols + j*widthA;

                    if( k == A.col0 )
                        multBlockSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                    else
                        multBlockAdd(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    /**
     * <p>
     * Performs a matrix multiplication with a transpose on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a<sup>T</sup> * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multTransA( int blockLength ,
                                   D1Submatrix64F A , D1Submatrix64F B ,
                                   D1Submatrix64F C )
    {
        for( int i = A.col0; i < A.col1; i += blockLength ) {
            int widthA = Math.min( blockLength , A.col1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.col0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*widthA;

                for( int k = A.row0; k < A.row1; k += blockLength ) {
                    int heightA = Math.min( blockLength , A.row1 - k );

                    int indexA = k*A.original.numCols + i*heightA;
                    int indexB = (k-A.row0+B.row0)*B.original.numCols + j*heightA;

//                    System.out.println("heightA "+heightA+" widthA "+widthA+" widthB "+widthB);

                    if( k == A.row0 )
                        multTransABlockSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                    else
                        multTransABlockAdd(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    /**
     * <p>
     * Performs a matrix multiplication with a transpose on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a * b <sup>T</sup> <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Length of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multTransB( int blockLength ,
                                   D1Submatrix64F A , D1Submatrix64F B ,
                                   D1Submatrix64F C )
    {
        for( int i = A.row0; i < A.row1; i += blockLength ) {
            int heightA = Math.min( blockLength , A.row1 - i );

            for( int j = B.row0; j < B.row1; j += blockLength ) {
                int widthC = Math.min( blockLength , B.row1 - j );

                int indexC = (i-A.row0+C.row0)*C.original.numCols + (j-B.row0+C.col0)*heightA;

                for( int k = A.col0; k < A.col1; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.col1 - k );

                    int indexA = i*A.original.numCols + k*heightA;
                    int indexB = j*B.original.numCols + (k-A.col0+B.col0)*widthC;

                    if( k == A.col0 )
                        multTransBBlockSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthC);
                    else
                        multTransBBlockAdd(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthC);
                }
            }
        }
    }

    /**
     * Performs a matrix multiplication between inner block matrices.
     *
     * (m , o) = (m , n) * (n , o)
     */
    protected static void multBlockSet( double[] dataA, double []dataB, double []dataC,
                                        int indexA, int indexB, int indexC,
                                        final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ , indexC++ ) {
                int indexBB = indexB + j;
                int indexAA = indexA;

                double val = 0;

                int end = indexA + widthA;

                for( ; indexAA != end; indexAA++) {
                    val += dataA[ indexAA ] * dataB[indexBB];
                    indexBB += widthC;
                }

                dataC[ indexC ] = val;
            }

            indexA += widthA;
        }
    }

    /**
     * Performs a matrix multiplication between inner block matrices.
     *
     * (m , o) += (m , n) * (n , o)
     */
    protected static void multBlockAdd( double[] dataA, double []dataB, double []dataC,
                                      int indexA, int indexB, int indexC,
                                      final int heightA, final int widthA, final int widthC) {
//        for( int i = 0; i < heightA; i++ ) {
//            for( int j = 0; j < widthC; j++ ) {
//                double val = 0;
//
//                for( int k = 0; k < widthA; k++ ) {
//                    val += dataA[i*widthA + k + indexA] * dataB[k*widthB + j + indexB];
//                }
//
//                dataC[ i*widthB + j + indexC ] += val;
//            }
//        }

        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ , indexC++ ) {
                int indexBB = indexB + j;
                int indexAA = indexA;

                double val = 0;

                int end = indexA + widthA;

                for( ; indexAA != end; indexAA++) {
                    val += dataA[ indexAA ] * dataB[indexBB];
                    indexBB += widthC;
                }

                dataC[ indexC ] += val;
            }

            indexA += widthA;
        }
    }

    protected static void multTransABlockSet( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < widthA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < heightA; k++ ) {
                    val += dataA[k*widthA + i + indexA] * dataB[k*widthC + j + indexB];
                }

                dataC[ i*widthC + j + indexC ] = val;
            }
        }
    }


    protected static void multTransABlockAdd( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC ) {
        for( int i = 0; i < widthA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < heightA; k++ ) {
                    val += dataA[k*widthA + i + indexA] * dataB[k*widthC + j + indexB];
                }

                dataC[ i*widthC + j + indexC ] += val;
            }
        }
    }

    protected static void multTransBBlockSet( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < widthA; k++ ) {
                    val += dataA[i*widthA + k + indexA] * dataB[j*widthA + k + indexB];
                }

                dataC[ i*widthC + j + indexC ] = val;
            }
        }
    }

    protected static void multTransBBlockAdd( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < widthA; k++ ) {
                    val += dataA[i*widthA + k + indexA] * dataB[j*widthA + k + indexB];
                }

                dataC[ i*widthC + j + indexC ] += val;
            }
        }
    }
}