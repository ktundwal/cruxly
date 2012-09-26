package com.cruxly.lib.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

public interface Storage {

	DataOutputStream getDataOutputStream(String fileName) throws IOException;
	DataInputStream getDataInputStream(String fileName) throws IOException;
	Date getModelLastTrainedOn();
	boolean fileAvailable(String fileName) throws IOException;
	boolean delete(String fileName) throws IOException;

}
