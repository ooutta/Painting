import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

//오른쪽 상단 패널. 이름, 점수, 생명 표시
public class ScorePanel extends JPanel {
	private int score = 0; //점수를 0으로 초기화
	private int life = 5; //생명을 5로 초기화
	private JLabel nameLabel = new JLabel("이름"); 
	private JLabel scoreLabel = new JLabel("점수 : "+Integer.toString(score)); //점수 레이블
	private JLabel lifeLabel = new JLabel("생명 : " + life);
	private JLabel success = new JLabel("모든 부품 모으기 성공!!"); //모든 부품을 모을 경우 scorePanel에 출력되는 레이블
	
	private SuccessThread th; //모든 부품 모으기에 성공할 경우 실행되는 스레드

	public ScorePanel() {
		setLayout(null);
		makeLabels();
	}

	//scorePanel에 레이블들을 부착
	public void makeLabels() {
		nameLabel.setBounds(10,10,70,20);
		nameLabel.setForeground(Color.WHITE);
		add(nameLabel);

		scoreLabel.setText("점수 : "+Integer.toString(score));
		scoreLabel.setSize(100,20);
		scoreLabel.setLocation(10,30);
		scoreLabel.setForeground(Color.WHITE);
		add(scoreLabel);
		
		lifeLabel.setText("생명 : " + life);
		lifeLabel.setSize(100,20);
		lifeLabel.setLocation(10,50);
		lifeLabel.setForeground(Color.WHITE);
		add(lifeLabel);
		
		setBackground(Color.BLACK);
	}

	public void increase(int point) { //점수 증가
		score+=point;
		scoreLabel.setText("점수 : "+Integer.toString(score));
	}

	public void decrease(int point) { //점수 감소
		score-=point;
		scoreLabel.setText("점수 : "+Integer.toString(score));
	}

	public void reset() { //score와 life를 초기화
		score = 0;
		scoreLabel.setText("점수 : "+Integer.toString(score));
		life = 5;
		lifeLabel.setText("생명 : " + life);
	}

	public int getScore() { 
		return score;
	}
	
	public void decreseLife() { //life를 1씩 감소
		life--;
		lifeLabel.setText("생명 : " + life);
	}
	
	public int getLife() {
		return life;
	}

	//GameFrame에서 받아온 userName으로 nameLabel의 텍스트 수정
	public void setUserName(String userName) { 
		nameLabel.setText("이름 : "+userName);
	}

	//모든 부품 모으기에 성공한 경우 스레드를 생성하고 시작한다.
	public void success() {
		th = new SuccessThread();
		th.start();
	}

	//실행중인 scorePanel이 깜빡거리는 스레드 종료
	public void stopSuccessThread() {
		th.interrupt();
	}

	//스레드가 없는지 실행중인지 종료된 상태인지에 따라 true, false return.
	public boolean runningClearThreadOrStop() {
		if(th == null) { //스레드가 없는 상태
			return false;
		}
		else {
			Thread.State state = th.getState();
			if(state == Thread.State.TERMINATED
					|| state == Thread.State.NEW
					||  state == Thread.State.RUNNABLE) { //실행중이 아닌 상태
				return false;
			}
			else { //실행중
				return true;
			}
		}
	}

	//모든 부품 모으기에 성공하면 실행되는 스레드
	class SuccessThread extends Thread {
		public void run() {
			//현재 srocePanel에 있는 레이블을 모두 지우고 success 레이블 부착
			remove(nameLabel);
			remove(scoreLabel);
			remove(lifeLabel);
			
			success.setSize(300,40);
			success.setLocation(10,10);
			success.setForeground(Color.WHITE);
			success.setFont(new Font("Gothic", Font.BOLD, 20));
			add(success);

			int n = 0, i = 0;
			while(true) {
				if(i == 4) { //success 레이블이 네 번 깜빡이고 종료
					remove(success);
					makeLabels();
					repaint();
					interrupt();
				}
				i++;

				if(n == 0) 
					setBackground(Color.BLACK);
				else
					setBackground(Color.YELLOW);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					return;
				}
				if(n==0) n=1;
				else n=0;
			}
		}
	}
}
