package ua.nikita.network;

public interface TCPConnectionListener { //Слой абстракции

    void onConnectionReady(TCPConnection tcpConnection); //Установление соединения
    void onReceiveString(TCPConnection tcpConnection, String value); //Получение строки
    void onDisconnect(TCPConnection tcpConnection); //Отключение
    void onException(TCPConnection tcpConnection, Exception e); //Что-то пошло не так, передаём исключение...

}
