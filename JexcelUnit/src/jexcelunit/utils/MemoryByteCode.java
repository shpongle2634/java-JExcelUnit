package jexcelunit.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.SimpleJavaFileObject;

class MemoryByteCode extends SimpleJavaFileObject {   
    private ByteArrayOutputStream baos;   
    public MemoryByteCode(String name) throws URISyntaxException {       
        super(URI.create("byte:///"+ name+ ".class"), Kind.CLASS);   
    }   
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {       
        throw new IllegalStateException();   
    }   
    public OutputStream openOutputStream() {       
        baos = new ByteArrayOutputStream();       
        return baos;   
    }   
    public InputStream openInputStream() {       
        throw new IllegalStateException();   
    }   
    public byte[] getBytes() {       
        return baos.toByteArray();   
    }
}
