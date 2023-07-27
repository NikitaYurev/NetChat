package ua.nikita.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener; //Слушатель событий
    private final BufferedReader in;
    private final BufferedWriter out;

    //создание нового сокета из айпи-адреса и порта
    public TCPConnection (TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    //принимает внешний готовый сокет
    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException{
        this.eventListener = eventListener;
        this.socket = socket;

        //Получение потока ввода
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        //поток слушающий входящее соединение
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while(!rxThread.isInterrupted()) {
                        eventListener.onReceiveString(TCPConnection.this, in.readLine());
                    }
                    String msg = in.readLine();
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    public synchronized void sendString(String value) {
        try {
            out.write(value + "\r\n");
            out.flush(); //сбрасывание буферов и отправка.
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        //socket.getInetAddress() - адрес с которого соединено соединение
        //socket.getPort() - номер порта
        return "TCPConnection: " +  socket.getInetAddress() + ": " + socket.getPort();
    }
}
