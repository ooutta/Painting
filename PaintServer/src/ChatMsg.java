
// ChatMsg.java ä�� �޽��� ObjectStream ��.
import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import javax.swing.ImageIcon;

class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	public String code; // 100:�α���, 400:�α׾ƿ�, 200:ä�ø޽���, 300:Image, 500: Mouse Event
	public String UserName;
	public String data;
	public ImageIcon img;
	public MouseEvent mouse_e;
	public Color pen_color;
	public int pen_size; // pen size
	public int x1, y1, x2, y2;
//	public int status;
	public String shape;
	public String path;
	public String textBoxWord;
	
	public ChatMsg(String UserName, String code, String msg) {
		this.code = code;
		this.UserName = UserName;
		this.data = msg;
	}
}