import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import net.java.games.input.Controller;

public class GUI extends JFrame{

	private Ball[] Balls = {
			new Ball(Ball.bc.red),new Ball(Ball.bc.red),new Ball(Ball.bc.red),new Ball(Ball.bc.red),new Ball(Ball.bc.red),
			new Ball(Ball.bc.blue),new Ball(Ball.bc.blue),new Ball(Ball.bc.blue),new Ball(Ball.bc.pink),new Ball(Ball.bc.pink),
			new Ball(Ball.bc.orange),new Ball(Ball.bc.yellow)};
	private SwerveGui My_Swerve = new SwerveGui();
	Painting Floor = new Painting();
	long time = -1;
	int highScore = 0;
	boolean freeMode = false;
	Controller.Type type = null;
	
	private JRadioButtonMenuItem mCrab = new JRadioButtonMenuItem("Crab Mode");
	private JRadioButtonMenuItem mGyro = new JRadioButtonMenuItem("Gyro Mode");
	private JRadioButtonMenuItem mFree = new JRadioButtonMenuItem("Free Mode");
	private JRadioButtonMenuItem mTimed = new JRadioButtonMenuItem("Timed Mode");
	private JMenuItem mGoal = new JMenuItem("Goal");
	private JMenuItem mHow = new JMenuItem("How to Play");
	private JMenuItem mSpeed = new JMenuItem("Speed");
	private JMenuItem mSize = new JMenuItem("Size");
	private JMenuItem mUseKeyboard = new JMenuItem("Use Keyboard");
	
	public GUI(Controller.Type itype){
		type = itype;
		
		setLayout(new BorderLayout());
		
		JMenuBar mb = new JMenuBar();
		JMenu mDriveMode = new JMenu("Drive Mode");
		mb.add(mDriveMode);
		ButtonGroup mDriveGroup = new ButtonGroup();
		mDriveGroup.add(mCrab);
		mCrab.addActionListener(new meventDrive());
		mDriveMode.add(mCrab);
		mDriveGroup.add(mGyro);
		mGyro.addActionListener(new meventDrive());
		mGyro.setSelected(true);
		mDriveMode.add(mGyro);
		JMenu mGameMode = new JMenu("Game Mode");
		mb.add(mGameMode);
		ButtonGroup mGameGroup = new ButtonGroup();
		mGameGroup.add(mFree);
		mFree.addActionListener(new meventDrive());
		mGameMode.add(mFree);
		mGameGroup.add(mTimed);
		mTimed.addActionListener(new meventDrive());
		mTimed.setSelected(true);
		mGameMode.add(mTimed);
		JMenu mSettings = new JMenu("Settings");
		mb.add(mSettings);
		mSpeed.addActionListener(new meventSettings());
		mSettings.add(mSpeed);
		mSize.addActionListener(new meventSettings());
		mSettings.add(mSize);
		mUseKeyboard.addActionListener(new meventSettings());
		mSettings.add(mUseKeyboard);
		JMenu mHelp = new JMenu("Help");
		mb.add(mHelp);
		mGoal.addActionListener(new meventInfo());
		mHelp.add(mGoal);
		mHow.addActionListener(new meventInfo());
		mHelp.add(mHow);
		setJMenuBar(mb);
		
		Floor.setPreferredSize(new Dimension(1000, 600));
		add("Center", Floor);
	}
	
	public void Drive(float[] joys) { // y1, x1, y2, x2
		if(time>=0 || freeMode){
			My_Swerve.Drive(joys);
		}else{
			My_Swerve.Drive(new float[] {0,0,0,0});
		}
		Floor.repaint();
	}
	
