import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
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

    public static void writeMessage(DataOutputStream out, Message msg) {
        try {
            String json = toJson(msg);
            log.info("Sending JSON: {}", json);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            out.writeInt(data.length);
            out.write(data);
            out.flush();
            log.info("Message: {} delivered successfully to outputStream", msg);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static Message readMessage(DataInputStream in) {
        try {
            int len = in.readInt();
            byte[] data = new byte[len];
            in.readFully(data);
            String json = new String(data, StandardCharsets.UTF_8);
            log.info("Message: {} received successfully", json);
            return fromJson(json);
        }
        catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
