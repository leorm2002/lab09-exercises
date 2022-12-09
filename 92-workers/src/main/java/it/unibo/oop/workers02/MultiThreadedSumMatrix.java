package it.unibo.oop.workers02;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiThreadedSumMatrix implements SumMatrix {
    private final int nThread;


    public MultiThreadedSumMatrix(int nThread) {
        this.nThread = nThread;
    }


    @Override
    public double sum(double[][] matrix) {
        final int size = matrix.length % nThread + matrix.length / nThread;
        /*
         * Build a stream of workers
         */
        Set<Worker> workers = IntStream
                .iterate(0, start -> start + size)
                .limit(nThread)
                .mapToObj(start -> new Worker(matrix, start, size))
                .collect(Collectors.toSet());
        
        workers.forEach(Thread::start);
                // Join them
        workers.forEach(MultiThreadedSumMatrix::joinUninterruptibly);

                 // Get their result and sum
        return workers.stream().mapToDouble(Worker::getResult).sum();
    }

    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         * 
         * @param matrix
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startpos, final int nelem) {
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        public void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            res = Arrays.stream(matrix).skip(startpos).limit(nelem).flatMapToDouble(Arrays::stream).sum();
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public double getResult() {
            return this.res;
        }

    }


}
