/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import java.lang.reflect.Array;

/**
 *
 * @author gangsu
 */
public class MatrixUtils {

    public static final int DIRECTION_ROW = 0;
    public static final int DIRECTION_COLUMN = 1;

    public static <T extends Object> T[][] transpose(T[][] input, Class<T> type) throws InterruptedException {
        //T[][] returnMx = (T[][])(new Object[input[0].length][input.length]);
        if (input == null || input.length == 0 || input[0].length == 0) {
            return null;
        }

        T[][] returnMx = (T[][]) (Array.newInstance(type, input[0].length, input.length));
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                returnMx[j][i] = input[i][j];
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Matrix Transpose Interrupted");
                }
            }
        }
        return returnMx;
    }

    public static Object[][] transpose(Object[][] input) throws InterruptedException {
        if (input == null) {
            return null;
        }

        Object[][] obj = new Object[input[0].length][input.length];

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                obj[i][j] = input[j][i];
            }
        }
        
        return obj;
    }

    public static void fillDoubleWithGradient(Double[][] mx) {
        if (mx != null) {
            double value = 0;
            for (int i = 0; i < mx.length; i++) {
                for (int j = 0; j < mx[0].length; j++) {
                    mx[i][j] = value++;
                }
            }
        }
    }

    public static <T extends Object> T[][] transpose(final T[][] input, final Class<T> type, final Integer threadNum) throws InterruptedException {
        //T[][] returnMx = (T[][])(new Object[input[0].length][input.length]);
        if (input == null || input.length == 0 || input[0].length == 0 || threadNum == null || threadNum < 0) {
            return null;
        }

        Integer[][] rowRanges;
        Integer[][] colRanges;
        //This is still pending.
        int numRow = input.length;
        int numCol = input[0].length;

        if (numRow < threadNum || numCol < threadNum) {
            return transpose(input, type);
        }

        //compute row range
//        for (int i = 1; i < threadNum; i++) {
//            rowRanges[i - 1][0] = numRow / threadNum * (i - 1);
//            rowRanges[i - 1][1] = numRow / threadNum * i;
//        }
//        rowRanges[threadNum - 1][0] = numRow / threadNum * (threadNum - 1);
//        rowRanges[threadNum - 1][1] = numRow;
//
//        //compute col range
//        for (int i = 1; i < threadNum; i++) {
//            colRanges[i - 1][0] = numCol / threadNum * (i - 1);
//            colRanges[i - 1][1] = numCol / threadNum * i;
//        }
//        rowRanges[threadNum - 1][0] = numCol / threadNum * (threadNum - 1);
//        rowRanges[threadNum - 1][1] = numCol;

        rowRanges = rangeDivide(0, numRow, threadNum);
        colRanges = rangeDivide(0, numCol, threadNum);

        Thread[] threads = new Thread[threadNum * threadNum];
        int index = 0;

        final T[][] returnMx = (T[][]) (Array.newInstance(type, input[0].length, input.length));
        for (int i = 0; i < rowRanges.length; i++) {
            for (int j = 0; j < colRanges.length; j++) {
                final int startRow = rowRanges[i][0];
                final int endRow = rowRanges[i][1];
                final int startCol = colRanges[j][0];
                final int endCol = colRanges[j][1];
                threads[index++] = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        for (int ii = startRow; ii < endRow; ii++) {
                            for (int jj = startCol; jj < endCol; jj++) {
                                returnMx[jj][ii] = input[ii][jj];
                                if (Thread.currentThread().isInterrupted()) {
                                    //if this is interrupted, then a matrix is still returned..
                                    return;
                                }
                            }
                        }

                    }
                });

            }
        }

        for (Thread ti : threads) {
            ti.start();
        }

        for (Thread ti : threads) {
            ti.join();
        }

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        //printMatrix(rowRanges);
        return returnMx;
    }

    public static Integer[][] rangeDivide(Integer begin, Integer end, Integer divideNum) {
        if (begin == null || end == null || begin >= end || divideNum == null || divideNum <= 0) {
            return null;
        }
        Integer[][] ranges = new Integer[divideNum][2];
        int spread = end - begin;
        for (int i = 1; i < divideNum; i++) {
            ranges[i - 1][0] = spread / divideNum * (i - 1) + begin;
            ranges[i - 1][1] = spread / divideNum * i + begin;
        }
        ranges[divideNum - 1][0] = spread / divideNum * (divideNum - 1) + begin;
        ranges[divideNum - 1][1] = spread + begin;
        return ranges;
    }

    public static void printMatrix(Object[][] mx) {
        for (int i = 0; i < mx.length; i++) {
            for (int j = 0; j < mx[0].length; j++) {
                System.out.print(mx[i][j] + " ");
            }
//            System.out.println();
        }
    }

    public static <T extends Object> T[][] multiShiftMatrix(T[][] input, int target, int[][] ranges, Class<T> type) {


        return null;
    }

    public static <T extends Object> T[] multiShift(T[] input, int target, int[][] ranges, Class<T> type) {
//        Object[] result = new Object[input.length];
//        System.out.println("\nBefore shift:");
//        System.out.println(Arrays.toString(input));
        T[] result = (T[]) Array.newInstance(type, input.length);


        if (target < 0) {
            target = 0;
        }
        if (target > input.length) {
            target = input.length;
        }
        int cursor = 0;
////////////////////////////////////////////////////////////////////////////////
//        Shift to leftmost
        //shift to left most
        //if it's itself, don't do anything
        if (target <= ranges[0][0]) {
            //Copy the header
            for (int i = 0; i < target; i++) {
                result[i] = input[i];
            }
            //Copy the selected regions
            cursor = target;
            for (int i = 0; i < ranges.length; i++) {
                for (int j = ranges[i][0]; j < ranges[i][1]; j++) {
                    result[cursor++] = input[j];
                }
            }

            //System.out.println(Arrays.toString(result));

            //Copy the residual regions
            int beginIndex;
            int endIndex;
            for (int i = 0; i < ranges.length; i++) {
                if (i == 0) {
                    beginIndex = target;
                } else {
                    beginIndex = ranges[i - 1][1];
                }
                endIndex = ranges[i][0];
                for (int j = beginIndex; j < endIndex; j++) {
                    result[cursor++] = input[j];
                }
            }
            //copy the trialer
            for (int i = ranges[ranges.length - 1][1]; i < input.length; i++) {
                result[cursor++] = input[i];
            }


        } //shift to right most
        /////////////////////////////////////////////////
        //shift to right most
        else if (target >= ranges[ranges.length - 1][1]) {
            //shirt after last
            //exactly same idea but reverse
            //copy tail
            //cursor = input.length-1;
            for (int i = input.length - 1; i >= target; i--) {
                result[i] = input[i];
            }

            //copy ranges
            cursor = target - 1;
            for (int i = ranges.length - 1; i >= 0; i--) {
                for (int j = ranges[i][1] - 1; j >= ranges[i][0]; j--) {

                    result[cursor--] = input[j];
                }
            }

            //copy residual
            int beginIndex = 0;
            int endIndex = 0;
            for (int i = ranges.length - 1; i >= 0; i--) {
                if (i == ranges.length - 1) {
                    beginIndex = target;
                } else {
                    beginIndex = ranges[i + 1][0];
                }
                endIndex = ranges[i][1];
                for (int j = beginIndex - 1; j >= endIndex; j--) {
                    result[cursor--] = input[j];
                }
            }

            //copy head
            for (int i = ranges[0][0] - 1; i >= 0; i--) {
                result[cursor--] = input[i];
            }

        } //the target is within one of the ranges
        else {
            //need to call itself though, but definietly worth the array copy operations
            //either write a lot of code, or just use array copies
            T[] firstHalf = (T[]) Array.newInstance(type, target);     //new Object[target];
            T[] secondHalf = (T[]) Array.newInstance(type, input.length - target);    //new Object[input.length - target];

            for (int i = 0; i < firstHalf.length; i++) {
                firstHalf[i] = input[i];
            }

            for (int j = 0; j < secondHalf.length; j++) {
                secondHalf[j] = input[j + target];
            }

//            System.out.println(Arrays.toString(firstHalf));
//            System.out.println(Arrays.toString(secondHalf));

            //need to create new ranges as well.
            //find where the cutoff point is
            int cutIndex = -1;
            boolean withInRegion = true;
            for (int i = 0; i < ranges.length; i++) {
                if (target >= ranges[i][0] && target < ranges[i][1]) {
                    cutIndex = i;
                    withInRegion = true;
                    break;
                } else if (i < ranges.length - 1 && target >= ranges[i][1] && target < ranges[i + 1][0]) {
                    cutIndex = i;
                    withInRegion = false;
                    break;
                }
            }
            //
            int[][] firstRanges = null, secondRanges = null;
            if (withInRegion) {
                //The cut target is within region
                firstRanges = new int[cutIndex + 1][2];
                for (int i = 0; i < firstRanges.length - 1; i++) {
                    firstRanges[i][0] = ranges[i][0];
                    firstRanges[i][1] = ranges[i][1];
                }
                firstRanges[firstRanges.length - 1][0] = ranges[firstRanges.length - 1][0];
                firstRanges[firstRanges.length - 1][1] = target;

                secondRanges = new int[ranges.length - firstRanges.length + 1][2];
                secondRanges[0][0] = target;
                secondRanges[0][1] = ranges[cutIndex][1];
                for (int i = 1; i < secondRanges.length; i++) {
                    secondRanges[i][0] = ranges[i + firstRanges.length - 1][0];
                    secondRanges[i][1] = ranges[i + firstRanges.length - 1][1];
                }

            } else {
                //The cut target is between region
                firstRanges = new int[cutIndex + 1][2];
                for (int i = 0; i < firstRanges.length; i++) {
                    firstRanges[i][0] = ranges[i][0];
                    firstRanges[i][1] = ranges[i][1];
                }
                secondRanges = new int[ranges.length - firstRanges.length][2];
                for (int i = 0; i < secondRanges.length; i++) {
                    secondRanges[i][0] = ranges[i + firstRanges.length][0];
                    secondRanges[i][1] = ranges[i + firstRanges.length][1];
                }
            }

            for (int i = 0; i < secondRanges.length; i++) {
                secondRanges[i][0] = secondRanges[i][0] - target;
                secondRanges[i][1] = secondRanges[i][1] - target;
            }




//            System.out.println("First ranges");
//            for (int i = 0; i < firstRanges.length; i++) {
//                System.out.println(firstRanges[i][0] + "," + firstRanges[i][1]);
//            }
//
//            System.out.println("Second ranges");
//            for (int i = 0; i < secondRanges.length; i++) {
//                System.out.println(secondRanges[i][0] + "," + secondRanges[i][1]);
//            }


            T[] firstHalfShifted = multiShift(firstHalf, firstHalf.length, firstRanges, type);
            for (int i = 0; i < firstHalfShifted.length; i++) {
                result[i] = firstHalfShifted[i];
            }

            T[] secondHalfShifted = multiShift(secondHalf, 0, secondRanges, type);
            for (int i = 0; i < secondHalfShifted.length; i++) {
                result[i + firstHalfShifted.length] = secondHalfShifted[i];
            }


        }
//        System.out.println("\nAfter shift:");
//        System.out.println(Arrays.toString(result));
        return result;
    }

    //Primitive int version
    private static int[] _multiShift(int[] input, int target, int[][] ranges) {
        int[] result = new int[input.length];
//        System.out.println("\nBefore shift:");
//        System.out.println(Arrays.toString(input));
        if (target < 0) {
            target = 0;
        }
        if (target > input.length) {
            target = input.length;
        }
        int cursor = 0;
////////////////////////////////////////////////////////////////////////////////
//        Shift to leftmost
        //shift to left most
        //if it's itself, don't do anything
        if (target <= ranges[0][0]) {
            //Copy the header
            for (int i = 0; i < target; i++) {
                result[i] = input[i];
            }
            //Copy the selected regions
            cursor = target;
            for (int i = 0; i < ranges.length; i++) {
                for (int j = ranges[i][0]; j < ranges[i][1]; j++) {
                    result[cursor++] = input[j];
                }
            }

            //System.out.println(Arrays.toString(result));

            //Copy the residual regions
            int beginIndex;
            int endIndex;
            for (int i = 0; i < ranges.length; i++) {
                if (i == 0) {
                    beginIndex = target;
                } else {
                    beginIndex = ranges[i - 1][1];
                }
                endIndex = ranges[i][0];
                for (int j = beginIndex; j < endIndex; j++) {
                    result[cursor++] = input[j];
                }
            }
            //copy the trialer
            for (int i = ranges[ranges.length - 1][1]; i < input.length; i++) {
                result[cursor++] = input[i];
            }


        } //shift to right most
        /////////////////////////////////////////////////
        //shift to right most
        else if (target >= ranges[ranges.length - 1][1]) {
            //shirt after last
            //exactly same idea but reverse
            //copy tail
            //cursor = input.length-1;
            for (int i = input.length - 1; i >= target; i--) {
                result[i] = input[i];
            }

            //copy ranges
            cursor = target - 1;
            for (int i = ranges.length - 1; i >= 0; i--) {
                for (int j = ranges[i][1] - 1; j >= ranges[i][0]; j--) {

                    result[cursor--] = input[j];
                }
            }

            //copy residual
            int beginIndex = 0;
            int endIndex = 0;
            for (int i = ranges.length - 1; i >= 0; i--) {
                if (i == ranges.length - 1) {
                    beginIndex = target;
                } else {
                    beginIndex = ranges[i + 1][0];
                }
                endIndex = ranges[i][1];
                for (int j = beginIndex - 1; j >= endIndex; j--) {
                    result[cursor--] = input[j];
                }
            }

            //copy head
            for (int i = ranges[0][0] - 1; i >= 0; i--) {
                result[cursor--] = input[i];
            }

        } //the target is within one of the ranges
        else {
            //need to call itself though, but definietly worth the array copy operations
            //either write a lot of code, or just use array copies
            int[] firstHalf = new int[target];
            int[] secondHalf = new int[input.length - target];

            for (int i = 0; i < firstHalf.length; i++) {
                firstHalf[i] = input[i];
            }

            for (int j = 0; j < secondHalf.length; j++) {
                secondHalf[j] = input[j + target];
            }

//            System.out.println(Arrays.toString(firstHalf));
//            System.out.println(Arrays.toString(secondHalf));

            //need to create new ranges as well.
            //find where the cutoff point is
            int cutIndex = -1;
            boolean withInRegion = true;
            for (int i = 0; i < ranges.length; i++) {
                if (target >= ranges[i][0] && target < ranges[i][1]) {
                    cutIndex = i;
                    withInRegion = true;
                    break;
                } else if (i < ranges.length - 1 && target >= ranges[i][1] && target < ranges[i + 1][0]) {
                    cutIndex = i;
                    withInRegion = false;
                    break;
                }
            }
            //
            int[][] firstRanges = null, secondRanges = null;
            if (withInRegion) {
                //The cut target is within region
                firstRanges = new int[cutIndex + 1][2];
                for (int i = 0; i < firstRanges.length - 1; i++) {
                    firstRanges[i][0] = ranges[i][0];
                    firstRanges[i][1] = ranges[i][1];
                }
                firstRanges[firstRanges.length - 1][0] = ranges[firstRanges.length - 1][0];
                firstRanges[firstRanges.length - 1][1] = target;

                secondRanges = new int[ranges.length - firstRanges.length + 1][2];
                secondRanges[0][0] = target;
                secondRanges[0][1] = ranges[cutIndex][1];
                for (int i = 1; i < secondRanges.length; i++) {
                    secondRanges[i][0] = ranges[i + firstRanges.length - 1][0];
                    secondRanges[i][1] = ranges[i + firstRanges.length - 1][1];
                }

            } else {
                //The cut target is between region
                firstRanges = new int[cutIndex + 1][2];
                for (int i = 0; i < firstRanges.length; i++) {
                    firstRanges[i][0] = ranges[i][0];
                    firstRanges[i][1] = ranges[i][1];
                }
                secondRanges = new int[ranges.length - firstRanges.length][2];
                for (int i = 0; i < secondRanges.length; i++) {
                    secondRanges[i][0] = ranges[i + firstRanges.length][0];
                    secondRanges[i][1] = ranges[i + firstRanges.length][1];
                }
            }

            for (int i = 0; i < secondRanges.length; i++) {
                secondRanges[i][0] = secondRanges[i][0] - target;
                secondRanges[i][1] = secondRanges[i][1] - target;
            }




//            System.out.println("First ranges");
//            for (int i = 0; i < firstRanges.length; i++) {
//                System.out.println(firstRanges[i][0] + "," + firstRanges[i][1]);
//            }
//
//            System.out.println("Second ranges");
//            for (int i = 0; i < secondRanges.length; i++) {
//                System.out.println(secondRanges[i][0] + "," + secondRanges[i][1]);
//            }


            int[] firstHalfShifted = _multiShift(firstHalf, firstHalf.length, firstRanges);
            for (int i = 0; i < firstHalfShifted.length; i++) {
                result[i] = firstHalfShifted[i];
            }

            int[] secondHalfShifted = _multiShift(secondHalf, 0, secondRanges);
            for (int i = 0; i < secondHalfShifted.length; i++) {
                result[i + firstHalfShifted.length] = secondHalfShifted[i];
            }


        }

//        System.out.println("\nAfter shift:");
//        System.out.println(Arrays.toString(result));
        return result;
    }
}
