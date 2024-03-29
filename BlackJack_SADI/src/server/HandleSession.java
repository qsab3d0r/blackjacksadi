package server;

import java.io.*;
import java.net.*;
import java.util.*;
import utility.*;

/**
 * 
 * @author Suzann
 * This class is to handle the game sessions between client and server
 */

class HandleSession extends Thread implements GameStatus
{
	private Socket playerSocket[]  = null;
	private DataInputStream fromPlayer[]  = null;
	private DataOutputStream toPlayer[]  = null;
	private BlackJackGame game = null;
	private Vector <Player> players;
	private boolean isGameEnd = false;
	private int whoseTurn = 0;
	private String winnerMsg = "";
	
	public HandleSession(BlackJackGame game)
	{
		//initialise all the variables
		players = game.getWaitingPlayers();
		playerSocket = new Socket [MAXPLAYERS];
		fromPlayer = new DataInputStream[MAXPLAYERS];
		toPlayer = new DataOutputStream [MAXPLAYERS];
		this.game = game;
		
		//Add the playing from waiting list to player list
		for (int i=0; i<players.size() && i < GameStatus.MAXPLAYERS; i++)
		{
			if(game.addPlayer(players.elementAt(i)))
			{
				game.addPlayersCount();
				game.minusWaitingPlayersCount();
			}
		}
		
		//Show the total players joined for the game.
		game.append("Total players joined: " + game.getPlayersCount() + "\n\n");
		
		//The dealer is also one of the player. Add the dealer into the player list.
		game.addPlayer(game.getDealer());
		
		players = game.getPlayers();//reusing var players
		
		//Remove the players from waiting list after added.
		game.removeWaitingPlayers();//to change for connection
	}
	
