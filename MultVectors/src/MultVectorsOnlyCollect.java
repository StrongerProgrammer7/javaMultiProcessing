import mpi.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MultVectorsOnlyCollect
{
    static final int ROW = 10;
    static final int ROOT = 0;
    static int[] count;
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        Instant starts = Instant.now();
        int[] matrix = new int[ROW*ROW];
        int[] vector = new int[ROW];
        if(rank == ROOT)
        {
            for(int i=0;i<ROW * ROW;i++)
            {
                matrix[i] =  ThreadLocalRandom.current().nextInt(1,20);
            }
            for(int i=0;i<ROW;i++)
            {
                vector[i] =  ThreadLocalRandom.current().nextInt(1,20);
            }
        }

        MPI.COMM_WORLD.Bcast(vector,0,ROW,MPI.INT,ROOT);
        if(rank ==0)
            System.out.println(Arrays.toString(vector));
        int countElementsForEveryRank = getCountRowForRank(size) * ROW;

        System.out.println("Rank = " + rank + " rows = " + countElementsForEveryRank /ROW + " elem = " + countElementsForEveryRank);
        int[] localVector = new int[countElementsForEveryRank];

        int[] diplacements = new int[size];
        for(int i=1;i<size;i++)
            diplacements[i] = diplacements[i-1] + count[i-1];

        MPI.COMM_WORLD.Scatterv(matrix, 0, count, diplacements, MPI.INT,
                localVector, 0, count[rank], MPI.INT, ROOT);
        printVector(localVector,rank);

        int[] localResult = new int[countElementsForEveryRank /ROW];

        int k =0;
        for (int i = 0; i < countElementsForEveryRank /ROW; i++)
        {
            for (int j = 0; j < ROW; j++)
            {
                localResult[i] += localVector[k] * vector[j];
                k++;
            }
        }
       // System.out.println(Arrays.toString(localResult));

        int[] globalResult = new int[ROW];
        for(int i=0;i<count.length;i++)
            count[i] = count[i] / ROW;
        //System.out.println(Arrays.toString(count));
        for(int i=1;i<size;i++)
            diplacements[i] = diplacements[i-1] + count[i-1];
        //System.out.println(Arrays.toString(diplacements));
        MPI.COMM_WORLD.Gatherv(localResult, 0, countElementsForEveryRank /ROW, MPI.INT,
                globalResult, 0, count, diplacements, MPI.INT, ROOT);

        if (rank == ROOT) {
            System.out.print("Result: ");
            printVector(globalResult,ROOT);

            Duration res = Duration.between(starts,Instant.now());

            System.out.println("Time = " + res + " INT " + res.getSeconds());
        }

        MPI.Finalize();
    }

    private static int getCountRowForRank(int size)
    {
        int[] c = new int[size];
        int[] result = new int[1];
        Arrays.fill(c,(ROW/(size-1)));
        int totalSum = Arrays.stream(c).sum();

        if(totalSum > ROW)
        {
            for(int i=0;i<c.length;i++)
            {
                if(c[i]!=0)
                {
                    c[i]--;
                    totalSum--;
                }
                if(totalSum == ROW)
                    break;
                else if(i == c.length-1)
                    i = 0;
            }
           // System.out.println(" " + Arrays.toString(c));
            count = Arrays.stream(c).map((elem) -> elem * ROW).toArray();
            MPI.COMM_WORLD.Scatter(c, 0, 1, MPI.INT,
                    result, 0, 1 , MPI.INT, ROOT);

        }else
        {
            if(totalSum < ROW)
            {
                for(int i=0;i<c.length;i++)
                {
                    c[i]++;
                    totalSum++;
                    if(totalSum == ROW)
                        break;
                    else if(i == c.length-1)
                        i = 0;
                }
            }
            //System.out.println(" " + Arrays.toString(c));
            MPI.COMM_WORLD.Scatter(c, 0, 1, MPI.INT,
                    result, 0, 1 , MPI.INT, ROOT);
            count = Arrays.stream(c).map((elem) -> elem * ROW).toArray();

        }
        return result[0];
    }

    private static void printMatr(int[][] matr,int rank)
    {
        //System.out.println("Rank out matr " + rank);
        for(int i=0;i<matr.length;i++)
            printVector(matr[i],-1);
        System.out.println();
    }
    private static void printVector(int[] vector,int rank)
    {
       // if(rank!=-1)
        //    System.out.println("Rank out vector " + rank);
        for(int i=0;i<vector.length;i++)
            System.out.print(vector[i] + " ");
        System.out.println();
    }
}
