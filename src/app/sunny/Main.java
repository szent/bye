package app.sunny;

import com.sun.net.httpserver.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.lang.reflect.*;

/**
 * A simple http server.
 * The client can connect through a http browser on the dedicated port.
 *
 * @author Laszlo Szenttornyai
 * @version 1.0, 11/23/2015
 */
public class Main
{

  public int port = 8080;
  public int backlog = 100;
  public Logger trace = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

  
  // last instantiated server
  public HttpHandler handler;

  // last made context
  HttpContext context;

  HttpServer server;
  
  // null means default executor
  Executor pool;

  WorkerThreads workers = new WorkerThreads();

  public static void main( String[] args )
    throws Exception
  {
    new Main( args );
  }

  /**
   * Prepare path with variable options.
   */
  public static String userPath( String macro )
  {
    if (macro == null || macro.length() == 0)
      return System.getProperty( "user.dir" );

    char c = macro.charAt( 0 );
    if (c == '~')
      return System.getProperty( "user.home" ) + macro.substring( 1 );
    if (c == '.' && macro.length() > 1 && macro.charAt( 1 ) != '.')
      return System.getProperty( "user.dir" ) + macro.substring( 1 );

    return macro;
  }

  public static String getPackageName()
  {
    return getPackageName( Main.class );
  }

  public static String getPackageName( Class<?> c )
  {
    String fullname = c.getName();
    int pointLoc = fullname.lastIndexOf( '.' );
    if (pointLoc == -1)
      return "";
    return fullname.substring( 0, pointLoc );
  }

  Main( String[] args )
    throws Exception
  {
    trace.info( "boot" );
    server = HttpServer.create();

    File auto = new File( "httpd.start" );
    if ( auto.exists() )
    {
      trace.log( Level.INFO, "execute", auto );
      BufferedReader reader;
      reader = new BufferedReader( new FileReader( auto ) );
      for(;;)
      {
        String line = reader.readLine();
        if ( line == null )
          break;
        line = line.trim();
        if ( line.isEmpty() || line.startsWith( "#" ) || line.startsWith( "//" ) )
          continue;
        if ( line.equals( "end" ) )
          break;
        exec( line );
      }
      reader.close();
    }
    if ( args.length > 0 )
    {
      split( args );
    }
    else
    {
      run();
    }
  }

  void run()
    throws Exception
  {
    System.out.println( "sun http server 1.03 ranged, zero length, last-modified" );
    System.out.println( "enter q to quit" );
    System.out.println( "enter up to serve" );
    SystemLineReader lines = new SystemLineReader();
    for(;;)
    {
      String line = lines.readLine();
      if ( "x".equals( line ) )
      {
        break;
      }
      exec( line );
    }
  }

  void exec( String line )
    throws Exception
  {
    StringTokenizer s = new StringTokenizer( line );
    int n = s.countTokens();
    String[] args = new String[n];
    for(int i = 0; i < n; i++)
    {
      args[i] = s.nextToken();
    }
    exec( args );
  }

  void split( String[] more )
    throws Exception
  {
    List<String> ls = new LinkedList<String>();
    for(String a : more)
    {
      if ( "::".equals( a ) )
      {
        String[] args = ls.toArray( new String[ls.size()] );
        ls.clear();
        exec( args );
      }
      else
      {
        ls.add( a );
      }
    }
    String[] args = ls.toArray( new String[ls.size()] );
    exec( args );
  }

  void exec( String[] args )
    throws Exception
  {
    int argc = args.length;
    if ( argc == 0 )
    {
      return;
    }

    try
    {
      if ( argc > 1 && "=".equals( args[1] ) )
      {
        FieldOf on = getField( args[0] );

        if ( on == null )
        {
          return;
        }

        Field field = on.field;
        if ( argc > 2 )
        {
          if ( field.getType().equals( int.class ) )
          {
            field.set( on.of, Integer.parseInt( args[2] )  );
          }
          else
          {
            field.set( on.of, args[2] );
          }
        }

        System.out.println( field.get( on.of ) );
        return;
      }
    }
    catch (NoSuchFieldException notFound)
    {
      trace.info( "no field like " + args[0] );
      return;
    }

    try
    {
      if ( argc == 1 )
      {
        Method m = Main.class.getMethod( args[0] + "Command" );
        m.invoke( this );
      }
      else if ( argc == 2 )
      {
        Method m = Main.class.getMethod( args[0] + "Command", String.class );
        m.invoke( this, args[1] );
      }
    }
    catch (NoSuchMethodException notFound)
    {
      trace.info( "what ? " + args[0] );
    }
  }

