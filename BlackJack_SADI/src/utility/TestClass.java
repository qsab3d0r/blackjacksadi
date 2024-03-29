package utility;
import java.util.Vector;

import server.*;
import client.*;

public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
				
		BlackJackDeck deck = new BlackJackDeck();
		BlackJackHand hand = new BlackJackHand();
		//System.out.println("-------------Before shuffle: ------------");
		//deck.printDeck();
		
		deck.shuffle();
		
		/*System.out.println("----------After shuffle: ---------");
		deck.printDeck();
		System.out.println("---------------------------------");
		
		System.out.println("Deal Top card = " + deck.dealTopCard());
		deck.printDeck();
		System.out.println("Deal Top card = " + deck.dealTopCard());
		System.out.println("---------------------------------");
		System.out.println("Top card now is = " + deck.topCard());
		System.out.println("---------------------------------");*/
		
		//random card
		hand = new BlackJackHand();
		hand.addCard(new BlackJackCard(BlackJackCard.DIAMONDS,4));
		hand.addCard(new BlackJackCard(BlackJackCard.CLUBS,10));
		hand.addCard(deck.dealTopCard());
		System.out.println(hand.getClass());
		
		System.out.println("\nAll the cards on hand are: " + hand.getCardsOnHand());
		System.out.println("The value of cards in hand = " + hand.calculateValue());
		
		System.out.println("Is burst? = " + hand.isBurst());
		System.out.println("Is 2Aces? = " + hand.isTwoAce());
		System.out.println("Is BlackJack? = " + hand.isBlackJack());
		
		//two aces
		hand = new BlackJackHand();
		hand.addCard(new BlackJackCard(BlackJackCard.SPADES,BlackJackCard.ACE));
		hand.addCard(new BlackJackCard(BlackJackCard.CLUBS,BlackJackCard.ACE));
		//hand.addCard(deck.dealTopCard());
		System.out.println(hand.getClass());
		
		System.out.println("\nAll the cards on hand are: " + hand.getCardsOnHand());
		System.out.println("The value of cards in hand = " + hand.calculateValue());
		
		System.out.println("Is burst? = " + hand.isBurst());
		System.out.println("Is 2Aces? = " + hand.isTwoAce());
		System.out.println("Is BlackJack? = " + hand.isBlackJack());
	
		//blackjack
		hand = new BlackJackHand();
		hand.addCard(new BlackJackCard(BlackJackCard.SPADES,BlackJackCard.JACK));
		hand.addCard(new BlackJackCard(BlackJackCard.CLUBS,BlackJackCard.ACE));
		//hand.addCard(deck.dealTopCard());
		System.out.println(hand.getClass());
		
		System.out.println("\nAll the cards on hand are: " + hand.getCardsOnHand());
		System.out.println("The value of cards in hand = " + hand.calculateValue());
		
		System.out.println("Is burst? = " + hand.isBurst());
		System.out.println("Is 2Aces? = " + hand.isTwoAce());
		System.out.println("Is BlackJack? = " + hand.isBlackJack());
		
		ScoreBoard sb = new ScoreBoard(4);
		sb.setWinner(4);
		sb.setWinner(4);
		sb.setWinner(4);
		sb.setWinner(4);
		sb.setWinner(4);
		sb.setWinner(5);
		
		System.out.println("Scores:" + sb.getResults());
		
		
		/*BlackJackGame game = new BlackJackGame();
		Player player = new Player();
		Vector <Player> v = new Vector<Player>();
		v.add(player);
		
		
		System.out.println(v.elementAt(0).toString());*/
		
	}

}
