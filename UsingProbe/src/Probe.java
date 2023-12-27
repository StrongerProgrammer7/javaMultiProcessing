import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

//операция Probe используется для проверки наличия сообщения в определенном канале связи (исходном процессе и теге) без фактического получения сообщения.
public class Probe
{
    public static void main(String[] args) throws MPIException, InterruptedException
    {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size < 2)
        {
            System.out.println("This program requires at least 2 MPI processes.");
            MPI.Finalize();
            return;
        }

        int data[] = new int[1];
        int buf[] = {1,3,5};
        int count, TAG = 0;
        Status status = null;
        data[0]=2023;
        if(rank == 0)
        {
            MPI.COMM_WORLD.Send(data,0,1,MPI.INT,2,TAG);
        }else if(rank == 1)
        {
            MPI.COMM_WORLD.Send(buf,0,buf.length,MPI.INT,2,TAG);
        }else if(rank == 2)
        {
            status = MPI.COMM_WORLD.Probe(0,0);
            System.out.println(status);

            if(status!= null)
            {
                count = status.Get_count(MPI.INT);
                System.out.println(count);
                int[] back_buf = new int[count];
                MPI.COMM_WORLD.Recv(back_buf,0,1,MPI.INT,0,TAG);
                System.out.println("RANK = " + (size - 1 - rank));
                for(int i =0;i<count;i++)
                    System.out.print(back_buf[i] +" ");

            }else
                System.out.println("Message from " + (size - 1-rank) + "is not avaliablTAG" );

            status = MPI.COMM_WORLD.Iprobe(1,TAG);
            System.out.println("\n Message from 1 " + status);
            if(status!=null)
            {
                count = status.Get_count(MPI.INT);
                System.out.println("RANK = " + (size - rank));
                int[] back_buf = new int[count];
                MPI.COMM_WORLD.Recv(back_buf,0,count,MPI.INT,size - rank,TAG);
                for(int i=0;i<count;i++)
                    System.out.print(back_buf[i] + " ");

            }else
                System.out.println("Message from " + (size -rank) + " is not avaliable" );

        }


        MPI.Finalize();
    }
}
