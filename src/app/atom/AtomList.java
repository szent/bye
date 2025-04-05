package app.atom;

import java.io.PrintWriter;
import java.util.*;

/**
 * The AtomList represents the list of Atom objects.
 * The AtomList contains <b>only</b> Atom objects.
 */

public class AtomList
{
  static final AtomList empty = new AtomList();

  public static final char  OPEN = '[';
  public static final char CLOSE = ']';

  public static final String EMPTY = "[]";


  /**
   *  The real container of the elements.
   */
  private Vector list;

  /**
   *  Create an empty list.
   */
  public AtomList()
  {
    list = new Vector();
  }

  /**
   *  Create a new list.
   * The elements of the parameter list copied into this list.
   * This method use the Vecor.clone() to make the copy.
   *
   * @param twin the original list.
   */
  public AtomList( AtomList twin )
  {
    try
    {
      list = (Vector)twin.clone();
    }
    catch( CloneNotSupportedException ex )
    {
    }
  }

  @Override
  public boolean equals( Object as )
  {
    if ( this == as )
      return true;

    if ( as instanceof AtomList )
    {
      AtomList b = (AtomList)as;
      if ( list == null && b.list == null )
        return true;
      if ( list != null && b.list != null )
        return list.equals( b.list );
    }
    return false;
  }


  public void unlist()
  {
    if ( size() == 1 && first().isList() )
      list = first().listValue().list;
  }


  /**
   *  @return true if all elements is atom.
   */
  public boolean flat()
  {
    int n = size();
    for(int i=0; i<n; i++)
      if ( get(i).isList() )
        return false;
    return true;
  }

  /**
   *  Number of elements in this list.
   * @return the number of Atom's in this list.
   */
  public int size()
  {
    return list.size();
  }

  /**
   *  Check the index.
   * @param n the test index to the list.
   * @return true if the index is out of range.
   */
  final boolean out(int n)
  {
    return n < 0 || n > list.size();
  }

  /**
   *  Get Atom from this list.
   * @param n the index to this list
   * @return null if the index out range or
   * Atom at index n.
   */
  public Atom get(int n)
  {
    if ( out(n) )
      return Atom.nothing;
    return (Atom)list.elementAt(n);
  }

  /**
   *  Get second Atom from this list.
   */
  public Atom second()
  {
    return get(1);
  }

  /**
   *  Get third Atom from this list.
   */
  public Atom third()
  {
    return get(2);
  }

  /**
   *  Get forth Atom from this list.
   */
  public Atom forth()
  {
    return get(3);
  }

  /**
   *  Get fifth Atom from this list.
   */
  public Atom fifth()
  {
    return get(4);
  }

  /**
   *  Get the last Atom from this list.
   */
  public Atom last()
  {
    return get( size()-1 );
  }

  /**
   *  Get the first Atom from this list.
   */
  public Atom first()
  {
    if ( size() > 0 )
      return (Atom)list.firstElement();
    return Atom.nothing;
  }

  /**
   * The remaining list without the first element.
   * The method makes a copy of this list.
   * The elements remains the same.
   */
  public AtomList rest()
  {
    int n = size()-1;
    if ( n < 1 )
      return empty;

    AtomList rest = new AtomList();
    int si = 1;
    for(int i=0; i<n; i++)
      rest.add( get(si++) );
    return rest;
  }

  /**
   *  Insert an Atom to the front of this list.
   * Increment the size of the list.
   * @param a the Atom to be inserted
   */
  public void insert( Atom a )
  {
    list.insertElementAt( a, 0 );
  }

  /**
   *  Append an Atom to the end of this list.
   * Increment the size of the list.
   * @param a the Atom to be inserted
   */
  public void add( Atom a )
  {
    list.addElement( a );
  }



  /**
   *  Parseable string representation.
   * @return machine parseable String representation of this list.
   */
  @Override
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    appendParse( b );
    return b.toString();
  }

  public void appendParse( StringBuffer to )
  {
    to.append( OPEN );
    int n = size();
    for(int i=0; i<n; i++ )
    {
      get(i).appendParse( to );
      if ( i+1 < n )
        to.append( ' ' );
    }
    to.append( CLOSE );
  }

  public void appendTo( StringBuffer to, int ident )
  {
    if ( ident > 0 )
      Spaces.repTo( to, ident*2 );

    if ( flat() )
    {
      /*
       *  Plain list, no sub list inside.
       */
/*
      if ( pair() )
      {
        to.append(':');
        to.append( first() );
        to.append( ' ' );
        to.append( second() );
      }
      else
      {
*/
        to.append( OPEN );
        int n = size();
        for(int i=0; i<n; i++ )
        {
          get(i).appendTo( to,0 );
          if ( i+1 < n )
            to.append(' ');
        }
        to.append( CLOSE );
//      }
    }
    else
    {
      /*
       *   Compound list
       */
      to.append( OPEN );
      ident++;
      int n = size();
      for(int i=0; i<n; i++ )
      {
        Atom atom = get(i);
        if ( i == 0 )
        {
          if ( atom.isAtom() )
            atom.appendTo( to, 0 );
          else
          {
            to.append('\n');
            atom.appendTo( to, ident );
          }
        }
        else
          atom.appendTo( to, ident );
        to.append('\n');
      }
      ident--;
      Spaces.repTo( to, ident*2 );
      to.append( CLOSE );
    }
  }

  public void niceList( PrintWriter out )
  {
    StringBuffer nice = new StringBuffer();
    appendTo( nice, 0 );
    out.println( nice.toString() );
  }

}
