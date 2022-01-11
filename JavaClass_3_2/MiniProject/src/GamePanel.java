import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GamePanel extends JPanel {
	private JTextField input = new JTextField(40); //맞출 단얼르 입력할 필드
	private ScorePanel scorePanel = null; //오른쪽 상단 패널. 이름, 점수, 생명을 보여준다.
	private ShowPartsPanel showPartsPanel = null; //오른쪽 중앙 패널. 모아야 할 부품 이미지를 보여준다.
	private GameGroundPanel ground = null; //중앙 패널. 단어가 떨어지는 공간.
	private TextSource textSource = null; //떨어질 단어 벡터를 생성.

	private Vector<JLabel> labelVector = new Vector<JLabel>(); //떨어지는 단어를 하나씩 벡터에 담는다.
	private Vector<FallingThread> fallVector = new Vector<FallingThread>(); //떨어지는 단어 하나당 스레드 하나를 담을 벡터.
	private ArrayList<File> partsImageFiles; //건담 부품 이미지 파일을 담은 리스트

	private MakeLabelThread lbThread = null; //떨아질 단어 레벨을 생성하는 스레드
	private InputThread inputThread = null; //떨어지는 단어를 맞추기 위한 단어 입력 스레드
	private String userName; //사용자 이름
	private FileWriter fWriter; //랭킹 기록을 위한 객체

	private String fallingWord = null; //떨어지는 단어
	private int x; //단어 레이블의 x 좌표
	private long delay; //떨어질 단어가 생성되는 시간 간격 
	private int point; //쌓일 점수
	private int speed = 5; //단어가 떨어지는 속도
	private int life; //생명 개수
	private boolean deleteOrNot; //단어 레이블이 지워졌는지 아닌지를 판단할 변수

	public GamePanel(ScorePanel scorePanel, ShowPartsPanel showPartsPanel) {
		this.scorePanel = scorePanel;
		this.showPartsPanel = showPartsPanel;

		ground = new GameGroundPanel(); //단어가 떨어지는 패널 생성
		setLayout(new BorderLayout());
		add(ground, BorderLayout.CENTER); //단어가 떨어지는 패널인 ground를 센터에 부착
		add(new InputPanel() ,BorderLayout.SOUTH); //단어를 입력하는 InputPanel을 아래에 부착

		textSource = new TextSource("word.txt"); //단어를 읽어올 파일을 word.txt로 설정
	}

	public void startGame() { //GameFrame에서 startBtn 버튼을 누르면 불린다.
		Container c = ground;
		c.removeAll();
		c.repaint(); //단어가 떨어질 ground 패널에 있는 모든 것을 삭제
		deleteOrNot = false; //단어 삭제 여부를 판단할 변수의 default를 false로 지정

		partsImageFiles = showPartsPanel.firstGetImageFiles(); //떨어지는 단어 레이블 왼쪽에 붙을 부품 이미지를 가져온다.
		lbThread = new MakeLabelThread(); // 떨어질 단어 레이블 생성 스레드 생성
		lbThread.start(); //떨어질 단어 레이블 생성 스레드 시작
		inputThread = new InputThread(); //단어 입력받는 스레드 생성
		inputThread.start(); //단어 입력받는 스레드 시작
	}

	public void stopGame() { //GameFrame에서 stopBtn 버튼이 눌리거나 life가 0이 되는 경우 실행
		JLabel endLabel = new JLabel("**Game Over**");
		endLabel.setSize(400,50);
		endLabel.setLocation(100, 100);
		endLabel.setFont(new Font("Tahoma", Font.BOLD, 30));
		endLabel.setForeground(Color.MAGENTA);
		ground.add(endLabel);
		ground.repaint(); //게임이 종료되었다는 레이블 부착

		lbThread.interrupt(); //단어 생성 스레드 종료
		inputThread.interrupt(); //단어 입력 받는 스레드 종료
		if(scorePanel.runningClearThreadOrStop()) //모든 부품을 모아 scorePanel이 깜빡거리는 스레드가 실행중이라면 
			scorePanel.stopSuccessThread(); //해당 스레드를 종료시킨다.

		for(int j=0;j<fallVector.size();j++) { //실행중인 떨어지는 스레드를 모두 종료
			fallVector.get(j).interrupt();
		}

		//랭크 기록에 userName과 점수 기록
		writeRank(userName + "\t" +scorePanel.getScore());

		scorePanel.reset(); //scorePanel 변수들을 초기화
	}

	public void hitFloor() { // 스레드가 InputPanel이 있는 바닥에 닿아서 실패할 때 호출
		scorePanel.decrease(point); //레벨에 따른 점수만큼 감소
		scorePanel.decreseLife(); //생명 하나 감소
	}

	public void setUserName(String userName) { //사용자 이름 설정
		this.userName = userName;
	}

	//레벨에 따라 떨어지는 단어 생성 속도와 적립되는 점수를 다르게 설정
	public void setDelayPoint(int chooseLevel) { 
		if(chooseLevel == 0)  { //레벨1
			delay = 4000;
			point = 10;
		}
		else if (chooseLevel == 1) { //레벨2
			delay =  2000;
			point = 15;
		}
		else { //레벨3
			delay =  1000;
			point = 20;
		}
	}
	
	//사용자 이름과 점수를 기록
	public void writeRank(String rank) {
		try {
			fWriter = new FileWriter("ranking.txt", true);
			fWriter.write(rank);
			fWriter.write("\r\n");
			fWriter.close();
		}
		catch(IOException e) {
			System.exit(0);
		}
	}

	//떨어지는 단어 레이블 왼쪽에 붙어 함께 떨어질 건담 부품의 이미지 크기 조절
	public ImageIcon changeImageSize(String path) {
		ImageIcon partsImg;
		partsImg = new ImageIcon(path);
		Image img = partsImg.getImage();
		Image chageImg = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		ImageIcon changeIcon = new ImageIcon(chageImg);
		return changeIcon;
	}

	//떨어지는 단어를 만들 스레드
	class MakeLabelThread extends Thread {
		private String path = "";
		private int imgFileIndex; //건담 부품 인덱스
		private int imgCount = 0; //이미지 레이블 생성 주기
		
		@Override
		public void run() {
			while(true) {
				try {
					//모든 부품들을 다 모았으면 100점 추가와 scorePanel이 깜빡거리도록 한다.
					if(showPartsPanel.collectEntireParts()) { 
						scorePanel.increase(100);
						scorePanel.success();
					}
					
					life = scorePanel.getLife(); //scorePanel에 기록된 남은 생명 개수

					if(life == 0) { //생명이 0이면 게임 종료
						stopGame();
						break;
					}

					fallingWord = textSource.getRandomWord(); //떨어질 단어를 랜덤으로 하나 가져온다.
					JLabel label = new JLabel();
					label.setText(fallingWord);
					label.setSize(200, 30);
					x = (int)(Math.random()*(getWidth()-label.getWidth())); //레이블의 x 위치를 랜덤하게.
					label.setLocation(x+30, 0); // 레이블 위치				
					label.setForeground(Color.WHITE); //레이블의 글자 색을 설정한다.				
					label.setFont(new Font("Tahoma", Font.PLAIN, 20));
					ground.add(label); //ground 패널에 단어 레이블을 부착해서 떨어지도록 한다.
					labelVector.add(label); //떨어지는 단어들을 벡터에 저장

					JLabel imgLabel = null;
					if(imgCount % 3 == 0) { //단어 레이블이 3번 떨어질 때 이미지 레이블은 1번 떨어지도록 한다.
						partsImageFiles = showPartsPanel.imageFiles(); //레벨에 따른 부품 이미지 전체 파일을 가져온다.

						imgFileIndex = (int)(Math.random()*partsImageFiles.size()); //부품들 중 하나를 랜덤으로 선택

						path = partsImageFiles.get(imgFileIndex).getPath();

						imgLabel = new JLabel(changeImageSize(path)); //이미지의 크기를 조절한 후 레이블에 삽입
						imgLabel.setSize(30,30);
						imgLabel.setLocation(x, 0); //이미지 레이블의 위치와 크기 설정
						ground.add(imgLabel); //ground 패널에 이미지 레이블을 부착해서 떨어지도록 한다.
					}

					FallingThread thread = new FallingThread(ground, label , imgLabel, path);
					fallVector.add(thread); 
					thread.start(); //생성된 단어 레이블과 이미지 레이블을 delay 주기로 떨어지도록 하는 스레드 생성 후 벡터에 넣은 다음 실행

					imgCount++;

					sleep(delay); //레벨1에서는 4초, 레벨2는 2초, 레벨3은 1초 간격으로 단어 ㄹ벨이 생성된다.
				} catch (InterruptedException e) {
					return;
				}

			}
		}
	}

	//MakeLabelThread에서 생성된 레이블을 speed만큼 떨어지도록 하는 스레드
	class FallingThread extends Thread {
		private GameGroundPanel ground; //단어가 떨어지는 패널을 GameGroundPanel로 설정
		private JLabel label; //떨어지는 단어를 출력하는 레이블
		private JLabel imgLabel; //떨어지는 이미지를 출력하는 레이블
		private String path; //imgLabel에 부착할 이미지의 경로

		public FallingThread(GameGroundPanel ground, JLabel label, JLabel imgJLabel, String path) {
			this.ground = ground;
			this.label = label;
			this.imgLabel = imgJLabel;
			this.path = path;
		}

		@Override
		public void run() {
			while(true) {
				try {
					int score = scorePanel.getScore(); //scorePanel에서 현재 점수를 가져온다.

					//score에 따라 단어가 떨어지는 y 좌표 간격 조절
					if(score >= 240) speed = 20;
					else if (score >= 120) speed = 15;
					else speed = 10;
					
					int y = label.getY() + speed; //speed 픽셀씩 아래로 이동
					if(y >= ground.getHeight()-label.getHeight()) { //단어 레이블이 바닥에 닿으면
						Container c = label.getParent();
						c.remove(label); //레이블을 부모 컨테이너에서 삭제
						if(imgLabel != null) { //이미지 레이블이 있다면
							c.remove(imgLabel); //이미지 레이블 삭제
							repaint(); //다시 그리기
						}
						hitFloor(); //바닥에 닿였으므로 점수와 생명 감소
						break; // 스레드 종료
					}

					label.setLocation(label.getX(), y); //레이블 위치를 y로 변경
					if(imgLabel != null)   //이미지 레이블이 있다면 이미지 레이블의 y 위치도 변경
						imgLabel.setLocation(label.getX()-30, y);
					GamePanel.this.repaint(); //패널 다시 그린다.

					
					sleep(500);
				} catch (InterruptedException e) { 
					//InputThead에서 단어를 올바르게 맞춘 경우 단어 레이블과 이미지 레이블 삭제
					Container c = label.getParent();
					c.remove(label);
					if(imgLabel != null) {
						c.remove(imgLabel);
						
						//단어를 맞춰서 interrupt가 발생한 경우에 건담 부품을 보여주는 showPartsPanel에서 해당 경로의 이미지 삭제
						if(deleteOrNot) { 
							showPartsPanel.deleteImage(path);
						}
					}
					c.repaint(); 
					return;
				}

			}
		}	
	}

	//사용자가 단어를 입력해 ground 패널에서 떨어지는 단어를 제거하는 스레드
	class InputThread extends Thread {
		private JLabel label;
		@Override
		public void run() {
			//input JTextField에서 엔터를 입력하면 실행
			input.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JTextField t = (JTextField)e.getSource();
					String inWord = t.getText(); //사용자가 입력한 단어
					for(int j=0; j < labelVector.size() ; j++) {
						label = labelVector.get(j);
						//떨어지는 단어가 저장된 labelVector에서 사용자가 입력한 단어 inWord가 있으면
						//점수를 증가시키고 labelVector에서 삭제 및 해당 FallingThread 종료
						if(label.getText().equals(inWord)) {
							scorePanel.increase(point);
							t.setText("");
							labelVector.remove(label.getText());
							fallVector.get(j).interrupt();
							deleteOrNot = true; //떨어지는 단어를 맞춘 경우이므로 true
							return;
						}
					}

				}		
			});
		}
	}

	class GameGroundPanel extends JPanel { //단어가 출몰하는 곳. CENTER
		//배경 이미지 설정
		ImageIcon gamePanelIcon = new ImageIcon("image/gamePanelImg.jpg");
		Image gamePanelImg = gamePanelIcon.getImage();
		public GameGroundPanel() {
			setLayout(null);
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			//GameGroundPanel 크기로 배경 이미지 출력
			g.drawImage(gamePanelImg,0,0,this.getWidth(), this.getHeight(), null);
		}
	}

	class InputPanel extends JPanel { //단어 입력하는 곳. SOUTH
		public InputPanel() {
			setLayout(new FlowLayout());
			this.setBackground(Color.BLACK);
			add(input); //JTextField 부착
		}
	}
}
