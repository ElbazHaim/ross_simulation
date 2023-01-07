package org.example;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.distribution.NormalDistribution;


public class Main {
    public static void main(String[] args) {
        /*
        See run_simulation method code for simulation details.
        Main part runs multiple simulations, stores the results in addition to a statistical report.
        The final results of all runs are stored at simulations_summary.csv .
        Simulation also stores results of an individual run at simulation_output.csv .
        The statistics report is stored at stats.txt .
        Methods "next_fall" and "next_fix" are also defined in this code.
         */

        // Run multiple simulations
        int n_simulations = 1000;
        double[] data = new double[1000];
        try {
            FileWriter output = new FileWriter("simulations_summary.csv");
            output.write("Simulation ID, Runtime\n");
            for (int i = 0; i < n_simulations; i++) {
                double result = run_simulation();
                data[i] = result;
                output.write(i + ", " + result + "\n");
            }
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Calculate the mean
        double mean = StatUtils.mean(data);

        // Calculate the standard deviation
        double variance = StatUtils.variance(data);
        double stdDev = Math.sqrt(StatUtils.variance(data));

        // Calculate the z value for a 95% confidence interval
        NormalDistribution normalDist = new NormalDistribution();
        double zValue = normalDist.inverseCumulativeProbability((1 + 0.95) / 2);

        // Calculate the confidence interval
        double margin = zValue * stdDev / Math.sqrt(data.length);
        double lowerBound = mean - margin;
        double upperBound = mean + margin;

        String to_print =  String.format("Mean: %f \n", mean);
        to_print += String.format("Variance: %f \n", variance);
        to_print += String.format("Standard Deviation: %f \n", stdDev);
        to_print += "95% Confidence interval: ";
        to_print += String.format("[%f, %f]", lowerBound, upperBound);
        System.out.println(to_print);

        FileWriter output = null;
        try {
            output = new FileWriter("stats.txt");
            output.write(to_print);
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static double next_fall() {
        // Function to generate part break time
        Random rand = new Random();
        double fall_time;
        fall_time = rand.nextExponential() * 4000.0;
        return fall_time;
    }

    public static double next_fix() {
        // Function to generate part fix time
        Random rand = new Random();
        double fix_time;
        fix_time = rand.nextInt(10, 20);
        return fix_time;
    }

    public static double run_simulation() {
        int broken_parts = 0;
        int spare_parts = 2;
        int uptime = 0;

        double inf = 99999999.9;
        double[] fall = {next_fall(), next_fall(), next_fall()};
        double repair = inf;

        try {
            FileWriter output = new FileWriter("simulation_output.csv");
            output.write("Time, Event, Position, Broken Parts, Spare Parts\n");

            /*
            Shutdown occurs when the broken parts number reaches 3 - the machine needs 3 and starts with 3, and there
            are 2 spare parts, that means that in general the factory can go on as long as there are less than 3
            broken parts out of the total 5.
            Main Loop:
            Until 3 parts are broken or until uptime reaches upper limit:
            1. loop over active parts to see if they break
            2. If there are broken parts:
                A. Check if a part has been repaired
                B. If there are more broken parts, and just finished repairing one, start fixing the rest.
            3. As long as there are 3 active parts, uptime continues to count every hour.
            */

            // Check if machine is still working
            while (broken_parts < 3 && uptime <= inf) {

                // Loop over active parts
                for (int i = 0; i < 3; i++){

                    // Check if the part have broke
                    if (fall[i] < uptime) {
                        broken_parts += 1;

                        // replace part with spare one
                        fall[i] = uptime + next_fall();
                        spare_parts -= 1;

                        // Report broken part
                        output.write(uptime + ", Break, " + i + ", " + broken_parts + ", " + spare_parts + "\n");

                        // if no parts are waiting to be fixed, start fixing
                        if (broken_parts == 1) {
                            repair = uptime + next_fix();
                        }

                    }
                }

                // Check for newly fixed parts
                    if (repair < uptime) {
                        spare_parts += 1;
                        broken_parts -= 1;
                        if (broken_parts > 0) {
                            repair = uptime + next_fix();
                        } else {
                            repair = inf;
                        }

                        // Report repaired part
                        output.write(uptime + ", Repair, -, " + broken_parts + ", " + spare_parts + "\n");
                    }
                uptime += 1.0;
            }

            // Report shutdown (end of while loop)
            output.write(uptime + ", Shutdown, " + -1 + ", " + broken_parts + ", " + spare_parts + "\n");
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return uptime;
    }
}

