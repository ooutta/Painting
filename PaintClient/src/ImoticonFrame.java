import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.Color;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//이모티콘 창
public class ImoticonFrame extends JFrame {
	private File [] imgFiles;
	private int x = 0, y = 0;
	private JButton [] imgBtns;
	private ChatMsg msg;
	private String path;

	public ImoticonFrame() {
		super("이모티콘");

		setLayout(null);
		setBackground(Color.WHITE);
		setSize(230,150);

		addIcon();
		setResizable(false);
		setVisible(true);
	}
	
	public void addIcon() {
		imgFiles = new File("images").listFiles();
		imgBtns = new JButton[imgFiles.length];

		for(int i=0;i<imgFiles.length;i++) {
			File f = imgFiles[i];
			ImageIcon img = new ImageIcon(f.getPath());

			imgBtns[i] = new JButton(img);
			imgBtns[i].setBounds(x, y, 50, 50);
			imgBtns[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					path = f.getPath();
					setVisible(false);
				}
			});
			add(imgBtns[i]);

			if(x+70 >= this.getWidth()){
				x = 0;
				y += 55;
			}
			else x+=55;
		}
	}

	public String getChatMsg() {
		return path;
	}
	
}
