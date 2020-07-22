/********************************************************************
* Copyright (c) 2012, Institute of Cancer Research
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 
* (1) Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
* 
* (2) Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
* 
* (3) Neither the name of the Institute of Cancer Research nor the
*     names of its contributors may be used to endorse or promote
*     products derived from this software without specific prior
*     written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
* COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

/*********************************************************************
* @author Simon J Doran
* Java class: Vector2D.java
* First created on Jun 11, 2009 at 4:27:30 PM
* 
* This class combines the utility of an *extensible* Vector, to which
* items can be added simply by using the add method, with the notion
* of a traditional 2-D matrix in which element (i, j) can easily be
* extracted and in which columns can be addressed as straightforwardly
* as rows. Clearly, there are incompatibilities between the two
* pictures, since in a Vector<Vector<E>> not all the rows have to be
* the same length. Appropriate errors are thrown in this case.
* 
* Note that it would have been possible simply to return null for all
* positions where a given row does not have sufficient columns.
* However, this is less useful, because (a) it may be useful to know
* when the "2-D matrix" is not correctly formed, and (b) it may be
* useful to have genuine null elements in the 2-D matrix.
* 
* Historical note: Unfortunately, at the time I wrote this, I was
* just learning Java and did not appreciate that the Vector class
* itself was deprecated. Vector2D turned out to be really useful,
* though, and it has found its way into lots of corners of XNAT_DAO.
* A big refactoring is planned at some stage to replace Vector2D with
* a more appropriate collection. SJD 29.11.12
*********************************************************************/


package generalUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;


/**
 * This class combines the utility of an *extensible* Vector, to which items
 * can be added simply by using the add method, with the notion of a traditional
 * 2-D matrix in which element (i, j) can easily be extracted and in which
 * columns can be addressed as straightforwardly as rows. Clearly, there are
 * incompatibilities between the two pictures, since in a Vector<Vector<E>>
 * not all the rows have to be the same length. Appropriate errors are thrown
 * in this case.
 * 
 * Note that it would have been possible simply to return null for all positions
 * where a given row does not have sufficient columns. However, this is less
 * useful, because (a) it may be useful to know when the "2-D matrix" is not
 * correctly formed, and (b) it may be useful to have genuine null elements
 * in the 2-D matrix.
 * @author simond
 * @param <E>
 */


public class Vector2D<E>
{
   private Vector<Vector<E>> v2d;

   public Vector2D()
   {
      v2d = new Vector<Vector<E>>();
   }

   /**
    * @param col
    * @param row
    * @return the "2-D matrix element" at (col, row)
    */
   public E atom(int col, int row)
   {
      int    nRows = v2d.size();
      String s = "row=" + row + ", max=" + (nRows-1);
      if (row < 0 || row >= nRows)
         throw new  ArrayIndexOutOfBoundsException(s);
      
      Vector<E> vRow  = v2d.elementAt(row);
      int       nCols = vRow.size();
      s = "col=" + col + ", but row " + row + " has length of only " + nCols;
      if (col < 0 || col >= vRow.size())
         throw new ArrayIndexOutOfBoundsException(s);
      
      return vRow.elementAt(col);
   }
   
   
   
