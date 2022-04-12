import javax.swing.*;
import java.awt.*;
import java.util.EventListener;



public class frame extends JFrame implements EventListener {
	private String tab1Name = "File Transfer";
	
	JCheckBox reliableornot = new JCheckBox("Check for Unreliable transfer");
	JButton ISALIVE = new JButton("ISALIVE?");
	JButton Send = new JButton("SEND");

	//1
    JLabel receiveripaddresslabel = new JLabel("Enter IP address of the receiver");
    JLabel receiverportlabel = new JLabel("Enter port number used by the receiver");
    JLabel senderportlabel = new JLabel("Enter port number of this application");
    JLabel textfilelabel = new JLabel("Enter name of file to be transferred");
    JLabel timeoutlabel = new JLabel("Enter the time in microseconds you would like the timeout to be (default is set at 1 second)");

    JTextField errorlabel = new JTextField();
	JTextField receiveripAddress = new JTextField();
	JTextField receiverportNum = new JTextField();
    JTextField senderportNum = new JTextField();
    JTextField textfilename = new JTextField();
    JTextField timeoutinput = new JTextField();
    
	JTextArea resultArea = new JTextArea();
    JScrollPane resultscroll = new JScrollPane(resultArea);
	
	//**** THIS IS WHAT MAKES THE TABS
	public void addComponentToPane(Container pane) {
        JTabbedPane tabbedPane = new JTabbedPane();
 
        //Create the "cards". (Cards are the tabs essentially)
        JPanel card1 = new JPanel() {
            //Make the panel wider than it really needs, so
            //the window's wide enough for the tabs to stay
            //in one row.
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                //size.width += ;
                return size;
            }
        };
        // card1.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 24));
        card1.setLayout(new BoxLayout(card1, BoxLayout.PAGE_AXIS));

        card1.add(receiveripaddresslabel);
        
        receiveripAddress.setPreferredSize(new Dimension( 300, 24 ));
        card1.add(receiveripAddress);
        
        card1.add(receiverportlabel);

        receiverportNum.setPreferredSize(new Dimension( 300, 24 ));
		card1.add(receiverportNum);

        card1.add(senderportlabel);

        senderportNum.setPreferredSize(new Dimension( 300, 20 ));
		card1.add(senderportNum);

        card1.add(textfilelabel);

        textfilename.setPreferredSize(new Dimension( 300, 20 ));
		card1.add(textfilename);

        card1.add(timeoutlabel);

        timeoutinput.setPreferredSize(new Dimension( 300, 20 ));
        card1.add(timeoutinput);
		
		ISALIVE.setPreferredSize(new Dimension(150,20));
        card1.add(ISALIVE);

        card1.add(reliableornot);
        
        Send.setPreferredSize(new Dimension(150,20));
		card1.add(Send);
        
        errorlabel.setVisible(true);
        errorlabel.setPreferredSize(new Dimension(400,20));
        errorlabel.setEditable(false);
        errorlabel.setForeground(new Color(255,0,0));
        card1.add(errorlabel);

        resultArea.setSize(new Dimension(400,200));
        resultArea.setEditable(false);
        resultArea.setForeground(new Color(255,255,255));
        resultArea.setBackground(new Color(0,0,0));
        resultArea.setRows(500);

        resultscroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        card1.add(resultscroll);

         tabbedPane.addTab(tab1Name, card1);
         pane.add(tabbedPane, BorderLayout.CENTER);
     }
	

	
	
	//opens the frame (container)
	public frame() {
		super("File Transfer Application");
		setSize(650,700);
        setResizable(false);
		addComponentToPane(super.getContentPane());
		setVisible(true);
	}
}