package progetto_2;

import java.util.AbstractMap;

public class ShareEntry extends AbstractMap.SimpleEntry {
    public ShareEntry(String file, Integer hash) {
        super(file, hash);
    }

    public String getFile(){
        return (String)super.getKey();
    }
    public Integer getHash(){
        return (Integer)super.getValue();
    }
}