   /**
    * Method of selecting a set of elements from a single column.
    * @param col
    * @param rows
    * @return the "2-D matrix elements" at (col, rows)
    */
   public ArrayList<E> atom(int col, ArrayList<Integer> rows)
   {
      int nRows = v2d.size();
      
      ArrayList<E> output = new ArrayList<E>();

      for (int i=0; i<rows.size(); i++)
      {
         int    row = rows.get(i);
         String s   = "Row argument contains" + row + ", max=" + (nRows-1);
         if (row < 0 || row >= nRows)
         throw new  ArrayIndexOutOfBoundsException(s);
         
         Vector<E> vRow  = v2d.elementAt(row);
         int       nCols = vRow.size();
         s = "col=" + col + ", but row " + row + " has length of only " + nCols;
         if (col < 0 || col >= vRow.size())
            throw new ArrayIndexOutOfBoundsException(s);
         
         output.add(vRow.elementAt(col));
      }
      
      return output;      
   }
   
   
   /**
    * Method of selecting a set of elements from a single column.
    * @param cols
    * @param row
    * @return the "2-D matrix elements" at (cols, row)
    */
   public ArrayList<E> atom(ArrayList<Integer> cols, int row)
   {
      int nRows = v2d.size();
      String s = "row=" + row + ", max=" + (nRows-1);
      if (row < 0 || row >= nRows)
         throw new  ArrayIndexOutOfBoundsException(s);
      
      Vector<E> vRow  = v2d.elementAt(row);
      
      ArrayList<E> output = new ArrayList<E>();

      for (int i=0; i<cols.size(); i++)
      {
         int col   = cols.get(i);
         int nCols = vRow.size();
         s = "col=" + col + ", but row " + row + " has length of only " + nCols;
         if (col < 0 || col >= vRow.size())
            throw new ArrayIndexOutOfBoundsException(s);
         
         output.add(vRow.elementAt(col));
      }
      
      return output;      
   }
   
   
   /**
    * Synonym of elementAt and get
    * @param row
    * @return Vector of results
    */
   public Vector<E> getRow(int row)
   {
      int    nRows = v2d.size();
      String s;

      s = "row=" + row + ", max=" + (nRows-1);
      if (row < 0 || row >= nRows) throw new ArrayIndexOutOfBoundsException(s);

      return v2d.elementAt(row);
   }



   
   /**
    * This command can return a meaningful result only where the individual
    * Vector<E>'s corresponding to each row are at least as long as col.
    * @param col
    * @return Vector of results
    */
   public Vector<E> getColumn(int col)
   {
      int       nRows  = v2d.size();
      int       nCols;
      Vector<E> column = new Vector<E>();
      Vector<E> row;
      String    s;
      
      for (int i=0; i<nRows; i++)
      {
         row   = v2d.elementAt(i);
         nCols = row.size();
         s = "col=" + col + ", but row " + row + " has length of only " + nCols;
         if (col >= nCols) throw new ArrayIndexOutOfBoundsException(s);
         
         column.add(row.elementAt(col));
      }
      
      return column;
   }
   
   
   public List<E> getColumnAsList(int col)
   {
      List<E>   colAsList   = new ArrayList<>();
      Vector<E> colAsVector = this.getColumn(col);
      for (int i=0; i<colAsVector.size(); i++)
      {
         colAsList.add(colAsVector.get(i));
      }
      
      return colAsList;
   }


   /**
    * This command is only meaningful if there are the same number of rows in
    * v2d as there are elements in the input vector and if all the rows
    * extend at least as far as beforeColumn.
    * @param newCol
    * @param beforeColumn
    */
   public void insertColumn(Vector<E> newCol, int beforeColumn)
   {
      int       nRows = v2d.size();
      int       nCols;
      String    s;

      s = "The input vector should have the same number of rows as the Searchable2DVector." +
              " I expected " + nRows + " but found " + newCol.size() + ".";
      if (newCol.size() != nRows) throw new ArrayIndexOutOfBoundsException(s);

      for (int i=0; i<nRows; i++)
      {
         Vector<E> row = v2d.elementAt(i);
         s = "The insertion point cannot be after the end of any of the rows of the" +
                 "Searchable2DVector. Row " + i + " has " + row.size() + "elements but you " +
                 "are trying to insert something at column " + beforeColumn + " (zero-indexed).";
         if (v2d.elementAt(i).size() < beforeColumn) throw new ArrayIndexOutOfBoundsException(s);
         v2d.elementAt(i).add(beforeColumn, newCol.elementAt(i));
      }
   }



   /**
    * This command is only meaningful if there are the same number of rows in
    * v2d as there are elements in the input vector and if all the rows
    * extend at least as far as col.
    * @param newCol
    * @param col
    */
   public void replaceColumn(Vector<E> newCol, int col)
   {
      int       nRows = v2d.size();
      int       nCols;
      String    s;

      s = "The input vector should have the same number of rows as the Searchable2DVector." +
              " I expected " + nRows + " but found " + newCol.size() + ".";
      if (newCol.size() != nRows) throw new ArrayIndexOutOfBoundsException(s);

      for (int i=0; i<nRows; i++)
      {
         Vector<E> row = v2d.elementAt(i);
         s = "The replacement column cannot be after the end of any of the rows of the" +
                 "Searchable2DVector. Row " + i + " has " + row.size() + "elements but you " +
                 "are trying to replace something at column " + col + " (zero-indexed).";
         if (v2d.elementAt(i).size() < col) throw new ArrayIndexOutOfBoundsException(s);
         v2d.elementAt(i).setElementAt(newCol.elementAt(i), col);
      }
   }


