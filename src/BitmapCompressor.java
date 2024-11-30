/******************************************************************************
 *  Compilation:  javac BitmapCompressor.java
 *  Execution:    java BitmapCompressor - < input.bin   (compress)
 *  Execution:    java BitmapCompressor + < input.bin   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   q32x48.bin
 *                q64x96.bin
 *                mystery.bin
 *
 *  Compress or expand binary input from standard input.
 *
 *  % java DumpBinary 0 < mystery.bin
 *  8000 bits
 *
 *  % java BitmapCompressor - < mystery.bin | java DumpBinary 0
 *  1240 bits
 ******************************************************************************/

import java.util.LinkedList;
import java.util.Queue;

/**
 *  The {@code BitmapCompressor} class provides static methods for compressing
 *  and expanding a binary bitmap input.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *  @author Zach Blick
 *  @author Sohum Berry
 */
public class BitmapCompressor {
    /**
     * Sample Usage: java BitmapCompressor - 0 < in.bin > out.bin
     * | This would compress in.bin into out.bin using my encoding algorithm.
     * <p>
     * Reads a sequence of bits from standard input, compresses them,
     * and writes the results to standard output.
     */
    // Write a 0 on command line to use my encoding algorithm, write a 1 to use Sedgewick's runLengthEncoding.
    public static void compress(int encodingType) {
        if (encodingType == 0) {
            trueEncoding();
        } else if (encodingType == 1) {
            runLengthEncoding();
        }
    }

    public static void trueEncoding() {
        // Create a queue to hold all the boolean values from the inputted bitmap
        Queue<Boolean> input = new LinkedList<Boolean>();
        while (!BinaryStdIn.isEmpty()) {
            input.add(BinaryStdIn.readBoolean());
        }
        int n = input.size();
        // Write out the length of the string using 16 bits. No need for all 32, only positive values are needed.
        BinaryStdOut.write(n, 16);

        short trueStreak = 0;
        int trueStreakStart = 0;

        // Loop through every bit in the bitmap
        for (int i = 0; i < n; i++) {
            // If the bit is a 1 initialize the start of a true streak if needed
            if (input.remove()) {
                if (trueStreak == 0) {
                    trueStreakStart = i;
                }
                // Increment the number of consecutive 1s
                trueStreak++;
            } else {
                // Reset the true streak if a 0 is encountered and write out the data of the positions of 1s
                if (trueStreak > 0) {
                    BinaryStdOut.write(trueStreakStart, 16);
                    BinaryStdOut.write(trueStreak, 8);
                    trueStreak = 0;
                }
            }
        }

        BinaryStdOut.close();
    }

    public static void runLengthEncoding() {
        // Read the 1s and 0s individually into a string
        String s = "";
        while (!BinaryStdIn.isEmpty()) {
            s += (BinaryStdIn.readBoolean() ? '1' : '0');
        }
        int n = s.length();
        int position = 0;
        int falseStreak = 0;
        int trueStreak = 0;
        boolean end = false;

        while (position < n) {
            // While there is a 0
            while (s.charAt(position) == '0') {
                // Increment the 0s streak and the position
                falseStreak++;
                position++;
                // If at the final bit, set end to true and break out of the loop
                if (position==n) {
                    end = true;
                    break;
                }
            }
            // Write the bits in chunks of 225 (8 bits)
            while (falseStreak > 255) {
                BinaryStdOut.write(255, 8);
                // Alternate with 0 to write the rest of the bits that are 0
                BinaryStdOut.write(0, 8);
                // Decrement the false streak for the ones that were just written
                falseStreak-=255;
            }
            // Write the final 8 bits of the false streak, then reset it
            BinaryStdOut.write(falseStreak, 8);
            falseStreak = 0;

            // Check if the position is at the end of the string and end the loop if so
            if (end) {
                break;
            }

            // Same as above
            while (s.charAt(position) == '1') {
                trueStreak++;
                position++;
            }
            while (trueStreak > 255) {
                BinaryStdOut.write(255, 8);
                BinaryStdOut.write(0, 8);
                trueStreak-=255;
            }
            BinaryStdOut.write(trueStreak, 8);
            trueStreak=0;
        }
        BinaryStdOut.close();
    }

    /**
     * Sample Usage: java BitmapCompressor + 0 < in.bin > out.bin
     * | This would expand in.bin into out.bin using my encoding algorithm.
     * <p>
     * Reads a sequence of bits from standard input, decodes it,
     * and writes the results to standard output.
     */
    public static void expand(int decodingType) {
        if (decodingType == 0) {
            trueDecoding();
        } else if (decodingType == 1) {
            runLengthDecoding();
        }
    }

    public static void trueDecoding() {
        short length = BinaryStdIn.readShort();
        int pos = 0;
        while (!BinaryStdIn.isEmpty()) {
            // Read in where the consecutive 1s start and for how long
            short trueStart = BinaryStdIn.readShort();
            int trueLength = BinaryStdIn.readInt(8);
            // Fill in 0s while the current position is less than the start of the true streak
            while (pos < trueStart) {
                BinaryStdOut.write(0, 1);
                pos++;
            }
            // Then fill in the 1s for the length given
            for (int i = 0; i < trueLength; i++) {
                BinaryStdOut.write(1, 1);
                pos++;
            }
        }
        // Fill the rest of the file with 0s
        for (int i = pos; i < length; i++) {
            BinaryStdOut.write(0, 1);
        }
        BinaryStdOut.close();
    }

    public static void runLengthDecoding() {
        // Alternate between reading 8 bits for the 0s length and 8 bits for the 1s length
        while (!BinaryStdIn.isEmpty()) {
            // Read the number of 0s, then write them
            int falseLength = BinaryStdIn.readInt(8);
            for (int i = 0; i < falseLength; i++) {
                BinaryStdOut.write(0, 1);
            }
            // Check if there are still bits to read, otherwise end the loop
            if (BinaryStdIn.isEmpty()) { break; }
            // Read the number of 1s, then write them
            int trueLength = BinaryStdIn.readInt(8);
            for (int i = 0; i < trueLength; i++) {
                BinaryStdOut.write(1, 1);
            }
        }
        BinaryStdOut.close();
    }

    /**
     * When executed at the command-line, run {@code compress()} if the first command-line
     * argument is "-" and {@code expand()} if it is "+".
     * <p>
     * When executed at the command-line, run my algorithm if the second command-line
     * argument is "0" and Sedgewick's algorithm if it is "1".
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        if      (args[0].equals("-")) compress(Integer.parseInt(args[1]));
        else if (args[0].equals("+")) expand(Integer.parseInt(args[1]));
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}