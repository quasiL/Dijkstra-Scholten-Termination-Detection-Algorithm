package cz.cvut.dsv.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class Variables implements Serializable {

    private Integer defIn = 0;
    private Integer defOut = 0;
    private List<Address> others = Collections.synchronizedList(new ArrayList<>());
    private Address parent = null;
    private Boolean isInitiator = false;

    private Map<List<String>, Address> messagesToSend = new ConcurrentHashMap<>();
    private Map<List<String>, Address> messages = new ConcurrentHashMap<>();
    private Map<String, Integer> result = new ConcurrentHashMap<>();

    private Map<Map<String, Integer>, Address> signalToSend = new ConcurrentHashMap<>();
}