	//start the thread here
	public void run()
	{
		//create the scoreboard
		game.initScoreBoard(game.getPlayersCount());
		
		try
		{
			//initilise all the sockets and data I/O streams
			for (int i=0; i<game.getPlayersCount(); i++)
			{
				playerSocket[i] = players.elementAt(i).getSocket();
				fromPlayer[i] = new DataInputStream(playerSocket[i].getInputStream());
				toPlayer[i] = new DataOutputStream(playerSocket[i].getOutputStream());
			}
			
			//write to players and notify their turn
			for (int i=0; i<game.getPlayersCount(); i++)
			{
				players.elementAt(i).setPlayerTurn(i+1);
				int turn = players.elementAt(i).getPlayerTurn();
				toPlayer[i].writeUTF("Game started, your turn is: " + turn);
				toPlayer[i].writeInt(turn);
			}
			
			for (int trial=0; trial< MAXTRIALS; trial++)
			{
				//get current trial number				
				//start a new game
				if(trial==0)
					game.newGame();				
				else if (trial!=0)			
					game.restartGame();
					
				//distribute to each player 2 cards (dealer the last)
				for (int j=0; j<2; j++)
				{
					for (int i=0; i<game.getPlayersCount() +1 /*including the dealer */; i++)
					{
						game.deal();
						game.nextPlayer();
					}
				}
					
				//Tell the players the two card they are given 
				//This approach is implemented for GUI based
				for (int i=0; i<players.size() && i < GameStatus.MAXPLAYERS; i++)
				{
					for (int j=0; j<2; j++)
					{
						if(i<MAXPLAYERS)
						{
							//write the suit then value
							int suit = game.getPlayers().elementAt(i).getHand().getCard(j).getSuit();
							int value = game.getPlayers().elementAt(i).getHand().getCard(j).getCardValue();
							
							toPlayer[i].writeInt(suit);
							toPlayer[i].writeInt(value);
						}
					}
				}
				game.append("\n------------------------------Trial = " + game.getTrial() + "--------------------------------\n");	
				
				//List down the cards that each players has in server side
				for (int i=0; i<players.size() && i < GameStatus.MAXPLAYERS + 1; i++)
				{
					BlackJackHand hand = game.getPlayers().elementAt(i).getHand();
					if(i<MAXPLAYERS)
						game.append("Player " + (i+1) + " has " + hand.getCardsOnHand() + "\n");
					else
						game.append("Dealer has "+ hand.getCardsOnHand() + "\n");
				}
				
				//Get whose turn for this game
				whoseTurn = game.getWhoseTurn();
				
				for (int i=0; i<players.size() && i < GameStatus.MAXPLAYERS; i++)
				{
					//tell the players trial number
					toPlayer[i].writeUTF("\nTrial = " + game.getTrial());
				}
				
				//Tell all the players in the player list about their turns
				while(whoseTurn < DEALER && whoseTurn < players.size())
				{
					//game.append("----------WhoseTurn2:----------" + game.getWhoseTurn() +"\n");
					
					game.append("\nNow is Player " + game.getWhoseTurn() + " turn.\n Waiting player to perform action.\n");
										
					//Notify player to start
					//toPlayer[game.getWhoseTurn()-1].writeInt(game.getWhoseTurn());
					for (int i=0; i<players.size() && i < GameStatus.MAXPLAYERS; i++)
					{
						//tell the players trial number
						//toPlayer[i].writeUTF("\nTrial = " + game.getTrial());
						
						//send an integer to client indicating whose turn now
						toPlayer[i].writeInt(game.getWhoseTurn());
						
						
						if ((i+1) == game.getWhoseTurn())
						{
							toPlayer[i].writeUTF("Your turn now.\nYou may type 1 to HIT, or 2 to STAND.\n");
							
						}
						else
							toPlayer[i].writeUTF("Now is Player " + game.getWhoseTurn() + " turn.\nWaiting player to perform action...\n");
						
						
					}
						
					
					//while the player is still elligible to continue hitting, hit one card to player
					boolean continueHit = true;
					String actionString = "", cards = "";
					
					while (continueHit)
					{
						int action = fromPlayer[game.getWhoseTurn() - 1].readInt();
						
						if (action == HIT)
						{
							if(game.getPlayers().elementAt(game.getWhoseTurn() - 1).getHand().getCardsTotal() < 5)
							{
								actionString ="HIT";
								//hit card if client select hit
								game.hit();
								
								//calculate the total of the card and send to the client side
								continueHit = true;
							}
							else
								action = STAND;
						}
						if (action == STAND)
						{
							actionString = "STAND";
							continueHit = false;
						}
						else if(action == HIT)
							actionString ="HIT";
						else
						{
							actionString = "INVALID";
							continueHit = true;
						}
						
						//Tell the player its action
						toPlayer[game.getWhoseTurn() -1].writeUTF(actionString);
												
						//tell the client the cards on hand that he has
						//write the suit then value
						if (action == HIT)
						{
							int suit = game.getPlayers().elementAt(game.getWhoseTurn() -1).getHand().getLastCard().getSuit();
							int value = game.getPlayers().elementAt(game.getWhoseTurn() -1).getHand().getLastCard().getCardValue();
							toPlayer[game.getWhoseTurn() -1].writeInt(suit);
							toPlayer[game.getWhoseTurn() -1].writeInt(value);
						}
						
						
						//tell all the player that current player can / cannot continue hitting
						for(int i = 0; i < players.size() && i < GameStatus.MAXPLAYERS; i++)
							toPlayer[i].writeBoolean(continueHit);
						
						String msg = "Player action: " + actionString + "\n";
						game.append(msg);
						game.append("Player " + game.getWhoseTurn() + " has cards: "  + game.getPlayers().elementAt(game.getWhoseTurn() - 1).getHand().getCardsOnHand() + "\n");
						game.append("Value on Player " + game.getWhoseTurn() + " hand: " + game.getPlayers().elementAt(whoseTurn -1).getHand().calculateValue() + '\n');
						
						if(!continueHit && action == STAND)
						{
							game.stand();
							whoseTurn = game.getWhoseTurn();
						}
					}
				}
				
				//now dealer's turn to hit/stand
				while (game.getDealer().getHand().isUnder17())
					game.hit();
				
				//show the cards in dealer's hand
				game.append("Dealer has cards: " + game.getPlayers().elementAt(whoseTurn -1).getHand().getCardsOnHand() + '\n');
				game.append("Value on dealer's hand: " + game.getPlayers().elementAt(whoseTurn -1).getHand().calculateValue() + '\n');

				//display the results
				int playerValue [] = new int [game.getPlayersCount()];
				int highestPoint , winnerTurn;		
				boolean hasMoreWinner = false, hasTwoAce = false, hasBlackJack = false; /*counter keep track of two or more winner*/
					
				//count the dealer's cards first
				BlackJackHand dealerHand = game.getDealer().getHand();
				int dealerValue = dealerHand.calculateValue(), dealerTurn = game.getPlayersCount() +1 ;
				
				//if the dealer has only two cards and is blackjack / two aces
				if(dealerHand.isOnlyTwoCards())
				{
					if (dealerHand.isBlackJack())
					{
						hasBlackJack = true;
					}
					else if (dealerHand.isTwoAce())
					{
						hasTwoAce = true;
					}
					
					highestPoint = dealerValue;
					winnerTurn = dealerTurn;
				}
				
				//set dealer as the default winner if it is not burst
				else if (!dealerHand.isOnlyTwoCards() && !dealerHand.isBurst())
				{
					highestPoint = dealerValue;
					winnerTurn = game.getPlayersCount() + 1;
				}
				
				else
				{
					highestPoint = 0;
					winnerTurn = DRAW;
				}
				//compare with the dealer's value (if dealer is not burst)
				for (int i = 0; i < game.getPlayersCount(); i ++)
				{
					BlackJackHand playerHand = game.getPlayers().elementAt(i).getHand();
					playerValue[i] = playerHand.calculateValue();
					
					if (playerHand.isOnlyTwoCards())
					{
						if (playerHand.isBlackJack() && hasBlackJack == false)
						{
							hasBlackJack = true;
							winnerTurn = i+1;
						}
						else if (playerHand.isTwoAce() && hasTwoAce == false)
						{
							hasTwoAce = true;
							winnerTurn = i+1;
						}
						else if ((playerValue[i] > highestPoint) &&( hasBlackJack == false && hasTwoAce == false))
						{
							highestPoint = playerValue[i];
							winnerTurn = i + 1;
						}
						else if (playerValue[i] == highestPoint)
						{
							hasMoreWinner = true;
							winnerTurn = DRAW;
							//break;
						}
					}
					else if (!playerHand.isOnlyTwoCards() && (playerHand.isOver17() && playerHand.isUnder21()))
					{
						if(playerHand.is21() && (hasBlackJack == false || hasTwoAce == false))
						{
							if (playerValue[i] > highestPoint)
							{
								highestPoint = playerValue[i];
								winnerTurn = i + 1;
							}
						}
						else if((hasBlackJack == false || hasTwoAce == false))
						{
							if (playerValue[i] > highestPoint)
							{
								highestPoint = playerValue[i];
								winnerTurn = i + 1;
							}
						}
						
						else if (playerValue[i] == highestPoint)
						{
							hasMoreWinner = true;
							winnerTurn = DRAW;
							//break;
						}
					}
				}
				
				//Determine the winner msg
				if (winnerTurn == DRAW || winnerTurn == 0)
						winnerMsg = "\nThis game is a draw. There is no winner.\n";
				
				else
				{
					if(winnerTurn <= game.getPlayersCount())
						winnerMsg = "\nThe winner is Player " + winnerTurn + '\n';
					else
						winnerMsg = "\n*******The dealer wins the game!*******\n";
				}
				winnerMsg += "\n";
				game.append(winnerMsg);
				
				//determine if the game still continues
				if(game.getTrial() < MAXTRIALS)
					isGameEnd = false;
				else
					isGameEnd = true;
				
				//send the signal to the client whether to continue playing or not
				for (int i = 0; i < game.getPlayersCount(); i ++)
					toPlayer[i].writeBoolean(isGameEnd);			
				
				//tell the client who's the winner
				for (int i = 0; i < game.getPlayersCount(); i ++)
					toPlayer[i].writeInt(winnerTurn);
				
				//add the winner into scoreboard
				game.setWinner(winnerTurn);
			}
			
			//display the score board
			String scoreBoard = game.getScoreBoard().getResults();
			game.append("Scores for this game: " + scoreBoard + "\n" );
			
			//send the scoreboard to the client
			for (int i = 0; i < game.getPlayersCount(); i ++)
				toPlayer[i].writeUTF(scoreBoard);	
		}
		catch(IOException ex)
		{
			game.append(ex.toString());
		}
	}
	
	
}
