package app.atom.runner;

/**
 * This interface provide a minimal System related functions for
 * the Runner interpreter.
 */
public interface Toploop
{
  /**
   *  Get a String.
   * This method block the execution, while a String entered.
   * @return a String from the System
   */
  String readLine();

  /**
   *  Print a String to the System.
   * @param String output string
   */
  void print( String s );

}