  FieldOf getField( String dottedName )
    throws Exception
  {
    if ( dottedName.indexOf( '.' ) == -1 )
    {
      return new FieldOf( Main.class.getField( dottedName ), this );
    }

    StringTokenizer s = new StringTokenizer( dottedName, "." );

    Field field = null;
    String name = null;
    Object on = this;
    while(s.hasMoreTokens())
    {
      if ( field != null )
      {
        on = field.get( on );
      }
      if ( on == null )
      {
        throw new Exception( name + " is null" );
      }
      name = s.nextToken();
      field = on.getClass().getField( name );
    }
    return new FieldOf( field, on );
  }

  public static String getListFields( Object o )
  {
    StringBuilder b = new StringBuilder();
    listFields( o, b );
    return b.toString();
  }

  public static void listFields( Object o, StringBuilder b )
  {
    if ( o == null )
    {
      b.append( "null" );
    }
    Field[] all = o.getClass().getFields();
    for(Field f : all)
    {
      try
      {
        b.append( f.getName() ).append( ':' ).append( f.get( o ) );
      }
      catch (Exception any)
      {
        // really dont care
      }
      b.append( '\n' );
    }
  }

  public void bindCommand()
  {
    try
    {
      InetSocketAddress addr = new InetSocketAddress( port );
      server.bind( addr, backlog );
    }
    catch( Exception any )
    {
      trace.log( Level.SEVERE, "cant handle", any );
    }
  }

  
  public void poolCommand()
  {
    if ( pool == null )
      trace.info( "default pool" );
    else
      trace.info( "pool :"+ pool.getClass() +" #"+ System.identityHashCode( pool ) );
  }

  public void fixpoolCommand( String arg )
  {
    int max = Integer.parseInt( arg );
    pool = Executors.newFixedThreadPool( max, workers );
  }

  public void cachedpoolCommand()
  {
    pool = Executors.newCachedThreadPool( workers );
  }
  
  public void defpoolCommand()
  {
    pool = null;
  }


  public void upCommand()
  {
    try
    {
      trace.log( Level.INFO, "starting", "port "+port );
      server.setExecutor( pool );
      server.start();
    }
    catch (Exception e)
    {
      trace.log( Level.SEVERE, "cant start", e );
      return;
    }

    trace.info( "up on port " + port + " pool #"+System.identityHashCode( server.getExecutor() ) );
  }

  public void qCommand()
  {
    System.exit( 0 );
  }

  public void fineCommand()
  {
    trace.setLevel( Level.FINE );
    trace.info( "Level "+trace.getLevel() );
    trace.fine( "Fine" );
  }

  public void handleCommand( String name )
  {
    String fullname = name;
    if ( name.contains( "." ) )
      fullname = name;
    else
      fullname = getPackageName()+"."+name;
    if ( !name.endsWith( "HttpHandler" ) )
      fullname = fullname+"HttpHandler";

    try
    {
      Class<?> c = Class.forName( fullname );
      handler = (HttpHandler)c.getDeclaredConstructor().newInstance();
//      handler = (HttpHandler)c.newInstance();
      trace.log( Level.INFO, "handler "+c+" ready to mount" );
    }
    catch( Exception any )
    {
      trace.log( Level.SEVERE, "cant handle", any );
    }
  }

  public void mountCommand( String path )
  {
    if ( handler == null )
    {
      trace.log( Level.INFO, "no handler" );
      return;
    }
    context = server.createContext( path, handler );
    if ( handler instanceof Made )
    {
      try
      {
        trace.info( "make" );
        ((Made)handler).madein(  this  );
      }
      catch( Exception any )
      {
        trace.log( Level.SEVERE, "cant handle", any );
      }
    }
    
    trace.log( Level.INFO, "handler "+handler+" mounted to "+path );
  }
}

class WorkerThreads implements Thread.UncaughtExceptionHandler, ThreadFactory  
{
  /**
   * Thread auto-name postfix.
   */
  int index;
  
  ThreadGroup group;

  WorkerThreads()
  {
    group = new ThreadGroup( "http-worker" );
  }

  public void uncaughtException( Thread t, Throwable e )
  {
    System.out.println( t.getName() );
    e.printStackTrace();
  }

  public Thread newThread(Runnable r)
  {
    index++;
    System.out.println( "new thread "+index );
    Thread t = new Thread( r, "http-worker-"+index );
    t.setUncaughtExceptionHandler( this );
    
    return t;
  }


}

interface Made
{
  void madein( Main main ) throws Exception;
}

