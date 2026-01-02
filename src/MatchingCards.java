import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;



class Card {
    String cardName;
    ImageIcon cardImageIcon;

    Card(String cardName, ImageIcon cardImageIcon) {
        this.cardName = cardName;
        this.cardImageIcon = cardImageIcon;
    }

    public String toString() {
        return cardName;
    }
}

public class MatchingCards {

    String[] cardList = { // track cardNames
            "darkness",
            "double",
            "fairy",
            "fighting",
            "fire",
            "grass",
            "lightning",
            "metal",
            "psychic",
            "water"
    };
    Stack<JButton> undoStack = new Stack<>();
    int rows = 4;
    int columns = 5;
    int cardWidth = 90;
    int cardHeight = 128;

    ArrayList<Card> cardSet; // create a deck of cards with cardNames and cardImageIcons
    ImageIcon cardBackImageIcon;

    int boardWidth = columns * cardWidth; // 5*128 = 640px
    int boardHeight = rows * cardHeight; // 4*90 = 360px

    JFrame frame = new JFrame("Pokemon Match Cards");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel restartGamePanel = new JPanel();
    JButton restartButton = new JButton();

    int errorCount = 0;
    ArrayList<JButton> board;
    Timer hideCardTimer;
    boolean gameReady = false;
    Timer gameTimer;
    int elapsedTime = 0;
    JLabel timerLabel;

    JButton card1Selected;
    JButton card2Selected;

    MatchingCards() {
        setupCards();
        shuffleCards();

        // frame.setVisible(true);
        frame.setLayout(new BorderLayout());
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null); // Game Window will appear at the centre
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // error text
        textLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Errors: " + Integer.toString(errorCount));
        textPanel.setPreferredSize(new Dimension(boardWidth, 30));
        textPanel.add(textLabel);
        timerLabel = new JLabel("Time: 0 sec");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        textPanel.add(timerLabel);  // Adds it to the same panel as error counter
        frame.add(textPanel, BorderLayout.NORTH);


        // card game board
        board = new ArrayList<JButton>();
        boardPanel.setLayout(new GridLayout(rows, columns));
        for (int i = 0; i < cardSet.size(); i++) {
            JButton tile = new JButton();
            tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
            tile.setOpaque(true);
            tile.setIcon(cardSet.get(i).cardImageIcon);
            tile.setFocusable(false);
            tile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!gameReady) {
                        return;
                    }

                    JButton tile = (JButton) e.getSource();
                    if (card1Selected == null && card2Selected == null && elapsedTime == 0) {
                        startGameTimer();
                    }
        
                    if (tile.getIcon() == cardBackImageIcon) {
                        if (card1Selected == null) {
                            card1Selected = tile;
                            undoStack.push(tile); // <-- ADD THIS
                            int index = board.indexOf(card1Selected);
                            card1Selected.setIcon(cardSet.get(index).cardImageIcon);
                        } 
                        else if (card2Selected == null) {
                            card2Selected = tile;
                            undoStack.push(tile); // <-- ADD THIS
                            int index = board.indexOf(card2Selected);
                            card2Selected.setIcon(cardSet.get(index).cardImageIcon);

                            if (card1Selected.getIcon() != card2Selected.getIcon()) {
                                errorCount += 1;
                                textLabel.setText("Errors: " + Integer.toString(errorCount));
                                hideCardTimer.start();
                            } 
                            else {
                                card1Selected = null;
                                card2Selected = null;
                                if (allCardsMatched() && gameTimer != null) {
                                    gameTimer.stop();
                                    JOptionPane.showMessageDialog(frame, "🎉 All pairs matched!\nTime: " + elapsedTime + " sec\nErrors: " + errorCount, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                    }

                }
            });
            board.add(tile);
            boardPanel.add(tile);
        }
        frame.add(boardPanel);

