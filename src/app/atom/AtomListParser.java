package app.atom;

import java.util.*;

/**
 *  This class produce AtomList from a String.
 */

public class AtomListParser
{

  /**
   *  Turn it off if you don't wanna see quoted Atom.
   */
  boolean quoteAtoms = true;


  /**
   * Build parser a tree.
   *
   *  Every elements is Atom.
   *  The branches represent Atom contains AtomList value.
   *  <br>
   *  Accept the following comment styles:
   *  <ul>
   *  <li>C block comments <code>slash-star ... star-slash</code>
   *  <li>C++ running comment <code>slash-slash ... EOL</code>
   *  <li>LISP line comment <code>; ... EOL</code>
   *  </ul>
   *  Accept the following quote styles:
   *  <ul>
   *  <li>Double quote to double quote "like this " terminated with
   *  next double quote
   *  <li>LISP style single quote 'name terminated with the next white
   *  space.
   *  <li>Length delimited String <code>like #7"the nil</code>
   *  </ul>
   *
   */
  public AtomList parse( String line ) throws ParserException
  {
    if ( line == null )
      return AtomList.empty;

    int len  = line.length();
    if ( len == 0 )
      return AtomList.empty;

    boolean inword = false;
    boolean inquote = false;
    boolean inbrace = false;
    boolean incomment = false;
    boolean inlinecomment = false;
    int braces = 0;
    int from = 0; // marker

    /*
     * Count the parsed line.
     */
    int lineCount = 0;
    char c;
    int i;
    AtomList list = new AtomList();
    for(i=0; i<len; i++)
    {
      c = line.charAt( i );
      int next = i+1;
      int nc = next < len ? line.charAt(next) : 0;

      if ( c == '\n' )
        lineCount++;

      // we inside a quote, and that not finshed
      if ( inquote && c != '"' )
        continue;

      if ( incomment )
      {
        if ( c == '*' && nc  == '/' )
        {
          incomment = false;
          i++;
        }
        continue;
      }
      else
      {
        if ( inlinecomment )
        {
          if ( c == '\r' || c == '\n' )
          {
            inlinecomment = false;
            // read the double line termination character
            if ( nc  == '\r' || nc == '\n' )
              i++;
          }
          // dont read any until CR or LF encounter
          continue;
        }
        else
        {
          if ( c == ';' )
          {
            inlinecomment = true;
            continue;
          }
        }

        if ( c == '/' )
        {
          if ( nc  == '*' )
          {
            incomment = true;
            i++;
            continue;
          }
          // turn on the // running comment
          if ( nc == '/' )
          {
            inlinecomment = true;
            i++;
            continue;
          }
        }
      }

      if ( !inquote )
      {

        if ( c == AtomList.OPEN )
        {
          inbrace = true;
          if ( braces == 0 )
            from = next;
          braces++;
          continue;
        }

        // found a close brace
        if ( c == AtomList.CLOSE )
        {
          // badly terminated list
          if ( !inbrace )
            throw new ParserException( "missing "+AtomList.OPEN+" at line "+lineCount );

          // YESSS! this is the pair of the opened brace
          if ( braces == 1 )
          {

// THE recursion
//---------------
            AtomList innerList = parse( line.substring( from, i ) );
            Atom a = new Atom( innerList );
            if ( from > 1 )
            {
              char c1 = line.charAt( from-2);
//              System.out.println("*** "+line.substring( from-1, i ) );
              if ( c1 == '\'' && quoteAtoms )
                a.quote();
            }
            list.add( a );
            braces = 0;
            inbrace = false;
            inword = false;
          }
          else
            braces--;
          continue;
        }
      }

      // a quote comes
      if ( c == '"' )
      {
        // we are inside a quoted string
        // quote close it
        if ( inquote )
        {
          inquote = false;
          if ( inbrace )
            // we just looking to the closing brace
            // this String will be processed in lower level
            continue;

          // we are in the top level brace
          // the quoted String is argument of this list
          // add a new Atom to the list
          // a quoted String
          Atom a = new Atom( line.substring( from, i ) );
          if ( quoteAtoms )
	  {
//            System.out.println( "quoted:"+line.substring( from, i )+"<<" );
            a.quote();
	  }
          list.add( a );
          inword = false;
          continue;
        }

        // Not in a quoted word

        // we are inside a word
        // and this word contains a quote
        if ( inword )
        {
          continue;
        }

        // outside a word
        // a quoted word start here
        inquote = true;
        inword = false;
        if ( inbrace )
          continue;
        from = next;
        continue;
      }

      // the words doesn't matter if we collect a list
      // just the braces and the quote sign
      if ( inbrace )
      {
        continue;
      }



      if ( Spaces.whiteSpace(c) )
      {
        if ( inword )
        {
          // A whitespace terminate this word
          if ( !inbrace )
            // if we are in the top level
            // of this list
            list.add( getWord( line, from, i ) );
          inquote = false;
          inword = false;
        }
        continue;
      }

      if ( inword )
        // counting the non whitespace characters in a word
        continue;

      // not in a word
      // not a quote
      // not a white space
      // not a bracket
      // THEN
      // a word start here
      from = i;
      inword = true;
    }

    // end of String pending operations close here
    if ( !incomment )
    {
      if ( inbrace )
        throw new ParserException( "missing "+AtomList.CLOSE+" at line "+lineCount );

      if ( inquote )
      {
        Atom a = new Atom(line.substring( from, i ));
        if ( quoteAtoms )
          a.quote();
        list.add( a );
      }
      else if ( inword )
        list.add( getWord( line, from, i ) );
    }

    return list;

  }


  /**
   *  @return with an Atom from the line[from-til]
   */
  private Atom getWord( String line, int from, int til )
  {
//    System.out.println( "getWord:" + line.substring( from, til ) + "<" );
    // Maybe a quoted String
    if ( line.charAt( from ) == '\'' )
    {
      if ( til-from == 1 )
        // A single quote sign
        // accept as a quoted space
        return Atom.space;

      // one character at least
      if ( til > from )
      {
        // a single quoted string
        String word = line.substring( from+1, til );
        Atom a = new Atom( word );
        if ( quoteAtoms )
          a.quote();
        return a;
      }

    }
    // a normal not quoted String
    return new Atom( line.substring( from, til ) );
  }



}

