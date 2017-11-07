package com.blackduck.integration.scm.fileinject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.undertow.io.Sender;

/**
 * Used to model writing Undertow responses as an output stream. Implements
 * buffering, so no additional buffering is needed.
 * 
 * @author ybronshteyn
 *
 */
public class SenderOutputStream extends OutputStream {
	private final Sender sender;
	
	private final int bufferSize = 1042;
	
	private final ByteBuffer buf;

	public SenderOutputStream(Sender sender) {
		this.sender = sender;
		buf = ByteBuffer.allocate(1024);
	}

	@Override
	public void write(int b) throws IOException {
		if (buf.position() >= bufferSize)
			flush();
		buf.put((byte) b);
	}

	@Override
	public void flush() throws IOException {
		sender.send(buf);
		buf.clear();
		super.flush();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
	}

}
