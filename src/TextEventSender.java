import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by frederik290 on 15/04/16.
 * <p>
 * The resposibility of the TextEventSender is to await new text event from the DocumentEventCapturer.
 * When a new object is caught by the DocumentEventCapturer, the TextEventSender takes the object off the queue and sends it
 * to the corresponding socket.
 */
public class TextEventSender implements Runnable, DisconnectHandler, EventSender {

    private Socket socket;
    private LinkedBlockingQueue<MyTextEvent> queue;
    boolean shutdown;

    public TextEventSender(Socket socket) {
        this.socket = socket;
        this.queue = new LinkedBlockingQueue<>();
    }

    public TextEventSender(Socket socket, LinkedBlockingQueue<MyTextEvent> queue) {
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void put(MyTextEvent e) throws InterruptedException {
        queue.put(e);
    }

    @Override
    public void run() {
        MyTextEvent textEvent;
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                textEvent = queue.take();
                objectOutputStream.writeObject(textEvent);

                if (textEvent instanceof ShutDownEvent) {
                    shutdown = ((ShutDownEvent) textEvent).getShutdown();
                    break;
                }
            }
            if (shutdown) {
                objectOutputStream.close();
            }


        } catch (IOException e) {
            //TODO
        } catch (InterruptedException e) {
            //TODO
        }
    }

    @Override
    public void disconnect() throws InterruptedException {
        queue.put(new ShutDownEvent(false));
    }
}
