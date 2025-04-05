package app.atom.runner;

import app.atom.*;

import java.util.*;

/**
 * This is the AtomList interpreter environment.
 * This environment contains the defined functions and
 * the bindings with dynamic scope stack.
 * The Runner use to interface to communicate with the front-end api.
 * The Toploop interface for the system function, and the
 * Parser interface for the commandline processing.
 * Any number of Parsers can be add to a Runner.
 * The last added parser invoked first.
 */
public class Runner
{
  public AtomListParser parser;

  /**
   * The System.
   */
  public Toploop top;

  /**
   *  The deepest scope.
   */
  public Scope globals;

  /**
   *  The stack of the scopes.
   */
  public Scopes vars;

  /**
   *  List of the defined functions.
   */
  Hashtable funtab;

  /**
   *  List of the user defined parsers.
   */
  Vector functions;

  /**
   * Create a new interpreter environment.
   *  Create an empty environment.
   *  No Toploop system.
   *  No functions.
   *  No variables.
   *  No parsers.
   */
  public Runner()
  {
    top = null;
    parser = new AtomListParser();
    funtab = new Hashtable();
    vars = new Scopes();
    vars.makeScope();
    globals = vars.getGlobalScope();
    functions = new Vector();
  }

  /**
   *  Check the back from recursion state.
   * @return true if recursion stopped.
   */
  public boolean goingBack()
  {
    return vars.localScope().goingBack();
  }

  /**
   *  Catch and stop the recursion going back.
   */
  public void stopBack()
  {
    vars.localScope().stopBack();
  }

  /**
   *  Check the reason of throw.
   * If nothing happened the return value is 0.
   * @return RETURN or BREAK or other user defined value
   */
  public int backWith()
  {
    return vars.localScope().backWith();
  }

  /**
   *  Throw an exception.
   * @param value the non zero value of the reason
   */
  public void goBackWith( int val )
  {
    vars.localScope().goBackWith(val);
  }



  /**
   *  Set the System interface.
   * @param t the Toploop system.
   */
  public void setToploop( Toploop t )
  {
    top = t;
  }

  /**
   *  Print an object.
   * Print the toString value of an Object without newline.
   * @param what the object to be print
   */
  public void print( Object what )
  {
    if ( top == null )
      System.out.println( what );
    else
      top.print( what.toString() );
  }

  /**
   *  Print an error message.
   * Print an error message String and a newline.
   * The printout decorated with 3 leading stars.
   * @param message the error messages.
   */
  public void error(String message)
  {
    print("*** " + message + "\n");
  }

  /**
   * Looking for a function by name.
   * @return the function or null if the function not defined
   */
  Fun getFun( String name )
  {
    if ( name != null )
      return (Fun)funtab.get(name);
    return null;
  }

  /**
   *  Register this function for the later use.
   * @param f the defined function
   */
  public void addFun( Fun f )
  {
    funtab.put( f.getName(), f );
  }

  /**
   *  Call a function.
   * Make a new Scope.
   * Assign the passed arguments to the defined function
   * argument names. The not defined aguments asigned as nil.
   * Execute the definition body of this function.
   * If a RETURN goBackWith value received stop the execution
   * of the definition body.
   * @param fn the function
   * @param args the arguments
   */
  Atom invoke( Fun fn, AtomList args )
  {
    int argc = args.size();

    // create a local variable frame
    vars.makeScope();


    // get the name of the function parameteres
    String[] argnames = fn.getAtoms();
    for(int i=0; i<argnames.length; i++)
    {
      String argname = argnames[i];
      // if no passed variables make a nil parameter list
      if ( i+1 >= argc )
      {
        vars.make( argname, Atom.nothing );
        continue;
      }

      // assign the parameter names to the
      // passed variables
      vars.make( argname, eval( args.get(i+1) ) );
    }

    Atom ret = Atom.nothing;
    AtomList body = fn.getBody();
    int bl = body.size();
    for(int i=0; i<bl; i++)
    {
      ret = eval( body.get(i) );
      if ( backWith() == Functions.RETURN )
      {
        stopBack();
        break;
      }
    }
    if ( vars.isRet() )
      ret = vars.getReturnValue();
    vars.dropScope();
    return ret;
  }

