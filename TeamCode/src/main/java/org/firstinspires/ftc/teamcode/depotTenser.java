/* Copyright (c) 2018 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.robotcontroller.external.samples;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import java.util.List;

//import static com.sun.tools.doclint.Entity.and;

@Autonomous(name = "depotTenser", group = "Concept")
@Disabled
public class depotTenser extends LinearOpMode
{

    private ElapsedTime runtime = new ElapsedTime();
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    private static final String VUFORIA_KEY = " AT0ySZn/////AAABGVDoF4gdNkZugx61WeftYqVhwz6Leeu2a1cWYQR+08xsATI6GQf3vvrvynP8JpemukCajxoFg32bkspzJx8g6uNBgHlQsFPxmFMJ8b4V/fDFTRSpy+vMOzIMoV2CHuitvtyrn/a6AsPUWczm5rsTqcCzAUEL6YD0xrzXkvaNJBzm3Jq5BkUW2ualta+LldpZ0ho/rdkDuyp6xOjsSvAbsjIDkQt807jlfYLBJAsaJNqRnQUU4mZMzl5aJsr+VnUbTfeev943zeK34ENEQzW1jCeCnLTsuWGmOd7QP+gF1YhxUr0A0s5Tr6+v8QntHPwYHp7QRpRErFqkst/OxbA4omIuLTsC41FFwYOr5YxDHhyl";
    DcMotor FrontLeft = null;
    DcMotor FrontRight = null;
    DcMotor BackLeft = null;
    DcMotor BackRight = null;

    double distPerRot = (Math.PI * 3.8125);
    double stepsPerRot = 1120;
    double lengthOfField = 12 * 12;
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    private ModernRoboticsI2cGyro gyro;
    private boolean Left = false, Right = false, Center = false;
//----------------------------------------

    //Reset all encoder values
    public void resetEncoder()
    {
        FrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        FrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        FrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        BackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        BackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    //Stop
    public void Stop()
    {
        FrontLeft.setPower(0);
        FrontRight.setPower(0);
        BackLeft.setPower(0);
        BackRight.setPower(0);
    }

    //Forward
    public void Forward(double power)
    {
        FrontLeft.setPower(power);
        FrontRight.setPower(power);
        BackLeft.setPower(power);
        BackRight.setPower(power);
    }

    public void Backward(double power)
    {
        FrontLeft.setPower(-power);
        FrontRight.setPower(-power);
        BackLeft.setPower(-power);
        BackRight.setPower(-power);
    }

    public void LTurn(double power)
    {
        FrontLeft.setPower(-power);
        FrontRight.setPower(power);
        BackLeft.setPower(-power);
        BackRight.setPower(power);
    }

    public void RTurn(double power)
    {
        FrontLeft.setPower(power);
        FrontRight.setPower(-power);
        BackLeft.setPower(power);
        BackRight.setPower(-power);
    }
//---------------------------------------------------------------------------------------------
//This method is for moving a distance in inches USING ENCODERS
    public void moveDistance(double power, double length)
    {
        //Total distance per 1 rotation is the product of pi * our wheel diameter
        double distPerRot = Math.PI * 3.8125;
        //There are this many steps per each rotation of NeverRest 20:1 dc motors
        double stepsPerRot = 1120;

        //This tells us exactly how many steps we need to travel
        double totDistInSteps = ((length / distPerRot) * stepsPerRot);

        //IF THE NUMBER IS A NEGATIVE NUMBER WE GO FORWARD!
        if (totDistInSteps > 0)
        {
            //Move forward until we over shoot
            while (totDistInSteps >= BackLeft.getCurrentPosition())
            {
                Forward(power);
                telemetry.addData("power", BackLeft.getCurrentPosition());
                telemetry.update();
            }
        }

        //IF THE NUMBER IS A NEGATIVE NUMBER WE GO BACKWARD!
        else if (totDistInSteps < 0)
        {
            //Move backward until we over shoot
            while (totDistInSteps <= BackLeft.getCurrentPosition())
            {
                Backward(power);
                telemetry.addData("power", BackLeft.getCurrentPosition());
                telemetry.update();
            }
        }

        //reset our encoder values because we may need to do more encoder driving
        Stop();
        resetEncoder();
        sleep(1000);
    }
//---------------------------------------------------------------------------------------------    @Override
    public void runOpMode() throws InterruptedException
    {

        runtime.milliseconds();

        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector())
        {
            initTfod();
        }

        else
        {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();
        runtime.reset();
        waitForStart();

        if (opModeIsActive())
        {
            /** Activate Tensor Flow Object Detection. */
            if (tfod != null)
            {
                tfod.activate();
            }

            //This is for our configuration in the robot
            FrontLeft = hardwareMap.get(DcMotor.class, "FL");
            FrontRight = hardwareMap.get(DcMotor.class, "FR");
            BackLeft = hardwareMap.get(DcMotor.class, "BL");
            BackRight = hardwareMap.get(DcMotor.class, "BR");

            //Declaring the gyro
            gyro = hardwareMap.get(ModernRoboticsI2cGyro.class, "gyro");

            //Set which direction is forward and backwards for our wheels
            FrontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
            FrontRight.setDirection(DcMotorSimple.Direction.FORWARD);
            BackLeft.setDirection(DcMotorSimple.Direction.REVERSE);
            BackRight.setDirection(DcMotorSimple.Direction.FORWARD);

            //Resetting all of the individual encoders to 0
            FrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            FrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            BackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            BackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            //Here, we tell the robot that we are using encoders and to pull data from them
            FrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            FrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            BackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            BackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //THis resets our gyro code and calibrates it to 0
            gyro.resetZAxisIntegrator();
            gyro.calibrate();

            //This updates our gyro heading post resetting it
            telemetry.addData("Heading", gyro.getHeading());
            telemetry.update();

            //This if for COUNTING the number of encoder steps
            waitForStart();
            runtime.reset();
