/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab5;

/**
 *
 * @author srosenbe
 */
public class Matrix
{
    private int numRows;
    private int numColumns;
    private double[][] entries;

    public Matrix(int numberOfRows, int numberOfColumns)
    {
        numRows = numberOfRows;
        numColumns = numberOfColumns;
        entries = new double[numRows][numColumns];
    }

    public Matrix(double[][] array)
    {
        numRows = array.length;
        numColumns = array[0].length;
        entries = new double[numRows][numColumns];
        for(int i = 0; i < numRows; i++)
        {
            for(int j = 0; j < numColumns; j++)
            {
                entries[i][j] = array[i][j];
            }
        }
    }

    public Matrix(Matrix original)
    {
        numRows = original.numRows;
        numColumns = original.numColumns;
        entries = new double[numRows][numColumns];
        for(int i = 0; i < numRows; i++)
        {
            for(int j = 0; j < numColumns; j++)
            {
                entries[i][j] = original.entries[i][j];
            }
        }
    }

    public int getNumberOfRows()
    {
        return numRows;
    }

    public int getNumberOfColumns()
    {
        return numColumns;
    }

    public void setEntry(int rowNumber, int columnNumber, double entry)
    {
        entries[rowNumber][columnNumber] = entry;
    }

    public double getEntry(int rowNumber, int columnNumber)
    {
        return entries[rowNumber][columnNumber];
    }

    public Matrix multiply(Matrix RHS) throws MatrixException
    {
        if(numColumns != RHS.numRows)
        {
            throw new MatrixException("Illegal matrix sizes in Matrix multiply: " + numColumns + " != " + RHS.numRows);
        }
        Matrix product = new Matrix(numRows, RHS.numColumns);
        for(int i = 0; i < product.numRows; i++)
        {
            for(int j = 0; j < product.numColumns; j++)
            {
                double sum = 0;
                for(int k = 0; k < numColumns; k++)
                {
                    sum += entries[i][k] * RHS.entries[k][j];
                }
                product.entries[i][j] = sum;
            }
        }
        return product;
    }

    public Matrix add(Matrix RHS) throws MatrixException
    {
        if(numColumns != RHS.numColumns || numRows != RHS.numRows)
        {
            throw new MatrixException("Illegal matrix sizes in Matrix add:" + numColumns +
                    "x" + numRows + " vs " + RHS.numColumns + "x" + RHS.numRows);
        }
        Matrix sum = new Matrix(numRows, numColumns);
        for(int i = 0; i < numRows; i++)
        {
            for(int j = 0; j < numColumns; j++)
            {
                sum.entries[i][j] = entries[i][j] + RHS.entries[i][j];
            }
        }
        return sum;
    }

    public void setColumn(int columnNumber, VectorGraphics.Point vector)
    {
        entries[0][columnNumber] = vector.x();
        entries[1][columnNumber] = vector.y();
        entries[2][columnNumber] = vector.z();
    }

    public Matrix transpose()
    {
        Matrix result = new Matrix(numColumns, numRows);
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                double originalEntry = getEntry(row, col);
                result.setEntry(col, row, originalEntry);
            }
        }
        return result;
    }

    @Override public String toString()
    {
        String output = "";
        for(int i = 0; i < numRows; i++)
        {
            for(int j = 0; j < numColumns; j++)
            {
                output += entries[i][j] + "\t";
            }
            output += "\r\n";
        }
        return output;
    }
}

class MatrixException extends Exception
{
    public MatrixException(String s)
    {
        super(s);
    }
}