	public boolean IsContacting(int i, int winWidth, int winHeight){
		int ballX = winWidth/2+Balls[i].ballX;
		int ballY = winHeight/2-Balls[i].ballY;
		boolean frontBound = true;
		boolean middleBound = true;
		if(My_Swerve.resetInFront){
			Balls[i].prevInFront = false;
		}
		if(My_Swerve.FRx - My_Swerve.FLx > 0){
			frontBound = (ballY-My_Swerve.FRy <= (float)(My_Swerve.FRy - My_Swerve.FLy)/(My_Swerve.FRx - My_Swerve.FLx)*(ballX-My_Swerve.FRx));
		}else if(My_Swerve.FRx - My_Swerve.FLx < 0){
			frontBound = (ballY-My_Swerve.FRy >= (float)(My_Swerve.FRy - My_Swerve.FLy)/(My_Swerve.FRx - My_Swerve.FLx)*(ballX-My_Swerve.FRx));
		}else if(My_Swerve.FRx < My_Swerve.BRx){
			frontBound = (ballX <= My_Swerve.FRx);
		}else if(My_Swerve.FRx > My_Swerve.BRx){
			frontBound = (ballX >= My_Swerve.FRx);
		}
		if(My_Swerve.FRx - My_Swerve.BRx > 0){
			middleBound = (ballY-My_Swerve.FRy <= (float)(My_Swerve.FRy - My_Swerve.BRy)/(My_Swerve.FRx - My_Swerve.BRx)*(ballX-My_Swerve.FRx))
					&& (ballY-My_Swerve.FLy >= (float)(My_Swerve.FLy - My_Swerve.BLy)/(My_Swerve.FLx - My_Swerve.BLx)*(ballX-My_Swerve.FLx));
		}else if(My_Swerve.FRx - My_Swerve.BRx < 0){
			middleBound = (ballY-My_Swerve.FRy >= (float)(My_Swerve.FRy - My_Swerve.BRy)/(My_Swerve.FRx - My_Swerve.BRx)*(ballX-My_Swerve.FRx))
					&& (ballY-My_Swerve.FLy <= (float)(My_Swerve.FLy - My_Swerve.BLy)/(My_Swerve.FLx - My_Swerve.BLx)*(ballX-My_Swerve.FLx));
		}else if(My_Swerve.FRx < My_Swerve.FLx){
			middleBound = (ballX >= My_Swerve.FRx) && (ballX <= My_Swerve.FLx);
		}else if(My_Swerve.FRx > My_Swerve.FLx){
			middleBound = (ballX <= My_Swerve.FRx) && (ballX >= My_Swerve.FLx);
		}
		boolean curInFront = frontBound && middleBound;
		boolean curInBack = !frontBound && middleBound;
		
		boolean isContacting = (Balls[i].prevInFront && curInBack);
		Balls[i].prevInFront = curInFront;
		return isContacting;
	}

	public int getPoints() {
		time = -1;
		return Floor.points;
	}

	public void printTime(long l) {
		time = l;
	}

	public void setHighScore(int points) {
		highScore = points;
	}

	public void resetPoints() {
		Floor.points = 0;
	}

	public void changeWindow() {
		for(int i = 0; i < Balls.length; i++){
			Balls[i].changeWindow(Floor.getWidth(), Floor.getHeight());
		}
	}
	
	public class meventDrive implements ActionListener{
		public void actionPerformed(ActionEvent me1){
			if(me1.getSource() == mCrab){
				My_Swerve.setDriveMode(Swerve.driveMode.crab);
			}
			if(me1.getSource() == mGyro){
				My_Swerve.setDriveMode(Swerve.driveMode.gyro);
			}
			if(me1.getSource() == mFree){
				freeMode = true;
			}
			if(me1.getSource() == mTimed){
				freeMode = false;
			}
		}
	}
	
