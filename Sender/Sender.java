import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.event.*;

public final class Sender {
    public static void main(String[] args) throws IOException {
        frame theframe = new frame();
        theframe.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });

        theframe.ISALIVE.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                isalive(theframe.receiveripAddress.getText(), theframe.receiverportNum.getText(), theframe.senderportNum.getText(), theframe.timeoutinput.getText(), theframe);
            }
        });

        theframe.Send.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if(theframe.reliableornot.isSelected()) {
                    unreliablesendpackets(theframe.receiveripAddress.getText(), theframe.receiverportNum.getText(), theframe.senderportNum.getText(), theframe.textfilename.getText(), theframe.timeoutinput.getText(), theframe);
                } else {
                    sendpackets(theframe.receiveripAddress.getText(), theframe.receiverportNum.getText(), theframe.senderportNum.getText(), theframe.textfilename.getText(), theframe.timeoutinput.getText(), theframe);
                }
                
            }
        });

    }

    private static void isalive(String ipaddress, String receiverportnum, String senderportnum, String timeout, frame theframe) {
        theframe.errorlabel.setText("");
        InetAddress ipadd;
        Integer rportnum;
        Integer sportnum;
        Integer tout = 1000;
        if(ipaddress.equals("") || receiverportnum.equals("") || senderportnum.equals("")) {
            theframe.errorlabel.setText("Please enter IPaddress and portnum of receiver, and portnum of this application");
            return;
        }
        DatagramSocket socket = null;
        try {
            ipadd = InetAddress.getByName(ipaddress);
            rportnum = Integer.parseInt(receiverportnum);
            sportnum = Integer.parseInt(senderportnum);
            if(rportnum < 0 || rportnum > 65535 || sportnum < 0 || sportnum > 65535) {
                theframe.errorlabel.setText("Please enter only numbers for port nums that are in the range of 0 to 65535");
                return;
            }
            if(timeout.equals("") == false) {
                tout = (Integer.parseInt(timeout)) / 1000;
            }
            socket = new DatagramSocket(sportnum);
            socket.connect(ipadd, rportnum);
            socket.setSoTimeout(tout);

            byte[] buffer = new byte[1];
            buffer[0] = -2;
        
            DatagramPacket isalivepacket = new DatagramPacket(buffer, buffer.length, ipadd, rportnum);
            socket.send(isalivepacket);

            DatagramPacket isaliveack = new DatagramPacket(new byte[512], 512);

            socket.receive(isaliveack);

            byte[] ack = isaliveack.getData();

            
            if(ack[0] == -2) {
                theframe.resultArea.append("-------------------------------------\n");
                theframe.resultArea.append("Receiver is alive and well!\n");
                theframe.resultArea.append("-------------------------------------\n");
            }

            socket.close();
        } catch (NumberFormatException e) {
            theframe.errorlabel.setText("Please enter only numbers for port nums, and timeout (if added)");
            return;
        } catch (SocketException se) {
            theframe.errorlabel.setText("Could not create socket with entered info");
            return;
        } catch (UnknownHostException ue) {
            theframe.errorlabel.setText("Please enter valid ipaddress");
            return;
        } catch (SocketTimeoutException te) {
            theframe.errorlabel.setText("Timeout has been reached, packet was lost or timeout entered needs to be increased");
            return;
        } catch (IOException ie) {
            theframe.errorlabel.setText("Could not send packet for isalive check");
            return;
        } catch (NullPointerException ne){
            System.out.println(ne);
            return;
        } finally {
            socket.close();
        }
        
    }

    private static void sendpackets(String ipaddress, String receiverportnum, String senderportnum, String filename, String timeout, frame theframe) {
        theframe.errorlabel.setText("");
        InetAddress ipadd;
        Integer rportnum;
        Integer sportnum;
        Integer tout = 1000;
        if(ipaddress.equals("") || receiverportnum.equals("") || senderportnum.equals("")) {
            theframe.errorlabel.setText("Please enter IPaddress and portnum of receiver, and portnum of this application");
            return;
        }
        
        
        DatagramSocket socket = null;
        try {
            ipadd = InetAddress.getByName(ipaddress);
            rportnum = Integer.parseInt(receiverportnum);
            sportnum = Integer.parseInt(senderportnum);
            if(rportnum < 0 || rportnum > 65535 || sportnum < 0 || sportnum > 65535) {
                theframe.errorlabel.setText("Please enter only numbers for port nums that are in the range of 0 to 65535");
                return;
            }
            if(timeout.equals("") == false) {
                tout = (Integer.parseInt(timeout)) / 1000;
            }

            Path path = Paths.get(filename);

            byte[] data = Files.readAllBytes(path);
            int datalength = data.length;
            
            
            socket = new DatagramSocket(sportnum);
    
            socket.setSoTimeout(tout);
            
            int packetsent = 1;
            int resentpackets = 0;
            int sequencenum = 0;
            int packetrange = 0;

            while(true) {
                if(packetrange == datalength) {
                    theframe.resultArea.append("---------------------------------------------------------------------------------------------\n");
                    theframe.resultArea.append(String.format("%d packets were sent, %d packets were resent, total packets sent = %d\n", packetsent - 1, resentpackets, ((packetsent + resentpackets) - 1)));
                    theframe.resultArea.append("---------------------------------------------------------------------------------------------\n");
                    break;
                } 
            
                byte[] buffer = new byte[512];
                int lastbytes = 0;

                if(datalength - packetrange > 511) {
                    System.arraycopy(data, packetrange, buffer, 1, 511);
                    buffer[0] = (byte) sequencenum;
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipadd, rportnum);
                    socket.send(packet);
                    packetrange = packetrange + 511;
                } else {
                    lastbytes = datalength - packetrange;
                    System.arraycopy(data, packetrange, buffer, 1, lastbytes);
                    buffer[0] = (byte) sequencenum;
                    DatagramPacket packet = new DatagramPacket(buffer, lastbytes + 1, ipadd, rportnum);
                    socket.send(packet);
                    packetrange = datalength;
                }

                theframe.resultArea.append(String.format("Packet %d sent, %d bytes remaining to be sent\n", packetsent, datalength - packetrange));

                DatagramPacket ack = new DatagramPacket(new byte[512], 512);
                try {
                    socket.receive(ack);
                } catch (IOException e) {
                    theframe.resultArea.append(String.format("timeout occured resending packet %d\n", packetsent));
                    if(lastbytes > 0) {
                        packetrange = datalength - lastbytes;
                    } else {
                        packetrange = packetrange - 511;
                    }
                    resentpackets++;
                    continue;
                }
                
                
                byte[] ackbyte = ack.getData();

                theframe.resultArea.append(String.format("ack received; %d, ack expected; %d\n", ackbyte[0], sequencenum));
                
                if(ackbyte[0] == sequencenum) {
                    packetsent++;
                    if(sequencenum == 0) {
                        sequencenum = 1;
                    } else {
                        sequencenum = 0;
                    }
                } else {
                    theframe.resultArea.append(String.format("Incorrect ack received resending packet %d\n", packetsent));
                    resentpackets++;
                    if(lastbytes > 0) {
                        packetrange = datalength - lastbytes;
                    } else {
                        packetrange = packetrange - 511;
                    }
                }

            }

            byte[] eot = new byte[1];
            eot[0] = -1;
            DatagramPacket packet = new DatagramPacket(eot, eot.length, ipadd, rportnum);
            socket.send(packet);

        } catch (NumberFormatException e) {
            theframe.errorlabel.setText("Please enter only numbers for port nums, and timeout (if added)");
            System.out.println(e);
            return;
        } catch (BindException be) {
            theframe.errorlabel.setText("Socket already bound, could not close previous socket");
            System.out.println(be);
            return;
        } catch (SocketException se) {
            theframe.errorlabel.setText("Could not create socket with entered info");
            System.out.println(se);
            return;
        } catch (UnknownHostException ue) {
            theframe.errorlabel.setText("Please enter valid ipaddress");
            System.out.println(ue);
            return;
        } catch (SocketTimeoutException te) {
            theframe.errorlabel.setText("Timeout has been reached, packet was lost or timeout entered needs to be increased");
            System.out.println(te);
            return;
        } catch (IOException ie) {
            theframe.errorlabel.setText("Could not open file");
            System.out.println(ie);
            return;
        } catch (NullPointerException ne){
            System.out.println(ne);
            return;
        } finally {
            socket.close();
        }
        
    }

    private static void unreliablesendpackets(String ipaddress, String receiverportnum, String senderportnum, String filename, String timeout, frame theframe) {
        theframe.errorlabel.setText("");
        InetAddress ipadd;
        Integer rportnum;
        Integer sportnum;
        Integer tout = 1000;
        if(ipaddress.equals("") || receiverportnum.equals("") || senderportnum.equals("")) {
            theframe.errorlabel.setText("Please enter IPaddress and portnum of receiver, and portnum of this application");
            return;
        }
        
        
        DatagramSocket socket = null;
        try {
            ipadd = InetAddress.getByName(ipaddress);
            rportnum = Integer.parseInt(receiverportnum);
            sportnum = Integer.parseInt(senderportnum);
            if(rportnum < 0 || rportnum > 65535 || sportnum < 0 || sportnum > 65535) {
                theframe.errorlabel.setText("Please enter only numbers for port nums that are in the range of 0 to 65535");
                return;
            }
            if(timeout.equals("") == false) {
                tout = (Integer.parseInt(timeout)) / 1000;
            }

            Path path = Paths.get(filename);

            byte[] data = Files.readAllBytes(path);
            int datalength = data.length;
            
            
            socket = new DatagramSocket(sportnum);
    
            socket.setSoTimeout(tout);
            
            int packetsent = 1;
            int resentpackets = 0;
            int sequencenum = 0;
            int packetrange = 0;

            while(true) {
                if(packetrange == datalength) {
                    theframe.resultArea.append("---------------------------------------------------------------------------------------------\n");
                    theframe.resultArea.append(String.format("%d packets were sent, %d packets were resent, total packets sent = %d\n", packetsent - 1, resentpackets, ((packetsent + resentpackets) - 1)));
                    theframe.resultArea.append("---------------------------------------------------------------------------------------------\n");
                    break;
                } 
            
                byte[] buffer = new byte[512];
                int lastbytes = 0;

                if (((packetsent + resentpackets) % 10) == 0) {
                    theframe.resultArea.append(String.format("Simulating drop of packet %d, ", packetsent));
                    TimeUnit.MILLISECONDS.sleep(tout);
                    resentpackets++;
                    continue;
                } else {
                    if(datalength - packetrange > 511) {
                        System.arraycopy(data, packetrange, buffer, 1, 511);
                        buffer[0] = (byte) sequencenum;
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipadd, rportnum);
                        socket.send(packet);
                        packetrange = packetrange + 511;
                    } else {
                        lastbytes = datalength - packetrange;
                        System.arraycopy(data, packetrange, buffer, 1, lastbytes);
                        buffer[0] = (byte) sequencenum;
                        DatagramPacket packet = new DatagramPacket(buffer, lastbytes + 1, ipadd, rportnum);
                        socket.send(packet);
                        packetrange = datalength;
                    }
                }
                

                theframe.resultArea.append(String.format("Packet %d sent, %d bytes remaining to be sent\n", packetsent, datalength - packetrange));

                DatagramPacket ack = new DatagramPacket(new byte[512], 512);
                try {
                    socket.receive(ack);
                } catch (IOException e) {
                    theframe.resultArea.append(String.format("timeout occured resending packet %d\n", packetsent));
                    if(lastbytes > 0) {
                        packetrange = datalength - lastbytes;
                    } else {
                        packetrange = packetrange - 511;
                    }
                    resentpackets++;
                    continue;
                }
                
                
                byte[] ackbyte = ack.getData();

                theframe.resultArea.append(String.format("ack received; %d, ack expected; %d\n", ackbyte[0], sequencenum));
                
                if(ackbyte[0] == sequencenum) {
                    packetsent++;
                    if(sequencenum == 0) {
                        sequencenum = 1;
                    } else {
                        sequencenum = 0;
                    }
                } else {
                    theframe.resultArea.append(String.format("Incorrect ack received resending packet %d\n", packetsent));
                    resentpackets++;
                    if(lastbytes > 0) {
                        packetrange = datalength - lastbytes;
                    } else {
                        packetrange = packetrange - 511;
                    }
                }

            }

            byte[] eot = new byte[1];
            eot[0] = -1;
            DatagramPacket packet = new DatagramPacket(eot, eot.length, ipadd, rportnum);
            socket.send(packet);

        } catch (NumberFormatException e) {
            theframe.errorlabel.setText("Please enter only numbers for port nums, and timeout (if added)");
            System.out.println(e);
            return;
        } catch (BindException be) {
            theframe.errorlabel.setText("Socket already bound, could not close previous socket");
            System.out.println(be);
            return;
        } catch (SocketException se) {
            theframe.errorlabel.setText("Could not create socket with entered info");
            System.out.println(se);
            return;
        } catch (UnknownHostException ue) {
            theframe.errorlabel.setText("Please enter valid ipaddress");
            System.out.println(ue);
            return;
        } catch (SocketTimeoutException te) {
            theframe.errorlabel.setText("Timeout has been reached, packet was lost or timeout entered needs to be increased");
            System.out.println(te);
            return;
        } catch (IOException ie) {
            theframe.errorlabel.setText("Could not open file");
            System.out.println(ie);
            return;
        } catch (NullPointerException ne){
            System.out.println(ne);
            return;
        } catch (InterruptedException ite) {
            System.out.println(ite);
            return;
        } finally {
            socket.close();
        }
        
    }

    
}


// TO-DO LIST
// ADD LISTENERS TO SEND BUTTON AND CALL FUNCTION SENDPACKET()
// need to open file given through text field, then to take set byte packets, 512 bytes is the number for now.
// need to connect to receiver and start sending and receiving acks