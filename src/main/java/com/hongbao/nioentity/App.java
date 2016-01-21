package com.hongbao.nioentity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.server.ServerCloneException;
import java.util.Iterator;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
        Selector selector = Selector.open();
        
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        
        ServerSocket socket = serverSocketChannel.socket();
        InetSocketAddress address = new InetSocketAddress(9089);
        socket.bind(address);
        
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        System.out.println("server start");
        while(true){
        	//该调用会阻塞，直到至少有一个事件发生
        	selector.select();
        	Set<SelectionKey> keys = selector.selectedKeys();
        	Iterator<SelectionKey> iterator = keys.iterator();
        	while(iterator.hasNext()){

        		SelectionKey key = (SelectionKey)iterator.next();
        		iterator.remove();
        		
        		if(key.isAcceptable()){
           	     	System.out.println("acceptable");
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
        				sKey.attach(nameString);
        				
        				channel.configureBlocking(false);
        		        //channel.write(ByteBuffer.wrap("hzlclient".getBytes()));
        			}else{
        				channel.close();
        				
        			}
        			buffer.clear();
        		}else if(key.isWritable()){
        			System.out.println("writable receive");
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