  /**
   *  Add a new Parser.
   * This is how you can connect your parser to the
   * AtomList interpreter.
   */
  public void addFunctions(Functions functions)
  {
    this.functions.insertElementAt( functions, 0 );
  }

  /**
   *  Parse and evaluate a String.
   * @return with the evaluated value
   */
  public Atom parse( String op )
  {
    AtomList list;
    try
    {
      list = parser.parse( op );
    }
    catch( Exception ex )
    {
      error( "parse error: " + ex );
      return Atom.nothing;
    }
//    print( "Runner parse: "+list );
    Atom ret = Atom.nothing;
    int n = list.size();

    // if no bracket, but the first argument is
    // a String than execute as a list
    if ( n > 1 && list.first().isString() )
    {
      return eval( new Atom( list ) );
    }

    for(int i=0; i<n; i++)
      ret = eval( list.get(i) );
    return ret;
  }


  /**
   *  Evaluate an expression, with parameter list.
   * If goingBack the eval return without any action.
   * The eval of Atom.nil is Atom.nil.
   * If Atom is quoted the evaluated Atom is one less
   * quoted Atom.
   * @param arg
   * @return the eval value of the arg.
   */
  public Atom eval( Atom arg )
  {
    if ( goingBack() )
      return Atom.nothing;

    if ( arg.isNothing() )
      return Atom.nothing;

    if ( arg.isQuoted() )
    {
      return arg.unQuote();
    }

    if ( arg.isList() )
    {
      AtomList args = arg.listValue();
      int n = args.size();
      // Evaluated empty list is nil
      // this should'n't be happened anyway, since we
      // checked the zero length AtomList parameter
      if ( n == 0 )
        return Atom.nothing;

      Atom op = args.first();
      // Call a built in function
      if ( op.isString() )
      {
        String opcode = op.toString();
        if ( "fun".equals( opcode ) )
        {
          Fun fun = new Fun();
          if ( fun.define( args ) )
          {
            addFun( fun );
            return Atom.one;
          }
        }
        else if ( "need".equals(opcode) )
        {
          int si = 1;
          n--;
          for(int i=0; i<n; i++)
          {
            try
            {
              String name = args.get(si++).toString();
              if ( name.indexOf('.') == -1 )
              {
                String pack = getClass().getName();
                int lastDot = pack.lastIndexOf('.');
                name = pack.substring(0, lastDot)+ '.' + name + "Functions";
              }
              Class functionClass = Class.forName( name );
              Functions fs = (Functions)functionClass.newInstance();
              addFunctions( fs );
            }
            catch( Exception any )
            {
              any.printStackTrace();
              return new Atom( any );
            }
          }
          return Atom.nothing;
        }
        else if ( "ret".equals( opcode ) )
        {
          Atom ret = eval( args.second() );
          vars.setReturnValue( ret );
          return ret;
        }
        // normally don't have return value
        Atom ret = new Atom();

        Fun fn = (Fun)funtab.get( opcode );
        if ( fn != null )
          return invoke( fn, args );

        Enumeration en = functions.elements();
        while( en.hasMoreElements() )
        {
          Functions pa = (Functions)en.nextElement();
          if ( pa.parse( this, ret, opcode, args ) )
            return ret;
        }

        if ( n == 1 )
          return eval( op );
        return ret;
      }
      else
      {
        // Parse a list operations
        Atom ret = Atom.nothing;
        for(int i=0; i<n; i++)
          ret = eval( args.get(i) );
        return ret;
      }


//      error( "not a function:"+op);
//      return Atom.nil;
    }


    if ( arg.isString() )
    {
      // give a chance to the parsers to recognize
      // the default data types from string
      String name = arg.toString();
      Atom val;
      Enumeration en = functions.elements();
      while( en.hasMoreElements() )
      {
        Functions pa = (Functions)en.nextElement();
        val = pa.eval( name );
        if ( val != null )
          return val;
      }

      // othervise guess it is an identifier
      // try to dereference it
      //      System.out.println( vars.toString() );
      val = vars.get( name );
      if ( val == null )
      {
        error("unbound variable: " + name);
        return arg;
      }
      return val;
    }

// no way
    return arg;
  }


}
