package com.zs.nio.cs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    /* 标识数字 */
    private int flag = 0;

    /* 缓冲区大小 */
    private int BLOCK = 4096;

    /* 接受数据缓冲区 */
    private ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);// 4KB
    /* 发送数据缓冲区 */

    private ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);// 4KB

    private Selector selector;

    ServerSocketChannel serverSocketChannelTemp;

    public NIOServer(int port) throws IOException {
        // ServerSocketChannel用来在服务端监听Socket连接
        // 在这个ServerSocketChannel建立之后(open静态方法建立),创建ServerSocket相应TCP请求
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        // 在这个ServerSocketChannel
        // 创建ServerSocket相应TCP请求。一个ServerSocketChannel上的ServerSocket是单例对象
        System.out.println("serverSocketChannel" + serverSocketChannel);
        ServerSocket serverSocket = serverSocketChannel.socket();
        // 然后在这个ServerSocket上绑定ip+port
        serverSocket.bind(new InetSocketAddress(port));
        serverSocketChannelTemp = serverSocketChannel;

        // 最后，把那个ServerSocketChannel对象注册到selector上
        // -----------------------------------------------------------Selector--------------------------------------------------------------
        // 可以从selector中获取多个注册了的channel,一个selector可以管理多个channel，
        // 因此消除了创建多个线程去处理多个请求的做法。
        // 另外，selector中对channel的管理都是非阻塞的，所以FileChannel这种阻塞的channel不能使用selector

        selector = Selector.open();

        // register第二个参数是“interest set”,指定了channel监听的时间类型
        /**
         * <pre>
         * -Connect SelectionKey.OP_CONNECT 一个成功连接了Server的channel，注册为Connect
         *
         * -Accept SelectionKey.OP_ACCEPT 一个接受连接请求的 serverSocketChannel，被注册为Accept状态
         * 
         * -Read SelectionKey.OP_READ 一个有数据并可被读取的channel,注册为Read状态
         * 
         * -Write SelectionKey.OP_WRITE 一个可写入数据的channel,注册为Write状态
         * 
         * 如果你对不止一种事件感兴趣，那么可以用“位或”操作符将常量连接起来 
         * int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
         * </pre>
         */
        // 每次在一个selector上注册一个channel，就会产生一个SelectionKey对象，
        // 需要说一下，SelectionKey对象的如下属性
        //
        /**
         * 1)interestOps: channel上的关注的事件，通过&运算可以得到相应的判断
         * 
         * <pre>
         * int interestSet = selectionKey.interestOps();
         * 
         * boolean isInterestedInAccept = (interestSet & SelectionKey.OP_ACCEPT)==SelectionKey.OP_ACCEPT;
         * 
         * boolean isInterestedInConnect = (interestSet & SelectionKey.OP_CONNECT)==SelectionKey.OP_CONNECT;
         * 
         * boolean isInterestedInRead = (interestSet & SelectionKey.OP_READ)==SelectionKey.OP_READ;
         *
         * boolean isInterestedInWrite = (interestSet & SelectionKey.OP_WRITE)==SelectionKey.OP_WRITE;
         * 
         * 2)readyOps 是channel准备好了的事件类型；注意与interestOps并不一样! Selector.select()就是检查是否有注册的兴趣事件中已经准备好了的事件！
         * 可以通过如下方式判断: 
         * selectionKey.isAcceptable(); 一个server socket channel准备好接收新进入的连接
         * selectionKey.isConnectable();  某个channel成功连接到另一个服务器
         * selectionKey.isReadable(); 一个有数据可读的通道
         * selectionKey.isWritable();等待写数据的通道
         * </pre>
         */
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT) " + selectionKey);
        System.out.println("Server Start----:" + port + "	selector	" + selector);
    }

    // 监听
    private void listen() throws IOException {
        while (true) {
            /**
             * <pre>
             * select 返回在这个selector上注册过的兴趣事件 (interestsSet)对应的channel.
             * 比如你在这个selector上注册过，Accept事件，
             * 那么select的含义就是去选择已经准备好的，accept事件对应的channel。
             * 
             * int select():阻塞方法，直到至少返回一个你注册过的兴趣事件对应的channel.
             * int select(long timeout)：与select()类似，不同之处在于设定了阻塞超时时间
             * int selectNow()：与select()类似，只是不会产生阻塞，立即返回
             * 
             * </pre>
             */
            // It returns only after at least one channel is selected, this
            // selector's wakeup method is invoked, or the current thread is
            // interrupted, whichever comes first.
            selector.select(); // Selector.select()就是检查是否有注册的兴趣事件中已经准备好了的事件！注意.这里返回的是处于ready状态的事件对应的channel数量

            // 执行完selector.select(),会暗示你是否有准备好的channel，
            // 接着执行 Set<SelectionKey> selectionKeys = selector.selectedKeys();
            // 遍历获取准备好的channel
            // 每次在一个selector上注册一个channel，就会产生一个SelectionKey对象
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                ++i;
                SelectionKey selectionKey = iterator.next();
                iterator.remove();// 必须手动remove这个使用过的key
                handleKey(selectionKey);
            }
            System.out.println("iterator size" + i);
        }
    }

    /**
     * 通过selectKey可以获取到对应的channel和selector (selectionKey.selector())
     * @param selectionKey
     * @throws IOException
     */
    private void handleKey(SelectionKey selectionKey) throws IOException {
        // 接受请求
        ServerSocketChannel server = null;
        SocketChannel client = null;
        String receiveText;
        String sendText;
        int count = 0;
        // 测试此键的通道是否已准备好接新的Socket connection。
        if (selectionKey.isAcceptable()) {
            server = (ServerSocketChannel) selectionKey.channel();
            // 可阻塞模式：若为阻塞方法,只用于ServerSocketChannel，去监听建立的连接。
            // 如果有连接过来，就返回这个新连接的channel
            // 如非阻塞模式：若没有连接建立，会返回null
            System.out.println("serverSocketChannel：" + server);
            System.err.println(serverSocketChannelTemp == server);// 可以看到还是server端之前自己注册的那个serverSocketChannel

            // 通过 ServerSocketChannel.accept() 方法监听新进来的连接。当
            // accept()方法返回的时候,它返回一个包含新进来的连接的 SocketChannel。
            // 通常不会仅仅只监听一个连接,在while循环中调用 accept()方法
            client = server.accept();
            client.configureBlocking(false);
            // 配置为非阻塞
            System.out.println("clientSocketChannel：" + client);
            // 注册到selector，等待连接
            client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isConnectable()) {
            // a connection was established with a remote server.
            System.out.println(" this is connectable !");
        } else if (selectionKey.isReadable()) {
            // 返回为之创建此键的通道。
            client = (SocketChannel) selectionKey.channel();
            System.err.println(client.toString());// 可以看到还是server端之前自己注册的那个serverSocketChannel

            // 将缓冲区清空以备下次读取
            receivebuffer.clear();
            // 读取服务器发送来的数据到缓冲区中
            count = client.read(receivebuffer);
            if (count > 0) {
                receiveText = new String(receivebuffer.array(), 0, count);
                System.out.println("服务器端接受客户端数据--:" + receiveText);
                client.register(selector, SelectionKey.OP_WRITE);// 客户端消息获取后，读取掉。接着注册一个可写事件，用来向客户端发送消息
            }
        } else if (selectionKey.isWritable()) {
            // 将缓冲区清空以备下次写入
            sendbuffer.clear();
            // 返回为之创建此键的通道。
            client = (SocketChannel) selectionKey.channel();
            sendText = "message from server--" + flag++;
            // 向缓冲区中输入数据
            sendbuffer.put(sendText.getBytes());
            // 将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
            sendbuffer.flip();
            // 输出到通道
            client.write(sendbuffer);
            System.out.println("服务器端向客户端发送数据--：" + sendText);
            client.register(selector, SelectionKey.OP_READ);// 向客户端发送消息后，注册一个可读事件，当客户端再次发送消息时，这个事件将ready
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8989;
        NIOServer server = new NIOServer(port);
        server.listen();
    }
}