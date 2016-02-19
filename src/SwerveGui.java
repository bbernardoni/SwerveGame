import java.awt.Color;
import java.awt.Graphics;

public class SwerveGui{
	
	private Swerve Robot = new Swerve();
	double RobotX = 0;
	double RobotY = 0;
	double RobotR = 0;
	double diag = Math.sqrt(Robot.m_dRobotLen*Robot.m_dRobotLen + Robot.m_dRobotWidth*Robot.m_dRobotWidth)/2;
	double pxPerIn = 0;
	int FRx = 0;
	int FRy = 0;
	int FLx = 0;
	int FLy = 0;
	int BLx = 0;
	int BLy = 0;
	int BRx = 0;
	int BRy = 0;
	boolean resetInFront = false;
	double speed = 10;
	double size = 1/6.0;
	
	public void Drive(float[] joys) { // y1, x1, y2, x2
		Robot.Drive(joys[1], -joys[0], joys[3]);
	}

	public void setDriveMode(Swerve.driveMode dm) {
		Robot.m_eDriveMode = dm;
	}
	
	public int getX(){
		return (int)(RobotX*pxPerIn);
	}
	
	public int getY(){
		return (int)(RobotY*pxPerIn);
	}
	
	public double getZ(){
		return diag*pxPerIn;
	}

	public void setScreenSize(int width, int height) {
		pxPerIn = (height/8)/Robot.m_dRobotLen;
	}
	
