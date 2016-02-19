public class Swerve {
	
	public double m_dThetaFR = Math.PI/2;
	public double m_dThetaBR = Math.PI/2;
	public double m_dThetaFL = Math.PI/2;
	public double m_dThetaBL = Math.PI/2;
	
	public double m_dSpeedFR = 0;
	public double m_dSpeedBR = 0;
	public double m_dSpeedFL = 0;
	public double m_dSpeedBL = 0;

	public double _dX = 0; //Delta X from Pivot Point to Center of Rotation (-*..+*)
	public double _dY = 0; //Delta Y from Pivot Point to Center of Rotation (-*..+*)
	public double _dZ = 0; //Distance from Pivot Point to Center of Rotation [0..+*)
	//Which direction we want to translate in + PI/2.0 () 
	public double _dPivotAngle = 0;
	public double _dTurnAngle = Math.PI/2;
	public double _dMagnitude = 0;
	
	public double m_tssr = 0.3;
	public double m_dSensitivity = 5.0;
	public double m_dGyroAngle = Math.PI/2.0;
	public double m_dRobotLen = 10.0;
	public double m_dRobotWidth = 10.0;
	
	public enum driveMode {steer, car, translate, crab, gyro}
	public driveMode m_eDriveMode = driveMode.gyro;
	
	void Drive(double _dJoy1, double _dJoy2)
	{
		// The magnitude for car mode is like a gas petal (that reverses)
		// and a steering wheel (that can turn the wheels to spin in place)
		if(m_eDriveMode == driveMode.car) //Joy1 = magnitude    Joy2 = angle
		{
			SwerveCalc(0, _dJoy1, _dJoy2);
		}
		// For translate we only are not allowed to turn
		else if(m_eDriveMode == driveMode.translate) //Joy1 = X    Joy2 = Y
		{
			SwerveCalc(_dJoy1, _dJoy2, 0);
		}
	}

	void Drive(double _dJoy1X, double _dJoy1Y, double _dJoy2X)
	{
		// For steer mode the translate X, translate Y, rotate values are passed in
		if(m_eDriveMode == driveMode.steer)
		{
			SwerveCalc(_dJoy1X, _dJoy1Y, _dJoy2X);
		}
		// crab and gyro mode are similar to steer mode but extra math is done to allow for the robot to turn in place
		else if(m_eDriveMode == driveMode.crab || m_eDriveMode == driveMode.gyro)
		{
			// Get the Maximum translate speed
			double xlatMaxSpeed = (Math.abs(_dJoy1X) > Math.abs(_dJoy1Y))? Math.abs(_dJoy1X):Math.abs(_dJoy1Y);
			
			// Rotate less when going forward fast
			// and scale to [-1..1]
			double joyRot = 0;
			if(xlatMaxSpeed != 0.0 || _dJoy2X != 0.0)
			{
				joyRot = Math.atan2(_dJoy2X, xlatMaxSpeed)*2.0/Math.PI;
			}
			
			double joyXlatX = _dJoy1X;
			double joyXlatY = _dJoy1Y;
			// if we are rotating more than we are translating then we want to scale the translate speeds
			if(Math.abs(_dJoy2X) > xlatMaxSpeed)
			{
				// set the scalar to the absolute value of the (rotate value)/(max translate speed)
				// we have to be careful that we don't divide by zero
				double xlatScalar;
				if(Math.abs(_dJoy1X) >= Math.abs(_dJoy1Y) && Math.abs(_dJoy1X) != 0.0)
				{
					xlatScalar = Math.abs(_dJoy2X/_dJoy1X);
				}
				else if (Math.abs(_dJoy1X) < Math.abs(_dJoy1Y))
				{
					xlatScalar = Math.abs(_dJoy2X/_dJoy1Y);
				}
				else
				{
					joyXlatX = _dJoy2X;
					joyXlatY = _dJoy2X;
					xlatScalar = 1.0;
				}
				joyXlatX *= xlatScalar;
				joyXlatY *= xlatScalar;
			}
			
			SwerveCalc(joyXlatX, joyXlatY, joyRot);
		}
	}
	
	// Calculates the speed and angles of the modules
	void SwerveCalc(double joyXlatX, double joyXlatY, double joyRot)
	{
		//Solve for the max translate speed
		_dMagnitude = (Math.abs(joyXlatX) > Math.abs(joyXlatY))? Math.abs(joyXlatX): Math.abs(joyXlatY);
		
		//Get the modified Rotate value
		if(m_eDriveMode != driveMode.crab && m_eDriveMode != driveMode.gyro)
		{
			joyRot *= (1-(1-m_tssr)*_dMagnitude);
		}
		else if(Math.abs(joyRot) != 1)
		{
			joyRot *= (1-(1-2.0*m_tssr)*_dMagnitude);
		}
		
		// How much we want to turn. PI/2.0 is foward, 0 is full right, PI is full left
		_dTurnAngle = (1 - joyRot) * Math.PI/2.0;
		
		if(_dTurnAngle != Math.PI/2.0)	//If we are turning
		{
			_dPivotAngle = FullAtan(joyXlatY, joyXlatX) - Math.PI/2.0;
			
			//change _dPivotAngle to point in the direction we want to go relative to the field
			if(m_eDriveMode == driveMode.gyro)
			{
				_dPivotAngle -= m_dGyroAngle - Math.PI/2.0;
			}
			
			_dZ = (m_dSensitivity * Math.tan(_dTurnAngle));	//Solving for opposite side
			_dX = _dZ * Math.cos(_dPivotAngle);				//Solving for Delta X
			_dY = _dZ * Math.sin(_dPivotAngle);				//Solving for Delta Y
			
			// flip the Wheels if turning left
			double flipWheels = (_dTurnAngle > Math.PI/2.0)? Math.PI : 0; //Left turn
			
			//Solve for angles (wheel position)
			m_dThetaBL = (FullAtan(_dY + m_dRobotLen/2.0, _dX + m_dRobotWidth/2.0) + Math.PI/2.0 + flipWheels) % (2.0*Math.PI);
			m_dThetaBR = (FullAtan(_dY + m_dRobotLen/2.0, _dX - m_dRobotWidth/2.0) + Math.PI/2.0 + flipWheels) % (2.0*Math.PI);
			m_dThetaFL = (FullAtan(_dY - m_dRobotLen/2.0, _dX + m_dRobotWidth/2.0) + Math.PI/2.0 + flipWheels) % (2.0*Math.PI);
			m_dThetaFR = (FullAtan(_dY - m_dRobotLen/2.0, _dX - m_dRobotWidth/2.0) + Math.PI/2.0 + flipWheels) % (2.0*Math.PI);
			
			//Solve for radii (relative wheel speed)
			m_dSpeedFL = DistanceFormula(_dX + m_dRobotWidth/2.0, _dY - m_dRobotLen/2.0);
			m_dSpeedFR = DistanceFormula(_dX - m_dRobotWidth/2.0, _dY - m_dRobotLen/2.0);
			m_dSpeedBL = DistanceFormula(_dX + m_dRobotWidth/2.0, _dY + m_dRobotLen/2.0);
			m_dSpeedBR = DistanceFormula(_dX - m_dRobotWidth/2.0, _dY + m_dRobotLen/2.0);
		}
		else	//Forward
		{
			// The angle we want to go
			m_dThetaBL = FullAtan(joyXlatY, joyXlatX);
			m_dThetaBR = FullAtan(joyXlatY, joyXlatX);
			m_dThetaFL = FullAtan(joyXlatY, joyXlatX);
			m_dThetaFR = FullAtan(joyXlatY, joyXlatX);
			
			// adjust for which way we want to go relative to the field
			if(m_eDriveMode == driveMode.gyro)
			{
				double dAngle = 5.0*Math.PI/2.0 - m_dGyroAngle;
				m_dThetaBL = (m_dThetaBL + dAngle) % (2.0*Math.PI);
				m_dThetaBR = (m_dThetaBR + dAngle) % (2.0*Math.PI);
				m_dThetaFL = (m_dThetaFL + dAngle) % (2.0*Math.PI);
				m_dThetaFR = (m_dThetaFR + dAngle) % (2.0*Math.PI);
			}
			
			// wheels all go the same relative speed
			m_dSpeedFR = 1;
			m_dSpeedBR = 1;
			m_dSpeedFL = 1;
			m_dSpeedBL = 1;
		}
		
		//Solve for fastest wheel speed
		double _dSpeedArray[] = {m_dSpeedFR, m_dSpeedBR, m_dSpeedFL, m_dSpeedBL};
	    double _dMaxSpeed = _dSpeedArray[0];
	    for(int i = 1; i < 4; i++)
	    {
			if(_dSpeedArray[i] > _dMaxSpeed)
			{
				_dMaxSpeed = _dSpeedArray[i];
			}
	    }
		
		//Set ratios based on maximum wheel speed
		m_dSpeedFR = (m_dSpeedFR / _dMaxSpeed) * _dMagnitude;
		m_dSpeedBR = (m_dSpeedBR / _dMaxSpeed) * _dMagnitude;
		m_dSpeedFL = (m_dSpeedFL / _dMaxSpeed) * _dMagnitude;
		m_dSpeedBL = (m_dSpeedBL / _dMaxSpeed) * _dMagnitude;
	}
	
	//gets the polar angle of (_dX, _dY)
	double FullAtan(double _dY, double _dX)
	{
		double _dAngle = 0;
		if(_dX > 0 && _dY >= 0){
			_dAngle = Math.atan(_dY / _dX);
		}
		else if(_dX == 0 && _dY >= 0){
			_dAngle = Math.PI / 2.0;
		}
		else if(_dX == 0 && _dY < 0){
			_dAngle = 3.0*Math.PI/2.0;
		}
		else if(_dX > 0 && _dY < 0){
			_dAngle = Math.atan(_dY / _dX) + 2.0*Math.PI;
		}
		else if(_dX < 0){
			_dAngle = Math.atan(_dY / _dX) + Math.PI;
		}
		return _dAngle;
	}

	//gets the distance of (_dX, _dY)
	double DistanceFormula(double _dX, double _dY)
	{
		return Math.sqrt(_dX*_dX+_dY*_dY);
	}
}
