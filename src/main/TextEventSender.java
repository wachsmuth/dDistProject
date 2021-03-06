package main;

import events.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by frederik290 on 15/04/16.
 * <p>
 * The resposibility of the TextEventSender is to await new text event from the DocumentEventCapturer.
 * When a new object is caught by the DocumentEventCapturer, the TextEventSender takes the object off the outgoingQueue and sends it
 * to the corresponding socket.
 */
public class TextEventSender implements Runnable, EventSender {

    private Socket socket;
    private LinkedBlockingQueue<MyTextEvent> outgoingQueue;
    boolean terminated = false;

    public TextEventSender(Socket socket) {
        this.socket = socket;
        this.outgoingQueue = new LinkedBlockingQueue<>();
    }

    public TextEventSender(Socket socket, LinkedBlockingQueue<MyTextEvent> outgoingQueue) {
        this.socket = socket;
        this.outgoingQueue = outgoingQueue;
    }

    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void put(MyTextEvent e) throws InterruptedException {
        outgoingQueue.put(e);
    }

    @Override
    public void run() {
        MyTextEvent textEvent;
        ObjectOutputStream objectOutputStream;
        boolean closeSocket;

        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                textEvent = outgoingQueue.take();
                objectOutputStream.writeObject(textEvent);

                if(textEvent instanceof RootAssignAckEvent){
                    closeSocket = false;
                    break;
                }

                if(textEvent instanceof RootAssignEvent) {
                    RootAssignEvent e = (RootAssignEvent) textEvent;
                    if(e.assignIsFinished()){
                        objectOutputStream.writeObject(new ShutDownEvent(true));
                        closeSocket = true;
                        break;
                    }

                }

                if (textEvent instanceof ShutDownEvent) {
                    closeSocket = ((ShutDownEvent) textEvent).getCloseSocket();
                    break;
                }
            }
            if (closeSocket) {
                objectOutputStream.close();
            }


        } catch (IOException e) {
            //TODO
        } catch (InterruptedException e) {
            //TODO
        } finally {
            terminated = true;
        }
    }

}
