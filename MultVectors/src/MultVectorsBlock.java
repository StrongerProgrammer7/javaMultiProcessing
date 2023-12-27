import mpi.MPI;
import mpi.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MultVectorsBlock
{
    //Разработать алгоритм вычисления произведения матрицы на вектор. Учесть наличие хвоста.
    /*
        [2][3][4] [2] = [2*2+3*3+4*1]
        [3][4][2] [3] = [3*2+4*3+2*1]
        [2][2][2] [1] = [2*2+2*3+2*1]
     */
    public static void main(String[] args) throws InterruptedException
    {
        Instant starts = Instant.now();

        int ROW = 10;

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        //System.out.println("ROW = " + ROW);

        int rowForCurRank = getCountRowForRunk(ROW,size,rank);

        if(rank==0)
        {
            int len = ROW > size ? size : ROW;
            //MPI.COMM_WORLD.Send(new int[]{col},0,1,MPI.INT,1,0);
            int[] vector = new int[ROW];
            for(int i =0;i<ROW;i++)
                vector[i] = ThreadLocalRandom.current().nextInt(1,20);
            for(int i =1;i<len;i++)
                MPI.COMM_WORLD.Send(vector,0,ROW,MPI.INT,i,0);

            //Thread.sleep(10000);
            System.out.print("Total vector = " );
            print(vector);
            int ind =0;
            for(int i=1;i<len;i++)
            {
                Status st = MPI.COMM_WORLD.Probe(i,0);
                int count = st.Get_count(MPI.INT);
                int[] buf = new int[count];
               // System.out.println("Count = " + count);
                MPI.COMM_WORLD.Recv(buf,0,count,MPI.INT,i,0);

                for(int j=0;j<count;j++)
                {
                    vector[ind] = buf[j];
                    ind++;
                    //System.out.println(buf[j]);
                }
            }
            Instant end = Instant.now();
            Duration res = Duration.between(starts,end);
            System.out.println("Time = " + res);
            System.out.print("Result = ");
            print(vector);
        }else if(rowForCurRank!=0)
        {
            int[] vectorForRanks = new int[ROW];
            MPI.COMM_WORLD.Recv(vectorForRanks,0,ROW,MPI.INT,0,0);
            System.out.println("RANK = " + rank + " matr = " + Arrays.toString(vectorForRanks));
            int[] newVec = new int[rowForCurRank];
            for(int i =0;i < rowForCurRank;i++)
            {
                int sum = 0;
                //System.out.println("Rank = " + rank);
                for(int j=0;j<ROW;j++)
                {
                    int A = ThreadLocalRandom.current().nextInt(1,20);
                    //System.out.print(A + " ");
                    sum += A * vectorForRanks[j];
                }
                //System.out.println();
                //System.out.println("Rank = " + rank + " i = " + i + " sum = " + sum);
                newVec[i] = sum;
            }
            //System.out.println("Len = " + newVec.length);
            MPI.COMM_WORLD.Send(newVec,0,newVec.length,MPI.INT,0,0);
        }
        MPI.Finalize();
    }


    private static int getCountRowForRunk(int ROW,int size,int rank)
    {
        int rowForCurRank =  ROW < size -1 ? (int)(size-1) / ROW : (int)ROW / (size-1);

        //System.out.println("Rank = " + rank + " row = " + rowForCurRank);
        int countRowForRankTotal = getTotalRowEveryRank(rowForCurRank,size);
        if(countRowForRankTotal==ROW)
            return rowForCurRank;
        boolean isMore = isMore(countRowForRankTotal,ROW);
        if(rank==0)
            getEvenRows(size,countRowForRankTotal,ROW,isMore);
        else
            return sendFromOtherRankToZero(rowForCurRank,rank);

        return rowForCurRank;
    }
    private static int getTotalRowEveryRank(int rowForCurRank,int countRank)
    {
        int countRowForRankTotal = 0;
        for(int i=0;i<countRank-1;i++)
            countRowForRankTotal+=rowForCurRank;
        return countRowForRankTotal;
    }


    private static void getEvenRows(int size, int countRowForRankTotal, int ROW, boolean isMore)
    {
        int[] buf = new int[size];
        for(int i=1;i<size;i++)
            MPI.COMM_WORLD.Recv(buf,i-1,1,MPI.INT,i,0);
        //print(buf);

        for(int i=buf.length-2;i>= 0;i--)
        {
            if(buf[i]>0 && isMore == true)
            {
                buf[i]--;
                countRowForRankTotal--;
            }else if(isMore == false)
            {
                buf[i]++;
                countRowForRankTotal++;
            }
            if(countRowForRankTotal == ROW)
                break;
            else if(i==0)
                i = buf.length-1;
        }

        for(int i=1;i<size;i++)
            MPI.COMM_WORLD.Send(buf,i-1,1,MPI.INT,i,0);
    }

    private static boolean isMore(int countRowForRankTotal,int ROW)
    {
        return countRowForRankTotal>ROW;
    }

    private static int sendFromOtherRankToZero(int rowForCurRank,int rank)
    {
        MPI.COMM_WORLD.Send(new int[]{rowForCurRank},0,1,MPI.INT,0,0);

        int[] buf = new int[1];
        MPI.COMM_WORLD.Recv(buf,0,1,MPI.INT,0,0);
        rowForCurRank = buf[0];
       // System.out.println("After check Rank = " + rank + " row = " + rowForCurRank);
        return rowForCurRank;
    }
    private static void print(int[] vector)
    {
        for(int i=0;i<vector.length;i++)
            System.out.print(vector[i] + " ");
        System.out.println();
    }
}
