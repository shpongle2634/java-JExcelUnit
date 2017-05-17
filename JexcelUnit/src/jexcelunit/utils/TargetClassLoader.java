package jexcelunit.utils;

import java.util.HashMap;
import java.util.Map;

public class TargetClassLoader extends ClassLoader {
    private Map<String,MemoryByteCode> m = new HashMap<String,MemoryByteCode>();
    
    protected Class<?> findClass(String name) throws ClassNotFoundException {       
        MemoryByteCode mbc = m.get(name);       
        if (mbc==null){           
            mbc = m.get(name.replace(".","/"));           
            if (mbc==null){               
                return super.findClass(name);           
            }       
        }       
        return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);   
    }
 
    public void addClass(String name, MemoryByteCode mbc) {       
        m.put(name, mbc);   
    }
}
