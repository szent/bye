package app.atom;

import java.util.*;

public class NamedAtomList
{
  /**
   * The name of this list.
   */
  protected String name = "";

  /**
   * The list of bindings of the pairs.
   */
  protected Hashtable names = new Hashtable();

  public NamedAtomList( AtomList list )
  {
    set( list );
  }

  public NamedAtomList( AtomList list, boolean named )
  {
    set( list, named );
  }

  public boolean defined( String name )
  {
    return names.get(name) != null;
  }

  public AtomList get(String name)
  {
    return (AtomList)names.get(name);
  }

  public String[] getNames()
  {
    String[] allname = new String[ names.size() ];
    Enumeration en = names.keys();
    int i = 0;
    while( en.hasMoreElements() )
      allname[i++] = (String)en.nextElement();
    return allname;

  }

  public String getName()
  {
    return name;
  }

  public void set( AtomList list )
  {
    set( list, true );
  }

  public void set( AtomList list, boolean named )
  {
    names.clear();
    if ( named )
      name = list.first().toString();
    else
      name = "";
    int n = list.size();
    for(int i=0; i<n; i++)
    {
      Atom a = list.get(i);
      if ( a.isList() )
      {
        AtomList part = a.listValue();
        Atom first = part.first();
        if ( first.isString() )
          names.put( first.toString(), part.rest() );
      }
    }
  }
}
