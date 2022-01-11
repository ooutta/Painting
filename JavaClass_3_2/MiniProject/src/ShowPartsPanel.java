import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

//오른쪽 중앙에서 건담의 부품 이미지를 보여주는 패널
public class ShowPartsPanel extends JPanel {
	private int level;
	private File dir; //레벨에 따라 가져올 건담 부품 이미지 파일이 담긴 디렉토리 
	private File [] partsFile; //dir 경로에 있는 모든 이미지 파일을 가져오는 File 배열
	private ArrayList<File> partsFileList = new ArrayList<File>(); //ShowPartsPanel에 출력할 이미지 리스트
	private boolean once = true; //레벨 선택 전 디폴트로 이미지 파일들을 불러오기 위한 변수

	public ShowPartsPanel() {
		this.setBackground(Color.BLACK);
		setLayout(null);
	}

	//레벨에 따라 다른 디렉토리에 있는 이미지들을 가져와서 모아야 할 부품 리스트인 partsFileList에 추가한다.
	public void setFile() {
		partsFileList.clear();
		if(level == 0) dir = new File("image/level1");
		else if (level == 1) dir = new File("image/level2");
		else dir = new File("image/level3");
		partsFile =  dir.listFiles();
		for(int i=0;i<partsFile.length;i++)
			partsFileList.add(partsFile[i]);
	}
	
	//레벨에 따라 다르게 가져온 이미지 리스트를 현재 패널에 다시 그린다.
	public void setLevel(int level) {
		this.level = level;
		setFile();
		repaint();
	}

	//게임 시작 버튼을 누를 경우 이미지 파일을 다시 가져와서 사용한다.
	public ArrayList<File> firstGetImageFiles() {
		setFile();
		return partsFileList;
	}
	
	//ShowPartsPanel에 출력된 이미지 파일 리스트 리턴
	public ArrayList<File> imageFiles() {
		return partsFileList;
	}
	
	//ShowPartsPanel에 있는 모든 부품을 모았으면 true, 모아야 할 부품이 남았으면 false 리턴
	public boolean collectEntireParts() {
		if(partsFileList.size() == 0) {
			setFile();
			return true;
		}
		else return false;
	}

	//사용자가 이미지 레이블이 붙은 단어를 맞춰서 삭제될 경우 showPartsPanel에서도 해당 이미지 레이블이 삭제되도록 한다.
	public void deleteImage(String path) {
		//삭제할 이미지 파일을 제외한 나머지 이미지 파일을 저장할 임시 이미지 파일 리스트
		ArrayList<File> temporaryFile = new ArrayList<File>(); 
		
		for(int i=0;i<partsFileList.size();i++) {
			//모아야 할 부품 리스트인 partsFileList에 사용자가 맞춘 이미지의 path와 경로가 같은 이미지가 있으면 
			//해당 이미지를 제외하고 임시 파일 리스트 temporaryFile에 저장.
			if(!partsFileList.get(i).getPath().equals(path)) {
				temporaryFile.add(new File(partsFileList.get(i).getPath()));
			}
		}
		//partsFileList을 clear()하고, 삭제한 이미지를 제외하고 저장된 temporaryFile를 모두 저장한다.
		partsFileList.clear();
		partsFileList.addAll(temporaryFile);
		repaint();
	}
	
	//현재 패널에 모아야 할 부품을 (60x60) 크기로 출력
	public void drawImages(Graphics g) {
		int x = 0, y = 0;

		for(int i=0;i<partsFileList.size();i++) {
			File f = partsFileList.get(i);
			Image img = new ImageIcon(f.getPath()).getImage();
			if(x+60>this.getWidth()) {
				x = 0;
				y += 65;
			}
			g.drawImage(img, x, y, 60, 60, null);
			x+=70;
		}
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(once) { //레벨을 선택하지 않았을 경우 디폴트로 한번만 이미지 파일을 가져온다.
			setFile();
			once = false;
		}
		drawImages(g);
	}
}
