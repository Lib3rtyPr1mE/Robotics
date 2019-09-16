package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;


@TeleOp(name="TeleOp", group = "test")
@Disabled
public class LinearTele extends LinearOpMode
{
    private ElapsedTime runTime = new ElapsedTime();
    LinearMap robot = new LinearMap();


    public void FaB(double pow){
        robot.FL.setPower(pow);
        robot.FR.setPower(pow);
        robot.BL.setPower(pow);
        robot.BR.setPower(pow);
    }
    public void Turn(double pow){
        robot.FL.setPower(pow);
        robot.FR.setPower(-pow);
        robot.BL.setPower(pow);
        robot.BR.setPower(-pow);
    }

    public void moveArm(int power){

    }
    public void JointTwo(int power){
        robot.JointTwo.setPower(power);
    }

    public void Cease(){
        robot.FL.setPower(0);
        robot.FR.setPower(0);
        robot.BL.setPower(0);
        robot.BR.setPower(0);
        robot.JointOne.setPower(0);
        robot.JointTwo.setPower(0);
    }

    double dPosition = 100;
    double ePos = 100;


    public void runOpMode(){

        runTime.reset();

        telemetry.addData("Status", "Initialized");
        telemetry.update();



        waitForStart();
        runTime.reset();

        while(opModeIsActive()){

            if(gamepad1.right_stick_y!=0)
                FaB(-gamepad1.right_stick_y / 2);

            else if(gamepad1.left_stick_x != 0)
                Turn(gamepad1.left_stick_x / 2);

            else
            {
                robot.FL.setPower(0);
                robot.FR.setPower(0);
                robot.BL.setPower(0);
                robot.BR.setPower(0);
            }

            if(gamepad2.right_stick_y!=0)
                robot.JointOne.setPower(gamepad2.right_stick_y);

            else if(gamepad2.left_stick_y!=0)
                robot.JointTwo.setPower(gamepad2.left_stick_y);

            else if(gamepad2.right_bumper)
                robot.D.setPower(.8);

            else if(gamepad2.left_bumper)
                robot.D.setPower(-.8);

            else if(gamepad2.right_trigger !=0)
                robot.EndDefectorSweeper.setPower(gamepad2.right_trigger);

            else if(gamepad2.left_trigger !=0)
                robot.EndDefectorSweeper.setPower(-gamepad2.left_trigger);

            else if(gamepad2.y)
                robot.HookMotor.setPower(1);

            else if(gamepad2.a)
                robot.HookMotor.setPower(-1);

            else
            {
                robot.EndDefectorSweeper.setPower(0);
                robot.HookMotor.setPower(0);

                robot.D.setPower(0);
                robot.JointOne.setPower(0);
                robot.JointTwo.setPower(0);
            }
            telemetry.update();
        }
    }
}
