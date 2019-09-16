package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Disabled
public class LinearMap
{
    /* Public OpMode members */
    DcMotor FL = null;
    DcMotor FR = null;
    DcMotor BR = null;
    DcMotor BL = null;

    DcMotor EndDefectorSweeper = null;
    DcMotor HookMotor = null;

    DcMotor JointOne = null;
    DcMotor JointTwo = null;

    GyroSensor gyro;
    public CRServo D;
//--------------------------------------------------------------------------------------------------
    HardwareMap hwMap  =  null;
    public LinearMap(){}

    public void init(HardwareMap ahwMap)
    {
        hwMap  = ahwMap;
        gyro   = hwMap.get(GyroSensor.class, "gyro");

        FL = hwMap.get(DcMotor.class, "FL");
        BL = hwMap.get(DcMotor.class, "BL");
        FR = hwMap.get(DcMotor.class, "FR");
        BR = hwMap.get(DcMotor.class, "BR");

        EndDefectorSweeper = hwMap.get(DcMotor.class, "Sweeper");
        HookMotor = hwMap.get(DcMotor.class, "Hook");

        JointOne = hwMap.get(DcMotor.class, "one");
        JointTwo = hwMap.get(DcMotor.class, "two");

        D = hwMap.get(CRServo.class, "d");
        gyro = hwMap.get(GyroSensor.class, "gyro");

        JointOne.setDirection(DcMotorSimple.Direction.FORWARD);
        JointTwo.setDirection(DcMotorSimple.Direction.FORWARD);

        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        FR.setDirection(DcMotorSimple.Direction.FORWARD);
        BL.setDirection(DcMotorSimple.Direction.REVERSE);
        BR.setDirection(DcMotorSimple.Direction.FORWARD);

        FL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        FL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        FR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        BL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        BR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        gyro.resetZAxisIntegrator();
        gyro.calibrate();
    }
}