        // restart game button
        restartButton.setFont(new Font("Arial", Font.PLAIN, 16));
        restartButton.setText("Restart Game");
        restartButton.setPreferredSize(new Dimension(boardWidth, 30));
        restartButton.setFocusable(false);
        restartButton.setEnabled(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameReady) {
                    return;
                }
                
                gameReady = false;
                restartButton.setEnabled(false);
                card1Selected = null;
                card2Selected = null;
                undoStack.clear();

                shuffleCards();

                // re assign buttons with new cards
                for (int i = 0; i < board.size(); i++) {
                    board.get(i).setIcon(cardSet.get(i).cardImageIcon);
                }

                errorCount = 0;
                textLabel.setText("Errors: " + Integer.toString(errorCount));
                hideCardTimer.start();
                startGameTimer();

                if (gameTimer!= null)
                {
                    gameTimer.stop();
                }
                elapsedTime =0;
                timerLabel.setText("Time: 0 sec");
                hideCardTimer.start();


            }
        });
        restartGamePanel.add(restartButton);
        JButton undoButton = new JButton("Undo");
        undoButton.setFocusable(false);
        undoButton.addActionListener(e -> {
            if (!undoStack.isEmpty()) {
                JButton lastCard = undoStack.pop();
                lastCard.setIcon(cardBackImageIcon);

                // FIX game state
                if (lastCard == card2Selected) {
                    card2Selected = null;
                } else if (lastCard == card1Selected) {
                    card1Selected = null;
                }
            }
        });
        restartGamePanel.add(undoButton);   
        frame.add(restartGamePanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

        // start game
        hideCardTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideCards();
            }
        });
        hideCardTimer.setRepeats(false);
        hideCardTimer.start();

    }



    void startGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    
        elapsedTime = 0;
        timerLabel.setText("Time: 0 sec");
    
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTime++;
                timerLabel.setText("Time: " + elapsedTime + " sec");
            }
        });
    
        gameTimer.start();
    }

    private boolean allCardsMatched() {
        for (JButton button : board) {
            if (button.getIcon() == cardBackImageIcon) {
                return false; // Found a face-down card, game is not complete
            }
        }
        return true; // All cards are face-up (matched)
    }

    void setupCards() {
        cardSet = new ArrayList<Card>();
        for (String cardName : cardList) {
            // load each card image
            Image cardImg = new ImageIcon(getClass().getResource("./img/" + cardName + ".jpg")).getImage();
            ImageIcon cardImageIcon = new ImageIcon(
                    cardImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_SMOOTH)); // Resizing the image 

            // create card object and add to cardSet
            Card card = new Card(cardName, cardImageIcon);
            cardSet.add(card);
        }

        cardSet.addAll(cardSet); // To make pairs, you duplicate them

        // load the back card image
        Image cardBackImg = new ImageIcon(getClass().getResource("./img/back.jpg")).getImage();
        cardBackImageIcon = new ImageIcon(
                cardBackImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_SMOOTH));
    }

    
    void shuffleCards() {
        System.out.println(cardSet);
        // shuffle
        for (int i = 0; i < cardSet.size(); i++) {
            int j = (int) (Math.random() * cardSet.size()); // get random index from 0-19
            // swap
            Card temp = cardSet.get(i);
            cardSet.set(i, cardSet.get(j));
            cardSet.set(j, temp);
        }
        System.out.println(cardSet);
    }

    void hideCards() {
        if (gameReady && card1Selected != null && card2Selected != null) { // Flip back Two Mismatched Cards
            card1Selected.setIcon(cardBackImageIcon);
            card1Selected = null;
            card2Selected.setIcon(cardBackImageIcon);
            card2Selected = null;
        } 
        else { // flip all cards face down
            for (int i = 0; i < board.size(); i++) {
                board.get(i).setIcon(cardBackImageIcon);
            }
            gameReady = true;
            restartButton.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        MatchingCards c = new MatchingCards();
    }
}