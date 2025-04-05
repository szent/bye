package app.atom.runner;

import app.atom.*;

/**
 * This is the front end part of this package.
 * The api can have a chance to implements his
 * command set through this interface.
 */

public interface Functions
{
  /**
   * Non 0 value of type of throw.
   * 0 is used for sign as normal execution.
   */
  public static final int RETURN = 1;
  public static final int BREAK = 2;

  /**
   *   Execute a command.
   * The command is the op String. The arguments of the command line
   * stored in args. The args also contains the op as a first argument
   * of the list. Use the runner to evaluate the elements from the
   * args list. The ret is the eval value of this command. Use the
   * setValue to set the return value.
   * If this Parser return with false,
   * the Runner will ask the other parsers to evaluate this expression.
   * @param runner the interpreter
   * @param ret the return value
   * @param op the command string equals with args.first()
   * @param args the passed argument list
   * @return true if op is executable with this parser
   */
  boolean parse( Runner runner, Atom ret, String op, AtomList args );

  /**
   *  Convert a String to Atom.
   * The runner call this method to give a chance this Parser
   * to recognize a data type.
   * If this parser return with null the Runner will pass this String
   * to the other parsers.
   * @param arg the String definition of a basic type
   * @return the Atom representation of this String.
   */
  Atom eval(String arg);
}
