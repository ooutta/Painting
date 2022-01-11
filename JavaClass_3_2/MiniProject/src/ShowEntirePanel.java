import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

//오른쪽 하단 패널에 레벨에 따라 모아야 할 건담 부품의 완성품 이미지를 출력한다.
public class ShowEntirePanel extends JPanel {
	private int level;
	private int width, height; //완성품의 가로, 세로 크기
	private Image new_img; //이미지 크기 조절 후의 이미지
	private ImageIcon icon = null; // 레벨에 따라 가져올 이미지 아이콘
	private int x = 0, y = 0; //이미지 아이콘을 출력할 x,y 좌표
	
	//레벨에 따라 다른 건담 완성품을 가져와서 출력
	public void setLevel(int level) {
		this.level = level;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setBackground(Color.BLACK);
		//레벨에 따라 완성해야 할 건담의 완성품을 다르게 보여준다.
		if(level == 0) //레벨1
			icon = new ImageIcon("image/level1.jpg");
		else if (level == 1) //레벨2
			icon = new ImageIcon("image/level2.jpg");
		else if (level == 2) //레벨3
			icon = new ImageIcon("image/level3.jpg");
		
		resizeImg(icon); //icon 크기 조절
		g.drawImage(icon.getImage(), x, y, width, height, null);
	}
	
	//showEntirePanel의 크기에 맞게 이미지 크기와 출력 좌표를 조절
	public void resizeImg(ImageIcon icon) {
		Image ori_img=icon.getImage();
		double ratio;
		width = icon.getIconWidth();
		height = icon.getIconHeight();
		
		//이미지의 크기가 범위보다 크다면 크기 조절
		if (width > 250 || height > 220) {
			if (width > height) { //너비가 높이보다 큰 사진. 너비 기준으로 높이 축소.
				ratio = (double) height / width;
				width = 250;
				height = (int) (width * ratio);
			} else { //높이가 너비보다 큰 이미지. 높이 기준으로 너비 축소.
				ratio = (double) width / height;
				height = 220;
				width = (int) (height * ratio);
			}
			new_img = ori_img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		}
		else {
			new_img = ori_img;
		}
		//패널의 중앙에 오도록 좌표 조절
		x = (this.getWidth()-width)/2;
		y = (this.getHeight()-height)/2;
	}
}
