import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EnterPanel extends JFrame {
	private JLabel user; 
	private JTextField userName; //user name을 입력 받을 text field
	private ImageIcon backImgIcon = new ImageIcon("image/enterImg.jpg"); //배경 이미지를 가져온다.
	private Image backImg = backImgIcon.getImage(); 
	
	public EnterPanel() {
		setTitle("건담 조립 타이핑 게임");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800,600);
		setContentPane(new FirstPanel()); //컨텐트팬 지정
		
		//userName field에 이름을 입력하고 엔터를 누르면 게임 화면인 GameFrame으로 이동
		userName.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				GameFrame f = new GameFrame(userName.getText());
				setVisible(false); //현재 패널은 보이지 않도록 한다.
			}
		});
		
		setResizable(false); //창 크기 변경 불가
		setVisible(true);
	}
	
	private class FirstPanel extends JPanel {
		public FirstPanel() {
			setLayout(null);
			user = new JLabel("ID");
			user.setForeground(Color.WHITE);
			user.setBounds(70,250,100,50);
			user.setFont(new Font("Gothic", Font.BOLD, 30));

			userName = new JTextField(); //userName을 입력할 공간
			userName.setBounds(150,250,200,50);
			userName.setFont(new Font("Gothic", Font.BOLD, 30));
			
			add(user);
			add(userName);
		}
		
		//현재 패널 전체에 배경 이미지를 그린다.
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(backImg,0,0,this.getWidth(), this.getHeight(), null);
		}
	}
	
	public static void main(String[] args) {
		new EnterPanel();
	}
}



