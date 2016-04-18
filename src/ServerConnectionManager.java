import javax.print.Doc;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jeppe Vinberg on 15-04-2016.
 */
public class ServerConnectionManager implements Runnable {

    private ServerSocket serverSocket;
    private DocumentEventCapturer documentEventCapturer;
    private LinkedBlockingQueue<MyTextEvent> incomingEvents;

    public ServerConnectionManager(ServerSocket serverSocket, DocumentEventCapturer documentEventCapturer, LinkedBlockingQueue<MyTextEvent> incomingEvents){
        this.serverSocket = serverSocket;
        this.documentEventCapturer = documentEventCapturer;
        this.incomingEvents = incomingEvents;

    }

    @Override
    public void run() {
        Socket socket;
        while(true){
            socket = waitForConnectionFromClient(serverSocket);
            if(socket != null){
                System.out.println("New connection established to client " + socket);
                initThreads(socket);
            }else{
                System.out.println("Connection manager terminated");
                break;
            }
        }
    }

    private void initThreads(Socket socket){
        TextEventReceiver receiver = new TextEventReceiver(socket, incomingEvents, documentEventCapturer);
        TextEventSender sender = new TextEventSender(documentEventCapturer, socket);
        Thread senderThread = new Thread(sender);
        Thread receiverThread = new Thread(receiver);
        senderThread.start();
        receiverThread.start();
    }

    private Socket waitForConnectionFromClient(ServerSocket serverSocket) {
        Socket res = null;
        try {
            res = serverSocket.accept();
        } catch (IOException e) {
            // We return null on IOExceptions
        }
        return res;
    }
}
