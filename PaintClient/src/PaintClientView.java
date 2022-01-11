
// JavaObjClientView.java ObjecStram 기반 Client
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class PaintClientView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtInput;
	private String UserName;
	private JButton btnSend;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private JLabel lblUserName;
	private JTextPane textArea;

	private Frame frame;
	private FileDialog fd;
	private JButton imgBtn;

	JPanel panel;
	private JLabel lblMouseEvent;
	private Graphics2D gc;
	private int pen_size = 2; // minimum 2
	private Color pen_color = Color.BLACK;
	private int x1, y1, x2, y2;
	
	// 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
	private Image panelImage = null; 
	private Image tmpImage = null; 
	private Graphics2D gc2 = null;
	private Graphics2D gc3 = null;

	//추가
	private JButton imoticon;
	private JButton savePenalImg;
	private JButton textBoxBtn;
	
	private MyBtn freeBtn;
	private MyBtn rectBtn;
	private MyBtn fillRectBtn;
	private MyBtn ovalBtn;
	private MyBtn fillOvalBtn;
	private MyBtn lineBtn;

	private JButton clearBtn;
	private JButton redBtn;
	private JButton greenBtn;
	private JButton blueBtn;
	private JButton blackBtn;
	private JLabel lbPenSize;
	private Image ii = null;	
	private int positionX=0;
	private String shape;

	private ImoticonFrame imoticonFrame;
	private ImoticonThread imoticonThread;
	private Image img;
	private String path;
	private String textBoxWord;
	
	class MyBtn extends JButton {
		MyBtn(ImageIcon icon) {
			setBounds(12+(positionX++)*47, 589, 34,30);
			setIcon(icon);
			setBackground(Color.WHITE);
		}
	}
	
	//지우개 기능 시 사용. 패널을 다시 그린다.
	public void createGraphics2() {
		panelImage = createImage(panel.getWidth(), panel.getHeight());
		gc2 = (Graphics2D)panelImage.getGraphics();
		gc2.setColor(panel.getBackground());
		gc2.fillRect(0,0, panel.getWidth(),  panel.getHeight());
		gc2.setColor(Color.BLACK);
		gc2.drawRect(0,0, panel.getWidth()-1,  panel.getHeight()-1);
	}

	public PaintClientView(String username, String ip_addr, String port_no)  {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 680);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 352, 471);
		contentPane.add(scrollPane);

		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(74, 489, 209, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
		btnSend.setBounds(295, 489, 69, 40);
		contentPane.add(btnSend);

		lblUserName = new JLabel("Name");
		lblUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblUserName.setBackground(Color.WHITE);
		lblUserName.setFont(new Font("굴림", Font.BOLD, 14));
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(5, 539, 62, 40);
		contentPane.add(lblUserName);
		setVisible(true);

		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		lblUserName.setText(username);

		imgBtn = new JButton("+");
		imgBtn.setFont(new Font("굴림", Font.PLAIN, 16));
		imgBtn.setBounds(12, 489, 50, 40);
		contentPane.add(imgBtn);

		JButton btnNewButton = new JButton("종 료");
		btnNewButton.setFont(new Font("굴림", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "400", "Bye");
				SendObject(msg);
				System.exit(0);
			}
		});
		btnNewButton.setBounds(295, 539, 69, 40);
		contentPane.add(btnNewButton);

		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBackground(Color.WHITE);
		panel.setBounds(376, 10, 400, 520);
		contentPane.add(panel);

		gc = (Graphics2D) panel.getGraphics();

		createGraphics2();

		tmpImage = createImage(panel.getWidth(), panel.getHeight());
		gc3 = (Graphics2D)tmpImage.getGraphics();
		gc3.setColor(panel.getBackground());
		gc3.fillRect(0,0, panel.getWidth(),  panel.getHeight());
		gc3.setColor(Color.BLACK);
		gc3.drawRect(0,0, panel.getWidth()-1,  panel.getHeight()-1);

		
		lblMouseEvent = new JLabel("<dynamic>");
		lblMouseEvent.setHorizontalAlignment(SwingConstants.CENTER);
		lblMouseEvent.setFont(new Font("굴림", Font.BOLD, 14));
		lblMouseEvent.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblMouseEvent.setBackground(Color.WHITE);
		lblMouseEvent.setBounds(376, 539, 400, 40);
		contentPane.add(lblMouseEvent);

		//추가
		//이모티콘 버튼
		imoticon = new JButton("이모티콘");
		imoticon.setFont(new Font("굴림", Font.PLAIN, 12));
		imoticon.setBounds(67, 539, 80, 40);
		imoticon.addActionListener(new ImoticonActionListener());
		contentPane.add(imoticon);

		//Text Box 버튼
		textBoxBtn = new JButton("Text box");
		textBoxBtn.setFont(new Font("굴림", Font.PLAIN, 12));
		textBoxBtn.setBounds(144, 539, 87, 40);
		contentPane.add(textBoxBtn);
		
		//Save 버튼
		savePenalImg = new JButton("Save");
		savePenalImg.setBounds(230, 539, 62, 40);
		savePenalImg.setFont(new Font("굴림", Font.PLAIN, 12));
		contentPane.add(savePenalImg);

		//자유 드로잉 버튼
		freeBtn = new MyBtn(new ImageIcon(".\\freeBtnImg.jpg"));
		freeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shape = "free";
				freeBtn.setBackground(Color.BLACK);
				rectBtn.setBackground(Color.WHITE);
				fillRectBtn.setBackground(Color.WHITE);
				ovalBtn.setBackground(Color.WHITE);
				fillOvalBtn.setBackground(Color.WHITE);
				lineBtn.setBackground(Color.WHITE);
				ChatMsg msg = new ChatMsg(UserName, "600", "Select Shape");
				msg.shape = shape;
				SendObject(msg);
			}
		});
		contentPane.add(freeBtn);

		//빈 사각형 버튼
		rectBtn = new MyBtn(new ImageIcon(".\\rectBtnImg.jpg"));
		rectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shape = "rect";
				freeBtn.setBackground(Color.WHITE);
				rectBtn.setBackground(Color.BLACK);
				fillRectBtn.setBackground(Color.WHITE);
				ovalBtn.setBackground(Color.WHITE);
				fillOvalBtn.setBackground(Color.WHITE);
				lineBtn.setBackground(Color.WHITE);
				ChatMsg msg = new ChatMsg(UserName, "600", "Select Shape");
				msg.shape = shape;
				SendObject(msg);
			}
		});
		contentPane.add(rectBtn);

		//채워진 사각형 버튼
		fillRectBtn = new MyBtn(new ImageIcon(".\\fillRectBtnImg.jpg"));
		fillRectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shape = "fillRect";
				freeBtn.setBackground(Color.WHITE);
				rectBtn.setBackground(Color.WHITE);
				fillRectBtn.setBackground(Color.BLACK);
				ovalBtn.setBackground(Color.WHITE);
				fillOvalBtn.setBackground(Color.WHITE);
				lineBtn.setBackground(Color.WHITE);
				ChatMsg msg = new ChatMsg(UserName, "600", "Select Shape");
				msg.shape = shape;
				SendObject(msg);
			}
		});
		contentPane.add(fillRectBtn);

		//빈 원 버튼
		ovalBtn = new MyBtn(new ImageIcon(".\\ovalBtnImg.jpg"));
		ovalBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shape = "oval";
				freeBtn.setBackground(Color.WHITE);
				rectBtn.setBackground(Color.WHITE);
				fillRectBtn.setBackground(Color.WHITE);
				ovalBtn.setBackground(Color.BLACK);
				fillOvalBtn.setBackground(Color.WHITE);
				lineBtn.setBackground(Color.WHITE);
				ChatMsg msg = new ChatMsg(UserName, "600", "Select Shape");
				msg.shape = shape;
				SendObject(msg);
			}
		});
		contentPane.add(ovalBtn);

		//채워진 원 버튼
		fillOvalBtn = new MyBtn(new ImageIcon(".\\fillOvalBtnImg.jpg"));
		fillOvalBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shape = "fillOval";
				freeBtn.setBackground(Color.WHITE);
				rectBtn.setBackground(Color.WHITE);
				fillRectBtn.setBackground(Color.WHITE);
				ovalBtn.setBackground(Color.WHITE);
				fillOvalBtn.setBackground(Color.BLACK);
				lineBtn.setBackground(Color.WHITE);
				ChatMsg msg = new ChatMsg(UserName, "600", "Select Shape");
				msg.shape = shape;
				SendObject(msg);
			}
		});
		contentPane.add(fillOvalBtn);

		//직선 버튼
		lineBtn = new MyBtn(new ImageIcon(".\\lineBtnImg.jpg"));
		lineBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shape = "line";
				freeBtn.setBackground(Color.WHITE);
				rectBtn.setBackground(Color.WHITE);
				fillRectBtn.setBackground(Color.WHITE);
				ovalBtn.setBackground(Color.WHITE);
				fillOvalBtn.setBackground(Color.WHITE);
				lineBtn.setBackground(Color.BLACK);
				ChatMsg msg = new ChatMsg(UserName, "600", "Select Shape");
				msg.shape = shape;
				SendObject(msg);
			}
		});
		contentPane.add(lineBtn);

		//지우개 버튼
		clearBtn = new JButton("Clear");
		clearBtn.setFont(new Font("굴림", Font.PLAIN, 12));
		clearBtn.setBounds(295, 589, 69, 30);
		clearBtn.setBackground(Color.WHITE);
		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(ii != null) {
					gc2.drawImage(ii,  0,  0, panel.getWidth(), panel.getHeight(), panel);
					gc.drawImage(panelImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);

				} else {
					gc2.clearRect(0,0, panel.getWidth(),  panel.getHeight());
					createGraphics2();

					panel.revalidate();
					panel.repaint();

				}
			}
		});
		contentPane.add(clearBtn);


		//색 선택 버튼들
		redBtn = new JButton("Red");
		redBtn.setFont(new Font("굴림", Font.PLAIN, 12));
		redBtn.setBounds(376, 589, 69, 30);
		redBtn.setBackground(Color.RED);
		redBtn.addActionListener(new MyActionListener());
		contentPane.add(redBtn);

		greenBtn = new JButton("Green");
		greenBtn.setFont(new Font("굴림", Font.PLAIN, 12));
		greenBtn.setBounds(457, 589, 69, 30);
		greenBtn.setBackground(Color.GREEN);
		greenBtn.addActionListener(new MyActionListener());
		contentPane.add(greenBtn);

		blueBtn = new JButton("Blue");
		blueBtn.setFont(new Font("굴림", Font.PLAIN, 12));
		blueBtn.setBounds(538, 589, 69, 30);
		blueBtn.setBackground(Color.BLUE);
		blueBtn.setForeground(Color.WHITE);
		blueBtn.addActionListener(new MyActionListener());
		contentPane.add(blueBtn);

		blackBtn = new JButton("Black");
		blackBtn.setFont(new Font("굴림", Font.PLAIN, 12));
		blackBtn.setBounds(619, 589, 69, 30);
		blackBtn.setBackground(Color.BLACK);
		blackBtn.setForeground(Color.WHITE);
		blackBtn.addActionListener(new MyActionListener());
		contentPane.add(blackBtn);

		lbPenSize = new JLabel("   Pen="+pen_size);
		lbPenSize.setFont(new Font("굴림", Font.PLAIN, 12));
		lbPenSize.setBounds(707, 589, 69, 30);
		lbPenSize.setBorder(new LineBorder(new Color(0, 0, 0),2));
		contentPane.add(lbPenSize);
		

		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello");
			SendObject(obcm);

			ListenNetwork net = new ListenNetwork();
			net.start();
			TextSendAction action = new TextSendAction();
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.requestFocus();
			
			//+ 버튼 선택 시 실행
			ImageSendAction action2 = new ImageSendAction();
			imgBtn.addActionListener(action2);

			//Save 버튼 선택 시 실행
			ImageSaveAction action3 = new ImageSaveAction();
			savePenalImg.addActionListener(action3);

			//Text Box 버튼 선택 시 실행
			DrawTextBoxAction action4 = new DrawTextBoxAction();
			textBoxBtn.addActionListener(action4);

			//MouseEvent 발생 시 실행
			MyMouseEvent mouse = new MyMouseEvent();
			panel.addMouseMotionListener(mouse);
			panel.addMouseListener(mouse);
 
			//MouseWheelEvent 발생 시 실행
			MyMouseWheelEvent wheel = new MyMouseWheelEvent();
			panel.addMouseWheelListener(wheel);


		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}

	}

	//컬러 버튼들 선택 시 실행
	class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton)e.getSource();
			String btnColor = btn.getText();
			switch(btnColor) {
				case "Red": pen_color = new Color(255, 0, 0); break;
				case "Green": pen_color = new Color(0, 255, 0); break;
				case "Blue": pen_color = new Color(0, 0, 255); break;
				default : pen_color = new Color(0, 0, 0);
			}
			ChatMsg msg = new ChatMsg(UserName, "700", "Select Pen Color");
			msg.pen_color = pen_color;
			SendObject(msg);
		}
	}

	//그릴 텍스트를 입력받기 위한 다이얼로그가 뜨고 텍스트의 내용과 textBox라는 shpae 송신
	class DrawTextBoxAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			shape = "textBox";
			
			textBoxWord = JOptionPane.showInputDialog("단어를 입력하세요.");
			ChatMsg msg = new ChatMsg(UserName, "500", "Text Box");
			msg.shape = shape;
			msg.textBoxWord = textBoxWord;
			SendObject(msg);
		}
	}
	
	
	//이모티콘 버튼 선택 시 실행. 이모티콘이 나열되어있는 프레임이 뜬다.
	class ImoticonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			imoticonFrame = new ImoticonFrame();

			imoticonThread = new ImoticonThread();
			imoticonThread.start();
		}
	}
	
	//이모티콘이 선택되면 이모티콘 창이 닫긴다.
	class ImoticonThread extends Thread {
		@Override
		public void run() {
			while(true) {
				try {
					path = imoticonFrame.getChatMsg();
					if(path == null) {}
					else if(!path.equals("")) {
						img = new ImageIcon(path).getImage();
						shape = "imoticon";
						ChatMsg msg = new ChatMsg(UserName, "500", "Send Imoticon");
						msg.shape = shape;
						msg.path = path;
						SendObject(msg);
						break;
					}
				} catch(Exception e) {
					break;
				}
			}
		}
	}
	
	public void paint(Graphics2D g) {
		super.paint(g);
		// Image 영역이 가려졌다 다시 나타날 때 그려준다.
		gc.drawImage(panelImage, 0, 0, this);
	}

	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s]\n%s", cm.UserName, cm.data);
					} else
						continue;
					switch (cm.code) {
					case "200": // chat message
						if (cm.UserName.equals(UserName))
							AppendTextR(msg); // 내 메세지는 우측에
						else
							AppendText(msg);
						break;
					case "300": // Image 첨부
						if (cm.UserName.equals(UserName)) //나
							AppendTextR("[" + cm.UserName + "]");
						else //나 빼고 다른 유저
							AppendText("[" + cm.UserName + "]");
						AppendImage(cm.img);
						break;
					case "500": // Mouse Event 수신
						DoMouseEvent(cm);
						break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			}

		}
	}

	// Mouse Event 수신 처리
	public void DoMouseEvent(ChatMsg cm) {
		if (cm.UserName.matches(UserName)) // 본인 것은 이미 Local 로 그렸다.
			return; 
		gc2.setColor(cm.pen_color); //다른 사용자 색 설정
		gc2.setStroke(new BasicStroke(cm.pen_size, BasicStroke.CAP_ROUND, 0));

		if(cm.shape == null || cm.shape.equals("free")) { //free
			if(cm.mouse_e.getID() == MouseEvent.MOUSE_PRESSED) {
				x1 = cm.mouse_e.getX();
				y1 = cm.mouse_e.getY();
			}
			x2 = cm.mouse_e.getX();
			y2 = cm.mouse_e.getY();
			gc2.drawLine(x1, y1, x2, y2);
			gc.drawLine(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
			gc.drawImage(panelImage, 0, 0, panel);
		}
		else if(cm.shape.equals("rect")) {
			x1 = cm.x1;
			y1 = cm.y1;
			x2 = cm.x2;
			y2 = cm.y2;
			gc2.drawRect(x1, y1, x2-x1, y2-y1);
			gc.drawImage(panelImage, 0, 0, panel);
		}
		else if(cm.shape.equals("fillRect")) {
			x1 = cm.x1;
			y1 = cm.y1;
			x2 = cm.x2;
			y2 = cm.y2;
			gc2.fillRect(x1, y1, x2-x1, y2-y1);
			gc.drawImage(panelImage, 0, 0, panel);
		}
		else if(cm.shape.equals("oval")) {
			x1 = cm.x1;
			y1 = cm.y1;
			x2 = cm.x2;
			y2 = cm.y2;
			gc2.drawOval(x1, y1, x2-x1, y2-y1);
			gc.drawImage(panelImage, 0, 0, panel);
		}
		else if(cm.shape.equals("fillOval")) {
			x1 = cm.x1;
			y1 = cm.y1;
			x2 = cm.x2;
			y2 = cm.y2;
			gc2.fillOval(x1, y1, x2-x1, y2-y1);
			gc.drawImage(panelImage, 0, 0, panel);
		}
		else if(cm.shape.equals("line")) {
			x1 = cm.x1;
			y1 = cm.y1;
			x2 = cm.x2;
			y2 = cm.y2;
			gc2.drawLine(x1, y1, x2, y2);
			gc.drawImage(panelImage, 0, 0, panel);
		}
		else if(cm.shape.equals("imoticon")) {
			if (cm.UserName.matches(UserName)) // 본인 것은 이미 Local 로 그렸다.
				return; 
			Image img = new ImageIcon(cm.path).getImage();
			x1 = cm.x1;
			y1 = cm.y1;
			x2 = cm.x2;
			y2 = cm.y2;
			gc2.drawImage(img, x1, y1, x2-x1, y2-y1, panel);
			gc.drawImage(panelImage, 0, 0, panel);
		}
		else if(cm.shape.equals("textBox")) {
			x1 = cm.x1;
			y1 = cm.y1;
			gc2.setFont(new Font("Gothic", Font.PLAIN, cm.pen_size+10));
			gc2.drawString(cm.textBoxWord, x1, y1);
			gc.drawImage(panelImage, 0, 0, panel);
		}
	}

	public void SendMouseEvent(MouseEvent e) {
		ChatMsg cm = new ChatMsg(UserName, "500", "MOUSE");
		cm.mouse_e = e;
		cm.pen_size = pen_size;
		cm.pen_color = pen_color;
		cm.shape = shape;
		cm.path = path;
		cm.textBoxWord = textBoxWord;
		cm.x1 = x1;
		cm.y1 = y1;
		cm.x2 = x2;
		cm.y2 = y2;
		SendObject(cm);
	}

	//펜 사이즈 조절
	class MyMouseWheelEvent implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			if (e.getWheelRotation() < 0) { // 위로 올리는 경우 pen_size 증가
				if (pen_size < 20)
					pen_size++;
			} else {
				if (pen_size > 2)
					pen_size--;
			}
			lblMouseEvent.setText("mouseWheelMoved Rotation=" + e.getWheelRotation() 
			+ " " + e.getX() + "," + e.getY());

			lbPenSize.setText("   Pen = " + pen_size);
		
			
			ChatMsg msg = new ChatMsg(UserName, "800", "Change Pen Size");
			msg.pen_size = pen_size;
			SendObject(msg);
		}
	}


	// Mouse Event Handler
	class MyMouseEvent implements MouseListener, MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseDragged " + e.getX() + "," + e.getY());// 좌표출력가능
			gc2.setColor(pen_color);
			gc.setStroke(new BasicStroke(pen_size, BasicStroke.CAP_ROUND, 0));

			x2 = e.getX();
			y2 = e.getY();

			if(shape == null || shape.equals("free")) { // Free & default
				gc2.drawLine(x1, y1, x2, y2);
				x1 = x2;
				y1 = y2;
				gc.drawImage(panelImage, 0, 0, panel);
				SendMouseEvent(e);
			}
			else if(shape.equals("rect")) {
				gc.drawImage(tmpImage, 0, 0, panel);
				gc2.drawImage(tmpImage, 0, 0, panel);
				gc.drawRect(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("fillRect")) {
				gc.drawImage(tmpImage, 0, 0, panel);
				gc2.drawImage(tmpImage, 0, 0, panel);
				gc.fillRect(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("oval")) {
				gc.drawImage(tmpImage, 0, 0, panel);
				gc2.drawImage(tmpImage, 0, 0, panel);
				gc.drawOval(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("fillOval")) {
				gc.drawImage(tmpImage, 0, 0, panel);
				gc2.drawImage(tmpImage, 0, 0, panel);
				gc.fillOval(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("line")) {
				gc.drawImage(tmpImage, 0, 0, panel);
				gc2.drawImage(tmpImage, 0, 0, panel);
				gc.drawLine(x1, y1, x2, y2);
			}
			else if(shape.equals("imoticon")) {
				gc.drawImage(tmpImage, 0, 0, panel);
				gc2.drawImage(tmpImage, 0, 0, panel);
				gc.drawImage(img, x1, y1, x2-x1, y2-y1, panel);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mousePressed " + e.getX() + "," + e.getY());
			gc.setStroke(new BasicStroke(pen_size, BasicStroke.CAP_ROUND, 0));
			if(shape == null || shape.equals("free"))  { //free & default
				x1 = e.getX();
				y1 = e.getY();
				SendMouseEvent(e);
			}
			else if(shape.equals("textBox")) {
				gc2.setColor(pen_color);
				x1 = e.getX();
				y1 = e.getY();
				gc2.setFont(new Font("Gothic", Font.PLAIN, pen_size+10));
				gc2.drawString(textBoxWord, x1, y1);
				gc.drawImage(panelImage, 0, 0, panel);
				SendMouseEvent(e);
			}
			else {
				tmpImage = panelImage;
				gc.setColor(pen_color);

				gc3.drawImage(panelImage, 0, 0, panel);
				x1 = e.getX();
				y1 = e.getY();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseReleased " + e.getX() + "," + e.getY());
			gc2.setColor(pen_color);
			gc2.setStroke(new BasicStroke(pen_size, BasicStroke.CAP_ROUND, 0));

			x2 = e.getX();
			y2 = e.getY();

			if(shape == null) {}
			else if(shape.equals("rect")) {
				gc2.drawRect(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("fillRect")) {
				gc2.fillRect(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("oval")) {
				gc2.drawOval(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("fillOval")) {
				gc2.fillOval(x1, y1, x2-x1, y2-y1);
			}
			else if(shape.equals("line")){
				gc2.drawLine(x1, y1, x2, y2);
			}
			else if(shape.equals("imoticon")) {
				gc2.drawImage(img, x1, y1, x2-x1, y2-y1, panel);
			}
			SendMouseEvent(e);
			gc.drawImage(panelImage, 0, 0, panel);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseMoved " + e.getX() + "," + e.getY());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseClicked " + e.getX() + "," + e.getY());
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseEntered " + e.getX() + "," + e.getY());
		}

		@Override
		public void mouseExited(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseExited " + e.getX() + "," + e.getY());
		}
	}


	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", UserName, txtInput.getText());
				msg = txtInput.getText();
				SendMessage(msg);
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료 처리
					System.exit(0);
			}
		}
	}

	class ImageSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 액션 이벤트가 sendBtn일때 또는 textField 에세 Enter key 치면
			if (e.getSource() == imgBtn) {
				frame = new Frame("이미지첨부");
				fd = new FileDialog(frame, "이미지 선택", FileDialog.LOAD);
				fd.setVisible(true);
				
				if (fd.getDirectory().length() > 0 && fd.getFile().length() > 0) {
					ChatMsg obcm = new ChatMsg(UserName, "300", "IMG");
					ImageIcon img = new ImageIcon(fd.getDirectory() + fd.getFile());
					obcm.img = img;
					SendObject(obcm);
				}
			}
		}
	}

	class ImageSaveAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == savePenalImg) {
				frame = new Frame("이미지저장");
				fd = new FileDialog(frame, "이미지 저장", FileDialog.SAVE);
				fd.setVisible(true);
				String path = fd.getDirectory() + fd.getFile();
				File file = new File(path);
				try {
					ImageIO.write((BufferedImage) panelImage, "jpg", file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	

	ImageIcon icon1 = new ImageIcon("src/icon1.jpg");

	public void AppendIcon(ImageIcon icon) {
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.insertIcon(icon);
	}

	// 화면에 출력
	public void AppendText(String msg) {
		// textArea.append(msg + "\n");
		// AppendIcon(icon1);
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		//textArea.setCaretPosition(len);
		//textArea.replaceSelection(msg + "\n");

		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);
		doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), msg+"\n", left );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		//textArea.replaceSelection("\n");


	}
	// 화면 우측에 출력
	public void AppendTextR(String msg) {
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.	
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setForeground(right, Color.BLUE);	
		doc.setParagraphAttributes(doc.getLength(), 1, right, false);
		try {
			doc.insertString(doc.getLength(),msg+"\n", right );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		//textArea.replaceSelection("\n");

	}

	public void AppendImage(ImageIcon ori_icon) {
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len); // place caret at the end (with no selection)
		Image ori_img = ori_icon.getImage();
		ii=ori_img;
		Image new_img;
		ImageIcon new_icon;
		int width, height;
		double ratio;
		width = ori_icon.getIconWidth();
		height = ori_icon.getIconHeight();
		// Image가 너무 크면 최대 가로 또는 세로 200 기준으로 축소시킨다.
		if (width > 200 || height > 200) {
			if (width > height) { // 가로 사진
				ratio = (double) height / width;
				width = 200;
				height = (int) (width * ratio);
			} else { // 세로 사진
				ratio = (double) width / height;
				height = 200;
				width = (int) (height * ratio);
			}
			new_img = ori_img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			new_icon = new ImageIcon(new_img);
			textArea.insertIcon(new_icon);
		} else {
			textArea.insertIcon(ori_icon);
			new_img = ori_img;
		}
		len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		textArea.replaceSelection("\n");
		// ImageViewAction viewaction = new ImageViewAction();
		// new_icon.addActionListener(viewaction); // 내부클래스로 액션 리스너를 상속받은 클래스로
		// panelImage = ori_img.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_DEFAULT);

		gc2.drawImage(ori_img,  0,  0, panel.getWidth(), panel.getHeight(), panel);
		gc.drawImage(panelImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
	}

	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	// Server에게 network으로 전송
	public void SendMessage(String msg) {
		try {
			ChatMsg obcm = new ChatMsg(UserName, "200", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			// AppendText("dos.write() error");
			AppendText("oos.writeObject() error");
			try {
				//				dos.close();
				//				dis.close();
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			// textArea.append("메세지 송신 에러!!\n");
			AppendText("SendObject Error");
		}
	}
}
