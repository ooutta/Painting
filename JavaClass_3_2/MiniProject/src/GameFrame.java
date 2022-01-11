import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

public class GameFrame extends JFrame {
	//단어와 관련된 메뉴
	private JMenuItem addWord = new JMenuItem("추가");
	private JMenuItem deleteWord = new JMenuItem("삭제");
	private JMenuItem searchWord = new JMenuItem("검색");

	//순위와 관련된 메뉴
	private JMenuItem allRank = new JMenuItem("TOP 10");
	private JMenuItem myRank = new JMenuItem("내 기록");

	//툴바에 삽입할 레벨과 관련된 콤보 박스
	private JLabel lbLevel = new JLabel("레벨");
	private String [] level = {"Level1", "Level2", "Level3"};
	private JComboBox<String> levelCombo = new JComboBox<String>(level);
	
	//실행 관련 버튼들
	private JButton homeBtn = new JButton(new ImageIcon("image/homeBtn.jpg"));
	private JButton startBtn = new JButton(new ImageIcon("image/startBtn.jpg"));
	private JButton stopBtn = new JButton(new ImageIcon("image/stopBtn.jpg"));

	//필요한 패널 객체들
	private ScorePanel scorePanel = new ScorePanel(); 
	private ShowEntirePanel showEntirePanel = new ShowEntirePanel();
	private ShowPartsPanel showPartsPanel = new ShowPartsPanel();
	private GamePanel gamePanel = new GamePanel(scorePanel, showPartsPanel);
	private JFrame frame;
	
	private String userName; //사용자가 입력한 이름을 저장할 변수
	private int chooseLevel = 0; //사용자가 레벨 콤보박스에서 선택한 레벨을 저장할 변수 

	public GameFrame(String userName) {
		setTitle("건담 조립 타이핑 게임");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800,600);
		
		frame = this; //현재 frame을 변수에 저장
		this.userName = userName; //EnterPanel로부터 가져온 userName을 GameFrame에서도 사용할 수 있도록 저장

		scorePanel.setUserName(userName); //ScorePanel에서 userName 사용할 수 있도록 전송
		gamePanel.setUserName(userName); //GamePanel에서 userName 사용할 수 있도록 전송
		gamePanel.setDelayPoint(chooseLevel); //콤보박스에서 선택된 레벨에 따라 게임 플레이가 다르게 되도록 레벨을 전송

