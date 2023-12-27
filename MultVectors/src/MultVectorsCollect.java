import mpi.MPI;
import mpi.Request;
import mpi.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MultVectorsCollect
{
    static final int ROOT = 0;
    static final int ROW  = 3;
    static int attempt = 0;
    static Duration[] time = new Duration[4];
    public static void main(String[] args) throws InterruptedException
    {
        MPI.Init(args);

        final int rank = MPI.COMM_WORLD.Rank();
        final int size = MPI.COMM_WORLD.Size();

        while(true)
        {
            Instant starts = Instant.now();


            int rowForCurRank = getCountRowForRank(size,rank);
            //System.out.println("Rank = " + rank + " row = " + rowForCurRank);

            int[] vector = getVector(rowForCurRank,rank,size);
            MPI.COMM_WORLD.Bcast(vector,0,ROW,MPI.INT,ROOT);

            if(rank==0)
            {

                Request[] requests = new Request[ROW];
                int[] result = new int[ROW];
                int countElem = 0;

                for(int i=1;i<=rowForCurRank;i++)
                {
                    Status status = MPI.COMM_WORLD.Probe(i,2);
                    requests[i-1] = MPI.COMM_WORLD.Irecv(result,countElem,status.Get_count(MPI.INT),MPI.INT,i,2);
                    countElem +=status.Get_count(MPI.INT);
                }
                Request.Waitall(requests);
                print(result);

                Instant end = Instant.now();
                Duration res = Duration.between(starts,end);

                time[attempt] = res;
                System.out.println("Time = " + res + "INT " + res.getSeconds());

            }else if(rowForCurRank!=0)
            {

                int[] newVec = new int[rowForCurRank];
                for(int i =0;i < rowForCurRank;i++)
                {
                    int sum = 0;
                    //System.out.println("Rank = " + rank);
                    for(int j=0;j<ROW;j++)
                    {
                        int A = ThreadLocalRandom.current().nextInt(1,20);
                        //System.out.print(A + " ");
                        sum += A * vector[j];
                    }
                    //System.out.println();
                    //System.out.println("Rank = " + rank + " i = " + i + " sum = " + sum);
                    newVec[i] = sum;
                }
                //System.out.println("Len = " + newVec.length);
                MPI.COMM_WORLD.Isend(newVec,0,newVec.length,MPI.INT,ROOT,2);
            }
            attempt++;
            if(attempt>1)
                break;
        }
//        if(rank == 0)
//        {
//            int sum = 0;
//            for(int i=0;i<time.length;i++)
//            {
//                sum+=time[i].getSeconds();
//            }
//            System.out.println("sum = " + sum + " average = " + sum/attempt);
//        }

        MPI.Finalize();
    }

    private static int[] getVector(int rows,int rank,int size)
    {
        if(rank==ROOT)
        {
            int[] buf = new int[ROW];
            Request[] requests = new Request[size];
            int countPaste = 0;
            for(int i=1;i<size;i++)
            {
                Status st = MPI.COMM_WORLD.Probe(i,0);
                requests[i] = MPI.COMM_WORLD.Irecv(buf,countPaste,st.Get_count(MPI.INT),MPI.INT,i,0);
                countPaste += st.Get_count(MPI.INT);
            }
            Request.Waitall(requests);

            for(int i=1;i<size;i++)
                MPI.COMM_WORLD.Isend(buf,0,ROW,MPI.INT,i,0);
            return buf;
        }else
        {
            int[] buf = new int[rows];
            for(int i=0;i<rows;i++)
                buf[i] = ThreadLocalRandom.current().nextInt(1,20);
            MPI.COMM_WORLD.Isend(buf,0,rows,MPI.INT,ROOT,0);
            int[] buf2 = new int[ROW];
            Request req = MPI.COMM_WORLD.Irecv(buf2,0,ROW,MPI.INT,ROOT,0);
            req.Wait();
            return buf2;
        }
    }
    private static int getCountRowForRank(int size, int rank)
    {
        int rowForCurRank =  ROW < size -1 ? ((size-1) / ROW) : (ROW / (size-1));
        if(rank==ROOT)
            rowForCurRank = 0;

        int[] countRowForRankTotal = new int[]{ getTotalRowEveryRank(rowForCurRank)};

        MPI.COMM_WORLD.Bcast(countRowForRankTotal,0,1,MPI.INT,ROOT);
        //System.out.println("Rank = " + rank + " : " + countRowForRankTotal[0]);
        if(countRowForRankTotal[0]==ROW)
            return rowForCurRank;

        if(rank==0)
            return getEvenRows(size,countRowForRankTotal[0]);
        else
            return sendFromOtherRankToZero(rowForCurRank);

    }
    private static int getTotalRowEveryRank(int rowForCurRank)
    {
        int[] buf = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{rowForCurRank},0,buf,0,1,MPI.INT,MPI.SUM,ROOT);
        return buf[0];

    }


    private static int getEvenRows(int size, int countRowForRankTotal)
    {
        int[] buf = new int[size-1];
        int rowForRank = ROW < size -1 ? ((size-1) / ROW) : (ROW / (size-1));
        Arrays.fill(buf,rowForRank);
        boolean isMore = isMore(countRowForRankTotal);
        for(int i=buf.length-1;i>= 0;i--)
        {
            if((buf[i] > 0) && (isMore == true))
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
                i = buf.length;
        }

        for(int i=1;i<size;i++)
            MPI.COMM_WORLD.Isend(buf,i-1,1,MPI.INT,i,1);
        int countRank = 0;
        for(int i=0;i<buf.length;i++)
            if(buf[i]>0)
                countRank++;
        return countRank;
    }

    private static boolean isMore(int countRowForRankTotal)
    {
        return countRowForRankTotal>ROW;
    }

    private static int sendFromOtherRankToZero(int rowForCurRank)
    {
        int[] buf = new int[1];
        Request req = MPI.COMM_WORLD.Irecv(buf,0,1,MPI.INT,ROOT,1);
        req.Wait();

        return buf[0];
    }

    private static void print(int[] vector)
    {
        for(int i=0;i<vector.length;i++)
            System.out.print(vector[i] + " ");
        System.out.println();
    }

}