	public void paint(Graphics g, int WinHeight, int WinWidth){
		int height = (int) (WinHeight*size);
		pxPerIn = (height)/Robot.m_dRobotLen;
		int width = (int)(pxPerIn*Robot.m_dRobotWidth);
		double radius = Math.sqrt(width*width+height*height)/2.0;
		double angle = Math.atan2(height,width);
		if(Robot.m_eDriveMode == Swerve.driveMode.gyro){
			Robot.m_dGyroAngle = (RobotR + Math.PI/2) % (2*Math.PI);
			if(Robot.m_dGyroAngle<0){
				Robot.m_dGyroAngle += 2*Math.PI;
			}
		}
		//Move Robot
		double speedScalar = speed/pxPerIn;
		diag = Math.sqrt(Robot.m_dRobotLen*Robot.m_dRobotLen + Robot.m_dRobotWidth*Robot.m_dRobotWidth)/2;
		double dFLx = 0;
		double dFLy = 0;
		if(Robot.m_dSpeedFL != 0){
			if(Robot._dTurnAngle != Math.PI/2.0){
				double FLrx = -Robot.m_dRobotWidth/2 - Robot._dX;
				double FLry = Robot.m_dRobotLen/2 - Robot._dY;
				double FLr = Math.sqrt(FLrx*FLrx+FLry*FLry);
				double FLda = Robot.m_dSpeedFL*speedScalar/FLr;
				if(Robot._dTurnAngle < Math.PI/2.0){
					FLda *= -1;
				}
				double FLa = Math.atan2(FLry, FLrx)+FLda;
				dFLx = FLr*Math.cos(FLa)-FLrx;
				dFLy = FLr*Math.sin(FLa)-FLry;
			} else {
				dFLx = Robot.m_dSpeedFL*Math.cos(Robot.m_dThetaFL)*speedScalar;
				dFLy = Robot.m_dSpeedFL*Math.sin(Robot.m_dThetaFL)*speedScalar;
			}
		}
		double dFRx = 0;
		double dFRy = 0;
		if(Robot.m_dSpeedFL != 0){
			if(Robot._dTurnAngle != Math.PI/2.0){
				double FRrx = Robot.m_dRobotWidth/2 - Robot._dX;
				double FRry = Robot.m_dRobotLen/2 - Robot._dY;
				double FRr = Math.sqrt(FRrx*FRrx+FRry*FRry);
				double FRda = Robot.m_dSpeedFR*speedScalar/FRr;
				if(Robot._dTurnAngle < Math.PI/2.0){
					FRda *= -1;
				}
				double FRa = Math.atan2(FRry, FRrx)+FRda;
				dFRx = FRr*Math.cos(FRa)-FRrx;
				dFRy = FRr*Math.sin(FRa)-FRry;
			} else {
				dFRx = Robot.m_dSpeedFR*Math.cos(Robot.m_dThetaFR)*speedScalar;
				dFRy = Robot.m_dSpeedFR*Math.sin(Robot.m_dThetaFR)*speedScalar;
			}
		}
		double offsetAng = Math.atan2(dFLy-dFRy, dFLx-dFRx-Robot.m_dRobotWidth)+ Math.atan(Robot.m_dRobotLen/Robot.m_dRobotWidth);
		double dX = Robot.m_dRobotWidth/2 + dFRx + diag*Math.cos(offsetAng);
		double dY = Robot.m_dRobotLen/2 + dFRy + diag*Math.sin(offsetAng);
		double newAng = Math.atan2(dY, dX)+RobotR;
		double dZ = Math.sqrt(dX*dX+dY*dY);
		RobotX += dZ*Math.cos(newAng);
		RobotY += dZ*Math.sin(newAng);
		RobotR += Math.atan((dFLy-dFRy)/(dFRx-dFLx-Robot.m_dRobotWidth));
		resetInFront = false;
		if(Math.abs(RobotX)>WinWidth/pxPerIn/2){
			RobotX = (RobotX>0)? RobotX-WinWidth/pxPerIn : RobotX+WinWidth/pxPerIn;
			resetInFront = true;
		}
		if(Math.abs(RobotY)>WinHeight/pxPerIn/2){
			RobotY = (RobotY>0)? RobotY-WinHeight/pxPerIn : RobotY+WinHeight/pxPerIn;
			resetInFront = true;
		}
		// paint robot
		FRx = WinWidth/2+(int)Math.round(radius*Math.cos(angle+RobotR))+getX();
		FRy = WinHeight/2-(int)Math.round(radius*Math.sin(angle+RobotR))-getY();
		FLx = WinWidth/2+(int)Math.round(radius*Math.cos(Math.PI-angle+RobotR))+getX();
		FLy = WinHeight/2-(int)Math.round(radius*Math.sin(Math.PI-angle+RobotR))-getY();
		BLx = WinWidth/2+(int)Math.round(radius*Math.cos(angle+Math.PI+RobotR))+getX();
		BLy = WinHeight/2-(int)Math.round(radius*Math.sin(angle+Math.PI+RobotR))-getY();
		BRx = WinWidth/2+(int)Math.round(radius*Math.cos(2*Math.PI-angle+RobotR))+getX();
		BRy = WinHeight/2-(int)Math.round(radius*Math.sin(2*Math.PI-angle+RobotR))-getY();
		int[] xPoints = new int[]{FLx,BLx,BRx,FRx};
		int[] yPoints = new int[]{FLy,BLy,BRy,FRy};
		g.setColor(new Color(0, 127, 255));
		g.fillPolygon(xPoints, yPoints, 4);
		int[] txPoints = new int[]{BLx,BRx,(FRx+FLx)/2};
		int[] tyPoints = new int[]{BLy,BRy,(FRy+FLy)/2};
		g.setColor(Color.GREEN);
		g.fillPolygon(txPoints, tyPoints, 3);
		g.setColor(Color.BLACK);
		//paint wheels
		paintWheel(g, FRx, FRy, Robot.m_dThetaFR+RobotR); // FR
		paintWheel(g, FLx, FLy, Robot.m_dThetaFL+RobotR); // FL
		paintWheel(g, BRx, BRy, Robot.m_dThetaBR+RobotR); // BR
		paintWheel(g, BLx, BLy, Robot.m_dThetaBL+RobotR); // BL
	}
	
	public void paintWheel(Graphics g, int x, int y, double ang){
		int hyp = 20;
		double rad = .25;
		int[] xPoints = new int[]{(int)(hyp*Math.cos(ang-rad)),(int)(hyp*Math.cos(ang+rad)),(int)(hyp*Math.cos(ang-rad+Math.PI)),(int)(hyp*Math.cos(ang+rad+Math.PI))};
		int[] yPoints = new int[]{(int)(hyp*Math.sin(ang-rad)),(int)(hyp*Math.sin(ang+rad)),(int)(hyp*Math.sin(ang-rad+Math.PI)),(int)(hyp*Math.sin(ang+rad+Math.PI))};
		for(int i = 0; i < 4; i++){
			xPoints[i] += x;
			yPoints[i] = y - yPoints[i];
		}
		g.fillPolygon(xPoints, yPoints, 4);
	}
}
