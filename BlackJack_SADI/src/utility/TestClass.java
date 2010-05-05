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
		
		
		hand.addCard(new BlackJackCard(1,1));
		hand.addCard(new BlackJackCard(2,1));
		hand.addCard(deck.dealTopCard());
		System.out.println(hand.getClass());
		//hand.addCard(new BlackJackCard(3,1));
		
		System.out.println("All the cards on hand are: " + hand.getCardsOnHand());
		System.out.println("The value of cards in hand = " + hand.calculateValue());
		
		System.out.println("Is burst? = " + hand.isBurst());
		
		BlackJackGame game = new BlackJackGame();
		Player player = new Player();
		Vector <Player> v = new Vector<Player>();
		v.add(player);
		
		
		System.out.println(v.elementAt(0).toString());
		
	}

}