package forwadingNotBlock;
import forwadingNotBlock.ForwardNonBlockNoBuffer;
import mpi.*;


public class ForwardingOnRingNotBLock
{
    public static void main(String[] args)  throws MPIException
    {
        boolean withBuffer = false;
        if(withBuffer==true)
        {
            MPI.Init(args);
            int TAG = 0;

            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();

            int[] buf = new int[]{rank};
            int[] s = new int[]{0};

            Request recvRequest = null;
            Request sendRequest = null;

            while (true)
            {

                recvRequest = MPI.COMM_WORLD.Irecv(buf, 0, 1, MPI.INT, (rank + size - 1) % size, TAG);

                sendRequest = MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.INT, (rank + 1) % size, TAG);

                recvRequest.Wait();
                sendRequest.Wait();

                s[0] += buf[0];
                System.out.println("rank " + rank + " s - " + s[0]);

                //0 поток отправит всем потом обновленную s , а те в свою очередь увеличат ее
                MPI.COMM_WORLD.Bcast(s, 0, 1, MPI.INT, 0);
                System.out.println("After buffer rank " + rank + " s - " + s[0]);

                //0 отправляет все на s до тех пор пока не обойдет все процессы
                if (rank == 0 && s[0] == size * (size - 1) / 2)
                    break;
                else if(rank!=0 && s[0] >= size * (size - 1) / 2)
                    break; //чтобы другие потоки вышли из цикла как только получат нужное значение

            }

            if (rank == 0)
                System.out.println("Total sum: " + s[0]);

            MPI.Finalize();
        }else
            ForwardNonBlockNoBuffer.noBuffer(args);

    }

}

//Simple example
 /*public static void main(String[] args)  throws MPIException
    {
        MPI.Init(args);
        int TAG = 0;

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size < 2)
        {
            System.out.println("This program requires at least 2 MPI processes.");
            MPI.Finalize();
            return;
        }

        if (rank == 0)
        {
            int[] sendData = new int[]{2};
            int[] receiveData = new int[1];

            Request request = MPI.COMM_WORLD.Isend(sendData, 0, 1, MPI.INT, 1, 0);
            System.out.println("Process " + rank + " sent data: " + sendData[0] + " to 1rank");

            // Perform some work here

            // Wait for the send to complete
            request.Wait();

            // Non-blocking receive from process 1
            Request recvRequest = MPI.COMM_WORLD.Irecv(receiveData, 0, 1, MPI.INT, 1, 0);

            // Continue performing work

            // Wait for the receive to complete
            recvRequest.Wait();

            System.out.println("Process " + rank + " received data: " + receiveData[0] + " from 1 rank");
        } else if (rank == 1)
        {
            int[] receiveData = new int[1];

            // Non-blocking receive from process 0
            Request recvRequest = MPI.COMM_WORLD.Irecv(receiveData, 0, 1, MPI.INT, 0, 0);

            // Perform some work here

            // Wait for the receive to complete
            recvRequest.Wait();

            System.out.println("Process " + rank + " received data: " + receiveData[0]);

            int[] sendData = new int[]{receiveData[0] + 5};
            Request request = MPI.COMM_WORLD.Isend(sendData, 0, 1, MPI.INT, 0, 0);
            System.out.println("Process " + rank + " sent data: " + sendData[0]);

            // Continue performing work

            // Wait for the send to complete
            request.Wait();
        }

        MPI.Finalize();
    }*/
