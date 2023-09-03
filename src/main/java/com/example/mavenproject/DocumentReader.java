package com.example.mavenproject;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DocumentReader{
	
	public static final int BUFF = 4096;

	public static Document getData(InputStream in) throws Exception {
		ArrayList<Byte> bytes = new ArrayList<>();
		int count = BUFF;
		while(count > 0) {
			byte[] read = new byte[BUFF];
			count = in.read(read, 0, BUFF);
			for (int i = 0; i < count; i++)
				bytes.add(read[i]);
		}
		
		byte[] byteArray = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++)
			// Using a loop because the automatic function Array.toList() doesn't convert the object Byte to the primitive type byte.
			byteArray[i] = bytes.get(i);
		String xml = new String(byteArray, StandardCharsets.UTF_8);
		
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(xml));
	    Document doc = db.parse(is);
	    return doc;
	}

}
