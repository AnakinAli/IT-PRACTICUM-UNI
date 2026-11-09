package websockets;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/** Pairs connected clients and manages server-authoritative tic-tac-toe games. */
@ServerEndpoint("/tictactoe")
public class TicTacToe {
    private static final Object LOCK = new Object();
    private static final Map<String, Game> gamesByPlayer = new HashMap<>();
    private static Session waitingPlayer;

    @OnOpen
    public void open(Session session) {
        synchronized (LOCK) {
            if (waitingPlayer == null || !waitingPlayer.isOpen()) {
                waitingPlayer = session;
                send(session, "waiting");
                return;
            }

            Game game = new Game(waitingPlayer, session);
            gamesByPlayer.put(game.x.getId(), game);
            gamesByPlayer.put(game.o.getId(), game);
            waitingPlayer = null;
            send(game.x, "start:X:yourTurn");
            send(game.o, "start:O:wait");
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        synchronized (LOCK) {
            Game game = gamesByPlayer.get(session.getId());
            if (game == null || game.finished || !message.startsWith("move:")) {
                return;
            }

            int cell;
            try {
                cell = Integer.parseInt(message.substring("move:".length()));
            } catch (NumberFormatException exception) {
                send(session, "error:Невалидно поле.");
                return;
            }

            if (!game.play(session, cell)) {
                send(session, "error:Този ход не е позволен.");
                return;
            }

            String symbol = game.board[cell];
            Session nextPlayer = game.currentPlayer();
            send(game.x, "move:" + cell + ":" + symbol + (game.x.equals(nextPlayer) ? ":yourTurn" : ":wait"));
            send(game.o, "move:" + cell + ":" + symbol + (game.o.equals(nextPlayer) ? ":yourTurn" : ":wait"));

            String winner = game.winner();
            if (winner != null) {
                game.finished = true;
                if ("draw".equals(winner)) {
                    send(game.x, "end:Равенство!");
                    send(game.o, "end:Равенство!");
                } else {
                    send(game.x, "end:" + (winner.equals("X") ? "Победихте!" : "Загубихте."));
                    send(game.o, "end:" + (winner.equals("O") ? "Победихте!" : "Загубихте."));
                }
                removeGame(game);
            }
        }
    }

    @OnClose
    public void close(Session session) {
        synchronized (LOCK) {
            if (session.equals(waitingPlayer)) {
                waitingPlayer = null;
            }

            Game game = gamesByPlayer.get(session.getId());
            if (game != null) {
                Session opponent = game.x.equals(session) ? game.o : game.x;
                send(opponent, "opponentLeft");
                removeGame(game);
            }
        }
    }

    private static void removeGame(Game game) {
        gamesByPlayer.remove(game.x.getId());
        gamesByPlayer.remove(game.o.getId());
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

    private static final class Game {
        private static final int[][] WINNING_LINES = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        private final Session x;
        private final Session o;
        private final String[] board = new String[9];
        private boolean xTurn = true;
        private boolean finished;

        private Game(Session x, Session o) {
            this.x = x;
            this.o = o;
        }

        private boolean play(Session player, int cell) {
            if (cell < 0 || cell >= board.length || board[cell] != null) {
                return false;
            }
            if ((xTurn && !x.equals(player)) || (!xTurn && !o.equals(player))) {
                return false;
            }
            board[cell] = xTurn ? "X" : "O";
            xTurn = !xTurn;
            return true;
        }

        private Session currentPlayer() {
            return xTurn ? x : o;
        }

        private String winner() {
            for (int[] line : WINNING_LINES) {
                String value = board[line[0]];
                if (value != null && value.equals(board[line[1]]) && value.equals(board[line[2]])) {
                    return value;
                }
            }
            return Arrays.stream(board).allMatch(value -> value != null) ? "draw" : null;
        }
    }
}
