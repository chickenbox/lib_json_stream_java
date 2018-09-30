package org.chickenbox.json;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class InputStream {
	private Vector<String> decodePropertyNames = new Vector<>();
	
	private final DataInputStream input;
	
	private final String stringEncoding;
	
	InputStream( java.io.InputStream inputStream ){
		this.input = new DataInputStream(inputStream);
		this.stringEncoding = "utf-8";
	}
	
	InputStream(  java.io.InputStream inputStream, String stringEncoding){
		this.input = new DataInputStream(inputStream);
		this.stringEncoding = stringEncoding;
	}
	
	protected void finalize() throws Throwable {
		this.input.close();
		super.finalize();
	}
	
	private int toInt( Object json ) {
		if( json instanceof Byte ) {
			return (Byte)json;
		}else if( json instanceof Short ) {
			return (Short)json;
		}else if( json instanceof Integer ) {
			return (Integer)json;
		}else if( json instanceof Long ) {
			return ((Long)json).intValue();
		}else if( json instanceof Float ) {
			return ((Float)json).intValue();
		}else if( json instanceof Double ) {
			return ((Double)json).intValue();
		}
		return 0;
	}	

	private Object _read() throws IOException {
		DataType type = DataType.values()[input.readByte()];
		switch (type) {
		case _byte:
			return input.readByte();
		case _short:
			return input.readShort();
		case _int32:
			return input.readInt();
		case _float:
			return input.readFloat();
		case _double:
			return input.readDouble();
		case bool:
			return input.readBoolean();
		case string:
			int strLen = input.readInt();
			byte [] buf = new byte[strLen];
			input.read(buf);		
			return new String(buf,this.stringEncoding);
		case array:
		{
			JSONArray a = new JSONArray();
			int len = toInt(_read());
			for( int i=0; i<len; i++ ) {
				a.put(_read());
			}
			return a;
		}
		case object:
		{
			JSONObject obj = new JSONObject();
			int len = toInt(_read());
			for( int i=0; i<len; i++ ) {
				String key = decodePropertyNames.get(toInt(_read()));
				obj.put(key, _read());
			}
			return obj;
		}
		case _null:
			return null;
		}
		
		return null;
	} 
	
	public Object read() throws IOException {
		int numKey = input.readShort();
		for( int i=0; i<numKey; i++ ) {
			int strLen = input.readByte();
			byte [] buf = new byte[strLen];
			input.read(buf);		
			String s = new String(buf,this.stringEncoding);
			decodePropertyNames.add( s );
		}
		Object o = _read();
		return o;
	}
}
