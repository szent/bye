package app.atom.runner;

import java.util.*;

import app.atom.*;

/**
 *
 *   Scope is an execution frame in the runner environment.
 * <ul>
 * Scope contains:
 * <li>a set of named variable bindings
 * <li>a special variables used for the return value
 * <li>a mark for handling return
 * </ul>
 * This served as one element of the calling stack.
 */

public class Scope
{
  /**
   *  Default value of back.
   * Means don't go back.
   */
  static final int NO = 0;

  /**
   *  Setup the different throw method's.
   * true if we are in a throwing process
   */
  int back;

  /**
   *  true if ret value defined
   */
  boolean hasReturnValue;

  /**
   *  The return value of this block.
   */
  Atom returnValue;

  /**
   *  Collection of bindings.
   */
  Hashtable locals;

  /**
   *  Create a new collection of bindings.
   * Initialize a normal state Scope without ret value.
   */
  Scope()
  {
    stopBack();
    hasReturnValue = false;
    locals = new Hashtable();
  }

  /**
   *  Check the back from recursion state.
   * @return true if recursion stopped.
   */
  boolean goingBack()
  {
    return back != NO;
  }

  /**
   *  Check the reason of throw.
   * If nothing happened the return value is 0.
   * @return RETURN or BREAK or other user defined value
   */
  int backWith(){ return back; }

  /**
   *  Throw an exception.
   * @param value the non zero value of the reason
   */
  void goBackWith( int with ){ back = with; }

  /** Catch and stop the recursion going back. */
  void stopBack(){ back = NO; }


  /**
   *  Copy the state of this Scope.
   * This method inherit the signal variables of the
   * lost Scope to the next. The goal is to reach the
   * signal handler level of the stack.
   * It does not inherit any of the bindings, but the return value.
   * @param from the parent Scope
   */
  void setAs( Scope from )
  {
    if ( from.goingBack() )
    {
      goBackWith( from.backWith() );
      hasReturnValue = from.hasReturnValue;
      returnValue = from.returnValue;
    }
  }

  /**
   *  Check the number of bindings.
   * @return true if no bindings defined in this frame.
   */
  boolean empty(){ return locals.size() == 0; }

  /**
   *  Get a value of a symbol.
   * @param name the name pair of a bindings
   * @return the value of the name or <code>null</code> if not defined
   */
  Atom get(String name)
  {
    if ( name == null )
      return null;
    return (Atom)locals.get( name );
  }

  /**
   *  Set the value of a variables.
   * Make a new variables if not defined.
   * @param name the name of the variables
   * @param val the value of the variables
   */
  void set( String name, Atom val )
  {
    locals.put( name, val );
  }

  /**
   *  Set the value of the return.
   * Setup the special return value.
   * Make it defined too.
   * @param val the return value
   */
  void setReturnValue( Atom val )
  {
    returnValue = val;
    hasReturnValue = true;
  }

  /**
   *  Get the return value.
   * The return value is always Atom type.
   * @return Atom.nil if not defined.
   */
  Atom getReturnValue()
  {
    if ( hasReturnValue )
      return returnValue;
    return Atom.nothing;
  }

  /**
   *  Check the existence of return value.
   * @return true if return value defined.
   */
  boolean hasReturnValue()
  {
    return hasReturnValue;
  }

  /**
   *  Dump all variables.
   * @return a oneline String with the defined variables
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    int n = locals.size();
    b.append( n );
    b.append( '\n' );
    Enumeration en = locals.keys();
    String var=null;
    while( en.hasMoreElements() )
    {
      var=(String)en.nextElement();
      b.append( var );
      b.append(' ');
    }
    b.append( '\n' );
    return b.toString();
  }

}