		splitPane(); //JSplitPane을 생성하여 컨텐트팬의 CENTER에 부착
		makeMenu(); //메뉴 생성 함수
		makeToolBar(); //툴바 생성 함수
 		setResizable(false); //창 크기 변경 불가
		setVisible(true);
	}

	private void splitPane() { //GameFrame의 화면 분할
		JSplitPane hPane = new JSplitPane();
		getContentPane().add(hPane, BorderLayout.CENTER);
		hPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT); //세로로 분할한다.
		hPane.setDividerLocation(500); //500px 위치에 세로선으로 분할
		hPane.setEnabled(false); //분리선 못움직이게
		hPane.setLeftComponent(gamePanel); //새로 500px기준 왼쪽에 gamePanel 부착

		JSplitPane p2Pane = new JSplitPane();
		p2Pane.setOrientation(JSplitPane.VERTICAL_SPLIT); //가로로 분할한다.
		p2Pane.setDividerLocation(70); //70px 위치에 가로선으로 분할한다.
		p2Pane.setTopComponent(scorePanel); //70px 기준 위에 scorePanel 부착
		p2Pane.setBottomComponent(showPartsPanel); //70px 기준 아래에 showPartsPanel 부착
		hPane.setRightComponent(p2Pane); //hPane의 오른쪽에 p2Pane 부착

		JSplitPane pPane = new JSplitPane();
		pPane.setOrientation(JSplitPane.VERTICAL_SPLIT); //가로로 분할한다.
		pPane.setDividerLocation(270); //270px 위치에 가로선으로 분할한다.
		pPane.setTopComponent(p2Pane); //270px 기준 위에 p2Pane 부착
		pPane.setBottomComponent(showEntirePanel); //270px 기준 아래에 showEntirePanel 부착
		hPane.setRightComponent(pPane); //hPane의 오른쪽에 pPane 붙임
	}

	private void makeMenu() { //메뉴 생성 함수
		JMenuBar mBar = new JMenuBar();
		this.setJMenuBar(mBar); //현재 frame에 mBar 메뉴 부착

		mBar.setBackground(Color.BLACK); 

		JMenu wordMenu = new JMenu("단어편집"); //단어편집 메뉴 생성
		wordMenu.setForeground(Color.WHITE);
		//wordMenu 메뉴에 메뉴아이템 생성 삽입
		wordMenu.add(addWord);
		wordMenu.add(deleteWord);
		wordMenu.add(searchWord);
		mBar.add(wordMenu);

		//랭킹 메뉴 설정하고 메뉴아이템 부착
		JMenu rankingMenu = new JMenu("순위");
		rankingMenu.setForeground(Color.WHITE);
		rankingMenu.add(allRank);
		rankingMenu.add(myRank);
		mBar.add(rankingMenu);

		//각 메뉴 아이템 선택하면 작동하는 액션 코드 작성
		addWord.addActionListener(new InputWord());
		deleteWord.addActionListener(new DeleteWord());
		searchWord.addActionListener(new SearchWord());

		allRank.addActionListener(new AllRank());
		myRank.addActionListener(new MyRank());
	}

	private void makeToolBar() { //툴바 생성 함수
		JToolBar tBar = new JToolBar();
		tBar.setBackground(Color.BLACK);
		tBar.add(homeBtn); //홈버튼 생성하고 툴바에 부착

		//homeBtn 누르면 프로그램 첫 화면인 EnterPanel로 화면 전환
		homeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EnterPanel f = new EnterPanel();
				setVisible(false);
			}
		});
		tBar.addSeparator();
		tBar.addSeparator();

		//"레벨"을 보여주는 레이블의 글자색을 하얀색으로 변경
		lbLevel.setForeground(Color.WHITE);
		tBar.add(lbLevel); //레벨 레이블 생성하고 툴바에 부착
		tBar.add(levelCombo); //레벨 콤보박스 생성하고 툴바에 부착

		levelCombo.addActionListener(new ChoosedLevel());

		JLabel empty = new JLabel("                 "); //컴포넌트 사이의 간격을 띄워주기 위해 삽입
		JLabel empty2 = new JLabel("                                                                                                                                  ");
		tBar.add(empty);
		tBar.add(startBtn); //시작 버튼 생성하고 툴바에 부착
		tBar.add(stopBtn); //중단 버튼 생성하고 툴바에 부착
		tBar.add(empty2);

		getContentPane().add(tBar, BorderLayout.NORTH); //북쪽에 tBar 툴바 부착

		startBtn.addActionListener(new StartAction()); //starBtn을 누르면 실행할 액션
		stopBtn.addActionListener(new StopAction()); //stopBtn을 누르면 실행할 액션

	}

	//startBtn 버튼을 누르면 gamePanel의 startGame() 실행
	private class StartAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			gamePanel.startGame();
		}
	}

	//stopBtn 버튼을 누르면 gamePanel의 stopGame() 실행
	private class StopAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			gamePanel.stopGame();
		}
	}

	//addWord 메뉴 아이템이 선택되면 실행할 액션
	private class InputWord implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//word.txt에 저장할 단어를 입력 받는 다이얼로그 출력
			String inputWord = JOptionPane.showInputDialog("추가할 단어를 입력하세요.");
			try {
				FileWriter wordFiles = new FileWriter("word.txt", true);
				wordFiles.write(inputWord); //wordFiles에 다이얼로그에서 입력 받은 inputWord 추가
				wordFiles.write("\r\n");
				wordFiles.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	//deleteWord 메뉴 아이템이 선택되면 실행할 액선
	private class DeleteWord implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//word.txt에서 삭제할 단어를 입력 받는 다이얼로그 출력
			String deleteWord = JOptionPane.showInputDialog("삭제할 단어를 입력하세요.");
			File src = new File("word.txt"); //기존에 저장된 단어 파일
			File dest = new File("word2.txt"); //word.txt에서 deleteWord를 제외한 단어를 복사할 파일
			boolean contain = false; //deleteWord가 src파일에 있으면 true, 없으면 false.
			
			try {
				FileInputStream fi = new FileInputStream(src); //word.txt 파일에서 읽어온다.
				FileOutputStream fo = new FileOutputStream(dest); //word2.txt 파일에 저장한다.

				Scanner scanner = new Scanner(fi);
				while(scanner.hasNext()) { // 파일 끝까지 한 단어씩 읽는다.
					String word = scanner.nextLine(); // 한 라인을 읽고 '\n'을 버린 나머지 문자열만 리턴
					if(word.equals(deleteWord)) { //deleteWord와 word.txt 파일에서 읽어온 word가 일치
						contain = true;
						break;
					}
					else { 
						//deleteWord와 word.txt 파일에서 읽어온 word가 일치하지 않으면 word2.txt파일에 word 저장
						word+="\r\n";
						fo.write(word.getBytes());
					}
				}
				scanner.close();

				fi.close();
				fo.close();

				if(contain) { //삭제할 단어가 word.txt에 있으면 실행
					Path oldpath = Paths.get("word.txt");
					Path newpath = Paths.get("word2.txt");
					Files.move(newpath, oldpath, StandardCopyOption.REPLACE_EXISTING); //word2.txt 파일을 word.txt로 이름 수정
					dest.delete(); //word2.txt 삭제

					JOptionPane.showMessageDialog(null, "삭제를 성공했습니다.", "삭제 성공", JOptionPane.INFORMATION_MESSAGE);
				}
				else 
					JOptionPane.showMessageDialog(null, "삭제할 단어가 없습니다.", "삭제 실패", JOptionPane.WARNING_MESSAGE);

			} catch(IOException e1) {
				return;
			}
		}
	}

	private class SearchWord implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//word.txt에서 검색할 단어를 입력 받는 다이얼로그 출력
			String searchWord = JOptionPane.showInputDialog("검색할 단어를 입력하세요.");
			File src = new File("word.txt");
			boolean contain = false;
			try {
				FileInputStream fi = new FileInputStream(src);
				Scanner scanner = new Scanner(fi);
				while(scanner.hasNext()) { // 파일 끝까지 읽음
					String word = scanner.nextLine(); // 한 라인을 읽고 '\n'을 버린 나머지 문자열만 리턴
					if(word.equals(searchWord)) { //찾는 단어 searchWord와 word.txt의 한 단어와 일치
						contain = true;
						break;
					}
				}
				scanner.close();

				if(contain) //찾는 단어가 있으면 실행
					JOptionPane.showMessageDialog(null, searchWord+"이/가 있습니다.", "검색 성공", JOptionPane.INFORMATION_MESSAGE);
				else //찾는 단어가 없으면 실행
					JOptionPane.showMessageDialog(null, searchWord+"이/가 없습니다.", "검색 실패", JOptionPane.WARNING_MESSAGE);

			} catch(IOException e1) {
				return;
			}
		}
	}

	//랭킹을 점수의 오름차순으로 정렬하기 위한 클래스
	private class Ranking implements Comparable<Ranking> {
		private String name;
		private int score;

		public Ranking(String name, int score) {
			this.name = name;
			this.score = score;
		}
		
		@Override //score를 내림차순으로 정렬하도록 메소드 재정의
		public int compareTo(Ranking o) {
			if(this.score < o.score) { 
				return 1;
			}
			else if (this.score == o.score) {
				return 0;
			}
			else {
				return -1;
			}
		}
	}
	
	//랭킹을 보여줄 다이얼로그 생성 클래스
	private class RankingDialog extends JDialog {
		private JScrollPane rankJScrollPane; //랭킹을 보여줄 JScrollPane
		private JList<String> scrollList; //정렬된 랭킹 리스트를 담을 리스트

		public RankingDialog(JFrame frame, String title, Vector<String> rankingVector) {
			super(frame, title);
			setLayout(null);
			setBounds(1000, 100, 300, 350);
			
			scrollList = new JList<String>(rankingVector); //랭킹이 기록된 벡터를 랭킹 리스트에 저장
			rankJScrollPane = new JScrollPane(scrollList); //랭킹 리스트를 JScrollPane에 부착 
			
			scrollList.setFont(new Font("Gothic", Font.PLAIN, 15));
			rankJScrollPane.setBounds(20,30,250,250);
			
			add(rankJScrollPane); //다이얼로그에 rankJScrollPane 부착
		}
	}
	
	//allRank 메뉴 아이템이 선택되면 실행. 전체 기록에서 1위부터 10위까지 순위를 보여주는 클래스
	private class AllRank implements ActionListener {
		private List<Ranking> myRankingList = new ArrayList<Ranking>(); //Ranking객체를 저장할 리스트 생성
		private Vector<String> rankingVector = new Vector<String>(); //myRankingList를 score 기준으로 정렬한 내용을 담을 벡터 
		private RankingDialog rankingDialog;
		
		public void actionPerformed(ActionEvent e) {
			File src = new File("ranking.txt"); //랭킹이 저장된 파일
			try {
				//랭킹을 name과 score로 분리해서 Ranking객체에 저장하고 List에 담는다.
				FileInputStream fi = new FileInputStream(src);
				Scanner scanner = new Scanner(fi);
				while(scanner.hasNext()) {
					String word = scanner.nextLine(); 
					String [] splitWord = new String[2];
					splitWord = word.split("\t");
					for(int i=0;i<2;i++) {
						splitWord[i] = splitWord[i].trim();
					}
					
					myRankingList.add(new Ranking(splitWord[0], Integer.parseInt(splitWord[1])));

				}
				scanner.close();

			} catch(IOException e1) {
				return;
			}
			
			Collections.sort(myRankingList); //score 기준 내림차순으로 정렬
			
			//상위 10개만 벡터에 저장
			for(int i=0;i<10;i++) {
				rankingVector.add("[" + (i+1) + "]  " + "ID : " +myRankingList.get(i).name + "   ,   score : "+ myRankingList.get(i).score);
			}
			
			//Top 10 기록을 다이얼로그에 띄운다.
			rankingDialog = new RankingDialog(frame, "TOP 10", rankingVector);
			rankingDialog.setVisible(true);
		}
	}
	
	//myRank 메뉴 아이템이 선택되면 실행. 현재 게임 진행중인 사용자의 점수 기록을 보여주는 클래스
	private class MyRank implements ActionListener {
		private List<Ranking> myRankingList = new ArrayList<>(); //Ranking객체를 저장할 리스트 생성
		private Vector<String> rankingVector = new Vector<String>(); //myRankingList를 score 기준으로 정렬한 내용을 담을 벡터 
		private RankingDialog rankingDialog;
		
		public void actionPerformed(ActionEvent e) {
			File src = new File("ranking.txt");
			try {
				FileInputStream fi = new FileInputStream(src);
				Scanner scanner = new Scanner(fi);
				while(scanner.hasNext()) { 
					String word = scanner.nextLine(); 
					String [] splitWord = new String[2];
					splitWord = word.split("\t");
					for(int i=0;i<2;i++) {
						splitWord[i] = splitWord[i].trim();
					}
					
					//랭킹 파일에 기록된 사용자 이름이 현재 게임 진행자와 같으면 랭킹 리스트에 저장
					if(splitWord[0].equals(userName))
						myRankingList.add(new Ranking(splitWord[0], Integer.parseInt(splitWord[1])));

				}
				scanner.close();

			} catch(IOException e1) {
				return;
			}
			
			Collections.sort(myRankingList); //score 기준 내림차순 정렬
			
			for(int i=0;i<myRankingList.size();i++) { //랭킹 파일에 기록된 현재 사용자와 이름이 같은 모든 기록을 벡터에 저장
				rankingVector.add("[" + (i+1) + "]  " + "ID : " +myRankingList.get(i).name + "   ,   score : "+ myRankingList.get(i).score);
			}
			
			//현재 사용자의 기록을 다이얼로그에 띄운다.
			rankingDialog = new RankingDialog(frame, "내 점수", rankingVector);
			rankingDialog.setVisible(true);
		}
		
	}

	//JComboBox에서 레벨이 선택되면 실행할 클래스
	private class ChoosedLevel implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>)e.getSource();
			chooseLevel = cb.getSelectedIndex(); //콤보박스에서 선택된 레벨
			gamePanel.setDelayPoint(chooseLevel); //gamePanel에 선택된 레벨 전송
			showPartsPanel.setLevel(chooseLevel); //showPartsPanel에 선택된 레벨 전송
			showEntirePanel.setLevel(chooseLevel); //showEntirePanel에 선택된 레벨 전송
		}
	}

}