//--------------------------------------------------------------------------------------------------------------
            while (opModeIsActive())
            {
                runtime.reset();
//----------------------------------------
                if (Left)
//----------------------------------------
                {
                    //Turn to face cube
                    while (!(gyro.getHeading() >= 30 && gyro.getHeading() <= 35))
                    {
                        LTurn(.1);
                    }

                    //Move to knock off cube
                    moveDistance(.5,-27);
                    sleep(1000);
                    Stop();

                    //To back off of the block
                    moveDistance(.5,1);

                    while(gyro.getHeading() >= 120 && gyro.getHeading() >= 130)
                    {
                        RTurn(.1);
                    }

                    moveDistance(.5,-20);

                    //THIS IS A TEAM MARKER SLEEP!
                    sleep(4000);

                    moveDistance(1,140);
                    stop();
                }
//----------------------------------------
                else if (Right)
//----------------------------------------
                {
                    //Rotate towards the Gold Mineral
                    while (!(gyro.getHeading() >= 335 && gyro.getHeading() <= 340))
                    {
                        RTurn(.1);
                    }
                    //Move to knock off Cube
                    moveDistance(.5,-15);
                    sleep(1000);
                    Stop();

                    while(!(gyro.getHeading() >= 43 && gyro.getHeading() <= 48))
                    {
                        LTurn(.1);
                    }

                    moveDistance(.5,-23);

                    //THIS SLEEP IS FOR THE TEAM MARKER
                    sleep(1000);

                    while(!(gyro.getHeading() >= 126 && gyro.getHeading() <= 131))
                    {
                        LTurn(.1);
                    }

                    moveDistance(1,-90);
                    Stop();
                    stop();
                }
//----------------------------------------
                else if (Center)
//----------------------------------------
                {
                    //This moves the robot forward
                    moveDistance(.5,-27);

                    //THIS SLEEP IS FOR THE TEAM MARKER
                    sleep(2000);

                    //Back up so the block is not stuck to the bot
                    moveDistance(.5,1);
                    Stop();

                    //Turn to face the red Crater after sampling
                    while(!(gyro.getHeading() >= 128 && gyro.getHeading() <= 133))
                    {
                        telemetry.addData("heading", gyro.getHeading());
                        LTurn(.2);
                    }
                    Stop();

                    //Got crater on the red side
                    moveDistance(1,-140);
                    Stop();
                    stop();
                }
//---------------------------------------
                else
                    {
                    if (tfod != null)
                    {
                        List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                        if (updatedRecognitions != null)
                        {
                            telemetry.addData("# Object Detected", updatedRecognitions.size());
                            int goldMineralX = -100;

                            sleep(2000);

                            if(updatedRecognitions.size() != 0)
                            {
                                runtime.reset();
                                while (runtime.milliseconds() < 3000)
                                {
                                    for (Recognition recognition : updatedRecognitions)
                                    {

                                        if (recognition.getLabel().equals(LABEL_GOLD_MINERAL))
                                        {
                                            goldMineralX = (int) recognition.getTop();

                                            if (goldMineralX > -100 && goldMineralX < 600)
                                            {
                                                telemetry.addData("Pos:", "Right");
                                                telemetry.addData("MineralPos: ", goldMineralX);
                                                telemetry.update();
                                            }

                                            else if (goldMineralX > +600 && goldMineralX < 1100)
                                            {
                                                telemetry.addData("Pos:", "Center");
                                                telemetry.addData("MineralPos: ", goldMineralX);
                                                telemetry.update();
                                            }

                                            else
                                            {
                                                telemetry.addData("Pos:", "Left");
                                                telemetry.addData("MineralPos: ", goldMineralX);
                                                telemetry.update();
                                            }
                                        }
                                        telemetry.addData("MineralPos: ", goldMineralX);
                                        telemetry.update();
                                    }
                                }
                            }

                            else
                            {
                                runtime.reset();
                                if(runtime.milliseconds() < 3000)
                                {
                                    telemetry.addData("Pos:", "Left");
                                    Left = true;
                                }
                            }

                            telemetry.addData("MineralPos: ", goldMineralX);

                            if (goldMineralX > -100 && goldMineralX < 600)
                            {
                                telemetry.addData("Pos:", "Right");
                                Right = true;
                            }

                            else if (goldMineralX >+ 600 && goldMineralX < 1200)
                            {
                                telemetry.addData("Pos:", "Center");
                                Center = true;
                            }

                            else
                            {
                                telemetry.addData("Pos:", "Left");
                                Left = true;
                            }
                            telemetry.update();
                        }
                    }
                }
            }
        }

        if (tfod != null)
        {
            tfod.shutdown();
        }
    }


    private void initVuforia()
    {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.FRONT;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod()
    {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}

//ethan.hampton@msd.oregonk-12.net
//NHS-Programming@hotmail.com