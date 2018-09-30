package org.chickenbox.json;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class OutputStream {
	private Vector<String> propertyNames = new Vector<>();
	private HashMap<String, Integer> propertyIndexLookup = new HashMap<>();
	
	private final String stringEncoding;
	
	OutputStream(){
		this.stringEncoding = "utf-8";
	}
	
	OutputStream( String stringEncoding){
		this.stringEncoding = stringEncoding;
	}
	

	private int getIndex( String key ) {
		if( !propertyIndexLookup.containsKey(key)) {
			propertyNames.add(key);
			propertyIndexLookup.put(key, propertyNames.size()-1);
		}
		return propertyIndexLookup.get(key);
	}

	private double toDouble( Object json ) {
		if( json instanceof Byte ) {
			return (Byte)json;
		}else if( json instanceof Short ) {
			return (Short)json;
		}else if( json instanceof Integer ) {
			return (Integer)json;
		}else if( json instanceof Long ) {
			return (Long)json;
		}else if( json instanceof Float ) {
			return (Float)json;
		}else if( json instanceof Double ) {
			return (Double)json;
		}
		return 0;
	}
	
	private void _writeNumber( Object json, DataOutputStream outBuf ) throws IOException {
		double d = toDouble(json);
		
		if (d%1.0d==0) {
          if( d>=-128 && d<128 ){
              outBuf.writeByte(DataType._byte.ordinal());
              outBuf.writeByte((int) d);
          }else if( d>=-32768 && d<32768 ) {
        	  outBuf.writeByte(DataType._short.ordinal());
              outBuf.writeShort((int) d);
          }else if( d>=-2147483648 && d<2147483648L ){
        	  outBuf.writeByte(DataType._int32.ordinal());
              outBuf.writeInt((int) d);
          }else{
        	  outBuf.writeByte(DataType._double.ordinal());
              outBuf.writeDouble(d);
            }
        }else{
          if( ((float)d)==d ){
        	  outBuf.writeByte(DataType._float.ordinal());
              outBuf.writeFloat((float)d);
          }else{
        	  outBuf.writeByte(DataType._double.ordinal());
              outBuf.writeDouble(d);
          }
        }
	}
	
	private void _write( Object json, DataOutputStream outBuf ) throws IOException {
		if( json instanceof Byte ) {
			_writeNumber(json, outBuf);
		}else if( json instanceof Short ) {
			_writeNumber(json, outBuf);
		}else if( json instanceof Integer ) {
			_writeNumber(json, outBuf);
		}else if( json instanceof Long ){
			_writeNumber(json, outBuf);
		}else if( json instanceof Float ){
			_writeNumber(json, outBuf);
		}else if( json instanceof Double ){
			_writeNumber(json, outBuf);
		}else if( json instanceof Boolean ){
			outBuf.writeByte(DataType.bool.ordinal());
			outBuf.writeBoolean((Boolean)json);
		}else if( json instanceof String ){
			outBuf.writeByte(DataType.string.ordinal());
			String s = (String)json;
			byte [] b = s.getBytes(this.stringEncoding);
			outBuf.writeInt(b.length);
			outBuf.write(b);
		}else if( json instanceof JSONArray ){
			outBuf.writeByte(DataType.array.ordinal());
			JSONArray array = (JSONArray)json;
			_write(array.length(), outBuf);
			for( int i=0; i<array.length(); i++ ) {
				_write(array.get(i), outBuf);
			}
		}else if( json instanceof JSONObject ){
			outBuf.writeByte(DataType.object.ordinal());
			JSONObject obj = (JSONObject)json;
			_write(obj.keySet().size(), outBuf);
			for( String key : obj.keySet() ) {
				_write(getIndex(key), outBuf);
				_write(obj.get(key), outBuf);
			}
		}else {
			outBuf.writeByte(DataType._null.ordinal());
		}
	}	
	
	public byte[] write( Object json ) throws IOException {
		int startIndex = this.propertyNames.size();
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
		DataOutputStream outBufWriter = new DataOutputStream(outBuf);
		
      	_write(json, outBufWriter);
      	
      	outBufWriter.close();
      	outBuf.close();
  	
      	ByteArrayOutputStream headerOutBuf = new ByteArrayOutputStream();
      	DataOutputStream headerWriter = new DataOutputStream(headerOutBuf);
      	headerWriter.writeShort(propertyNames.size()-startIndex);
      	for( int i=startIndex; i<propertyNames.size(); i++ ) {
      		String s = propertyNames.get(i);
			byte [] b = s.getBytes(this.stringEncoding);
			headerWriter.writeByte(b.length);
			headerWriter.write(b);
      	}
      	
      	headerWriter.write(outBuf.toByteArray());
      	
      	headerWriter.close();
      	headerOutBuf.close();
  
  		return headerOutBuf.toByteArray();
	}
}
