package app.atom.runner;

import java.util.*;

import app.atom.*;

/**
 *  The Stack of Scope's.
 * The new Scope goes to the top of this heap.
 * The visibility goes in top down order.
 * The global variables located at the bottom of the
 * heap.
 */

public class Scopes
{
  /**
   *  Variable tables.
   * Every element of this Vector is a HashTable.
   * The visibilty goes from end of this list to zero.
   */
  Stack scopes = new Stack();      // local visibility variable tables

  /**
   * @return true if no Scope defined.
   */
  boolean noScope()
  {
    return scopes.size() == 0;
  }

  /**
   *  Get the top Scope.
   * @return the current visibilty, null if no Scope defined in this heap.
   */
  Scope localScope()
  {
    if ( noScope() )
      return null;
    return (Scope)scopes.peek();
  }

  /**
   *  Get a value of a predefined vars.
   * This method looks through the heap.
   *
   * @param name the name of the variables.
   * @return null if variable not defined
   */
  public Atom get( String name )
  {
    Scope scope = getScopeOf( name );
    if ( scope == null )
      return null;
    return scope.get( name );
  }

  /**
   *  Return value of the top Scope.
   * @return with the eval value of the current execution frame
   */
  Atom getReturnValue()
  {
    return localScope().getReturnValue(); 
  }

  /**
   *  Set the return value of this execution frame.
   * @param val the return value
   */
  public void setReturnValue( Atom val )
  {
    localScope().setReturnValue( val ); 
  }

  /**
   *  Is return value defined?
   * @return true if return is defined
   */
  boolean isRet()
  {
    return localScope().hasReturnValue(); 
  }


  /**
   *  Get the variables storage frame.
   * This method do the top-down visibility
   *
   * @param name the variable name
   * @return frame of the variables or null if variables not defined.
   */
  Scope getScopeOf( String name )
  {
    if ( noScope() )
      return null;
    Scope frame = null;
    int top = scopes.size();
    for(;;)
    {
      frame = (Scope)scopes.elementAt(--top);
      if ( frame.get( name ) != null )
        return frame;
      if ( top == 0 )
        return null;
    }
  }

  /**
   *  Get the global visibilty scope.
   * The variables in this Scope stay visible, until
   * a local bindings not hiding them.
   * @return the bottom Scope
   */
  Scope getGlobalScope()
  {
    if ( scopes.size() == 0 )
      return null;
    return (Scope)scopes.firstElement();
  }

  /**
   *  Set the value of a variables.
   * Make a new variables if not defined.
   * @param name the name of the variables
   * @param val the value of the variables
   */
  public void set( String name, Atom val )
  {
    Scope frame = getScopeOf( name );
    if ( frame == null )
      make( name, val );
    else
    {
      Atom was = frame.get( name );
      was.setAs( val );
    }
  }

  /**
   *  Create a new binding.
   * The binding created in this local scope.
   * The value of this binding is Atom.T.
   * @param name the name of the new binding
   */
  public void make( String name )
  {
    make( name, new Atom() );
  }

  /**
   * Create and define a new binding.
   * @param name the name of the binding
   * @param val the value of this binding
   */
  public void make( String name, Atom val )
  {
    localScope().set( name, val );
  }

  /**
   *  Create a new execution Scope.
   * The Scope created on the top.
   */
  public void makeScope()
  {
    scopes.push( new Scope() );
  }

  /**
   *  Destroy the last made Scope.
   * If there is no Scope made before,
   * the method has no effect.
   */
  public void dropScope()
  {
    if ( noScope() )
      return;
    Scope was = localScope();
    scopes.pop();
    Scope now = localScope();
    now.setAs( was );
  }

  /**
   *  Make a list of Scope's and the bindings in there.
   * Don't use it in a real environment it can be BIG.
   * @return a printable representation of this Scopes.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    int n = scopes.size();
    b.append( n );
    b.append( ':' );
    for(int i=0; i<n; i++)
    {
      b.append(' ');
      b.append('[');
      b.append( scopes.elementAt(i) );
      b.append(']');
    }
    return b.toString();
  }
}