   /**
    * Insert a new row into the 2D vector.
    * Note that, because of the way things are implemented in terms of vectors, we can
    * actually add vectors of any length. The only requirement is that there must already
    * be at least beforeRow+1 (zero indexed) elements in the outer vector.
    * @param newRow
    * @param beforeRow
    */
   public void insertRow(Vector<E> newRow, int beforeRow)
   {
      int       nRows = v2d.size();

      String s = "The searchable2DVector must have at least " + (beforeRow+1) + "rows."
                     + "This one has only " + nRows + ".";
      if (nRows < beforeRow)  throw new ArrayIndexOutOfBoundsException(s);
      v2d.add(beforeRow, newRow);
   }



   /**
    * Replace a row in the 2D vector.
    * Note that, because of the way things are implemented in terms of vectors, we can
    * actually add vectors of any length. The only requirement is that there must already
    * be at least row+1 (zero-indexed) elements in the outer vector.
    * @param newRow
    * @param row
    */
   public void replaceRow(Vector<E> newRow, int row)
   {
      int       nRows = v2d.size();

      String s = "The searchable2DVector must have at least " + (row+1) + "rows."
                     + "This one has only " + nRows + ".";
      if (nRows < row)  throw new ArrayIndexOutOfBoundsException(s);
      v2d.setElementAt(newRow, row);
   }


   /**
    * Search for a given element in a given row of the "matrix".
    * @param row
    * @param searchElement
    * @return true if the specified row contains the search element, else false
    */
   public boolean rowContains(int row, E searchElement)
   {  
      Vector<E> vRow = getRow(row);
      return vRow.contains(searchElement);
   }


   /**
    * @param row
    * @param searchElement
    * @return the index of a given element in a given row of the "matrix".
    */
   public int indexOfForRow(int row, E searchElement)
   {
      Vector<E> vRow = getRow(row);
      return vRow.indexOf(searchElement);
   }


   /**
    * @param row
    * @param startPosition
    * @param searchElement
    * @return the index of a given element in a given row of the "matrix",
    * starting the search from index startPosition.
    */
   public int indexOfForRow(int row, int startPosition, E searchElement)
   {
      Vector<E> vRow = getRow(row);
      return vRow.indexOf(searchElement, startPosition);
   }


   /**
    * 
    * @param row
    * @param searchElement
    * @return the indices of all occurrences of a given element in a given
    * row of the "matrix"
    */
   public ArrayList<Integer> indicesOfForRow(int row, E searchElement)
   {
      Vector<E> vRow = getRow(row);
      ArrayList<Integer> indices = new ArrayList<Integer>();
      int ind = 0;
      do
      {
         ind = vRow.indexOf(searchElement);
         if (ind != -1) indices.add(ind);
      }
      while (ind != -1);
      
      return indices;
   }


   /**
    * Search for a given element in a given column of the "matrix".
    * @param col
    * @param searchElement
    * @return true if the supplied value is contained in the column
    */
   public boolean columnContains(int col, E searchElement)
   {
      Vector<E> vCol = getColumn(col);
      return vCol.contains(searchElement);
   }


    /**
    * @param col
    * @param searchElement
    * @return the index of the first occurrence of a given element
    * in a given column of the "matrix"
    */
   public int indexOfForCol(int col, E searchElement)
   {
      Vector<E> vCol = getColumn(col);
      return vCol.indexOf(searchElement);
   }
   
   
      /**
    * @param col
    * @param startPosition
    * @param searchElement
    * @return the index of a given element in a given column of the "matrix",
    * starting the search from index startPosition.
    */
   public int indexOfForCol(int col, int startPosition, E searchElement)
   {
      Vector<E> vCol = getColumn(col);
      return vCol.indexOf(searchElement, startPosition);
   }


   /**
    * Sort the rows of the underlying Vector<Vector<E>>. The comparator must
    * provide a way of comparing the rows in some way that is up to the
    * programmer
    * @param comp
    */
   public void sortRows(Comparator<Vector<E>> comp)
   {
      Collections.sort(v2d, comp);
   }


   /**
    * 
    * @param col
    * @param searchElement
    * @return the indices of all occurrences of a given element in a given
    * column of the "matrix"
    */
   public ArrayList<Integer> indicesOfForCol(int col, E searchElement)
   {
      Vector<E> vCol = getColumn(col);
      ArrayList<Integer> indices = new ArrayList<Integer>();
      int ind = 0;
      do
      {
         ind = vCol.indexOf(searchElement);
         if (ind != -1) indices.add(ind);
      }
      while (ind != -1);
      
      return indices;
   }


