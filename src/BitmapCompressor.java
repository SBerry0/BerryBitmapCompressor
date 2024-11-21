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
     * Reads a sequence of bits from standard input, compresses them,
     * and writes the results to standard output.
     */
    public static void compress() {
//        trueEncoding();
        runLengthEncoding();
    }

    public static void trueEncoding() {
        Queue<Boolean> input = new LinkedList<Boolean>();
        while (!BinaryStdIn.isEmpty()) {
            input.add(BinaryStdIn.readBoolean());
        }
        int n = input.size();
        // Write out the length of the string using 16 bits. No need for all 32, only positive values are needed.
        BinaryStdOut.write(n, 16);

        short trueStreak = 0;
        int trueStreakStart = 0;

        for (int i = 0; i < n; i++) {
            if (input.remove()) {
                if (trueStreak == 0) {
                    trueStreakStart = i;
                }
                trueStreak++;
            } else {
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
        Queue<Boolean> input = new LinkedList<>();
        while (!BinaryStdIn.isEmpty()) {
            input.add(BinaryStdIn.readBoolean());
        }
        int n = input.size();

        int i = 0;
        int falseStreak = 0;
        int trueStreak = 0;
        while (i < n) {
            while (!input.remove()) {
                falseStreak++;
                i++;
            }
            i++;

            while (falseStreak > 255) {
                BinaryStdOut.write(255, 8);
                BinaryStdOut.write(0, 8);
                falseStreak-=255;
            }
            BinaryStdOut.write(falseStreak, 8);
            falseStreak = 0;

            while (input.remove()) {
                trueStreak++;
                i++;
            }
            i++;

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
     * Reads a sequence of bits from standard input, decodes it,
     * and writes the results to standard output.
     */
    public static void expand() {
//        trueDecoding();
        runLengthDecoding();
    }


    public static void trueDecoding() {
        short length = BinaryStdIn.readShort();
        int pos = 0;
        while (!BinaryStdIn.isEmpty()) {
            short trueStart = BinaryStdIn.readShort();
            int trueLength = BinaryStdIn.readInt(8);
            while (pos < trueStart) {
                BinaryStdOut.write(0, 1);
                pos++;
            }
            for (int i = 0; i < trueLength; i++) {
                BinaryStdOut.write(1, 1);
                pos++;
            }
        }
        // To fill the rest of the file with 0s
        for (int i = pos; i < length; i++) {
            BinaryStdOut.write(0, 1);
        }
        BinaryStdOut.close();
    }

    public static void runLengthDecoding() {
        while (!BinaryStdIn.isEmpty()) {
            int falseLength = BinaryStdIn.readInt(8);
            for (int i = 0; i < falseLength; i++) {
                BinaryStdOut.write(0, 1);
            }
            int trueLength = BinaryStdIn.readInt(8);
            for (int i = 0; i < trueLength; i++) {
                BinaryStdOut.write(1, 1);
            }
        }
        BinaryStdOut.close();
    }

    /**
     * When executed at the command-line, run {@code compress()} if the command-line
     * argument is "-" and {@code expand()} if it is "+".
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}