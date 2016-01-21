/**
 * 
 */
package com.hongbao.nioentity;

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

/**
 * @author hzllb
 *
 * 2016年1月21日
 */
public class AppClient {
	public static void main( String[] args ) throws IOException
    {
        Selector selector = Selector.open();
        
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        
        ServerSocket socket = serverSocketChannel.socket();
        InetSocketAddress address = new InetSocketAddress(9090);
        socket.bind(address);
        
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        System.out.println("client start");
        SocketChannel socketChannel = SocketChannel.open();  
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9089));  
        while(socketChannel.finishConnect()){
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            socketChannel.write(ByteBuffer.wrap("hzlserver".getBytes()));
        	break;
        }

        
        while(true){
        	//该调用会阻塞，直到至少有一个事件发生
        	selector.select();
        	Set<SelectionKey> keys = selector.selectedKeys();
        	Iterator<SelectionKey> iterator = keys.iterator();
        	while(iterator.hasNext()){
        		SelectionKey key = (SelectionKey)iterator.next();
        		iterator.remove();
        		
        		if(key.isAcceptable()){
        			ServerSocketChannel serverSocketChannel2 = (ServerSocketChannel)key.channel();
        			SocketChannel channel = serverSocketChannel2.accept();
        			
        			channel.configureBlocking(false);
        			channel.register(selector, SelectionKey.OP_READ);
        		}else if(key.isReadable()){
        			ByteBuffer buffer = ByteBuffer.allocate(1000);
        			SocketChannel channel = (SocketChannel)key.channel();
        			int count = channel.read(buffer);
        			if(count > 0){
        				buffer.flip();
        				byte[] bb = new byte[1000];
        				buffer.get(bb,0,buffer.limit());
        				String nameString = new String(bb);
        				System.out.println("receive string = " + nameString);
        				SelectionKey sKey = channel.register(selector, SelectionKey.OP_WRITE);
        				//sKey.attach(nameString);
        			}else{
        				channel.close();
        			}
        			buffer.clear();
        		}else if(key.isWritable()){
        			 SocketChannel channel = (SocketChannel) key.channel();   
        		     String name = (String) key.attachment();   
        		     ByteBuffer block = ByteBuffer.wrap(("Hello " + name).getBytes());   
        		        if(block != null)  
        		        {  
        		            channel.write(block);  
        		        }  
        		        else  
        		        {  
        		            channel.close();  
        		        }  
        		}
        	}
        }
        
    }
}
