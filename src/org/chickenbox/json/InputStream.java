package org.chickenbox.json;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class InputStream {
	private Vector<String> decodePropertyNames = new Vector<>();
	
	private final String stringEncoding;
	
	InputStream(){
		this.stringEncoding = "utf-8";
	}
	
	InputStream( String stringEncoding){
		this.stringEncoding = stringEncoding;
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

	private Object _decode( DataInputStream input ) throws IOException {
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
			int len = toInt(_decode(input));
			for( int i=0; i<len; i++ ) {
				a.put(_decode(input));
			}
			return a;
		}
		case object:
		{
			JSONObject obj = new JSONObject();
			int len = toInt(_decode(input));
			for( int i=0; i<len; i++ ) {
				String key = decodePropertyNames.get(toInt(_decode(input)));
				obj.put(key, _decode(input));
			}
			return obj;
		}
		case _null:
			return null;
		}
		
		return null;
	} 
	
	public Object decode( byte [] bytes ) throws IOException {
		ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
		DataInputStream input = new DataInputStream(byteInput);
		
		int numKey = input.readShort();
		for( int i=0; i<numKey; i++ ) {
			int strLen = input.readByte();
			byte [] buf = new byte[strLen];
			input.read(buf);		
			String s = new String(buf,this.stringEncoding);
			decodePropertyNames.add( s );
		}
		Object o = _decode( input );
		
		input.close();
		byteInput.close();
		
		return o;
	}
}
