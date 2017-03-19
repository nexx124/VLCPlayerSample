package windylabs.com.vlcplayersample;

/**
 * Created by Александр on 14.03.2017.
 */


import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
        import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
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
import java.util.List;
import java.util.regex.Pattern;

public class Server extends Service{
    ServerActivity activity;
    ServerSocket serverSocket;
    String message = "";
    Socket socket;
    InputStream is;

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
                is.close();
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
                    is = socket.getInputStream();
                    byte buf[] = new byte[64*1024];
                    int r = is.read(buf);
                    final String data = new String(buf, 0, r);

                    if (data.equals("@@stop")) {
//                        Intent server_activity = new Intent(getApplicationContext(), ServerActivity.class);
//                        server_activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(server_activity);

                        Intent mIntent = new Intent("ActionCode.intent.VLC");
                        mIntent.putExtra("action_code", 2);
                        getBaseContext().sendBroadcast(mIntent);

                        Log.e(TAG, "получена команда остановить");
                        continue;
                    }
                    if (data.equals("@@pause_stream")) {
                        Log.e(TAG, "continue  command was detected");
                        Intent mIntent = new Intent("ActionCode.intent.VLC");
                        mIntent.putExtra("action_code", 1);
                        getBaseContext().sendBroadcast(mIntent);
                        continue;
                    }
                    if (Pattern.matches("http://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{1,5}/[a-z,A-Z,0-9]*", data)) {
                        final String reply = new String("Successfully connected!");
                        OutputStream outputStream = socket.getOutputStream();
                        if (r > 0) {
                            outputStream.write(reply.getBytes());
                            outputStream.flush();
                        }

                        final String url = new String(data);
                        Intent toFullscreen = new Intent (getApplicationContext(), VideoVLCActivity.class);
                        Bundle b = new Bundle();
                        b.putString("videoUrl", url);
                        toFullscreen.putExtras(b);
                        toFullscreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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