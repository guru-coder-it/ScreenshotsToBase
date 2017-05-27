package screenshotstobase;
/**
 *
 * @author GuruCoder
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.swing.*;
import java.util.Calendar;
import java.util.Date;
import javax.imageio.ImageIO;
import java.awt.Rectangle; 
import java.awt.Robot;
import java.io.FileInputStream;
import java.sql.Blob;

// SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Platform.exit;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



class ThreadStopWatch {
    
    // JDBC URL, username and password of MySQL server
    String url = "jdbc:mysql://localhost:3306/base_screenshots";
    String user = "root";      // test - name database
    String password = "";
    
    
    // JDBC variables for opening and managing connection
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;
    
    SimpleDateFormat sdf;
    SimpleDateFormat sdt;
    Date currentDate;
    DefaultListModel listModel;
    BufferedImage screenShot;
    Robot robot;
    Blob blob = null;
    BufferedImage destImage = null;
    
    JFrame jfrmscreenshots; 
    JLabel jtime;   // Timer
    JLabel jlab;    // Show screenShot
    JLabel jtest;   // Text
    JLabel jintr;   // Text
    JLabel jmins;   // Text
    JLabel jdtvw;   // Text
    JButton jbtnStart;  // Start timer
    JButton jbtnStop;   // Stop timer
    JButton jviewscrsh;
    JComboBox jitr;
    JComboBox dtvw;
    JList jlst;
    JScrollPane jscrlp;
    long start; // Value start timer

    int param = 60; // 
    int prm = 1;

    String item = "";
    String dtvwval = "";
    String query = "";
    // Interval
    String interval[] = {"1", "5", "10", "30", "60"};

    ArrayList<String> dateview;
    String namefile = "";
    String dates = "";
    String times = "";
    String temp1 = "";
    String temp2 = "";
    String usercomp = "";
    String sdate = "";
    String binfile = "";
    
    Thread thrd; // Flow timer
    Thread scrn;
    Thread scsh;
    boolean running = false; // Value stopwatch
    boolean screening = false;
    boolean screensh = false;
    
    File file;
    FileInputStream fis = null;
    
    ThreadStopWatch() {
        
        
        sdf = new SimpleDateFormat("dd.MM.yy");
        sdt = new SimpleDateFormat("HH:mm:ss");
        // Title window 
        JFrame jfrm = new JFrame("Screenshots to base");
        jfrm.setIconImage(Toolkit.getDefaultToolkit().getImage
        ("image/icon.png"));
        jfrm.getContentPane().setLayout(new FlowLayout());
        jfrm.setBounds(5, 5, 300, 300);
        jfrm.setResizable(false);
        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        try {
            sdate = "";
            con = DriverManager.getConnection(url, user, password);
            String query = "select * from screenshot";
            Statement stmt = con.createStatement();                     
            ResultSet rs = stmt.executeQuery(query);
            dateview = new ArrayList<String>();
            while (rs.next()) {
                if(!sdate.equals(rs.getString(3)))
                dateview.add(rs.getString(3));
                sdate = rs.getString(3);
            }
            con.close();
        } catch (SQLException ex) {            
            int response = JOptionPane.showConfirmDialog(jfrm,
                    "Close program?",
                    "No connections to network or database!",
                    JOptionPane.CLOSED_OPTION);
            
            switch(response)
            {
                case 0:  
                System.exit(0);               
                break;
            }
            Logger.getLogger(ThreadStopWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
                

        // manager of the creation

        jtime = new JLabel("timer");
        jtest = new JLabel("");
        jintr = new JLabel("Interval: ");
        jmins = new JLabel(" min  ");
        jdtvw = new JLabel("Date view: ");
        jlab = new JLabel("");
        jlab.setSize(280, 180);
        jbtnStart = new JButton("Start");
        jbtnStop = new JButton("Stop");
        jviewscrsh = new JButton("View screenshot");
        jitr = new JComboBox(interval);
        jitr.setSelectedIndex(0);
        dtvw = new JComboBox(dateview.toArray());
        dtvw.setSelectedIndex(0);
        listModel = new DefaultListModel();
        jlst = new JList(listModel);
        jlst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jscrlp = new JScrollPane(jlst);
        jscrlp.setPreferredSize(new Dimension(290, 180));
        
        jbtnStop.setEnabled(false);
        jbtnStop.setVisible(true);
        jfrmscreenshots  = new JFrame("Screenshots");
        
    /**
     * Thread - timer, add line
     */        
        Runnable myThread = new Runnable() {
            @Override
            public void run() {
                try {
                    for(;;) {
                        Thread.sleep(100);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateTime();
                            }
                        });
                    }
                } catch(InterruptedException exc) {
                    System.out.println("Call to sleep was interrupted.");
                    System.exit(1);
                }
            }
            
        };
        thrd = new Thread(myThread);
        thrd.start();
        
    /**
     * Thread - save in MySQL
     */
        Runnable myScreen = new Runnable() {
            @Override
            public void run() {
                try {
                    for(;;) {
                        Thread.sleep(100);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateScreen();
                            }
                        });
                    }
                } catch(InterruptedException exc) {
                    System.out.println("Call to sleep was interrupted.");
                    System.exit(1);
                }
            }
            
        };
        scrn = new Thread(myScreen);
        scrn.start();

    jbtnStart.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            listModel.clear();
            param = 60;
            start = Calendar.getInstance().getTimeInMillis();
            jbtnStop.setEnabled(true);
            jbtnStart.setEnabled(false);
            jtest.setText("");
            running = true;
        }
    });
    /**
     * Stop screenshots save
     */    
    jbtnStop.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            long stop = Calendar.getInstance().getTimeInMillis();
            jbtnStop.setEnabled(false);
            jbtnStart.setEnabled(true);
            running = false;
            screening = false;
            screensh = false;
          
        // add Items in comboBox            
        int idxbox = dtvw.getItemCount();
        int indicis = listModel.getSize();
        String s = dtvw.getItemAt(idxbox-1).toString();
        for(int i=0; i < indicis; i++)
        {                
            String spd = listModel.elementAt(i).toString().substring(0,8);
            if(!s.equals(spd))
            {        
                dtvw.addItem(spd);
                s = spd;
            }
        }

        }
        
       
    });
      
    jitr.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            item = (String)jitr.getSelectedItem();
            prm = Integer.parseInt(item);
            param = 60*prm; 
        }
    });
    
    // Clicked Date view
    dtvw.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            dtvwval = (String)dtvw.getSelectedItem();
            listModel.clear();
    
    // reading database on date        
    try {
        con = DriverManager.getConnection(url, user, password);
        String query = "select * from screenshot where date='"+dtvwval+"'";
        Statement stmt = con.createStatement();                     
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
                listModel.addElement(rs.getString(3) +
            "   " + rs.getString(4)  +
            "   " + rs.getString(5));
            
        }
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ThreadStopWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        }
    });
    
    // Clicked button View screenshot
    jviewscrsh.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent ae) {     

        try {
            //PrintWriter outf = new PrintWriter("image/"+binfile+".jpg");
            
            con = DriverManager.getConnection(url, user, password);
            String query = "SELECT image FROM screenshot "
                    + "WHERE img = '"+binfile+"'";
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next())
            {
                blob = rs.getBlob("image");
                destImage = ImageIO.read(blob.getBinaryStream());
            }
            
            rs.close();
            pst.close();
            con.close();            
            
        jfrmscreenshots.setIconImage(Toolkit.getDefaultToolkit().getImage("image/icon.png"));
        jfrmscreenshots.getContentPane().setLayout(new FlowLayout());
        jfrmscreenshots.setBounds(0, 0, destImage.getWidth()+5, destImage.getHeight()+5);
        jfrmscreenshots.setResizable(false);
        jfrmscreenshots.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        jlab.setBounds(0, 0, destImage.getWidth(), destImage.getHeight());

        jfrmscreenshots.getContentPane().add(jlab);
        Image scaledImage = destImage.getScaledInstance
            (jlab.getWidth(),jlab.getHeight(), Image.SCALE_DEFAULT);
        ImageIcon imgIc = new ImageIcon(scaledImage); 
        jlab.setIcon(imgIc);
        jfrmscreenshots.setVisible(true);
            
        } catch (SQLException ex) {
            Logger.getLogger(ThreadStopWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
               
        catch (IOException ex) {
            Logger.getLogger(ThreadStopWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
               
        }
    }); 
    
    // select jList
    jlst.addListSelectionListener(new ListSelectionListener() {
    public void valueChanged(ListSelectionEvent le) { 
        int idx = jlst.getSelectedIndex();
        if(idx != -1)
        {
            binfile = listModel.getElementAt(idx).toString().substring(22,43);                     
        }
    } 
    }) ; 

    
    jfrm.getContentPane().add(jintr);
    jfrm.getContentPane().add(jitr);
    jfrm.getContentPane().add(jmins);
    jfrm.getContentPane().add(jbtnStart);
    jfrm.getContentPane().add(jbtnStop);
    jfrm.getContentPane().add(jtime);   
    jfrm.getContentPane().add(jtest);
    jfrm.getContentPane().add(jscrlp);
    jfrm.getContentPane().add(jdtvw);
    jfrm.getContentPane().add(dtvw);
    jfrm.getContentPane().add(jviewscrsh);    
    jfrm.setVisible(true);
}
    void updateTime() {
        currentDate = new Date();
        jtime.setText("Date: "+String.valueOf(sdf.format(currentDate)) +
                "    Time: " + String.valueOf(sdt.format(currentDate)));
        if(!running) return;
        long temp = Calendar.getInstance().getTimeInMillis();
        if(((double)(temp - start)/1000) > param) {
        screensave();
        param += 60*prm;
        }
    }
    
    void screensave() {
        
        dates = String.valueOf(sdf.format(currentDate));
        times = String.valueOf(sdt.format(currentDate));
        temp1 = String.valueOf(sdf.format(currentDate)).replace('.', '_').trim();
        temp2 = String.valueOf(sdt.format(currentDate)).replace(':', '_').trim();
        namefile = ("img_"+temp1+"_"+temp2).trim();
        listModel.addElement(String.valueOf(sdf.format(currentDate)) +
                "   " + String.valueOf(sdt.format(currentDate))  +
                "   " + namefile);
        screening = true;
    }
    
    
    void updateScreen() {
        
        if(!screening) return;
        try {
            robot = new Robot();
            screenShot = robot.createScreenCapture(new Rectangle
            (Toolkit.getDefaultToolkit().getScreenSize()));
            try {
                ImageIO.write(screenShot, "JPG", new File("image/temp.jpg"));
                file = new File("image/temp.jpg");
                fis = new FileInputStream(file);
            } catch (IOException ex) {
                Logger.getLogger(ThreadStopWatch.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (AWTException ex) {
            Logger.getLogger(ThreadStopWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
        
               
        try {

            usercomp = System.getenv("USERNAME");
                     
            con = DriverManager.getConnection(url, user, password);
            String INSERT_PICTURE = "insert into screenshot(id, user, date, time, img, image)"
                    + " values (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement ps = null;
            con.setAutoCommit(false);
            ps = con.prepareStatement(INSERT_PICTURE);
            
            ps.setNull(1, 0);
            ps.setString(2, usercomp);
            ps.setString(3, dates);
            ps.setString(4, times);
            ps.setString(5, namefile);            
            ps.setBlob(6, fis, (int) file.length());
            
            ps.executeUpdate();
            con.commit();
            ps.close();
            con.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(ThreadStopWatch.class.getName()).log(Level.SEVERE, null, ex);
        }      
        screening = false;
    }    
}

public class ScreenshotsToBase {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ThreadStopWatch();
            }
        });
    }
    
}
