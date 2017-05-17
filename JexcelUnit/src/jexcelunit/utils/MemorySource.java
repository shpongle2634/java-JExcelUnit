package jexcelunit.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.SimpleJavaFileObject;

public class MemorySource extends SimpleJavaFileObject {

    private String src;   
    public MemorySource(String name, String src) throws URISyntaxException {     
        super(URI.create("file:///"+ name + ".java"), Kind.SOURCE);
        this.src = src;   
    }   
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {       
        return src;   
    }   
    public OutputStream openOutputStream() {       
        throw new IllegalStateException();   
    }   
    public InputStream openInputStream() {       
        return new ByteArrayInputStream(src.getBytes());   
    }
}