class TimeHttpHandler implements HttpHandler
{
  @Override public void handle( HttpExchange exchange )
   throws IOException
  {
    String text = "millis: " + System.currentTimeMillis();
    byte[] response=text.getBytes();
    exchange.sendResponseHeaders( HttpURLConnection.HTTP_OK, response.length );
    exchange.getResponseBody().write( response );
    exchange.close();
  }
}

class CommandHttpHandler implements HttpHandler, Made
{
  int pathFrom;
  
  @Override public void handle( HttpExchange x )
   throws IOException
  {
    URI u = x.getRequestURI();
    String get = u.getPath();
    String cmd = get.substring( pathFrom );
    String ret = "Unknown command";
    
    if ( "down".equals( cmd ) )
    {
      ret = "Going down..";
    }
      
    byte[] response= ret.getBytes();
    x.sendResponseHeaders( HttpURLConnection.HTTP_OK, response.length );
    x.getResponseBody().write( response );
    x.close();
    
    if ( "down".equals( cmd ) )
    {
      System.out.println( "\nDown by request:"+u+" "+x.getRemoteAddress() );
      System.exit( 0 );
    }
  }
  
  public void madein( Main main )
    throws Exception
  {
    String mount = main.context.getPath();
    pathFrom = mount.length()+1;
  }
  
}

class EchoHttpHandler implements HttpHandler
{
  @Override public void handle( HttpExchange x )
   throws IOException
  {
    StringBuilder b = new StringBuilder();
    b.append( "Method: "+x.getRequestMethod()+"\r\n" );
    URI u = x.getRequestURI();
    b.append( "Path: "+u.getPath()+"\r\n" );
    b.append( " Raw: "+u.getRawPath()+"\r\n" );
    b.append( "Query: "+u.getQuery()+"\r\n" );
    b.append( "  Raw: "+u.getRawQuery()+"\r\n" );
    byte[] response=b.toString().getBytes();
    x.sendResponseHeaders( HttpURLConnection.HTTP_OK, response.length );
    x.getResponseBody().write( response );
    x.close();
  }
  
}

/**
 * Range: bytes=1073152-64313343
 * @author laszlo
 *
 */
class FileHttpHandler implements HttpHandler, Made
{
  public static final int BLOCK = 4096;

  public String docroot;
  Logger trace = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

  DateFormat dateFormat;
  int pathFrom;
  Path root;

  void filenotfound( HttpExchange x, String err )
   throws IOException
  {
    byte[] b = err.getBytes();
    x.sendResponseHeaders( 404, b.length );
    x.getResponseBody().write( b );
    x.close();
  }
  
  void sendfile( HttpExchange x, Path file )
   throws IOException
  {
    String method = x.getRequestMethod();
    long size = Files.size( file );
    Headers h = x.getRequestHeaders();
    String ranges = h.getFirst( "Range" );
    trace.info( "Range: "+ranges );
    long rangeStart = 0;
    long rangeCount = 0;
    boolean ranged = false;
    if ( ranges != null )
    {
      ranges.trim();
      if ( ranges.startsWith( "bytes" ) )
      {
        ranged = true;
        int at = ranges.indexOf( '=' );
        if ( at > 0 )
        {
          String nums = ranges.substring( at+1 ).trim();
          int dash = nums.indexOf( '-' );
          if ( dash < 0 )
          {
            // no dash, like:
            // bytes=500
            ranged = false;
          }
          else
          {
            String starts = nums.substring( 0, dash );
            String ends = nums.substring( dash+1 );
            if ( dash == 0 )
            {
              // bytes=-500
              // the final 500 byte
              rangeCount = Long.parseLong( nums.substring(1) );
              rangeStart = size - rangeCount;
            }
            else
            {
              if ( dash == nums.length()-1 )
              {
                // bytes=9500-
                // from 9500 until the end
                rangeStart = Long.parseLong( nums.substring( 0, dash ) );
                rangeCount = size-rangeStart;
              }
              else
              {
                // bytes=500-999
                // The second 500 bytes (byte offsets 500-999, inclusive)
                rangeStart = Long.parseLong( nums.substring(0,dash).trim() );
                rangeCount = Long.parseLong( nums.substring(dash+1).trim() ) - rangeStart;
                
              }
            }
          }
        }
      }
    }
      
    method = method.toLowerCase();

    FileTime time = Files.getLastModifiedTime( file );
    long mill = time.toMillis();
    Date date = new Date( mill );
    String daten;
    String dates;
    synchronized( dateFormat )
    {
      dates = dateFormat.format( date );
      
      daten = dateFormat.format( new Date() );
    }


    h = x.getResponseHeaders();
    h.set( "Date", daten );
    h.set( "Last-Modified", dates );

    if ( "head".equals( method ) )
    {
      h.set( "Content-Length", String.valueOf( size ) );
      x.sendResponseHeaders( 200, -1 );
    }
    else
    {
      if ( size == 0 )
      {
        h.set( "Content-Length", "0" );
        x.sendResponseHeaders( 200, -1 );
      }
      else
      {
        if ( ranged )
        {
          h.set( "Content-Length", String.valueOf( rangeCount ) );
          // Content-Range: bytes 734-1233/1234
          String ranger = "bytes "+rangeStart+"-"+rangeCount+"/"+size;
          trace.info( ranger );
          h.set( "Content-Range", ranger );
          if ( rangeStart == 0 && rangeCount+1 == size )
          {
            x.sendResponseHeaders( 206, size );
            Files.copy( file, x.getResponseBody() );
          }
          else
          {
            h.set( "Content-Length", String.valueOf( rangeCount ) );
            // Content-Range: bytes 734-1233/1234
            h.set( "Content-Range", "bytes "+rangeStart+"-"+rangeCount+"/"+size );
            FileInputStream in = new FileInputStream( file.toFile() );
            if ( rangeStart > 0 )
              in.skip( rangeStart );
            x.sendResponseHeaders( 206, rangeCount+1 );
            copy( in, x.getResponseBody(), rangeCount+1 );
            in.close();
          }
        }
        else
        {
          x.sendResponseHeaders( 200, size );
          Files.copy( file, x.getResponseBody() );
        }
      }
    }
    x.close();
  }

