package org.firstinspires.ftc.leagueCode.misc;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

@TeleOp(name="leagueTele", group = "A")
//@Disabled
public class leagueTele extends LinearOpMode
{
    leagueMap robot = new leagueMap();
    Orientation angles;
    BNO055IMU imu;

    float currHeading = 0;
    // these booleans are all used for toggles
    boolean canTogglePlateGrabber = true;
    boolean canAddToLiftPos = true;
    boolean canSubtractFromLiftPos = true;
    boolean canInitiateSpitCycle = true;

    //This boolean tells us if we have a block in the intake
    boolean hasBlock = false;

    boolean blockIsGrabbed = false;

    //this array allows us to store the encoder values that correspond to different positions we
    // might want the lift to go to
    public int[] liftPositions = {0, 500, 900, 1500, 2150, 2650, 3250, 3550, 4000, 4000};

    //these are the servo positions for the end effector - they allow us to change these values
    // everywhere in the code at once
    double flippedIn = 0.93;
    double flippedGrab = 0.83;
    double flippedOut = 0.2;
    double flippedAbouToPlace = 0.25;
    double flipStartPos = 0.7;
    double wristWhenIn = 0.81;
    double wristWhenOut = 0.06;
    double rotateGrab = 0.98;
    double rotateFar = 0.58;
    double rotateLeft = 0.23;
    double rotateClose = 0;
    double grabbed = 0.8;
    double ungrabbed = 0.25;
    double capStore = 0;
    double capSlap = 0.38;
    
    //This tells the code what position the lift should be in at the moment
    int currentLiftPos = 0;
    
	//Whether or not the capstone has been activated
	boolean capStonePlaced    = false;
	//This represents the time needed to pass before we can place our capstone on the plate
	double capstoneTimer;

    //This is a value that lowers the lift slightly when we want to place a brick
    int blockPlaceValue = 500;

    //these track what our next place position on, to make it so our driver doesn't have to deal
    // with as much
    int nextLiftPos = 1;
    int nextPlacePos = 3;

    //This boolean is used to tell the code when the spit cycle should return the intake to intaking
    //instead of just stopping the intake
    boolean isInIntakeCycle = false;
    //This boolean tracks if the lift is high enough that our arm will not collide with the bot when flipping out
    boolean canFlipOut = false;
    //This boolean tracks if the lift is at or near it's target position
    boolean isAtTargetPosition = false;
    //This boolean tracks if the block is flipped out the back
    boolean isFlippedOutBack = false;

