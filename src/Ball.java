import java.awt.Color;
import java.awt.Graphics;

public class Ball {
	int ballX = 0;
	int ballY = 0;
	enum bc {red, blue, pink, orange, yellow};
	bc color = bc.red;
	int ballWidth = 15;
	int winX = 980;
	int winY = 460;
	boolean prevInFront = false;
	
	
	Ball (bc c) {
		color = c;
		switch(color){
		case red:
			ballWidth = 15;
			break;
		case blue:
			ballWidth = 20;
			break;
		case pink:
			ballWidth = 25;
			break;
		case orange:
			ballWidth = 30;
			break;
		case yellow:
			ballWidth = 35;
			break;
		default:
			ballWidth = 15;
			break;
		}
	}

	public void changeWindow(int width, int height) {
		winX = width - 20;
		winY = height - 120;
	}
	
	public void paint(Graphics g, int WinHeight, int WinWidth){
		switch(color){
		case red:
			g.setColor(Color.RED);
			break;
		case blue:
			g.setColor(Color.BLUE);
			break;
		case pink:
			g.setColor(Color.PINK);
			break;
		case orange:
			g.setColor(Color.ORANGE);
			break;
		case yellow:
			g.setColor(Color.YELLOW);
			break;
		default:
			g.setColor(Color.RED);
			break;
		}
		g.fillOval(WinWidth/2-ballWidth/2+ballX, WinHeight/2-ballWidth/2-ballY, ballWidth, ballWidth);
	}

	public int collect(double robotX, double robotY, double maxDist) {
		double ballDist = 0;
		
		do{
			ballX = (int)(Math.random()*winX-winX/2);
			ballY = (int)(Math.random()*winY-winY/2);
			double ballDistX = Math.abs(robotX-ballX);
			double ballDistY = Math.abs(robotY-ballY);
			ballDist = Math.sqrt(ballDistX*ballDistX+ballDistY*ballDistY);
		} while (ballDist < maxDist);

		switch(color){
		case red:
			return 100;
		case blue:
			return 200;
		case pink:
			return 300;
		case orange:
			return 500;
		case yellow:
			return 1000;
		default:
			return 100;
		}
	}
}
