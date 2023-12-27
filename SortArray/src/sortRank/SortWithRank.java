package sortRank;
import mpi.*;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class SortWithRank
{
    public static void main(String[] args) throws MPIException
    {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if(rank != 0 && rank!= size - 1 && rank != size - 2) //7 & 8   0-8 = 9
        {
            int[] buf = new int[]{ThreadLocalRandom.current().nextInt(1,20)};
            //System.out.println("Rank = " + rank + " random numb = " + buf[0]);

            if(rank >= 1 && rank <=3)
                MPI.COMM_WORLD.Isend(buf,0,1,MPI.INT,size-1,0);
            else
                MPI.COMM_WORLD.Isend(buf,0,1,MPI.INT,size-2,1);

        }else if(rank == size - 2)
        {
            //System.out.println("RECV RANK (-2) = " + rank);
            Request[] requests = new Request[3];
            int[] receivedData = new int[3];
            for(int i=4;i<=6;i++)
            {
                Status st = null;
                while(st == null)
                    st = MPI.COMM_WORLD.Iprobe(i,1); // Возможно любые другие вычисления пока ждем сигнал
                requests[i-4] = MPI.COMM_WORLD.Irecv(receivedData,i-4,st.Get_count(MPI.INT),MPI.INT,i,1);

            }

            Request.Waitall(requests);
            System.out.println("Rank = " + rank + " : " + Arrays.toString(receivedData));
            Arrays.sort(receivedData);
            MPI.COMM_WORLD.Isend(receivedData,0,receivedData.length,MPI.INT,0,0);

        }else if(rank == size -1)
        {
            //System.out.println("RECV RANK (-1)  = " + rank);
            Request[] requests = new Request[3];
            int[] receivedData = new int[]{0,0,0};
            for(int i=1;i<=3;i++)
            {
                Status st = null;
                while(st == null)
                    st = MPI.COMM_WORLD.Probe(i,0);
                requests[i-1] = MPI.COMM_WORLD.Irecv(receivedData,i-1,st.Get_count(MPI.INT),MPI.INT,i,0);

            }

            Request.Waitall(requests);
            System.out.println("Rank = " + rank + " : " + Arrays.toString(receivedData));
            Arrays.sort(receivedData);
            MPI.COMM_WORLD.Isend(receivedData,0,receivedData.length,MPI.INT,0,1);
        }else if(rank == 0)
        {
           // System.out.println("Rank = " + rank);
            Status status = MPI.COMM_WORLD.Probe(7,0);
            Status status1 = MPI.COMM_WORLD.Probe(8,1);
            int[] buf = new int[size-3];
            Request[] requests = new Request[2];
            if(status!=null)
                 requests[0] = MPI.COMM_WORLD.Irecv(buf,0,status1.Get_count(MPI.INT),MPI.INT,7,0);
            if(status1!=null)
                requests[1] = MPI.COMM_WORLD.Irecv(buf,3,status1.Get_count(MPI.INT),MPI.INT,8,1);
            Request.Waitall(requests);
            for(int i=0;i<size-3;i++)
                System.out.print(buf[i] + " ");
            Arrays.sort(buf);
            System.out.println("\n After sort");
            for(int i=0;i<size-3;i++)
                System.out.print(buf[i] + " ");
        }
        MPI.Finalize();
    }

}
