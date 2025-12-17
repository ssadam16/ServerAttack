public enum MessageType {
    JOIN,
    JOIN_ACK,
    ACTION,
    ACTION_ACK,
    STATE_UPDATE,
    LOG_EVENT,
    VOTE,
    VOTE_RESULT,
    PRESENCE_UPDATE,
    PING,
    PONG,
    ERROR,
    HACKER_ACTION,  // Новый тип для хакерских действий
    SABOTAGE_ALERT  // Новый тип для оповещения о саботаже
}