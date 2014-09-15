package app.bye;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.Iterator;
import java.awt.*;

import javax.swing.*;

public class Main
{
  static final long kiloByte = 1024;

  JFrame frame;
  Drawer drawer;

  /**
   * @param args
   */
  public static void main( String[] args )
  {
    Main m = new Main();
    // m.show();
    // m.copy();
    // m.path();
    // m.wget();
    m.disks();
  }


  void disks()
  {
    String format = "%-16s %-20s %-8s %-8s %12s %12s %12s\n";
    System.out.printf(format,"Name", "Filesystem", "Type",
    "Readonly", "Size(KB)", "Used(KB)",
    "Available(KB)");
    FileSystem fileSystem = FileSystems.getDefault();
    for (FileStore fileStore : fileSystem.getFileStores())
    {
      try
      {
        long totalSpace = fileStore.getTotalSpace() / kiloByte;
        long usedSpace = (fileStore.getTotalSpace() - fileStore.getUnallocatedSpace()) / kiloByte;
        long usableSpace = fileStore.getUsableSpace() / kiloByte;
        String name = fileStore.name();
        String type = fileStore.type();
        boolean readOnly = fileStore.isReadOnly();
        NumberFormat numberFormat = NumberFormat.getInstance();
        System.out.printf
        (
          format,
              name, fileStore, type, readOnly,
              numberFormat.format(totalSpace),
              numberFormat.format(usedSpace),
              numberFormat.format(usableSpace));
      }
      catch (IOException ex)
      {
        ex.printStackTrace();
      }
    }
  }
  

  void wget()
  {

    Path newFile = Paths.get( "sz.apk" );


    URI url = URI.create( "http://www.smallball.com/szotar.apk" );
    try ( InputStream inputStream = url.toURL().openStream() )
    {
      System.out.println("Downloading: "+url);
      Files.copy(inputStream, newFile);
      System.out.println("Downloaded!");
    }
    catch (MalformedURLException ex) {
    ex.printStackTrace();
    }
    catch (IOException ex) {
    ex.printStackTrace();
    }
    
  }
  
  void nums()
  {
    int n = 1_000_000_000;
    int b = 0b1111_1111_0000;
    System.out.println( "Welcome cruel world!");
    for( int i=0; i<10; i++ )
    {
      System.out.println( "i: "+i+" n: "+n+" b: "+b );
      n--;
    }
    System.out.println( "Good bye cruel world!");
  }
  
  void path()
  {
    Path tmp = Paths.get( "/tmp/laszlo/books" );
    try
    {
      tmp = Files.createDirectories( tmp );
      System.out.println( "Created: "+tmp.getFileName() );
    }
    catch( IOException ex )
    {
      System.err.println( "Cannot create: "+tmp );
    }
    Path path = FileSystems.getDefault().getPath
    (
      "/Users/laszlo/Documents/notesz.txt"
    );
    System.out.println();
    System.out.printf("toString: %s\n", path.toString());
    System.out.printf("getFileName: %s\n", path.getFileName());
    System.out.printf("getRoot: %s\n", path.getRoot());
    System.out.printf("getNameCount: %d\n", path.getNameCount());
    for(int index=0; index<path.getNameCount(); index++)
    {
      System.out.printf("getName(%d): %s\n", index, path.getName(index));
    }
    Iterator<Path> iterator = path.iterator();
    while(iterator.hasNext())
    {
      System.out.println( iterator.next() );
    }
    System.out.printf("subpath(0,2): %s\n", path.subpath(0, 2));
    System.out.printf("getParent: %s\n", path.getParent());
    System.out.println(path.isAbsolute());
  }
  
  void copy()
  {
    try
    (
        BufferedReader inputReader = Files.newBufferedReader
        (
          Paths.get( new URI("file:///C:/home/docs/users.txt")),
          Charset.defaultCharset()
        );
        BufferedWriter outputWriter = Files.newBufferedWriter
        (
          Paths.get(new URI("file:///C:/home/docs/users.bak")),
          Charset.defaultCharset()
        )
    )
    {
        String inputLine;
        while ((inputLine = inputReader.readLine()) != null)
        {
          outputWriter.write(inputLine);
          outputWriter.newLine();
        }
        System.out.println("Copy complete!");
    }
    catch (URISyntaxException | IOException ex) 
    {
        ex.printStackTrace();
    }
  }

  void show()
  {
    SwingUtilities.invokeLater( new Shower() );
  }
  
  class Shower implements Runnable
  {
    public void run()
    {
      frame = new JFrame( "karika" );
      drawer = new Drawer();
      frame.add( drawer );
      frame.pack();
      frame.setLocation( 400, 200 );
      frame.setVisible( true );
    }
  }
  
  class Drawer extends JComponent
  {
    Dimension preferedSize = new Dimension( 600, 400 );
    
    public Dimension getPreferredSize()
    {
      return preferedSize;
    }
    
    public void paint( Graphics g )
    {
      int xs = getWidth();
      int ys = getHeight();
      g.drawOval( 0, 0, xs, ys );
    }
    
  }

}
