package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;
import com.vuforia.HINT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

@Autonomous(name="Red Block", group = "Red")
//@Disabled
public class redBlock extends LinearOpMode
{
    RevMap robot = new RevMap();
    Orientation angles;
    BNO055IMU imu;

    float currHeading = 0;
    double Circ = 11.97;
    double Steps = 1160;

    //Inches -> Steps  = #Inches * (1120/11.97)
    //Steps  -> Inches = #Steps  * (11.97/1120)

    double drive = 0;
    double turn  = 0;

    double distGone   = 0;
    double distRemain = 0;
    double totField   = -4000;//length * ((1/11.97) * 1120); = steps per inch ------ 144in = 13473steps
    double testField  = -842.105;//This is 9 inches in steps - THe distance from the bot on the XZ Axis

    double blockLength   = 748.53;//My fake length of a single 8 inch block
    double distMultipler = -6;

    boolean isExtended = false;
    boolean isVertical = false;
    boolean inView     = false;

    private VuforiaLocalizer vuforiaLocalizer;
    private VuforiaLocalizer.Parameters parameters;
    private VuforiaTrackables visionTargets;
    private VuforiaTrackable target;
    private VuforiaTrackableDefaultListener listener;

    private OpenGLMatrix lastKnownLocation;
    private OpenGLMatrix phoneLocation;

    private static final String VUFORIA_KEY = "AZ6Zar7/////AAABmb9BpTFpR0aao8WchstmN7g6gEQUqWGKJOgwV0UnhrDJwzv1nw8KkSFm4bLbbd/e63bMkh4k2W5raskv2je6UOaSviD58AJtw7RiTt/T1hmt/Row6McUnaoB4KLMoADScEMRa6EnJuW2fMeSgFFy8554WHyYai9AjCfoF3MY4BXSYhZmAx/Y/8fSPBqsbfBxSs5sBZityMz6XsraptRFNQVuRuQlo19wDUc4eU3Eq9D0R1QxiFPxv8yxS6x1jN4rwfkkQBl9eQzNI0/FxSr7Caig9WOwrc65x1+3Op7UmUapHboIn+oRKlOktmT98sGtTBpxY/nz6IV9B6UTjquUNwS3Yu5eRJiu5IZoNWtuxjFA";

    private float robotX = 0;
    private float robotY = 0;
    private float robotAngle = 0;
    //--------------------------------------------------------------------------------------------------
//----------------------------------------//
//----------------------------------------//
//---These are all of my Called Methods---//gyro.getHeading()
//----------------------------------------//
//----------------------------------------//
//--------------------------------------------------------------------------------------------------
private void imuInit()
{
    BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
    parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
    parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
    parameters.calibrationDataFile = "BNO055IMUCalibration.json";
    parameters.loggingEnabled = true;
    parameters.loggingTag = "IMU";
    parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

    robot.init(hardwareMap);
    imu = hardwareMap.get(BNO055IMU.class, "imu");
    imu.initialize(parameters);
}
//--------------------------------------------------------------------------------------------------
private void setupVuforia()
{
    parameters = new VuforiaLocalizer.Parameters(R.id.cameraMonitorViewId);
    parameters.vuforiaLicenseKey = VUFORIA_KEY;
    parameters.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;
    parameters.useExtendedTracking = false;
    vuforiaLocalizer = ClassFactory.createVuforiaLocalizer(parameters);

    visionTargets = vuforiaLocalizer.loadTrackablesFromAsset("Skystone");
    Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 4);

    target = visionTargets.get(0);
    target.setName("Wheels Target");
    target.setLocation(createMatrix(0, 500, 0, 90, 0, 90));
    phoneLocation = createMatrix(0, 225, 0, 90, 0, 0);

