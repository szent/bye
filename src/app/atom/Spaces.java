package app.atom;

public class Spaces
{
  public static void open( StringBuffer b )
  {
    b.append( AtomList.OPEN );
  }

  public static void close( StringBuffer b )
  {
    b.append( AtomList.CLOSE );
  }

  /**
   * Repeat character.
   *
   * Create a fix length String filled with a character.
   *
   * @param c filler character.
   * @param n length of the result String.
   * @return with a generated String, "" if n <= 0
   */
  public static void repTo( StringBuffer to, int n )
  {
    repTo( to, n, ' ' );
  }

  public static void repTo( StringBuffer to, int n, char filler)
  {
    for (int i = 0; i < n; i++)
      to.append( filler );
  }


  /**
   *  Check the character.
   *  <ul>
   *  The whitespaces.
   *  <li>space
   *  <li>carrige return
   *  <li>linefeed
   *  <li>tabulator
   *  </ul>
   * @return true if the character is whitespace.
   */
  public static boolean whiteSpace(char c)
  {
    return c == ' ' || c == '\n' || c == '\r' || c == '\t';
  }


  /**
   *   Find the next whitespace.
   *
   * @param line the String where we have search
   * @param from the inclusive start index.
   * @return index of the next white space, or the length if not found.
   */
  public static int skipToWhiteSpace(String line, int from)
  {
    int len = line.length();
    while( from++ < len )
      if ( whiteSpace( line.charAt(from) ) )
        break;
    return from;
  }


  public static boolean whiteSpaced( String word )
  {
    if ( word == null )
      return true;
    int n = word.length();
    for( int i=0; i<n; i++ )
    {
      if ( whiteSpace(word.charAt( i )) )
        return true;
    }
    return false;
  }

  /**
   *  Replace all white spaces to space.
   *  Remove the repeated white spaces too.
   *  This append a whitespace at the end if any.
   * @param from noisy input string
   * @param start first filtered character index, inclusive
   * @param end last unfiltered character index, exclusive
   * @return the cleaned up String
   */
  public static String whiteFilter( String from, int start, int end )
  {
    StringBuilder b = new StringBuilder();
    boolean inquote = false;
    // previous was white space
    boolean prev = true;
    char c;
    for(int i=start; i<end; i++)
    {
      c = from.charAt(i);

      // keep all white space in the quoted string
      if ( inquote )
      {
        if ( c == '"' )
          inquote = false;
        // keep the quote sign
        b.append( c );
        prev = false;
        continue;
      }
      if ( c == '"' )
      {
        inquote = true;
        b.append( c );
        continue;
      }

      if ( whiteSpace( c ) )
      {
        if ( prev )
          continue;
        b.append(' ');
        prev = true;
        continue;
      }
      b.append( c );
      prev = false;
    }
    return b.toString();
  }

  /**
   *  Remove the unecessary spaces.
   *  The result String is usually not nice for human eyes,
   *  because it is a pretty long line.
   *  @return with a machine readable String
   */
  public static String whiteFilter( String from )
  {
    return whiteFilter( from, 0, from.length() );
  }
}

