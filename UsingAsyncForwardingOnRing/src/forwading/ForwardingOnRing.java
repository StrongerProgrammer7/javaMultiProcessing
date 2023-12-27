package forwading;
import mpi.*;

import java.util.Arrays;

public class ForwardingOnRing
{
    public static void main(String[] args)
    {
        MPI.Init(args);
        int TAG = 0;
        int[] buf = new int[10];
        Arrays.fill(buf, 0);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        //System.out.println(" size = " + size);
        int to = rank == size - 1 ? size - 1 - rank : rank + 1;
        int from = rank == 0 ? size - 1 : rank - 1;
        boolean usingSenRecv = false;
        if(usingSenRecv==false)
        {
            if(rank == 0)
            {
                buf[0] += rank;
                System.out.println("Send "+ buf[0] + " from : " + rank + " to " + to);
                MPI.COMM_WORLD.Send(buf,0,buf.length,MPI.INT,to,TAG);
            }else
                get_send(buf,from,to,TAG,rank);
            if(rank ==0)
            {
                mpi.Status status = MPI.COMM_WORLD.Recv(buf,0,buf.length,MPI.INT,from,TAG);
                System.out.println("Get " + buf[0] + " from " + from + " for " + rank);
                System.out.println(" Finish = " + buf[0]);
            }
        }else
        {
            buf[1] = rank;
            buf[0] +=rank;
            while(true)
            {
                if (rank == 0)
                {
                    MPI.COMM_WORLD.Sendrecv(buf, 0, 2, MPI.INT, to, 0,
                            buf, 0, 2, MPI.INT, from, 0);
                    System.out.println("Process " + rank + " s= " + buf[0] + " b= " + buf[1]);
                    if(buf[0] >= size * (size - 1) / 2)
                        break;
                } else
                {
                    if(buf[0] >= size * (size - 1) / 2 && buf[1] == to)
                        break;
                    send_recv(buf,from,to,TAG,rank);
                }

            }
            System.out.println("Current rank = " + rank + " buf[0] = " + buf[0] + " buf[1] = " + buf[1]);

        }


        MPI.Finalize();
    }

    private static void send_recv(int[] buf,int from,int to,int TAG,int rank)
    {
        // Process 2 receives modified data from process 1, modifies it, and sends it to process 0
        MPI.COMM_WORLD.Sendrecv(buf, 0, 2, MPI.INT, to, TAG,
                buf, 0, 2, MPI.INT, from, TAG);
        System.out.println("Process " + rank + " s=: " + buf[0] + " buf= " + buf[1]);
        // Modify the received data
        //buf[1] =rank;
        buf[0] += rank;//buf[1];

        System.out.println("Process " + rank + " sent modified data: s=" + buf[0] + " buf=" + buf[1]);
    }
    private static void get_send(int[] buf,int from,int to,int TAG,int rank)
    {
        MPI.COMM_WORLD.Recv(buf,0,buf.length,MPI.INT,from,TAG);
        System.out.println("Get " + buf[0] + " from " + from + "for " + rank);

        buf[0] += rank;
        System.out.println("Send "+ buf[0] + " from : " + rank + " to " + to);
        MPI.COMM_WORLD.Send(buf,0,buf.length,MPI.INT,to,TAG);
    }
}
