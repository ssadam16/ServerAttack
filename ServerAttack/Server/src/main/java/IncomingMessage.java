import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class IncomingMessage {

    private final Message message;
    private final ClientHandler client;

}