    //This double tracks the angle of the bot, for some of our automated action groups
    public double currAngle = 0;

//--------------------------------------------------------------------------------------------------
//----------------------------------------//
//----------------------------------------//
//---These are all of my Called Methods---//
//----------------------------------------//
//----------------------------------------//
//--------------------------------------------------------------------------------------------------
private void turnIMU()
{
	BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
	parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
	parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
	parameters.calibrationDataFile = "BNO055IMUCalibration.json";
	parameters.loggingEnabled = true;
	parameters.loggingTag = "IMU";
	parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

	robot.init(hardwareMap);
	imu = hardwareMap.get(BNO055IMU.class,"imu1");
	imu.initialize(parameters);
}
//--------------------------------------------------------------------------------------------------
private void driveIMU()
{
	BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
	parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
	parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
	parameters.calibrationDataFile = "BNO055IMUCalibration.json";
	parameters.loggingEnabled = true;
	parameters.loggingTag = "IMU";
	parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

	robot.init(hardwareMap);
	imu = hardwareMap.get(BNO055IMU.class,"imu2");
	imu.initialize(parameters);
}
//--------------------------------------------------------------------------------------------------
//this method tells the lift motors to drive towards their target position and updates our tracking booleans
public void driveLiftToPosition()
{
	int currentPos = robot.liftSecondary.getCurrentPosition();
	int posDifference = currentPos - liftPositions[currentLiftPos];
	double power = posDifference * 0.003;
	robot.liftPrimary.setPower(power);
	robot.liftSecondary.setPower(-power);
	if((robot.liftSecondary.getCurrentPosition() > (liftPositions[currentLiftPos] - 50)) && (robot.liftSecondary.getCurrentPosition() < (liftPositions[currentLiftPos] + 50)))
	{
		isAtTargetPosition = true;
	}
	else
	{
		isAtTargetPosition = false;
	}
	if(robot.liftSecondary.getCurrentPosition() > liftPositions[3])
	{
		canFlipOut = true;
	}
	else
	{
		canFlipOut = false;
	}
}
//--------------------------------------------------------------------------------------------------
//this method sets the target position for the lift
public void setLiftPosition(int position)
{
	if(position >= 0 && position <= 9)
	{
		currentLiftPos = position;
	}
}
//--------------------------------------------------------------------------------------------------
//this method starts an intake cycle - it will start the intake, wait until we have a block, and
//then grab onto it
public void initiateIntakeCycle()
{
	isFlippedOutBack = false;
	isInIntakeCycle = true;
	setLiftPosition(0);
	setFlipPosition(flippedGrab);
	robot.rotate.setPosition(rotateGrab);
	robot.grabber.setPosition(ungrabbed);
	robot.wrist.setPosition(wristWhenIn);
	robot.intake(0.05);
	while(!hasBlock && !isStopRequested())
	{
		telemetry.addLine("We are in the initiateIntakeCycle method");
		normalTeleopStuff();
	}
	robot.stopIntake();
	double start = System.currentTimeMillis();
	blockIsGrabbed = true;
	while((System.currentTimeMillis() - start) < 250 && !isStopRequested())
	{
		telemetry.addLine("We are in the initiateIntakeCycle method");
		normalTeleopStuff();
	}
	setFlipPosition(flippedIn);
	start = System.currentTimeMillis();
	while((System.currentTimeMillis() - start) < 500 && !isStopRequested())
	{
		telemetry.addLine("We are in the initiateIntakeCycle method");
		normalTeleopStuff();
	}
	robot.grabber.setPosition(grabbed);
}
//--------------------------------------------------------------------------------------------------
//this method stores most of our standard (not automated) teleop code like driving and toggling
//the plate grabber.
//making this a method allowed us to add this functionality to other methods easier
public void normalTeleopStuff()
{
	double frontRight;
	double frontLeft;
	double backRight;
	double backLeft;
	//This is full Holonomic
	frontRight = gamepad1.right_stick_y + (0.25 * gamepad1.left_stick_y) + (0.25 * gamepad1.left_stick_x) + gamepad1.right_stick_x;
	frontLeft  = gamepad1.right_stick_y + (0.25 * gamepad1.left_stick_y) - (0.25 * gamepad1.left_stick_x) - gamepad1.right_stick_x;
	backRight  = gamepad1.right_stick_y + (0.25 * gamepad1.left_stick_y) - (0.25 * gamepad1.left_stick_x) + gamepad1.right_stick_x;
	backLeft   = gamepad1.right_stick_y + (0.25 * gamepad1.left_stick_y) + (0.25 * gamepad1.left_stick_x) - gamepad1.right_stick_x;
	telemetry.addData("Right stick x", gamepad1.right_stick_x);
	//This is holonomic drive
	robot.front_right.setPower(frontRight);
	robot.front_left.setPower(frontLeft);
	robot.back_right.setPower(backRight);
	robot.back_left.setPower(backLeft);
//--------------------------------------------------------------------------------------------------
	if(robot.sensorColor.red() > 2 * robot.sensorColor.blue())
	{
		hasBlock = true;
	}
	else if(robot.sensorColor.red() <= 2 * robot.sensorColor.blue())
	{
		hasBlock = false;
	}
//--------------------------------------------------------------------------------------------------
	//this spits out a block if for some reason we need to
	if(gamepad1.left_bumper && canInitiateSpitCycle)
	{
		canInitiateSpitCycle = false;
		if(!blockIsGrabbed)
		{
			robot.disengageIntake();
			double start = System.currentTimeMillis();
			robot.outtake(0.02);
			while((System.currentTimeMillis() - start) < 1500 && !isStopRequested())
			{
				normalTeleopStuff();
			}
			robot.ungrabPlate();
			if(isInIntakeCycle)
			{
				robot.intake(0.05);
			}
			else
			{
				robot.stopIntake();
			}
		}
		else
		{
			robot.grabber.setPosition(ungrabbed);
			double start = System.currentTimeMillis();
			while((System.currentTimeMillis() - start) < 400 && !isStopRequested())
			{
				normalTeleopStuff();
			}
			robot.disengageIntake();
			setFlipPosition(flippedGrab);
			start = System.currentTimeMillis();
			while((System.currentTimeMillis() - start) < 400 && !isStopRequested())
			{
				normalTeleopStuff();
			}
			robot.outtake(0.01);
			start = System.currentTimeMillis();
			while((System.currentTimeMillis() - start) < 750 && !isStopRequested())
			{
				normalTeleopStuff();
			}
			robot.stopIntake();
			robot.ungrabPlate();
			blockIsGrabbed = false;
		}
	}
	else if(!canInitiateSpitCycle && !gamepad1.left_bumper)
	{
		canInitiateSpitCycle = true;
	}
//--------------------------------------------------------------------------------------------------
	if(canTogglePlateGrabber && gamepad1.dpad_left)
	{
		if(robot.plateGrabber1.getPosition() == 0.73 && canTogglePlateGrabber)
		{
			robot.grabPlate();
		}
		else
		{
			robot.ungrabPlate();
		}
		canTogglePlateGrabber = false;
	}
	else if(!canTogglePlateGrabber && !gamepad1.dpad_left)
	{
		canTogglePlateGrabber = true;
	}
//------------------------------------------------------------------------------------------
	if(canAddToLiftPos && (gamepad1.dpad_up || gamepad2.dpad_up) && nextLiftPos < 9)
	{
		nextLiftPos++;
		canAddToLiftPos = false;
	}
	else if(!canAddToLiftPos && (!gamepad1.dpad_up || !gamepad2.dpad_up))
	{
		canAddToLiftPos = true;
	}
	if(canSubtractFromLiftPos && (gamepad2.dpad_down || gamepad2.dpad_down) && nextLiftPos > 0)
	{
		nextLiftPos--;
		canSubtractFromLiftPos = false;
	}
	else if(!canSubtractFromLiftPos && (!gamepad2.dpad_down || !gamepad2.dpad_down))
	{
		canSubtractFromLiftPos = true;
	}
	telemetry.addData("Next lift position", nextLiftPos);
//--------------------------------------------------------------------------------------------------
	if(gamepad1.a)
	{
		nextPlacePos = 0;
	}
	if(gamepad1.x)
	{
		nextPlacePos = 1;
	}
	if(gamepad1.y)
	{
		nextPlacePos = 2;
	}
	if(gamepad1.b)
	{
		nextPlacePos = 3;
	}
//THIS CODE EXTENDS THE NEW MEASURING TAPE
	if(gamepad2.y)
	{
		robot.measuringTape.setPower(1);
	}
//THIS RETRACTS THE MEASURING TAPE
	else if(gamepad2.x)
	{
		robot.measuringTape.setPower(-1);
	}
//THIS CODE OVERRIDES OUR COLOR SENSOR IN CASE IS MALFUNCTIONS - true
	if(gamepad2.left_trigger !=0)
	{
		hasBlock = true;
	}
//THIS CODE OVERRIDES OUR COLOR SENSOR IN CASE IS MALFUNCTIONS - false
	if(gamepad2.right_trigger !=0)
	{
		hasBlock = false;
	}
//------------------------------------------------------------------------------------------
	if(nextPlacePos == 0)
	{
		telemetry.addLine("the next brick will be placed close");
	}
	if(nextPlacePos == 1)
	{
		telemetry.addLine("the next brick will be placed on the left");
	}
	if(nextPlacePos == 2)
	{
		telemetry.addLine("the next brick will be placed far");
	}
	if(nextPlacePos == 3)
	{
		telemetry.addLine("the next brick will be placed on the right");
	}
//------------------------------------------------------------------------------------------
//THIS CODE STOPS THE SERVO FORM MOVING AFTER WE LET GO
	if(gamepad1.dpad_right)
	{
		robot.measuringTape.setPower(-1);
	}
//------------------------------------------------------------------------------------------
	driveLiftToPosition();
	telemetry.addData("Lift encoder position", robot.liftPrimary.getCurrentPosition());
	telemetry.update();
}
//--------------------------------------------------------------------------------------------------
//this sets both the flip servos to a position.
public void setFlipPosition(double position)
{
	robot.flip1.setPosition(position);
	robot.flip2.setPosition(position);
}
//--------------------------------------------------------------------------------------------------
//this method moves the lift to it's next place position, as tracked with nextLiftPos and nextPlacePos
//It allows our driver to press 1 button and have the robot do everything to get ready to place
public void goToNextPosition()
{
	double start = System.currentTimeMillis();
	robot.disengageIntake();
	while((System.currentTimeMillis() - start) < 500 && !isStopRequested())
	{
		normalTeleopStuff();
		telemetry.addLine("We are in the goToNextPosition method");
	}
	if(nextLiftPos >= 4)
	{
		setLiftPosition(nextLiftPos);
	}
	else
	{
		setLiftPosition(4);
	}
	normalTeleopStuff();
	while(!isAtTargetPosition && !isStopRequested())
	{
		normalTeleopStuff();
		telemetry.addLine("We are in the goToNextPosition method");
	}
	isFlippedOutBack = true;
	robot.ungrabPlate();
	setFlipPosition(flippedAbouToPlace);
	robot.wrist.setPosition(wristWhenOut);
	if(nextPlacePos == 0)
	{
		robot.rotate.setPosition(rotateClose);
	}
	else if(nextPlacePos == 1)
	{
		robot.rotate.setPosition(rotateLeft);
	}
	else if(nextPlacePos == 2)
	{
		robot.rotate.setPosition(rotateFar);
	}
	else
	{
		robot.rotate.setPosition(rotateGrab);
	}
	setLiftPosition(nextLiftPos);
}
//--------------------------------------------------------------------------------------------------
//this method drops the block, flips the arm back in, and collapses the slides to get ready for
// the next cycle
public void place()
{
	if(currentLiftPos > 1)
	{
		setLiftPosition(currentLiftPos);
	}
	normalTeleopStuff();
	while(!isAtTargetPosition && !isStopRequested())
	{
		telemetry.addLine("we are in the place method");
		normalTeleopStuff();
	}
	double start = System.currentTimeMillis();
	setFlipPosition(flippedOut);
	while((System.currentTimeMillis() - start) < 500 && !isStopRequested())
	{
		telemetry.addLine("we are in the place method");
		normalTeleopStuff();
	}
//NORMAL FUNCTION
	if(gamepad1.right_bumper && capstoneTimer < 90000 && !capStonePlaced)
	{
		telemetry.addLine("Placing the brick");
		telemetry.update();
	}
//TIMER IS READY AND THE CAPSTONE HAS BEEN PLACED ON THE BLOCK
	else if(gamepad1.right_bumper && capStonePlaced && capstoneTimer > 90000)
	{
		telemetry.addLine("Placing the brick");
		telemetry.update();
	}
//IF THE DRIVER IS PLACING A BRICK BEFORE THE CAPSTONE BRICK
	else if(gamepad1.right_bumper && !capStonePlaced && capstoneTimer > 90000)
	{
		telemetry.addLine("Hurry it up gamer!!!");
		telemetry.update();
	}
//IF THE TIMER IS NOT READY AND THE CAPSTONE IS
	else if(gamepad1.right_bumper && capStonePlaced && capstoneTimer < 90000)
	{
		telemetry.addLine("Waiting on the Timer");
		telemetry.update();
		normalTeleopStuff();//Unsure what exactly is supposed to go right here
	}
	robot.grabber.setPosition(ungrabbed);
	blockIsGrabbed = false;
	start = System.currentTimeMillis();
	while((System.currentTimeMillis() - start) < 400 && !isStopRequested())
	{
		telemetry.addLine("we are in the place method");
		normalTeleopStuff();
	}
	isFlippedOutBack = false;
	setFlipPosition(flippedIn);
	robot.rotate.setPosition(rotateGrab);
	robot.wrist.setPosition(wristWhenIn);
	start = System.currentTimeMillis();
	while((System.currentTimeMillis() - start) < 300 && !isStopRequested())
	{
		telemetry.addLine("we are in the place method");
		normalTeleopStuff();
	}
	setLiftPosition(0);
	driveLiftToPosition();
}
//--------------------------------------------------------------------------------------------------
public void regrabBlock()
{
	isFlippedOutBack = false;
	robot.grabber.setPosition(ungrabbed);
	setLiftPosition(0);
	robot.wrist.setPosition(wristWhenIn);
	robot.rotate.setPosition(rotateGrab);
	double start = System.currentTimeMillis();
	while((System.currentTimeMillis() - start) < 300 && !isStopRequested())
	{
		normalTeleopStuff();
	}
	setFlipPosition(flippedGrab);
	start = System.currentTimeMillis();
	while((System.currentTimeMillis() - start) < 300 && !isStopRequested())
	{
		normalTeleopStuff();
	}
	setFlipPosition(flippedIn);
	start = System.currentTimeMillis();
	while((System.currentTimeMillis() - start) < 300 && !isStopRequested())
	{
		normalTeleopStuff();
	}
	robot.grabber.setPosition(grabbed);
}
//--------------------------------------------------------------------------------------------------
public void slapTheCap()
{
	robot.capStone.setPosition(capSlap);
	double start = System.currentTimeMillis();
	while((System.currentTimeMillis() - start) < 1500 && !isStopRequested())
	{
		normalTeleopStuff();
	}
	robot.capStone.setPosition(capStore);
	capStonePlaced = true;
}
//--------------------------------------------------------------------------------------------------
public double compensateAngle()
{
	angles = this.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
	this.imu.getPosition();
	currAngle = angles.firstAngle;
	return(currAngle/260);
}
//--------------------------------------------------------------------------------------------------
public void setRotateToCompensatedAngle()
{
	if(nextPlacePos == 0)
	{
		robot.rotate.setPosition(rotateClose + compensateAngle());
	}
	else if(nextPlacePos == 1)
	{
		robot.rotate.setPosition(rotateLeft + compensateAngle());
	}
	else if(nextPlacePos == 2)
	{
		robot.rotate.setPosition(rotateFar + compensateAngle());
	}
	else if(nextPlacePos == 3);
	{
		robot.rotate.setPosition(rotateGrab + compensateAngle());
	}
}
//--------------------------------------------------------------------------------------------------
//------------------------------------------------------//
//------------------------------------------------------//
//---Here is my Actual Run Op where I call my methods---//
//------------------------------------------------------//
//------------------------------------------------------//
//--------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    public void runOpMode()
    {
        turnIMU();
        driveIMU();
        robot.resetLift();
        robot.sensorColor.enableLed(true);
        setFlipPosition(flipStartPos);
        robot.rotate.setPosition(rotateGrab);
        robot.wrist.setPosition(wristWhenIn);
        robot.grabber.setPosition(ungrabbed);
        robot.ungrabPlate();
        waitForStart();
        initiateIntakeCycle();
//--------------------------------------------------------------------------------------------------
        if(opModeIsActive() && (!(isStopRequested())))
		{
			capstoneTimer = System.currentTimeMillis();
			while(opModeIsActive() && (!(isStopRequested())))
			{
				normalTeleopStuff();
				if(gamepad1.left_stick_button)
				{
					if(blockIsGrabbed)
					{
						regrabBlock();
					}
					else
					{
						initiateIntakeCycle();
					}
				}
				if(gamepad1.right_stick_button)
				{
					goToNextPosition();
				}
				if(gamepad1.right_bumper)
				{
					place();
				}
				if(gamepad1.back)
				{
					slapTheCap();
				}
			}
        }
    }

}
//--------------------------------------------------------------------------------------------------
//-------------------------------------------//
//-------------------------------------------//
//---There is No More Code Past This Point---//
//-------------------------------------------//
//-------------------------------------------//
//--------------------------------------------------------------------------------------------------