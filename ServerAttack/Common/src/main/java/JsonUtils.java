import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Message msg) throws JsonProcessingException {
        return mapper.writeValueAsString(msg);
    }

    public static Message fromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, Message.class);
    }

    public static JsonNode toJsonNode(Object o) {
        return mapper.valueToTree(o);
    }

    public static JsonNode parse(String json) throws JsonProcessingException {
        return mapper.readTree(json);
    }

    public static void writeMessage(DataOutputStream out, Message msg) throws IOException {
        String json = toJson(msg);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public static Message readMessage(DataInputStream in) throws IOException {
        int len = in.readInt();
        byte[] data = new byte[len];
        in.readFully(data);
        String json = new String(data, StandardCharsets.UTF_8);
        return fromJson(json);
    }

}
