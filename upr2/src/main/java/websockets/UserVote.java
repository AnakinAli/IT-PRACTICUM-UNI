package websockets;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/** WebSocket endpoint for a shared yes/no vote. */
@ServerEndpoint("/vote")
public class UserVote {
    private static final Object LOCK = new Object();
    private static final Map<String, Session> sessions = new HashMap<>();
    private static final Map<String, String> statuses = new HashMap<>();
    private static final Set<String> participants = new HashSet<>();

    private static boolean voteInAction;
    private static int yesVotes;
    private static int noVotes;

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        synchronized (LOCK) {
            sessions.put(session.getId(), session);
            statuses.put(session.getId(), "connected");
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        synchronized (LOCK) {
            if (!sessions.containsKey(session.getId())) {
                return;
            }

            if ("userReady".equals(message)) {
                handleReady(session);
                return;
            }

            if (message.startsWith("vote:")) {
                handleVote(session, message.substring("vote:".length()));
            }
        }
    }

    @OnClose
    public void close(Session session) {
        synchronized (LOCK) {
            sessions.remove(session.getId());
            statuses.remove(session.getId());
            participants.remove(session.getId());

            if (voteInAction) {
                finishVoteIfEveryoneVoted();
            } else {
                startVoteIfEveryoneIsReady();
            }
        }
    }

    private static void handleReady(Session session) {
        if (voteInAction) {
            statuses.put(session.getId(), "voteInAction");
            send(session, "voteInAction");
            return;
        }

        statuses.put(session.getId(), "userReady");
        send(session, "userReady");
        startVoteIfEveryoneIsReady();
    }

    private static void startVoteIfEveryoneIsReady() {
        if (sessions.isEmpty()) {
            return;
        }

        boolean allReady = sessions.keySet().stream()
                .allMatch(id -> "userReady".equals(statuses.get(id)));
        if (!allReady) {
            return;
        }

        voteInAction = true;
        participants.clear();
        participants.addAll(sessions.keySet());
        broadcast("startVote");
    }

    private static void handleVote(Session session, String vote) {
        String id = session.getId();
        if (!voteInAction || !participants.contains(id) || "voted".equals(statuses.get(id))) {
            return;
        }
        if (!"yes".equals(vote) && !"no".equals(vote)) {
            send(session, "Невалиден глас.");
            return;
        }

        if ("yes".equals(vote)) {
            yesVotes++;
        } else {
            noVotes++;
        }
        statuses.put(id, "voted");
        send(session, "voteAccepted");
        finishVoteIfEveryoneVoted();
    }

    private static void finishVoteIfEveryoneVoted() {
        boolean allVoted = participants.stream()
                .allMatch(id -> "voted".equals(statuses.get(id)));
        if (!allVoted) {
            return;
        }

        broadcast("result:Резултат — yes: " + yesVotes + ", no: " + noVotes);
        voteInAction = false;
        yesVotes = 0;
        noVotes = 0;
        participants.clear();
        statuses.replaceAll((id, status) -> "connected");
    }

    private static void broadcast(String message) {
        sessions.values().forEach(session -> send(session, message));
    }

    private static void send(Session session, String message) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
