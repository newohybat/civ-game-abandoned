package cz.muni.fi.civ.newohybat.game.init;

import javax.enterprise.inject.Produces;
import javax.transaction.TransactionManager;

import bitronix.tm.TransactionManagerServices;

public class TransactionManagerFactory {
	@Produces
	public TransactionManager getTransactionManager(){
		return TransactionManagerServices.getTransactionManager();
	}
}
