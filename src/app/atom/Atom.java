package app.atom;

/**
 *  Atom is the atomic component of the lista processor.
 *  Atom is a wrapper of the Object embedded in this Atom.
 *  Every Atom has one and only one Object value.
 *  The value can be any Object.
 *  The nested Object is the value of the Atom.
 */
public class Atom
{
  /**
   *  An Atom with an Integer 0 value.
   *  The logical value is false;
   */
  public static final Atom zero = new Atom( 0 );

  /**
   *  An Atom with an Integer 1 value.
   */
  public static final Atom one = new Atom( 1 );

  /**
   *  An Atom with a single space as string value.
   */
  public static final Atom space;

  static
  {
    space = new Atom( " " );
    space.quote();
  }

  /**
   *  The <b>null</b> Atom.
   *  This Atom represents the null value.
   *  Its not equal with any of the other created Atom other than itself.
   *  Nor even another empty length list.
   *  The logical value is false.
   *  Its a list and a flat too.
   */
  public static final Atom nothing = new Atom( AtomList.empty );

  /**
   *  The embedded value.
   */
  private Object value;

  /**
   *  The depth of quoting.
   *  Value zero represents no quoting.
   */
  private int quotes;

  /**
   *  Create a nothing Atom.
   *  Create a non quoted AtomList.empty valued Atom.
   */
  public Atom()
  {
    value = AtomList.empty;
  }

  /**
   *  Create a non quoted Atom.
   * @param o the value of this Atom.
   */
  public Atom( Object o )
  {
    setValue(o);
  }

  /**
   *  Create another reference.
   * These two Atom will have the same value.
   * @param twin the refferable Atom.
   */
  public Atom( Atom twin )
  {
    this();
    setAs( twin );
  }

  /**
   *  Handling the quoted Atom.
   *  If this Atom not quoted return with the Atom itself.
   *  @return a clone of this Atom with one less quote
   */
  public Atom unQuote()
  {
    Atom ret = new Atom(this);
    if ( ret.quotes > 0 )
      ret.quotes--;
    return ret;
  }


  /**
   *  Quote this Atom.
   * Preserve this Atom from evaluation.
   * Increase the depth of quoting.
   */
  public void quote()
  {
    quotes++;
  }


  /**
   *  Make this Atom same as from Atom.
   *  Copy the value of the given Atom.
   *  @param from original Atom.
   */
  public void setAs( Atom from )
  {
    if ( from != null )
    {
      quotes = from.quotes;
      setValue( from.value );
    }
  }

  /**
   *  Get the embeded value.
   *  The quote not effect the return value.
   *  @return with the value of the Atom.
   */
  public Object getValue()
  {
    return value;
  }

  /**
   *  Assign the value.
   * @param o the embeded Object.
   */
  public void setValue( Object o )
  {
    value = o;
  }

  /**
   *  Check the state of qouting.
   * This Atom is quoted, if the level of qouation bigger than 0.
   * @return true if this Atom quoted.
   */
  public boolean isQuoted()
  {
    return quotes != 0;
  }

  /**
   *  Is the value of this Atom nil?
   *  This method check the value of Atom not the direct
   *  equality with the Atom.nil Object.
   */
  public boolean isNothing(){ return AtomList.empty.equals( value ); }


  /**
   *  Is the type of this Atom String?
   * @return true if not quoted and the value is String.
   */
  public boolean isString()
  {
    return !isQuoted() && value instanceof String;
  }

  public boolean isAtom()
  {
    if ( isNothing() )
      return true;
    if ( isList() )
      return false;
    return true;
  }

  /**
   *  Is this a List?
   * The nothing value is a List also!.
   * @return true if the value of this Atom is String.
   */
  public boolean isList()
  {
    return value instanceof AtomList;
  }

  /**
   *  Check logical state of this Atom.
   * @return false if the this is nil or if this is an Integer
   * with 0 value.
   * true if this is T.
   */
  public boolean isTrue()
  {
    if ( isNothing() )
      return false;
    if ( value == null )
      return true;

    if ( isInt() )
    {
      if ( intValue() == 0 )
        return false;
      return true;
    }

    return true;
  }


  /**
   *  Get the AtomList value.
   * @return if this Atom is not a List return with
   * a one element list contains this flat
   */
  public AtomList listValue()
  {
    if ( isList() )
      return (AtomList)value;
    AtomList list = new AtomList();
    list.add( this );
    return list;
  }


  /**
   *  Is this Atom an Integer?
   * @return true if the value is an Integer or
   * if the String representation can convert to an Integer.
   */
  public boolean isInt()
  {
    if ( value instanceof Integer )
      return true;

// if it can convert to an Integer, than this is an integer
    try
    {
      Integer.valueOf( value.toString() );
    }
    catch( NumberFormatException e )
    {
      return false;
    }
    return true;
  }

  /**
   *  Make this Atom as an Integer.
   * And set the value of this Integer.
   * @param i the value of this Integer.
   */
  public void setInt( int i )
  {
    value = i;
  }

  /**
   *  Get the integer representation of this Atom.
   * If the value is Integer return the int value of that.
   * The intValue of the T is 1. Return with 0, if the String value of this
   * Atom cannot convert to int.
   * @return the integer value of this Atom.
   */
  public int intValue()
  {
    return intValue(0);
  }
  public int intValue( int errorValue )
  {
    if ( value instanceof Integer )
      return ((Integer)value).intValue();

    if ( value == null || isNothing() )
      return errorValue;

    int retval = errorValue;
    try
    {
      retval = Integer.valueOf( value.toString() ).intValue();
    }
    catch( NumberFormatException e ){}
    return retval;
  }


  /**
   *  String representation of this Atom.
   * This representation is machine readable too.
   * The quotation marked as single quotes at the
   * beginning of the String.
   * @return the parseable String value of this Atom.
   */
  @Override
  public String toString()
  {
    if ( isNothing() )
      return AtomList.EMPTY;
    if ( isQuoted() )
    {
      StringBuilder b = new StringBuilder();
      for(int i=0; i<quotes; i++)
        b.append( '\'' );
      b.append( value );
      return b.toString();
    }
    else
      return value.toString();
  }

  public String toParseString()
  {
    String word = toString();
    if ( Spaces.whiteSpaced( word ) )
    {
      StringBuilder to = new StringBuilder();
      to.append( '"' );
      to.append( word );
      to.append( '"' );
      return to.toString();
    }
    return word;
  }

  public void appendParse( StringBuffer to )
  {
    if ( isList() )
      listValue().appendParse( to );
    else
    {
      String word = toString();
      if ( Spaces.whiteSpaced( word ) )
      {
        to.append( '"' );
        to.append( word );
        to.append( '"' );
      }
      else
        to.append( word );
    }
  }

  public void appendTo( StringBuffer to, int ident )
  {
    if ( isList() )
    {
      AtomList list = listValue();
      int n = list.size();
      Atom first = list.first();

      if ( first.isAtom() && n == 2 )
      {
        Atom second = list.second();
        Spaces.repTo( to, ident*2 );
        to.append( ':' );
        first.appendParse( to );
        if ( second.isAtom() )
        {
          to.append( ' ' );
          second.appendParse( to );
          to.append( ' ' );
        }
        else
        {
          to.append( '\n' );
          second.appendTo( to, ident+1 );
        }
      }
      else

        list.appendTo( to, ident );
    }
    else
    {
      to.append( toParseString() );
    }
  }

}
