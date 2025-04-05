package app.atom.runner;

import app.atom.*;

import java.util.*;

/**
 * The function call.
 * The Runner use this object to define and invoke a function.
 * The function definition macro follow the CommonLisp tradition.
 * This class used just inside this package.
 */
public class Fun
{
  /**
   * The literal name of the function.
   */
  String name;

  /**
   * The name of the passed parameters.
   */
  String[] args;

  /**
   * The function definition.
   */
  AtomList body;

  /**
   * Create an empty not defined function.
   */
  public Fun()
  {
    undef();
  }


  /**
   *  Check the function definition.
   * @return true if function body defined.
   */
  public boolean defined(){ return body != null; }

  /**
   *  Check the argumentum list.
   * @return true if no passed argument defined.
   */
  public boolean argless(){ return args == null; }

  /**
   *  Name of this function.
   * @return the predefined name of the function.
   */
  public String getName(){ return name; }

  /**
   *  The executable function body.
   * @return the definition of this function.
   */
  AtomList getBody(){ return body; }

  /**
   *  Passed argument names.
   * The Runner assign the passed variables to this
   * names.
   * @return the names of the arguments.
   */
  String[] getAtoms(){ return args; }

  /**
   *  Mark this function undefined.
   */
  void undef()
  {
    args = null;
    body = null;
  }

  /**
   *  Define this function.
   * This definition must have all parts:
   * <ul>Commonlisp convention
   * <li>fun
   * <li>name_of_the_function
   * <li>argument_name*
   * <li>body*
   * </ul>
   * @param def function definition list
   * @return true if definition succesfull.
   */
  public boolean define(AtomList def)
  {
    undef();
    int n = def.size();
    // 4 at least 1:<fun> 2:<name> 3:(args) 4:..body..
    if ( n < 4 )
      return false;

    Atom a;
    // get the name
    a = def.second();
    if ( a.isString() )
      name = a.toString();
    else
      return false;
      // not a name in function definition

    // get function description Lot the body
    a = def.third();
    if ( a.isList() )
    {
      AtomList list = a.listValue();
      int an = list.size();
      args = new String[an];
      for(int i=0; i<an; i++)
      {
        a = list.get(i);
        if ( a.isString() )
          args[i] = a.toString();
        else
        {
          undef();
          return false;
        }
      }
    }
    else
      return false;
      // not an Atomlist in argument definition list

    body = new AtomList();
    for(int i=3; i<n; i++)
    {
      body.add( def.get(i) );
    }
    return true;
  }

  /**
   *  Human readable function definition.
   * It also machine readable, if defined.
   * @return the definition String
   */
  @Override
  public String toString()
  {
    if ( !defined() )
      return "not defined";
    StringBuffer b = new StringBuffer();
    Spaces.open( b );
    b.append("fun ");
    b.append(name);
    b.append('\n');
    Spaces.open( b );
    if ( !argless() )
    {
      int argc = args.length;
      for(int i=0; i<argc; i++)
      {
        b.append( args[i] );
        b.append( ' ' );
      }
    }
    Spaces.close( b );
    b.append('\n');
    b.append( body );
    b.append('\n');
    return b.toString();
  }

}

