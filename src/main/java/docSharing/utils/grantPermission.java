package docSharing.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class grantPermission {
    public static List<String> list ;

    /**
     * items in URI that don't need to check for permission
     */
    public grantPermission() {
        this.list = new ArrayList<>();
        list.add("getAll");
        list.add("getPath");
        list.add("getUser");
        list.add("ws");
        list.add("getContent");
        list.add("onlineUsers");
        list.add("import");
        list.add("export");
        list.add("isExists");
        list.add("auth");
    }
}
