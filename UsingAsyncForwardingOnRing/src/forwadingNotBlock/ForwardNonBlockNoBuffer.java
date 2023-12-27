package forwadingNotBlock;

import java.util.Arrays;
import mpi.MPI;
import mpi.MPIException;
import mpi.Request;

class ForwardNonBlockNoBuffer
{
        public static void noBuffer(String[] args)  throws MPIException
        {
            MPI.Init(args);
            int TAG = 0;

            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();

            int[] s = new int[2];
            Arrays.fill(s,0);

            Request recvRequest = null;
            Request sendRequest = null;
            s[1] = rank;
            while (true)
            {

                s[0] += s[1];
                int temp = s[1];
                recvRequest = MPI.COMM_WORLD.Irecv(s, 0, 1, MPI.INT, (rank + size - 1) % size, TAG);
                sendRequest = MPI.COMM_WORLD.Isend(s, 0, 1, MPI.INT, (rank + 1) % size, TAG);

                if (rank == 0 && s[0] == size * (size - 1) / 2)
                    break;
                else if(s[0] >= size * (size - 1) / 2)
                    break;

                System.out.println("[Before recv wait] rank=" + rank + " s=" + s[0] + "(+"+s[1] +")");
                recvRequest.Wait();
                System.out.println("[After get Before send wait] rank=" + rank + " get s=" + s[0]);
                sendRequest.Wait();

                System.out.println("rank=" + rank + " s=" + s[0]);

            }

            if (rank == 0)
                System.out.println("Total sum: " + s[0]);
            else
                System.out.println("Rank = " + rank + " my sum = " + s[0]);

            MPI.Finalize();
        }

}