  public static void copy( InputStream in, OutputStream out, long length )
    throws IOException
  {
     byte[] block = new byte[ BLOCK ];
     long remain = length;
     long readed;
     for(;;)
     {
       if ( remain > BLOCK )
         readed = in.read( block );
       else
         readed = in.read( block, 0, (int)remain );
       if ( readed == -1 )
         throw new IOException( "unexpected end of file" );

       if ( readed == BLOCK )
         out.write( block );
       else
         out.write( block, 0, (int)readed );
       remain -= readed;
       if ( remain == 0 )
         break;
       if ( remain < 0 )
         throw new IOException( "unexpected end of file" );
     }
  }
  
  
  
  @Override public void handle( HttpExchange x )
   throws IOException
  {
//    trace.info( Thread.currentThread().getUncaughtExceptionHandler().toString() );
    URI u = x.getRequestURI();
    String get = u.getPath();
    trace.info( Thread.currentThread().getName()+" serving "+ get );
    long mill = System.currentTimeMillis();
    
    if ( get == null || get.length() < pathFrom )
    {
      filenotfound( x, "oops, "+get+" "+pathFrom );
      return;
    }

    trace.info( "uripath: "+get+" from "+pathFrom );
    String filepath = get.substring( pathFrom );
    Path path = root.resolve( filepath );
    trace.info( "Localpath: "+path );
    if ( Files.exists( path ) )
    {
      if ( Files.isDirectory( path ) )
      {
        Path index = path.resolve( "index.html" );
        if ( Files.exists( index ) )
          sendfile( x, index );
        else
          filenotfound( x, "directory" );
      }
      else
      {
        sendfile( x, path );
      }
    }
    else
    {
      filenotfound( x, get );
    }
    long ms = System.currentTimeMillis()-mill;
    trace.info( Thread.currentThread().getName()+" served in "+ms+"ms "+ get );
//    throw new IOException( "nem megyen" );
  }

  public void madein( Main main )
    throws Exception
  {
    String mount = main.context.getPath();

    int l = mount.length();
    pathFrom = l;
    if ( l > 1 )
      pathFrom++;

    root = FileSystems.getDefault().getPath( Main.userPath( docroot ) );
    if ( Files.exists( root ) )
    {
      main.trace.info( mount + " -> " + root );
    }
    else
      throw new FileNotFoundException( "no such path "+root );
    dateFormat = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss zzz" );
    dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

  }
  
}

/*
< HTTP/1.1 200 OKServer: twofish-files
< Date: Wed, 10 Aug 2011 10:11:47 GMT
< Content-Length: 3027271
< Content-Type: application/octet-stream
<

< HTTP/1.1 200 OK
< Server: Apache-Coyote/1.1
< Accept-Ranges: bytes
< ETag: W/"3027273-1312926671000"
< Last-Modified: Tue, 09 Aug 2011 21:51:11 GMT
< Content-Length: 3027273
< Date: Wed, 10 Aug 2011 10:46:10 GMT

 */
