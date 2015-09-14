package Bennett.Bernardoni;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class MainC {
	
	static byte[] keys = {0,0,0,0,0,0}; // WSADLR
	static boolean timing = false;
	static boolean reset = false;
	static long time;
	static int highScore = 0;
	static GUI gui;
    static Component stickX1 = null;
    static Component stickY1 = null;
    static Component stickX2 = null;
    static Component stickY2 = null;
    static Component startBtn = null;
	static float [] joys = new float[4];
	
	public static void main (String args[]){
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Controller xbox = null;
		Controller stick1 = null;
		Controller stick2 = null;
        Controller.Type type = null;
        for(int i =0;i<ca.length;i++){
        	if(ca[i].getType() == Controller.Type.GAMEPAD){
        		xbox = ca[i];
        		stickX1 = xbox.getComponent(Component.Identifier.Axis.X);
        		stickY1 = xbox.getComponent(Component.Identifier.Axis.Y);
        		stickX2 = xbox.getComponent(Component.Identifier.Axis.RX);
        		stickY2 = xbox.getComponent(Component.Identifier.Axis.RY);
        		type = Controller.Type.GAMEPAD;
        		startBtn = xbox.getComponent(Component.Identifier.Button.A);
            	break;
        	} else if (ca[i].getType() == Controller.Type.STICK) {
        		if(stick1 == null){
        			stick1 = ca[i];
        		} else {
            		stick2 = ca[i];
            		stickX1 = stick1.getComponent(Component.Identifier.Axis.X);
            		stickY1 = stick1.getComponent(Component.Identifier.Axis.Y);
            		stickX2 = stick2.getComponent(Component.Identifier.Axis.X);
            		stickY2 = stick2.getComponent(Component.Identifier.Axis.Y);
            		type = Controller.Type.STICK;
            		startBtn = stick2.getComponent(Component.Identifier.Button._1);
            		break;
        		}
        	}
        }
        
		gui = new GUI(type);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.pack();
		//gui.setResizable(false);
		gui.setTitle("Pwnage Swerve Game");
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
		gui.requestFocusInWindow();
        
		gui.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				gui.changeWindow();
			}
			
			@Override public void componentHidden(ComponentEvent arg0) {}
			@Override public void componentMoved(ComponentEvent arg0) {}
			@Override public void componentShown(ComponentEvent arg0) {}
		});
		
        gui.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
			    switch (e.getKeyCode()){
		        case KeyEvent.VK_W:		keys[0] = 1;	break;
		        case KeyEvent.VK_S:		keys[1] = 1;	break;
		        case KeyEvent.VK_A:		keys[2] = 1;	break;
		        case KeyEvent.VK_D:		keys[3] = 1;	break;
		        case KeyEvent.VK_LEFT:	keys[4] = 1;	break;
		        case KeyEvent.VK_RIGHT:	keys[5] = 1;	break;
		        case KeyEvent.VK_SPACE:	reset = true;	break;
			    }
		        e.consume();
		    }
			public void keyReleased(KeyEvent e) {
			    switch (e.getKeyCode()){
		        case KeyEvent.VK_W:		keys[0] = 0;	break;
		        case KeyEvent.VK_S:		keys[1] = 0;	break;
		        case KeyEvent.VK_A:		keys[2] = 0;	break;
		        case KeyEvent.VK_D:		keys[3] = 0;	break;
		        case KeyEvent.VK_LEFT:	keys[4] = 0;	break;
		        case KeyEvent.VK_RIGHT:	keys[5] = 0;	break;
			    }
		        e.consume();
			}
		});
        
        if ((new File("highscore.hs")).exists() == false){
        	try {
        	    BufferedWriter out = new BufferedWriter(new FileWriter("highscore.hs"));
        	    out.write("0");
        	    out.close();
        	} catch (IOException e) {}
        }
        try {
		    BufferedReader in = new BufferedReader(new FileReader("highscore.hs"));
		    String str = in.readLine();
		    highScore = Integer.parseInt(str);
		    gui.highScore = highScore;
		    in.close();
		} catch (IOException e) {}
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
            	try {
            	    BufferedWriter out = new BufferedWriter(new FileWriter("highscore.hs"));
            	    out.write(""+gui.highScore);
            	    out.close();
            	} catch (IOException e) {}
            }
        }));
        
        long s = System.currentTimeMillis();
        while(true){
        	if(System.currentTimeMillis()-s > 10){
        		type = gui.type;
        		if(type == Controller.Type.GAMEPAD){
        			if(xbox.poll()){
	            		joysticks();
            		}else{
            			type = null;
            			gui.type = null;
            		}
        		}else if(type == Controller.Type.STICK || type == Controller.Type.GAMEPAD){
        			if(stick1.poll() && stick2.poll()){
	            		joysticks();
            		}else{
            			type = null;
            			gui.type = null;
            		}
        		}else{
        			double keyboardSpeed = 0.8;
        			joys[0] = (float) (keyboardSpeed*(keys[1] - keys[0]));
        			joys[1] = (float) (keyboardSpeed*(keys[3] - keys[2]));
        			joys[2] = 0;
        			joys[3] = (float) (keyboardSpeed*(keys[5] - keys[4]));
        			gui.Drive(joys);
        			timer(reset);
        		}
	            s = System.currentTimeMillis();
        	}
        }
	}

	static void joysticks(){
		float [] prevJoys = new float[4];
		double k = .8;
		double deadband = .1;
		timer(startBtn.getPollData()==1);
		joys[0] = (Math.abs(stickX1.getPollData()) > deadband)? stickX1.getPollData() : 0;
		joys[1] = (Math.abs(stickY1.getPollData()) > deadband)? stickY1.getPollData() : 0;
		joys[2] = (Math.abs(stickX2.getPollData()) > deadband)? stickX2.getPollData() : 0;
		joys[3] = (Math.abs(stickY2.getPollData()) > deadband)? stickY2.getPollData() : 0;
		for(int i = 0; i<4; i++){
			prevJoys[i] = joys[i];
			joys[i] = (float)(k*joys[i]+(1-k)*prevJoys[i]);
		}
		gui.Drive(joys);
	}
	
	static void timer(boolean startBtn){
		if(startBtn)
		{
			gui.resetPoints();
			reset = false;
	    	timing = true;
	    	time = System.currentTimeMillis();
		}
		if(timing){
			gui.printTime(System.currentTimeMillis()-time);
			if(System.currentTimeMillis()-time > 20000){
				timing = false;
				int points = gui.getPoints();
				if(points > highScore){
					highScore = points;
					gui.setHighScore(points);
				}
			}
		}
	}
}