   /**
    * Return the implementing object.
    * @return Vector<Vector<E>>
    */
   public Vector<Vector<E>> getVectorOfVectors()
   {
      return v2d;
   }


   /* ------------------------------------------
   /* Implement the methods of the Vector class.
    * ------------------------------------------
    */

   public boolean add(Vector<E> vs)
   {
      return v2d.add(vs);
   }


   public void add(int index, Vector<E> vs)
   {
      v2d.add(index, vs);
   }


   public boolean addAll(Collection<Vector<E>> c)
   {
      return v2d.addAll(c);
   }


   public void addElement(Vector<E> vs)
   {
      v2d.addElement(vs);
   }


   public int capacity()
   {
      return v2d.capacity();
   }


   public void clear()
   {
      v2d.clear();
   }


   public boolean contains(Object o)
   {
      return v2d.contains((Vector<E>) o);
   }


   public boolean containsAll(Collection<Vector<E>> c)
   {
      return v2d.containsAll(c);
   }


   public void copyInto(Object[] array)
   {
      v2d.copyInto(array);
   }


   public Vector<E> elementAt(int index)
   {
      return v2d.elementAt(index);
   }


   public Enumeration<E> elements()
   {
      return (Enumeration<E>) v2d.elements();
   }


   public void ensureCapacity(int minCapacity)
   {
      v2d.ensureCapacity(minCapacity);
   }


   public boolean equals(Vector<Vector<E>> testObj)
   {
      return v2d.equals(testObj);
   }


   public Vector<E> firstElement()
   {
      return v2d.firstElement();
   }


   public Vector<E> get(int index)
   {
      return v2d.get(index);
   }


   public int hashcode()
   {
      return v2d.hashCode();
   }


   public int indexOf(Vector<E> vs)
   {
      return v2d.indexOf(vs);
   }


   public int indexOf(Vector<E> vs, int index)
   {
      return v2d.indexOf(vs, index);
   }


   public void insertElementAt(Vector<E> vs, int index)
   {
      v2d.insertElementAt(vs, index);
   }


   public boolean isEmpty()
   {
      return v2d.isEmpty();
   }


   public Vector<E> lastElement()
   {
      return v2d.lastElement();
   }


   public int lastIndexOf(Vector<E> vs)
   {
      return v2d.lastIndexOf(vs);
   }


   public int lastIndexOf(Vector<E> vs, int index)
   {
      return v2d.lastIndexOf(vs, index);
   }


   public Vector<E> remove(int index)
   {
      return v2d.remove(index);
   }


   public boolean remove(Vector<E> vs)
   {
      return v2d.remove(vs);
   }


   public boolean removeAll(Collection<Vector<E>> c)
   {
      return v2d.removeAll(c);
   }


   public void removeAllElements()
   {
      v2d.removeAllElements();
   }


   public boolean removeElement(Vector<E> vs)
   {
      return v2d.removeElement(vs);
   }


   public void removeElementAt(int index)
   {
      v2d.removeElementAt(index);
   }


   public boolean retainAll(Collection<Vector<E>> c)
   {
      return v2d.retainAll(c);
   }


   public Vector<E> set(int index, Vector<E> vs)
   {
      return v2d.set(index, vs);
   }


   public void setElementAt(Vector<E> vs, int index)
   {
      v2d.setElementAt(vs, index);
   }


   public void setSize(int newSize)
   {
      v2d.setSize(newSize);
   }


   public int size()
   {
      return v2d.size();
   }


   public List<Vector<E>> subList(int fromIndex, int toIndex)
   {
      return v2d.subList(toIndex, toIndex);
   }


   public Object[] toArray()
   {
      return v2d.toArray();
   }


   public Vector<E>[] toArray(Vector<E>[] a)
   {
      return v2d.toArray(a);
   }


   @Override
   public String toString()
   {
      return v2d.toString();
   }


   public void trimToSize()
   {
      v2d.trimToSize();
   }


   /* ---------------------------------------------
    * Methods inherited by Vector from AbstractList
    * ---------------------------------------------
    */

   public Iterator<Vector<E>> iterator()
   {
      return v2d.iterator();
   }


   public ListIterator<Vector<E>> listIterator()
   {
      return v2d.listIterator();
   }


   public ListIterator<Vector<E>> listIterator(int index)
   {
      return v2d.listIterator(index);
   }
}