    listener = (VuforiaTrackableDefaultListener) target.getListener();
    listener.setPhoneInformation(phoneLocation, parameters.cameraDirection);
}
private OpenGLMatrix createMatrix(float x, float y, float z, float u, float v, float w)
{
    return OpenGLMatrix.translation(x, y, z).multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, u, v, w));
}
private String formatMatrix(OpenGLMatrix matrix)
{
    return matrix.formatAsTransform();
}
public boolean vufoCrap()
{
    OpenGLMatrix latestLocation = listener.getUpdatedRobotLocation();

    if(latestLocation != null)
    {
        lastKnownLocation = latestLocation;
    }
    float[] coordinates = lastKnownLocation.getTranslation().getData();

    robotX = coordinates[0];
    robotY = coordinates[1];
    robotAngle = Orientation.getOrientation(lastKnownLocation, AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES).thirdAngle;

    telemetry.addData("Tracking " + target.getName(), listener.isVisible());
    telemetry.addData("Last Known Location", formatMatrix(lastKnownLocation));
    telemetry.update();
    return listener.isVisible();
}
//--------------------------------------------------------------------------------------------------
private double angleBoi()
{
    telemetry.addLine().addData("Heading",currHeading);
    telemetry.update();
    angles = this.imu.getAngularOrientation(AxesReference.INTRINSIC,AxesOrder.ZYX,AngleUnit.DEGREES);
    this.imu.getPosition();
    currHeading = angles.firstAngle;
    return currHeading;
}
//--------------------------------------------------------------------------------------------------
public void moveDistance(double length, double power)
{
    double totDistInSteps = (((length / 11.97) * 1120) * -1);

    double leftPower;
    double rightPower;

    if(totDistInSteps < robot.front_right.getCurrentPosition())
    {
        addMultiplier();
        while(opModeIsActive() && (!(isStopRequested())) && totDistInSteps < robot.front_right.getCurrentPosition())
        {
            telemetry.addData("distRemain",distRemain);
            telemetry.addData("currSteps",robot.front_right.getCurrentPosition());
            telemetry.addData("distMultiplier", distMultipler);
            angleBoi();
            drive = -power;
            turn  = .05 * currHeading;
            leftPower    = Range.clip(drive - turn, -1.0, 1.0);
            rightPower   = Range.clip(drive + turn, -1.0, 1.0);

            robot.front_right.setPower(rightPower);
            robot.front_left.setPower(leftPower);
            robot.back_right.setPower(rightPower);
            robot.back_left.setPower(leftPower);
        }
        robot.Halt();
        robot.resetEncoder();
    }

    else if(totDistInSteps > robot.front_right.getCurrentPosition())
    {
        addMultiplier();
        while(opModeIsActive() && (!(isStopRequested())) && totDistInSteps > robot.front_right.getCurrentPosition())
        {
            telemetry.addData("----distRemain",distRemain);
            telemetry.addData("----currSteps",robot.front_right.getCurrentPosition());
            telemetry.addData("----distMultiplier", distMultipler);
            angleBoi();
            drive = power;
            turn  = .05 * currHeading;
            leftPower    = Range.clip(drive - turn, -1.0, 1.0);
            rightPower   = Range.clip(drive + turn, -1.0, 1.0);

            robot.front_right.setPower(rightPower);
            robot.front_left.setPower(leftPower);
            robot.back_right.setPower(rightPower);
            robot.back_left.setPower(leftPower);
        }
        robot.Halt();
        robot.resetEncoder();
    }
}
//--------------------------------------------------------------------------------------------------
public void armUp(double length)
{
    double totDistInSteps = 1120 * length;
//3 inches --93.567
    while (totDistInSteps > robot.arm.getCurrentPosition() && (!(isStopRequested())))
    {
        telemetry.update();
        robot.arm.setPower(.7);
    }
    isExtended = true;
    robot.arm.setPower(0);
    robot.resetArm();
}
//--------------------------------------------
public void armDown(double length)
{
    double totDistInSteps = -1120 * length;

    while(totDistInSteps < robot.arm.getCurrentPosition() && (!(isStopRequested())))
    {
        telemetry.update();
        robot.arm.setPower(-.7);
    }
    isExtended = false;
    robot.arm.setPower(0);
    robot.resetArm();
}
//--------------------------------------------------------------------------------------------------
private void liftUp()
{
    double totDistInSteps = -787;

    while(totDistInSteps < robot.lift.getCurrentPosition() && (!(isStopRequested())))
    {
        telemetry.update();
        robot.lift.setPower(-.5);
    }
    isVertical = true;
    robot.lift.setPower(0);
    robot.resetLift();
}
//--------------------------------------------
private void liftDown()
{
    double totDistInSteps = 787;

    while(totDistInSteps > robot.lift.getCurrentPosition() && (!(isStopRequested())))
    {
        telemetry.update();
        robot.lift.setPower(.5);
    }
    isVertical = false;
    robot.lift.setPower(0);
    robot.resetLift();
}
//--------------------------------------------------------------------------------------------------
private void turnAngle(double angle)
{
    if(angle > 0)
    {
        while(angle >= currHeading && (!(isStopRequested())))
        {
            telemetry.addLine().addData("Heading", currHeading);
            telemetry.update();
            angles = this.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            this.imu.getPosition();
            currHeading = angles.firstAngle;
            robot.turnLeft(.6);
        }
        imuInit();
    }

    else if(angle < 0)
    {
        while(angle <= currHeading && (!(isStopRequested())))
        {
            telemetry.addLine().addData("---Heading", currHeading);
            telemetry.update();
            angles = this.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            this.imu.getPosition();
            currHeading = angles.firstAngle;
            robot.turnRight(.6);
        }
        imuInit();
    }
    currHeading = 0;
}
//--------------------------------------------------------------------------------------------------
public boolean checkSight()
{
    if(listener.isVisible())
    {
        inView = true;
        getBlock();
    }
    else
    {
        inView = false;
        moveDistance(6.6,.6);
    }
    return inView;
}
//--------------------------------------------------------------------------------------------------
public double addMultiplier()
{
    distMultipler = distMultipler + 1;
    return distMultipler;
}
//--------------------------------------------------------------------------------------------------
public void checkEncoder()
{
    while(opModeIsActive() && (!(isStopRequested())))
    {
        moveDistance(17.2,1);
        turnAngle(-76);
        moveDistance(7.5,1);
        if(inView == false)
        {
            while(inView == false && (!(isStopRequested())))
            {
                checkSight();
                sleep(1000);
            }
        }
    }
}
//--------------------------------------------------------------------------------------------------
public void checkDistance()
{
    distGone   = (467.83 * distMultipler) * (-1);//5 is the length in inches I travel per run = 467.83
    distRemain = ((testField + distGone) * (11.97/1120)) * (-1);
    moveDistance(distRemain,1);
}
//--------------------------------------------------------------------------------------------------
//THIS IS FOR TESTING CODE//
//--------------------------------------------------------------------------------------------------
    private void getBlock()//Needs to go 6000 steps remaining distance
    {
        robot.Halt();
        turnAngle(86);
        robot.openClaw();
        moveDistance(6, .3);
        liftUp();
        armUp(.4);
        moveDistance(8,.3);
        robot.closeClaw();
        sleep(300);
        armDown(-.4);
        liftDown();
        moveDistance(-11,.3);
        turnAngle(-86);
        checkDistance();//----------
        turnAngle(80);
        liftUp();
        armUp(3);
        moveDistance(16,.3);
        robot.openClaw();
        armDown(3);
        moveDistance(-35,.5);
        armUp(1);
        moveDistance(-3,.5);
        turnAngle(90);
        armDown(1);
        liftDown();
        robot.closeClaw();
        moveDistance(40,.5);
        robot.Halt();
        stop();
    }
//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
//----------------------------------------//
//----------------------------------------//
//---No More Methods Are Made Past This---//
//----------------------------------------//
//----------------------------------------//
//--------------------------------------------------------------------------------------------------
































































    //--------------------------------------------------------------------------------------------------
    public void runOpMode()
    {
        imuInit();
        setupVuforia();
        lastKnownLocation = createMatrix(0, 500, 0, 90, 0, 90);


        telemetry.addData("Status","Initialized");
        telemetry.update();
        waitForStart();
        visionTargets.activate();
//--------------------------------------------------------------------------------------------------
        while(opModeIsActive() && (!(isStopRequested())))
        {
//----------------------------------
            vufoCrap();
            checkEncoder();
//----------------------------------
        }
    }
}
//--------------------------------------------------------------------------------------------------