	public class meventInfo implements ActionListener{
		public void actionPerformed(ActionEvent me1){
			if(me1.getSource() == mGoal){
				JOptionPane.showMessageDialog(GUI.this, 
	            		"The goal is to drive around and collect as many balls as possible using the front end of the swerve drive.\n"+
							"The bigger the ball the more points it is worth.\n"+
							"Note: The green triangle is pointing to the front of the robot.", 
	            		"Goal", JOptionPane.INFORMATION_MESSAGE);
			}
			if(me1.getSource() == mHow){
				JOptionPane.showMessageDialog(GUI.this, 
	            		"If using the keyboard, WASD translates and left/right arrow rotates.\n"+
	            				"If using a gamepad or two joysticks, left stick translates and right stick rotates.\n"+
	            				"See \"README.txt\" for more information", 
	            		"How To Play", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	public class meventSettings implements ActionListener{
		public void actionPerformed(ActionEvent me1){
			if(me1.getSource() == mSpeed){
				int speed = JOptionPane.showOptionDialog(GUI.this, "Select Desired Speed", "Speed",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						new Object[] {1,2,3,4,5,6,7,8,9,10}, 5);
				if(speed != -1){
					My_Swerve.speed = (speed+1)/5.0*10.0;
				}
			}
			if(me1.getSource() == mSize){
				int size = JOptionPane.showOptionDialog(GUI.this, "Select Desired Size", "Size",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						new Object[] {1,2,3,4,5,6,7,8,9,10}, 5);
				if(size != -1){
					My_Swerve.size = (size+6)/60.0;
				}
			}
			/*if(me1.getSource() == mStartBtn){
				int btn = JOptionPane.showOptionDialog(GUI.this, "Select Desired Start Button", "Start Button",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						new Object[] {0,1,2,3,4,5,6,7,8,9,10}, 5);
				if(btn != -1){
					startBtn = btn;
				}
			}*/
			if(me1.getSource() == mUseKeyboard){
				type = null;
			}
		}
	}

	public class Painting extends JPanel{
		int points = 0;
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			for(int i = 0; i < Balls.length; i++){
				Balls[i].paint(g, this.getHeight(), this.getWidth());
				if(IsContacting(i, this.getWidth(), this.getHeight())){
					if(time>=0){
						points += Balls[i].collect(My_Swerve.getX(), My_Swerve.getY(), Balls[i].ballWidth/2+My_Swerve.getZ());
					}else{
						Balls[i].collect(My_Swerve.getX(), My_Swerve.getY(), Balls[i].ballWidth/2+My_Swerve.getZ());
					}
				}
			}
			/*boolean frontBound = false;
			boolean middleBound = false;
			for(int ballX = 0; ballX < this.getWidth(); ballX+=20){
				for(int ballY = 0; ballY < this.getHeight(); ballY+=20){
					if(My_Swerve.FRx - My_Swerve.FLx > 0){
						frontBound = (ballY-My_Swerve.FRy <= (float)(My_Swerve.FRy - My_Swerve.FLy)/(My_Swerve.FRx - My_Swerve.FLx)*(ballX-My_Swerve.FRx));
					}else if(My_Swerve.FRx - My_Swerve.FLx < 0){
						frontBound = (ballY-My_Swerve.FRy >= (float)(My_Swerve.FRy - My_Swerve.FLy)/(My_Swerve.FRx - My_Swerve.FLx)*(ballX-My_Swerve.FRx));
					}else if(My_Swerve.FRx < My_Swerve.BRx){
						frontBound = (ballX <= My_Swerve.FRx);
					}else if(My_Swerve.FRx > My_Swerve.BRx){
						frontBound = (ballX >= My_Swerve.FRx);
					}
					if(My_Swerve.FRx - My_Swerve.BRx > 0){
						middleBound = (ballY-My_Swerve.FRy <= (float)(My_Swerve.FRy - My_Swerve.BRy)/(My_Swerve.FRx - My_Swerve.BRx)*(ballX-My_Swerve.FRx))
								&& (ballY-My_Swerve.FLy >= (float)(My_Swerve.FLy - My_Swerve.BLy)/(My_Swerve.FLx - My_Swerve.BLx)*(ballX-My_Swerve.FLx));
					}else if(My_Swerve.FRx - My_Swerve.BRx < 0){
						middleBound = (ballY-My_Swerve.FRy >= (float)(My_Swerve.FRy - My_Swerve.BRy)/(My_Swerve.FRx - My_Swerve.BRx)*(ballX-My_Swerve.FRx))
								&& (ballY-My_Swerve.FLy <= (float)(My_Swerve.FLy - My_Swerve.BLy)/(My_Swerve.FLx - My_Swerve.BLx)*(ballX-My_Swerve.FLx));
					}else if(My_Swerve.FRx < My_Swerve.FLx){
						middleBound = (ballX >= My_Swerve.FRx) && (ballX <= My_Swerve.FLx);
					}else if(My_Swerve.FRx > My_Swerve.FLx){
						middleBound = (ballX <= My_Swerve.FRx) && (ballX >= My_Swerve.FLx);
					}
					if(frontBound && middleBound){
						g.setColor(Color.GREEN);
					}
					if(!frontBound && middleBound){
						g.setColor(Color.BLUE);
					}
					if(frontBound && !middleBound){
						g.setColor(Color.GRAY);
					}
					if(!frontBound && !middleBound){
						g.setColor(Color.BLACK);
					}
					g.fillOval(ballX, ballY, 10, 10);
				}
			}*/
			My_Swerve.paint(g, this.getHeight(), this.getWidth());
			g.setColor(Color.BLACK);
    		g.setFont(new Font("Arial", Font.PLAIN, 50));
			g.drawString("Score: "+points, 5, this.getHeight()-5);
			if(time<0){
				if(type == Controller.Type.GAMEPAD){
					g.drawString("Press A to Start", 5, 50);
				}else if(type == Controller.Type.STICK){
					g.drawString("Press Button 1 to Start", 5, 50);
				}else{
					g.drawString("Press Space to Start", 5, 50);
				}
			}else{
				g.drawString("Time: "+(20000-time)/1000.0, 5, 50);
			}
			g.drawString("High Score: "+highScore, this.getWidth()/2+5, this.getHeight()-5);
		}
	}
}