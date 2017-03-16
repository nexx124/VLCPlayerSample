package windylabs.com.vlcplayersample;

/**
 * Created by Александр on 14.03.2017.
 */


import android.app.Service;
import android.content.Intent;
        import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class Server extends Service{
    ServerActivity activity;
    ServerSocket serverSocket;
    String message = "";
    Socket socket;
    static final int socketServerPORT = 8080;
    private final String TAG = "SERVER_LOG";

    public Server() {

    }
    public Server(ServerActivity activity) throws IOException {
        this.activity = activity;
//        Thread socketServerThread = new Thread(new SocketServerThread());
//        serverSocket = new ServerSocket(socketServerPORT);
//        socketServerThread.start();
    }

    public int getPort() {
        return socketServerPORT;
    }

    @Override
    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        try {
            Thread socketServerThread = new Thread(new SocketServerThread());
            serverSocket = new ServerSocket(socketServerPORT);
            socketServerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SocketServerThread extends Thread {

        @Override
        public void run() {
            try {
                socket = serverSocket.accept();
                while (true) {
                    InputStream is = socket.getInputStream();
                    byte buf[] = new byte[64*1024];
                    int r = is.read(buf);
                    final String data = new String(buf, 0, r);

                    if (data.equals("@@stop")) {
                        //is.close();
                        Intent server_activity = new Intent(getApplicationContext(), ServerActivity.class);
                        server_activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(server_activity);
                        Log.e(TAG, "получена команда остановить");
                        continue;
                    }
                    if (data.equals("@@continue_stream")) {
                        Log.e(TAG, "continue  command was detected");
                        continue;
                    }
                    if (Pattern.matches("http://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{1,5}/[a-z,A-Z,0-9]*", data)) {
                        final String reply = new String("Successfully connected!");
                        OutputStream outputStream = socket.getOutputStream();
                        if (r > 0) {
                            outputStream.write(reply.getBytes());
                            outputStream.flush();
                        }
//                        PrintStream printStream = new PrintStream(outputStream);
//                        printStream.print("kek");
//                        printStream.close();
//                        activity.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                activity.msg.setText(data);
//                            }
//                        });
//					    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
//							    socket, count);
//					    socketServerReplyThread.run();

                        final String url = new String(data);
                        Intent toFullscreen = new Intent (getApplicationContext(), VideoVLCActivity.class);
                        Bundle b = new Bundle();
                        b.putString("videoUrl", url);
                        toFullscreen.putExtras(b); //Put your id to your next Intent
                        toFullscreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK /*| Intent.FLAG_ACTIVITY_CLEAR_TOP*/ | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(toFullscreen);
                        Log.e(TAG, "получена команда на открытие стрима");
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

//    private class SocketServerReplyThread extends Thread {
//
//        private Socket hostThreadSocket;
//        int cnt;
//
//        SocketServerReplyThread(Socket socket, int c) {
//            hostThreadSocket = socket;
//            cnt = c;
//        }
//
//        @Override
//        public void run() {
//            OutputStream outputStream;
//            String msgReply = "Hello from Server, you are #" + cnt;
//
//            try {
//                outputStream = hostThreadSocket.getOutputStream();
//                PrintStream printStream = new PrintStream(outputStream);
//                printStream.print(msgReply);
//                printStream.close();
//
//                message += "replayed: " + msgReply + "\n";
//
//                activity.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        activity.msg.setText(message);
//                    }
//                });
//
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                message += "Something wrong! " + e.toString() + "\n";
//            }
//
//            activity.runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    activity.msg.setText(message);
//                }
//            });
//        }
//
//    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip +=  inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}