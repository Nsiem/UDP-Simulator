import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Receiver {

	public static void main(String[] args) throws IOException {

		String hostAddress = "";
		int portAck = 0;
		int portData = 0;
		String fileNameSaveTo = "";

		try {
			hostAddress = args[0];
			portData = Integer.parseInt(args[1]);
			portAck = Integer.parseInt(args[2]);
			fileNameSaveTo = args[3];
		} catch (Exception exception) {
			System.out.println("The arguments provided were not valid");
			return;
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(fileNameSaveTo);
		} catch (FileNotFoundException e1) {
			System.out.println("Output name was not valid");
			return;
		}

		DatagramSocket socket;
		try {
			socket = new DatagramSocket(portData);
		} catch (SocketException e) {
			System.out.println("Failed to bind server to port: " + portData);
			out.close();
			return;
		}

		System.out.println("The file server is ready. Bound to port "
				+ portData);
		
		int expectedSeqNum = 0;
        boolean flag = true;
        long starttime = 0;
        long endtime = 0;

		while(true) {

			DatagramPacket packet = new DatagramPacket(new byte[512], 512);

			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] data = packet.getData();
            if(data[0] == expectedSeqNum && flag) {
                flag = false;
                starttime = System.currentTimeMillis();
            }
            int datalength = packet.getLength();

			if (data[0] == -1) {
				System.out.println("-1 received, End of transmission");
                endtime = System.currentTimeMillis();
                System.out.println("-----------------------------------");
                System.out.println(String.format("Total transmission time is; %d", (endtime - starttime)) + "ms");
				out.close();
				return;
			}

            if (data[0] == -2) {
				sendPacketAck(packet, socket, portAck, hostAddress, (byte) -2);
				System.out.println("Isalive request made, ack '-2' sent back");
                continue;
			}

			if (data[0] != expectedSeqNum) {
                if(expectedSeqNum == 0) {
                    sendPacketAck(packet, socket, portAck, hostAddress, (byte) 1 );
                } else {
                    sendPacketAck(packet, socket, portAck, hostAddress, (byte) 0 );
                }
				
				continue;
			}
			for (int i = 1; i < datalength; i++) {
				try {
					out.write(data[i]);
				} catch (IOException e) {
					System.out.println("Fatal! Buffer could not be allocated properly.");
				}
			}

			if(expectedSeqNum == 0) {
                expectedSeqNum = 1;
            } else {
                expectedSeqNum = 0;
            }

			sendPacketAck(packet, socket, portAck, hostAddress, data[0]);

			// out.flush();

		}

	}

	private static void sendPacketAck(DatagramPacket packet,
			DatagramSocket socket, int ackPort, String ackHost,  byte seqAcknowledge) throws IOException {
		
		InetAddress address;

		try {
			address = InetAddress.getByName(ackHost);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return;
		}
		
		byte[] buffer = new byte[1];
		buffer[0] = seqAcknowledge;

		DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length,
				address, ackPort);
		socket.send(ackPacket);

	}

}